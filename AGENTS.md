# AGENTS.md - Project Rules & Architectural Directives

## 1. Project Constitution & Highest Directives

- **Core Definition**: This project is a **No-Code Collaboration Platform**, NOT a code-hosting or developer platform.
- **Container Semantics**: A "Repository" is an enterprise collaboration container for documents, issues, discussions, milestones, policies, and artifacts.
- **Strictly Excluded**:
  - No Git / Commits / Branches / Tags / Pull Requests / Diffs
  - No Source Code / Syntax Highlighting / File Editors
  - No CI/CD / Actions / Build Pipelines / Release Binaries
  - No Development Environment features (Codespaces / Terminal)

## 2. Technical Stack & Implementation Rules

- **Platform**: Android Native using Kotlin and Jetpack Compose.
- **Design System**: Strict adherence to Material Design 3 (M3), using dynamic/modern color schemes, proper spacing (8dp grid), minimum 48dp touch targets, and `Modifier.testTag` on all primary interactive elements.
- **Data Architecture**:
  - All entities and relationships persist in **Room Database** (`AppDatabase`, `GovernanceDao`).
  - State management uses `ViewModel` + `MutableStateFlow` / `collectAsStateWithLifecycle`.
  - Policy decisions must route through `HierarchicalPolicyEngine`.
- **Testing**:
  - Local JVM tests via Robolectric (`testDebugUnitTest`).
  - No instrumented emulator tests (`androidTest`) or direct ADB commands in cloud container.

## 3. Platform Identity Preservation

- `metadata.json` must remain synchronized with `app/src/main/res/values/strings.xml` (`app_name`).
- Do not remove `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` from `metadata.json`.
- Do not introduce `local.properties` (the build environment manages `ANDROID_SDK_ROOT`).
