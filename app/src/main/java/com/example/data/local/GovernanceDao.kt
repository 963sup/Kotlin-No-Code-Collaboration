package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.EnterpriseWithHierarchy
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMemberWithUser
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OrganizationWithDetails
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMemberWithUser
import com.example.data.model.TeamMembership
import com.example.data.model.TeamWithDetails
import com.example.data.model.User
import com.example.data.model.UserWithGovernanceProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GovernanceDao {

    // --- ENTERPRISE ---
    @Query("SELECT * FROM enterprises ORDER BY createdAt ASC")
    fun getAllEnterprises(): Flow<List<Enterprise>>

    @Query("SELECT * FROM enterprises ORDER BY createdAt ASC")
    suspend fun getAllEnterprisesOnce(): List<Enterprise>

    @Query("SELECT * FROM enterprises WHERE id = :id LIMIT 1")
    fun getEnterpriseById(id: String): Flow<Enterprise?>

    @Query("SELECT * FROM enterprises WHERE id = :id LIMIT 1")
    suspend fun getEnterpriseByIdOnce(id: String): Enterprise?

    @Query("SELECT * FROM enterprises LIMIT 1")
    fun getEnterprise(): Flow<Enterprise?>

    @Query("SELECT * FROM enterprises LIMIT 1")
    suspend fun getEnterpriseOnce(): Enterprise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnterprise(enterprise: Enterprise)

    @Update
    suspend fun updateEnterprise(enterprise: Enterprise)

    @Delete
    suspend fun deleteEnterprise(enterprise: Enterprise)

    // --- ORGANIZATIONS ---
    @Query("SELECT * FROM organizations ORDER BY name ASC")
    fun getAllOrganizations(): Flow<List<Organization>>

    @Query("SELECT * FROM organizations WHERE enterpriseId = :enterpriseId ORDER BY name ASC")
    fun getOrganizationsByEnterprise(enterpriseId: String): Flow<List<Organization>>

    @Query("SELECT * FROM organizations ORDER BY name ASC")
    suspend fun getAllOrganizationsOnce(): List<Organization>

    @Query("SELECT * FROM organizations WHERE enterpriseId = :enterpriseId ORDER BY name ASC")
    suspend fun getOrganizationsByEnterpriseOnce(enterpriseId: String): List<Organization>

    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    fun getOrganizationById(orgId: String): Flow<Organization?>

    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    suspend fun getOrganizationByIdOnce(orgId: String): Organization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: Organization)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizations(organizations: List<Organization>)

    @Update
    suspend fun updateOrganization(organization: Organization)

    @Delete
    suspend fun deleteOrganization(organization: Organization)

    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY displayName ASC")
    suspend fun getAllUsersOnce(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    // --- TEAMS ---
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<Team>>

    @Query("SELECT * FROM teams ORDER BY name ASC")
    suspend fun getAllTeamsOnce(): List<Team>

    @Query("SELECT * FROM teams WHERE orgId = :orgId ORDER BY name ASC")
    fun getTeamsByOrg(orgId: String): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE orgId = :orgId ORDER BY name ASC")
    suspend fun getTeamsByOrgOnce(orgId: String): List<Team>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<Team>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team)

    // --- MEMBERSHIPS ---
    @Query("SELECT * FROM team_memberships")
    fun getAllTeamMemberships(): Flow<List<TeamMembership>>

    @Query("SELECT * FROM team_memberships")
    suspend fun getAllTeamMembershipsOnce(): List<TeamMembership>

    @Query("SELECT * FROM team_memberships WHERE teamId = :teamId")
    fun getMembershipsByTeam(teamId: String): Flow<List<TeamMembership>>

    @Query("SELECT * FROM team_memberships WHERE userId = :userId")
    suspend fun getMembershipsByUserOnce(userId: String): List<TeamMembership>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMemberships(memberships: List<TeamMembership>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMembership(membership: TeamMembership)

    @Update
    suspend fun updateTeamMembership(membership: TeamMembership)

    @Query("DELETE FROM team_memberships WHERE id = :id")
    suspend fun deleteTeamMembershipById(id: String)

    @Query("DELETE FROM team_memberships WHERE teamId = :teamId AND userId = :userId")
    suspend fun deleteTeamMembership(teamId: String, userId: String)

    @Query("SELECT * FROM org_memberships")
    fun getAllOrgMemberships(): Flow<List<OrgMembership>>

    @Query("SELECT * FROM org_memberships")
    suspend fun getAllOrgMembershipsOnce(): List<OrgMembership>

    @Query("SELECT * FROM org_memberships WHERE orgId = :orgId")
    fun getMembershipsByOrg(orgId: String): Flow<List<OrgMembership>>

    @Query("SELECT * FROM org_memberships WHERE userId = :userId")
    suspend fun getOrgMembershipsByUserOnce(userId: String): List<OrgMembership>

    @Query("SELECT * FROM org_memberships WHERE orgId = :orgId AND userId = :userId LIMIT 1")
    suspend fun getOrgMembership(orgId: String, userId: String): OrgMembership?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrgMemberships(memberships: List<OrgMembership>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrgMembership(membership: OrgMembership)

    @Update
    suspend fun updateOrgMembership(membership: OrgMembership)

    @Query("DELETE FROM org_memberships WHERE id = :id")
    suspend fun deleteOrgMembershipById(id: String)

    @Query("DELETE FROM org_memberships WHERE orgId = :orgId AND userId = :userId")
    suspend fun deleteOrgMembership(orgId: String, userId: String)

    // --- REPOSITORIES (No-Code Containers) ---
    @Query("SELECT * FROM repositories ORDER BY updatedAt DESC")
    fun getAllRepositories(): Flow<List<Repository>>

    @Query("SELECT * FROM repositories ORDER BY updatedAt DESC")
    suspend fun getAllRepositoriesOnce(): List<Repository>

    @Query("SELECT * FROM repositories WHERE id = :repoId LIMIT 1")
    fun getRepositoryById(repoId: String): Flow<Repository?>

    @Query("SELECT * FROM repositories WHERE id = :repoId LIMIT 1")
    suspend fun getRepositoryByIdOnce(repoId: String): Repository?

    @Query("SELECT * FROM repositories WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun getRepositoriesByOwner(ownerId: String): Flow<List<Repository>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<Repository>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: Repository)

    @Update
    suspend fun updateRepository(repository: Repository)

    @Delete
    suspend fun deleteRepository(repository: Repository)

    // --- REPO ACCESS RULES ---
    @Query("SELECT * FROM repo_access_rules WHERE repoId = :repoId")
    fun getAccessRulesByRepo(repoId: String): Flow<List<RepoAccessRule>>

    @Query("SELECT * FROM repo_access_rules WHERE repoId = :repoId")
    suspend fun getAccessRulesByRepoOnce(repoId: String): List<RepoAccessRule>

    @Query("SELECT * FROM repo_access_rules")
    fun getAllRepoAccessRules(): Flow<List<RepoAccessRule>>

    @Query("SELECT * FROM repo_access_rules")
    suspend fun getAllRepoAccessRulesOnce(): List<RepoAccessRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepoAccessRules(rules: List<RepoAccessRule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepoAccessRule(rule: RepoAccessRule)

    @Delete
    suspend fun deleteRepoAccessRule(rule: RepoAccessRule)

    // --- NO-CODE ARTIFACTS ---
    @Query("SELECT * FROM no_code_artifacts WHERE repoId = :repoId ORDER BY updatedAt DESC")
    fun getArtifactsByRepo(repoId: String): Flow<List<NoCodeArtifact>>

    @Query("SELECT * FROM no_code_artifacts WHERE repoId = :repoId ORDER BY updatedAt DESC")
    suspend fun getArtifactsByRepoOnce(repoId: String): List<NoCodeArtifact>

    @Query("SELECT * FROM no_code_artifacts WHERE id = :artifactId LIMIT 1")
    fun getArtifactById(artifactId: String): Flow<NoCodeArtifact?>

    @Query("SELECT * FROM no_code_artifacts WHERE id = :artifactId LIMIT 1")
    suspend fun getArtifactByIdOnce(artifactId: String): NoCodeArtifact?

    @Query("SELECT * FROM no_code_artifacts ORDER BY updatedAt DESC")
    fun getAllArtifacts(): Flow<List<NoCodeArtifact>>

    @Query("SELECT * FROM no_code_artifacts WHERE authorUserId = :authorUserId ORDER BY updatedAt DESC")
    fun getArtifactsByAuthor(authorUserId: String): Flow<List<NoCodeArtifact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifacts(artifacts: List<NoCodeArtifact>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifact(artifact: NoCodeArtifact)

    @Update
    suspend fun updateArtifact(artifact: NoCodeArtifact)

    @Delete
    suspend fun deleteArtifact(artifact: NoCodeArtifact)

    // --- REVIEWS & APPROVALS ---
    @Query("SELECT * FROM artifact_reviews ORDER BY reviewedAt DESC")
    fun getAllReviews(): Flow<List<ArtifactReview>>

    @Query("SELECT * FROM artifact_reviews WHERE artifactId = :artifactId ORDER BY reviewedAt DESC")
    fun getReviewsByArtifact(artifactId: String): Flow<List<ArtifactReview>>

    @Query("SELECT * FROM artifact_reviews WHERE reviewerUserId = :reviewerUserId ORDER BY reviewedAt DESC")
    fun getReviewsByReviewer(reviewerUserId: String): Flow<List<ArtifactReview>>

    @Query("SELECT * FROM artifact_reviews WHERE artifactId = :artifactId ORDER BY reviewedAt DESC")
    suspend fun getReviewsByArtifactOnce(artifactId: String): List<ArtifactReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ArtifactReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ArtifactReview>)

    @Query("SELECT * FROM artifact_approvals ORDER BY signedAt DESC")
    fun getAllApprovals(): Flow<List<ArtifactApproval>>

    @Query("SELECT * FROM artifact_approvals WHERE artifactId = :artifactId ORDER BY signedAt DESC")
    fun getApprovalsByArtifact(artifactId: String): Flow<List<ArtifactApproval>>

    @Query("SELECT * FROM artifact_approvals WHERE approverUserId = :approverUserId ORDER BY signedAt DESC")
    fun getApprovalsByApprover(approverUserId: String): Flow<List<ArtifactApproval>>

    @Query("SELECT * FROM artifact_approvals WHERE artifactId = :artifactId ORDER BY signedAt DESC")
    suspend fun getApprovalsByArtifactOnce(artifactId: String): List<ArtifactApproval>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ArtifactApproval)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovals(approvals: List<ArtifactApproval>)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE actorUserId = :actorUserId ORDER BY timestamp DESC")
    fun getAuditLogsByActor(actorUserId: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE repoId = :repoId ORDER BY timestamp DESC")
    fun getAuditLogsByRepo(repoId: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLog>)

    // --- REPO ISSUES ---
    @Query("SELECT * FROM repo_issues ORDER BY updatedAt DESC")
    fun getAllIssues(): Flow<List<RepoIssue>>

    @Query("SELECT * FROM repo_issues WHERE repoId = :repoId ORDER BY updatedAt DESC")
    fun getIssuesByRepo(repoId: String): Flow<List<RepoIssue>>

    @Query("SELECT * FROM repo_issues WHERE authorUserId = :authorUserId ORDER BY updatedAt DESC")
    fun getIssuesByAuthor(authorUserId: String): Flow<List<RepoIssue>>

    @Query("SELECT * FROM repo_issues WHERE assigneeId = :assigneeId ORDER BY updatedAt DESC")
    fun getIssuesByAssignee(assigneeId: String): Flow<List<RepoIssue>>

    @Query("SELECT * FROM repo_issues WHERE repoId = :repoId ORDER BY updatedAt DESC")
    suspend fun getIssuesByRepoOnce(repoId: String): List<RepoIssue>

    @Query("SELECT * FROM repo_issues WHERE id = :issueId LIMIT 1")
    fun getIssueById(issueId: String): Flow<RepoIssue?>

    @Query("SELECT * FROM repo_issues WHERE id = :issueId LIMIT 1")
    suspend fun getIssueByIdOnce(issueId: String): RepoIssue?

    @Query("SELECT COUNT(*) FROM repo_issues WHERE repoId = :repoId")
    suspend fun getIssueCountByRepo(repoId: String): Int

    @Query("SELECT * FROM repo_issues WHERE parentIssueId = :parentIssueId ORDER BY issueNumber ASC")
    fun getSubIssues(parentIssueId: String): Flow<List<RepoIssue>>

    @Query("SELECT * FROM repo_issues WHERE parentIssueId = :parentIssueId ORDER BY issueNumber ASC")
    suspend fun getSubIssuesOnce(parentIssueId: String): List<RepoIssue>

    @Query("SELECT * FROM repo_issues WHERE repoId = :repoId AND parentIssueId IS NULL ORDER BY updatedAt DESC")
    fun getRootIssuesByRepo(repoId: String): Flow<List<RepoIssue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: RepoIssue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<RepoIssue>)

    @Update
    suspend fun updateIssue(issue: RepoIssue)

    @Delete
    suspend fun deleteIssue(issue: RepoIssue)

    // --- ISSUE DEPENDENCIES (BLOCKED-BY / BLOCKING) ---
    @Query("SELECT * FROM issue_dependencies ORDER BY createdAt DESC")
    fun getAllDependencies(): Flow<List<IssueDependency>>

    @Query("SELECT * FROM issue_dependencies WHERE repoId = :repoId ORDER BY createdAt DESC")
    fun getDependenciesByRepo(repoId: String): Flow<List<IssueDependency>>

    @Query("SELECT * FROM issue_dependencies WHERE repoId = :repoId ORDER BY createdAt DESC")
    suspend fun getDependenciesByRepoOnce(repoId: String): List<IssueDependency>

    @Query("SELECT * FROM issue_dependencies WHERE blockedIssueId = :issueId")
    fun getBlockedByForIssue(issueId: String): Flow<List<IssueDependency>>

    @Query("SELECT * FROM issue_dependencies WHERE blockedIssueId = :issueId")
    suspend fun getBlockedByForIssueOnce(issueId: String): List<IssueDependency>

    @Query("SELECT * FROM issue_dependencies WHERE blockingIssueId = :issueId")
    fun getBlockingForIssue(issueId: String): Flow<List<IssueDependency>>

    @Query("SELECT * FROM issue_dependencies WHERE blockingIssueId = :issueId")
    suspend fun getBlockingForIssueOnce(issueId: String): List<IssueDependency>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueDependency(dependency: IssueDependency)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueDependencies(dependencies: List<IssueDependency>)

    @Query("DELETE FROM issue_dependencies WHERE id = :dependencyId")
    suspend fun deleteIssueDependencyById(dependencyId: String)

    @Query(
        "DELETE FROM issue_dependencies WHERE blockedIssueId = :blockedIssueId AND blockingIssueId = :blockingIssueId",
    )
    suspend fun deleteIssueDependencyBetween(blockedIssueId: String, blockingIssueId: String)

    @Query("DELETE FROM issue_dependencies WHERE blockedIssueId = :issueId OR blockingIssueId = :issueId")
    suspend fun deleteAllDependenciesForIssue(issueId: String)

    // --- ISSUE COMMENTS ---
    @Query("SELECT * FROM issue_comments WHERE issueId = :issueId ORDER BY createdAt ASC")
    fun getCommentsByIssue(issueId: String): Flow<List<IssueComment>>

    @Query("SELECT * FROM issue_comments WHERE authorUserId = :authorUserId ORDER BY createdAt ASC")
    fun getCommentsByAuthor(authorUserId: String): Flow<List<IssueComment>>

    @Query("SELECT * FROM issue_comments WHERE issueId = :issueId ORDER BY createdAt ASC")
    suspend fun getCommentsByIssueOnce(issueId: String): List<IssueComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueComment(comment: IssueComment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueComments(comments: List<IssueComment>)

    // --- REPO DISCUSSIONS ---
    @Query("SELECT * FROM repo_discussions ORDER BY updatedAt DESC")
    fun getAllDiscussions(): Flow<List<RepoDiscussion>>

    @Query("SELECT * FROM repo_discussions WHERE repoId = :repoId ORDER BY updatedAt DESC")
    fun getDiscussionsByRepo(repoId: String): Flow<List<RepoDiscussion>>

    @Query("SELECT * FROM repo_discussions WHERE authorUserId = :authorUserId ORDER BY updatedAt DESC")
    fun getDiscussionsByAuthor(authorUserId: String): Flow<List<RepoDiscussion>>

    @Query("SELECT * FROM repo_discussions WHERE repoId = :repoId ORDER BY updatedAt DESC")
    suspend fun getDiscussionsByRepoOnce(repoId: String): List<RepoDiscussion>

    @Query("SELECT * FROM repo_discussions WHERE id = :discussionId LIMIT 1")
    fun getDiscussionById(discussionId: String): Flow<RepoDiscussion?>

    @Query("SELECT * FROM repo_discussions WHERE id = :discussionId LIMIT 1")
    suspend fun getDiscussionByIdOnce(discussionId: String): RepoDiscussion?

    @Query("SELECT COUNT(*) FROM repo_discussions WHERE repoId = :repoId")
    suspend fun getDiscussionCountByRepo(repoId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussion(discussion: RepoDiscussion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussions(discussions: List<RepoDiscussion>)

    @Update
    suspend fun updateDiscussion(discussion: RepoDiscussion)

    @Delete
    suspend fun deleteDiscussion(discussion: RepoDiscussion)

    // --- DISCUSSION COMMENTS ---
    @Query("SELECT * FROM discussion_comments WHERE discussionId = :discussionId ORDER BY createdAt ASC")
    fun getCommentsByDiscussion(discussionId: String): Flow<List<DiscussionComment>>

    @Query("SELECT * FROM discussion_comments WHERE discussionId = :discussionId ORDER BY createdAt ASC")
    suspend fun getCommentsByDiscussionOnce(discussionId: String): List<DiscussionComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussionComment(comment: DiscussionComment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussionComments(comments: List<DiscussionComment>)

    @Update
    suspend fun updateDiscussionComment(comment: DiscussionComment)

    // --- HIERARCHY RELATIONAL QUERIES (@Transaction) ---

    @Transaction
    @Query("SELECT * FROM enterprises WHERE id = :id LIMIT 1")
    fun getEnterpriseWithHierarchy(id: String): Flow<EnterpriseWithHierarchy?>

    @Transaction
    @Query("SELECT * FROM enterprises WHERE id = :id LIMIT 1")
    suspend fun getEnterpriseWithHierarchyOnce(id: String): EnterpriseWithHierarchy?

    @Transaction
    @Query("SELECT * FROM enterprises ORDER BY createdAt ASC")
    fun getAllEnterprisesWithHierarchy(): Flow<List<EnterpriseWithHierarchy>>

    @Transaction
    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    fun getOrganizationWithDetails(orgId: String): Flow<OrganizationWithDetails?>

    @Transaction
    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    suspend fun getOrganizationWithDetailsOnce(orgId: String): OrganizationWithDetails?

    @Transaction
    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    fun getTeamWithDetails(teamId: String): Flow<TeamWithDetails?>

    @Transaction
    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    suspend fun getTeamWithDetailsOnce(teamId: String): TeamWithDetails?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserWithGovernanceProfile(userId: String): Flow<UserWithGovernanceProfile?>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserWithGovernanceProfileOnce(userId: String): UserWithGovernanceProfile?

    @Transaction
    @Query("SELECT * FROM org_memberships WHERE orgId = :orgId")
    fun getOrgMembersWithUsers(orgId: String): Flow<List<OrgMemberWithUser>>

    @Transaction
    @Query("SELECT * FROM org_memberships WHERE orgId = :orgId")
    suspend fun getOrgMembersWithUsersOnce(orgId: String): List<OrgMemberWithUser>

    @Transaction
    @Query("SELECT * FROM team_memberships WHERE teamId = :teamId")
    fun getTeamMembersWithUsers(teamId: String): Flow<List<TeamMemberWithUser>>

    @Transaction
    @Query("SELECT * FROM team_memberships WHERE teamId = :teamId")
    suspend fun getTeamMembersWithUsersOnce(teamId: String): List<TeamMemberWithUser>

    // --- NOTIFICATIONS & INBOX ---

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT * FROM notifications WHERE recipientUserId = :recipientUserId ORDER BY createdAt DESC")
    fun getNotificationsForUser(recipientUserId: String): Flow<List<AppNotification>>

    @Query(
        "SELECT * FROM notifications WHERE recipientUserId = :recipientUserId AND status = 'UNREAD' ORDER BY createdAt DESC",
    )
    fun getUnreadNotificationsForUser(recipientUserId: String): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientUserId = :recipientUserId AND status = 'UNREAD'")
    fun getUnreadCountForUser(recipientUserId: String): Flow<Int>

    @Query("SELECT * FROM notifications WHERE recipientUserId = :recipientUserId ORDER BY createdAt DESC")
    suspend fun getNotificationsForUserOnce(recipientUserId: String): List<AppNotification>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationByIdOnce(id: String): AppNotification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotification>)

    @Update
    suspend fun updateNotification(notification: AppNotification)

    @Query("UPDATE notifications SET status = 'READ', readAt = :readAt WHERE id = :id")
    suspend fun markNotificationAsRead(id: String, readAt: Long = System.currentTimeMillis())

    @Query(
        "UPDATE notifications SET status = 'READ', readAt = :readAt WHERE recipientUserId = :recipientUserId AND status = 'UNREAD'",
    )
    suspend fun markAllNotificationsAsReadForUser(recipientUserId: String, readAt: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET status = 'ARCHIVED' WHERE id = :id")
    suspend fun archiveNotification(id: String)

    @Query("UPDATE notifications SET isActionable = 0, actionCompletedAt = :timestamp WHERE id = :id")
    suspend fun markActionCompleted(id: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteNotification(notification: AppNotification)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)

    // --- WORK EVIDENCE ---
    @Query("SELECT * FROM work_evidence WHERE issueId = :issueId ORDER BY submittedAt DESC")
    fun getWorkEvidenceForIssue(issueId: String): Flow<List<com.example.data.model.WorkEvidence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkEvidence(evidence: com.example.data.model.WorkEvidence)

    // --- WORK VERIFICATION ---
    @Query("SELECT * FROM work_verifications WHERE evidenceId = :evidenceId ORDER BY verifiedAt DESC")
    fun getVerificationsForEvidence(evidenceId: String): Flow<List<com.example.data.model.WorkVerification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkVerification(verification: com.example.data.model.WorkVerification)

    // --- TASK CHECKLIST ---
    @Query("SELECT * FROM task_checklists WHERE issueId = :issueId ORDER BY createdAt ASC")
    fun getChecklistForIssue(issueId: String): Flow<List<com.example.data.model.TaskChecklist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskChecklist(item: com.example.data.model.TaskChecklist)

    @Query(
        "UPDATE task_checklists SET isCompleted = :isCompleted, completedByUserId = :userId, completedByDisplayName = :displayName, completedAt = :completedAt WHERE id = :id",
    )
    suspend fun updateTaskChecklistStatus(
        id: String,
        isCompleted: Boolean,
        userId: String?,
        displayName: String?,
        completedAt: Long?,
    )
}
