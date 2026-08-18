package com.example.navigation

import com.example.data.model.AppNotification
import com.example.data.model.GranteeType
import com.example.data.model.NotificationCategory
import com.example.data.model.OrgMembership
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.Repository
import com.example.data.model.TeamMembership
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollaborationTargetResolverTest {
    @Test
    fun explicitIssueActionDoesNotFallBackToLinkedArtifact() {
        val notification = notification(
  actionType = "VIEW_ISSUE",
  repoId = "repo_1",
  issueId = "issue_1",
  artifactId = "artifact_linked"
        )

        assertEquals(
  CollaborationTarget.Issue("repo_1", "issue_1"),
  CollaborationTargetResolver.resolve(notification)
        )
    }

    @Test
    fun resolvesDiscussionTarget() {
        assertEquals(
  CollaborationTarget.Discussion("repo_1", "discussion_1"),
  CollaborationTargetResolver.resolve(
      notification(
          actionType = "VIEW_DISCUSSION",
          repoId = "repo_1",
          discussionId = "discussion_1"
      )
  )
        )
    }

    @Test
    fun reviewActionResolvesArtifactTarget() {
        assertEquals(
  CollaborationTarget.Artifact("repo_1", "artifact_1"),
  CollaborationTargetResolver.resolve(
      notification(actionType = "REVIEW", repoId = "repo_1", artifactId = "artifact_1")
  )
        )
    }

    @Test
    fun resolvesRepositoryFallback() {
        assertEquals(
  CollaborationTarget.Repository("repo_1"),
  CollaborationTargetResolver.resolve(notification(actionType = null, repoId = "repo_1"))
        )
    }

    @Test
    fun unknownActionUsesMostSpecificReference() {
        assertEquals(
  CollaborationTarget.Issue("repo_1", "issue_1"),
  CollaborationTargetResolver.resolve(
      notification(
          actionType = "CUSTOM_ACTION",
          repoId = "repo_1",
          issueId = "issue_1",
          artifactId = "artifact_1"
      )
  )
        )
    }

    @Test
    fun explicitIssueActionWithMissingIssueDoesNotOpenRepository() {
        assertNull(
  CollaborationTargetResolver.resolve(
      notification(actionType = "VIEW_ISSUE", repoId = "repo_1", issueId = null)
  )
        )
    }

    @Test
    fun blankIdentifiersAreRejected() {
        assertNull(
  CollaborationTargetResolver.resolve(
      notification(actionType = "VIEW_REPO", repoId = "   ")
  )
        )
    }

    @Test
    fun resolvesTeamAndUserProfileTargetsOnlyWhenExplicit() {
        assertEquals(
  CollaborationTarget.Team("team_1"),
  CollaborationTargetResolver.resolve(notification(actionType = "VIEW_TEAM", teamId = "team_1"))
        )
        assertEquals(
  CollaborationTarget.UserProfile("actor_1"),
  CollaborationTargetResolver.resolve(notification(actionType = "VIEW_PROFILE"))
        )
    }

    private fun notification(
        actionType: String?,
        repoId: String? = null,
        artifactId: String? = null,
        issueId: String? = null,
        discussionId: String? = null,
        teamId: String? = null
    ) = AppNotification(
        id = "notification_1",
        recipientUserId = "recipient_1",
        actorUserId = "actor_1",
        actorDisplayName = "Actor",
        category = NotificationCategory.ISSUE_ASSIGNMENT,
        title = "Target notification",
        body = "Open target",
        isActionable = true,
        actionType = actionType,
        repoId = repoId,
        artifactId = artifactId,
        issueId = issueId,
        discussionId = discussionId,
        teamId = teamId
    )
}

class CollaborationTargetAccessTest {
    private val user = User(
        id = "user_1",
        enterpriseId = "enterprise_1",
        username = "worker",
        displayName = "Worker",
        email = "worker@example.com",
        title = "Engineer"
    )

    @Test
    fun repositoryOwnerCanOpenPersonalRepository() {
        assertTrue(
  CollaborationTargetAccess.canOpenRepository(
      user = user,
      repository = repository(ownerType = OwnerType.USER, ownerId = user.id),
      orgMemberships = emptyList(),
      teamMemberships = emptyList(),
      accessRules = emptyList()
  )
        )
    }

    @Test
    fun organizationMembershipGrantsOrganizationRepositoryAccess() {
        assertTrue(
  CollaborationTargetAccess.canOpenRepository(
      user = user,
      repository = repository(ownerType = OwnerType.ORGANIZATION, ownerId = "org_1"),
      orgMemberships = listOf(OrgMembership(orgId = "org_1", userId = user.id)),
      teamMemberships = emptyList(),
      accessRules = emptyList()
  )
        )
    }

    @Test
    fun directAndTeamRulesGrantAccess() {
        val repo = repository(ownerType = OwnerType.USER, ownerId = "other_user")
        assertTrue(
  CollaborationTargetAccess.canOpenRepository(
      user = user,
      repository = repo,
      orgMemberships = emptyList(),
      teamMemberships = emptyList(),
      accessRules = listOf(
          RepoAccessRule(
              repoId = repo.id,
              granteeType = GranteeType.USER,
              granteeId = user.id,
              granteeName = user.displayName,
              role = com.example.data.model.RepoRole.VIEWER,
              grantedByUserId = "owner"
          )
      )
  )
        )

        assertTrue(
  CollaborationTargetAccess.canOpenRepository(
      user = user,
      repository = repo,
      orgMemberships = emptyList(),
      teamMemberships = listOf(TeamMembership(teamId = "team_1", userId = user.id)),
      accessRules = listOf(
          RepoAccessRule(
              repoId = repo.id,
              granteeType = GranteeType.TEAM,
              granteeId = "team_1",
              granteeName = "Team One",
              role = com.example.data.model.RepoRole.VIEWER,
              grantedByUserId = "owner"
          )
      )
  )
        )
    }

    @Test
    fun unrelatedAndCrossEnterpriseRepositoriesAreDenied() {
        val unrelated = repository(ownerType = OwnerType.USER, ownerId = "other_user")
        assertFalse(
  CollaborationTargetAccess.canOpenRepository(
      user = user,
      repository = unrelated,
      orgMemberships = emptyList(),
      teamMemberships = emptyList(),
      accessRules = emptyList()
  )
        )

        assertFalse(
  CollaborationTargetAccess.canOpenRepository(
      user = user.copy(isEnterpriseAdmin = true),
      repository = unrelated.copy(enterpriseId = "enterprise_2"),
      orgMemberships = emptyList(),
      teamMemberships = emptyList(),
      accessRules = emptyList()
  )
        )
    }

    private fun repository(ownerType: OwnerType, ownerId: String) = Repository(
        id = "repo_1",
        name = "repo-one",
        displayName = "Repository One",
        ownerType = ownerType,
        ownerId = ownerId,
        ownerDisplayName = ownerId,
        enterpriseId = "enterprise_1",
        description = "Test repository"
    )
}
