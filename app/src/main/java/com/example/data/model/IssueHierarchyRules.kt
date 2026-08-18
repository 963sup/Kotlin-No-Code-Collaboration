package com.example.data.model

/**
 * Pure recursive hierarchy rules for RepoIssue.
 * Nested tasks and WBS rows are projections of Issues, never a second Task entity.
 */
data class WbsProjectionRow(
    val issue: RepoIssue,
    val depth: Int,
    val code: String,
    val completedCount: Int,
    val totalCount: Int,
    val progress: Float
)

object IssueHierarchyRules {
    fun descendantIds(rootIssueId: String, issues: List<RepoIssue>): Set<String> {
        val childrenByParent = issues.groupBy { it.parentIssueId }
        val descendants = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(rootIssueId)
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            childrenByParent[currentId].orEmpty().forEach { child ->
                if (child.id != rootIssueId && descendants.add(child.id)) {
                    queue.addLast(child.id)
                }
            }
        }
        return descendants
    }

    fun canAssignParent(issueId: String, candidateParentId: String?, issues: List<RepoIssue>): Boolean {
        if (candidateParentId == null) return true
        if (candidateParentId == issueId) return false
        val issue = issues.firstOrNull { it.id == issueId } ?: return false
        val candidate = issues.firstOrNull { it.id == candidateParentId } ?: return false
        if (issue.repoId != candidate.repoId) return false
        return candidateParentId !in descendantIds(issueId, issues)
    }

    fun depthOf(issueId: String, issues: List<RepoIssue>): Int {
        val byId = issues.associateBy { it.id }
        val visited = mutableSetOf<String>()
        var depth = 0
        var current = byId[issueId]
        while (current?.parentIssueId != null) {
            val parentId = current.parentIssueId ?: break
            if (!visited.add(parentId)) break
            current = byId[parentId] ?: break
            depth += 1
        }
        return depth
    }

    fun orderedForDisplay(issues: List<RepoIssue>): List<Pair<RepoIssue, Int>> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }
            .mapValues { (_, children) -> children.sortedBy { it.issueNumber } }
        val result = mutableListOf<Pair<RepoIssue, Int>>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int) {
            if (!visited.add(issue.id)) return
            result += issue to depth
            childrenByParent[issue.id].orEmpty().forEach { child -> visit(child, depth + 1) }
        }

        issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }
            .sortedBy { it.issueNumber }
            .forEach { visit(it, 0) }

        // Keep malformed/cyclic records visible once instead of recursing forever.
        issues.sortedBy { it.issueNumber }.forEach { issue ->
            if (issue.id !in visited) visit(issue, depthOf(issue.id, issues))
        }
        return result
    }

    /**
     * Creates a deterministic WBS view from the existing Issue tree.
     * Codes are sibling ordinals (1, 1.1, 1.2...) and are never persisted.
     */
    fun wbsProjection(issues: List<RepoIssue>): List<WbsProjectionRow> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }
            .mapValues { (_, children) -> children.sortedBy { it.issueNumber } }
        val rows = mutableListOf<WbsProjectionRow>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int, code: String) {
            if (!visited.add(issue.id)) return
            val subtreeIds = descendantIds(issue.id, issues) + issue.id
            val subtree = issues.filter { it.id in subtreeIds }
            val completedCount = subtree.count { it.status == IssueStatus.CLOSED }
            val totalCount = subtree.size.coerceAtLeast(1)
            rows += WbsProjectionRow(
                issue = issue,
                depth = depth,
                code = code,
                completedCount = completedCount,
                totalCount = totalCount,
                progress = completedCount.toFloat() / totalCount.toFloat()
            )
            childrenByParent[issue.id].orEmpty().forEachIndexed { index, child ->
                visit(child, depth + 1, "$code.${index + 1}")
            }
        }

        var rootOrdinal = 1
        issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }
            .sortedBy { it.issueNumber }
            .forEach { root ->
                visit(root, 0, rootOrdinal.toString())
                rootOrdinal += 1
            }

        // Cyclic records have no valid root. Promote each unseen component to a safe root.
        issues.sortedBy { it.issueNumber }.forEach { issue ->
            if (issue.id !in visited) {
                visit(issue, 0, rootOrdinal.toString())
                rootOrdinal += 1
            }
        }
        return rows
    }

    fun overallProgress(issues: List<RepoIssue>): Float {
        if (issues.isEmpty()) return 0f
        return issues.count { it.status == IssueStatus.CLOSED }.toFloat() / issues.size.toFloat()
    }
}
