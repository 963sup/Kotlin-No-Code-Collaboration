# Mobile Collaboration v2 — First-Principles Decision

## Invariants

- Repository remains a no-code collaboration container owned only by User or Organization.
- Team access remains an access rule, never ownership.
- RepoIssue remains the only persisted work truth; WBS and Kanban are projections.
- Scope-aware Home uses existing scoped records; no dashboard persistence is added.
- SavedTarget is the single generic favorite relation.
- UserFollow is the only follow graph; XP and public activity are projections from allow-listed audit events.
- Room remains the local UI source of truth.
- Remote sync uses an outbox, explicit versions/cursors/conflicts, Firebase-authenticated HTTPS requests, and FCM only as an untrusted sync hint.
- Schema 4 to 5 uses an explicit migration and must preserve existing issue data.

## 80/20 scope

The highest-leverage field loop is: select scope → find work → update one Issue → roll up WBS → notify/sync → retrieve through Inbox or Explore. Existing cross-repository My Work from PR #20 is reused rather than replaced.
