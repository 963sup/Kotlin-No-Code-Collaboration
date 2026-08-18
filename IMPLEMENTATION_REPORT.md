# First-Principles Collaboration Implementation Report

## Decision framework

This branch applies three constraints:

1. **First Principles Thinking** — work, permissions, evidence, navigation, and synchronization remain the primary truths.
2. **Occam's Razor** — no parallel WBS task model, no persisted dashboard snapshots, no separate favorite table per object type, and no mutable achievement counters.
3. **80/20** — implement the shared primitives that unlock the eight product surfaces before adding decorative or backend-specific complexity.

## Implemented foundations

### 1. Mobile shell and theme

- Four primary destinations: Home, Inbox, Kanban/My Work, Explore.
- Scope, search, and profile remain top-level actions.
- A real Material 3 light blue-and-white scheme is paired with an optional dark scheme.

### 2. Scope-aware Home

- `ScopeDashboardProjector` produces Enterprise, Organization, Team, or User summaries from operational records.
- No dashboard entity or copied metric state is introduced.

### 3. Issue-backed WBS

- `WbsIssueProjection` adds only sibling order, planned dates, weight, and direct progress metadata around an existing Issue ID.
- `WbsProjection` generates stable numbering and weighted parent roll-up.
- No `WbsTask` aggregate or duplicate Kanban status exists.

### 4. Mobile My Work

- `MyWorkProjector` aggregates only issues in accessible repositories that are assigned to the active user or one of the active user's teams.
- Repository filtering remains a query concern, not duplicated persistence.

### 5. Canonical target navigation

- `CollaborationTarget` represents Repository, Issue, Artifact, Discussion, Organization, Team, Enterprise, and User destinations.
- `SafeTargetResolver` checks exact existence and authorization.
- Missing or denied child targets fail explicitly and never fall back to a broader repository or organization.

### 6. Explore and favorites

- `ExploreService` applies authorization before returning search results.
- `SavedTargetEntity` is one generic favorite relation for all supported target types.

### 7. Follow, feed, trending, achievements

- `UserFollowEntity` stores follow relationships.
- Feed, trending, XP, level, and achievements are projections of verified domain events.
- Governance audit events are always excluded from social projections.
- No mutable XP or popularity counter is trusted as source data.

### 8. Synchronization foundation

- Room remains the intended local UI source of truth.
- Added authenticated transport contracts, enterprise-scoped cursors, stable server versions, idempotent outbox records, bounded retry, conflict states, and untrusted push-target parsing.
- No credential, production endpoint, or fake successful backend is committed.
- `FirstPrinciplesSchema.migrationFrom` provides an explicit additive migration for saved targets, follows, outbox, and cursors; production registration must use the actual application database version and must remove destructive fallback.

## Security-sensitive invariants

- Every displayed or opened cross-object target must pass hierarchical permission checks.
- Client-side checks do not replace server-side authorization after a backend is connected.
- Push payloads are hints only and must be re-resolved and re-authorized.
- Search, My Work, favorites, feeds, trending, and achievements must never widen scope after denial.
- Audit-only events are not social content.
- Outbox idempotency keys are unique and cursors are enterprise-scoped.

## Tests

Focused unit tests cover:

- weighted WBS roll-up and numbering;
- fail-closed exact target resolution;
- authorization filtering before Explore display;
- exclusion of governance audit events from feed, trending, and achievements;
- repository-access and assignment requirements for My Work;
- bounded sync retry behavior.

GitHub Actions runs `testDebugUnitTest` and `assembleDebug` on this branch and pull requests to `main`.

## Required integration before production release

The core models and projections must be wired into the repository's existing screen/ViewModel/repository graph and the existing Room `@Database` entity list. The migration must be registered at the repository's real current schema version. A real remote adapter requires an authenticated backend contract and push provider configuration. These boundaries are intentionally explicit rather than simulated.
