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

## Fast development path
1. Inspect only files relevant to the requested change.
2. Reuse the existing model before adding entities, screens, states or abstractions.
3. Make the smallest coherent change that satisfies the request.
4. Preserve ownership, permissions, persistence and audit relationships.
5. Run only the smallest relevant verification when available; do not block routine web edits on full-project CI.
6. Stop when the requested behavior is complete. Avoid speculative refactors and extra documentation.
