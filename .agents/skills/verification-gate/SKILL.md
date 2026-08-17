---
name: verification-gate
description: Proves that a change satisfies its original objective while preserving product, relationship, governance, mobile, and technical invariants.
---

# Verification Gate

Use this skill after implementation and before declaring work complete.

Compilation alone is not sufficient evidence.

## 1. Verification Dimensions

### Semantic Evidence

- Does the delivered capability solve the stated objective?
- Is the product meaning no-code collaboration rather than developer tooling?
- Are names and states consistent with the approved core model?

### Relationship Evidence

- Is ownership assigned to the correct Enterprise, Organization, User, Team, or Repository scope?
- Are cross-entity links valid and traceable?
- Are orphan records, invalid cross-Repository relationships, and duplicate sources of truth prevented?

### Behavioral Evidence

- Does the primary mobile flow reach the intended result?
- Do failure, empty, unavailable, and permission-denied paths behave deliberately?
- Does navigation reach the exact target without unnecessary intermediate discovery?

### Governance Evidence

- Does authorization route through `HierarchicalPolicyEngine`?
- Are least privilege, separation of duties, independent verification, and audit logging preserved where applicable?
- Can a user bypass policy through an alternate UI path?

### Constitutional Evidence

- No Git, commit, branch, tag, pull request, diff, source editor, CI/CD, terminal, or IDE semantics were introduced.
- `Repository` remains a no-code collaboration container.
- No unnecessary top-level mobile navigation surface was added.

### Technical Evidence

- Room entities, DAO queries, converters, and database registration remain consistent when persistence changes.
- ViewModel and StateFlow remain the authoritative UI state path.
- New primary controls have `Modifier.testTag` and adequate touch targets.
- Relevant unit and Robolectric tests pass.

## 2. Minimum Commands

Run the smallest relevant checks available in the environment. For a normal application change, prefer:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

Do not use instrumented emulator tests or direct ADB commands in the cloud container.

For documentation-only changes, validate structure, references, YAML syntax, and consistency instead of running unrelated Android builds.

## 3. Result Contract

Return:

```text
Objective Evidence: PASS | FAIL
Semantic Evidence: PASS | FAIL
Relationship Evidence: PASS | FAIL | N/A
Behavioral Evidence: PASS | FAIL | N/A
Governance Evidence: PASS | FAIL | N/A
Constitutional Evidence: PASS | FAIL
Technical Evidence: PASS | FAIL | N/A
Commands Run:
Failures:
Residual Risk:
Final Verdict: PASS | FAIL
```

## 4. Failure Rule

A failed invariant or objective check blocks completion even when the project compiles.

Return a failure to the root constraint; do not mask it with additional scope.

## 5. Stop Rule

When all applicable evidence passes, stop. Additional polish requires a separate objective and decision gate.
