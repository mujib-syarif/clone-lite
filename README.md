# CloneLite

Minimal Android work-profile launcher for running separate app instances on supported Android devices.

## What it does
- Requests Android's native managed/work profile setup.
- In the work profile, lists launchable apps installed in that profile and opens them.
- Provides a shortcut to the Play Store so apps can be installed separately inside the work profile.
- Builds a debug APK automatically with GitHub Actions.

## Important limitations
CloneLite does **not** copy APKs or bypass app security. It uses Android's managed profile feature. Availability depends on device firmware, encryption/setup state, and whether a work profile already exists. Android normally permits only one managed profile. Install apps separately in the work profile (for example, from the work-profile Play Store). Some apps block work profiles or multiple accounts.

This is a minimal MVP and has not been device-tested on every Android skin. Infinix/XOS support is not guaranteed until tested on the target phone.

## Build
Open **Actions → Android APK → Run workflow** or push a commit to `main`. Download the `CloneLite-debug-apk` artifact from the successful workflow run.

## Target
- Package: `com.mujib.clonelite`
- Minimum Android: 8.0 (API 26)
- Target SDK: 30
- Intended test device: Infinix Hot 11S NFC / Android 11

## Privacy
No analytics, accounts, backend, or network permission is included. The app queries launchable apps to show apps available in the current profile.
