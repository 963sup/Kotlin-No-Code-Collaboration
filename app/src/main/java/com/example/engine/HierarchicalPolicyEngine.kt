package com.example.engine

import com.example.data.model.ApprovalStatus
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.GranteeType
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
import com.example.data.model.OwnerType
import com.example.data.model.PolicyCheckItem
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User

object HierarchicalPolicyEngine {

    /**
     * Resolves the effective repository role for a user using GitHub's hierarchical model:
     * 1. Personal Ownership: If repo owned by user -> OWNER
     * 2. Direct User Grant: Explicit RepoAccessRule for this user
     * 3. Team Grants: RepoAccessRules granted to Teams the user belongs to
     * 4. Organization Membership: Org Owner/Admin -> MAINTAINER, Member -> COLLABORATOR
     * 5. Enterprise Admin: Enterprise Admin -> MAINTAINER
     * 6. Default Fallback: VIEWER
     */
    fun resolveEffectiveRole(
        actor: User,
        repo: Repository,
        orgMemberships: List<OrgMembership>,
        teamMemberships: List<TeamMembership>,
        teams: List<Team>,
        accessRules: List<RepoAccessRule>
    ): Pair<RepoRole, String> {
        // 1. Is User direct Owner of Personal Repo?
        if (repo.ownerType == OwnerType.USER && repo.ownerId == actor.id) {
            return Pair(RepoRole.OWNER, "Direct Repository Owner (Personal User Account)")
        }

        var highestRole = RepoRole.VIEWER
        var roleSource = "Default Public / Viewer Access"

        // 2. Organization-level inheritance (if repo is Org-owned)
        if (repo.ownerType == OwnerType.ORGANIZATION) {
            val orgMembership = orgMemberships.firstOrNull { it.orgId == repo.ownerId && it.userId == actor.id }
            if (orgMembership != null) {
                when (orgMembership.role) {
                    OrgRole.OWNER, OrgRole.ADMIN -> {
                        highestRole = RepoRole.MAINTAINER
                        roleSource = "Inherited from Organization ${orgMembership.role.name} (${repo.ownerDisplayName})"
                    }
                    OrgRole.MEMBER -> {
                        highestRole = RepoRole.COLLABORATOR
                        roleSource = "Default Organization Member Access"
                    }
                    OrgRole.BILLING_MANAGER -> {
                        highestRole = RepoRole.VIEWER
                        roleSource = "Organization Billing Manager (Read-Only)"
                    }
                }
            }
        }

        // 3. Team-level inheritance
        val userTeamIds = teamMemberships.filter { it.userId == actor.id }.map { it.teamId }.toSet()
        val teamRules = accessRules.filter { it.repoId == repo.id && it.granteeType == GranteeType.TEAM && it.granteeId in userTeamIds }
        
        for (tRule in teamRules) {
            if (tRule.role.rank > highestRole.rank) {
                highestRole = tRule.role
                val teamName = teams.firstOrNull { it.id == tRule.granteeId }?.name ?: tRule.granteeName
                roleSource = "Inherited from Team Grant: '$teamName' as ${tRule.role.name}"
            }
        }

        // 4. Direct User-level Access Rule on Repo
        val userRule = accessRules.firstOrNull { it.repoId == repo.id && it.granteeType == GranteeType.USER && it.granteeId == actor.id }
        if (userRule != null && userRule.role.rank >= highestRole.rank) {
            highestRole = userRule.role
            roleSource = "Direct Repository Role Assignment (${userRule.role.name})"
        }

        // 5. Enterprise Admin override
        if (actor.isEnterpriseAdmin && highestRole.rank < RepoRole.MAINTAINER.rank) {
            highestRole = RepoRole.MAINTAINER
            roleSource = "Enterprise Administrator Oversight"
        }

        return Pair(highestRole, roleSource)
    }

