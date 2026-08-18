# AGENTS.md

## Product constitution

- This is an Android no-code collaboration platform.
- `Repository` means a collaboration container for Issues, Discussions, WBS/Kanban, artifacts, governance and audit.
- Never introduce product semantics for source code, Git commits/branches/tags, pull requests/diffs, CI/CD, terminals or developer environments.

## Canonical model invariants

- `Repository` is the single no-code collaboration container.
- `RepoIssue` is the only persisted work record; WBS, Kanban and My Work are projections of the same Issues.
- Repository ownership belongs to an Organization or User. Teams receive repository access through `RepoAccessRule`; do not create a parallel workspace or Team-owned repository model.
- `CollaborationTarget` is the canonical navigation target for Repository, Artifact, Issue, Discussion, Organization, Team and UserProfile.
- Reuse existing entities, policy rules, audit records and projections before adding new persisted models.

## Technical invariants

- Kotlin + Jetpack Compose + Material 3.
- Persist entities and relationships in Room; use ViewModel/StateFlow for UI state.
- UI screens/components must not import persistence (`data.local`) or repository implementation (`data.repository`) directly; route data access through ViewModels.
- Route authorization and governance decisions through the existing policy/access-control layer; never infer access from stale local navigation or saved state alone.
- Keep primary mobile interactions touch-friendly and testable.
- Keep `metadata.json` name aligned with `app/src/main/res/values/strings.xml` and preserve `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.
- Do not add `local.properties`, credentials, secrets or generated build outputs.

## Working scope

- Inspect and edit only the smallest relevant surface; reuse the existing model and avoid unrelated refactors or documentation.
- Preserve ownership, permissions, persistence and audit relationships.
- Prefer one coherent change over many speculative abstractions.
- For meaningful Kotlin/Gradle changes, use `gradle :app:detekt :app:testDebugUnitTest :app:assembleDebug` before merge when practical.
- Detekt is advisory during the initial rollout; do not churn the codebase to erase historical style debt. Konsist architecture tests are hard gates.
- Direct GitHub / Google AI Studio edits may remain fast; relevant pushes to `main` are verified asynchronously by Android Verification.
