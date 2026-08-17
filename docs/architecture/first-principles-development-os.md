# First Principles Development OS

**Status:** Accepted  
**Scope:** Product and engineering decision process  
**Constitution:** No-Code Collaboration Platform

## 1. Problem

The repository already described First Principles Thinking, but the behavior was not mechanically enforced:

- consequential changes could begin before facts and assumptions were separated
- the agent routing document referenced skills that did not exist
- compilation could be treated as completion without proving the original objective
- benchmark features could be copied before resolving their no-code product meaning
- adjacent scope could enter a change without a stop condition

The root constraint was not a lack of more reasoning prose. It was the absence of a small, repeatable gate at feature intake, implementation routing, and pull-request verification.

## 2. Objective

Convert First Principles Thinking from optional guidance into the default operating system for consequential repository changes.

```text
Request
↓
Objective
↓
Facts vs Assumptions
↓
Invariants
↓
Core Model
↓
Root Constraint
↓
Highest-Leverage Change
↓
Minimum Viable Change
↓
Verification Evidence
↓
Pass → Stop
Fail → Re-evaluate Root Constraint
```

## 3. Core Formula

```text
Quality Change
=
Verified Facts
×
Correct Product Semantics
×
Minimum Complete Scope
×
Evidence-Based Verification
```

A zero in any factor invalidates completion.

## 4. Mandatory Decision Object

Every consequential change must define:

| Field | Question |
|---|---|
| Objective | What outcome must exist? |
| Facts | What is verified in the repository? |
| Assumptions | What remains unknown? |
| Invariants | What cannot be broken? |
| Core Model | Which entities, relationships, states, events, and responsibilities are involved? |
| Root Constraint | What upstream cause prevents the objective? |
| Highest-Leverage Change | What smallest intervention removes the most constraint? |
| Minimum Viable Change | What is the smallest complete implementation? |
| Rejected Complexity | What is explicitly excluded? |
| Verification Evidence | What proves semantic and technical correctness? |
| Stop Condition | When must implementation stop? |

## 5. Skill Chain

Only four skills are required for the current maturity level:

```text
first-principles-thinking
→ product-guardian
→ feature-delivery
→ verification-gate
```

This is intentionally smaller than the previously declared skill catalog. Additional skills should be introduced only after a repeated, distinct responsibility cannot be handled by the existing four.

## 6. 80/20 Enforcement Points

The operating system is enforced at four high-leverage points:

1. **Root `AGENTS.md`** — constitutional and mandatory decision rules.
2. **Feature issue form** — facts, scope, exclusions, and evidence before work starts.
3. **Agent skills** — product decision, minimum-delta delivery, and verification routing.
4. **Pull-request template** — proof that the objective and invariants were satisfied before completion.

No additional workflow engine, automation service, or meta-framework is required at this stage.

## 7. First Product Application: Unified Collaboration Target

The first runtime feature to pass through this operating system should be unified collaboration navigation.

### Objective

A notification must open the exact collaboration object requiring attention, without forcing the user to rediscover it inside a Repository.

### Verified Facts

- notifications can already reference Repository, Artifact, Issue, Discussion, Organization, Team, and User identifiers
- Artifact notifications can navigate to the Artifact
- Issue and Discussion notifications currently fall back to broader Repository navigation
- mobile navigation cost increases with every intermediate search and tab selection

### Core Model

```text
CollaborationTarget
├─ Repository(repoId)
├─ Artifact(repoId, artifactId)
├─ Issue(repoId, issueId)
├─ Discussion(repoId, discussionId)
├─ Organization(orgId)
├─ Team(teamId)
└─ User(userId)

Notification
↓ resolve
CollaborationTarget
↓
navigate(target)
```

### Minimum Viable Runtime Change

1. Introduce one canonical `CollaborationTarget` model.
2. Resolve each actionable notification into that model.
3. Route Inbox actions through one `navigate(target)` entry point.
4. Allow Repository detail to open an exact Issue or Discussion target.
5. Prove Artifact, Issue, Discussion, Repository, and invalid-target behavior with focused tests.

### Rejected Complexity

The first implementation must not add:

- a general-purpose navigation framework
- new top-level tabs
- Projects, Milestones, or Global Search
- URI deep-link infrastructure not required by in-app navigation
- speculative target types without a current source and destination

### Stop Condition

Stop when Inbox can open the exact Artifact, Issue, Discussion, or Repository target through one canonical target path and all declared tests pass. Home and future Search may reuse the mechanism in separate changes.

## 8. Scope Boundary of This Change

This architecture change establishes and enforces the development operating system. It deliberately does not mix runtime navigation code into the same change. Combining process governance and product behavior would increase review radius and weaken verification clarity.
