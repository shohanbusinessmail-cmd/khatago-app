# KhataGo architecture

## Runtime shape

`MainActivity` installs a deliberately light Material 3 theme and obtains one `MainViewModel`. The ViewModel owns screen state and delegates all writes to `FinanceRepository`. Compose observes `StateFlow`s and never performs financial calculations in a Composable.

```
Compose screens -> MainViewModel -> FinanceRepository -> Room
                         |                |
                         +-> DataStore    +-> independent payment ledger
                         +-> BackupCodec
                         +-> reminder settings / WorkManager
```

## Offline first

There is no `INTERNET` permission. Records, search, calculations, reports, export and restore are local. WorkManager only schedules an Android-native local notification and quietly exits when notification permission is unavailable.

## Financial correctness

- Every monetary amount is a positive `Long` in minor units.
- `remaining = max(amount - sum(payments), 0)`.
- A payment larger than the remaining balance is rejected by the repository.
- A payment is never silently folded into or overwritten on the source record.
- Status is derived using the balance and the due date, and is not stored as stale duplicated data.
- Backup import is parsed and validated fully before the Room transaction replaces local data.

## UI system

`ui/Theme.kt` defines the light-only color scheme and typography. The `KhataGoTheme` intentionally ignores the device dark-mode flag. `ui/components` contains shared cards, rows, status chips, empty states and amount/progress primitives. Screens are intentionally thin: they collect state, format it and dispatch user intent.

## Background reminders

`ReminderWorker` runs at most once per day. It reads current records and payments, identifies balances due today/tomorrow and sends one grouped local notification. The system notification permission is requested only when the user enables notifications.

## Evolution

The first release uses a compact relational model that keeps all record families in one indexed table while preserving the payment ledger as its own table. This allows shared search, dashboard and reporting queries without duplicating balance logic. New record-specific fields can be added with a Room migration; existing user data should never be silently dropped in a production migration.
