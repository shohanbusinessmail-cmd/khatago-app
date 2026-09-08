# KhataGo

**Your Money. Your Records. Your Control.**

KhataGo is a free, privacy-first, offline personal finance record manager for shop credit, loans, EMI purchases, borrowed money, lent money, income, expenses and payments. It is designed to make a complicated financial picture easy to understand without an account, advertisements or a cloud service.

Created by **Shohan Khan** · [helloiamshohan@gmail.com](mailto:helloiamshohan@gmail.com)

## Product highlights

- Light-only premium fintech UI, with a calm emerald visual system.
- Offline-first local Room database; core records remain usable without internet.
- Shop credit, bank/NGO loans, EMI purchases, borrowing, lending, income and expense records.
- Independent payment ledger with safe partial-payment calculations and overpayment prevention.
- Home dashboard for outstanding debt, money owed to the user, cash flow, due-soon and overdue balances.
- Global search, record-family filters, payment center and local rule-based insights.
- Analytics with actual stored data: cash flow, expense categories and outstanding breakdown.
- JSON backup/restore with schema validation and safe replace semantics; CSV export.
- Optional Android local reminders through WorkManager.
- No ads, subscription, tracking SDK, paid API, mandatory login or unnecessary network permission.

## Architecture

The app uses a single Android application module with clear layers:

- `domain/`: money-safe models, statuses and pure financial calculation rules.
- `data/`: Room entities/DAO/database, repository, DataStore settings, backup codec and WorkManager reminders.
- `ui/`: Compose screens, reusable components, light-only Material 3 theme and a state-driven ViewModel.

Money is stored as integer minor units (`Long`), never `Float` or `Double`. Payment rows are independent of an obligation; balances are derived from the payment ledger. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/DATA_MODEL.md](docs/DATA_MODEL.md).

## Build

Open the repository in Android Studio (Ladybug or newer) with JDK 17 and Android SDK 35 installed. Gradle resolves dependencies from Google Maven and Maven Central. Then run:

```bash
./gradlew test
./gradlew connectedDebugAndroidTest   # emulator/device required
./gradlew assembleDebug
./gradlew assembleRelease
```

The release build is intentionally unsigned by default. Configure a private signing key outside the repository before distributing a release. Full release notes are in [docs/RELEASE.md](docs/RELEASE.md).

## Data and privacy

KhataGo stores records locally on the device. It does not require internet access, an account or a cloud subscription. A JSON backup is readable data: protect files you export and only share them with people you trust. Restore validates the complete file before replacing local data. Read [docs/PRIVACY.md](docs/PRIVACY.md).

## Backup

Use **More → Create backup** for a complete versioned JSON snapshot or **Export CSV** for spreadsheet use. **Restore backup** uses a safe replace workflow: malformed files are rejected before Room is changed, and the screen warns that existing local records will be replaced.

## Testing

Pure unit coverage includes minor-unit parsing, balance clamping, payment status boundaries, due labels, dashboard separation of money owed versus money owed to the user, record types and backup round trips. An instrumented Room test covers foreign-key payment persistence. See `app/src/test` and `app/src/androidTest`.

## License

KhataGo source is released under the Apache License 2.0. See [LICENSE](LICENSE). The supplied KhataGo logo is included as the project brand asset.
