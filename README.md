# Kotlin No-Code Collaboration

Android 原生的企業無程式碼協作平台。產品語意借鑑 GitHub 的 Repository、Issue、Discussion、Team、權限與協作模式，但明確排除程式碼、Commit、Branch、Pull Request、Diff、CI/CD 等開發者功能。

## 核心模型

```text
Enterprise
├─ Organization
├─ Team
├─ User
└─ Repository｜無程式碼協作容器
   ├─ Issue / 子 Issue / 依賴
   ├─ WBS / Kanban / My Work｜同一批 RepoIssue 的不同投影
   ├─ Discussion
   ├─ Artifact / 文件 / 決策 / 成果
   ├─ Review / Approval
   ├─ Access Rule / Policy
   └─ Audit
```

Repository 只由 Organization 或 User 擁有；Team 透過 Access Rule 取得權限。`RepoIssue` 是唯一持久化工作真相，不另外建立 WBS Task 或 Kanban Task。

## 目前已實作

- Home / Inbox / Work / Explore 四個手機主入口。
- Enterprise / Organization / Team / User 範圍切換與範圍化作業摘要。
- Repository WBS、Issue tree、跨 Repository My Work、Kanban。
- Canonical `CollaborationTarget`，用於通知、導覽、搜尋與收藏。
- Explore、SavedTarget、UserFollow、公開活動與衍生成就投影。
- `HierarchicalPolicyEngine` 權限治理、Review / Approval、Audit。
- Room local-first persistence，明確 4→5 migration 與資料保留測試。
- Outbox、版本、Cursor、Conflict、Retry、WorkManager 背景同步。
- Firebase Auth ID token、FCM sync hint 與 HTTPS-only remote sync 邊界。
- Material 3 亮色 / 暗色介面。

## 開發方式

本專案主要在 **GitHub** 與 **Google AI Studio** 開發。Repository 只保留正式產品程式碼、必要測試、架構決策與按需 Android Verification；一次性生成腳本、自修改 workflow、臨時觸發檔與重複代理規則不應留在主幹。

Google AI Studio 相關專案 metadata 保留於 `metadata.json`，`MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` 不移除。

## 技術棧

| 層 | 技術 |
|---|---|
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose / Material 3 |
| State | ViewModel + StateFlow |
| Local Data | Room |
| Background Sync | WorkManager |
| Network | Retrofit + OkHttp + Moshi |
| Identity / Push | Firebase Auth + Firebase Cloud Messaging |
| AI Integration | Google AI Studio project capability / server-side Gemini boundary |
| Tests | JVM / Robolectric / Roborazzi |
| Build | Gradle Kotlin DSL + Version Catalog |

Android 設定：`minSdk 24`、`targetSdk 36`、`compileSdk 36.1`。

## 驗證

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

`.github/workflows/android.yml` 只在 Pull Request 的 Android/Gradle 變更或手動觸發時執行，不阻塞直接推送 `main` 的 Web/AI Studio 快速路徑。Room schema 變更不得使用 destructive migration；同步端點必須為 HTTPS，伺服器端必須驗證 Firebase ID token 與實際授權。

## Release boundary

Android client 已具備 local-first 與 authenticated sync client 邊界，但正式多人環境仍需要一個符合 `docs/contracts/collaboration-sync-v1.md` 的後端服務。FCM payload 只作同步提示，不直接修改領域資料。

## License

Apache License 2.0。
