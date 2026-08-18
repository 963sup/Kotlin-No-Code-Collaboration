# Codex Security Validation

## Scope

Security-focused diff validation against `main` for the first-principles collaboration branch, covering canonical navigation, WBS projection, scope dashboards, My Work aggregation, global Explore, generic favorites, follow/activity/achievement projections, Room schema additions, and synchronization contracts.

## Method

The review followed an evidence-first source-to-sink process:

1. Identify user-controlled identifiers and event payloads.
2. Trace them through target parsing, repository/entity lookup, authorization, projection, and navigation.
3. Check cross-enterprise, cross-organization, cross-repository, and stale-object behavior.
4. Review persistence and migration SQL for data-loss and tenant-isolation risks.
5. Review outbox, cursor, version, retry, conflict, authentication-token, and push boundaries.
6. Search the change set for embedded credentials, cleartext transport, destructive migration, exported Android components, mutable `PendingIntent`, fail-open permission paths, and broad fallback navigation.
7. Add focused tests for the highest-risk invariants.

## Validated security properties

### Exact target resolution is fail-closed

`SafeTargetResolver` returns `Missing` or `Denied` for the requested target. It does not replace a denied Issue, Artifact, or Discussion with its Repository or another broader destination. This prevents hidden authorization failures and accidental disclosure of tenant context.

### Explore is authorization-filtered before display

`ExploreService` applies `TargetAuthorization` before results are returned. UI code must not render a candidate and defer authorization until after a click.

### My Work requires access and assignment

`MyWorkProjector` requires both repository access and assignment to the current user or one of the user's active teams. Assignment alone is not treated as authorization.

### Social projections exclude governance audit data

Governance audit events are categorically removed before feed, trending, XP, level, or achievement projection. Public-looking flags cannot override this exclusion.

### Push payloads are untrusted

Push data is represented only as a `PushTargetHint`. The resulting target must pass exact existence and authorization resolution before navigation.

### Synchronization does not embed credentials

Bearer tokens enter through a runtime authenticated context. No production endpoint, API key, service-account material, or static token is defined by the feature.

### Outbox replay and tenant boundaries are explicit

Outbox rows carry enterprise and actor identities, a unique idempotency key, expected server version, bounded retry metadata, and explicit conflict/dead-letter states. Sync cursors are keyed by enterprise and stream.

### Schema changes are additive

The migration creates new tables and indexes with explicit SQL. It does not call destructive migration. Production registration must use the existing database's actual historical version chain.

## Focused test evidence

`FirstPrinciplesCoreTest` validates:

- denied Issue navigation does not widen to Repository;
- unauthorized Explore candidates are removed before display;
- audit events cannot produce feed, trending, XP, or achievements;
- inaccessible or unassigned Issues are excluded from My Work;
- WBS roll-up and numbering remain deterministic;
- retry backoff remains bounded.

The branch CI executes unit tests and debug assembly.

## Residual risks and release gates

1. A production server must independently authorize every query and mutation using authenticated enterprise/user context. Client checks are defense in depth only.
2. The existing app database must register the migration at its real current version and include the new entities; every released historical schema should be tested with Room migration tests.
3. A production sync worker must redact tokens and payloads from logs, add retry jitter, enforce request timeouts, and treat HTTP conflict responses as explicit reconciliation states.
4. Push notification components must remain non-exported unless a verified platform integration requires otherwise; any `PendingIntent` must use the narrowest immutable flags supported.
5. Activity visibility must be computed from source-object authorization at projection time and rechecked when the target is opened, because permissions can change after an event is emitted.
6. Saved targets and follows must not disclose that an inaccessible target exists; list queries should remove or tombstone inaccessible entries without returning target metadata.

## Result

No validated Critical or High severity vulnerability is introduced by the new core primitives. The remaining items are production-integration gates rather than accepted permission bypasses or embedded-secret risks.
