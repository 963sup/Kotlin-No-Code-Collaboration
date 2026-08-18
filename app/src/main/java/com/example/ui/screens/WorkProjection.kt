package com.example.ui.screens

import com.example.data.model.GranteeType
import com.example.data.model.RepoIssue
import com.example.data.model.TeamMembership

internal enum class WorkAssignmentScope(val label: String) {
    MINE("我的工作"),
    ALL("全部工作")
}

internal fun projectWorkIssues(
    allIssues: List<RepoIssue>,
    activeUserId: String?,
    teamMemberships: List<TeamMembership>,
    assignmentScope: WorkAssignmentScope,
    repositoryId: String?
): List<RepoIssue> {
    val activeTeamIds = if (activeUserId == null) {
        emptySet()
    } else {
        teamMemberships.asSequence()
            .filter { it.userId == activeUserId }
            .map { it.teamId }
            .toSet()
    }

    return allIssues.filter { issue ->
        val matchesRepository = repositoryId == null || issue.repoId == repositoryId
        val matchesAssignment = when (assignmentScope) {
            WorkAssignmentScope.ALL -> true
            WorkAssignmentScope.MINE -> when (issue.assigneeType) {
                GranteeType.USER -> activeUserId != null && issue.assigneeId == activeUserId
                GranteeType.TEAM -> issue.assigneeId != null && issue.assigneeId in activeTeamIds
                null -> false
            }
        }
        matchesRepository && matchesAssignment
    }
}
