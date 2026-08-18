package com.example

import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueHierarchyRulesTest {
    private fun issue(
        id: String,
        number: Int,
        parentId: String? = null,
        repoId: String = "repo-1",
        status: IssueStatus = IssueStatus.OPEN
    ): RepoIssue = RepoIssue(
        id = id,
        repoId = repoId,
        issueNumber = number,
        title = "任務 $number",
        description = "",
        status = status,
        priority = IssuePriority.MEDIUM,
        authorUserId = "user-1",
        authorDisplayName = "使用者",
        parentIssueId = parentId
    )

    @Test
    fun `supports arbitrary nested task depth`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("d", 4, "c"))
        assertEquals(setOf("b", "c", "d"), IssueHierarchyRules.descendantIds("a", issues))
        assertEquals(3, IssueHierarchyRules.depthOf("d", issues))
        assertEquals(listOf(0, 1, 2, 3), IssueHierarchyRules.orderedForDisplay(issues).map { it.second })
    }

    @Test
    fun `rejects self descendant and cross repository parent`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("x", 9, repoId = "repo-2"))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "a", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "b", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "c", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "x", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", "a", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", null, issues))
    }

    @Test
    fun `projects deterministic WBS codes and subtree progress`() {
        val issues = listOf(
            issue("a", 10),
            issue("b", 20, "a", status = IssueStatus.CLOSED),
            issue("c", 30, "a"),
            issue("d", 40, "c", status = IssueStatus.CLOSED),
            issue("e", 50, status = IssueStatus.CLOSED)
        )

        val rows = IssueHierarchyRules.wbsProjection(issues)
        assertEquals(listOf("1", "1.1", "1.2", "1.2.1", "2"), rows.map { it.code })
        assertEquals(listOf("a", "b", "c", "d", "e"), rows.map { it.issue.id })
        assertEquals(2, rows.first { it.issue.id == "a" }.completedCount)
        assertEquals(4, rows.first { it.issue.id == "a" }.totalCount)
        assertEquals(0.5f, rows.first { it.issue.id == "a" }.progress, 0.001f)
        assertEquals(0.5f, rows.first { it.issue.id == "c" }.progress, 0.001f)
        assertEquals(1f, rows.first { it.issue.id == "e" }.progress, 0.001f)
        assertEquals(0.6f, IssueHierarchyRules.overallProgress(issues), 0.001f)
    }

    @Test
    fun `keeps orphan and cyclic records visible exactly once`() {
        val issues = listOf(
            issue("a", 1, "b"),
            issue("b", 2, "a"),
            issue("c", 3, "missing")
        )

        val rows = IssueHierarchyRules.wbsProjection(issues)
        assertEquals(3, rows.size)
        assertEquals(setOf("a", "b", "c"), rows.map { it.issue.id }.toSet())
        assertEquals(3, rows.map { it.issue.id }.distinct().size)
        assertFalse(IssueHierarchyRules.descendantIds("a", issues).contains("a"))
    }
}
