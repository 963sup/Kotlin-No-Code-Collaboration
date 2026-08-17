---
name: product-guardian
description: Guards the No-Code Collaboration Platform boundary and translates useful GitHub collaboration semantics without importing developer mechanics.
---

# Product Guardian

Use this skill after First Principles analysis and before implementation whenever product meaning, ownership, navigation, roles, or Repository scope could change.

## 1. Constitutional Boundary

The product is a mobile **No-Code Collaboration Platform**.

A `Repository` may contain collaboration objects such as:

- issues and work items
- discussions and decisions
- milestones and labels
- policies and access rules
- specifications, documents, forms, workflows, and other no-code artifacts

The following semantics are prohibited:

- Git, commits, branches, tags, pull requests, and diffs
- source-code files, syntax tools, code review, and file editors
- CI/CD, build pipelines, release binaries, terminals, and IDE features

## 2. Product Decision

Classify the proposal as exactly one of:

- **ACCEPT** — directly creates collaboration, governance, traceability, or work-management value.
- **TRANSLATE** — the benchmark idea is useful, but its code-centric form must be converted into a no-code collaboration meaning.
- **REJECT** — the proposal requires prohibited developer semantics or adds no material collaboration value.

## 3. Required Checks

### Objective Check

What user outcome exists independently of the proposed GitHub feature name?

### Ownership Check

Which existing owner is correct?

- Enterprise
- Organization
- User
- Team as access grantee, not Repository owner
- Repository

Do not create a new ownership layer when an existing one is sufficient.

### Container Check

Does the capability belong inside one Repository, across multiple Repositories, or at Organization/User scope?

### Semantic Reuse Check

Can an existing entity, relationship, state, or event represent the need without a new parallel model?

### Mobile Cost Check

Can the capability fit into an existing screen, contextual action, filter, dialog, or bottom sheet? A new top-level tab or screen requires explicit evidence.

### Governance Check

Does the proposal preserve least privilege, separation of duties, independent verification, and auditability?

## 4. Output Contract

Return:

```text
Verdict: ACCEPT | TRANSLATE | REJECT
Objective:
Canonical Owner:
Canonical Container:
Core Entities:
Core Relationships:
Allowed Semantics:
Excluded Semantics:
Mobile Placement:
Governance Impact:
Reason:
```

## 5. Stop Rule

Once the smallest valid product meaning is established, stop product expansion and route the bounded model to `feature-delivery`.
