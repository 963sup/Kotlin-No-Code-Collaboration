package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.ArtifactType
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.NoCodeArtifact
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationStatus
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.TaskChecklist
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.data.model.WorkEvidence
import com.example.data.model.WorkVerification
import com.example.data.repository.GovernanceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiMessage(val text: String, val isError: Boolean = false)

class GovernanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GovernanceRepository

    val enterprises: StateFlow<List<Enterprise>>
    val enterprise: StateFlow<Enterprise?>
    val organizations: StateFlow<List<Organization>>
    val users: StateFlow<List<User>>
    val teams: StateFlow<List<Team>>
    val repositories: StateFlow<List<Repository>>
    val auditLogs: StateFlow<List<AuditLog>>
    val allAccessRules: StateFlow<List<RepoAccessRule>>
    val allOrgMemberships: StateFlow<List<OrgMembership>>
    val allTeamMemberships: StateFlow<List<TeamMembership>>
    val allArtifacts: StateFlow<List<NoCodeArtifact>>
    val allIssues: StateFlow<List<RepoIssue>>
    val allDiscussions: StateFlow<List<RepoDiscussion>>
    val allReviews: StateFlow<List<ArtifactReview>>
    val allApprovals: StateFlow<List<ArtifactApproval>>
    val allDependencies: StateFlow<List<IssueDependency>>

    private val _activeUser = MutableStateFlow<User?>(null)
    val activeUser: StateFlow<User?> = _activeUser.asStateFlow()

    private val _userNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val userNotifications: StateFlow<List<AppNotification>> = _userNotifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _notificationFilterCategory = MutableStateFlow<NotificationCategory?>(null)
    val notificationFilterCategory: StateFlow<NotificationCategory?> = _notificationFilterCategory.asStateFlow()

    private val _notificationFilterStatus = MutableStateFlow<NotificationStatus?>(null)
    val notificationFilterStatus: StateFlow<NotificationStatus?> = _notificationFilterStatus.asStateFlow()

    private val _inspectedProfileUser = MutableStateFlow<User?>(null)
    val inspectedProfileUser: StateFlow<User?> = _inspectedProfileUser.asStateFlow()

    private val _profileUserArtifacts = MutableStateFlow<List<NoCodeArtifact>>(emptyList())
    val profileUserArtifacts: StateFlow<List<NoCodeArtifact>> = _profileUserArtifacts.asStateFlow()

    private val _profileUserReviews = MutableStateFlow<List<ArtifactReview>>(emptyList())
    val profileUserReviews: StateFlow<List<ArtifactReview>> = _profileUserReviews.asStateFlow()

    private val _profileUserApprovals = MutableStateFlow<List<ArtifactApproval>>(emptyList())
    val profileUserApprovals: StateFlow<List<ArtifactApproval>> = _profileUserApprovals.asStateFlow()

    private val _profileUserAuditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val profileUserAuditLogs: StateFlow<List<AuditLog>> = _profileUserAuditLogs.asStateFlow()

    private val _profileUserIssues = MutableStateFlow<List<RepoIssue>>(emptyList())
    val profileUserIssues: StateFlow<List<RepoIssue>> = _profileUserIssues.asStateFlow()

    private val _profileUserDiscussions = MutableStateFlow<List<RepoDiscussion>>(emptyList())
    val profileUserDiscussions: StateFlow<List<RepoDiscussion>> = _profileUserDiscussions.asStateFlow()

    private val _selectedRepo = MutableStateFlow<Repository?>(null)
    val selectedRepo: StateFlow<Repository?> = _selectedRepo.asStateFlow()

    private val _selectedArtifact = MutableStateFlow<NoCodeArtifact?>(null)
    val selectedArtifact: StateFlow<NoCodeArtifact?> = _selectedArtifact.asStateFlow()

    private val _selectedArtifactReviews = MutableStateFlow<List<ArtifactReview>>(emptyList())
    val selectedArtifactReviews: StateFlow<List<ArtifactReview>> = _selectedArtifactReviews.asStateFlow()

    private val _selectedArtifactApprovals = MutableStateFlow<List<ArtifactApproval>>(emptyList())
    val selectedArtifactApprovals: StateFlow<List<ArtifactApproval>> = _selectedArtifactApprovals.asStateFlow()

    private val _selectedRepoArtifacts = MutableStateFlow<List<NoCodeArtifact>>(emptyList())
    val selectedRepoArtifacts: StateFlow<List<NoCodeArtifact>> = _selectedRepoArtifacts.asStateFlow()

    private val _selectedRepoAccessRules = MutableStateFlow<List<RepoAccessRule>>(emptyList())
    val selectedRepoAccessRules: StateFlow<List<RepoAccessRule>> = _selectedRepoAccessRules.asStateFlow()

    private val _selectedRepoIssues = MutableStateFlow<List<RepoIssue>>(emptyList())
    val selectedRepoIssues: StateFlow<List<RepoIssue>> = _selectedRepoIssues.asStateFlow()

    private val _selectedRepoDependencies = MutableStateFlow<List<IssueDependency>>(emptyList())
    val selectedRepoDependencies: StateFlow<List<IssueDependency>> = _selectedRepoDependencies.asStateFlow()

    private val _selectedRepoDiscussions = MutableStateFlow<List<RepoDiscussion>>(emptyList())
    val selectedRepoDiscussions: StateFlow<List<RepoDiscussion>> = _selectedRepoDiscussions.asStateFlow()

    private val _selectedIssueComments = MutableStateFlow<List<IssueComment>>(emptyList())
    val selectedIssueComments: StateFlow<List<IssueComment>> = _selectedIssueComments.asStateFlow()

    private val _selectedDiscussionComments = MutableStateFlow<List<DiscussionComment>>(emptyList())
    val selectedDiscussionComments: StateFlow<List<DiscussionComment>> = _selectedDiscussionComments.asStateFlow()

    private val _simulationResult = MutableStateFlow<PolicyEvaluationDetail?>(null)
    val simulationResult: StateFlow<PolicyEvaluationDetail?> = _simulationResult.asStateFlow()

    private val _uiMessages = MutableSharedFlow<UiMessage>()
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = GovernanceRepository(db.governanceDao())

        enterprise = repository.enterprise.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        enterprises = repository.enterprises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        organizations = repository.organizations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        users = repository.users.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        teams = repository.teams.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        repositories = repository.repositories.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allAccessRules = repository.allAccessRules.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allOrgMemberships = repository.allOrgMemberships.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allTeamMemberships = repository.allTeamMemberships.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allArtifacts = repository.allArtifacts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allIssues = repository.allIssues.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allDiscussions = repository.allDiscussions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allReviews = repository.allReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allApprovals = repository.allApprovals.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        allDependencies = repository.allDependencies.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

        viewModelScope.launch {
            repository.initializeIfEmpty()
            // Set default active user
            users.collect { userList ->
                if (_activeUser.value == null && userList.isNotEmpty()) {
                    switchActiveUser(userList.first())
                }
            }
        }
    }

    fun switchActiveUser(user: User) {
        _activeUser.value = user
        selectProfileUser(user)
        viewModelScope.launch {
            repository.getNotificationsForUser(user.id).collect {
                _userNotifications.value = it
            }
        }
        viewModelScope.launch {
            repository.getUnreadCountForUser(user.id).collect {
                _unreadNotificationCount.value = it
            }
        }
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("Switched active persona to ${user.displayName} (${user.title})"))
        }
    }

    fun selectProfileUser(user: User?) {
        _inspectedProfileUser.value = user
        if (user != null) {
            viewModelScope.launch {
                repository.getArtifactsByAuthor(user.id).collect {
                    _profileUserArtifacts.value = it
                }
            }
            viewModelScope.launch {
                repository.getReviewsByReviewer(user.id).collect {
                    _profileUserReviews.value = it
                }
            }
            viewModelScope.launch {
                repository.getApprovalsByApprover(user.id).collect {
                    _profileUserApprovals.value = it
                }
            }
            viewModelScope.launch {
                repository.getAuditLogsByActor(user.id).collect {
                    _profileUserAuditLogs.value = it
                }
            }
            viewModelScope.launch {
                repository.getIssuesByAuthor(user.id).collect {
                    _profileUserIssues.value = it
                }
            }
            viewModelScope.launch {
                repository.getDiscussionsByAuthor(user.id).collect {
                    _profileUserDiscussions.value = it
                }
            }
        } else {
            _profileUserArtifacts.value = emptyList()
            _profileUserReviews.value = emptyList()
            _profileUserApprovals.value = emptyList()
            _profileUserAuditLogs.value = emptyList()
            _profileUserIssues.value = emptyList()
            _profileUserDiscussions.value = emptyList()
        }
    }

    fun selectRepository(repo: Repository?) {
        _selectedRepo.value = repo
        if (repo != null) {
            viewModelScope.launch {
                repository.getArtifactsByRepo(repo.id).collect {
                    _selectedRepoArtifacts.value = it
                }
            }
            viewModelScope.launch {
                repository.getAccessRulesByRepo(repo.id).collect {
                    _selectedRepoAccessRules.value = it
                }
            }
            viewModelScope.launch {
                repository.getIssuesByRepo(repo.id).collect {
                    _selectedRepoIssues.value = it
                }
            }
            viewModelScope.launch {
                repository.getDependenciesByRepo(repo.id).collect {
                    _selectedRepoDependencies.value = it
                }
            }
            viewModelScope.launch {
                repository.getDiscussionsByRepo(repo.id).collect {
                    _selectedRepoDiscussions.value = it
                }
            }
        } else {
            _selectedRepoArtifacts.value = emptyList()
            _selectedRepoAccessRules.value = emptyList()
            _selectedRepoIssues.value = emptyList()
            _selectedRepoDependencies.value = emptyList()
            _selectedRepoDiscussions.value = emptyList()
        }
    }

    fun selectArtifact(artifact: NoCodeArtifact?) {
        _selectedArtifact.value = artifact
        if (artifact != null) {
            viewModelScope.launch {
                repository.getReviewsByArtifact(artifact.id).collect {
                    _selectedArtifactReviews.value = it
                }
            }
            viewModelScope.launch {
                repository.getApprovalsByArtifact(artifact.id).collect {
                    _selectedArtifactApprovals.value = it
                }
            }
        } else {
            _selectedArtifactReviews.value = emptyList()
            _selectedArtifactApprovals.value = emptyList()
        }
    }

    fun createRepository(
        name: String,
        displayName: String,
        ownerType: OwnerType,
        ownerId: String,
        ownerDisplayName: String,
        description: String,
        category: String,
        onComplete: (Boolean) -> Unit,
    ) {
        val user = _activeUser.value ?: return
        val ent = enterprise.value ?: return

        viewModelScope.launch {
            val (success, msg) = repository.createRepository(
                name = name,
                displayName = displayName,
                ownerType = ownerType,
                ownerId = ownerId,
                ownerDisplayName = ownerDisplayName,
                enterpriseId = ent.id,
                description = description,
                category = category,
                creatorUser = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            onComplete(success)
        }
    }

    fun createNoCodeArtifact(
        repoId: String,
        title: String,
        type: ArtifactType,
        summary: String,
        content: String,
        onComplete: (Boolean) -> Unit,
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createNoCodeArtifact(
                repoId = repoId,
                title = title,
                type = type,
                summary = summary,
                content = content,
                author = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            onComplete(success)
        }
    }

    fun submitForReview(artifactId: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.submitForReview(artifactId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            // Refresh selected artifact if viewing
            if (_selectedArtifact.value?.id == artifactId) {
                val updated = repository.allArtifacts.stateIn(viewModelScope).value.firstOrNull { it.id == artifactId }
                if (updated != null) _selectedArtifact.value = updated
            }
        }
    }

    fun submitReview(artifactId: String, decision: ReviewDecision, feedback: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.submitReview(artifactId, user, decision, feedback)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (_selectedArtifact.value?.id == artifactId) {
                val updated = repository.allArtifacts.stateIn(viewModelScope).value.firstOrNull { it.id == artifactId }
                if (updated != null) _selectedArtifact.value = updated
            }
        }
    }

    fun submitApproverSignOff(artifactId: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.submitApproverSignOff(artifactId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (_selectedArtifact.value?.id == artifactId) {
                val updated = repository.allArtifacts.stateIn(viewModelScope).value.firstOrNull { it.id == artifactId }
                if (updated != null) _selectedArtifact.value = updated
            }
        }
    }

    fun publishAndLock(artifactId: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.publishAndLock(artifactId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (_selectedArtifact.value?.id == artifactId) {
                val updated = repository.allArtifacts.stateIn(viewModelScope).value.firstOrNull { it.id == artifactId }
                if (updated != null) _selectedArtifact.value = updated
            }
        }
    }

    fun addRepoAccessRule(
        repoId: String,
        granteeType: GranteeType,
        granteeId: String,
        granteeName: String,
        role: RepoRole,
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            repository.addRepoAccessRule(repoId, granteeType, granteeId, granteeName, role, user)
            _uiMessages.emit(UiMessage("Role '${role.name}' assigned to ${granteeType.name} '$granteeName'"))
        }
    }

    fun removeRepoAccessRule(rule: RepoAccessRule) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            repository.removeRepoAccessRule(rule, user)
            _uiMessages.emit(UiMessage("Access rule revoked"))
        }
    }

    fun updateEnterprisePolicies(
        enforceDualApproval: Boolean,
        allowUserOwnedRepos: Boolean,
        enforceReviewerBeforeApprover: Boolean,
        enforceSegregationOfDuties: Boolean,
    ) {
        val ent = enterprise.value ?: return
        val updated = ent.copy(
            enforceDualApproval = enforceDualApproval,
            allowUserOwnedRepos = allowUserOwnedRepos,
            enforceReviewerBeforeApprover = enforceReviewerBeforeApprover,
            enforceSegregationOfDuties = enforceSegregationOfDuties,
        )
        viewModelScope.launch {
            repository.updateEnterprise(updated)
            _uiMessages.emit(UiMessage("Enterprise compliance policies updated successfully."))
        }
    }

    fun runPolicySimulation(actor: User, repo: Repository, artifact: NoCodeArtifact?, action: GovernanceAction) {
        viewModelScope.launch {
            val detail = repository.evaluateAction(actor, repo, artifact, action)
            _simulationResult.value = detail
        }
    }

    fun clearSimulationResult() {
        _simulationResult.value = null
    }

    fun createTeam(orgId: String, name: String, slug: String, description: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createTeam(orgId, name, slug, description, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    fun addTeamMember(teamId: String, userId: String, role: TeamRole) {
        viewModelScope.launch {
            repository.addTeamMember(teamId, userId, role)
            _uiMessages.emit(UiMessage("User added to team."))
        }
    }

    // --- ISSUE VM ACTIONS ---
    fun loadIssueComments(issueId: String) {
        viewModelScope.launch {
            repository.getIssueComments(issueId).collect {
                _selectedIssueComments.value = it
            }
        }
    }

    fun createIssue(
        repoId: String,
        title: String,
        description: String,
        priority: IssuePriority,
        assigneeType: GranteeType?,
        assigneeId: String?,
        assigneeName: String?,
        linkedArtifactId: String?,
        linkedArtifactTitle: String?,
        parentIssueId: String? = null,
        labels: String,
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createIssue(
                repoId = repoId,
                title = title,
                description = description,
                priority = priority,
                assigneeType = assigneeType,
                assigneeId = assigneeId,
                assigneeName = assigneeName,
                linkedArtifactId = linkedArtifactId,
                linkedArtifactTitle = linkedArtifactTitle,
                parentIssueId = parentIssueId,
                labels = labels,
                author = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun linkParentIssue(issueId: String, parentIssueId: String?, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.linkParentIssue(issueId, parentIssueId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun addIssueDependency(
        repoId: String,
        blockedIssueId: String,
        blockingIssueId: String,
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.addIssueDependency(repoId, blockedIssueId, blockingIssueId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun removeIssueDependency(dependencyId: String, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.removeIssueDependency(dependencyId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun addIssueComment(issueId: String, content: String, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.addIssueComment(issueId, content, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun updateIssueStatus(issueId: String, newStatus: IssueStatus) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateIssueStatus(issueId, newStatus, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    fun assignIssue(issueId: String, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.assignIssue(issueId, assigneeType, assigneeId, assigneeName, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    fun updateIssuePlan(
        issueId: String,
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int,
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateIssuePlan(
                issueId = issueId,
                sortOrder = sortOrder,
                plannedStartAt = plannedStartAt,
                plannedEndAt = plannedEndAt,
                wbsWeight = wbsWeight,
                progressPercent = progressPercent,
                actor = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    // --- DISCUSSION VM ACTIONS ---
    fun loadDiscussionComments(discussionId: String) {
        viewModelScope.launch {
            repository.getDiscussionComments(discussionId).collect {
                _selectedDiscussionComments.value = it
            }
        }
    }

    fun createDiscussion(
        repoId: String,
        title: String,
        category: DiscussionCategory,
        body: String,
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createDiscussion(
                repoId = repoId,
                title = title,
                category = category,
                body = body,
                author = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun addDiscussionComment(discussionId: String, content: String, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.addDiscussionComment(discussionId, content, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun toggleLockDiscussion(discussionId: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.toggleLockDiscussion(discussionId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    fun markAcceptedAnswer(discussionId: String, commentId: String) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.markAcceptedAnswer(discussionId, commentId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

    fun upvoteDiscussion(discussionId: String) {
        viewModelScope.launch {
            repository.upvoteDiscussion(discussionId)
        }
    }

    fun upvoteDiscussionComment(commentId: String, discussionId: String) {
        viewModelScope.launch {
            repository.upvoteDiscussionComment(commentId, discussionId)
        }
    }

    // =========================================================================
    // ENTERPRISE VM OPERATIONS
    // =========================================================================

    fun createEnterprise(
        name: String,
        slug: String,
        description: String,
        enforceDualApproval: Boolean = true,
        allowUserOwnedRepos: Boolean = true,
        enforceReviewerBeforeApprover: Boolean = true,
        enforceSegregationOfDuties: Boolean = true,
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createEnterprise(
                name = name,
                slug = slug,
                description = description,
                enforceDualApproval = enforceDualApproval,
                allowUserOwnedRepos = allowUserOwnedRepos,
                enforceReviewerBeforeApprover = enforceReviewerBeforeApprover,
                enforceSegregationOfDuties = enforceSegregationOfDuties,
                creatorUser = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun updateEnterpriseSecurityPolicies(enterprise: Enterprise, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateEnterpriseSecurityPolicies(enterprise, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun createEnterpriseUser(
        enterpriseId: String,
        username: String,
        displayName: String,
        email: String,
        title: String,
        isEnterpriseAdmin: Boolean,
        avatarColorHex: String = "#8B5CF6",
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createEnterpriseUser(
                enterpriseId = enterpriseId,
                username = username,
                displayName = displayName,
                email = email,
                title = title,
                isEnterpriseAdmin = isEnterpriseAdmin,
                avatarColorHex = avatarColorHex,
                actor = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    // =========================================================================
    // ORGANIZATION VM OPERATIONS
    // =========================================================================

    fun createOrganization(
        enterpriseId: String,
        name: String,
        slug: String,
        description: String,
        badgeColorHex: String = "#4F46E5",
        defaultMemberRole: RepoRole = RepoRole.COLLABORATOR,
        ownerUserId: String = "",
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createOrganization(
                enterpriseId = enterpriseId,
                name = name,
                slug = slug,
                description = description,
                badgeColorHex = badgeColorHex,
                defaultMemberRole = defaultMemberRole,
                creatorUser = user,
                ownerUserId = ownerUserId,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun updateOrganization(org: Organization, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateOrganization(org, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun addOrgMember(orgId: String, userId: String, role: com.example.data.model.OrgRole, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.addOrgMember(orgId, userId, role, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun removeOrgMember(orgId: String, userId: String, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.removeOrgMember(orgId, userId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    // =========================================================================
    // TEAM VM OPERATIONS
    // =========================================================================

    fun createTeam(
        orgId: String,
        name: String,
        slug: String,
        description: String,
        parentTeamId: String? = null,
        onSuccess: () -> Unit = {},
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.createTeam(
                orgId = orgId,
                name = name,
                slug = slug,
                description = description,
                parentTeamId = parentTeamId,
                creatorUser = user,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun addTeamMember(teamId: String, userId: String, role: TeamRole = TeamRole.MEMBER, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.addTeamMember(teamId, userId, role, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    fun removeTeamMember(teamId: String, userId: String, onSuccess: () -> Unit = {}) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.removeTeamMember(teamId, userId, user)
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) onSuccess()
        }
    }

    // =========================================================================
    // USER PROFILE VM OPERATIONS
    // =========================================================================

    fun updateUserProfile(
        targetUser: User,
        displayName: String,
        title: String,
        bio: String,
        location: String,
        pronouns: String,
        avatarColorHex: String,
        notificationPreferences: String,
        onSuccess: () -> Unit = {},
    ) {
        val actor = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateUserProfile(
                user = targetUser,
                displayName = displayName,
                title = title,
                bio = bio,
                location = location,
                pronouns = pronouns,
                avatarColorHex = avatarColorHex,
                notificationPreferences = notificationPreferences,
                actor = actor,
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
            if (success) {
                // If we edited active user, refresh activeUser state
                if (_activeUser.value?.id == targetUser.id) {
                    _activeUser.value = targetUser.copy(
                        displayName = displayName.trim().ifEmpty { targetUser.displayName },
                        title = title.trim().ifEmpty { targetUser.title },
                        bio = bio.trim(),
                        location = location.trim(),
                        pronouns = pronouns.trim(),
                        avatarColorHex = avatarColorHex,
                        notificationPreferences = notificationPreferences,
                    )
                }
                selectProfileUser(
                    _inspectedProfileUser.value?.let { inspected ->
                        if (inspected.id == targetUser.id) {
                            targetUser.copy(
                                displayName = displayName.trim().ifEmpty { targetUser.displayName },
                                title = title.trim().ifEmpty { targetUser.title },
                                bio = bio.trim(),
                                location = location.trim(),
                                pronouns = pronouns.trim(),
                                avatarColorHex = avatarColorHex,
                                notificationPreferences = notificationPreferences,
                            )
                        } else {
                            inspected
                        }
                    },
                )
                onSuccess()
            }
        }
    }

    // =========================================================================
    // UNIFIED INBOX & NOTIFICATIONS VM METHODS
    // =========================================================================

    fun setNotificationFilterCategory(category: NotificationCategory?) {
        _notificationFilterCategory.value = category
    }

    fun setNotificationFilterStatus(status: NotificationStatus?) {
        _notificationFilterStatus.value = status
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
            _uiMessages.emit(UiMessage("All notifications marked as read"))
        }
    }

    fun archiveNotification(id: String) {
        viewModelScope.launch {
            repository.archiveNotification(id)
            _uiMessages.emit(UiMessage("Notification archived"))
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            _uiMessages.emit(UiMessage("Notification deleted"))
        }
    }

    fun markNotificationActionCompleted(id: String) {
        viewModelScope.launch {
            repository.markActionCompleted(id)
        }
    }

    // --- Work Item Detail Extensions ---
    private val _selectedIssueEvidence = MutableStateFlow<List<WorkEvidence>>(emptyList())
    val selectedIssueEvidence: StateFlow<List<WorkEvidence>> = _selectedIssueEvidence.asStateFlow()

    private val _selectedIssueChecklist = MutableStateFlow<List<TaskChecklist>>(emptyList())
    val selectedIssueChecklist: StateFlow<List<TaskChecklist>> = _selectedIssueChecklist.asStateFlow()

    private val _selectedEvidenceVerifications = MutableStateFlow<List<WorkVerification>>(emptyList())
    val selectedEvidenceVerifications: StateFlow<List<WorkVerification>> = _selectedEvidenceVerifications.asStateFlow()

    fun loadIssueDetailData(issueId: String) {
        viewModelScope.launch {
            repository.dao.getWorkEvidenceForIssue(issueId).collect {
                _selectedIssueEvidence.value = it
            }
        }
        viewModelScope.launch {
            repository.dao.getChecklistForIssue(issueId).collect {
                _selectedIssueChecklist.value = it
            }
        }
    }

    fun toggleChecklistItem(id: String, isCompleted: Boolean, activeUser: User?) {
        viewModelScope.launch {
            val completedBy = if (isCompleted) activeUser?.id else null
            val completedName = if (isCompleted) activeUser?.displayName else null
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            repository.dao.updateTaskChecklistStatus(id, isCompleted, completedBy, completedName, completedAt)
        }
    }

    fun addChecklistItem(issueId: String, title: String, activeUser: User?) {
        viewModelScope.launch {
            val item = TaskChecklist(
                issueId = issueId,
                title = title.trim(),
                isCompleted = false,
                completedByDisplayName = activeUser?.displayName ?: "待分配",
            )
            repository.dao.insertTaskChecklist(item)
            _uiMessages.emit(UiMessage("已新增任務: $title"))
        }
    }

    fun addWorkEvidence(issueId: String, description: String, activeUser: User?) {
        viewModelScope.launch {
            val evd = WorkEvidence(
                issueId = issueId,
                submitterUserId = activeUser?.id ?: "usr_unknown",
                submitterDisplayName = activeUser?.displayName ?: "王小明",
                description = description.trim(),
                status = "PENDING",
            )
            repository.dao.insertWorkEvidence(evd)
            _uiMessages.emit(UiMessage("已提交 Evidence 成果物"))
        }
    }

    fun loadEvidenceVerifications(evidenceId: String) {
        viewModelScope.launch {
            repository.dao.getVerificationsForEvidence(evidenceId).collect {
                _selectedEvidenceVerifications.value = it
            }
        }
    }

    fun submitVerification(
        evidenceId: String,
        issueId: String,
        isAccepted: Boolean,
        comment: String,
        activeUser: User?,
    ) {
        viewModelScope.launch {
            val currentUser = _activeUser.value ?: users.value.firstOrNull()
            val ver = WorkVerification(
                evidenceId = evidenceId,
                issueId = issueId,
                reviewerUserId = currentUser?.id ?: "usr_reviewer",
                reviewerDisplayName = currentUser?.displayName ?: "驗證審查員",
                decision = if (isAccepted) ReviewDecision.APPROVED else ReviewDecision.CHANGES_REQUESTED,
                feedbackNote = comment,
            )
            repository.dao.insertWorkVerification(ver)

            val issue = allIssues.value.firstOrNull { it.id == issueId }
            val reviewerUser = currentUser ?: User(
                id = "usr_reviewer",
                enterpriseId = enterprise.value?.id ?: "ent_1",
                username = "reviewer",
                displayName = "驗證審查員",
                email = "reviewer@enterprise.internal",
                title = "驗證委員",
            )
            if (issue != null) {
                val entId = enterprise.value?.id ?: "ent_1"
                if (isAccepted) {
                    repository.updateIssueStatus(issueId, IssueStatus.CLOSED, reviewerUser)
                    repository.dao.insertAuditLog(
                        AuditLog(
                            enterpriseId = entId,
                            actorUserId = reviewerUser.id,
                            actorDisplayName = reviewerUser.displayName,
                            actionName = "通過獨立驗證 (ACCEPT)",
                            repoId = issue.repoId,
                            verdict = PolicyVerdict.ALLOWED,
                            reasoning = "Evidence 審查通過: #${issue.issueNumber} ${issue.title}。${if (comment.isNotBlank()) "意見: $comment" else ""}",
                        ),
                    )
                    _uiMessages.emit(UiMessage("驗證通過！已完成工作項目 #${issue.issueNumber}"))
                } else {
                    repository.updateIssueStatus(issueId, IssueStatus.IN_PROGRESS, reviewerUser)
                    repository.dao.insertAuditLog(
                        AuditLog(
                            enterpriseId = entId,
                            actorUserId = reviewerUser.id,
                            actorDisplayName = reviewerUser.displayName,
                            actionName = "駁回重行執行 (REJECT)",
                            repoId = issue.repoId,
                            verdict = PolicyVerdict.DENIED_REVIEW_GATE_REQUIRED,
                            reasoning = "Evidence 未通過驗證: #${issue.issueNumber} ${issue.title}。意見: $comment",
                        ),
                    )
                    _uiMessages.emit(UiMessage("已駁回！工作項目 #${issue.issueNumber} 標記為需重新執行 (Rework)", isError = true))
                }
            }
        }
    }
}
