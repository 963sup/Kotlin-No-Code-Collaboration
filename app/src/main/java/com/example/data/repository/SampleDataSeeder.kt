package com.example.data.repository

import com.example.data.local.GovernanceDao
import com.example.data.model.AppNotification
import com.example.data.model.ApprovalStatus
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.ArtifactType
import com.example.data.model.AuditLog
import com.example.data.model.DependencyType
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
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

object SampleDataSeeder {

    suspend fun seedInitialDataIfEmpty(dao: GovernanceDao) {
        val existingEnterprise = dao.getEnterpriseOnce()
        if (existingEnterprise != null) return

        // 1. ENTERPRISE
        val enterprise = Enterprise(
            id = "ent_acme_global",
            name = "Acme Global Enterprise",
            slug = "acme-enterprise",
            description = "Global enterprise governance domain with strict multi-layer access control policies.",
            enforceDualApproval = true,
            allowUserOwnedRepos = true,
            enforceReviewerBeforeApprover = true,
            enforceSegregationOfDuties = true,
        )
        dao.insertEnterprise(enterprise)

        // 2. USERS
        val users = listOf(
            User(
                id = "usr_sarah_chen",
                enterpriseId = enterprise.id,
                username = "sarah_chen",
                displayName = "Sarah Chen",
                email = "sarah.chen@acme.io",
                title = "VP of Architecture & Org Owner",
                avatarColorHex = "#8B5CF6",
                isEnterpriseAdmin = true,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_marcus_vance",
                enterpriseId = enterprise.id,
                username = "marcus_vance",
                displayName = "Marcus Vance",
                email = "marcus.vance@acme.io",
                title = "Principal Security Approver",
                avatarColorHex = "#EC4899",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_elena_rostova",
                enterpriseId = enterprise.id,
                username = "elena_rostova",
                displayName = "Elena Rostova",
                email = "elena.r@acme.io",
                title = "Senior Governance Reviewer",
                avatarColorHex = "#3B82F6",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_david_kim",
                enterpriseId = enterprise.id,
                username = "david_kim",
                displayName = "David Kim",
                email = "david.kim@acme.io",
                title = "No-Code Workflow Builder",
                avatarColorHex = "#10B981",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_maya_lin",
                enterpriseId = enterprise.id,
                username = "maya_lin",
                displayName = "Maya Lin",
                email = "maya.lin@acme.io",
                title = "Product Stakeholder & Viewer",
                avatarColorHex = "#F59E0B",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_alex_rivera",
                enterpriseId = enterprise.id,
                username = "alex_rivera",
                displayName = "Alex Rivera",
                email = "alex.rivera@community.org",
                title = "Independent Solutions Specialist",
                avatarColorHex = "#06B6D4",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_wang",
                enterpriseId = enterprise.id,
                username = "wang_xiaoming",
                displayName = "王小明",
                email = "wang.xm@acme.io",
                title = "製造工程師",
                avatarColorHex = "#8B5CF6",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_li",
                enterpriseId = enterprise.id,
                username = "li_jiaying",
                displayName = "李佳穎",
                email = "li.jy@acme.io",
                title = "品質核驗專家",
                avatarColorHex = "#10B981",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_zhang",
                enterpriseId = enterprise.id,
                username = "zhang_xiaohua",
                displayName = "張小華",
                email = "zhang.xh@acme.io",
                title = "系統架構師",
                avatarColorHex = "#06B6D4",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
            User(
                id = "usr_chen",
                enterpriseId = enterprise.id,
                username = "chen_zhiqiang",
                displayName = "陳志強",
                email = "chen.zq@acme.io",
                title = "運營主管",
                avatarColorHex = "#EC4899",
                isEnterpriseAdmin = false,
                canOwnerRepository = true,
            ),
        )
        dao.insertUsers(users)

        // 3. ORGANIZATIONS
        val orgCloud = Organization(
            id = "org_cloud_platform",
            enterpriseId = enterprise.id,
            name = "Cloud & AI Platform Org",
            slug = "cloud-ai-platform",
            description = "Enterprise core infrastructure, AI orchestration workflows, and architecture specifications.",
            badgeColorHex = "#4F46E5",
            defaultMemberRole = RepoRole.COLLABORATOR,
            canOwnerRepository = true,
        )
        val orgFintech = Organization(
            id = "org_fintech_solutions",
            enterpriseId = enterprise.id,
            name = "Fintech & Payments Org",
            slug = "fintech-payments",
            description = "Mission-critical financial transaction workflows, compliance forms, and audit gates.",
            badgeColorHex = "#059669",
            defaultMemberRole = RepoRole.VIEWER,
            canOwnerRepository = true,
        )
        dao.insertOrganizations(listOf(orgCloud, orgFintech))

        // 4. ORG MEMBERSHIPS
        val orgMemberships = listOf(
            OrgMembership(orgId = orgCloud.id, userId = "usr_sarah_chen", role = OrgRole.OWNER),
            OrgMembership(orgId = orgCloud.id, userId = "usr_marcus_vance", role = OrgRole.ADMIN),
            OrgMembership(orgId = orgCloud.id, userId = "usr_elena_rostova", role = OrgRole.MEMBER),
            OrgMembership(orgId = orgCloud.id, userId = "usr_david_kim", role = OrgRole.MEMBER),
            OrgMembership(orgId = orgCloud.id, userId = "usr_maya_lin", role = OrgRole.MEMBER),
            OrgMembership(orgId = orgFintech.id, userId = "usr_sarah_chen", role = OrgRole.ADMIN),
            OrgMembership(orgId = orgFintech.id, userId = "usr_marcus_vance", role = OrgRole.OWNER),
            OrgMembership(orgId = orgFintech.id, userId = "usr_elena_rostova", role = OrgRole.MEMBER),
        )
        dao.insertOrgMemberships(orgMemberships)

        // 5. TEAMS (Strictly child of Organization, CANNOT own repos)
        val teamCoreInfra = Team(
            id = "team_core_infra",
            orgId = orgCloud.id,
            name = "Core Infrastructure Team",
            slug = "core-infra",
            description = "Manages enterprise architectural blueprints and system integration specs.",
            canOwnerRepository = false,
        )
        val teamSecurity = Team(
            id = "team_security_governance",
            orgId = orgCloud.id,
            name = "Security & Governance Team",
            slug = "security-gov",
            description = "Provides mandatory compliance reviews and cryptographic approval sign-offs.",
            canOwnerRepository = false,
        )
        val teamProductEngineering = Team(
            id = "team_product_eng",
            orgId = orgCloud.id,
            name = "Product Engineering Team",
            slug = "product-eng",
            description = "Designs no-code workflow automations and customer journey schemas.",
            canOwnerRepository = false,
        )
        dao.insertTeams(listOf(teamCoreInfra, teamSecurity, teamProductEngineering))

        // 6. TEAM MEMBERSHIPS
        val teamMemberships = listOf(
            TeamMembership(teamId = teamCoreInfra.id, userId = "usr_elena_rostova", role = TeamRole.MAINTAINER),
            TeamMembership(teamId = teamCoreInfra.id, userId = "usr_david_kim", role = TeamRole.MEMBER),
            TeamMembership(teamId = teamSecurity.id, userId = "usr_marcus_vance", role = TeamRole.MAINTAINER),
            TeamMembership(teamId = teamSecurity.id, userId = "usr_sarah_chen", role = TeamRole.MEMBER),
            TeamMembership(teamId = teamProductEngineering.id, userId = "usr_david_kim", role = TeamRole.MAINTAINER),
            TeamMembership(teamId = teamProductEngineering.id, userId = "usr_maya_lin", role = TeamRole.MEMBER),
        )
        dao.insertTeamMemberships(teamMemberships)

        // 7. REPOSITORIES (Strict No-Code Collaboration Containers; Owned by ORG or USER)
        val repo1 = Repository(
            id = "repo_enterprise_orchestration",
            name = "enterprise-orchestration-blueprints",
            displayName = "Enterprise Orchestration Blueprints",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgCloud.id,
            ownerDisplayName = orgCloud.name,
            enterpriseId = enterprise.id,
            description = "Centralized no-code operational process maps, trigger trees, and cross-department routing pipelines.",
            category = "Process Automation",
            requiredApproverCount = 2,
            requireReviewerPass = true,
            preventSelfApproval = true,
        )
        val repo2 = Repository(
            id = "repo_payment_compliance_specs",
            name = "payment-compliance-decision-records",
            displayName = "Payment Compliance Decision Records",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgFintech.id,
            ownerDisplayName = orgFintech.name,
            enterpriseId = enterprise.id,
            description = "ADR/RFC decision matrix for multi-currency settlement limits, AML approval triggers, and fraud rulebooks.",
            category = "Governance & RFCs",
            requiredApproverCount = 2,
            requireReviewerPass = true,
            preventSelfApproval = true,
        )
        val repo3 = Repository(
            id = "repo_alex_design_tokens",
            name = "alex-design-system-tokens",
            displayName = "Design System Semantic Tokens",
            ownerType = OwnerType.USER,
            ownerId = "usr_alex_rivera",
            ownerDisplayName = "Alex Rivera (@alex_rivera)",
            enterpriseId = enterprise.id,
            description = "User-owned workspace for visual design token schemas, typography scales, and responsive breakpoint specs.",
            category = "Design Architecture",
            requiredApproverCount = 1,
            requireReviewerPass = true,
            preventSelfApproval = false,
        )
        val repoMfg = Repository(
            id = "repo_mfg_opt",
            name = "manufacturing-line-optimization",
            displayName = "製造線優化專案",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgCloud.id,
            ownerDisplayName = orgCloud.name,
            enterpriseId = enterprise.id,
            description = "產線效能監控、基座沉降觀測與自動化調校作業規範。",
            category = "製造工程",
            requiredApproverCount = 2,
            requireReviewerPass = true,
            preventSelfApproval = true,
        )
        val repoEquip = Repository(
            id = "repo_equip_insp",
            name = "equipment-inspection-management",
            displayName = "設備檢修管理系統",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgCloud.id,
            ownerDisplayName = orgCloud.name,
            enterpriseId = enterprise.id,
            description = "定期檢修流程、巡檢標準作業程序與故障診斷決策庫。",
            category = "設備運維",
            requiredApproverCount = 2,
            requireReviewerPass = true,
            preventSelfApproval = true,
        )
        val repoCust = Repository(
            id = "repo_cust_serv",
            name = "customer-service-optimization",
            displayName = "客服流程優化專案",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgFintech.id,
            ownerDisplayName = orgFintech.name,
            enterpriseId = enterprise.id,
            description = "客戶服務 SLA 升級與智能工單分派節點定義。",
            category = "客戶運營",
            requiredApproverCount = 1,
            requireReviewerPass = true,
            preventSelfApproval = false,
        )
        val repoMkt = Repository(
            id = "repo_mkt_exec",
            name = "marketing-campaign-execution",
            displayName = "市場推廣執行專案",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgFintech.id,
            ownerDisplayName = orgFintech.name,
            enterpriseId = enterprise.id,
            description = "季末推廣活動排期、廣告投放預算決策與成效追蹤。",
            category = "市場推廣",
            requiredApproverCount = 1,
            requireReviewerPass = true,
            preventSelfApproval = false,
        )
        val repoTrain = Repository(
            id = "repo_train_prog",
            name = "internal-training-program",
            displayName = "內部培訓計畫",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = orgCloud.id,
            ownerDisplayName = orgCloud.name,
            enterpriseId = enterprise.id,
            description = "新進工程師培育手冊、安全合規考試與技能矩陣評鑑。",
            category = "組織發展",
            requiredApproverCount = 1,
            requireReviewerPass = false,
            preventSelfApproval = false,
        )
        dao.insertRepositories(listOf(repoMfg, repoEquip, repoCust, repoMkt, repoTrain, repo1, repo2, repo3))

        // 8. REPOSITORY ACCESS RULES (Collaborator / Team / Role Mappings)
        val accessRules = listOf(
            // Repo 1 mappings:
            RepoAccessRule(
                repoId = repo1.id,
                granteeType = GranteeType.TEAM,
                granteeId = teamSecurity.id,
                granteeName = teamSecurity.name,
                role = RepoRole.APPROVER,
                grantedByUserId = "usr_sarah_chen",
            ),
            RepoAccessRule(
                repoId = repo1.id,
                granteeType = GranteeType.TEAM,
                granteeId = teamCoreInfra.id,
                granteeName = teamCoreInfra.name,
                role = RepoRole.REVIEWER,
                grantedByUserId = "usr_sarah_chen",
            ),
            RepoAccessRule(
                repoId = repo1.id,
                granteeType = GranteeType.USER,
                granteeId = "usr_david_kim",
                granteeName = "David Kim",
                role = RepoRole.COLLABORATOR,
                grantedByUserId = "usr_sarah_chen",
            ),
            RepoAccessRule(
                repoId = repo1.id,
                granteeType = GranteeType.USER,
                granteeId = "usr_marcus_vance",
                granteeName = "Marcus Vance",
                role = RepoRole.APPROVER,
                grantedByUserId = "usr_sarah_chen",
            ),
            // Repo 2 mappings:
            RepoAccessRule(
                repoId = repo2.id,
                granteeType = GranteeType.TEAM,
                granteeId = teamSecurity.id,
                granteeName = teamSecurity.name,
                role = RepoRole.MAINTAINER,
                grantedByUserId = "usr_marcus_vance",
            ),
            RepoAccessRule(
                repoId = repo2.id,
                granteeType = GranteeType.USER,
                granteeId = "usr_elena_rostova",
                granteeName = "Elena Rostova",
                role = RepoRole.REVIEWER,
                grantedByUserId = "usr_marcus_vance",
            ),
            RepoAccessRule(
                repoId = repo2.id,
                granteeType = GranteeType.USER,
                granteeId = "usr_sarah_chen",
                granteeName = "Sarah Chen",
                role = RepoRole.APPROVER,
                grantedByUserId = "usr_marcus_vance",
            ),
            // Repo 3 (User Owned) mappings:
            RepoAccessRule(
                repoId = repo3.id,
                granteeType = GranteeType.USER,
                granteeId = "usr_david_kim",
                granteeName = "David Kim",
                role = RepoRole.REVIEWER,
                grantedByUserId = "usr_alex_rivera",
            ),
        )
        dao.insertRepoAccessRules(accessRules)

        // 9. NO-CODE ARTIFACTS
        val artifact1 = NoCodeArtifact(
            id = "art_incident_triage_flow",
            repoId = repo1.id,
            title = "High-Severity Automated Incident Escalation Flow",
            type = ArtifactType.PROCESS_WORKFLOW,
            summary = "Visual multi-stage event trigger routing critical production anomalies to on-call squads with auto-escalation SLAs.",
            structuredContent = """
                {
                  "trigger": "Cloud Event Severity == CRITICAL",
                  "stages": [
                    {"step": 1, "action": "Notify On-Call Lead via Pager", "sla_minutes": 5},
                    {"step": 2, "action": "Spin up Incident Command Bridge", "participants": ["Security Officer", "Infra Lead"]},
                    {"step": 3, "action": "Snapshot Audit State & Quarantine Affected Pods", "auto_execute": true}
                  ],
                  "compliance_gate": "Requires Dual Approver Sign-off for release"
                }
            """.trimIndent(),
            lifecycleState = LifecycleState.PENDING_APPROVAL,
            authorUserId = "usr_david_kim",
            authorDisplayName = "David Kim",
            version = "v1.3.0",
            lockedByPolicy = false,
        )

        val artifact2 = NoCodeArtifact(
            id = "art_adr_cross_region_routing",
            repoId = repo1.id,
            title = "ADR-042: Active-Active Cross-Region Failover Topology",
            type = ArtifactType.DECISION_RECORD,
            summary = "Consensus architecture decision documenting zero-downtime routing strategies across multi-cloud availability zones.",
            structuredContent = """
                {
                  "context": "Need 99.999% uptime with automated DNS failover under 30 seconds.",
                  "decision": "Adopt Anycast BGP routing paired with distributed health probes.",
                  "consequences": ["Eliminates single cloud vendor lock-in", "Adds 4ms telemetry sync overhead"],
                  "status": "In Review by Core Infrastructure Reviewers"
                }
            """.trimIndent(),
            lifecycleState = LifecycleState.IN_REVIEW,
            authorUserId = "usr_elena_rostova",
            authorDisplayName = "Elena Rostova",
            version = "v2.0.0",
            lockedByPolicy = false,
        )

        val artifact3 = NoCodeArtifact(
            id = "art_kyc_aml_form_schema",
            repoId = repo2.id,
            title = "Tier-3 Corporate Customer KYC Onboarding Form Schema",
            type = ArtifactType.FORM_SCHEMA,
            summary = "Dynamic adaptive form with automated validation rules, sanction check integration hooks, and signature capturing.",
            structuredContent = """
                {
                  "sections": [
                    {"title": "Entity Verification", "fields": ["Tax ID", "Jurisdiction Certificate", "Beneficial Owners"]},
                    {"title": "Financial Audit History", "fields": ["Annual Turnover", "Audited Balance Sheet (PDF)"]},
                    {"title": "Risk Rating Calculator", "type": "No-Code Formula", "formula": "RiskIndex = (JurisdictionWeight * 0.4) + (VolumeWeight * 0.6)"}
                  ]
                }
            """.trimIndent(),
            lifecycleState = LifecycleState.PUBLISHED,
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            version = "v3.1.0",
            lockedByPolicy = true,
        )

        val artifact4 = NoCodeArtifact(
            id = "art_design_color_palette_spec",
            repoId = repo3.id,
            title = "Universal Design System Color & Elevation Tokens",
            type = ArtifactType.SPECIFICATION_DOC,
            summary = "Standardized WCAG AAA compliant color scales, dynamic dark mode mappings, and surface elevation curves.",
            structuredContent = """
                {
                  "token_groups": [
                    {"name": "Primary Indigo", "values": {"50": "#EEF2FF", "500": "#6366F1", "900": "#312E81"}},
                    {"name": "Status Emerald", "values": {"50": "#ECFDF5", "500": "#10B981", "900": "#064E3B"}},
                    {"name": "Elevation", "values": {"level1": "1dp", "level2": "3dp", "level3": "6dp", "level4": "8dp"}}
                  ]
                }
            """.trimIndent(),
            lifecycleState = LifecycleState.DRAFT,
            authorUserId = "usr_alex_rivera",
            authorDisplayName = "Alex Rivera",
            version = "v0.9.0",
            lockedByPolicy = false,
        )

        dao.insertArtifacts(listOf(artifact1, artifact2, artifact3, artifact4))

        // 10. REVIEWS & APPROVALS
        val review1 = ArtifactReview(
            artifactId = artifact1.id,
            reviewerUserId = "usr_elena_rostova",
            reviewerDisplayName = "Elena Rostova",
            decision = ReviewDecision.APPROVED,
            feedbackNote = "Thorough escalation logic. SLA triggers verified with platform on-call policy.",
        )
        dao.insertReview(review1)

        val approval1 = ArtifactApproval(
            artifactId = artifact1.id,
            approverUserId = "usr_sarah_chen",
            approverDisplayName = "Sarah Chen",
            approverTitle = "VP of Architecture (Org Owner)",
            status = ApprovalStatus.APPROVED,
        )
        dao.insertApproval(approval1)

        val approvalKyc1 = ArtifactApproval(
            artifactId = artifact3.id,
            approverUserId = "usr_sarah_chen",
            approverDisplayName = "Sarah Chen",
            approverTitle = "Org Admin",
            status = ApprovalStatus.APPROVED,
        )
        val approvalKyc2 = ArtifactApproval(
            artifactId = artifact3.id,
            approverUserId = "usr_marcus_vance",
            approverDisplayName = "Marcus Vance",
            approverTitle = "Principal Security Approver",
            status = ApprovalStatus.APPROVED,
        )
        dao.insertApprovals(listOf(approvalKyc1, approvalKyc2))

        // 11. REPO ISSUES & HIERARCHICAL WORK RELATIONSHIPS
        val issue1 = RepoIssue(
            id = "iss_incident_escalation_failover",
            repoId = repo1.id,
            issueNumber = 1,
            title = "Incident Escalation SLA timeout handling missing in Stage 3",
            description = "The workflow diagram does not specify fallback routing if the Tier-2 on-call engineer does not respond within 15 minutes. Need fallback webhook trigger to PagerDuty or escalation to Incident Commander.",
            status = IssueStatus.OPEN,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_elena_rostova",
            authorDisplayName = "Elena Rostova",
            authorRole = "REVIEWER",
            assigneeType = GranteeType.TEAM,
            assigneeId = teamCoreInfra.id,
            assigneeName = teamCoreInfra.name,
            linkedArtifactId = artifact1.id,
            linkedArtifactTitle = artifact1.title,
            labels = "bug,sla,incident-response",
        )

        // Parent Epic Issue in Repo 1
        val issue2 = RepoIssue(
            id = "iss_dual_approval_audit_enforcement",
            repoId = repo1.id,
            issueNumber = 2,
            title = "Enterprise Multi-Signature & Hardware Key Attestation Rollout",
            description = "Per Acme Enterprise Security Directive 2026-Q1, rollout multi-signature cryptographic gates across tier-1 infrastructure repositories with hardware token verification.",
            status = IssueStatus.IN_PROGRESS,
            priority = IssuePriority.CRITICAL,
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            authorRole = "APPROVER",
            assigneeType = GranteeType.TEAM,
            assigneeId = teamCoreInfra.id,
            assigneeName = teamCoreInfra.name,
            linkedArtifactId = artifact1.id,
            linkedArtifactTitle = artifact1.title,
            labels = "compliance,security,dual-approval,epic",
        )

        // Sub-Issue 1 of Issue 2
        val issue4 = RepoIssue(
            id = "iss_sub_fido2_schema",
            repoId = repo1.id,
            issueNumber = 3,
            title = "Enforce FIDO2 Hardware Key Attestation Policy in Schema",
            description = "Verify WebAuthn / FIDO2 security keys during approver signature phase.",
            status = IssueStatus.CLOSED,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_david_kim",
            authorDisplayName = "David Kim",
            authorRole = "COLLABORATOR",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_david_kim",
            assigneeName = "David Kim",
            parentIssueId = issue2.id,
            parentIssueNumber = issue2.issueNumber,
            parentIssueTitle = issue2.title,
            closedAt = System.currentTimeMillis() - 86400000L,
            closedByUserId = "usr_sarah_chen",
            closedByDisplayName = "Sarah Chen",
            labels = "fido2,security,sub-issue",
        )

        // Sub-Issue 2 of Issue 2
        val issue5 = RepoIssue(
            id = "iss_sub_quorum_gate",
            repoId = repo1.id,
            issueNumber = 4,
            title = "Automated Cryptographic Quorum Signature Gate",
            description = "Implement multi-signature quorum collection checking minimum required approver threshold.",
            status = IssueStatus.IN_PROGRESS,
            priority = IssuePriority.CRITICAL,
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            authorRole = "APPROVER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_marcus_vance",
            assigneeName = "Marcus Vance",
            parentIssueId = issue2.id,
            parentIssueNumber = issue2.issueNumber,
            parentIssueTitle = issue2.title,
            labels = "cryptography,quorum,sub-issue",
        )

        // Sub-Issue 3 of Issue 2
        val issue6 = RepoIssue(
            id = "iss_sub_regression_suite",
            repoId = repo1.id,
            issueNumber = 5,
            title = "Policy Regression & Segregation of Duties Automated Verification",
            description = "Construct comprehensive test vectors verifying anti-self-approval and role-escalation constraints.",
            status = IssueStatus.OPEN,
            priority = IssuePriority.MEDIUM,
            authorUserId = "usr_sarah_chen",
            authorDisplayName = "Sarah Chen",
            authorRole = "MAINTAINER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_elena_rostova",
            assigneeName = "Elena Rostova",
            parentIssueId = issue2.id,
            parentIssueNumber = issue2.issueNumber,
            parentIssueTitle = issue2.title,
            labels = "testing,policy,sub-issue",
        )

        // Independent Issue in Repo 1 that is BLOCKED
        val issue7 = RepoIssue(
            id = "iss_hotfix_bypass_protocol",
            repoId = repo1.id,
            issueNumber = 6,
            title = "Production Hotfix Bypass Protocol Authorization Schema",
            description = "Define emergency bypass specifications allowing expedited changes under severity-1 incidents.",
            status = IssueStatus.OPEN,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_david_kim",
            authorDisplayName = "David Kim",
            authorRole = "COLLABORATOR",
            assigneeType = GranteeType.TEAM,
            assigneeId = teamSecurity.id,
            assigneeName = teamSecurity.name,
            labels = "emergency,hotfix,governance",
        )

        // Repo 2 Parent Issue
        val issue3 = RepoIssue(
            id = "iss_aml_sanctions_lookup_refresh",
            repoId = repo2.id,
            issueNumber = 1,
            title = "AML & Sanctions Screening Automated Policy Refresh",
            description = "Confirm if the KYC schema allows automated webhook refresh of OFAC list or if manual approval override is required for high risk jurisdictions.",
            status = IssueStatus.OPEN,
            priority = IssuePriority.MEDIUM,
            authorUserId = "usr_sarah_chen",
            authorDisplayName = "Sarah Chen",
            authorRole = "MAINTAINER",
            assigneeType = GranteeType.TEAM,
            assigneeId = teamSecurity.id,
            assigneeName = teamSecurity.name,
            linkedArtifactId = artifact3.id,
            linkedArtifactTitle = artifact3.title,
            labels = "aml,kyc,regulatory,epic",
        )

        // Repo 2 Sub-issues
        val issue8 = RepoIssue(
            id = "iss_sub_ofac_worker",
            repoId = repo2.id,
            issueNumber = 2,
            title = "Real-Time OFAC & PEP List Cache Invalidation Worker",
            description = "Setup hourly delta sync with sanction authority webhooks.",
            status = IssueStatus.CLOSED,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_elena_rostova",
            authorDisplayName = "Elena Rostova",
            authorRole = "REVIEWER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_elena_rostova",
            assigneeName = "Elena Rostova",
            parentIssueId = issue3.id,
            parentIssueNumber = issue3.issueNumber,
            parentIssueTitle = issue3.title,
            closedAt = System.currentTimeMillis() - 172800000L,
            closedByUserId = "usr_marcus_vance",
            closedByDisplayName = "Marcus Vance",
            labels = "sanctions,ofac,sub-issue",
        )

        val issue9 = RepoIssue(
            id = "iss_sub_jurisdiction_override",
            repoId = repo2.id,
            issueNumber = 3,
            title = "High-Risk Jurisdiction Manual Approval Override Rule",
            description = "Add branch conditions requiring FinTech Compliance Officer manual signature for Tier-3 countries.",
            status = IssueStatus.OPEN,
            priority = IssuePriority.MEDIUM,
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            authorRole = "OWNER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_marcus_vance",
            assigneeName = "Marcus Vance",
            parentIssueId = issue3.id,
            parentIssueNumber = issue3.issueNumber,
            parentIssueTitle = issue3.title,
            labels = "compliance,override,sub-issue",
        )

        // Photo Items (Manufacturing Line Optimization)
        val issue128 = RepoIssue(
            id = "iss_128_sinking_opt",
            repoId = repoMfg.id,
            issueNumber = 128,
            title = "基座沉降問題優化",
            description = "產線機台基座出現微幅沉降趨勢，需進行現場高程精準量測、成因分析與結構補強優化方案。",
            status = IssueStatus.IN_PROGRESS,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_wang",
            authorDisplayName = "王小明",
            authorRole = "COLLABORATOR",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_wang",
            assigneeName = "王小明",
            parentIssueTitle = "WBS-3.2 原因分析",
            labels = "製造,沉降,優化,WBS-3.2",
        )

        val issue110 = RepoIssue(
            id = "iss_110_site_check",
            repoId = repoMfg.id,
            issueNumber = 110,
            title = "現場檢查完成",
            description = "第 3 號產線基礎現場檢查與水平儀校準已完成。",
            status = IssueStatus.CLOSED,
            priority = IssuePriority.MEDIUM,
            authorUserId = "usr_li",
            authorDisplayName = "李佳穎",
            authorRole = "REVIEWER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_li",
            assigneeName = "李佳穎",
            closedAt = System.currentTimeMillis() - 86400000L,
            closedByUserId = "usr_li",
            closedByDisplayName = "李佳穎",
            labels = "現場,檢查,已驗證",
        )

        val issue131 = RepoIssue(
            id = "iss_131_design_opt",
            repoId = repoMfg.id,
            issueNumber = 131,
            title = "優化方案設計",
            description = "根據沉降測量數據與結構應力分析提出加固設計圖面與工法。",
            status = IssueStatus.OPEN,
            priority = IssuePriority.HIGH,
            authorUserId = "usr_zhang",
            authorDisplayName = "張小華",
            authorRole = "MAINTAINER",
            assigneeType = GranteeType.USER,
            assigneeId = "usr_wang",
            assigneeName = "王小明",
            labels = "設計,結構,加固",
        )

        dao.insertIssues(listOf(issue128, issue110, issue131, issue1, issue2, issue3, issue4, issue5, issue6, issue7, issue8, issue9))

        // 11b. ISSUE DEPENDENCIES (BLOCKED-BY / BLOCKING RELATIONSHIPS)
        val dependencies = listOf(
            // In Repo 1: Issue #6 (Hotfix protocol) is BLOCKED BY Issue #4 (Quorum Signature Gate)
            IssueDependency(
                id = "dep_hotfix_blocked_by_quorum",
                repoId = repo1.id,
                blockedIssueId = issue7.id,
                blockingIssueId = issue5.id,
                dependencyType = DependencyType.BLOCKS,
                createdByUserId = "usr_marcus_vance",
                createdByDisplayName = "Marcus Vance",
            ),
            // In Repo 1: Issue #1 (Incident SLA timeout) is BLOCKED BY Issue #5 (Regression Suite)
            IssueDependency(
                id = "dep_sla_blocked_by_regression",
                repoId = repo1.id,
                blockedIssueId = issue1.id,
                blockingIssueId = issue6.id,
                dependencyType = DependencyType.BLOCKS,
                createdByUserId = "usr_sarah_chen",
                createdByDisplayName = "Sarah Chen",
            ),
            // In Repo 2: Issue #3 (Jurisdiction Override) is BLOCKED BY Issue #2 (OFAC Worker)
            IssueDependency(
                id = "dep_override_blocked_by_ofac",
                repoId = repo2.id,
                blockedIssueId = issue9.id,
                blockingIssueId = issue8.id,
                dependencyType = DependencyType.BLOCKS,
                createdByUserId = "usr_marcus_vance",
                createdByDisplayName = "Marcus Vance",
            ),
        )
        dao.insertIssueDependencies(dependencies)

        // 12. ISSUE COMMENTS
        val issueComment1 = IssueComment(
            issueId = issue1.id,
            authorUserId = "usr_david_kim",
            authorDisplayName = "David Kim",
            authorRole = "COLLABORATOR",
            content = "Good catch Elena. I've updated the draft state machine to include a 15m timer branch that triggers the PagerDuty fallback webhook.",
            createdAt = System.currentTimeMillis() - 86400000L,
        )
        val issueComment2 = IssueComment(
            issueId = issue1.id,
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            authorRole = "APPROVER",
            content = "Security team confirms this satisfies the SLA failover policy. Once verified, we will sign off.",
            createdAt = System.currentTimeMillis() - 43200000L,
        )
        dao.insertIssueComments(listOf(issueComment1, issueComment2))

        // 13. REPO DISCUSSIONS
        val disc1 = RepoDiscussion(
            id = "disc_rfc_declarative_canvas",
            repoId = repo1.id,
            discussionNumber = 1,
            title = "RFC: Standardizing No-Code State Machine Specification Syntax",
            category = DiscussionCategory.RFC_PROPOSALS,
            body = "Proposing a unified JSON/YAML schema representation for visual workflows across Acme Enterprise. This allows automated static analysis of segregation-of-duties gates before submittal.",
            authorUserId = "usr_sarah_chen",
            authorDisplayName = "Sarah Chen",
            authorRole = "MAINTAINER",
            isLocked = false,
            isAnswered = true,
            acceptedAnswerCommentId = "dc_answer_schema_v2",
            upvoteCount = 8,
        )
        val disc2 = RepoDiscussion(
            id = "disc_qna_reviewer_roles",
            repoId = repo1.id,
            discussionNumber = 2,
            title = "Q&A: How do Team Grants interact with individual user roles?",
            category = DiscussionCategory.Q_AND_A,
            body = "If a user has COLLABORATOR via Org membership, but belongs to a Team granted REVIEWER on this repo, which role takes precedence during policy evaluation?",
            authorUserId = "usr_david_kim",
            authorDisplayName = "David Kim",
            authorRole = "COLLABORATOR",
            isLocked = false,
            isAnswered = true,
            acceptedAnswerCommentId = "dc_answer_policy_precedence",
            upvoteCount = 5,
        )
        val disc3 = RepoDiscussion(
            id = "disc_announcement_lockdown",
            repoId = repo2.id,
            discussionNumber = 1,
            title = "Announcement: End of Quarter Freeze for Payment Schemas",
            category = DiscussionCategory.ANNOUNCEMENTS,
            body = "All RFC modifications to payment decision records will require Enterprise Admin emergency sign-off from March 25th through April 2nd.",
            authorUserId = "usr_marcus_vance",
            authorDisplayName = "Marcus Vance",
            authorRole = "OWNER",
            isLocked = true,
            isAnswered = false,
            upvoteCount = 12,
        )
        dao.insertDiscussions(listOf(disc1, disc2, disc3))

        // 14. DISCUSSION COMMENTS
        val discComment1 = DiscussionComment(
            id = "dc_answer_schema_v2",
            discussionId = disc1.id,
            authorUserId = "usr_elena_rostova",
            authorDisplayName = "Elena Rostova",
            authorRole = "REVIEWER",
            content = "We have benchmarked the declarative state schema against v2.0 specs. It fully supports validation hooks for dual-approval constraints.",
            isAcceptedAnswer = true,
            upvotes = 6,
            createdAt = System.currentTimeMillis() - 72000000L,
        )
        val discComment2 = DiscussionComment(
            id = "dc_answer_policy_precedence",
            discussionId = disc2.id,
            authorUserId = "usr_sarah_chen",
            authorDisplayName = "Sarah Chen",
            authorRole = "MAINTAINER",
            content = "The Hierarchical Policy Engine resolves the highest rank among Direct Grants, Team Grants, and Org Defaults. Therefore, your Team's REVIEWER grant elevates your effective permission over the base Org Member COLLABORATOR default!",
            isAcceptedAnswer = true,
            upvotes = 9,
            createdAt = System.currentTimeMillis() - 36000000L,
        )
        dao.insertDiscussionComments(listOf(discComment1, discComment2))

        // 15. AUDIT LOGS
        val auditLogs = listOf(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                repoId = repo1.id,
                repoName = repo1.name,
                actorUserId = "usr_david_kim",
                actorDisplayName = "David Kim",
                actionName = "SUBMIT_FOR_REVIEW",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Collaborator David Kim submitted artifact 'High-Severity Automated Incident Escalation Flow' for peer review.",
            ),
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                repoId = repo1.id,
                repoName = repo1.name,
                actorUserId = "usr_elena_rostova",
                actorDisplayName = "Elena Rostova",
                actionName = "SUBMIT_REVIEW",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Reviewer Elena Rostova completed peer review with decision APPROVED.",
            ),
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                repoId = repo1.id,
                repoName = repo1.name,
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actionName = "SUBMIT_FINAL_APPROVAL",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Org Owner Sarah Chen granted 1st Approver signature (1 of 2 required signatures).",
            ),
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = orgFintech.id,
                repoId = repo2.id,
                repoName = repo2.name,
                actorUserId = "usr_marcus_vance",
                actorDisplayName = "Marcus Vance",
                actionName = "PUBLISH_AND_LOCK",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Artifact 'Tier-3 Corporate Customer KYC Onboarding Form Schema' locked and published following 2 distinct approver signatures.",
            ),
        )
        dao.insertAuditLogs(auditLogs)

        // 16. UNIFIED INBOX NOTIFICATIONS
        val now = System.currentTimeMillis()
        val notifications = listOf(
            // --- SARAH CHEN (Org Owner & Enterprise Admin) ---
            AppNotification(
                id = "notif_sarah_approval_1",
                recipientUserId = "usr_sarah_chen",
                actorUserId = "usr_david_kim",
                actorDisplayName = "David Kim",
                actorAvatarColorHex = "#10B981",
                category = NotificationCategory.APPROVAL_GATE,
                priority = NotificationPriority.URGENT,
                status = NotificationStatus.UNREAD,
                title = "Approval Gate: High-Severity Automated Incident Escalation Flow",
                body = "Peer review was approved by Elena Rostova. Your cryptographic sign-off is required as 1st Approver under Acme Dual-Approval Policy.",
                isActionable = true,
                actionType = "APPROVE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                artifactId = artifact1.id,
                artifactTitle = artifact1.title,
                createdAt = now - 3600000L,
            ),
            AppNotification(
                id = "notif_sarah_issue_1",
                recipientUserId = "usr_sarah_chen",
                actorUserId = "usr_marcus_vance",
                actorDisplayName = "Marcus Vance",
                actorAvatarColorHex = "#EC4899",
                category = NotificationCategory.ISSUE_ASSIGNMENT,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "Issue Assignment: Dual-approval gate failing on hotfix pipeline",
                body = "Marcus Vance assigned Issue #2 to you and the Core Infrastructure Platform Team for immediate policy reconciliation.",
                isActionable = true,
                actionType = "VIEW_ISSUE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                teamId = teamCoreInfra.id,
                teamName = teamCoreInfra.name,
                repoId = repo1.id,
                repoName = repo1.name,
                issueId = issue2.id,
                issueTitle = issue2.title,
                createdAt = now - 7200000L,
            ),
            AppNotification(
                id = "notif_sarah_disc_1",
                recipientUserId = "usr_sarah_chen",
                actorUserId = "usr_maya_lin",
                actorDisplayName = "Maya Lin",
                actorAvatarColorHex = "#F59E0B",
                category = NotificationCategory.MENTION_AND_REPLY,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.READ,
                title = "Mention in RFC Discussion #2",
                body = "Maya Lin mentioned you: '@sarah_chen can you verify the precedence order when team grants conflict with direct grants?'",
                isActionable = true,
                actionType = "VIEW_DISCUSSION",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                discussionId = disc2.id,
                discussionTitle = disc2.title,
                createdAt = now - 86400000L,
                readAt = now - 3600000L,
            ),

            // --- MARCUS VANCE (Security Approver & Fintech Owner) ---
            AppNotification(
                id = "notif_marcus_approval_2",
                recipientUserId = "usr_marcus_vance",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.APPROVAL_GATE,
                priority = NotificationPriority.URGENT,
                status = NotificationStatus.UNREAD,
                title = "Dual-Approver Gate: Incident Escalation Flow (Signature 2 of 2)",
                body = "Sarah Chen has granted the 1st signature. As designated Security Approver, your 2nd signature will unlock artifact publication.",
                isActionable = true,
                actionType = "APPROVE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                artifactId = artifact1.id,
                artifactTitle = artifact1.title,
                createdAt = now - 1800000L,
            ),
            AppNotification(
                id = "notif_marcus_gov_alert",
                recipientUserId = "usr_marcus_vance",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "System Governance Engine",
                actorAvatarColorHex = "#6366F1",
                category = NotificationCategory.GOVERNANCE_EVENT,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "Governance Alert: End of Quarter Freeze Enacted",
                body = "Payment decision record schemas locked for change window. RFC modifications now require Enterprise Admin emergency sign-off.",
                isActionable = true,
                actionType = "VIEW_REPO",
                enterpriseId = enterprise.id,
                orgId = orgFintech.id,
                orgName = orgFintech.name,
                repoId = repo2.id,
                repoName = repo2.name,
                createdAt = now - 14400000L,
            ),
            AppNotification(
                id = "notif_marcus_pub",
                recipientUserId = "usr_marcus_vance",
                actorUserId = "usr_marcus_vance",
                actorDisplayName = "Marcus Vance",
                actorAvatarColorHex = "#EC4899",
                category = NotificationCategory.PUBLICATION,
                priority = NotificationPriority.LOW,
                status = NotificationStatus.READ,
                title = "Publication Milestone: KYC Onboarding Schema Published",
                body = "Artifact 'Tier-3 Corporate Customer KYC Onboarding Form Schema' has been locked and released to production catalog.",
                isActionable = false,
                enterpriseId = enterprise.id,
                orgId = orgFintech.id,
                orgName = orgFintech.name,
                repoId = repo2.id,
                repoName = repo2.name,
                artifactId = artifact3.id,
                artifactTitle = artifact3.title,
                createdAt = now - 172800000L,
                readAt = now - 86400000L,
            ),

            // --- ELENA ROSTOVA (Senior Governance Reviewer) ---
            AppNotification(
                id = "notif_elena_review_req",
                recipientUserId = "usr_elena_rostova",
                actorUserId = "usr_david_kim",
                actorDisplayName = "David Kim",
                actorAvatarColorHex = "#10B981",
                category = NotificationCategory.REVIEW_REQUEST,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "Review Request: Multi-AZ Failover State Machine Spec",
                body = "David Kim requested your formal review on blueprint 'Multi-AZ Failover State Machine Spec' before submitting to approval gate.",
                isActionable = true,
                actionType = "REVIEW",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                artifactId = artifact2.id,
                artifactTitle = artifact2.title,
                createdAt = now - 900000L,
            ),
            AppNotification(
                id = "notif_elena_membership",
                recipientUserId = "usr_elena_rostova",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.MEMBERSHIP_CHANGE,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.UNREAD,
                title = "Team Role Update: Promoted to Maintainer",
                body = "Sarah Chen promoted your role to MAINTAINER in Team 'Core Infrastructure Platform Team'.",
                isActionable = true,
                actionType = "VIEW_TEAM",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                teamId = teamCoreInfra.id,
                teamName = teamCoreInfra.name,
                createdAt = now - 43200000L,
            ),
            AppNotification(
                id = "notif_elena_access",
                recipientUserId = "usr_elena_rostova",
                actorUserId = "usr_marcus_vance",
                actorDisplayName = "Marcus Vance",
                actorAvatarColorHex = "#EC4899",
                category = NotificationCategory.ACCESS_CHANGE,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.READ,
                title = "Repository Access Granted: REVIEWER on Payment Decision Matrix",
                body = "You were granted direct REVIEWER access rule on repository 'payment-decision-matrix'.",
                isActionable = true,
                actionType = "VIEW_REPO",
                enterpriseId = enterprise.id,
                orgId = orgFintech.id,
                orgName = orgFintech.name,
                repoId = repo2.id,
                repoName = repo2.name,
                createdAt = now - 259200000L,
                readAt = now - 86400000L,
            ),

            // --- DAVID KIM (Workflow Builder) ---
            AppNotification(
                id = "notif_david_review_approved",
                recipientUserId = "usr_david_kim",
                actorUserId = "usr_elena_rostova",
                actorDisplayName = "Elena Rostova",
                actorAvatarColorHex = "#3B82F6",
                category = NotificationCategory.REVIEW_REQUEST,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "Review Completed: Approved by Elena Rostova",
                body = "Elena Rostova approved your artifact 'High-Severity Automated Incident Escalation Flow': 'Meets all declarative schema validation requirements.'",
                isActionable = true,
                actionType = "VIEW_ARTIFACT",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                artifactId = artifact1.id,
                artifactTitle = artifact1.title,
                createdAt = now - 7200000L,
            ),
            AppNotification(
                id = "notif_david_issue_assigned",
                recipientUserId = "usr_david_kim",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.ISSUE_ASSIGNMENT,
                priority = NotificationPriority.URGENT,
                status = NotificationStatus.UNREAD,
                title = "Assigned to Issue #1: Segregation of duties check failing",
                body = "Sarah Chen assigned Issue #1 to you. Pipeline deployer is failing policy check when creator attempts self-approval.",
                isActionable = true,
                actionType = "VIEW_ISSUE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                issueId = issue1.id,
                issueTitle = issue1.title,
                createdAt = now - 18000000L,
            ),
            AppNotification(
                id = "notif_david_disc_reply",
                recipientUserId = "usr_david_kim",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.MENTION_AND_REPLY,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.READ,
                title = "Accepted Answer in Discussion #2",
                body = "Sarah Chen's answer was marked as accepted in discussion 'Hierarchical Policy Engine Precedence Explained'.",
                isActionable = true,
                actionType = "VIEW_DISCUSSION",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                discussionId = disc2.id,
                discussionTitle = disc2.title,
                createdAt = now - 50000000L,
                readAt = now - 20000000L,
            ),

            // --- MAYA LIN (Cloud Architect) ---
            AppNotification(
                id = "notif_maya_access_grant",
                recipientUserId = "usr_maya_lin",
                actorUserId = "usr_sarah_chen",
                actorDisplayName = "Sarah Chen",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.ACCESS_CHANGE,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "Collaborator Access Granted: APPROVER on cloud-infra-core",
                body = "Sarah Chen granted you direct APPROVER permissions on repository 'cloud-infra-core'.",
                isActionable = true,
                actionType = "VIEW_REPO",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                createdAt = now - 21600000L,
            ),
            AppNotification(
                id = "notif_maya_disc_reply",
                recipientUserId = "usr_maya_lin",
                actorUserId = "usr_elena_rostova",
                actorDisplayName = "Elena Rostova",
                actorAvatarColorHex = "#3B82F6",
                category = NotificationCategory.MENTION_AND_REPLY,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.UNREAD,
                title = "Reply on RFC: State schema v2 compatibility",
                body = "Elena Rostova replied to your RFC proposal in 'State schema v2 compatibility': 'We have benchmarked declarative schema against v2.0 specs.'",
                isActionable = true,
                actionType = "VIEW_DISCUSSION",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repo1.id,
                repoName = repo1.name,
                discussionId = disc1.id,
                discussionTitle = disc1.title,
                createdAt = now - 10800000L,
            ),
        )

        // 9. WORK EVIDENCE, VERIFICATION & CHECKLISTS
        val mainIssueId = issue128.id
        dao.insertTaskChecklist(
            com.example.data.model.TaskChecklist(
                id = "chk_001",
                issueId = mainIssueId,
                title = "現場測量與數據收集",
                isCompleted = true,
                completedByUserId = "usr_wang",
                completedByDisplayName = "王小明",
                completedAt = System.currentTimeMillis() - 86400000 * 5,
            ),
        )
        dao.insertTaskChecklist(
            com.example.data.model.TaskChecklist(
                id = "chk_002",
                issueId = mainIssueId,
                title = "根因分析",
                isCompleted = true,
                completedByUserId = "usr_li",
                completedByDisplayName = "李佳穎",
                completedAt = System.currentTimeMillis() - 86400000 * 3,
            ),
        )
        dao.insertTaskChecklist(
            com.example.data.model.TaskChecklist(
                id = "chk_003",
                issueId = mainIssueId,
                title = "提出優化方案",
                isCompleted = false,
            ),
        )

        val evd1 = com.example.data.model.WorkEvidence(
            id = "evd_001",
            issueId = mainIssueId,
            submitterUserId = "usr_wang",
            submitterDisplayName = "王小明",
            description = "現場高程相片與基座沉降測量報告",
            status = "PENDING",
        )
        dao.insertWorkEvidence(evd1)

        val photoNotifications = listOf(
            AppNotification(
                id = "notif_wang_evidence_wait_verify",
                recipientUserId = "usr_wang",
                actorUserId = "usr_wang",
                actorDisplayName = "王小明",
                actorAvatarColorHex = "#8B5CF6",
                category = NotificationCategory.APPROVAL_GATE,
                priority = NotificationPriority.URGENT,
                status = NotificationStatus.UNREAD,
                title = "等待驗證: #128 基座沉降問題優化",
                body = "王小明 提交了 Evidence",
                isActionable = true,
                actionType = "VERIFY",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repoMfg.id,
                repoName = repoMfg.name,
                issueId = issue128.id,
                issueTitle = issue128.title,
                createdAt = now - 1800000L,
            ),
            AppNotification(
                id = "notif_zhang_assign_step",
                recipientUserId = "usr_wang",
                actorUserId = "usr_zhang",
                actorDisplayName = "張小華",
                actorAvatarColorHex = "#06B6D4",
                category = NotificationCategory.ISSUE_ASSIGNMENT,
                priority = NotificationPriority.HIGH,
                status = NotificationStatus.UNREAD,
                title = "需要你處理: #131 優化方案設計",
                body = "張小華 指定你執行下一步",
                isActionable = true,
                actionType = "VIEW_ISSUE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repoMfg.id,
                repoName = repoMfg.name,
                issueId = issue131.id,
                issueTitle = issue131.title,
                createdAt = now - 5400000L,
            ),
            AppNotification(
                id = "notif_li_mention_disc",
                recipientUserId = "usr_wang",
                actorUserId = "usr_li",
                actorDisplayName = "李佳穎",
                actorAvatarColorHex = "#10B981",
                category = NotificationCategory.MENTION_AND_REPLY,
                priority = NotificationPriority.NORMAL,
                status = NotificationStatus.UNREAD,
                title = "需要回覆: 討論: #128 優化方案",
                body = "李佳穎 @你",
                isActionable = true,
                actionType = "VIEW_DISCUSSION",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repoMfg.id,
                repoName = repoMfg.name,
                createdAt = now - 7200000L,
            ),
            AppNotification(
                id = "notif_sys_check_done",
                recipientUserId = "usr_wang",
                actorUserId = "usr_li",
                actorDisplayName = "系統提醒",
                actorAvatarColorHex = "#F59E0B",
                category = NotificationCategory.GOVERNANCE_EVENT,
                priority = NotificationPriority.LOW,
                status = NotificationStatus.READ,
                title = "系統提醒: #110 現場檢查完成",
                body = "已完成，等待驗證",
                isActionable = true,
                actionType = "VIEW_ISSUE",
                enterpriseId = enterprise.id,
                orgId = orgCloud.id,
                orgName = orgCloud.name,
                repoId = repoMfg.id,
                repoName = repoMfg.name,
                issueId = issue110.id,
                issueTitle = issue110.title,
                createdAt = now - 86400000L,
                readAt = now - 43200000L,
            ),
        )

        dao.insertNotifications(photoNotifications + notifications)
    }
}
