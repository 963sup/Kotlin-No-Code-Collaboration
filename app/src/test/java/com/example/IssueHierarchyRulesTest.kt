package com.example

import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.RepoIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueHierarchyRulesTest {
    private fun issue(id: String, number: Int, parentId: String? = null, repoId: String = "repo-1"): RepoIssue = RepoIssue(
        id = id, repoId = repoId, issueNumber = number, title = "任務 $number", description = "",
        priority = IssuePriority.MEDIUM, authorUserId = "user-1", authorDisplayName = "使用者", parentIssueId = parentId
    )

    @Test fun `supports arbitrary nested task depth`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("d", 4, "c"))
        assertEquals(setOf("b", "c", "d"), IssueHierarchyRules.descendantIds("a", issues))
        assertEquals(3, IssueHierarchyRules.depthOf("d", issues))
        assertEquals(listOf(0, 1, 2, 3), IssueHierarchyRules.orderedForDisplay(issues).map { it.second })
    }

    @Test fun `rejects self descendant and cross repository parent`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("x", 9, repoId = "repo-2"))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "a", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "b", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "c", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "x", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", "a", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", null, issues))
    }
}
