# Agent Skills & Routing Overview

## Available Skills

- **first-principles-thinking**: Core reasoning engine for problem definition, scope reduction, root cause isolation, and skill routing.
- **product-guardian**: Guards the No-Code Collaboration Platform boundary and domain integrity.
- **architecture-steward**: Maintains architectural invariants, MVVM/Clean boundaries, and Room/Compose architecture.
- **feature-delivery**: Implements bounded features following MVP and minimal-delta principles.
- **verification-gate**: Formulates and executes verification strategies (compilation, Robolectric unit tests).
- **release-guardian**: Protects release invariants, security, and migration safety.
- **incident-triage**: Triage, root cause isolation, and hotfix delivery for defects and regressions.

## Composing Skills

Use the **smallest sufficient skill set**:
- For ambiguous/complex requests: `first-principles-thinking` $\rightarrow$ Target Skill (`product-guardian` / `architecture-steward` / `feature-delivery`) $\rightarrow$ `verification-gate`.
- For straightforward tasks: invoke the dedicated execution skill directly without unnecessary reasoning overhead.
