## Objective

<!-- State the user or system outcome without describing only the implementation. -->

## Verified Facts and Root Constraint

<!-- What did the repository prove, and what upstream constraint prevented the objective? -->

## Minimum Viable Change

<!-- Describe the smallest coherent delta delivered by this PR. -->

## Rejected Complexity

<!-- List adjacent features, abstractions, screens, states, and refactors deliberately excluded. -->

## Core Model and Data Flow

<!-- Summarize affected entities, relationships, states, events, responsibilities, and the Room → Repository → ViewModel → Compose path when applicable. -->

## Verification Evidence

<!-- Record semantic assertions, behavior checks, invariant checks, and commands actually run. -->

```text
Objective Evidence:
Semantic Evidence:
Relationship Evidence:
Behavioral Evidence:
Governance Evidence:
Constitutional Evidence:
Technical Evidence:
Commands Run:
Residual Risk:
```

## First Principles Checklist

- [ ] Facts are separated from assumptions.
- [ ] The change addresses a root constraint rather than only a visible symptom.
- [ ] Existing valid mechanisms were reused before adding a new abstraction.
- [ ] The PR contains the smallest complete change required by the objective.
- [ ] No speculative future scope was added.
- [ ] `Repository` remains a no-code collaboration container.
- [ ] No Git, code-hosting, developer-environment, or CI/CD product semantics were introduced.
- [ ] Ownership, access, policy, and audit relationships remain valid.
- [ ] New primary mobile interactions have adequate touch targets and `Modifier.testTag` where applicable.
- [ ] The declared verification evidence passes.
- [ ] The stop condition has been reached; further work belongs in a separate change.
