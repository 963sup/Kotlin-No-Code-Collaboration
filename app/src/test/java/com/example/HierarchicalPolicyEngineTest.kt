package com.example

import com.example.data.model.ArtifactType
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.GranteeType
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.engine.HierarchicalPolicyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HierarchicalPolicyEngineTest {

    private lateinit var enterprise: Enterprise
    private lateinit var org: Organization
    private lateinit var team: Team
    private lateinit var userAdmin: User
    private lateinit var userAuthor: User
    private lateinit var userReviewer: User
    private lateinit var repoOrg: Repository

    @Before
    fun setup() {
        enterprise = Enterprise(
            id = "ent-1",
            name = "TestCorp",
            slug = "testcorp",
            description = "Test Corporation Global"
        )
        org = Organization(
            id = "org-1",
            enterpriseId = "ent-1",
            name = "Engineering",
            slug = "engineering",
            description = "Engineering Organization",
            defaultMemberRole = RepoRole.COLLABORATOR
        )
        team = Team(
            id = "team-1",
            orgId = "org-1",
            name = "DevOps",
            slug = "devops",
            description = "DevOps Team"
        )

        userAdmin = User(
            id = "u-admin",
            enterpriseId = "ent-1",
            username = "admin",
            displayName = "Admin User",
            email = "admin@testcorp.internal",
            title = "Enterprise Administrator",
            isEnterpriseAdmin = true
        )
        userAuthor = User(
            id = "u-author",
            enterpriseId = "ent-1",
            username = "author",
            displayName = "Author User",
            email = "author@testcorp.internal",
            title = "Staff Engineer"
        )
        userReviewer = User(
            id = "u-reviewer",
            enterpriseId = "ent-1",
            username = "reviewer",
            displayName = "Reviewer User",
            email = "reviewer@testcorp.internal",
            title = "Principal Architect"
        )

        repoOrg = Repository(
            id = "repo-1",
            enterpriseId = "ent-1",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = "org-1",
            name = "infra-core",
            displayName = "Infrastructure Core",
            ownerDisplayName = "Engineering Org",
            description = "Infrastructure repo",
            requiredApproverCount = 2,
            requireReviewerPass = true,
            preventSelfApproval = true
        )
    }

    @Test
    fun `test enterprise admin gets Maintainer role oversight`() {
        val (role, source) = HierarchicalPolicyEngine.resolveEffectiveRole(
            actor = userAdmin,
            repo = repoOrg,
            orgMemberships = emptyList(),
            teamMemberships = emptyList(),
            teams = emptyList(),
            accessRules = emptyList()
        )

        assertEquals(RepoRole.MAINTAINER, role)
        assertTrue(source.contains("Enterprise Admin"))
    }

    @Test
    fun `test direct repo rule overrides org default`() {
        val directRule = RepoAccessRule(
            id = "r1",
            repoId = repoOrg.id,
            granteeType = GranteeType.USER,
            granteeId = userAuthor.id,
            granteeName = userAuthor.displayName,
            role = RepoRole.MAINTAINER,
            grantedByUserId = userAdmin.id
        )

        val (role, source) = HierarchicalPolicyEngine.resolveEffectiveRole(
            actor = userAuthor,
            repo = repoOrg,
            orgMemberships = listOf(OrgMembership(id = "om-1", orgId = org.id, userId = userAuthor.id, role = OrgRole.MEMBER)),
            teamMemberships = emptyList(),
            teams = emptyList(),
            accessRules = listOf(directRule)
        )

        assertEquals(RepoRole.MAINTAINER, role)
        assertTrue(source.contains("Direct Repository Role Assignment"))
    }

    @Test
    fun `test segregation of duties prevents author from approving own artifact`() {
        val artifact = NoCodeArtifact(
            id = "art-1",
            repoId = repoOrg.id,
            title = "Config Spec",
            type = ArtifactType.SPECIFICATION_DOC,
            summary = "Summary",
            structuredContent = "{}",
            authorUserId = userAuthor.id,
            authorDisplayName = userAuthor.displayName,
            lifecycleState = LifecycleState.IN_REVIEW
        )

        val eval = HierarchicalPolicyEngine.evaluateAction(
            enterprise = enterprise,
            actor = userAuthor,
            repo = repoOrg,
            artifact = artifact,
            reviews = emptyList(),
            approvals = emptyList(),
            orgMemberships = emptyList(),
            teamMemberships = emptyList(),
            teams = emptyList(),
            accessRules = listOf(
                RepoAccessRule(
                    id = "r1",
                    repoId = repoOrg.id,
                    granteeType = GranteeType.USER,
                    granteeId = userAuthor.id,
                    granteeName = userAuthor.displayName,
                    role = RepoRole.APPROVER,
                    grantedByUserId = userAdmin.id
                )
            ),
            action = GovernanceAction.SUBMIT_FINAL_APPROVAL
        )

        assertEquals(PolicyVerdict.DENIED_SELF_APPROVAL_PROHIBITED, eval.verdict)
        assertTrue(eval.finalExplanation.contains("Segregation of Duties"))
    }

    @Test
    fun `test independent reviewer can submit review`() {
        val artifact = NoCodeArtifact(
            id = "art-1",
            repoId = repoOrg.id,
            title = "Config Spec",
            type = ArtifactType.SPECIFICATION_DOC,
            summary = "Summary",
            structuredContent = "{}",
            authorUserId = userAuthor.id,
            authorDisplayName = userAuthor.displayName,
            lifecycleState = LifecycleState.IN_REVIEW
        )

        val eval = HierarchicalPolicyEngine.evaluateAction(
            enterprise = enterprise,
            actor = userReviewer,
            repo = repoOrg,
            artifact = artifact,
            reviews = emptyList(),
            approvals = emptyList(),
            orgMemberships = emptyList(),
            teamMemberships = emptyList(),
            teams = emptyList(),
            accessRules = listOf(
                RepoAccessRule(
                    id = "r2",
                    repoId = repoOrg.id,
                    granteeType = GranteeType.USER,
                    granteeId = userReviewer.id,
                    granteeName = userReviewer.displayName,
                    role = RepoRole.REVIEWER,
                    grantedByUserId = userAdmin.id
                )
            ),
            action = GovernanceAction.SUBMIT_REVIEW
        )

        assertEquals(PolicyVerdict.ALLOWED, eval.verdict)
    }

    @Test
    fun `test collaborators can create issues and discussions`() {
        val userCollab = User(
            id = "u-collab",
            enterpriseId = "ent-1",
            username = "collab",
            displayName = "Collaborator",
            email = "collab@testcorp.internal",
            title = "Software Engineer"
        )

        val (role, _) = HierarchicalPolicyEngine.resolveEffectiveRole(
            actor = userCollab,
            repo = repoOrg,
            orgMemberships = emptyList(),
            teamMemberships = emptyList(),
            teams = emptyList(),
            accessRules = listOf(
                RepoAccessRule(
                    id = "r3",
                    repoId = repoOrg.id,
                    granteeType = GranteeType.USER,
                    granteeId = userCollab.id,
                    granteeName = userCollab.displayName,
                    role = RepoRole.COLLABORATOR,
                    grantedByUserId = userAdmin.id
                )
            )
        )

        assertTrue(role.canPerform(RepoRole.COLLABORATOR))
        assertFalse(role.canPerform(RepoRole.MAINTAINER))
    }
}
