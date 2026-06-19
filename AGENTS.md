# AGENTS.md

## What This Is

**Galery** — Encrypted cloud gallery for Android. Photos are encrypted client-side via rclone crypt before upload; cloud stores only `.enc` blobs. All thumbnails are encrypted too.

**app ID:** `xy.onlasdan.galery` | **Package:** `xy.onlasdan.galery`

## Implementation Status

- **Phase 1:** Strip legacy, rebrand, new skeleton ✓
- **Phase 2:** Core functionality (crypto, upload, gallery UI) ✓
- **Phase 3:** Polish (network constraints, retry, error handling) ✓

## Build

**Build via GitHub Actions only.** Local builds not supported for full APK.

```sh
gh workflow run android.yml --ref main    # trigger build
gh run list --workflow=android.yml        # check status
gh run download <run-id>                  # download APKs
```

**Rclone is compiled from source by Gradle.** CI builds rclone for all 4 ABIs (arm, arm64, x86, x64) + universal. First build takes 5-15 min.

**Local code validation** (no full APK):

```sh
./gradlew lint -x :rclone:buildAll         # lint check
./gradlew test                              # unit tests
./gradlew compileDebugSources -x :rclone:buildAll  # check compilation
```

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
│   ├── ui/gallery/              PhotoGridActivity, PhotoDetailActivity, PhotoAdapter
│   ├── ui/setup/                OnboardingActivity, BackendPickerActivity
│   ├── ui/settings/             SettingsActivity
│   ├── ui/about/                AboutActivity
│   ├── crypto/                  CryptKeyStore, RcloneCryptProvider, BackendConfig
│   ├── thumbnails/              ThumbnailEncryptor, ThumbnailCache, ThumbnailLoader (Glide)
│   ├── upload/                  UploadWorker, UploadScheduler, MediaStoreObserver, UploadNotification
│   ├── camera/                  CameraIntentHandler
│   └── data/                    Room DB (GalleryDatabase, PhotoDao), model/Photo, repo/GalleryRepository
│
└── ca/pkay/rcloneexplorer/      ← LEGACY: kept for rclone wrappers + WorkManager infra
    ├── RcloneRcd.java    rclone CLI/API interface
    ├── Services/RcdService.java       rclone daemon lifecycle
    ├── BroadcastReceivers/            BootReciever
    └── util/                          FLog, NotificationUtils, WifiConnectivityUtil, etc.
```

**Key files that are NOT gallery code but must not be deleted:**
- `ca.pkay.rcloneexplorer.RcloneRcd` — all rclone operations go through this
- `ca.pkay.rcloneexplorer.Services.RcdService` — manifest-declared, required by rclone daemon
- `ca.pkay.rcloneexplorer.BroadcastReceivers.BootReciever` — schedules uploads on boot

## Crypt Flow

1. User configures a real rclone backend + crypt password/salt in onboarding → stored in `CryptKeyStore` (EncryptedSharedPreferences)
2. `MediaStoreObserver` watches for new photos → `UploadWorker` enqueues them
3. Worker reads source → generates encrypted thumbnail (512px JPEG) → uploads both via `RcloneCryptProvider` (rclone crypt remote)
4. Grid displays thumbnails: `ThumbnailLoader` (Glide ModelLoader) fetches `.enc` thumbnail → decrypts in-memory → returns Bitmap

## Features

- **Encrypted uploads**: All photos encrypted client-side via rclone crypt before upload
- **Thumbnail encryption**: Thumbnails encrypted separately for fast preview
- **Wi-Fi only mode**: Configurable network constraints
- **Background upload**: WorkManager-based with retry policy
- **Camera integration**: Direct capture with system camera
- **MediaStore sync**: Automatic detection of new photos
- **Notifications**: Upload progress, completion, and error notifications

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
