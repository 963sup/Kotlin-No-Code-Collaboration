# Context7 Implementation Notes

The branch uses current AndroidX patterns as implementation constraints:

- Material 3 light and dark palettes are separate `lightColorScheme` and `darkColorScheme` definitions rather than one dark palette renamed as light.
- Composable screens receive immutable state and callbacks; operational/domain truth remains in existing repositories and ViewModels.
- Room schema additions use explicit `Migration` SQL and require `addMigrations` registration at the real database version. Destructive fallback is not an accepted release path.
- The outbox is modeled as durable local state with bounded batches, idempotency, retry, conflict and cursor semantics; an authenticated transport adapter is a separate boundary.
- Phone navigation is limited to four primary destinations. Repository WBS and profile details remain contextual surfaces instead of additional bottom-navigation roots.
