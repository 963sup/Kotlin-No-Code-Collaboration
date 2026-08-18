# Mobile Collaboration MVP｜First Principles Decision Record

## Objective

Reduce the daily mobile path to the four highest-frequency outcomes: understand the current scope, receive work, execute work, and discover accessible collaboration containers.

## Facts

- Repository WBS already projects the existing recursive Issue hierarchy.
- Inbox already supports exact collaboration-target navigation.
- The bottom bar currently exposes only Home, Kanban, and Repositories.
- Inbox is duplicated in the top bar, while Repository search is presented as a generic search action.
- `LightColorScheme` is incorrectly created with `darkColorScheme`, and the app defaults to dark mode.
- Existing screens already provide Home, Inbox, Kanban, Repository discovery, Profile, and workspace scope switching.

## Assumptions

- The supplied product target uses a light operational canvas as the default experience.
- Repository discovery is sufficient for the first Explore slice; cross-entity search is a later objective.

## Invariants

- Repository remains a no-code collaboration container.
- No Git, source-code, CI/CD, terminal, or developer-platform product semantics are added.
- No ownership, permission, Room entity, or policy behavior changes.
- Profile and workspace scope remain top-level contextual actions rather than bottom destinations.
- Every primary navigation control retains a stable `Modifier.testTag`.

## Core Model

- **Entities:** existing User, Enterprise, Organization, Team, Repository, Issue, Notification, Artifact.
- **Relationships:** unchanged ownership, membership, access rules, assignments, and collaboration targets.
- **States:** selected top-level destination and selected workspace scope.
- **Events:** select Home, Inbox, Work, Explore, Profile, or workspace scope.
- **Responsibilities:** MainActivity owns shell navigation; existing screens own their bounded content; ViewModel remains the authoritative data path.

## Root Constraint

The product already contains the necessary capabilities, but the mobile information architecture and visual foundation do not expose them as one coherent daily work loop.

## Highest-Leverage Change

Reuse the existing screens and data flows, changing only the shell destination model, Explore wording, and design tokens instead of adding parallel screens or data models.

## Minimum Viable Change

1. Bottom navigation becomes Home, Inbox, Work, Explore.
2. Inbox unread count moves to the primary Inbox destination.
3. Profile and scope switching remain in the top app bar.
4. The existing Repository catalog becomes the first Explore surface.
5. The app uses a real Material 3 light color scheme by default.
6. Unit tests lock the destination order, labels, and test tags.

## Rejected Complexity

- No new Explore database, global search index, Favorite, Follow, Achievement, or synchronization model.
- No new top-level screen.
- No WBS or Issue persistence changes.
- No Room migration work in this slice.
- No speculative navigation framework refactor.

## Product Guardian

- **Verdict:** TRANSLATE
- **Objective:** expose collaboration work, attention, and discovery on mobile.
- **Canonical owner:** existing User and workspace scope.
- **Canonical container:** existing Repository access scope.
- **Allowed semantics:** Home, Inbox, work status, discovery, no-code Repository creation.
- **Excluded semantics:** code search, source files, branches, pull requests, or developer tooling.

## Verification Evidence

- `MainNavigationModelTest` proves the four destination order, Traditional Chinese labels, unique tags, and exclusion of Profile from the bottom bar.
- `gradle :app:testDebugUnitTest` passes.
- `gradle :app:assembleDebug` passes.
- Diff inspection confirms no persistence, ownership, or policy changes.

## Stop Condition

Stop when the four-destination shell, light-first palette, Explore wording, tests, and Android build pass. Cross-entity Explore and social features require separate decision records.
