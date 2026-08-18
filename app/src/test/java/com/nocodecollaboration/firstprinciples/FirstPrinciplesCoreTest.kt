package com.nocodecollaboration.firstprinciples

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstPrinciplesCoreTest {
    @Test
    fun wbsRollsUpWeightedChildrenAndKeepsStableNumbering() {
        val nodes = listOf(
            WbsIssueProjection(
                issueId = "root",
                repositoryId = "repo",
                parentIssueId = null,
                siblingOrder = 0,
            ),
            WbsIssueProjection(
                issueId = "a",
                repositoryId = "repo",
                parentIssueId = "root",
                siblingOrder = 0,
                weight = 1.0,
                directProgressPercent = 100,
            ),
            WbsIssueProjection(
                issueId = "b",
                repositoryId = "repo",
                parentIssueId = "root",
                siblingOrder = 1,
                weight = 3.0,
                directProgressPercent = 0,
            ),
        )

        assertEquals(25, WbsProjection.rollUp(nodes)["root"])
        assertEquals("1", WbsProjection.numbering(nodes)["root"])
        assertEquals("1.2", WbsProjection.numbering(nodes)["b"])
    }

    @Test
    fun deniedIssueNeverFallsBackToItsRepository() {
        val requested = CollaborationTarget.Issue(
            stableId = "issue-1",
            repositoryId = "repo-1",
        )
        val resolver = SafeTargetResolver(
            existence = TargetExistence { true },
            authorization = TargetAuthorization { _, _ -> false },
        )

        val result = resolver.resolve("user-1", requested)
        assertTrue(result is TargetResolution.Denied)
        assertEquals(requested, (result as TargetResolution.Denied).requested)
    }

    @Test
    fun exploreFiltersUnauthorizedTargetsBeforeDisplay() {
        val allowed = CollaborationTarget.Repository("allowed")
        val denied = CollaborationTarget.Repository("denied")
        val service = ExploreService(
            TargetAuthorization { _, target -> target == allowed },
        )

        val result = service.search(
            actorUserId = "user",
            query = "station",
            candidates = listOf(
                SearchableCollaborationItem(allowed, "A", "station a"),
                SearchableCollaborationItem(denied, "B", "station b"),
            ),
        )

        assertEquals(listOf(allowed), result.map { it.target })
    }

    @Test
    fun governanceAuditEventsNeverProduceFeedTrendingOrAchievements() {
        val event = CollaborationEvent(
            id = "event",
            actorUserId = "user",
            target = CollaborationTarget.User("user"),
            kind = CollaborationEventKind.GOVERNANCE_AUDIT,
            occurredAtEpochMillis = 1L,
            visibleWithinAuthorizedScope = true,
        )

        assertTrue(SocialProjection.visibleFeed(listOf(event)).isEmpty())
        assertTrue(SocialProjection.trending(listOf(event)).isEmpty())

        val projection = SocialProjection.achievements(
            userId = "user",
            events = listOf(event),
            definitions = listOf(
                AchievementDefinition(
                    id = "audit-achievement-must-not-exist",
                    qualifyingKind = CollaborationEventKind.GOVERNANCE_AUDIT,
                    threshold = 1,
                    xp = 100,
                ),
            ),
        )
        assertEquals(0, projection.xp)
        assertTrue(projection.awards.isEmpty())
    }

    @Test
    fun myWorkRequiresBothAssignmentAndRepositoryAccess() {
        val query = MyWorkQuery(
            activeUserId = "u1",
            activeTeamIds = setOf("t1"),
            accessibleRepositoryIds = setOf("r1"),
        )
        val issues = listOf(
            AccessibleIssue("visible-user", "r1", "u1", null, WorkStatus.TODO, "A"),
            AccessibleIssue("visible-team", "r1", null, "t1", WorkStatus.TODO, "B"),
            AccessibleIssue("hidden-repo", "r2", "u1", null, WorkStatus.TODO, "C"),
            AccessibleIssue("not-assigned", "r1", "u2", null, WorkStatus.TODO, "D"),
        )

        assertEquals(
            listOf("visible-user", "visible-team"),
            MyWorkProjector.group(query, issues)[WorkStatus.TODO]?.map { it.issueId },
        )
    }

    @Test
    fun retryBackoffIsBounded() {
        assertEquals(1L, RetryPolicy.nextDelaySeconds(0))
        assertEquals(900L, RetryPolicy.nextDelaySeconds(100))
    }
}
