# AGENTS.md

## What This Is

**Galery** — Encrypted cloud gallery for Android. Photos are encrypted client-side via rclone crypt before upload; cloud stores only `.enc` blobs. All thumbnails are encrypted too.

**app ID:** `xy.onlasdan.galery` | **Package:** `xy.onlasdan.galery`

## Build

Prerequisites: Go 1.24+, JDK 17, Android SDK + NDK `25.2.9519653` (auto-installed by Gradle if `sdkmanager` is available). Version pins in `gradle.properties`.

```sh
./gradlew assembleDebug         # debug (quick iteration)
./gradlew assembleRelease       # release (minified, signed)
```

**Rclone is compiled from source by Gradle.** The `:rclone:buildAll` task runs automatically before `:app:preBuild` and cross-compiles rclone (including crypt support) for all 4 ABIs. First build downloads rclone into `rclone/cache/` — expect 5-15 min.

**To skip rclone build** (when only changing Android code, after first full build):

```sh
./gradlew lint -x :rclone:buildAll                  # lint only
./gradlew assembleDebug -x :rclone:buildAll          # skip if app/lib/ .so files exist
```

**NEVER run a build without explicit user permission.**

## Modules

| Module | Purpose |
|--------|---------|
| `app` | Main Android app (`xy.onlasdan.galery`) — gallery UI, crypto, upload, camera |
| `rclone` | Go build script → cross-compiles rclone as `librclone.so` into `app/lib/` |
| `safdav` | SAF-over-WebDAV library (`io.github.x0b.safdav`) — used by upload pipeline |

## Source Layout

```
app/src/main/java/
├── xy/onlasdan/galery/          ← NEW: gallery app code
│   ├── ui/gallery/              PhotoGridActivity, PhotoDetailActivity
│   ├── ui/setup/                OnboardingActivity, BackendPickerActivity
│   ├── ui/settings/             SettingsActivity
│   ├── ui/about/                AboutActivity
│   ├── crypto/                  CryptKeyStore, RcloneCryptProvider, BackendConfig
│   ├── thumbnails/              ThumbnailEncryptor, ThumbnailCache, ThumbnailLoader (Glide)
│   ├── upload/                  UploadWorker, UploadScheduler, MediaStoreObserver
│   ├── camera/                  CameraIntentHandler
│   └── data/                    Room DB (GalleryDatabase, PhotoDao), model/Photo, repo/GalleryRepository
│
└── ca/pkay/rcloneexplorer/      ← LEGACY: kept for rclone wrappers + WorkManager infra
    ├── Rclone.java, RcloneRcd.java    rclone CLI/API interface
    ├── Services/RcdService.java       rclone daemon lifecycle
    ├── Services/SyncService.kt        WorkManager default service (required, do not remove)
    ├── BroadcastReceivers/            BootReciever, SyncRestartAction, ClearReport
    └── util/                          FLog, NotificationUtils, WifiConnectivityUtil, etc.
```

**Key files that are NOT gallery code but must not be deleted:**
- `ca.pkay.rcloneexplorer.RcloneRcd` — all rclone operations go through this
- `ca.pkay.rcloneexplorer.Services.SyncService` — manifest-declared, required by WorkManager
- `ca.pkay.rcloneexplorer.BroadcastReceivers.BootReciever` — schedules uploads on boot

## Crypt Flow

1. User configures a real rclone backend + crypt password/salt in onboarding → stored in `CryptKeyStore` (EncryptedSharedPreferences)
2. `MediaStoreObserver` watches for new photos → `UploadWorker` enqueues them
3. Worker reads source → generates encrypted thumbnail (512px JPEG) → uploads both via `RcloneCryptProvider` (rclone crypt remote)
4. Grid displays thumbnails: `ThumbnailLoader` (Glide ModelLoader) fetches `.enc` thumbnail → decrypts in-memory → returns Bitmap

## Running Tests

```sh
./gradlew test                  # unit tests
./gradlew lint                  # lint (uses baseline: app/lint-baseline.xml)
```

## Gotchas

- **ANDROID_HOME required.** Set env var or `sdk.dir` in `local.properties` (gitignored). Build fails hard without it.
- **NDK auto-installs.** If NDK version in `gradle.properties` is missing, Gradle runs `sdkmanager --install` (requires `cmdline-tools`).
- **`rclone/cache/` is gitignored.** Holds Go module cache + GOPATH. Delete to force clean rclone rebuild.
- **`app/lib/` holds built `.so` files.** Gitignored — only created by Go cross-compilation.
- **versionCode last digit reserved for ABI.** Never let `versionCode` end in anything other than `0` (`app/build.gradle`).
- **`app/src/main/java/ca/pkay/rcloneexplorer/` is legacy code.** Keep it as-is for now; new gallery code lives under `xy.onlasdan.galery/`. Do not mix the two packages.
- **Room DB uses SQLCipher.** `GalleryDatabase` uses `net.zetetic:android-database-sqlcipher`. Don't use plain `Room.databaseBuilder()` — use `GalleryDatabase.getInstance()`.
- **Translations managed via Crowdin** (`crowdin.yml`). Source strings: `app/src/main/res/values/strings.xml`.
- **Lint baseline exists.** New lint errors fail the build. Update baseline only for intentional suppressions.
- **Single build flavor.** No `oss`/`rs` flavors — collapsed during pivot. `./gradlew assembleDebug` only.
