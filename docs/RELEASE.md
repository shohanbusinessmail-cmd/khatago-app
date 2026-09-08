# Release checklist

1. Confirm `./gradlew test` and `./gradlew connectedDebugAndroidTest` pass.
2. Verify onboarding starts clean and no sample records are shipped.
3. Exercise every record family, partial payment, full payment, overdue status, search, filters, JSON backup/restore and CSV export.
4. Test on a light device and a device currently using system dark mode; KhataGo must remain light.
5. Test API 26, a current Android release, small-screen font scaling and a tablet layout.
6. Review notification permission behavior and backup warning copy.
7. Configure a release signing key only in a secure local/CI secret store.
8. Build `./gradlew bundleRelease` for Play distribution or `./gradlew assembleRelease` for an APK.
9. Verify the signed artifact on a clean device before publishing.

Version 1.0.0 is the initial data schema. Future schema changes require explicit Room migrations and a backup/restore compatibility test. No private keys are included in this repository.
