package com.example.ui.screens

import com.example.data.model.GranteeType
import com.example.data.model.RepoIssue
import com.example.data.model.TeamMembership
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkProjectionTest {

    @Test
    fun `mine includes direct and active-team assignments only`() {
        val issues = listOf(
            issue("direct", "repo-a", 1, GranteeType.USER, "user-me"),
            issue("team", "repo-b", 2, GranteeType.TEAM, "team-alpha"),
            issue("other-user", "repo-a", 3, GranteeType.USER, "user-other"),
            issue("other-team", "repo-b", 4, GranteeType.TEAM, "team-beta"),
            issue("unassigned", "repo-b", 5, null, null)
        )
        val memberships = listOf(
            TeamMembership(id = "tm-me", teamId = "team-alpha", userId = "user-me"),
            TeamMembership(id = "tm-other", teamId = "team-beta", userId = "user-other")
        )

        val projected = projectWorkIssues(
            allIssues = issues,
            activeUserId = "user-me",
            teamMemberships = memberships,
            assignmentScope = WorkAssignmentScope.MINE,
            repositoryId = null
        )

        assertEquals(listOf("direct", "team"), projected.map { it.id })
    }

    @Test
    fun `repository filter narrows the assignment projection`() {
        val issues = listOf(
            issue("repo-a-direct", "repo-a", 1, GranteeType.USER, "user-me"),
            issue("repo-b-direct", "repo-b", 2, GranteeType.USER, "user-me")
        )

        val projected = projectWorkIssues(
            allIssues = issues,
            activeUserId = "user-me",
            teamMemberships = emptyList(),
            assignmentScope = WorkAssignmentScope.MINE,
            repositoryId = "repo-b"
        )

        assertEquals(listOf("repo-b-direct"), projected.map { it.id })
    }

    @Test
    fun `all work preserves accessible issues including unassigned records`() {
        val issues = listOf(
            issue("direct", "repo-a", 1, GranteeType.USER, "user-me"),
            issue("unassigned", "repo-b", 2, null, null),
            issue("other", "repo-b", 3, GranteeType.USER, "user-other")
        )

        val projected = projectWorkIssues(
            allIssues = issues,
            activeUserId = "user-me",
            teamMemberships = emptyList(),
            assignmentScope = WorkAssignmentScope.ALL,
            repositoryId = null
        )

        assertEquals(issues.map { it.id }, projected.map { it.id })
    }

    @Test
    fun `mine is empty when no active user exists`() {
        val projected = projectWorkIssues(
            allIssues = listOf(issue("direct", "repo-a", 1, GranteeType.USER, "user-me")),
            activeUserId = null,
            teamMemberships = emptyList(),
            assignmentScope = WorkAssignmentScope.MINE,
            repositoryId = null
        )

        assertTrue(projected.isEmpty())
    }

    private fun issue(
        id: String,
        repoId: String,
        issueNumber: Int,
        assigneeType: GranteeType?,
        assigneeId: String?
    ) = RepoIssue(
        id = id,
        repoId = repoId,
        issueNumber = issueNumber,
        title = id,
        description = "",
        authorUserId = "author",
        authorDisplayName = "Author",
        assigneeType = assigneeType,
        assigneeId = assigneeId,
        assigneeName = assigneeId
    )
}
