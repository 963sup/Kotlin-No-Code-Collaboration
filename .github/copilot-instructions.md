# GitHub Copilot repository instructions

Read `/AGENTS.md` first. It is the authoritative product and architecture constitution for this repository.

Keep changes small and reuse the existing canonical model:

- `Repository` is the single no-code collaboration container.
- `RepoIssue` is the only persisted work record; WBS, Kanban and My Work are projections.
- Teams do not own repositories; access is represented by `RepoAccessRule`.
- `CollaborationTarget` is the canonical navigation target for Repository, Artifact, Issue, Discussion, Organization, Team and UserProfile.
- Do not introduce product semantics for source code, commits, branches, pull requests, diffs, CI/CD, terminals or developer environments.

For Kotlin or Gradle changes, prefer the smallest relevant verification. Before merging a meaningful product change, the standard check is:

```bash
gradle :app:detekt :app:testDebugUnitTest :app:assembleDebug
```

Detekt is advisory during the initial rollout. Do not create broad formatting/refactor churn only to eliminate historical findings. Konsist architecture tests are hard invariants and must remain green.
