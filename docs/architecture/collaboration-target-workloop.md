# Collaboration Target Work Loop｜First Principles Decision Record

## Objective

Allow a mobile user to open the exact collaboration object referenced by an Inbox notification in one deliberate action, without silently landing on a broader or unrelated object.

## Facts

- `AppNotification` already carries `actionType` plus optional Repository, Artifact, Issue, Discussion, Organization, Team, and User references.
- The current Inbox implements target precedence directly inside UI callbacks.
- Issue and Discussion references currently degrade to broader Repository or Artifact navigation in several paths.
- Existing `IssueDetailDialog` and `DiscussionDetailDialog` already provide bounded mobile detail surfaces.
- Repository ownership, Organization membership, Team membership, direct grants, and enterprise identity already exist.
- `HierarchicalPolicyEngine` remains the policy authority for role-sensitive actions.

## Assumptions

- Notifications may become stale because their referenced object can be deleted or become inaccessible.
- The correct failure behavior is an explicit message; opening a broader object is not an acceptable substitute for an explicit target.
- Transient target selection belongs to the Inbox UI, while business records remain authoritative in ViewModel state.

## Invariants

- Repository remains a no-code collaboration container.
- No Git, source-code, branch, pull-request, CI/CD, terminal, or developer-platform product semantics are introduced.
- No Room entity, schema, migration, ownership, membership, permission, or policy semantics change.
- Repository-scoped child objects must match the Repository identifier carried by the target.
- A notification is marked action-complete only after its target is validated and opened.
- Missing, stale, cross-Repository, cross-enterprise, or unauthorized targets fail visibly.

## Core Model

- **Entities:** existing Notification, User, Repository, Artifact, Issue, Discussion, Organization, Team.
- **Relationship:** a notification references one most-specific collaboration destination.
- **State:** unresolved, opened, or rejected target.
- **Event:** user performs the notification action.
- **Responsibilities:** resolver derives target meaning; access guard validates Repository reachability; Inbox controls transient overlay state; ViewModel supplies data and mutations; policy engine resolves role-sensitive Discussion behavior.

## Root Constraint

Target meaning and fallback precedence are duplicated in Inbox callbacks, so the UI cannot consistently distinguish an exact target from a broader fallback.

## Highest-Leverage Change

Introduce one non-persistent `CollaborationTarget` algebra and resolver, then route every Inbox action through the same validation path.

## Minimum Viable Change

1. Add canonical targets for Repository, Artifact, Issue, Discussion, Organization, Team, and User Profile.
2. Resolve explicit actions without unsafe fallback; unknown actions use the most-specific complete reference.
3. Reuse existing ownership, membership, and access-rule facts to reject inaccessible Repository targets.
4. Open Issue and Discussion targets in existing detail dialogs.
5. Show a deliberate failure dialog for stale, malformed, cross-container, or unauthorized references.
6. Mark an actionable notification complete only after successful opening.
7. Add JVM tests for target resolution and Repository access boundaries.

## Rejected Complexity

- No Navigation Compose migration.
- No generic global router or persisted deep-link table.
- No new screen or bottom-navigation destination.
- No database migration.
- No remote sync, global search, Favorites, Follow, Achievement, or My Work redesign in this slice.
- No speculative target types beyond objects already referenced by `AppNotification`.

## Product Guardian

- **Verdict:** ACCEPT
- **Canonical owner:** recipient User.
- **Canonical container:** existing Repository for Repository-scoped targets; existing Organization, Team, or User for directory targets.
- **Mobile placement:** existing Inbox action and existing detail dialogs.
- **Governance impact:** reduces privilege bypass and prevents stale references from silently opening broader content.

## Context7 Decision

Official Compose guidance supports keeping transient selected-item or dialog visibility state in the parent composable and passing data/actions into a dialog. The implementation therefore keeps `openedTarget` and failure visibility in Inbox while business collections and mutations remain in ViewModel flows.

## Delivery Record

- **Files To Change:** `InboxScreen.kt`; new `navigation/CollaborationTarget.kt`; new resolver/access tests; this decision record.
- **Files Explicitly Not Changing:** Room entities, DAO, database version, Repository ownership, policy engine behavior, bottom navigation.
- **Existing Mechanisms Reused:** `AppNotification`, ViewModel StateFlow data, access rules, memberships, `IssueDetailDialog`, `DiscussionDetailDialog`, `HierarchicalPolicyEngine`.
- **New Model Elements:** non-persistent sealed `CollaborationTarget`, deterministic resolver, read-access guard.
- **Data Flow:** Inbox notification → resolver → existence/container/access validation → existing detail or navigation surface.
- **Policy Path:** Repository reachability from existing grants; Discussion effective role through `HierarchicalPolicyEngine`.
- **UI Entry Point:** existing Inbox action button and notification inspection dialog.

## Verification Evidence

- Explicit Issue, Discussion, Artifact, Repository, Team, Organization, and User targets resolve deterministically.
- Explicit incomplete targets return no destination and never degrade to a Repository.
- Blank identifiers are rejected.
- Personal ownership, Organization membership, direct grants, Team grants, enterprise-admin scope, unrelated access, and cross-enterprise denial are tested.
- `:app:testDebugUnitTest` passes.
- `:app:assembleDebug` passes.
- Android CI passes on the feature branch.
- Security diff review covers every changed source file and confirms no unauthorized cross-scope opening path.

## Stop Condition

Stop when every Inbox action uses the canonical target path, exact Issue and Discussion targets open in existing dialogs, invalid targets fail visibly, tests and build pass, and the security diff review reports no unresolved high-confidence vulnerability.
