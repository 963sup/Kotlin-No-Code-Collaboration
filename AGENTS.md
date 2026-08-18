# Core Reasoning & Project Constitution

- **Understand Before Changing**: Reverse-engineer existing code, constraints, and conventions before altering behavior. Gather just enough evidence to decide safely, then act; stop when further analysis yields diminishing returns.
- **First Principles & Root Cause**: Reduce problems to facts, constraints, and causal invariants. Fix underlying causes rather than accumulating symptom patches.
- **Simplify Aggressively**: Choose the simplest solution meeting requirements. Eliminate unnecessary abstractions, indirection, branching, hidden state, and cognitive overhead.
- **Focus on High-Impact Scope**: Solve strictly what the current goal requires. Never introduce speculative features or unrequested enhancements.
- **Preserve System Invariants**: Identify and protect data invariants, contracts, ownership, boundaries, and externally relied-upon semantics.
- **Explicit & Consistent Design**: Make ownership, dependencies, contracts, and data flows explicit. Follow established codebase conventions and domain vocabulary.
- **Protect Structural Integrity**: Never treat changes as isolated patches. When structural decay appears (boundary leaks, dependency drift, duplicated concepts, recurring workarounds), resolve the root cause at the proper layer.
- **Smallest Complete Change**: Implement the minimal complete change that solves the issue and validates core assumptions. Prioritize safe, reversible steps.
- **Prevent Recurrence**: When fixing structural issues, add the minimal invariant, guard, contract, or test needed to prevent identical regression.
- **Validate With Evidence**: Verify assumptions, runtime behavior, and state changes with concrete evidence rather than inference.
- **Observe and Iterate**: Inspect execution results against intended invariants after every change, then adjust the next action.

# Architectural Principles & Boundaries

- **Separation of Concerns (SoC)**: Strictly isolate responsibilities; never mix business rules, application orchestration, infrastructure, and UI within the same module or layer.
- **High Cohesion, Low Coupling**: Keep related logic concentrated within its owning module. Expose minimal public interfaces and forbid sharing internal implementation details.
- **Explicit Boundaries**: Every module, package, and bounded context must maintain defined ownership, clean public contracts, and inviolable boundaries.
- **Unidirectional Dependencies**: Enforce strictly unidirectional dependencies. Forbid circular references, reverse dependencies, and core domain logic depending on upper-level implementations.
- **Reversible Design**: Prioritize designs that are easy to alter, replace, or roll back. Avoid premature, hard-to-reverse architectural commitments.
- **YAGNI (You Aren't Gonna Need It)**: Never introduce abstractions, extension points, dependencies, or architectural complexity for unvalidated or future requirements.
