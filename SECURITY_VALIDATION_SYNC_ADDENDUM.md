# Sync Credential Validation Addendum

The security pass identified that ordinary Kotlin data classes can expose secret fields through generated `toString`, equality, or logging. Production sync adapters must therefore use `SecureBearerToken` and `SecureAuthenticatedSyncContext`:

- token construction is restricted to the authenticated-session boundary;
- string rendering is always redacted;
- callers receive the authorization header only inside a narrow callback;
- credentials are not persisted in Room outbox or cursor tables;
- transport implementations must not log context objects or request headers.

The earlier plain sync context is a transport-neutral scaffold and must not be wired into production logging or persistence. The secure context is the release-path contract.
