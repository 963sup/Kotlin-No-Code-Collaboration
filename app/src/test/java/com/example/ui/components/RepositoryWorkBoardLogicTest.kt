package com.example.ui.components

import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryWorkBoardLogicTest {

    @Test
    fun `nested tasks preserve arbitrary hierarchy depth`() {
        val root = issue(id = "root", number = 1)
        val child = issue(id = "child", number = 2, parentId = root.id)
        val grandchild = issue(id = "grandchild", number = 3, parentId = child.id)
        val greatGrandchild = issue(id = "great-grandchild", number = 4, parentId = grandchild.id)

        val rows = flattenNestedTasks(listOf(greatGrandchild, child, root, grandchild))

        assertEquals(listOf("root", "child", "grandchild", "great-grandchild"), rows.map { it.issue.id })
        assertEquals(listOf(0, 1, 2, 3), rows.map { it.depth })
    }

    @Test
    fun `orphan and cyclic tasks stay visible exactly once`() {
        val orphan = issue(id = "orphan", number = 1, parentId = "missing")
        val cycleA = issue(id = "cycle-a", number = 2, parentId = "cycle-b")
        val cycleB = issue(id = "cycle-b", number = 3, parentId = "cycle-a")

        val rows = flattenNestedTasks(listOf(cycleB, orphan, cycleA))

        assertEquals(3, rows.size)
        assertEquals(setOf("orphan", "cycle-a", "cycle-b"), rows.map { it.issue.id }.toSet())
        assertEquals(3, rows.map { it.issue.id }.distinct().size)
    }

    @Test
    fun `nested progress counts all descendant levels`() {
        val root = issue(id = "root", number = 1)
        val childDone = issue(id = "child-done", number = 2, parentId = root.id, status = IssueStatus.CLOSED)
        val childOpen = issue(id = "child-open", number = 3, parentId = root.id, status = IssueStatus.OPEN)
        val grandDone = issue(id = "grand-done", number = 4, parentId = childOpen.id, status = IssueStatus.CLOSED)

        val progress = nestedTaskProgress(root.id, listOf(root, childDone, childOpen, grandDone))

        assertEquals(3, progress.total)
        assertEquals(2, progress.closed)
        assertTrue(progress.closed < progress.total)
    }

    private fun issue(
        id: String,
        number: Int,
        parentId: String? = null,
        status: IssueStatus = IssueStatus.OPEN
    ) = RepoIssue(
        id = id,
        repoId = "repo-1",
        issueNumber = number,
        title = "Task $number",
        description = "",
        status = status,
        priority = IssuePriority.MEDIUM,
        authorUserId = "user-1",
        authorDisplayName = "User One",
        parentIssueId = parentId,
        parentIssueNumber = null,
        parentIssueTitle = null,
        labels = "work"
    )
}
