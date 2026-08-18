---
name: feature-delivery
description: Implements an approved product model as the smallest coherent Room, ViewModel, Compose, policy, and test delta.
---

# Feature Delivery

Use this skill only after the objective, invariants, product meaning, minimum viable change, rejected complexity, and verification evidence are explicit.

## 1. Delivery Formula

```text
Existing Valid Mechanisms
+
Smallest Affected Scope
+
Minimum Necessary Files
+
Explicit Verification
=
Feature Delivery
```

## 2. Implementation Order

Change only the layers required by the approved model:

1. **Domain model** — add or modify an entity, relationship, state, or event only when the objective cannot be represented by an existing model.
2. **Room persistence** — update entity registration, converters, DAO queries, and migration-sensitive configuration only when persistence changes.
3. **Repository layer** — centralize data mutation and audit behavior; do not duplicate business rules in UI.
4. **Policy engine** — route authorization and governance decisions through `HierarchicalPolicyEngine`.
5. **ViewModel** — expose one authoritative state flow and bounded actions.
6. **Compose UI** — reuse existing screens and components; optimize for mobile interaction cost.
7. **Tests** — prove the objective and invariants at the narrowest reliable level.

Skip any layer that the change does not require.

## 3. Minimal-Delta Rules

- Reuse existing navigation, state, component, and repository mechanisms before adding another framework.
- Prefer one canonical abstraction when several callbacks or branches express the same semantic concept.
- Do not introduce speculative extension points, generic engines, or future-only states.
- Do not create a new top-level tab when an existing screen, contextual action, dialog, or bottom sheet is sufficient.
- Keep policy checks outside composables.
- Keep persistence decisions outside UI state.
- Preserve existing ownership and access hierarchy unless the approved product model explicitly changes it.
- Add `Modifier.testTag` to every new primary interaction.

## 4. Required Delivery Record

Before editing, state:

```text
Files To Change:
Files Explicitly Not Changing:
Existing Mechanisms Reused:
New Model Elements:
Data Flow:
Policy Path:
UI Entry Point:
Verification Plan:
```

After editing, report:

```text
Implemented Delta:
Rejected Complexity Preserved:
Checks Run:
Evidence:
Residual Risk:
```

## 5. Stop Rule

Stop when the minimum viable behavior is complete and the declared verification evidence passes. Do not add adjacent features merely because the changed area makes them convenient.
