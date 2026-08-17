package com.example

import com.example.data.model.Enterprise
import com.example.data.model.GranteeType
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileIntegrationTest {

    private lateinit var enterprise: Enterprise
    private lateinit var orgCore: Organization
    private lateinit var orgFintech: Organization
    private lateinit var teamCore: Team
    private lateinit var userAlice: User
    private lateinit var repoOrg: Repository
    private lateinit var repoPersonal: Repository

    @Before
    fun setup() {
        enterprise = Enterprise(
            id = "ent-test",
            name = "Nexus Enterprise",
            slug = "nexus",
            description = "Root Governance Entity"
        )
        orgCore = Organization(
            id = "org-core",
            enterpriseId = enterprise.id,
            name = "Platform Core",
            slug = "platform-core",
            description = "Core systems"
        )
        orgFintech = Organization(
            id = "org-fintech",
            enterpriseId = enterprise.id,
            name = "Fintech Division",
            slug = "fintech",
            description = "Financial technologies"
        )
        teamCore = Team(
            id = "team-arch",
            orgId = orgCore.id,
            name = "Architecture Review Board",
            slug = "arch-board",
            description = "Enterprise architecture review"
        )

        userAlice = User(
            id = "usr-alice",
            enterpriseId = enterprise.id,
            username = "alice.chen",
            displayName = "Alice Chen",
            email = "alice@nexus.internal",
            title = "Principal Solutions Architect",
            isEnterpriseAdmin = true,
            canOwnerRepository = true,
            bio = "Enterprise systems architect and governance lead",
            location = "San Francisco, CA",
            pronouns = "she/her",
            ssoProvider = "Okta SAML 2.0",
            authStatus = "Federated & Enforced",
            securityKeyEnforced = true,
            twoFactorEnabled = true
        )

        repoPersonal = Repository(
            id = "repo-alice-sandbox",
            name = "arch-blueprints",
            displayName = "Architecture Blueprints",
            ownerType = OwnerType.USER,
            ownerId = userAlice.id,
            ownerDisplayName = userAlice.displayName,
            enterpriseId = enterprise.id,
            description = "Alice's personal architecture repository"
        )

        repoOrg = Repository(
            id = "repo-org-contracts",
            name = "governance-schemas",
            displayName = "Enterprise Governance Schemas",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgCore.id,
            ownerDisplayName = orgCore.name,
            enterpriseId = enterprise.id,
            description = "Shared schemas"
        )
    }

    @Test
    fun testUserProfileCentralizedIdentityProperties() {
        assertEquals("usr-alice", userAlice.id)
        assertEquals("alice.chen", userAlice.username)
        assertEquals("Alice Chen", userAlice.displayName)
        assertEquals("Principal Solutions Architect", userAlice.title)
        assertTrue(userAlice.isEnterpriseAdmin)
        assertTrue(userAlice.canOwnerRepository)
        assertEquals("Okta SAML 2.0", userAlice.ssoProvider)
        assertEquals("Federated & Enforced", userAlice.authStatus)
        assertTrue(userAlice.twoFactorEnabled)
        assertTrue(userAlice.securityKeyEnforced)
    }

    @Test
    fun testUserProfileOrganizationalAndTeamMembershipsMapping() {
        val orgMemberships = listOf(
            OrgMembership(orgId = orgCore.id, userId = userAlice.id, role = OrgRole.OWNER),
            OrgMembership(orgId = orgFintech.id, userId = userAlice.id, role = OrgRole.ADMIN)
        )
        val teamMemberships = listOf(
            TeamMembership(teamId = teamCore.id, userId = userAlice.id, role = TeamRole.MAINTAINER)
        )

        val userOrgs = orgMemberships.filter { it.userId == userAlice.id }
        assertEquals(2, userOrgs.size)
        assertTrue(userOrgs.any { it.orgId == orgCore.id && it.role == OrgRole.OWNER })
        assertTrue(userOrgs.any { it.orgId == orgFintech.id && it.role == OrgRole.ADMIN })

        val userTeams = teamMemberships.filter { it.userId == userAlice.id }
        assertEquals(1, userTeams.size)
        assertEquals(TeamRole.MAINTAINER, userTeams.first().role)
    }

    @Test
    fun testUserProfileRepositoryOwnershipAndCollaboratorGrants() {
        val allRepos = listOf(repoPersonal, repoOrg)
        val directRules = listOf(
            RepoAccessRule(
                repoId = repoOrg.id,
                granteeType = GranteeType.USER,
                granteeId = userAlice.id,
                granteeName = userAlice.displayName,
                role = RepoRole.APPROVER,
                grantedByUserId = "usr-sys"
            )
        )

        // Owned repos
        val ownedRepos = allRepos.filter { it.ownerType == OwnerType.USER && it.ownerId == userAlice.id }
        assertEquals(1, ownedRepos.size)
        assertEquals("arch-blueprints", ownedRepos.first().name)

        // Collaborator grants
        val collaboratorRules = directRules.filter { it.granteeType == GranteeType.USER && it.granteeId == userAlice.id }
        assertEquals(1, collaboratorRules.size)
        assertEquals(RepoRole.APPROVER, collaboratorRules.first().role)
    }
}
