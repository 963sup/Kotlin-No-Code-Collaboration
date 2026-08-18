# AGENTS.md

## Product constitution
- This is an Android no-code collaboration platform.
- `Repository` means a collaboration container for Issues, Discussions, WBS/Kanban, artifacts, governance and audit.
- Never introduce product semantics for source code, Git commits/branches/tags, pull requests/diffs, CI/CD, terminals or developer environments.

## Technical invariants
- Kotlin + Jetpack Compose + Material 3.
- Persist entities and relationships in Room; use ViewModel/StateFlow for UI state.
- Route authorization and governance decisions through `HierarchicalPolicyEngine`.
- Keep primary mobile interactions touch-friendly and testable.
- Keep `metadata.json` name aligned with `app/src/main/res/values/strings.xml` and preserve `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.
- Do not add `local.properties`.

## Working scope
- Inspect and edit only the smallest relevant surface; reuse the existing model and avoid unrelated refactors or documentation.
- Preserve ownership, permissions, persistence and audit relationships; run the smallest relevant verification without blocking routine web edits on full-project CI.
