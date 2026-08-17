# RepoGovernance - No-Code Collaboration Platform

A modern Android application engineered with **Kotlin**, **Jetpack Compose**, and **Room Database**, reverse-engineering GitHub's proven collaboration, governance, and permission models into an enterprise **No-Code Collaboration Platform**.

---

## 🏛️ First Principles & Core Architecture

### "Repository" as a No-Code Collaboration Container
In traditional systems, repositories are tied to source code and Git trees. **RepoGovernance** treats the Repository as a pure **governance and collaboration workspace**, stripping away code-specific mechanics (commits, branches, PRs, diffs, CI/CD) and preserving enterprise-grade collaboration semantics:

```
Enterprise / Organization
  └── Team
        └── Repository (Collaboration Container)
              ├── Issues & Nested Tasks (Status, Priority, Assignees, Dependencies)
              ├── Kanban Work Board (Repository-scoped view over Issues)
              ├── Discussions & Decisions (Threads, Categories, Comments)
              ├── Artifacts (Specifications, Documents, Workflows, Trackers)
              └── Governance & Policy Engine (RBAC/ABAC, Approvals, Audit)
```

The Kanban Board is intentionally a **view of Repository Issues**, not a separate Project ownership model. Nested Tasks reuse the recursive Issue parent relationship, avoiding duplicate task entities and unnecessary persistence layers.

---

## ✨ Key Features

- **Hierarchical Access Control Policy Engine**:
  - Evaluation of permissions across **Enterprise -> Organization -> Team -> Repository -> Resource** scopes.
  - Multi-tier role model: `Owner`, `Admin`, `Maintainer`, `Reviewer`, `Collaborator`, `Member`, `Approver`.
  - Real-time policy simulator and trace inspection dialog.
- **No-Code Repository Workspace**:
  - Structured issues management with status tracking, priority filters, assignees, dependencies, and recursive Nested Tasks.
  - Repository-scoped Kanban Board projecting the existing `Open -> In Progress -> Closed` Issue lifecycle without duplicating work data.
  - Discussions forum with threaded replies and category segmentation.
  - Artifact and document governance with review & approval workflows.
- **Audit & Compliance Logging**:
  - Comprehensive immutable event stream tracking actor actions, target entities, and policy evaluations.
- **Local-First Resilience**:
  - Full Room database backing with TypeConverters, indexed queries, and sample data bootstrapping.
- **Material 3 UI**:
  - Native dynamic color theming, high-contrast typography, edge-to-edge rendering, and adaptive layouts.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM / Unidirectional Data Flow |
| **Local Persistence** | Android Room Database & Coroutines Flow |
| **Testing** | Robolectric (JVM Unit Tests), Roborazzi (Screenshot Testing) |
| **Build Tooling** | Gradle Kotlin DSL (`build.gradle.kts`), Version Catalog (`libs.versions.toml`) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2+ or Google AI Studio
- JDK 17+
- Android SDK 35 (minSdk: 26, targetSdk: 35)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/963sup/Kotlin-No-Code-Collaboration.git
cd Kotlin-No-Code-Collaboration

# Run JVM Unit & Robolectric Tests
gradle :app:testDebugUnitTest

# Build Debug APK
gradle :app:assembleDebug
```

---

## 📄 License
Licensed under the [Apache 2.0 License](LICENSE).