    /**
     * Evaluates a requested governance action against Enterprise guardrails,
     * Organization policies, Repository configurations, and Artifact states.
     */
    fun evaluateAction(
        enterprise: Enterprise,
        actor: User,
        repo: Repository,
        artifact: NoCodeArtifact?,
        reviews: List<ArtifactReview> = emptyList(),
        approvals: List<ArtifactApproval> = emptyList(),
        orgMemberships: List<OrgMembership> = emptyList(),
        teamMemberships: List<TeamMembership> = emptyList(),
        teams: List<Team> = emptyList(),
        accessRules: List<RepoAccessRule> = emptyList(),
        action: GovernanceAction
    ): PolicyEvaluationDetail {
        val (effectiveRole, roleSource) = resolveEffectiveRole(
            actor = actor,
            repo = repo,
            orgMemberships = orgMemberships,
            teamMemberships = teamMemberships,
            teams = teams,
            accessRules = accessRules
        )

        val enterpriseChecks = mutableListOf<PolicyCheckItem>()
        val repositoryChecks = mutableListOf<PolicyCheckItem>()

        // --- ENTERPRISE LEVEL CHECKS ---
        // 1. User-owned repository restriction
        if (repo.ownerType == OwnerType.USER) {
            val userRepoAllowed = enterprise.allowUserOwnedRepos
            enterpriseChecks.add(
                PolicyCheckItem(
                    title = "Enterprise User-Owned Repository Policy",
                    passed = userRepoAllowed,
                    detail = if (userRepoAllowed) "Enterprise allows individual User-owned repositories" 
                             else "Enterprise policy strictly restricts repositories to Organization ownership"
                )
            )
            if (!userRepoAllowed && action != GovernanceAction.VIEW_ARTIFACT) {
                return PolicyEvaluationDetail(
                    actor = actor,
                    targetRepo = repo,
                    targetArtifact = artifact,
                    action = action,
                    verdict = PolicyVerdict.DENIED_ENTERPRISE_RESTRICTION,
                    effectiveRole = effectiveRole,
                    roleSource = roleSource,
                    enterpriseChecks = enterpriseChecks,
                    repositoryChecks = repositoryChecks,
                    finalExplanation = "Enterprise governance policy prohibits mutations on User-owned repositories."
                )
            }
        } else {
            enterpriseChecks.add(
                PolicyCheckItem(
                    title = "Enterprise Valid Owner Verification",
                    passed = true,
                    detail = "Repository is properly owned by an Organization (${repo.ownerDisplayName})"
                )
            )
        }

        // 2. Segregation of Duties (Author cannot approve or review own RFC / Artifact)
        val enforceSegregation = enterprise.enforceSegregationOfDuties || repo.preventSelfApproval
        if (artifact != null && artifact.authorUserId == actor.id) {
            val isSignOffAction = action in listOf(
                GovernanceAction.SUBMIT_FINAL_APPROVAL,
                GovernanceAction.SUBMIT_REVIEW,
                GovernanceAction.PUBLISH_AND_LOCK
            )
            val passesSegregation = !enforceSegregation || !isSignOffAction
            enterpriseChecks.add(
                PolicyCheckItem(
                    title = "Segregation of Duties Policy (Anti-Self-Approval)",
                    passed = passesSegregation,
                    detail = if (passesSegregation) "Actor is not self-approving their own author draft" 
                             else "Actor '${actor.displayName}' is the original author of '${artifact.title}'. Self-approval or self-review is strictly barred by governance policy."
                )
            )
            if (!passesSegregation) {
                return PolicyEvaluationDetail(
                    actor = actor,
                    targetRepo = repo,
                    targetArtifact = artifact,
                    action = action,
                    verdict = PolicyVerdict.DENIED_SELF_APPROVAL_PROHIBITED,
                    effectiveRole = effectiveRole,
                    roleSource = roleSource,
                    enterpriseChecks = enterpriseChecks,
                    repositoryChecks = repositoryChecks,
                    finalExplanation = "Policy Violation: Segregation of Duties forbids author '${actor.displayName}' from reviewing or approving their own artifact."
                )
            }
        }

        // --- REPOSITORY & ROLE CHECKS ---
        // 3. Role Authority Check
        val roleHasAuthority = effectiveRole.canPerform(action.minimumRole)
        repositoryChecks.add(
            PolicyCheckItem(
                title = "Hierarchical Role Authority Check",
                passed = roleHasAuthority,
                detail = "Effective Role '${effectiveRole.name}' (Rank ${effectiveRole.rank}) vs Required Role '${action.minimumRole.name}' (Rank ${action.minimumRole.rank}) for action '${action.label}'"
            )
        )
        if (!roleHasAuthority) {
            return PolicyEvaluationDetail(
                actor = actor,
                targetRepo = repo,
                targetArtifact = artifact,
                action = action,
                verdict = PolicyVerdict.DENIED_INSUFFICIENT_ROLE,
                effectiveRole = effectiveRole,
                roleSource = roleSource,
                enterpriseChecks = enterpriseChecks,
                repositoryChecks = repositoryChecks,
                finalExplanation = "Access Denied: Effective role '${effectiveRole.name}' via $roleSource lacks the '${action.minimumRole.name}' permission required for '${action.label}'."
            )
        }

        // 4. Artifact Reviewer Pipeline Gate Check
        if (artifact != null && (action == GovernanceAction.SUBMIT_FINAL_APPROVAL || action == GovernanceAction.PUBLISH_AND_LOCK)) {
            val requireReviewGate = enterprise.enforceReviewerBeforeApprover || repo.requireReviewerPass
            val hasPassedReview = artifact.lifecycleState in listOf(LifecycleState.PENDING_APPROVAL, LifecycleState.APPROVED) ||
                    reviews.any { it.decision == ReviewDecision.APPROVED }
            
            repositoryChecks.add(
                PolicyCheckItem(
                    title = "Mandatory Reviewer Peer Gate",
                    passed = !requireReviewGate || hasPassedReview,
                    detail = if (hasPassedReview) "Artifact has satisfied peer review quality gate"
                             else "Artifact must complete Reviewer sign-off before Approver can sign off."
                )
            )
            if (requireReviewGate && !hasPassedReview) {
                return PolicyEvaluationDetail(
                    actor = actor,
                    targetRepo = repo,
                    targetArtifact = artifact,
                    action = action,
                    verdict = PolicyVerdict.DENIED_REVIEW_GATE_REQUIRED,
                    effectiveRole = effectiveRole,
                    roleSource = roleSource,
                    enterpriseChecks = enterpriseChecks,
                    repositoryChecks = repositoryChecks,
                    finalExplanation = "Governance Gate: Peer Reviewer must complete review before Approver sign-off is permitted."
                )
            }
        }

        // 5. Dual Approval Requirement for Publish & Lock
        if (artifact != null && action == GovernanceAction.PUBLISH_AND_LOCK) {
            val requiredApprovers = if (enterprise.enforceDualApproval) maxOf(repo.requiredApproverCount, 2) else repo.requiredApproverCount
            val validApprovalsCount = approvals.filter { it.status == ApprovalStatus.APPROVED }.distinctBy { it.approverUserId }.size
            val hasEnoughApprovals = validApprovalsCount >= requiredApprovers

            repositoryChecks.add(
                PolicyCheckItem(
                    title = "Multi-Signature Approver Quorum ($validApprovalsCount of $requiredApprovers required)",
                    passed = hasEnoughApprovals,
                    detail = if (hasEnoughApprovals) "Required $requiredApprovers distinct approver signatures verified"
                             else "Artifact requires $requiredApprovers distinct Approver signatures, but currently has $validApprovalsCount."
                )
            )
            if (!hasEnoughApprovals) {
                return PolicyEvaluationDetail(
                    actor = actor,
                    targetRepo = repo,
                    targetArtifact = artifact,
                    action = action,
                    verdict = PolicyVerdict.DENIED_DUAL_APPROVAL_DEFICIT,
                    effectiveRole = effectiveRole,
                    roleSource = roleSource,
                    enterpriseChecks = enterpriseChecks,
                    repositoryChecks = repositoryChecks,
                    finalExplanation = "Quorum Deficit: Publish & Lock requires $requiredApprovers distinct Approver sign-offs. Currently signed: $validApprovalsCount."
                )
            }
        }

        // All checks passed
        return PolicyEvaluationDetail(
            actor = actor,
            targetRepo = repo,
            targetArtifact = artifact,
            action = action,
            verdict = PolicyVerdict.ALLOWED,
            effectiveRole = effectiveRole,
            roleSource = roleSource,
            enterpriseChecks = enterpriseChecks,
            repositoryChecks = repositoryChecks,
            finalExplanation = "Access Granted: Policy evaluation succeeded. Actor has '${effectiveRole.name}' authority and satisfied all hierarchical enterprise & repository compliance policies."
        )
    }
}
