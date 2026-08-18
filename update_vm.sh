cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/viewmodel/GovernanceViewModel.kt

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

    fun loadEvidenceVerifications(evidenceId: String) {
        viewModelScope.launch {
            repository.dao.getVerificationsForEvidence(evidenceId).collect {
                _selectedEvidenceVerifications.value = it
            }
        }
    }

    fun submitVerification(evidenceId: String, issueId: String, isAccepted: Boolean, comment: String, activeUser: User?) {
        viewModelScope.launch {
            val ver = WorkVerification(
                evidenceId = evidenceId,
                issueId = issueId,
                reviewerUserId = activeUser?.id ?: "unknown",
                reviewerDisplayName = activeUser?.displayName ?: "Unknown User",
                decision = if (isAccepted) ReviewDecision.APPROVED else ReviewDecision.REJECTED,
                feedbackNote = comment
            )
            repository.dao.insertWorkVerification(ver)
        }
    }
INNER_EOF
sed -i '/^}$/d' app/src/main/java/com/example/ui/viewmodel/GovernanceViewModel.kt
echo "}" >> app/src/main/java/com/example/ui/viewmodel/GovernanceViewModel.kt
