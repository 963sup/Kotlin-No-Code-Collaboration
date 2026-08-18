package com.example.ui.model

import com.example.data.model.AppNotification
import com.example.data.model.ArtifactReview
import com.example.data.model.ArtifactType
import com.example.data.model.GranteeType
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationStatus
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileProductProjectionsTest {
    private val enterpriseId = "enterprise-1"
    private val organization = Organization(
        id = "org-1",
        enterpriseId = enterpriseId,
        name = "工程組織",
        slug = "engineering",
        description = "工程交付"
    )
    private val team = Team(
        id = "team-1",
        orgId = organization.id,
        name = "現場團隊",
        slug = "site-team",
        description = "現場協作"
    )
    private val repository = Repository(
        id = "repo-1",
        name = "station-delivery",
        displayName = "變電站交付",
        ownerType = OwnerType.ORGANIZATION,
        ownerId = organization.id,
        ownerDisplayName = organization.name,
        enterpriseId = enterpriseId,
        description = "無程式碼交付容器"
    )
    private val alice = user("alice", "Alice")
    private val bob = user("bob", "Bob")
    private val charlie = user("charlie", "Charlie")

    @Test
    fun teamSpaceUsesTeamAccessAndIssueProjection() {
        val issues = listOf(
            issue("root", 1, null, IssueStatus.OPEN, 0),
            issue("done", 2, "root", IssueStatus.CLOSED, 100),
            issue("open", 3, "root", IssueStatus.OPEN, 0)
        )
        val summary = TeamSpaceProjection.build(
            team = team,
            users = listOf(alice),
            memberships = listOf(TeamMembership(teamId = team.id, userId = alice.id, role = TeamRole.MAINTAINER)),
            repositories = listOf(repository),
            accessRules = listOf(
                RepoAccessRule(
                    repoId = repository.id,
                    granteeType = GranteeType.TEAM,
                    granteeId = team.id,
                    granteeName = team.name,
                    role = RepoRole.COLLABORATOR,
                    grantedByUserId = bob.id
                )
            ),
            issues = issues
        )

        assertEquals(1, summary.members.size)
        assertEquals(TeamRole.MAINTAINER, summary.members.single().role)
        assertEquals(1, summary.repositories.size)
        assertEquals(3, summary.repositories.single().issueCount)
        assertEquals(0.5f, summary.repositories.single().overallProgress, 0.001f)
    }

    @Test
    fun repositoryOverviewCountsDistinctEffectiveMembers() {
        val rules = listOf(
            RepoAccessRule(
                repoId = repository.id,
                granteeType = GranteeType.USER,
                granteeId = bob.id,
                granteeName = bob.displayName,
                role = RepoRole.REVIEWER,
                grantedByUserId = alice.id
            ),
            RepoAccessRule(
                repoId = repository.id,
                granteeType = GranteeType.TEAM,
                granteeId = team.id,
                granteeName = team.name,
                role = RepoRole.COLLABORATOR,
                grantedByUserId = alice.id
            )
        )
        val summary = RepositoryOverviewProjection.build(
            repository = repository,
            issues = listOf(issue("root", 1, null, IssueStatus.CLOSED, 100)),
            accessRules = rules,
            orgMemberships = listOf(OrgMembership(orgId = organization.id, userId = alice.id)),
            teamMemberships = listOf(
                TeamMembership(teamId = team.id, userId = bob.id),
                TeamMembership(teamId = team.id, userId = charlie.id)
            ),
            auditLogs = emptyList()
        )

        assertEquals(1, summary.wbsCount)
        assertEquals(1, summary.rootWbsCount)
        assertEquals(3, summary.memberCount)
        assertEquals(1f, summary.overallProgress, 0.001f)
    }

    @Test
    fun inboxPrimaryViewsUseRequestedSemantics() {
        val mention = notification("mention", NotificationCategory.MENTION_AND_REPLY, NotificationStatus.READ)
        val actionable = notification("action", NotificationCategory.REVIEW_REQUEST, NotificationStatus.READ, actionable = true)
        val system = notification("system", NotificationCategory.GOVERNANCE_EVENT, NotificationStatus.READ)
        val unread = notification("unread", NotificationCategory.PUBLICATION, NotificationStatus.UNREAD)
        val archived = notification("archived", NotificationCategory.GOVERNANCE_EVENT, NotificationStatus.ARCHIVED)

        assertTrue(mention.matches(InboxPrimaryView.MENTIONS_OR_ACTION))
        assertTrue(actionable.matches(InboxPrimaryView.MENTIONS_OR_ACTION))
        assertTrue(system.matches(InboxPrimaryView.SYSTEM))
        assertTrue(unread.matches(InboxPrimaryView.SYSTEM))
        assertTrue(unread.matches(InboxPrimaryView.UNREAD))
        assertFalse(archived.matches(InboxPrimaryView.ALL))
        assertFalse(archived.matches(InboxPrimaryView.SYSTEM))
    }

    @Test
    fun followingActivityOnlySurfacesAllowedVisibleEvents() {
        val follows = listOf(UserFollow(followerUserId = alice.id, followedUserId = bob.id))
        val visible = audit("public", bob, "CREATE_ISSUE", PolicyVerdict.ALLOWED, repository.id)
        val denied = audit("denied", bob, "CREATE_ISSUE", PolicyVerdict.DENIED_INSUFFICIENT_ROLE, repository.id)
        val privateAction = audit("private", bob, "DELETE_REPOSITORY", PolicyVerdict.ALLOWED, repository.id)
        val otherUser = audit("other", charlie, "CREATE_ISSUE", PolicyVerdict.ALLOWED, repository.id)
        val hiddenRepo = audit("hidden", bob, "CREATE_ISSUE", PolicyVerdict.ALLOWED, "repo-hidden")

        val activity = FollowingActivityProjection.build(
            activeUserId = alice.id,
            follows = follows,
            auditLogs = listOf(visible, denied, privateAction, otherUser, hiddenRepo),
            visibleRepositoryIds = setOf(repository.id)
        )

        assertEquals(1, activity.size)
        assertEquals(bob.id, activity.single().actorUserId)
        assertEquals("CREATE_ISSUE", activity.single().actionName)
    }

    @Test
    fun savedProjectionGroupsCanonicalTargets() {
        val saved = listOf(
            SavedTarget(
                userId = alice.id,
                targetKey = "REPOSITORY::${repository.id}",
                targetType = "REPOSITORY",
                targetId = repository.id
            ),
            SavedTarget(
                userId = alice.id,
                targetKey = "ARTIFACT:${repository.id}:artifact-1",
                targetType = "ARTIFACT",
                targetId = "artifact-1",
                repositoryId = repository.id
            ),
            SavedTarget(
                userId = alice.id,
                targetKey = "DISCUSSION:${repository.id}:discussion-1",
                targetType = "DISCUSSION",
                targetId = "discussion-1",
                repositoryId = repository.id
            )
        )

        val groups = SavedProjection.build(alice.id, saved)

        assertEquals(listOf(SavedGroup.REPOSITORIES, SavedGroup.DOCUMENTS, SavedGroup.DISCUSSIONS), groups.map { it.group })
        assertEquals(CollaborationTarget.Repository(repository.id), groups.first().targets.single())
    }

    @Test
    fun achievementsAreDeterministicAndEvidenceDerived() {
        val completed = issue("closed", 1, null, IssueStatus.CLOSED, 100).copy(
            assigneeType = GranteeType.USER,
            assigneeId = alice.id,
            closedByUserId = alice.id
        )
        val review = ArtifactReview(
            artifactId = "artifact-1",
            reviewerUserId = alice.id,
            reviewerDisplayName = alice.displayName,
            decision = ReviewDecision.APPROVED,
            feedbackNote = "符合要求"
        )
        val artifact = NoCodeArtifact(
            id = "artifact-1",
            repoId = repository.id,
            title = "交付文件",
            type = ArtifactType.SPECIFICATION_DOC,
            summary = "已驗證成果",
            structuredContent = "{}",
            lifecycleState = LifecycleState.PUBLISHED,
            authorUserId = alice.id,
            authorDisplayName = alice.displayName
        )
        val states = AchievementProjection.build(
            user = alice,
            issues = listOf(completed),
            reviews = listOf(review),
            artifacts = listOf(artifact),
            auditLogs = listOf(audit("create", alice, "CREATE_ISSUE", PolicyVerdict.ALLOWED, repository.id)),
            visibleRepositoryIds = setOf(repository.id)
        ).associateBy { it.badge }

        assertTrue(states.getValue(AchievementBadge.FIRST_COMPLETION).unlocked)
        assertTrue(states.getValue(AchievementBadge.FIRST_REVIEW).unlocked)
        assertTrue(states.getValue(AchievementBadge.FIRST_PUBLICATION).unlocked)
        assertFalse(states.getValue(AchievementBadge.RELIABLE_DELIVERY).unlocked)
        assertFalse(states.getValue(AchievementBadge.PUBLIC_COLLABORATOR).unlocked)
    }

    @Test
    fun exploreCategoriesDoNotCreateAnotherSearchArchitecture() {
        val repoResult = ExploreResult(
            target = CollaborationTarget.Repository(repository.id),
            typeLabel = "儲存庫",
            title = repository.displayName,
            subtitle = repository.description,
            searchableText = repository.name,
            score = 1,
            isSaved = false
        )
        val userResult = repoResult.copy(target = CollaborationTarget.UserProfile(alice.id), typeLabel = "用戶")

        assertTrue(repoResult.matches(ExploreCategory.REPOSITORIES))
        assertFalse(repoResult.matches(ExploreCategory.PEOPLE))
        assertTrue(userResult.matches(ExploreCategory.PEOPLE))
    }

    private fun user(id: String, name: String) = User(
        id = id,
        enterpriseId = enterpriseId,
        username = id,
        displayName = name,
        email = "$id@example.com",
        title = "工程師"
    )

    private fun issue(
        id: String,
        number: Int,
        parentId: String?,
        status: IssueStatus,
        progress: Int
    ) = RepoIssue(
        id = id,
        repoId = repository.id,
        issueNumber = number,
        title = "工作 $number",
        description = "工作內容",
        status = status,
        authorUserId = alice.id,
        authorDisplayName = alice.displayName,
        parentIssueId = parentId,
        progressPercent = progress
    )

    private fun notification(
        id: String,
        category: NotificationCategory,
        status: NotificationStatus,
        actionable: Boolean = false
    ) = AppNotification(
        id = id,
        recipientUserId = alice.id,
        actorUserId = bob.id,
        actorDisplayName = bob.displayName,
        category = category,
        status = status,
        title = id,
        body = id,
        isActionable = actionable
    )

    private fun audit(
        id: String,
        actor: User,
        action: String,
        verdict: PolicyVerdict,
        repoId: String
    ) = com.example.data.model.AuditLog(
        id = id,
        enterpriseId = enterpriseId,
        repoId = repoId,
        repoName = repository.displayName,
        actorUserId = actor.id,
        actorDisplayName = actor.displayName,
        actionName = action,
        verdict = verdict,
        reasoning = "test"
    )
}
