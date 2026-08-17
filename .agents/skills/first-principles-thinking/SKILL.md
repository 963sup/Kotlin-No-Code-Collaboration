---
name: first-principles-thinking
description: Mandatory reasoning gate for consequential product, architecture, navigation, policy, persistence, and multi-file changes.
---

# First Principles Thinking

Use this skill to convert an ambiguous request into the smallest verifiable change that preserves the No-Code Collaboration Platform constitution.

This skill is a decision gate, not an execution substitute.

## 1. Trigger Conditions

Invoke for changes involving any of the following:

- product or Repository semantics
- ownership, membership, access, roles, or policy
- entities, relationships, lifecycle states, or persistence
- navigation or cross-screen behavior
- multiple plausible implementation paths
- significant refactoring or recurring defects
- changes spanning multiple files or layers

### Zero-Overhead Rule

Do not invoke for trivial isolated edits whose objective, scope, and verification are already obvious.

## 2. Immutable Project Invariants

1. The product is a **No-Code Collaboration Platform**.
2. A `Repository` is a collaboration container, never a Git or source-code repository.
3. Git, commits, branches, tags, pull requests, diffs, CI/CD, source editors, terminals, and IDE semantics are excluded.
4. Persistent entities and relationships use Room.
5. UI state flows through ViewModel and StateFlow.
6. Permission decisions route through `HierarchicalPolicyEngine`.
7. Mobile interaction cost is a primary constraint; new top-level screens and tabs require explicit proof.

## 3. Mandatory Decision Record

Before implementation, produce the following concise record:

### Objective

State the user or system outcome without assuming a feature or technical solution.

### Facts

List only facts verified from the current repository.

### Assumptions

List material unknowns. Do not silently promote assumptions into facts.

### Invariants

List the constitutional, governance, architecture, and mobile constraints that must remain true.

### Core Model

Decompose the change into:

- **Entities** — who or what exists
- **Relationships** — ownership, membership, access, dependency, or linkage
- **States** — lifecycle or condition
- **Events** — triggers and mutations
- **Responsibilities** — which layer owns each decision

### Root Constraint

Identify the smallest upstream cause preventing the objective. Do not describe only the visible symptom.

### Highest-Leverage Change

Identify the single intervention that removes the most repeated logic, navigation cost, ambiguity, or failure risk.

### Minimum Viable Change

Define the smallest coherent implementation that completely resolves the root constraint.

### Rejected Complexity

Explicitly list features, abstractions, screens, states, and refactors that are not required now.

### Verification Evidence

Define exact semantic assertions, relationship assertions, behavior checks, invariant checks, and technical commands.

### Stop Condition

State when the work is sufficient and must stop.

## 4. Reasoning Protocol

```text
Request
↓
Objective
↓
Facts vs Assumptions
↓
Invariants
↓
Entities × Relationships × States × Events × Responsibilities
↓
Root Constraint
↓
Remove Accidental Complexity
↓
Highest-Leverage Change
↓
Minimum Viable Change
↓
Verification Evidence
↓
Pass → Stop
Fail → Return to Root Constraint
```

## 5. Decision Rules

- Repository evidence outranks benchmark imitation.
- Translate useful GitHub collaboration semantics; reject code-centric mechanics.
- Prefer an existing valid entity or mechanism before creating another one.
- One reusable semantic abstraction is preferable to several feature-specific callbacks when they represent the same concept.
- A smaller incomplete change is not an MVP. The minimum change must still form a complete behavior.
- Do not design for hypothetical future features unless the current objective requires the extension point.
- Do not optimize arbitrary line count; optimize semantic responsibility and dependency radius.

## 6. Skill Routing

After the decision record:

- use `product-guardian` when product meaning or constitutional boundaries are involved
- use `feature-delivery` for implementation
- use `verification-gate` before completion

Use only skills that exist under `.agents/skills`.

## 7. Completion Criteria

This skill is complete only when:

- facts and assumptions are separated
- the root constraint is explicit
- the minimum viable change is bounded
- rejected complexity is recorded
- verification evidence is testable
- a stop condition prevents speculative expansion
