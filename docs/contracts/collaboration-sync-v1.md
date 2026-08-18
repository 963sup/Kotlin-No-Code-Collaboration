# Collaboration Sync API v1

Every request uses `Authorization: Bearer <Firebase ID token>` over HTTPS. The server must verify token authenticity and re-evaluate enterprise membership, Repository visibility, ownership, role, and action policy; client payloads are never authorization evidence.

`POST /v1/sync/push` accepts idempotent outbox events with entity type/id, operation, local version, and bounded payload. `409` represents version conflict. `401/403` represents authentication or authorization failure.

`GET /v1/sync/pull?cursor=<opaque>` returns only mutations currently visible to the authenticated user. The client does not overwrite an entity that has a pending local change; it records a conflict instead. Generic remote envelopes cannot hard-delete governance records.

`POST /v1/push-registrations` binds an FCM token to the authenticated user/device. Push content is only a sync hint; domain state is fetched through authenticated pull.

Tokens and secrets must not be written to logs. Endpoint configuration must be HTTPS and must not contain embedded credentials.
