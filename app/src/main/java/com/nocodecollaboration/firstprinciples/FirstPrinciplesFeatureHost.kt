package com.nocodecollaboration.firstprinciples

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

/**
 * UI state is supplied by existing ViewModels/repositories. This host owns no
 * duplicate work, dashboard, permission, or synchronization state.
 */
data class FirstPrinciplesFeatureState(
    val destination: PrimaryDestination,
    val scope: WorkspaceScope,
    val scopeLabel: String,
    val homeSummary: OperationalSummary,
    val myWork: Map<WorkStatus, List<AccessibleIssue>>,
    val repositoryNames: Map<String, String>,
    val repositoryFilter: String?,
    val exploreQuery: String,
    val exploreResults: List<SearchableCollaborationItem>,
    val savedTargetIds: Set<String>,
)

@Composable
fun FirstPrinciplesFeatureHost(
    state: FirstPrinciplesFeatureState,
    onDestinationSelected: (PrimaryDestination) -> Unit,
    onScopeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRepositoriesClick: () -> Unit,
    onIssuesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onRepositoryFilterClick: () -> Unit,
    onIssueClick: (AccessibleIssue) -> Unit,
    onExploreQueryChange: (String) -> Unit,
    onTargetClick: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit,
    inboxContent: @Composable (PaddingValues) -> Unit,
) {
    FirstPrinciplesMobileShell(
        selectedDestination = state.destination,
        scopeLabel = state.scopeLabel,
        onDestinationSelected = onDestinationSelected,
        onScopeClick = onScopeClick,
        onSearchClick = onSearchClick,
        onProfileClick = onProfileClick,
    ) { padding ->
        when (state.destination) {
            PrimaryDestination.HOME -> ScopeAwareHomeContent(
                scope = state.scope,
                summary = state.homeSummary,
                onRepositoriesClick = onRepositoriesClick,
                onIssuesClick = onIssuesClick,
                onNotificationsClick = onNotificationsClick,
            )

            PrimaryDestination.INBOX -> inboxContent(padding)

            PrimaryDestination.KANBAN -> MyWorkContent(
                grouped = state.myWork,
                repositoryNames = state.repositoryNames,
                activeRepositoryFilter = state.repositoryFilter,
                onRepositoryFilterClick = onRepositoryFilterClick,
                onIssueClick = onIssueClick,
            )

            PrimaryDestination.EXPLORE -> ExploreContent(
                query = state.exploreQuery,
                results = state.exploreResults,
                savedStableIds = state.savedTargetIds,
                onQueryChange = onExploreQueryChange,
                onTargetClick = onTargetClick,
                onToggleSaved = onToggleSaved,
            )
        }
    }
}
