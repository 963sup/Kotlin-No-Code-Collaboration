package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationPriority
import com.example.data.model.NotificationStatus
import com.example.data.model.User
import com.example.engine.HierarchicalPolicyEngine
import com.example.navigation.CollaborationTarget
import com.example.navigation.CollaborationTargetAccess
import com.example.navigation.CollaborationTargetResolver
import com.example.ui.components.DiscussionDetailDialog
import com.example.ui.components.IssueDetailDialog
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.viewmodel.GovernanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InboxFilterTab(val label: String) {
    ALL("全部"),
    UNREAD("未讀"),
    ACTIONABLE("需處理"),
    ARCHIVED("已封存")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxScreen(
    viewModel: GovernanceViewModel,
    onNavigateToRepository: (String) -> Unit = {},
    onNavigateToArtifact: (String, String) -> Unit = { _, _ -> },
    onNavigateToOrg: (String) -> Unit = {},
    onNavigateToUserProfile: (User) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val allAccessRules by viewModel.allAccessRules.collectAsState()
    val allArtifacts by viewModel.allArtifacts.collectAsState()
    val allDependencies by viewModel.allDependencies.collectAsState()
    val allDiscussions by viewModel.allDiscussions.collectAsState()
    val allIssues by viewModel.allIssues.collectAsState()
    val allOrgMemberships by viewModel.allOrgMemberships.collectAsState()
    val allTeamMemberships by viewModel.allTeamMemberships.collectAsState()
    val allTeams by viewModel.teams.collectAsState()
    val allUsers by viewModel.users.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val repositories by viewModel.repositories.collectAsState()
    val selectedCategoryFilter by viewModel.notificationFilterCategory.collectAsState()
    val selectedDiscussionComments by viewModel.selectedDiscussionComments.collectAsState()
    val selectedIssueComments by viewModel.selectedIssueComments.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    var selectedTab by remember { mutableStateOf(InboxFilterTab.ALL) }
    var inspectingNotification by remember { mutableStateOf<AppNotification?>(null) }
    var showArchitectureExplainer by remember { mutableStateOf(false) }
    var openedTarget by remember { mutableStateOf<CollaborationTarget?>(null) }
    var targetFailureMessage by remember { mutableStateOf<String?>(null) }

    // Filter notifications based on tab and category
    val filteredNotifications = remember(notifications, selectedTab, selectedCategoryFilter) {
        notifications.filter { notif ->
            val matchesTab = when (selectedTab) {
                InboxFilterTab.ALL -> notif.status != NotificationStatus.ARCHIVED
                InboxFilterTab.UNREAD -> notif.status == NotificationStatus.UNREAD
                InboxFilterTab.ACTIONABLE -> notif.isActionable && notif.status != NotificationStatus.ARCHIVED
                InboxFilterTab.ARCHIVED -> notif.status == NotificationStatus.ARCHIVED
            }
            val matchesCategory = selectedCategoryFilter == null || notif.category == selectedCategoryFilter
            matchesTab && matchesCategory
        }
    }

    val actionableCount = remember(notifications) {
        notifications.count { it.isActionable && it.status != NotificationStatus.ARCHIVED }
    }


    fun rejectTarget(message: String): Boolean {
        openedTarget = null
        targetFailureMessage = message
        return false
    }

    fun canOpenRepository(repo: Repository): Boolean {
        val actor = activeUser ?: return false
        return CollaborationTargetAccess.canOpenRepository(
  user = actor,
  repository = repo,
  orgMemberships = allOrgMemberships,
  teamMemberships = allTeamMemberships,
  accessRules = allAccessRules
        )
    }

    fun openNotificationTarget(notification: AppNotification): Boolean {
        return when (val target = CollaborationTargetResolver.resolve(notification)) {
  is CollaborationTarget.Artifact -> {
      val repo = repositories.firstOrNull { it.id == target.repositoryId }
          ?: return rejectTarget("通知指向的儲存庫已不存在。")
      if (!canOpenRepository(repo)) {
          return rejectTarget("你目前沒有權限開啟這個儲存庫。")
      }
      val artifact = allArtifacts.firstOrNull {
          it.id == target.artifactId && it.repoId == target.repositoryId
      } ?: return rejectTarget("通知指向的成果已不存在或不屬於該儲存庫。")

      openedTarget = null
      viewModel.selectRepository(repo)
      viewModel.selectArtifact(artifact)
      onNavigateToArtifact(target.repositoryId, target.artifactId)
      true
  }

  is CollaborationTarget.Issue -> {
      val repo = repositories.firstOrNull { it.id == target.repositoryId }
          ?: return rejectTarget("通知指向的儲存庫已不存在。")
      if (!canOpenRepository(repo)) {
          return rejectTarget("你目前沒有權限開啟這個儲存庫任務。")
      }
      val issue = allIssues.firstOrNull {
          it.id == target.issueId && it.repoId == target.repositoryId
      } ?: return rejectTarget("通知指向的任務已不存在或不屬於該儲存庫。")

      targetFailureMessage = null
      openedTarget = target
      viewModel.loadIssueComments(issue.id)
      true
  }

  is CollaborationTarget.Discussion -> {
      val repo = repositories.firstOrNull { it.id == target.repositoryId }
          ?: return rejectTarget("通知指向的儲存庫已不存在。")
      if (!canOpenRepository(repo)) {
          return rejectTarget("你目前沒有權限開啟這個儲存庫討論。")
      }
      val discussion = allDiscussions.firstOrNull {
          it.id == target.discussionId && it.repoId == target.repositoryId
      } ?: return rejectTarget("通知指向的討論已不存在或不屬於該儲存庫。")

      targetFailureMessage = null
      openedTarget = target
      viewModel.loadDiscussionComments(discussion.id)
      true
  }

  is CollaborationTarget.Repository -> {
      val repo = repositories.firstOrNull { it.id == target.repositoryId }
          ?: return rejectTarget("通知指向的儲存庫已不存在。")
      if (!canOpenRepository(repo)) {
          return rejectTarget("你目前沒有權限開啟這個儲存庫。")
      }

      openedTarget = null
      viewModel.selectRepository(repo)
      onNavigateToRepository(target.repositoryId)
      true
  }

  is CollaborationTarget.Organization -> {
      val actor = activeUser ?: return rejectTarget("尚未選擇有效的使用者身分。")
      val organization = organizations.firstOrNull { it.id == target.organizationId }
          ?: return rejectTarget("通知指向的組織已不存在。")
      val canOpen = organization.enterpriseId == actor.enterpriseId &&
          (actor.isEnterpriseAdmin || allOrgMemberships.any {
              it.orgId == organization.id && it.userId == actor.id
          })
      if (!canOpen) return rejectTarget("你目前沒有權限開啟這個組織。")

      openedTarget = null
      onNavigateToOrg(organization.id)
      true
  }

  is CollaborationTarget.Team -> {
      val actor = activeUser ?: return rejectTarget("尚未選擇有效的使用者身分。")
      val team = allTeams.firstOrNull { it.id == target.teamId }
          ?: return rejectTarget("通知指向的團隊已不存在。")
      val organization = organizations.firstOrNull { it.id == team.orgId }
          ?: return rejectTarget("通知指向的團隊沒有有效的所屬組織。")
      val canOpen = organization.enterpriseId == actor.enterpriseId &&
          (actor.isEnterpriseAdmin || allTeamMemberships.any {
              it.teamId == team.id && it.userId == actor.id
          })
      if (!canOpen) return rejectTarget("你目前沒有權限開啟這個團隊。")

      openedTarget = null
      onNavigateToOrg(team.orgId)
      true
  }

  is CollaborationTarget.UserProfile -> {
      val actor = activeUser ?: return rejectTarget("尚未選擇有效的使用者身分。")
      val profileUser = allUsers.firstOrNull { it.id == target.userId }
          ?: return rejectTarget("通知指向的使用者已不存在。")
      if (profileUser.enterpriseId != actor.enterpriseId) {
          return rejectTarget("不能開啟其他企業範圍的使用者資料。")
      }

      openedTarget = null
      onNavigateToUserProfile(profileUser)
      true
  }

  null -> rejectTarget("這則通知沒有可開啟的有效協作目標。")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // 1. ACTIVE USER PERSONA SELECTOR & INBOX HEADER
        item {
            InboxHeaderCard(
                activeUser = activeUser,
                allUsers = allUsers,
                unreadCount = unreadCount,
                actionableCount = actionableCount,
                onSwitchPersona = { viewModel.switchActiveUser(it) },
                onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                onToggleArchitectureExplainer = { showArchitectureExplainer = !showArchitectureExplainer }
            )
        }

        // 2. ARCHITECTURE & GOVERNANCE RELATION EXPLAINER (COLLAPSIBLE)
        if (showArchitectureExplainer) {
            item {
                InboxGovernanceArchitectureCard(
                    onDismiss = { showArchitectureExplainer = false }
                )
            }
        }

        // 3. INBOX STATUS TABS (ALL / UNREAD / 需處理 / ARCHIVED)
        item {
            Surface(
                color = SophisticatedSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SophisticatedBorder)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = LavenderPrimary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = LavenderPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    InboxFilterTab.values().forEach { tab ->
                        val count = when (tab) {
                            InboxFilterTab.ALL -> notifications.count { it.status != NotificationStatus.ARCHIVED }
                            InboxFilterTab.UNREAD -> unreadCount
                            InboxFilterTab.ACTIONABLE -> actionableCount
                            InboxFilterTab.ARCHIVED -> notifications.count { it.status == NotificationStatus.ARCHIVED }
                        }
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == tab) TextHighEmphasis else TextMediumEmphasis
                                    )
                                    if (count > 0) {
                                        Surface(
                                            color = if (selectedTab == tab) LavenderContainer else SophisticatedContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                color = if (selectedTab == tab) LavenderGlow else TextMediumEmphasis,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("inbox_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 4. CATEGORY FILTER CHIPS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "分類篩選",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLowEmphasis,
                    letterSpacing = 1.sp
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { viewModel.setNotificationFilterCategory(null) },
                        label = { Text("所有分類（${notifications.count { if (selectedTab == InboxFilterTab.ARCHIVED) it.status == NotificationStatus.ARCHIVED else it.status != NotificationStatus.ARCHIVED }}）") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LavenderPrimary,
                            selectedLabelColor = LavenderOnPrimary,
                            containerColor = SophisticatedSurface,
                            labelColor = TextHighEmphasis
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategoryFilter == null) Color.Transparent else SophisticatedBorder,
                            enabled = true,
                            selected = selectedCategoryFilter == null
                        )
                    )
                    NotificationCategory.values().forEach { cat ->
                        val count = notifications.count { notif ->
                            val matchesTab = if (selectedTab == InboxFilterTab.ARCHIVED) notif.status == NotificationStatus.ARCHIVED else notif.status != NotificationStatus.ARCHIVED
                            matchesTab && notif.category == cat
                        }
                        if (count > 0 || selectedCategoryFilter == cat) {
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    viewModel.setNotificationFilterCategory(if (selectedCategoryFilter == cat) null else cat)
                                },
                                label = { Text("${cat.label} ($count)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedCategoryFilter == cat) LavenderOnPrimary else getCategoryColor(cat)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LavenderPrimary,
                                    selectedLabelColor = LavenderOnPrimary,
                                    containerColor = SophisticatedSurface,
                                    labelColor = TextHighEmphasis
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (selectedCategoryFilter == cat) Color.Transparent else SophisticatedBorder,
                                    enabled = true,
                                    selected = selectedCategoryFilter == cat
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. NOTIFICATION ITEMS LIST
        if (filteredNotifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, SophisticatedBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "目前都處理完了",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis
                        )
                        Text(
                            text = when (selectedTab) {
                                InboxFilterTab.ALL -> "收件匣目前沒有進行中的通知。"
                                InboxFilterTab.UNREAD -> "所有待處理通知皆已讀取。"
                                InboxFilterTab.ACTIONABLE -> "目前沒有待審查、待核准或任務指派。"
                                InboxFilterTab.ARCHIVED -> "目前沒有已封存通知。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMediumEmphasis
                        )
                    }
                }
            }
        } else {
            items(filteredNotifications, key = { it.id }) { notif ->
                NotificationItemCard(
                    notification = notif,
                    onMarkRead = { viewModel.markNotificationAsRead(notif.id) },
                    onArchive = { viewModel.archiveNotification(notif.id) },
                    onDelete = { viewModel.deleteNotification(notif.id) },
                    onInspectContext = { inspectingNotification = notif },
                    onPerformAction = {
              val opened = openNotificationTarget(notif)
              if (opened && notif.isActionable) {
                  viewModel.markNotificationActionCompleted(notif.id)
              }
          }
                )
            }
        }
    }

    // 6. INSPECTION DETAIL DIALOG (RELATIONAL LINKAGE GRAPH)
    if (inspectingNotification != null) {
        NotificationRelationalDetailDialog(
            notification = inspectingNotification!!,
            onDismiss = { inspectingNotification = null },
            onNavigateToEntity = {
      val notif = inspectingNotification!!
      if (openNotificationTarget(notif)) {
          inspectingNotification = null
      }
  }
        )
    }


    val issueTarget = openedTarget as? CollaborationTarget.Issue
    if (issueTarget != null) {
        val repo = repositories.firstOrNull { it.id == issueTarget.repositoryId }
        val repoIssues = allIssues.filter { it.repoId == issueTarget.repositoryId }
        val issue = repoIssues.firstOrNull { it.id == issueTarget.issueId }
        if (repo != null && issue != null) {
  val descendantIds = IssueHierarchyRules.descendantIds(issue.id, repoIssues)
  val subIssues = IssueHierarchyRules.orderedForDisplay(repoIssues)
      .map { it.first }
      .filter { it.id in descendantIds }
  val repoDependencies = allDependencies.filter { it.repoId == repo.id }
  val blockedBy = repoDependencies
      .filter { it.blockedIssueId == issue.id }
      .mapNotNull { dependency ->
          repoIssues.firstOrNull { it.id == dependency.blockingIssueId }
              ?.let { dependency to it }
      }
  val blocking = repoDependencies
      .filter { it.blockingIssueId == issue.id }
      .mapNotNull { dependency ->
          repoIssues.firstOrNull { it.id == dependency.blockedIssueId }
              ?.let { dependency to it }
      }
  val repoArtifacts = allArtifacts.filter { it.repoId == repo.id }

  IssueDetailDialog(
      repo = repo,
      issue = issue,
      subIssues = subIssues,
      blockedByDependencies = blockedBy,
      blockingDependencies = blocking,
      allRepoIssues = repoIssues,
      comments = selectedIssueComments,
      allUsers = allUsers,
      allTeams = allTeams,
      repoArtifacts = repoArtifacts,
      activeUser = activeUser,
      onDismiss = { openedTarget = null },
      onAddComment = { content ->
          viewModel.addIssueComment(issue.id, content) {
              viewModel.loadIssueComments(issue.id)
          }
      },
      onUpdateStatus = { status -> viewModel.updateIssueStatus(issue.id, status) },
      onAssignIssue = { type, id, name ->
          viewModel.assignIssue(issue.id, type, id, name)
      },
      onLinkParent = { parentId ->
          viewModel.linkParentIssue(issue.id, parentId) {}
      },
      onAddDependency = { blockingIssueId ->
          viewModel.addIssueDependency(repo.id, issue.id, blockingIssueId) {}
      },
      onRemoveDependency = { dependencyId ->
          viewModel.removeIssueDependency(dependencyId) {}
      },
      onAddSubIssue = {
          openedTarget = null
          viewModel.selectRepository(repo)
          onNavigateToRepository(repo.id)
      },
      onSelectArtifact = { artifactId ->
          val artifact = repoArtifacts.firstOrNull { it.id == artifactId }
          if (artifact == null) {
              rejectTarget("任務連結的成果已不存在。")
          } else {
              openedTarget = null
              viewModel.selectRepository(repo)
              viewModel.selectArtifact(artifact)
              onNavigateToArtifact(repo.id, artifact.id)
          }
      }
  )
        }
    }

    val discussionTarget = openedTarget as? CollaborationTarget.Discussion
    if (discussionTarget != null) {
        val repo = repositories.firstOrNull { it.id == discussionTarget.repositoryId }
        val discussion = allDiscussions.firstOrNull {
  it.id == discussionTarget.discussionId && it.repoId == discussionTarget.repositoryId
        }
        if (repo != null && discussion != null) {
  val effectiveRole = activeUser?.let { actor ->
      HierarchicalPolicyEngine.resolveEffectiveRole(
          actor = actor,
          repo = repo,
          orgMemberships = allOrgMemberships,
          teamMemberships = allTeamMemberships,
          teams = allTeams,
          accessRules = allAccessRules
      ).first
  } ?: RepoRole.VIEWER

  DiscussionDetailDialog(
      discussion = discussion,
      comments = selectedDiscussionComments,
      activeUser = activeUser,
      effectiveRole = effectiveRole,
      onDismiss = { openedTarget = null },
      onAddComment = { content ->
          viewModel.addDiscussionComment(discussion.id, content) {
              viewModel.loadDiscussionComments(discussion.id)
          }
      },
      onToggleLock = { viewModel.toggleLockDiscussion(discussion.id) },
      onMarkAcceptedAnswer = { commentId ->
          viewModel.markAcceptedAnswer(discussion.id, commentId)
      },
      onUpvoteDiscussion = { viewModel.upvoteDiscussion(discussion.id) },
      onUpvoteComment = { commentId ->
          viewModel.upvoteDiscussionComment(commentId, discussion.id)
      }
  )
        }
    }

    if (targetFailureMessage != null) {
        AlertDialog(
  onDismissRequest = { targetFailureMessage = null },
  title = { Text("無法開啟通知目標") },
  text = { Text(targetFailureMessage.orEmpty()) },
  confirmButton = {
      TextButton(onClick = { targetFailureMessage = null }) {
          Text("知道了")
      }
  }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: INBOX HEADER & PERSONA SWITCHER
// -----------------------------------------------------------------------------

@Composable
private fun InboxHeaderCard(
    activeUser: User?,
    allUsers: List<User>,
    unreadCount: Int,
    actionableCount: Int,
    onSwitchPersona: (User) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onToggleArchitectureExplainer: () -> Unit
) {
    var personaDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Title, Unread Tag, Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(LavenderContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "收件匣",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "統一收件匣",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis
                            )
                            if (unreadCount > 0) {
                                Surface(
                                    color = LavenderPrimary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount 則新通知",
                                        color = LavenderOnPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "集中管理協作待辦與可執行事件",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleArchitectureExplainer,
                        modifier = Modifier.testTag("inbox_info_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "架構說明",
                            tint = LavenderPrimary
                        )
                    }
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.testTag("inbox_mark_all_read_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "全部標為已讀",
                                tint = TextHighEmphasis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = SophisticatedBorder)

            // Bottom Row: Active Persona Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = try {
                                    Color(android.graphics.Color.parseColor(activeUser?.avatarColorHex ?: "#8B5CF6"))
                                } catch (e: Exception) {
                                    LavenderPrimary
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeUser?.displayName?.take(1) ?: "U",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            text = "收件匣範圍：${activeUser?.displayName ?: "No User"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighEmphasis
                        )
                        Text(
                            text = "${activeUser?.title ?: ""} • ${if (activeUser?.isEnterpriseAdmin == true) "企業管理員" else "組織成員"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis
                        )
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = { personaDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LavenderPrimary
                        ),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("switch_inbox_persona_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("切換身分", fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = personaDropdownExpanded,
                        onDismissRequest = { personaDropdownExpanded = false },
                        modifier = Modifier
                            .background(SophisticatedSurface)
                            .border(1.dp, SophisticatedBorder, RoundedCornerShape(8.dp))
                    ) {
                        allUsers.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = try {
                                                        Color(android.graphics.Color.parseColor(user.avatarColorHex))
                                                    } catch (e: Exception) {
                                                        LavenderPrimary
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = user.displayName.take(1),
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = user.displayName,
                                                fontWeight = if (user.id == activeUser?.id) FontWeight.Bold else FontWeight.Normal,
                                                color = if (user.id == activeUser?.id) LavenderPrimary else TextHighEmphasis,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = user.title,
                                                fontSize = 11.sp,
                                                color = TextMediumEmphasis
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSwitchPersona(user)
                                    personaDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: NOTIFICATION ITEM CARD
// -----------------------------------------------------------------------------

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onInspectContext: () -> Unit,
    onPerformAction: () -> Unit
) {
    val isUnread = notification.status == NotificationStatus.UNREAD
    val categoryColor = getCategoryColor(notification.category)
    val categoryIcon = getCategoryIcon(notification.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_card_${notification.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) SophisticatedSurface else SophisticatedSurfaceDark
        ),
        border = BorderStroke(
            width = if (isUnread) 1.5.dp else 1.dp,
            color = if (isUnread) SophisticatedBorderSubtle else SophisticatedBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Category Badge, Priority, Timestamp, Unread Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = notification.category.label,
                                color = categoryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Priority Pill if High / Urgent
                    if (notification.priority == NotificationPriority.URGENT || notification.priority == NotificationPriority.HIGH) {
                        Surface(
                            color = if (notification.priority == NotificationPriority.URGENT) RoseDark else AmberWarning.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (notification.priority == NotificationPriority.URGENT) RoseError else AmberWarning)
                        ) {
                            Text(
                                text = notification.priority.label.uppercase(),
                                color = if (notification.priority == NotificationPriority.URGENT) RoseError else AmberGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        fontSize = 11.sp,
                        color = TextMediumEmphasis
                    )
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(LavenderPrimary, CircleShape)
                        )
                    }
                }
            }

            // Hierarchy Context Breadcrumb (e.g. Org > Repo > Artifact/Issue/Discussion)
            val breadcrumb = buildHierarchyBreadcrumb(notification)
            if (breadcrumb.isNotEmpty()) {
                Surface(
                    color = SophisticatedContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = LavenderSubtle,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = breadcrumb,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = LavenderSubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Actor & Title
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = try {
                                Color(android.graphics.Color.parseColor(notification.actorAvatarColorHex))
                            } catch (e: Exception) {
                                LavenderPrimary
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notification.actorDisplayName.take(1),
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "來自 ${notification.actorDisplayName}",
                        fontSize = 11.sp,
                        color = TextMediumEmphasis
                    )
                }
            }

            // Body
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnread) TextHighEmphasis else TextMediumEmphasis,
                lineHeight = 20.sp
            )

            HorizontalDivider(color = SophisticatedBorder)

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Relational Context Inspector Button
                TextButton(
                    onClick = onInspectContext,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("inspect_notif_${notification.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = LavenderSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "治理關聯",
                        fontSize = 12.sp,
                        color = LavenderSubtle
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary Action: Mark Read / Archive
                    if (isUnread) {
                        IconButton(
                            onClick = onMarkRead,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "標為已讀",
                                tint = TextMediumEmphasis,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (notification.status != NotificationStatus.ARCHIVED) {
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "封存",
                                tint = TextMediumEmphasis,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Primary Action Button (if Actionable)
                    if (notification.isActionable) {
                        val actionLabel = getActionLabel(notification.actionType)
                        val actionIcon = getActionIcon(notification.actionType)
                        Button(
                            onClick = onPerformAction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("primary_action_${notification.id}")
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = actionLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: ARCHITECTURE EXPLAINER CARD
// -----------------------------------------------------------------------------

@Composable
private fun InboxGovernanceArchitectureCard(
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "統一收件匣與稽核紀錄架構",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "關閉",
                        tint = TextMediumEmphasis
                    )
                }
            }

            Text(
                text = "統一收件匣在企業治理階層上建立以使用者為中心的協作層，並在架構上與不可任意竄改的守規稽核紀錄分離：",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Personal Inbox Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = SophisticatedContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SophisticatedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Notifications, null, tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                            Text("統一收件匣", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LavenderGlow)
                        }
                        Text("• 僅屬目前使用者身分", fontSize = 11.sp, color = TextHighEmphasis)
                        Text("• 互動狀態：未讀／已讀／已封存", fontSize = 11.sp, color = TextMediumEmphasis)
                        Text("• 可執行：直接處理核准與審查", fontSize = 11.sp, color = TextMediumEmphasis)
                        Text("• 依使用者被指派的責任範圍顯示", fontSize = 11.sp, color = TextMediumEmphasis)
                    }
                }

                // Public Audit Log Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = SophisticatedContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SophisticatedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Security, null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text("稽核紀錄", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                        }
                        Text("• 全企業不可任意竄改的紀錄帳", fontSize = 11.sp, color = TextHighEmphasis)
                        Text("• 記錄所有政策引擎判定", fontSize = 11.sp, color = TextMediumEmphasis)
                        Text("• 守規與安全追溯事件流", fontSize = 11.sp, color = TextMediumEmphasis)
                        Text("• 僅追加、不可直接操作的歷史", fontSize = 11.sp, color = TextMediumEmphasis)
                    }
                }
            }

            Surface(
                color = SophisticatedSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SophisticatedBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Text(
                        text = "治理規則不變：從通知執行動作時，仍會經過與手動操作完全相同的階層政策引擎驗證。",
                        fontSize = 11.sp,
                        color = AmberGlow
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: RELATIONAL DETAIL MODAL (FULL HIERARCHY GRAPH)
// -----------------------------------------------------------------------------

@Composable
private fun NotificationRelationalDetailDialog(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onNavigateToEntity: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(getCategoryColor(notification.category).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(notification.category),
                        contentDescription = null,
                        tint = getCategoryColor(notification.category),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "通知關聯脈絡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "通知 ID：${notification.id}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMediumEmphasis
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextHighEmphasis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.body,
                        fontSize = 13.sp,
                        color = TextMediumEmphasis
                    )
                }

                item {
                    HorizontalDivider(color = SophisticatedBorder)
                }

                // 1. PARTICIPANTS (RECIPIENT & ACTOR)
                item {
                    RelationalSectionHeader(title = "IDENTITIES & ROLES", icon = Icons.Default.Person)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        RelationalRow(
                            label = "Recipient (Inbox Owner)",
                            value = notification.recipientUserId,
                            highlight = LavenderPrimary
                        )
                        RelationalRow(
                            label = "Actor (Triggered Event)",
                            value = "${notification.actorDisplayName} (${notification.actorUserId})"
                        )
                    }
                }

                // 2. GOVERNANCE BOUNDARIES (ENTERPRISE, ORG, TEAM)
                item {
                    RelationalSectionHeader(title = "GOVERNANCE BOUNDARIES", icon = Icons.Default.Apartment)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (notification.enterpriseId != null) {
                            RelationalRow(label = "Enterprise Root", value = notification.enterpriseId)
                        }
                        if (notification.orgName != null || notification.orgId != null) {
                            RelationalRow(label = "組織", value = "${notification.orgName ?: ""} (${notification.orgId ?: ""})")
                        }
                        if (notification.teamName != null || notification.teamId != null) {
                            RelationalRow(label = "Team Context", value = "${notification.teamName ?: ""} (${notification.teamId ?: ""})")
                        }
                    }
                }

                // 3. TARGET ARTIFACT / ISSUE / DISCUSSION / REPO
                item {
                    RelationalSectionHeader(title = "CONTAINER & TARGET OBJECT", icon = Icons.Default.Folder)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (notification.repoName != null || notification.repoId != null) {
                            RelationalRow(label = "Repository", value = "${notification.repoName ?: ""} (${notification.repoId ?: ""})")
                        }
                        if (notification.artifactTitle != null || notification.artifactId != null) {
                            RelationalRow(label = "No-Code Artifact", value = "${notification.artifactTitle ?: ""} (${notification.artifactId ?: ""})", highlight = LavenderPrimary)
                        }
                        if (notification.issueTitle != null || notification.issueId != null) {
                            RelationalRow(label = "Repository Issue", value = "${notification.issueTitle ?: ""} (${notification.issueId ?: ""})", highlight = AmberWarning)
                        }
                        if (notification.discussionTitle != null || notification.discussionId != null) {
                            RelationalRow(label = "RFC Discussion", value = "${notification.discussionTitle ?: ""} (${notification.discussionId ?: ""})", highlight = PinkAccent)
                        }
                    }
                }

                // 4. REVIEW, APPROVAL, MEMBERSHIP IDS
                if (notification.reviewId != null || notification.approvalId != null || notification.membershipId != null) {
                    item {
                        RelationalSectionHeader(title = "GATE & SECURITY AUDIT LINKS", icon = Icons.Default.Security)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (notification.reviewId != null) {
                                RelationalRow(label = "Peer Review Ref", value = notification.reviewId)
                            }
                            if (notification.approvalId != null) {
                                RelationalRow(label = "Approval Gate Ref", value = notification.approvalId)
                            }
                            if (notification.membershipId != null) {
                                RelationalRow(label = "Membership Ref", value = notification.membershipId)
                            }
                        }
                    }
                }

                // 5. AUDIT LOG SEPARATION EXPLANATION
                item {
                    Surface(
                        color = SophisticatedContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SophisticatedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "與系統稽核紀錄分離",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = LavenderGlow
                            )
                            Text(
                                text = "此通知將互動工作導向 ${notification.recipientUserId}，而相對應的動作仍會在稽核紀錄中保留不可任意竄改的追溯證據，供企業守規人員查核。",
                                fontSize = 11.sp,
                                color = TextMediumEmphasis,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (notification.repoId != null || notification.orgId != null) {
                Button(
                    onClick = onNavigateToEntity,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = LavenderOnPrimary
                    ),
                    modifier = Modifier.testTag("jump_to_entity_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("開啟目標物件")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("關閉", color = TextMediumEmphasis)
            }
        },
        containerColor = SophisticatedSurface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun RelationalSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextLowEmphasis,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun RelationalRow(
    label: String,
    value: String,
    highlight: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMediumEmphasis
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlight != null) FontWeight.Bold else FontWeight.Normal,
            color = highlight ?: TextHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// HELPER FUNCTIONS & FORMATTERS
// -----------------------------------------------------------------------------

private fun getCategoryColor(category: NotificationCategory): Color {
    return when (category) {
        NotificationCategory.REVIEW_REQUEST -> LavenderPrimary
        NotificationCategory.APPROVAL_GATE -> EmeraldSuccess
        NotificationCategory.ISSUE_ASSIGNMENT -> AmberWarning
        NotificationCategory.MENTION_AND_REPLY -> PinkAccent
        NotificationCategory.ACCESS_CHANGE -> LavenderSubtle
        NotificationCategory.MEMBERSHIP_CHANGE -> TextHighEmphasis
        NotificationCategory.PUBLICATION -> EmeraldSuccess
        NotificationCategory.GOVERNANCE_EVENT -> RoseError
    }
}

private fun getCategoryIcon(category: NotificationCategory): ImageVector {
    return when (category) {
        NotificationCategory.REVIEW_REQUEST -> Icons.Default.RateReview
        NotificationCategory.APPROVAL_GATE -> Icons.Default.Approval
        NotificationCategory.ISSUE_ASSIGNMENT -> Icons.Default.TaskAlt
        NotificationCategory.MENTION_AND_REPLY -> Icons.Default.Forum
        NotificationCategory.ACCESS_CHANGE -> Icons.Default.VpnKey
        NotificationCategory.MEMBERSHIP_CHANGE -> Icons.Default.Groups
        NotificationCategory.PUBLICATION -> Icons.Default.Lock
        NotificationCategory.GOVERNANCE_EVENT -> Icons.Default.Shield
    }
}

private fun getActionLabel(actionType: String?): String {
    return when (actionType) {
        "REVIEW" -> "Review Blueprint"
        "APPROVE" -> "Inspect & Approve"
        "VIEW_ISSUE" -> "Open Issue"
        "VIEW_DISCUSSION" -> "View Discussion"
        "VIEW_ARTIFACT" -> "Inspect Artifact"
        "VIEW_REPO" -> "Explore Repo"
        "VIEW_TEAM" -> "Inspect Team"
        "VIEW_ORG" -> "View Org"
        else -> "Inspect"
    }
}

private fun getActionIcon(actionType: String?): ImageVector {
    return when (actionType) {
        "REVIEW" -> Icons.Default.RateReview
        "APPROVE" -> Icons.Default.Approval
        "VIEW_ISSUE" -> Icons.Default.TaskAlt
        "VIEW_DISCUSSION" -> Icons.Default.Forum
        "VIEW_ARTIFACT" -> Icons.Default.Description
        "VIEW_REPO" -> Icons.Default.Folder
        "VIEW_TEAM" -> Icons.Default.Groups
        "VIEW_ORG" -> Icons.Default.Apartment
        else -> Icons.Default.Visibility
    }
}

private fun buildHierarchyBreadcrumb(notification: AppNotification): String {
    val parts = mutableListOf<String>()
    if (!notification.orgName.isNullOrBlank()) {
        parts.add(notification.orgName)
    }
    if (!notification.teamName.isNullOrBlank()) {
        parts.add(notification.teamName)
    }
    if (!notification.repoName.isNullOrBlank()) {
        parts.add(notification.repoName)
    }
    if (!notification.artifactTitle.isNullOrBlank()) {
        parts.add(notification.artifactTitle)
    } else if (!notification.issueTitle.isNullOrBlank()) {
        parts.add("Issue: ${notification.issueTitle.take(20)}...")
    } else if (!notification.discussionTitle.isNullOrBlank()) {
        parts.add("RFC: ${notification.discussionTitle.take(20)}...")
    }
    return parts.joinToString(" > ")
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "yesterday"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
