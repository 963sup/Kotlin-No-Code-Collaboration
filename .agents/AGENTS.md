# Agent Skills & Routing Overview

Only skills physically present under `.agents/skills` are available. Do not reference planned or imaginary skills.

## Available Skills

- **first-principles-thinking** — Defines the objective, facts, invariants, root constraint, minimum viable change, verification evidence, and stop condition.
- **product-guardian** — Accepts, translates, or rejects product semantics against the No-Code Collaboration constitution.
- **feature-delivery** — Implements the smallest coherent change through existing Room, ViewModel, Compose, and policy mechanisms.
- **verification-gate** — Proves semantic, relational, behavioral, constitutional, and technical correctness.

## Default Routing

### Consequential product or architecture change

```text
first-principles-thinking
→ product-guardian
→ feature-delivery
→ verification-gate
```

### Clear bounded implementation with an already-approved product model

```text
feature-delivery
→ verification-gate
```

### Trivial isolated edit

Use the direct edit path and run only the smallest relevant check.

## Routing Rules

1. Use the smallest sufficient skill set.
2. Run `product-guardian` before implementation whenever ownership, Repository semantics, navigation, permissions, or lifecycle meaning could change.
3. Do not begin implementation until the minimum viable change and rejected complexity are explicit.
4. Verification must test the original objective and invariants, not merely compilation.
5. Stop when the declared evidence passes; do not add speculative scope.
