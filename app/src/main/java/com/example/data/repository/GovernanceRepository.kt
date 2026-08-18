package com.example.data.repository

import com.example.data.local.GovernanceDao
import com.example.data.model.AppNotification
import com.example.data.model.ApprovalStatus
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationPriority
import com.example.data.model.NotificationStatus
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
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
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.engine.HierarchicalPolicyEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GovernanceRepository(private val dao: GovernanceDao) {

    val enterprises: Flow<List<Enterprise>> = dao.getAllEnterprises()
    val enterprise: Flow<Enterprise?> = dao.getEnterprise()
    val organizations: Flow<List<Organization>> = dao.getAllOrganizations()
    val users: Flow<List<User>> = dao.getAllUsers()
    val teams: Flow<List<Team>> = dao.getAllTeams()
    val repositories: Flow<List<Repository>> = dao.getAllRepositories()
    val auditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    val allAccessRules: Flow<List<RepoAccessRule>> = dao.getAllRepoAccessRules()
    val allOrgMemberships: Flow<List<OrgMembership>> = dao.getAllOrgMemberships()
    val allTeamMemberships: Flow<List<TeamMembership>> = dao.getAllTeamMemberships()
    val allArtifacts: Flow<List<NoCodeArtifact>> = dao.getAllArtifacts()
    val allNotifications: Flow<List<AppNotification>> = dao.getAllNotifications()
    val allIssues: Flow<List<RepoIssue>> = dao.getAllIssues()
    val allDiscussions: Flow<List<RepoDiscussion>> = dao.getAllDiscussions()
    val allReviews: Flow<List<ArtifactReview>> = dao.getAllReviews()
    val allApprovals: Flow<List<ArtifactApproval>> = dao.getAllApprovals()
    val allDependencies: Flow<List<IssueDependency>> = dao.getAllDependencies()

    suspend fun initializeIfEmpty() {
        SampleDataSeeder.seedInitialDataIfEmpty(dao)
    }

    fun getRepositoryById(repoId: String): Flow<Repository?> = dao.getRepositoryById(repoId)
    fun getArtifactsByRepo(repoId: String): Flow<List<NoCodeArtifact>> = dao.getArtifactsByRepo(repoId)
    fun getAccessRulesByRepo(repoId: String): Flow<List<RepoAccessRule>> = dao.getAccessRulesByRepo(repoId)
    fun getReviewsByArtifact(artifactId: String): Flow<List<ArtifactReview>> = dao.getReviewsByArtifact(artifactId)
    fun getApprovalsByArtifact(artifactId: String): Flow<List<ArtifactApproval>> = dao.getApprovalsByArtifact(artifactId)

    suspend fun updateEnterprise(enterprise: Enterprise) {
        dao.updateEnterprise(enterprise)
    }

    suspend fun createRepository(
        name: String,
        displayName: String,
        ownerType: OwnerType,
        ownerId: String,
        ownerDisplayName: String,
        enterpriseId: String,
        description: String,
        category: String,
        creatorUser: User
    ): Pair<Boolean, String> {
        // Enforce Owner Type Rule: ONLY Organization or User can own a Repository!
        if (ownerType != OwnerType.ORGANIZATION && ownerType != OwnerType.USER) {
            val log = AuditLog(
                enterpriseId = enterpriseId,
                actorUserId = creatorUser.id,
                actorDisplayName = creatorUser.displayName,
                actionName = "CREATE_REPOSITORY_ATTEMPT",
                verdict = PolicyVerdict.DENIED_UNAUTHORIZED_OWNER_ENTITY,
                reasoning = "Creation rejected: Only an Organization or User entity can be assigned as Owner of a Repository."
            )
            dao.insertAuditLog(log)
            return Pair(false, "Policy Violation: Only an Organization or User can own a Repository.")
        }

        val newRepo = Repository(
            id = "repo_${UUID.randomUUID().toString().take(8)}",
            name = name.trim().lowercase().replace(" ", "-"),
            displayName = displayName.trim(),
            ownerType = ownerType,
            ownerId = ownerId,
            ownerDisplayName = ownerDisplayName,
            enterpriseId = enterpriseId,
            description = description.trim(),
            category = category,
            requiredApproverCount = if (ownerType == OwnerType.ORGANIZATION) 2 else 1
        )
        dao.insertRepository(newRepo)

        // Add creator access rule
        val creatorRule = RepoAccessRule(
            repoId = newRepo.id,
            granteeType = com.example.data.model.GranteeType.USER,
            granteeId = creatorUser.id,
            granteeName = creatorUser.displayName,
            role = RepoRole.OWNER,
            grantedByUserId = creatorUser.id
        )
        dao.insertRepoAccessRule(creatorRule)

        val log = AuditLog(
            enterpriseId = enterpriseId,
            orgId = if (ownerType == OwnerType.ORGANIZATION) ownerId else null,
            repoId = newRepo.id,
            repoName = newRepo.name,
            actorUserId = creatorUser.id,
            actorDisplayName = creatorUser.displayName,
            actionName = "CREATE_REPOSITORY",
            verdict = PolicyVerdict.ALLOWED,
            reasoning = "Created No-Code Repository '${newRepo.displayName}' with Owner [${ownerType.displayName()}: $ownerDisplayName]."
        )
        dao.insertAuditLog(log)
        return Pair(true, "Repository created successfully!")
    }

    suspend fun addRepoAccessRule(
        repoId: String,
        granteeType: com.example.data.model.GranteeType,
        granteeId: String,
        granteeName: String,
        role: RepoRole,
        grantedByUser: User
    ) {
        val rule = RepoAccessRule(
            repoId = repoId,
            granteeType = granteeType,
            granteeId = granteeId,
            granteeName = granteeName,
            role = role,
            grantedByUserId = grantedByUser.id
        )
        dao.insertRepoAccessRule(rule)

        val repo = dao.getRepositoryByIdOnce(repoId)
        val log = AuditLog(
            enterpriseId = repo?.enterpriseId ?: "",
            orgId = if (repo?.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
            repoId = repoId,
            repoName = repo?.name,
            actorUserId = grantedByUser.id,
            actorDisplayName = grantedByUser.displayName,
            actionName = "MANAGE_ACCESS_RULES",
            verdict = PolicyVerdict.ALLOWED,
            reasoning = "Granted role '${role.name}' to ${granteeType.name} '$granteeName' on repository '${repo?.name}'."
        )
        dao.insertAuditLog(log)
    }

    suspend fun removeRepoAccessRule(rule: RepoAccessRule, actor: User) {
        dao.deleteRepoAccessRule(rule)
        val repo = dao.getRepositoryByIdOnce(rule.repoId)
        val log = AuditLog(
            enterpriseId = repo?.enterpriseId ?: "",
            orgId = if (repo?.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
            repoId = rule.repoId,
            repoName = repo?.name,
            actorUserId = actor.id,
            actorDisplayName = actor.displayName,
            actionName = "REVOKE_ACCESS_RULE",
            verdict = PolicyVerdict.ALLOWED,
            reasoning = "Revoked role '${rule.role.name}' for ${rule.granteeType.name} '${rule.granteeName}' on repository '${repo?.name}'."
        )
        dao.insertAuditLog(log)
    }

    suspend fun createNoCodeArtifact(
        repoId: String,
        title: String,
        type: com.example.data.model.ArtifactType,
        summary: String,
        content: String,
        author: User
    ): Pair<Boolean, String> {
        val repo = dao.getRepositoryByIdOnce(repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        // Policy evaluation
        val evaluation = evaluateAction(
            actor = author,
            repo = repo,
            artifact = null,
            action = GovernanceAction.CREATE_DRAFT
        )

        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            val log = AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = author.id,
                actorDisplayName = author.displayName,
                actionName = "CREATE_DRAFT_ATTEMPT",
                verdict = evaluation.verdict,
                reasoning = evaluation.finalExplanation
            )
            dao.insertAuditLog(log)
            return Pair(false, evaluation.finalExplanation)
        }

        val newArtifact = NoCodeArtifact(
            repoId = repoId,
            title = title.trim(),
            type = type,
            summary = summary.trim(),
            structuredContent = content.trim(),
            lifecycleState = LifecycleState.DRAFT,
            authorUserId = author.id,
            authorDisplayName = author.displayName,
            version = "v1.0.0"
        )
        dao.insertArtifact(newArtifact)

        val log = AuditLog(
            enterpriseId = enterprise.id,
            orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
            repoId = repo.id,
            repoName = repo.name,
            actorUserId = author.id,
            actorDisplayName = author.displayName,
            actionName = "CREATE_DRAFT",
            verdict = PolicyVerdict.ALLOWED,
            reasoning = "Created No-Code Artifact '${newArtifact.title}' (${type.label}) in repository '${repo.name}'."
        )
        dao.insertAuditLog(log)
        return Pair(true, "Draft created successfully")
    }

    suspend fun submitForReview(artifactId: String, actor: User): Pair<Boolean, String> {
        val artifact = dao.getArtifactByIdOnce(artifactId) ?: return Pair(false, "Artifact not found")
        val repo = dao.getRepositoryByIdOnce(artifact.repoId) ?: return Pair(false, "Repo not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(actor, repo, artifact, GovernanceAction.SUBMIT_FOR_REVIEW)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = actor.id,
                    actorDisplayName = actor.displayName,
                    actionName = "SUBMIT_FOR_REVIEW_BLOCKED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        val updated = artifact.copy(
            lifecycleState = LifecycleState.IN_REVIEW,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateArtifact(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "SUBMIT_FOR_REVIEW",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Transitioned '${artifact.title}' to IN_REVIEW lifecycle stage."
            )
        )
        return Pair(true, "Artifact submitted for Reviewer inspection.")
    }

    suspend fun submitReview(
        artifactId: String,
        reviewer: User,
        decision: ReviewDecision,
        feedback: String
    ): Pair<Boolean, String> {
        val artifact = dao.getArtifactByIdOnce(artifactId) ?: return Pair(false, "Artifact not found")
        val repo = dao.getRepositoryByIdOnce(artifact.repoId) ?: return Pair(false, "Repo not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val action = if (decision == ReviewDecision.CHANGES_REQUESTED) GovernanceAction.REQUEST_CHANGES else GovernanceAction.SUBMIT_REVIEW
        val evaluation = evaluateAction(reviewer, repo, artifact, action)

        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = reviewer.id,
                    actorDisplayName = reviewer.displayName,
                    actionName = "SUBMIT_REVIEW_BLOCKED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        val review = ArtifactReview(
            artifactId = artifactId,
            reviewerUserId = reviewer.id,
            reviewerDisplayName = reviewer.displayName,
            decision = decision,
            feedbackNote = feedback.trim()
        )
        dao.insertReview(review)

        val nextState = when (decision) {
            ReviewDecision.APPROVED -> LifecycleState.PENDING_APPROVAL
            ReviewDecision.CHANGES_REQUESTED -> LifecycleState.DRAFT
            ReviewDecision.COMMENTED -> artifact.lifecycleState
        }
        val updated = artifact.copy(
            lifecycleState = nextState,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateArtifact(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = reviewer.id,
                actorDisplayName = reviewer.displayName,
                actionName = "SUBMIT_REVIEW",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Reviewer ${reviewer.displayName} submitted review with decision ${decision.name}. Artifact state updated to ${nextState.name}."
            )
        )
        return Pair(true, "Review recorded successfully!")
    }

    suspend fun submitApproverSignOff(
        artifactId: String,
        approver: User
    ): Pair<Boolean, String> {
        val artifact = dao.getArtifactByIdOnce(artifactId) ?: return Pair(false, "Artifact not found")
        val repo = dao.getRepositoryByIdOnce(artifact.repoId) ?: return Pair(false, "Repo not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(approver, repo, artifact, GovernanceAction.SUBMIT_FINAL_APPROVAL)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = approver.id,
                    actorDisplayName = approver.displayName,
                    actionName = "APPROVAL_SIGN_OFF_BLOCKED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        // Add approval record
        val approval = ArtifactApproval(
            artifactId = artifactId,
            approverUserId = approver.id,
            approverDisplayName = approver.displayName,
            approverTitle = approver.title,
            status = ApprovalStatus.APPROVED
        )
        dao.insertApproval(approval)

        val approvals = dao.getApprovalsByArtifactOnce(artifactId)
        val requiredCount = if (enterprise.enforceDualApproval) maxOf(repo.requiredApproverCount, 2) else repo.requiredApproverCount
        val distinctApprovedCount = approvals.filter { it.status == ApprovalStatus.APPROVED }.distinctBy { it.approverUserId }.size

        val isNowApproved = distinctApprovedCount >= requiredCount
        val nextState = if (isNowApproved) LifecycleState.APPROVED else LifecycleState.PENDING_APPROVAL

        val updated = artifact.copy(
            lifecycleState = nextState,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateArtifact(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = approver.id,
                actorDisplayName = approver.displayName,
                actionName = "SUBMIT_FINAL_APPROVAL",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Approver ${approver.displayName} granted signature ($distinctApprovedCount of $requiredCount signatures collected). State: ${nextState.name}."
            )
        )

        return Pair(true, if (isNowApproved) "All required approvals acquired! Artifact is ready to publish." else "Approval recorded ($distinctApprovedCount/$requiredCount signatures).")
    }

    suspend fun publishAndLock(artifactId: String, actor: User): Pair<Boolean, String> {
        val artifact = dao.getArtifactByIdOnce(artifactId) ?: return Pair(false, "Artifact not found")
        val repo = dao.getRepositoryByIdOnce(artifact.repoId) ?: return Pair(false, "Repo not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(actor, repo, artifact, GovernanceAction.PUBLISH_AND_LOCK)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = actor.id,
                    actorDisplayName = actor.displayName,
                    actionName = "PUBLISH_LOCK_BLOCKED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        val updated = artifact.copy(
            lifecycleState = LifecycleState.PUBLISHED,
            lockedByPolicy = true,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateArtifact(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "PUBLISH_AND_LOCK",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Artifact '${artifact.title}' published and locked under cryptographic policy control by ${actor.displayName}."
            )
        )
        return Pair(true, "Artifact published & locked successfully!")
    }

    suspend fun evaluateAction(
        actor: User,
        repo: Repository,
        artifact: NoCodeArtifact?,
        action: GovernanceAction
    ): PolicyEvaluationDetail {
        val enterprise = dao.getEnterpriseOnce() ?: Enterprise(name = "Default", slug = "default", description = "")
        val reviews = if (artifact != null) dao.getReviewsByArtifactOnce(artifact.id) else emptyList()
        val approvals = if (artifact != null) dao.getApprovalsByArtifactOnce(artifact.id) else emptyList()
        val orgMemberships = dao.getAllOrgMembershipsOnce()
        val teamMemberships = dao.getAllTeamMembershipsOnce()
        val teams = dao.getAllTeamsOnce()
        val accessRules = dao.getAllRepoAccessRulesOnce()

        return HierarchicalPolicyEngine.evaluateAction(
            enterprise = enterprise,
            actor = actor,
            repo = repo,
            artifact = artifact,
            reviews = reviews,
            approvals = approvals,
            orgMemberships = orgMemberships,
            teamMemberships = teamMemberships,
            teams = teams,
            accessRules = accessRules,
            action = action
        )
    }

    suspend fun createTeam(
        orgId: String,
        name: String,
        slug: String,
        description: String,
        actor: User
    ): Pair<Boolean, String> {
        val team = Team(
            orgId = orgId,
            name = name.trim(),
            slug = slug.trim().lowercase().replace(" ", "-"),
            description = description.trim(),
            canOwnerRepository = false
        )
        dao.insertTeam(team)
        return Pair(true, "Team '${team.name}' created.")
    }

    suspend fun addTeamMember(teamId: String, userId: String, role: TeamRole) {
        val membership = TeamMembership(
            teamId = teamId,
            userId = userId,
            role = role
        )
        dao.insertTeamMembership(membership)
    }

    // --- REPO ISSUES & HIERARCHY METHODS ---
    fun getIssuesByRepo(repoId: String): Flow<List<RepoIssue>> = dao.getIssuesByRepo(repoId)
    fun getIssueComments(issueId: String): Flow<List<IssueComment>> = dao.getCommentsByIssue(issueId)
    fun getDependenciesByRepo(repoId: String): Flow<List<IssueDependency>> = dao.getDependenciesByRepo(repoId)
    fun getSubIssues(parentIssueId: String): Flow<List<RepoIssue>> = dao.getSubIssues(parentIssueId)

    suspend fun createIssue(
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
        author: User
    ): Pair<Boolean, String> {
        val repo = dao.getRepositoryByIdOnce(repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(author, repo, null, GovernanceAction.CREATE_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = author.id,
                    actorDisplayName = author.displayName,
                    actionName = "CREATE_ISSUE_DENIED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        // Validate parent issue scoping
        var parentNum: Int? = null
        var parentTitle: String? = null
        if (parentIssueId != null) {
            val parent = dao.getIssueByIdOnce(parentIssueId)
            if (parent == null) {
                return Pair(false, "Parent issue not found.")
            }
            if (parent.repoId != repoId) {
                return Pair(false, "Parent issue must belong to the same repository container.")
            }
            parentNum = parent.issueNumber
            parentTitle = parent.title
        }

        val count = dao.getIssueCountByRepo(repoId)
        val newIssue = RepoIssue(
            repoId = repoId,
            issueNumber = count + 1,
            title = title.trim(),
            description = description.trim(),
            status = IssueStatus.OPEN,
            priority = priority,
            authorUserId = author.id,
            authorDisplayName = author.displayName,
            authorRole = evaluation.effectiveRole.name,
            assigneeType = assigneeType,
            assigneeId = assigneeId,
            assigneeName = assigneeName,
            linkedArtifactId = linkedArtifactId,
            linkedArtifactTitle = linkedArtifactTitle,
            parentIssueId = parentIssueId,
            parentIssueNumber = parentNum,
            parentIssueTitle = parentTitle,
            labels = labels.ifBlank { "governance" }
        )
        dao.insertIssue(newIssue)

        val assignDetail = if (assigneeName != null) " (Assigned to ${assigneeType?.name ?: "USER"}: $assigneeName)" else ""
        val parentDetail = if (parentNum != null) " (Sub-issue of #$parentNum)" else ""
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = author.id,
                actorDisplayName = author.displayName,
                actionName = "CREATE_ISSUE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Created Issue #${newIssue.issueNumber} '${newIssue.title}' in repo '${repo.name}'$parentDetail$assignDetail."
            )
        )
        return Pair(true, "Issue #${newIssue.issueNumber} created successfully.")
    }

    suspend fun linkParentIssue(
        issueId: String,
        parentIssueId: String?,
        actor: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Target issue not found.")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found.")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found.")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ASSIGN_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        if (parentIssueId == null) {
            // Unlink parent
            val updated = issue.copy(
                parentIssueId = null,
                parentIssueNumber = null,
                parentIssueTitle = null,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateIssue(updated)
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = actor.id,
                    actorDisplayName = actor.displayName,
                    actionName = "UNLINK_PARENT_ISSUE",
                    verdict = PolicyVerdict.ALLOWED,
                    reasoning = "Unlinked parent issue relationship from Issue #${issue.issueNumber}."
                )
            )
            return Pair(true, "Parent issue removed.")
        }

        if (parentIssueId == issueId) {
            return Pair(false, "An issue cannot be its own parent.")
        }

        val parent = dao.getIssueByIdOnce(parentIssueId) ?: return Pair(false, "Parent issue not found.")
        if (parent.repoId != issue.repoId) {
            return Pair(false, "Parent issue must reside within the same repository.")
        }

        // Nested tasks may have arbitrary depth, but the hierarchy must stay acyclic.
        val repoIssues = dao.getIssuesByRepoOnce(issue.repoId)
        if (!IssueHierarchyRules.canAssignParent(issueId, parentIssueId, repoIssues)) {
            return Pair(false, "無法將任務設為自己或其任一子孫任務的下層，避免形成循環階層。")
        }

        val updated = issue.copy(
            parentIssueId = parent.id,
            parentIssueNumber = parent.issueNumber,
            parentIssueTitle = parent.title,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateIssue(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "LINK_PARENT_ISSUE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Set Issue #${parent.issueNumber} ('${parent.title}') as parent for Issue #${issue.issueNumber}."
            )
        )
        return Pair(true, "Linked to Parent Issue #${parent.issueNumber}.")
    }

    suspend fun addIssueDependency(
        repoId: String,
        blockedIssueId: String,
        blockingIssueId: String,
        actor: User
    ): Pair<Boolean, String> {
        val repo = dao.getRepositoryByIdOnce(repoId) ?: return Pair(false, "Repository not found.")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found.")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ASSIGN_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        if (blockedIssueId == blockingIssueId) {
            return Pair(false, "An issue cannot block itself.")
        }

        val blockedIssue = dao.getIssueByIdOnce(blockedIssueId) ?: return Pair(false, "Target blocked issue not found.")
        val blockingIssue = dao.getIssueByIdOnce(blockingIssueId) ?: return Pair(false, "Prerequisite blocking issue not found.")

        if (blockedIssue.repoId != repoId || blockingIssue.repoId != repoId) {
            return Pair(false, "Both issues must strictly belong to this repository container.")
        }

        // Check for direct inverted dependency (blockingIssue is already blocked by blockedIssue)
        val existingReverse = dao.getBlockedByForIssueOnce(blockingIssueId)
        if (existingReverse.any { it.blockingIssueId == blockedIssueId }) {
            return Pair(false, "Circular dependency conflict: Issue #${blockingIssue.issueNumber} is already blocked by Issue #${blockedIssue.issueNumber}.")
        }

        val dependency = IssueDependency(
            repoId = repoId,
            blockedIssueId = blockedIssueId,
            blockingIssueId = blockingIssueId,
            dependencyType = com.example.data.model.DependencyType.BLOCKS,
            createdByUserId = actor.id,
            createdByDisplayName = actor.displayName
        )
        dao.insertIssueDependency(dependency)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "ADD_ISSUE_DEPENDENCY",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Established dependency: Issue #${blockingIssue.issueNumber} ('${blockingIssue.title}') blocks Issue #${blockedIssue.issueNumber} ('${blockedIssue.title}')."
            )
        )
        return Pair(true, "Added dependency: #${blockingIssue.issueNumber} blocks #${blockedIssue.issueNumber}.")
    }

    suspend fun removeIssueDependency(
        dependencyId: String,
        actor: User
    ): Pair<Boolean, String> {
        val allDeps = dao.getAllEnterprisesOnce() // just to verify connection
        dao.deleteIssueDependencyById(dependencyId)
        return Pair(true, "Dependency removed.")
    }

    suspend fun addIssueComment(
        issueId: String,
        content: String,
        author: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Issue not found")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found")

        val evaluation = evaluateAction(author, repo, null, GovernanceAction.COMMENT_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        val comment = IssueComment(
            issueId = issueId,
            authorUserId = author.id,
            authorDisplayName = author.displayName,
            authorRole = evaluation.effectiveRole.name,
            content = content.trim()
        )
        dao.insertIssueComment(comment)

        // Update issue timestamp
        val updated = issue.copy(updatedAt = System.currentTimeMillis())
        dao.updateIssue(updated)
        return Pair(true, "Comment added.")
    }

    suspend fun updateIssueStatus(
        issueId: String,
        newStatus: IssueStatus,
        actor: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Issue not found")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.CLOSE_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED && issue.authorUserId != actor.id) {
            return Pair(false, evaluation.finalExplanation)
        }

        val isClosing = newStatus == IssueStatus.CLOSED
        val updated = issue.copy(
            status = newStatus,
            closedAt = if (isClosing) System.currentTimeMillis() else null,
            closedByUserId = if (isClosing) actor.id else null,
            closedByDisplayName = if (isClosing) actor.displayName else null,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateIssue(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = if (isClosing) "CLOSE_ISSUE" else "REOPEN_ISSUE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "${if (isClosing) "已完成" else "Reopened"} Issue #${issue.issueNumber} '${issue.title}'."
            )
        )
        return Pair(true, "Issue #${issue.issueNumber} updated to ${newStatus.label}.")
    }

    suspend fun assignIssue(
        issueId: String,
        assigneeType: GranteeType?,
        assigneeId: String?,
        assigneeName: String?,
        actor: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Issue not found")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ASSIGN_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        val updated = issue.copy(
            assigneeType = assigneeType,
            assigneeId = assigneeId,
            assigneeName = assigneeName,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateIssue(updated)

        val target = if (assigneeName != null) "${assigneeType?.name}: $assigneeName" else "未指派"
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "ASSIGN_ISSUE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Reassigned Issue #${issue.issueNumber} to $target."
            )
        )
        return Pair(true, "Issue #${issue.issueNumber} assigned to $target.")
    }

    // --- REPO DISCUSSIONS METHODS ---
    fun getDiscussionsByRepo(repoId: String): Flow<List<RepoDiscussion>> = dao.getDiscussionsByRepo(repoId)
    fun getDiscussionComments(discussionId: String): Flow<List<DiscussionComment>> = dao.getCommentsByDiscussion(discussionId)

    suspend fun createDiscussion(
        repoId: String,
        title: String,
        category: DiscussionCategory,
        body: String,
        author: User
    ): Pair<Boolean, String> {
        val repo = dao.getRepositoryByIdOnce(repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(author, repo, null, GovernanceAction.CREATE_DISCUSSION)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = enterprise.id,
                    orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                    repoId = repo.id,
                    repoName = repo.name,
                    actorUserId = author.id,
                    actorDisplayName = author.displayName,
                    actionName = "CREATE_DISCUSSION_DENIED",
                    verdict = evaluation.verdict,
                    reasoning = evaluation.finalExplanation
                )
            )
            return Pair(false, evaluation.finalExplanation)
        }

        val count = dao.getDiscussionCountByRepo(repoId)
        val discussion = RepoDiscussion(
            repoId = repoId,
            discussionNumber = count + 1,
            title = title.trim(),
            category = category,
            body = body.trim(),
            authorUserId = author.id,
            authorDisplayName = author.displayName,
            authorRole = evaluation.effectiveRole.name,
            upvoteCount = 1
        )
        dao.insertDiscussion(discussion)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = author.id,
                actorDisplayName = author.displayName,
                actionName = "CREATE_DISCUSSION",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Started Discussion #${discussion.discussionNumber} '${discussion.title}' under category [${category.label}]."
            )
        )
        return Pair(true, "Discussion #${discussion.discussionNumber} posted.")
    }

    suspend fun addDiscussionComment(
        discussionId: String,
        content: String,
        author: User
    ): Pair<Boolean, String> {
        val discussion = dao.getDiscussionByIdOnce(discussionId) ?: return Pair(false, "Discussion not found")
        val repo = dao.getRepositoryByIdOnce(discussion.repoId) ?: return Pair(false, "Repository not found")

        if (discussion.isLocked) {
            val (role, _) = HierarchicalPolicyEngine.resolveEffectiveRole(
                actor = author,
                repo = repo,
                orgMemberships = dao.getAllOrgMembershipsOnce(),
                teamMemberships = dao.getAllTeamMembershipsOnce(),
                teams = dao.getAllTeamsOnce(),
                accessRules = dao.getAllRepoAccessRulesOnce()
            )
            if (!role.canPerform(RepoRole.MAINTAINER)) {
                return Pair(false, "This discussion thread is locked by maintainers. Only Maintainers and Owners can post replies.")
            }
        }

        val evaluation = evaluateAction(author, repo, null, GovernanceAction.COMMENT_DISCUSSION)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        val comment = DiscussionComment(
            discussionId = discussionId,
            authorUserId = author.id,
            authorDisplayName = author.displayName,
            authorRole = evaluation.effectiveRole.name,
            content = content.trim()
        )
        dao.insertDiscussionComment(comment)

        // Update discussion timestamp
        val updated = discussion.copy(updatedAt = System.currentTimeMillis())
        dao.updateDiscussion(updated)
        return Pair(true, "Reply added.")
    }

    suspend fun toggleLockDiscussion(
        discussionId: String,
        actor: User
    ): Pair<Boolean, String> {
        val discussion = dao.getDiscussionByIdOnce(discussionId) ?: return Pair(false, "Discussion not found")
        val repo = dao.getRepositoryByIdOnce(discussion.repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.LOCK_DISCUSSION)
        if (evaluation.verdict != PolicyVerdict.ALLOWED) {
            return Pair(false, evaluation.finalExplanation)
        }

        val newLocked = !discussion.isLocked
        val updated = discussion.copy(isLocked = newLocked, updatedAt = System.currentTimeMillis())
        dao.updateDiscussion(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = if (newLocked) "LOCK_DISCUSSION" else "UNLOCK_DISCUSSION",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "${if (newLocked) "Locked" else "Unlocked"} Discussion #${discussion.discussionNumber} '${discussion.title}'."
            )
        )
        return Pair(true, if (newLocked) "Discussion thread locked." else "Discussion thread unlocked.")
    }

    suspend fun markAcceptedAnswer(
        discussionId: String,
        commentId: String,
        actor: User
    ): Pair<Boolean, String> {
        val discussion = dao.getDiscussionByIdOnce(discussionId) ?: return Pair(false, "Discussion not found")
        val repo = dao.getRepositoryByIdOnce(discussion.repoId) ?: return Pair(false, "Repository not found")

        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ACCEPT_DISCUSSION_ANSWER)
        if (evaluation.verdict != PolicyVerdict.ALLOWED && discussion.authorUserId != actor.id) {
            return Pair(false, "Only the author of the discussion, or Collaborators/Maintainers, can mark an accepted answer.")
        }

        val comments = dao.getCommentsByDiscussionOnce(discussionId)
        comments.forEach { c ->
            if (c.id == commentId) {
                dao.updateDiscussionComment(c.copy(isAcceptedAnswer = !c.isAcceptedAnswer))
            } else if (c.isAcceptedAnswer) {
                dao.updateDiscussionComment(c.copy(isAcceptedAnswer = false))
            }
        }

        val targetComment = comments.firstOrNull { it.id == commentId }
        val isNowAccepted = targetComment?.isAcceptedAnswer != true
        val updatedDiscussion = discussion.copy(
            isAnswered = isNowAccepted,
            acceptedAnswerCommentId = if (isNowAccepted) commentId else null,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateDiscussion(updatedDiscussion)
        return Pair(true, if (isNowAccepted) "Marked as accepted answer." else "Removed accepted answer.")
    }

    suspend fun upvoteDiscussion(discussionId: String) {
        val discussion = dao.getDiscussionByIdOnce(discussionId) ?: return
        dao.updateDiscussion(discussion.copy(upvoteCount = discussion.upvoteCount + 1))
    }

    suspend fun upvoteDiscussionComment(commentId: String, discussionId: String) {
        val comments = dao.getCommentsByDiscussionOnce(discussionId)
        val comment = comments.firstOrNull { it.id == commentId } ?: return
        dao.updateDiscussionComment(comment.copy(upvotes = comment.upvotes + 1))
    }

    // =========================================================================
    // ENTERPRISE CREATION & GOVERNANCE LIFECYCLE
    // =========================================================================

    suspend fun createEnterprise(
        name: String,
        slug: String,
        description: String,
        enforceDualApproval: Boolean,
        allowUserOwnedRepos: Boolean,
        enforceReviewerBeforeApprover: Boolean,
        enforceSegregationOfDuties: Boolean,
        creatorUser: User
    ): Pair<Boolean, String> {
        val cleanSlug = slug.trim().lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
        val entId = "ent_${cleanSlug.take(16)}_${UUID.randomUUID().toString().take(6)}"

        val newEnterprise = Enterprise(
            id = entId,
            name = name.trim(),
            slug = cleanSlug,
            description = description.trim(),
            enforceDualApproval = enforceDualApproval,
            allowUserOwnedRepos = allowUserOwnedRepos,
            enforceReviewerBeforeApprover = enforceReviewerBeforeApprover,
            enforceSegregationOfDuties = enforceSegregationOfDuties
        )
        dao.insertEnterprise(newEnterprise)

        // Ensure creator has admin credentials on enterprise
        val updatedUser = creatorUser.copy(
            enterpriseId = newEnterprise.id,
            isEnterpriseAdmin = true
        )
        dao.insertUser(updatedUser)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = newEnterprise.id,
                actorUserId = creatorUser.id,
                actorDisplayName = creatorUser.displayName,
                actionName = "CREATE_ENTERPRISE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Established new Root Enterprise '${newEnterprise.name}' with policy baseline (DualApproval=$enforceDualApproval, SoD=$enforceSegregationOfDuties, UserOwnedRepos=$allowUserOwnedRepos)."
            )
        )
        return Pair(true, "Enterprise '${newEnterprise.name}' created.")
    }

    suspend fun updateEnterpriseSecurityPolicies(
        enterprise: Enterprise,
        actor: User
    ): Pair<Boolean, String> {
        dao.updateEnterprise(enterprise)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "UPDATE_ENTERPRISE_POLICIES",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Updated Enterprise '${enterprise.name}' security governance policies: Dual Approval=${enterprise.enforceDualApproval}, SoD=${enterprise.enforceSegregationOfDuties}, User Repos=${enterprise.allowUserOwnedRepos}, Reviewer Gates=${enterprise.enforceReviewerBeforeApprover}."
            )
        )
        return Pair(true, "Enterprise policies updated successfully.")
    }

    suspend fun createEnterpriseUser(
        enterpriseId: String,
        username: String,
        displayName: String,
        email: String,
        title: String,
        isEnterpriseAdmin: Boolean,
        avatarColorHex: String,
        actor: User
    ): Pair<Boolean, String> {
        val cleanUsername = username.trim().lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
        val newUser = User(
            id = "usr_${cleanUsername.take(16)}_${UUID.randomUUID().toString().take(6)}",
            enterpriseId = enterpriseId,
            username = cleanUsername,
            displayName = displayName.trim(),
            email = email.trim().lowercase(),
            title = title.trim(),
            avatarColorHex = avatarColorHex,
            isEnterpriseAdmin = isEnterpriseAdmin,
            canOwnerRepository = true
        )
        dao.insertUser(newUser)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterpriseId,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "CREATE_USER",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Provisioned new Enterprise identity: ${newUser.displayName} (@${newUser.username}, Admin=$isEnterpriseAdmin)."
            )
        )
        return Pair(true, "User ${newUser.displayName} created.")
    }

    // =========================================================================
    // ORGANIZATION CREATION & GOVERNANCE LIFECYCLE
    // =========================================================================

    suspend fun createOrganization(
        enterpriseId: String,
        name: String,
        slug: String,
        description: String,
        badgeColorHex: String,
        defaultMemberRole: RepoRole,
        creatorUser: User,
        ownerUserId: String
    ): Pair<Boolean, String> {
        val cleanSlug = slug.trim().lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
        val orgId = "org_${cleanSlug.take(16)}_${UUID.randomUUID().toString().take(6)}"

        val newOrg = Organization(
            id = orgId,
            enterpriseId = enterpriseId,
            name = name.trim(),
            slug = cleanSlug,
            description = description.trim(),
            badgeColorHex = badgeColorHex,
            defaultMemberRole = defaultMemberRole,
            canOwnerRepository = true // Organizations CAN own repositories
        )
        dao.insertOrganization(newOrg)

        // Assign Organization Owner
        val initialOwnerId = if (ownerUserId.isNotBlank()) ownerUserId else creatorUser.id
        val ownerMem = OrgMembership(
            orgId = newOrg.id,
            userId = initialOwnerId,
            role = OrgRole.OWNER
        )
        dao.insertOrgMembership(ownerMem)

        // If creator is distinct from owner, also add creator as Admin
        if (creatorUser.id != initialOwnerId) {
            val creatorMem = OrgMembership(
                orgId = newOrg.id,
                userId = creatorUser.id,
                role = OrgRole.ADMIN
            )
            dao.insertOrgMembership(creatorMem)
        }

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterpriseId,
                orgId = newOrg.id,
                actorUserId = creatorUser.id,
                actorDisplayName = creatorUser.displayName,
                actionName = "CREATE_ORGANIZATION",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Established new Organization '${newOrg.name}' (@${newOrg.slug}) under Enterprise. Default Member Repo Role: ${defaultMemberRole.name}."
            )
        )
        return Pair(true, "Organization '${newOrg.name}' created.")
    }

    suspend fun updateOrganization(
        org: Organization,
        actor: User
    ): Pair<Boolean, String> {
        dao.updateOrganization(org)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org.enterpriseId,
                orgId = org.id,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "UPDATE_ORGANIZATION",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Updated Organization '${org.name}' settings (Default Member Role: ${org.defaultMemberRole.name})."
            )
        )
        return Pair(true, "Organization updated.")
    }

    suspend fun addOrgMember(
        orgId: String,
        userId: String,
        role: OrgRole,
        actor: User
    ): Pair<Boolean, String> {
        val org = dao.getOrganizationByIdOnce(orgId) ?: return Pair(false, "Organization not found")
        val user = dao.getUserByIdOnce(userId) ?: return Pair(false, "User not found")

        val existing = dao.getOrgMembership(orgId, userId)
        if (existing != null) {
            val updated = existing.copy(role = role)
            dao.updateOrgMembership(updated)
            dao.insertAuditLog(
                AuditLog(
                    enterpriseId = org.enterpriseId,
                    orgId = org.id,
                    actorUserId = actor.id,
                    actorDisplayName = actor.displayName,
                    actionName = "UPDATE_ORG_MEMBER_ROLE",
                    verdict = PolicyVerdict.ALLOWED,
                    reasoning = "Updated ${user.displayName}'s role to ${role.name} in Organization '${org.name}'."
                )
            )
            return Pair(true, "Updated ${user.displayName}'s role to ${role.name}.")
        }

        val membership = OrgMembership(
            orgId = orgId,
            userId = userId,
            role = role
        )
        dao.insertOrgMembership(membership)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org.enterpriseId,
                orgId = org.id,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "ADD_ORG_MEMBER",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Added ${user.displayName} to Organization '${org.name}' with role ${role.name}."
            )
        )
        return Pair(true, "Added ${user.displayName} to ${org.name}.")
    }

    suspend fun removeOrgMember(
        orgId: String,
        userId: String,
        actor: User
    ): Pair<Boolean, String> {
        val org = dao.getOrganizationByIdOnce(orgId) ?: return Pair(false, "Organization not found")
        val user = dao.getUserByIdOnce(userId) ?: return Pair(false, "User not found")

        dao.deleteOrgMembership(orgId, userId)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org.enterpriseId,
                orgId = org.id,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "REMOVE_ORG_MEMBER",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Removed ${user.displayName} from Organization '${org.name}'."
            )
        )
        return Pair(true, "Removed ${user.displayName} from ${org.name}.")
    }

    // =========================================================================
    // TEAM CREATION & MEMBERSHIP LIFECYCLE
    // =========================================================================

    suspend fun createTeam(
        orgId: String,
        name: String,
        slug: String,
        description: String,
        parentTeamId: String?,
        creatorUser: User
    ): Pair<Boolean, String> {
        val org = dao.getOrganizationByIdOnce(orgId) ?: return Pair(false, "Organization not found")
        val cleanSlug = slug.trim().lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")

        val newTeam = Team(
            id = "team_${cleanSlug.take(16)}_${UUID.randomUUID().toString().take(6)}",
            orgId = orgId,
            parentTeamId = if (parentTeamId.isNullOrBlank()) null else parentTeamId,
            name = name.trim(),
            slug = cleanSlug,
            description = description.trim(),
            canOwnerRepository = false // Strictly False: Teams CANNOT own repositories
        )
        dao.insertTeam(newTeam)

        // Assign creator as Team Maintainer
        val teamMem = TeamMembership(
            teamId = newTeam.id,
            userId = creatorUser.id,
            role = TeamRole.MAINTAINER
        )
        dao.insertTeamMembership(teamMem)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org.enterpriseId,
                orgId = org.id,
                actorUserId = creatorUser.id,
                actorDisplayName = creatorUser.displayName,
                actionName = "CREATE_TEAM",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Created Team '${newTeam.name}' (@${newTeam.slug}) under Organization '${org.name}'."
            )
        )
        return Pair(true, "Team '${newTeam.name}' created.")
    }

    suspend fun addTeamMember(
        teamId: String,
        userId: String,
        role: TeamRole,
        actor: User
    ): Pair<Boolean, String> {
        val teams = dao.getAllTeamsOnce()
        val team = teams.firstOrNull { it.id == teamId } ?: return Pair(false, "Team not found")
        val user = dao.getUserByIdOnce(userId) ?: return Pair(false, "User not found")
        val org = dao.getOrganizationByIdOnce(team.orgId)

        // Also ensure user is in the Org
        val orgMem = dao.getOrgMembership(team.orgId, userId)
        if (orgMem == null) {
            dao.insertOrgMembership(OrgMembership(orgId = team.orgId, userId = userId, role = OrgRole.MEMBER))
        }

        val existingMemberships = dao.getAllTeamMembershipsOnce()
        val existing = existingMemberships.firstOrNull { it.teamId == teamId && it.userId == userId }
        if (existing != null) {
            dao.updateTeamMembership(existing.copy(role = role))
        } else {
            val mem = TeamMembership(
                teamId = teamId,
                userId = userId,
                role = role
            )
            dao.insertTeamMembership(mem)
        }

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org?.enterpriseId ?: "",
                orgId = team.orgId,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "ADD_TEAM_MEMBER",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Added ${user.displayName} to Team '${team.name}' with role ${role.name}."
            )
        )
        return Pair(true, "Added ${user.displayName} to ${team.name}.")
    }

    suspend fun removeTeamMember(
        teamId: String,
        userId: String,
        actor: User
    ): Pair<Boolean, String> {
        val teams = dao.getAllTeamsOnce()
        val team = teams.firstOrNull { it.id == teamId } ?: return Pair(false, "Team not found")
        val user = dao.getUserByIdOnce(userId) ?: return Pair(false, "User not found")
        val org = dao.getOrganizationByIdOnce(team.orgId)

        dao.deleteTeamMembership(teamId, userId)
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = org?.enterpriseId ?: "",
                orgId = team.orgId,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "REMOVE_TEAM_MEMBER",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Removed ${user.displayName} from Team '${team.name}'."
            )
        )
        return Pair(true, "Removed ${user.displayName} from ${team.name}.")
    }

    // =========================================================================
    // USER PROFILE & IDENTITY OPERATIONS
    // =========================================================================

    fun getUserById(userId: String): Flow<User?> = dao.getUserById(userId)
    fun getArtifactsByAuthor(userId: String): Flow<List<NoCodeArtifact>> = dao.getArtifactsByAuthor(userId)
    fun getReviewsByReviewer(userId: String): Flow<List<ArtifactReview>> = dao.getReviewsByReviewer(userId)
    fun getApprovalsByApprover(userId: String): Flow<List<ArtifactApproval>> = dao.getApprovalsByApprover(userId)
    fun getAuditLogsByActor(userId: String): Flow<List<AuditLog>> = dao.getAuditLogsByActor(userId)
    fun getIssuesByAuthor(userId: String): Flow<List<RepoIssue>> = dao.getIssuesByAuthor(userId)
    fun getDiscussionsByAuthor(userId: String): Flow<List<RepoDiscussion>> = dao.getDiscussionsByAuthor(userId)

    suspend fun updateUserProfile(
        user: User,
        displayName: String,
        title: String,
        bio: String,
        location: String,
        pronouns: String,
        avatarColorHex: String,
        notificationPreferences: String,
        actor: User
    ): Pair<Boolean, String> {
        val updated = user.copy(
            displayName = displayName.trim().ifEmpty { user.displayName },
            title = title.trim().ifEmpty { user.title },
            bio = bio.trim(),
            location = location.trim(),
            pronouns = pronouns.trim(),
            avatarColorHex = avatarColorHex,
            notificationPreferences = notificationPreferences
        )
        dao.updateUser(updated)

        dao.insertAuditLog(
            AuditLog(
                enterpriseId = user.enterpriseId,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "UPDATE_USER_PROFILE",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Updated profile information for user ${user.username} (${updated.displayName})."
            )
        )
        return Pair(true, "Profile updated successfully.")
    }

    // =========================================================================
    // NOTIFICATIONS & UNIFIED INBOX CAPABILITY
    // =========================================================================

    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> = dao.getNotificationsForUser(userId)
    fun getUnreadNotificationsForUser(userId: String): Flow<List<AppNotification>> = dao.getUnreadNotificationsForUser(userId)
    fun getUnreadCountForUser(userId: String): Flow<Int> = dao.getUnreadCountForUser(userId)

    suspend fun markNotificationAsRead(id: String) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        dao.markAllNotificationsAsReadForUser(userId)
    }

    suspend fun archiveNotification(id: String) {
        dao.archiveNotification(id)
    }

    suspend fun deleteNotification(id: String) {
        dao.deleteNotificationById(id)
    }

    suspend fun markActionCompleted(id: String) {
        dao.markActionCompleted(id)
    }

    suspend fun sendNotification(
        recipientUserId: String,
        actor: User,
        category: NotificationCategory,
        title: String,
        body: String,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        isActionable: Boolean = false,
        actionType: String? = null,
        enterpriseId: String? = null,
        orgId: String? = null,
        orgName: String? = null,
        teamId: String? = null,
        teamName: String? = null,
        repoId: String? = null,
        repoName: String? = null,
        artifactId: String? = null,
        artifactTitle: String? = null,
        issueId: String? = null,
        issueTitle: String? = null,
        discussionId: String? = null,
        discussionTitle: String? = null,
        reviewId: String? = null,
        approvalId: String? = null,
        membershipId: String? = null
    ) {
        // Do not self-notify
        if (recipientUserId == actor.id) return

        val notif = AppNotification(
            recipientUserId = recipientUserId,
            actorUserId = actor.id,
            actorDisplayName = actor.displayName,
            actorAvatarColorHex = actor.avatarColorHex,
            category = category,
            priority = priority,
            status = NotificationStatus.UNREAD,
            title = title,
            body = body,
            isActionable = isActionable,
            actionType = actionType,
            enterpriseId = enterpriseId,
            orgId = orgId,
            orgName = orgName,
            teamId = teamId,
            teamName = teamName,
            repoId = repoId,
            repoName = repoName,
            artifactId = artifactId,
            artifactTitle = artifactTitle,
            issueId = issueId,
            issueTitle = issueTitle,
            discussionId = discussionId,
            discussionTitle = discussionTitle,
            reviewId = reviewId,
            approvalId = approvalId,
            membershipId = membershipId,
            createdAt = System.currentTimeMillis()
        )
        dao.insertNotification(notif)
    }
}
