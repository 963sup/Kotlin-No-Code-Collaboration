package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

/**
 * Enums representing the strict hierarchical access control model
 * reverse-engineering GitHub's enterprise governance for No-Code Collaboration Containers.
 */

enum class OwnerType {
    ORGANIZATION,
    USER;

    fun displayName(): String = when (this) {
        ORGANIZATION -> "Organization"
        USER -> "User"
    }
}

enum class RepoRole(val rank: Int, val description: String) {
    VIEWER(1, "Read-only access to published artifacts and documents"),
    COLLABORATOR(2, "Can create drafts, build no-code workflows, and submit proposals for review"),
    REVIEWER(3, "Authorized to review proposals, submit change requests, and validate quality"),
    APPROVER(4, "Sign-off authority for releases, workflow promotions, and artifact approvals"),
    MAINTAINER(5, "Full access to repository settings, access mappings, and policy enforcement"),
    OWNER(6, "Ultimate authority over repository lifecycle, policy overrides, and ownership transfer");

    fun canPerform(requiredRole: RepoRole): Boolean = this.rank >= requiredRole.rank
}

enum class OrgRole(val rank: Int) {
    MEMBER(1),
    BILLING_MANAGER(2),
    ADMIN(3),
    OWNER(4);

    fun toDefaultRepoRole(): RepoRole = when (this) {
        OWNER -> RepoRole.MAINTAINER
        ADMIN -> RepoRole.MAINTAINER
        BILLING_MANAGER -> RepoRole.VIEWER
        MEMBER -> RepoRole.COLLABORATOR
    }
}

enum class TeamRole {
    MEMBER,
    MAINTAINER
}

enum class GranteeType {
    USER,
    TEAM
}

enum class ArtifactType(val label: String, val iconName: String) {
    SPECIFICATION_DOC("Product Specification", "Description"),
    PROCESS_WORKFLOW("No-Code Workflow", "AccountTree"),
    DECISION_RECORD("Architecture Decision Record (RFC)", "Gavel"),
    FORM_SCHEMA("Form & Data Schema", "DynamicForm"),
    CANVAS_BOARD("Visual Process Canvas", "DashboardCustomize"),
    MILESTONE_RELEASE("Milestone Release Gate", "Flag")
}

enum class LifecycleState(val label: String) {
    DRAFT("Draft"),
    IN_REVIEW("In Review"),
    PENDING_APPROVAL("Pending Sign-Off"),
    APPROVED("Approved"),
    PUBLISHED("Published & Locked"),
    ARCHIVED("Archived")
}

enum class ReviewDecision {
    APPROVED,
    CHANGES_REQUESTED,
    COMMENTED
}

enum class ApprovalStatus {
    APPROVED,
    REJECTED
}

enum class IssueStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    CLOSED("Closed")
}

enum class IssuePriority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class DiscussionCategory(val label: String, val iconName: String, val description: String) {
    GENERAL("General", "Forum", "General community & repository discussions"),
    RFC_PROPOSALS("RFC Proposals", "Gavel", "Formal blueprints, architecture, and schema proposals"),
    ANNOUNCEMENTS("Announcements", "Campaign", "Official updates from repository maintainers and owners"),
    IDEAS_AND_BRAINSTORM("Ideas & Brainstorm", "Lightbulb", "Collaborative brainstorms for no-code workflows"),
    Q_AND_A("Q & A", "Help", "Ask questions and get verified answers"),
    GOVERNANCE_DEBATE("Governance & Policy", "Policy", "Discussion on compliance gates, access roles, and audit rules")
}

enum class PolicyVerdict {
    ALLOWED,
    DENIED_INSUFFICIENT_ROLE,
    DENIED_SELF_APPROVAL_PROHIBITED,
    DENIED_REVIEW_GATE_REQUIRED,
    DENIED_DUAL_APPROVAL_DEFICIT,
    DENIED_ENTERPRISE_RESTRICTION,
    DENIED_UNAUTHORIZED_OWNER_ENTITY
}

