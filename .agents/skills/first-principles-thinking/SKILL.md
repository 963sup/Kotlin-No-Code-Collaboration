---
name: first-principles-thinking
description: Core reasoning engine for problem definition, scope reduction, root-cause analysis, and composable skill routing across the No-Code Collaboration Platform.
---

# First Principles Thinking

A lightweight, high-leverage reasoning skill designed to deconstruct ambiguous requests, isolate root causes, eliminate non-essential complexity, and define the minimum viable path to verified outcomes.

This skill **does not duplicate or replace execution skills**. It acts as the cognitive front-end, routing guide, and course-corrector before, during, or after execution.

---

## 1. Trigger Conditions

Use this skill when facing:
- **Architectural Ambiguity**: Unclear system boundaries or responsibility distribution.
- **Unclear Product Semantics**: Ambiguity around whether a feature violates No-Code boundaries.
- **Multiple Divergent Solutions**: Competing implementation paths requiring trade-off evaluation.
- **Significant Refactoring**: Structural changes across multiple components.
- **Recurring Defects / Regressions**: Bugs persisting across multiple attempts.
- **Complex Dependencies**: Multi-layer state, policy, or data cascades.
- **Production / Release Incidents**: Urgent triage requiring symptom vs. cause isolation.
- **Consequential Decisions**: High-impact additions to data models, permissions, or navigation.

> **Zero-Overhead Rule**: Do NOT invoke for trivial, isolated, or low-risk tasks (e.g., simple text edits, single-parameter adjustments).

---

## 2. Core Reasoning Cycle

```
[Context / Prompt]
       │
       ▼
Reverse Engineer (if decomposing external benchmark)
       │
       ▼
First Principles (Deconstruct to fundamental truths & invariants)
       │
       ▼
Simplify (Remove non-essential semantics & accidental complexity)
       │
       ▼
Focus on Highest Leverage (Isolate root cause or primary leverage point)
       │
       ▼
Minimum Viable Change (Smallest coherent delta in code/models)
       │
       ▼
Verify (Evidence-based validation against invariants)
       │
       ▼
Learn / Stop (Halt when verified; iterate cleanly if invalidated)
```

---

## 3. Ten-Step Reasoning Protocol

1. **Identify the Actual Objective**: What fundamental problem is the user solving? (Strip away proposed implementation bias).
2. **Separate Facts from Assumptions**: What is objectively verified in the codebase vs. what is assumed?
3. **Isolate Immutable Invariants**: Respect project constitutions (No-Code Collaboration Platform, Repository as workspace container, Room + Compose architecture).
4. **Decompose the Problem**: Break down into canonical dimensions:
   - *Entities* (Who/What)
   - *Relationships* (Ownership/Access)
   - *States* (Lifecycle/Condition)
   - *Events* (Triggers/Mutations)
   - *Responsibilities* (Which layer owns what)
5. **Find Root Cause**: Avoid patching symptoms; trace regressions to upstream state or semantic mismatches.
6. **Remove Unnecessary Complexity**: Eliminate premature abstractions, speculative states, and extra layers.
7. **Identify the Highest-Leverage Point**: What single change or pivot solves the core constraint?
8. **Define the Smallest Sufficient Scope**: Target the minimal set of files and lines required for full correctness.
9. **Define Verification Evidence**: Formulate exact criteria and test assertions to prove correctness.
10. **Stop When Sufficient**: Do not over-design once the problem is resolved and verified.

---

## 4. Composable Skill Combinations

Combine this skill dynamically with domain execution skills using the smallest sufficient set:

| Combination | Purpose & Outcome |
| :--- | :--- |
| **First Principles + Product Guardian** | Clarify whether candidate features belong within the No-Code Collaboration domain and eliminate leaked code-centric semantics. |
| **First Principles + Architecture Steward** | Determine semantic responsibility boundaries and dependency radii before structural refactoring. |
| **First Principles + Feature Delivery** | Find the smallest coherent implementation scope that achieves user intent without unnecessary UI or architectural baggage. |
| **First Principles + Verification Gate** | Derive explicit verification criteria and invariant assertions directly from original objectives. |
| **First Principles + Release Guardian** | Isolate highest-risk assumptions, security vulnerabilities, and release-critical invariants. |
| **First Principles + Incident Triage** | Separate visible failure symptoms from root causes to execute the safest minimal recovery. |

---

## 5. Operating Modes

- **Standalone Analysis**: Deconstruct an ambiguous problem, validate feasibility, or define scope before proposing code changes.
- **Pre-Execution Filter**: Run prior to `feature-delivery` or `architecture-steward` to define minimal scope and boundaries.
- **Parallel Reasoning**: Accompany complex workflows to ensure focus on highest-leverage solutions.
- **Post-Failure Correction**: Re-evaluate first principles when a build, test, or feature implementation fails or loops.

---

## 6. Project Invariants & Decision Rules

1. **Constitutional Invariant**: The root `AGENTS.md` is the authoritative product constitution. The product is a **No-Code Collaboration Platform**.
2. **Container Semantics**: The `Repository` is a collaboration container for artifacts, issues, discussions, policies, and milestones—never a Git/code repository.
3. **No Developer Semantics**: Strictly exclude Git, branches, diffs, PRs, CI/CD, IDE features, and syntax tooling.
4. **Architecture Decisions**: Optimize for **semantic responsibility and tight dependency radii**, not arbitrary line counts.
5. **Implementation Decisions**: Always prefer:
   $$\text{Smallest affected scope} + \text{Minimum necessary files} + \text{Existing valid mechanisms} + \text{Explicit verification}$$
