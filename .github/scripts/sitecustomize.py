from pathlib import Path

HERE = Path(__file__).resolve().parent
TARGET = HERE / "implement_mobile_collaboration_v2_data.py"
text = TARGET.read_text(encoding="utf-8")

start_marker = '''\nreplace_once(\n    "app/src/main/java/com/example/data/repository/GovernanceRepository.kt",\n    \'\'\'        val updatedIssue = issue.copy('''
end_marker = '''\n\nwrite(\n    "app/src/main/java/com/example/sync/RemoteSync.kt",'''

if start_marker in text:
    start = text.index(start_marker)
    end = text.index(end_marker, start)
    compatibility = r'''
# Compatibility with the current governance API on main.
repo_path = ROOT / "app/src/main/java/com/example/data/repository/GovernanceRepository.kt"
repo_text = repo_path.read_text(encoding="utf-8")
status_needle = "        val updated = issue.copy(\n            status = newStatus,\n"
if "status = newStatus,\n            progressPercent = when" not in repo_text:
    if status_needle not in repo_text:
        raise RuntimeError("Current updateIssueStatus shape was not recognized")
    repo_text = repo_text.replace(
        status_needle,
        "        val updated = issue.copy(\n"
        "            status = newStatus,\n"
        "            progressPercent = when {\n"
        "                newStatus == IssueStatus.CLOSED -> 100\n"
        "                issue.status == IssueStatus.CLOSED -> 0\n"
        "                else -> issue.progressPercent\n"
        "            },\n",
        1,
    )

if "suspend fun updateIssuePlan(" not in repo_text:
    marker = "    // --- REPO DISCUSSIONS METHODS ---"
    if marker not in repo_text:
        raise RuntimeError("Repository discussion section marker missing")
    method = r"""
    suspend fun updateIssuePlan(
        issueId: String,
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int,
        actor: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Issue not found")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")
        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ASSIGN_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED && issue.authorUserId != actor.id) {
            return Pair(false, evaluation.finalExplanation)
        }
        val validationError = IssueHierarchyRules.validatePlan(
            sortOrder = sortOrder,
            plannedStartAt = plannedStartAt,
            plannedEndAt = plannedEndAt,
            wbsWeight = wbsWeight,
            progressPercent = progressPercent
        )
        if (validationError != null) return Pair(false, validationError)

        val hasChildren = dao.getSubIssuesOnce(issue.id).isNotEmpty()
        val effectiveProgress = when {
            issue.status == IssueStatus.CLOSED -> 100
            hasChildren -> issue.progressPercent
            else -> progressPercent
        }
        val now = System.currentTimeMillis()
        dao.updateIssue(
            issue.copy(
                sortOrder = sortOrder,
                plannedStartAt = plannedStartAt,
                plannedEndAt = plannedEndAt,
                wbsWeight = wbsWeight,
                progressPercent = effectiveProgress,
                updatedAt = now
            )
        )
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "UPDATE_ISSUE_PLAN",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Updated WBS plan for Issue #${issue.issueNumber}."
            )
        )
        return Pair(true, "Issue #${issue.issueNumber} WBS plan updated.")
    }

"""
    repo_text = repo_text.replace(marker, method + marker, 1)
repo_path.write_text(repo_text, encoding="utf-8")

vm_path = ROOT / "app/src/main/java/com/example/ui/viewmodel/GovernanceViewModel.kt"
vm_text = vm_path.read_text(encoding="utf-8")
if "fun updateIssuePlan(" not in vm_text:
    marker = "    // --- DISCUSSION VM ACTIONS ---"
    if marker not in vm_text:
        raise RuntimeError("ViewModel discussion section marker missing")
    method = r"""
    fun updateIssuePlan(
        issueId: String,
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int
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
                actor = user
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

"""
    vm_text = vm_text.replace(marker, method + marker, 1)
vm_path.write_text(vm_text, encoding="utf-8")
'''
    text = text[:start] + "\n" + compatibility + text[end:]
    TARGET.write_text(text, encoding="utf-8")

# One-shot compatibility bootstrap; final product must not retain generator helpers.
try:
    Path(__file__).unlink()
except OSError:
    pass