enum class GovernanceAction(val label: String, val minimumRole: RepoRole) {
    VIEW_ARTIFACT("View Artifact", RepoRole.VIEWER),
    CREATE_DRAFT("Create No-Code Draft", RepoRole.COLLABORATOR),
    EDIT_DRAFT("Edit No-Code Draft", RepoRole.COLLABORATOR),
    SUBMIT_FOR_REVIEW("Submit for Peer Review", RepoRole.COLLABORATOR),
    SUBMIT_REVIEW("Submit Formal Review", RepoRole.REVIEWER),
    REQUEST_CHANGES("Request Artifact Changes", RepoRole.REVIEWER),
    SUBMIT_FINAL_APPROVAL("Grant Approver Sign-Off", RepoRole.APPROVER),
    PUBLISH_AND_LOCK("Publish & Lock Artifact", RepoRole.APPROVER),
    MANAGE_ACCESS_RULES("Manage Collaborators & Roles", RepoRole.MAINTAINER),
    UPDATE_REPO_POLICY("Update Repository Policies", RepoRole.MAINTAINER),
    TRANSFER_OWNERSHIP("Transfer Repository Ownership", RepoRole.OWNER),
    DELETE_REPOSITORY("Delete Repository", RepoRole.OWNER),
    // Collaboration Features: Issues & Discussions
    CREATE_ISSUE("Create Repository Issue", RepoRole.COLLABORATOR),
    COMMENT_ISSUE("Comment on Issue", RepoRole.VIEWER),
    ASSIGN_ISSUE("Assign Issue (User/Team)", RepoRole.COLLABORATOR),
    CLOSE_ISSUE("Close/Reopen Issue", RepoRole.COLLABORATOR),
    DELETE_ISSUE("Delete Issue", RepoRole.MAINTAINER),
    CREATE_DISCUSSION("Create Discussion Thread", RepoRole.COLLABORATOR),
    COMMENT_DISCUSSION("Reply to Discussion", RepoRole.VIEWER),
    LOCK_DISCUSSION("Lock/Unlock Discussion", RepoRole.MAINTAINER),
    ACCEPT_DISCUSSION_ANSWER("Mark Accepted Answer", RepoRole.COLLABORATOR)
}

// -------------------------------------------------------------
// NOTIFICATIONS & INBOX DOMAIN MODELS
// -------------------------------------------------------------

enum class NotificationCategory(val label: String, val description: String) {
    REVIEW_REQUEST("Review Requests", "Peers requesting your formal review on artifacts or RFCs"),
    APPROVAL_GATE("Approvals & Sign-offs", "Governance gates awaiting your cryptographic sign-off"),
    ISSUE_ASSIGNMENT("Issue Assignments", "Repository issues assigned directly or to your team"),
    MENTION_AND_REPLY("Mentions & Replies", "Direct @mentions and replies to your threads"),
    ACCESS_CHANGE("Access & Permissions", "Direct repository collaborator grants and role updates"),
    MEMBERSHIP_CHANGE("Org & Team Memberships", "Organization invitations and team assignment changes"),
    PUBLICATION("Releases & Publications", "Artifact milestones published and locked"),
    GOVERNANCE_EVENT("Governance & Policy Alerts", "Enterprise policy checks, dual-approval alerts, and compliance gates")
}

enum class NotificationStatus {
    UNREAD,
    READ,
    ARCHIVED
}

enum class NotificationPriority(val label: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    URGENT("Urgent")
}

// -------------------------------------------------------------
// ROOM ENTITIES FOR ENTERPRISE GOVERNANCE HIERARCHY
// -------------------------------------------------------------

/**
 * Enterprise: Root boundary for security policies, SSO identity,
 * organization partitioning, and cross-organization compliance enforcement.
 */
