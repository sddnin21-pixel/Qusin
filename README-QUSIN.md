# Qusin — Android build

This source tree contains the Qusin-branded Android build, based on the original Mika application code.

## Changes in this build

- App name and visual identity changed to **Qusin**, using the supplied cat-and-clock logo for the launcher, welcome/about surfaces, capsule and notifications.
- **Tiếng Việt** added as an app language. The main chat, settings, model setup, provider/key management, onboarding, status and about strings are translated; strings not yet translated fall back to English.
- Provider API keys can be stored as a key ring. Add keys from **Settings → Model → provider → Create key**. Keys are masked in the UI and encrypted locally with Android Keystore. There is no app-level key-count limit; duplicate keys are not saved twice.
- Requests try the selected key first, then other saved keys after authentication errors, exhausted quota or rate limits. Network/server/content errors do not consume other keys. Streaming fallback only occurs before any answer text has been emitted, avoiding duplicate partial answers.
- Existing single-key data in `provider-keys.json` is migrated to the key-ring store on load.

## Build

Requirements: JDK 17 and Android SDK 36, with the Android SDK platform/build tools installed.

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Upgrade compatibility and project links

The Android `applicationId` remains `com.wangzan101.mika` and the version is `0.2.0` (`versionCode` 39), preserving the original app identity and allowing the old key data to migrate. To install over an existing release and keep its app data, the Qusin APK must also be signed with the exact same signing certificate as that installed release; a debug-signed APK cannot replace a release-signed installation. The in-app source/release links still point to the original Mika repository because no Qusin repository URL was supplied; change `MikaLinks.kt` when a Qusin repository is available.

The build in this workspace was not verified by a complete Android/Gradle build: the execution environment did not have Android SDK 36 installed and could not download Gradle. XML/resource checks and a standalone Kotlin syntax check for the key-rotation model were completed, but run the Gradle build above before publishing or installing a release build.
