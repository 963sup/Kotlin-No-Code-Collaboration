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

## 4. Mandatory First Principles Development Gate

Apply this gate before any non-trivial change affecting product semantics, ownership, permissions, persistence, navigation, lifecycle state, or multiple files. Trivial isolated edits may bypass it.

Every gated change must establish:

1. **Objective** — the outcome being created, without assuming an implementation.
2. **Facts** — what is verified in the current repository.
3. **Assumptions** — what remains unverified.
4. **Invariants** — constitutional, architectural, and governance rules that cannot be broken.
5. **Core Model** — entities, relationships, states, events, and responsibilities involved.
6. **Root Constraint** — the smallest cause preventing the objective.
7. **Highest-Leverage Change** — the smallest intervention that removes the root constraint.
8. **Minimum Viable Change** — the minimum coherent implementation scope.
9. **Rejected Complexity** — explicitly excluded abstractions, screens, states, or features.
10. **Verification Evidence** — exact checks proving semantic and technical correctness.
11. **Stop Condition** — the condition after which implementation must stop.

Rules:

- Repository facts override assumptions and benchmark imitation.
- Reuse an existing valid mechanism before creating a new abstraction.
- Do not add an entity, state, screen, navigation layer, or policy unless the objective requires it.
- Minimum scope means the smallest **complete** change, not the fewest lines.
- When verification evidence passes, stop. Do not expand the feature speculatively.

## 5. Skill Routing

Use only skills that physically exist under `.agents/skills`.

For consequential product work, use the smallest sufficient route:

```text
first-principles-thinking
→ product-guardian
→ feature-delivery
→ verification-gate
```

Skip unnecessary skills for trivial work. Never reference imaginary or planned skills as if they already exist.