@Entity(
    tableName = "enterprises",
    indices = [
        Index(value = ["slug"], unique = true)
    ]
)
data class Enterprise(
    @PrimaryKey val id: String = "ent_${UUID.randomUUID().toString().take(8)}",
    val name: String,
    val slug: String,
    val description: String,
    val enforceDualApproval: Boolean = true,
    val allowUserOwnedRepos: Boolean = true,
    val enforceReviewerBeforeApprover: Boolean = true,
    val enforceSegregationOfDuties: Boolean = true, // Author cannot approve or review own work
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Organization: First-class security context & resource container scoped to an Enterprise.
 * Strictly CAN own Repositories.
 */
@Entity(
    tableName = "organizations",
    indices = [
        Index(value = ["enterpriseId"]),
        Index(value = ["slug"], unique = true)
    ]
)
data class Organization(
    @PrimaryKey val id: String = "org_${UUID.randomUUID().toString().take(8)}",
    val enterpriseId: String,
    val name: String,
    val slug: String,
    val description: String,
    val badgeColorHex: String = "#3B82F6",
    val defaultMemberRole: RepoRole = RepoRole.COLLABORATOR,
    val canOwnerRepository: Boolean = true, // Strictly True: Organization CAN own Repositories
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User: Individual identity within the Enterprise directory.
 * Strictly CAN own personal workspace Repositories (if allowed by Enterprise policy).
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["enterpriseId"]),
        Index(value = ["username"], unique = true),
        Index(value = ["email"])
    ]
)
data class User(
    @PrimaryKey val id: String = "usr_${UUID.randomUUID().toString().take(8)}",
    val enterpriseId: String,
    val username: String,
    val displayName: String,
    val email: String,
    val title: String,
    val avatarColorHex: String = "#6366F1",
    val isEnterpriseAdmin: Boolean = false,
    val canOwnerRepository: Boolean = true, // Strictly True: User CAN own Repositories
    val bio: String = "Enterprise contributor & workflow author",
    val location: String = "San Francisco, CA / Remote",
    val pronouns: String = "they/them",
    val ssoProvider: String = "Enterprise SAML / OIDC",
    val authStatus: String = "Federated & Enforced",
    val securityKeyEnforced: Boolean = true,
    val twoFactorEnabled: Boolean = true,
    val notificationPreferences: String = "Policy alerts, review requests, release gates",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Team: Collaborative group within an Organization (supports nested parentTeamId hierarchy).
 * Strictly CANNOT own Repositories directly. Granted access via RepoAccessRule.
 */
@Entity(
    tableName = "teams",
    indices = [
        Index(value = ["orgId"]),
        Index(value = ["parentTeamId"]),
        Index(value = ["slug"])
    ]
)
data class Team(
    @PrimaryKey val id: String = "team_${UUID.randomUUID().toString().take(8)}",
    val orgId: String,
    val parentTeamId: String? = null,
    val name: String,
    val slug: String,
    val description: String,
    val canOwnerRepository: Boolean = false, // Strictly False: Teams CANNOT own Repositories
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * TeamMembership: Association of a User to a Team with granular TeamRole (MEMBER, MAINTAINER).
 */
@Entity(
    tableName = "team_memberships",
    indices = [
        Index(value = ["teamId", "userId"], unique = true),
        Index(value = ["teamId"]),
        Index(value = ["userId"])
    ]
)
data class TeamMembership(
    @PrimaryKey val id: String = "tm_${UUID.randomUUID().toString().take(8)}",
    val teamId: String,
    val userId: String,
    val role: TeamRole = TeamRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis()
)

/**
 * OrgMembership: Association of a User to an Organization with OrgRole (OWNER, ADMIN, BILLING_MANAGER, MEMBER).
 */
@Entity(
    tableName = "org_memberships",
    indices = [
        Index(value = ["orgId", "userId"], unique = true),
        Index(value = ["orgId"]),
        Index(value = ["userId"])
    ]
)
data class OrgMembership(
    @PrimaryKey val id: String = "om_${UUID.randomUUID().toString().take(8)}",
    val orgId: String,
    val userId: String,
    val role: OrgRole = OrgRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis()
)

/**
 * Repository: Strictly a No-Code Collaboration Container.
 * Owner MUST be either an Organization or a User.
 */
@Entity(tableName = "repositories")
data class Repository(
    @PrimaryKey val id: String = "repo_${UUID.randomUUID().toString().take(8)}",
    val name: String,
    val displayName: String,
    val ownerType: OwnerType, // ORGANIZATION or USER
    val ownerId: String,       // ID of the Org or User
    val ownerDisplayName: String,
    val enterpriseId: String,
    val description: String,
    val category: String = "No-Code Governance",
    val isArchived: Boolean = false,
    val requiredApproverCount: Int = 2,
    val requireReviewerPass: Boolean = true,
    val preventSelfApproval: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repo_access_rules")
data class RepoAccessRule(
    @PrimaryKey val id: String = "rar_${UUID.randomUUID().toString().take(8)}",
    val repoId: String,
    val granteeType: GranteeType, // USER or TEAM
    val granteeId: String,
    val granteeName: String,
    val role: RepoRole,
    val grantedByUserId: String,
    val grantedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "no_code_artifacts")
data class NoCodeArtifact(
    @PrimaryKey val id: String = "art_${UUID.randomUUID().toString().take(8)}",
    val repoId: String,
    val title: String,
    val type: ArtifactType,
    val summary: String,
    val structuredContent: String, // Rich no-code definition (steps, rules, schemas, fields)
    val lifecycleState: LifecycleState = LifecycleState.DRAFT,
    val authorUserId: String,
    val authorDisplayName: String,
    val version: String = "v1.0.0",
    val lockedByPolicy: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "artifact_reviews")
data class ArtifactReview(
    @PrimaryKey val id: String = "rev_${UUID.randomUUID().toString().take(8)}",
    val artifactId: String,
    val reviewerUserId: String,
    val reviewerDisplayName: String,
    val decision: ReviewDecision,
    val feedbackNote: String,
    val reviewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "artifact_approvals")
data class ArtifactApproval(
    @PrimaryKey val id: String = "appr_${UUID.randomUUID().toString().take(8)}",
    val artifactId: String,
    val approverUserId: String,
    val approverDisplayName: String,
    val approverTitle: String,
    val status: ApprovalStatus = ApprovalStatus.APPROVED,
    val signatureProof: String = "SIG_${UUID.randomUUID().toString().take(12).uppercase()}",
    val signedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = "audit_${UUID.randomUUID().toString().take(8)}",
    val enterpriseId: String,
    val orgId: String? = null,
    val repoId: String? = null,
    val repoName: String? = null,
    val actorUserId: String,
    val actorDisplayName: String,
    val actionName: String,
    val verdict: PolicyVerdict,
    val reasoning: String,
    val timestamp: Long = System.currentTimeMillis()
)

// -------------------------------------------------------------
// REPOSITORY COLLABORATION ENTITIES (ISSUES, SUB-ISSUES & DEPENDENCIES)
// -------------------------------------------------------------

enum class DependencyType(val label: String, val description: String) {
    BLOCKS("Blocks", "Must be resolved before the blocked issue can progress"),
    BLOCKED_BY("Blocked By", "Prerequisite issue that prevents this issue from closing"),
    RELATES_TO("Relates To", "Cross-referenced related task in same repository")
}

@Entity(
    tableName = "repo_issues",
    indices = [
        Index(value = ["repoId"]),
        Index(value = ["parentIssueId"]),
        Index(value = ["authorUserId"]),
        Index(value = ["assigneeId"])
    ]
)
data class RepoIssue(
    @PrimaryKey val id: String = "iss_${UUID.randomUUID().toString().take(8)}",
    val repoId: String,
    val issueNumber: Int,
    val title: String,
    val description: String,
    val status: IssueStatus = IssueStatus.OPEN,
    val priority: IssuePriority = IssuePriority.MEDIUM,
    val authorUserId: String,
    val authorDisplayName: String,
    val authorRole: String = "COLLABORATOR",
    val assigneeType: GranteeType? = null, // USER or TEAM delegation
    val assigneeId: String? = null,        // User ID or Team ID
    val assigneeName: String? = null,      // User Display Name or Team Name
    val linkedArtifactId: String? = null,  // Optional link to blueprint/schema
    val linkedArtifactTitle: String? = null,
    val parentIssueId: String? = null,     // Parent Issue (Strictly in the same Repo)
    val parentIssueNumber: Int? = null,
    val parentIssueTitle: String? = null,
    val labels: String = "governance",     // Comma-separated labels
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val closedByUserId: String? = null,
    val closedByDisplayName: String? = null
)

/**
 * IssueDependency: Explicit blocked-by / blocking relationship strictly scoped to a Repository.
 * An issue is blocked if any prerequisite `blockingIssueId` is OPEN or IN_PROGRESS.
 */
@Entity(
    tableName = "issue_dependencies",
    indices = [
        Index(value = ["repoId"]),
        Index(value = ["blockedIssueId"]),
        Index(value = ["blockingIssueId"]),
        Index(value = ["blockedIssueId", "blockingIssueId"], unique = true)
    ]
)
data class IssueDependency(
    @PrimaryKey val id: String = "dep_${UUID.randomUUID().toString().take(8)}",
    val repoId: String,                    // Strictly scoped to the Repository container!
    val blockedIssueId: String,            // Target issue waiting on prerequisite
    val blockingIssueId: String,           // Prerequisite issue that blocks the target
    val dependencyType: DependencyType = DependencyType.BLOCKS,
    val createdByUserId: String,
    val createdByDisplayName: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Aggregate model representing the complete hierarchical breakdown for an issue.
 */
data class IssueHierarchyDetails(
    val issue: RepoIssue,
    val parentIssue: RepoIssue? = null,
    val subIssues: List<RepoIssue> = emptyList(),
    val blockedBy: List<RepoIssue> = emptyList(),
    val blocking: List<RepoIssue> = emptyList(),
    val isBlocked: Boolean = false,
    val subIssuesTotalCount: Int = 0,
    val subIssuesClosedCount: Int = 0,
    val subIssuesProgress: Float = 0f
)

@Entity(tableName = "issue_comments")
data class IssueComment(
    @PrimaryKey val id: String = "ic_${UUID.randomUUID().toString().take(8)}",
    val issueId: String,
    val authorUserId: String,
    val authorDisplayName: String,
    val authorRole: String = "COLLABORATOR",
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repo_discussions")
data class RepoDiscussion(
    @PrimaryKey val id: String = "disc_${UUID.randomUUID().toString().take(8)}",
    val repoId: String,
    val discussionNumber: Int,
    val title: String,
    val category: DiscussionCategory = DiscussionCategory.GENERAL,
    val body: String,
    val authorUserId: String,
    val authorDisplayName: String,
    val authorRole: String = "COLLABORATOR",
    val isLocked: Boolean = false,
    val isAnswered: Boolean = false,
    val acceptedAnswerCommentId: String? = null,
    val upvoteCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "discussion_comments")
data class DiscussionComment(
    @PrimaryKey val id: String = "dc_${UUID.randomUUID().toString().take(8)}",
    val discussionId: String,
    val authorUserId: String,
    val authorDisplayName: String,
    val authorRole: String = "COLLABORATOR",
    val content: String,
    val isAcceptedAnswer: Boolean = false,
    val upvotes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * AppNotification: Personal, actionable notification item delivered to a User's Unified Inbox.
 * Explicitly distinguishes private, interactive notifications from the immutable public AuditLog,
 * while maintaining complete relational linkage across Enterprise, Org, Team, Repo, Artifact,
 * Review, Approval, Issue, Discussion, and Membership.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["recipientUserId"]),
        Index(value = ["status"]),
        Index(value = ["category"]),
        Index(value = ["createdAt"])
    ]
)
data class AppNotification(
    @PrimaryKey val id: String = "notif_${UUID.randomUUID().toString().take(8)}",
    val recipientUserId: String,
    val actorUserId: String,
    val actorDisplayName: String,
    val actorAvatarColorHex: String = "#6366F1",
    val category: NotificationCategory,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val status: NotificationStatus = NotificationStatus.UNREAD,
    val title: String,
    val body: String,
    val isActionable: Boolean = false,
    val actionType: String? = null, // e.g. "REVIEW", "APPROVE", "VIEW_ISSUE", "VIEW_DISCUSSION", "VIEW_REPO", "VIEW_ORG", "VIEW_TEAM", "VIEW_PROFILE"
    // Relational Cross-References
    val enterpriseId: String? = null,
    val orgId: String? = null,
    val orgName: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val repoId: String? = null,
    val repoName: String? = null,
    val artifactId: String? = null,
    val artifactTitle: String? = null,
    val issueId: String? = null,
    val issueTitle: String? = null,
    val discussionId: String? = null,
    val discussionTitle: String? = null,
    val reviewId: String? = null,
    val approvalId: String? = null,
    val membershipId: String? = null,
    val auditLogId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val actionCompletedAt: Long? = null
)

// -------------------------------------------------------------
// EVALUATION RESULT MODEL FOR SIMULATOR & ACCESS INSPECTOR
// -------------------------------------------------------------

data class PolicyEvaluationDetail(
    val actor: User,
    val targetRepo: Repository,
    val targetArtifact: NoCodeArtifact?,
    val action: GovernanceAction,
    val verdict: PolicyVerdict,
    val effectiveRole: RepoRole,
    val roleSource: String, // e.g. "Direct User Assignment", "Team Membership (Core Infra)", "Org Owner Inheritance", "Personal Repo Owner"
    val enterpriseChecks: List<PolicyCheckItem>,
    val repositoryChecks: List<PolicyCheckItem>,
    val finalExplanation: String
)

data class PolicyCheckItem(
    val title: String,
    val passed: Boolean,
    val detail: String
)

// -------------------------------------------------------------
// ROOM RELATIONAL HIERARCHY MODELS (@Relation)
// -------------------------------------------------------------

/**
 * Enterprise aggregate with all scoped child Organizations and Users.
 */
data class EnterpriseWithHierarchy(
    @Embedded val enterprise: Enterprise,
    @Relation(
        parentColumn = "id",
        entityColumn = "enterpriseId"
    )
    val organizations: List<Organization>,
    @Relation(
        parentColumn = "id",
        entityColumn = "enterpriseId"
    )
    val users: List<User>
)

/**
 * Organization aggregate with all child Teams and Organization Memberships.
 */
data class OrganizationWithDetails(
    @Embedded val organization: Organization,
    @Relation(
        parentColumn = "id",
        entityColumn = "orgId"
    )
    val teams: List<Team>,
    @Relation(
        parentColumn = "id",
        entityColumn = "orgId"
    )
    val memberships: List<OrgMembership>
)

/**
 * Team aggregate with its child Team Memberships.
 */
data class TeamWithDetails(
    @Embedded val team: Team,
    @Relation(
        parentColumn = "id",
        entityColumn = "teamId"
    )
    val memberships: List<TeamMembership>
)

/**
 * User profile aggregate with all Organization and Team memberships.
 */
data class UserWithGovernanceProfile(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val orgMemberships: List<OrgMembership>,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val teamMemberships: List<TeamMembership>
)

/**
 * Relational join model for an OrgMembership paired with its User entity.
 */
data class OrgMemberWithUser(
    @Embedded val membership: OrgMembership,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User
)

/**
 * Relational join model for a TeamMembership paired with its User entity.
 */
data class TeamMemberWithUser(
    @Embedded val membership: TeamMembership,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User
)

