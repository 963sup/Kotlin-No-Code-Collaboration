package com.example.data.model

import kotlin.math.max

/**
 * Pure recursive hierarchy rules for RepoIssue.
 * Kanban and WBS are projections of Issues, never a second Task entity.
 */
data class WbsProjectionRow(
    val issue: RepoIssue,
    val depth: Int,
    val code: String,
    val completedCount: Int,
    val totalCount: Int,
    val progress: Float,
)

object IssueHierarchyRules {
    private fun ordered(issues: List<RepoIssue>): List<RepoIssue> =
        issues.sortedWith(compareBy<RepoIssue> { it.sortOrder }.thenBy { it.issueNumber }.thenBy { it.id })

    fun descendantIds(rootIssueId: String, issues: List<RepoIssue>): Set<String> {
        val childrenByParent = issues.groupBy { it.parentIssueId }
        val descendants = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(rootIssueId)
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            childrenByParent[currentId].orEmpty().forEach { child ->
                if (child.id != rootIssueId && descendants.add(child.id)) queue.addLast(child.id)
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
        val childrenByParent = issues.groupBy { it.parentIssueId }.mapValues { ordered(it.value) }
        val result = mutableListOf<Pair<RepoIssue, Int>>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int) {
            if (!visited.add(issue.id)) return
            result += issue to depth
            childrenByParent[issue.id].orEmpty().forEach { visit(it, depth + 1) }
        }

        ordered(issues.filter { it.parentIssueId == null || it.parentIssueId !in byId })
            .forEach { visit(it, 0) }
        ordered(issues).forEach { if (it.id !in visited) visit(it, depthOf(it.id, issues)) }
        return result
    }

    fun wbsProjection(issues: List<RepoIssue>): List<WbsProjectionRow> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }.mapValues { ordered(it.value) }
        val progressMemo = mutableMapOf<String, Float>()

        fun progressOf(issue: RepoIssue, visiting: MutableSet<String>): Float {
            progressMemo[issue.id]?.let { return it }
            if (!visiting.add(issue.id)) return leafProgress(issue)
            val children = childrenByParent[issue.id].orEmpty().filter { it.id !in visiting }
            val value = if (children.isEmpty()) {
                leafProgress(issue)
            } else {
                val totalWeight = children.sumOf { max(it.wbsWeight, 0.01) }
                if (totalWeight <= 0.0) {
                    0f
                } else {
                    (
                        children.sumOf { child ->
                            progressOf(child, visiting) * max(child.wbsWeight, 0.01)
                        } / totalWeight
                        ).toFloat()
                }
            }
            visiting.remove(issue.id)
            return value.coerceIn(0f, 1f).also { progressMemo[issue.id] = it }
        }

        val rows = mutableListOf<WbsProjectionRow>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int, code: String) {
            if (!visited.add(issue.id)) return
            val subtreeIds = descendantIds(issue.id, issues) + issue.id
            val subtree = issues.filter { it.id in subtreeIds }
            rows += WbsProjectionRow(
                issue = issue,
                depth = depth,
                code = code,
                completedCount = subtree.count { it.status == IssueStatus.CLOSED },
                totalCount = subtree.size.coerceAtLeast(1),
                progress = progressOf(issue, mutableSetOf()),
            )
            childrenByParent[issue.id].orEmpty().forEachIndexed { index, child ->
                visit(child, depth + 1, "$code.${index + 1}")
            }
        }

        var rootOrdinal = 1
        ordered(issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }).forEach { root ->
            visit(root, 0, rootOrdinal.toString())
            rootOrdinal += 1
        }
        ordered(issues).forEach { issue ->
            if (issue.id !in visited) {
                visit(issue, 0, rootOrdinal.toString())
                rootOrdinal += 1
            }
        }
        return rows
    }

    fun overallProgress(issues: List<RepoIssue>): Float {
        if (issues.isEmpty()) return 0f
        val rows = wbsProjection(issues)
        val rootRows = rows.filter { it.depth == 0 }
        if (rootRows.isEmpty()) return 0f
        val totalWeight = rootRows.sumOf { max(it.issue.wbsWeight, 0.01) }
        return if (totalWeight <= 0.0) {
            0f
        } else {
            (rootRows.sumOf { it.progress * max(it.issue.wbsWeight, 0.01) } / totalWeight)
                .toFloat()
                .coerceIn(0f, 1f)
        }
    }

    fun validatePlan(
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int,
    ): String? = when {
        sortOrder < 0 -> "WBS 排序不得小於 0"

        plannedStartAt != null && plannedEndAt != null && plannedEndAt < plannedStartAt ->
            "計畫結束日不得早於開始日"

        !wbsWeight.isFinite() || wbsWeight <= 0.0 || wbsWeight > 1000.0 ->
            "WBS 權重必須介於 0 與 1000 之間"

        progressPercent !in 0..100 -> "進度必須介於 0% 與 100% 之間"

        else -> null
    }

    private fun leafProgress(issue: RepoIssue): Float = when (issue.status) {
        IssueStatus.CLOSED -> 1f
        else -> issue.progressPercent.coerceIn(0, 100) / 100f
    }
}
