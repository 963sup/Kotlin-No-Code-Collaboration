# My Work Projection｜First Principles Decision Record

## Objective

Let a field contributor open Work and immediately see every directly assigned or team-assigned Issue across all accessible Repositories, without first guessing which Repository contains the work.

## Facts

- `RepoIssue` is the single persisted work record and already supports User or Team assignees.
- `TeamMembership` already links Users to Teams.
- `MainActivity` already limits the Work input to Issues from the selected accessible workspace scope.
- The current Work screen forces one Repository selection before showing Issues.
- Kanban and WBS already project the same Issue records.

## Assumptions

- Repository, User, and Team identifiers remain globally unique within the local data set.
- Existing scoped Repository selection continues to be the authorization boundary supplied to this screen.

## Invariants

- Repository remains a no-code collaboration container.
- `RepoIssue` remains the only source of work truth.
- No Room entity, migration, ownership, role, policy, or mutation path changes.
- Team assignments are included only through active User membership.
- WBS remains a projection of one Repository Issue tree.
- New primary interactions retain stable test tags and at least 48dp targets.

## Core Model

- **Entities:** User, TeamMembership, Repository, RepoIssue.
- **Relationships:** User-to-Team membership and Issue-to-User/Team assignment.
- **States:** My Work versus All Work; all Repositories versus one Repository; Kanban versus WBS.
- **Events:** change assignment scope, change Repository filter, change projection, update Issue status.
- **Responsibilities:** MainActivity supplies already scoped records; pure projection logic narrows them; Compose renders; the existing ViewModel mutation remains authoritative.

## Root Constraint

A mandatory Repository selection hides the cross-Repository assignment relationship that field contributors actually use to decide what to do next.

## Highest-Leverage Change

Make Repository an optional filter and default the existing Work entry point to a pure My Work projection over existing scoped Issues.

## Minimum Viable Change

1. Default Work to direct and active-Team assignments across all accessible Repositories.
2. Provide My Work and All Work filters.
3. Provide an optional all-Repositories or single-Repository filter.
4. Show the Repository name on cross-Repository Kanban cards.
5. Require one Repository before enabling WBS.
6. Unit-test assignment and Repository projection rules.

## Rejected Complexity

- No new task, project, work-item, assignment, or WBS entity.
- No new top-level navigation destination.
- No vertical-board redesign, search engine, favorite, social, achievement, sync, or remote API work.
- No policy-engine or persistence refactor.

## Product Guardian

- **Verdict:** ACCEPT
- **Objective:** reduce field interaction cost while preserving one work truth.
- **Canonical owner:** existing Repository owns Issues; User and Team are assignees only.
- **Canonical container:** cross-Repository User work projection with optional Repository filter.
- **Allowed semantics:** assignment, progress, WBS, status transitions, accessible work aggregation.
- **Excluded semantics:** code projects, branches, commits, pull requests, CI/CD product features.

## Delivery Record

- **Files To Change:** KanbanBoardScreen, one pure projection helper, one unit test, this decision record.
- **Files Explicitly Not Changing:** Room database, DAO, repository layer, policy engine, ViewModel mutation implementation, navigation model.
- **Existing Mechanisms Reused:** scoped Repositories and Issues, TeamMembership, Issue assignment, IssueHierarchyRules, existing status callback and StateFlow.
- **New Model Elements:** one non-persistent assignment-scope enum and one pure projection function.
- **Data Flow:** MainActivity scoped records → pure projection → optional Repository filter → Kanban/WBS view.
- **Policy Path:** unchanged; the screen receives only already scoped records and uses the existing status mutation path.
- **UI Entry Point:** existing Work bottom destination.
- **Verification Plan:** projection unit tests, all JVM tests, debug assembly, branch diff inspection.

## Verification Evidence

- Direct User assignments and active Team assignments appear in My Work.
- Unrelated User, unrelated Team, and unassigned Issues do not appear in My Work.
- All Work preserves the already scoped Issue set.
- Repository filtering narrows but never widens the set.
- `gradle :app:testDebugUnitTest` passes.
- `gradle :app:assembleDebug` passes.
- Diff inspection confirms no persistence, ownership, permission, or policy change.

## Stop Condition

Stop when cross-Repository My Work, optional Repository filtering, single-Repository WBS gating, unit tests, and Android verification pass. Any adjacent Explore, sync, social, or evidence feature requires a separate decision record.
