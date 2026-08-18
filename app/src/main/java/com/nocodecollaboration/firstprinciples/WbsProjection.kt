package com.nocodecollaboration.firstprinciples

/**
 * Minimal WBS metadata projected over the existing RepoIssue identity.
 * This is deliberately not a second task aggregate.
 */
data class WbsIssueProjection(
    val issueId: String,
    val repositoryId: String,
    val parentIssueId: String?,
    val siblingOrder: Int,
    val plannedStartEpochMillis: Long? = null,
    val plannedEndEpochMillis: Long? = null,
    val weight: Double = 1.0,
    val directProgressPercent: Int = 0,
)

object WbsProjection {
    fun rollUp(nodes: List<WbsIssueProjection>): Map<String, Int> {
        require(nodes.map { it.issueId }.distinct().size == nodes.size) {
            "Issue IDs must be unique"
        }
        require(nodes.all { it.weight >= 0.0 }) {
            "WBS weights cannot be negative"
        }

        val byId = nodes.associateBy { it.issueId }
        val byParent = nodes.groupBy { it.parentIssueId }
        val visiting = mutableSetOf<String>()
        val result = mutableMapOf<String, Int>()

        fun progress(issueId: String): Int {
            result[issueId]?.let { return it }
            check(visiting.add(issueId)) { "WBS hierarchy contains a cycle at $issueId" }

            val node = requireNotNull(byId[issueId])
            val children = byParent[issueId].orEmpty()
            val value = if (children.isEmpty()) {
                node.directProgressPercent.coerceIn(0, 100)
            } else {
                val denominator = children.sumOf { it.weight }
                if (denominator <= 0.0) {
                    0
                } else {
                    (children.sumOf { progress(it.issueId) * it.weight } / denominator)
                        .toInt()
                        .coerceIn(0, 100)
                }
            }

            visiting.remove(issueId)
            result[issueId] = value
            return value
        }

        nodes.forEach { progress(it.issueId) }
        return result
    }

    /** Stable human-readable 1 / 1.1 / 1.2 numbering from parent and sibling order. */
    fun numbering(nodes: List<WbsIssueProjection>): Map<String, String> {
        val children = nodes.groupBy { it.parentIssueId }
            .mapValues { (_, value) ->
                value.sortedWith(
                    compareBy<WbsIssueProjection> { it.siblingOrder }
                        .thenBy { it.issueId },
                )
            }
        val result = mutableMapOf<String, String>()

        fun visit(parentId: String?, prefix: String) {
            children[parentId].orEmpty().forEachIndexed { index, node ->
                val number = if (prefix.isBlank()) {
                    "${index + 1}"
                } else {
                    "$prefix.${index + 1}"
                }
                result[node.issueId] = number
                visit(node.issueId, number)
            }
        }

        visit(parentId = null, prefix = "")
        return result
    }
}
