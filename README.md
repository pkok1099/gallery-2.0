# Galery - Encrypted Cloud Gallery

An encrypted cloud gallery for Android. Photos are encrypted client-side via rclone crypt before upload; cloud stores only `.enc` blobs. All thumbnails are encrypted too.

## Features

- **End-to-End Encryption**: All photos encrypted client-side using rclone crypt (AES-256)
- **Encrypted Thumbnails**: Thumbnails encrypted separately for fast preview
- **Cloud Storage**: Upload to any rclone-supported backend (S3, Google Drive, Dropbox, WebDAV, etc.)
- **Background Upload**: WorkManager-based with retry policy
- **Wi-Fi Only Mode**: Configurable network constraints
- **Camera Integration**: Direct capture with system camera
- **MediaStore Sync**: Automatic detection of new photos
- **Material Design**: Modern UI with dark theme support

## Installation

Download the latest APK from [GitHub Releases](https://github.com/pkok1099/gallery-2.0/releases).

| CPU architecture | Where to find | APK identifier |
|:---|:--|:---:|
| ARM 32 Bit | older devices | `armeabi-v7a` |
| **ARM 64 Bit** | **most devices** | `arm64-v8a` |
| Intel/AMD 32 Bit | some TV boxes and tablets | `x86` |
| Intel/AMD 64 Bit | some emulators | `x86_64` |

If you don't know which version to pick, use `galery-<version>-universal-debug.apk`. Most devices run ARM 64 Bit. The app runs on Android 13 (API 33) or newer.

## How It Works

1. **Setup**: Configure a cloud backend (S3, Drive, etc.) and set encryption password/salt
2. **Capture**: Take photos with the built-in camera or let MediaStore detect new photos
3. **Encrypt**: Photos are encrypted client-side using rclone crypt before upload
4. **Upload**: Encrypted files are uploaded to your cloud storage
5. **View**: Encrypted thumbnails are decrypted on-the-fly for preview

## Architecture

```
app/src/main/java/
├── xy/onlasdan/galery/
│   ├── ui/                 # Activities (Gallery, Setup, Settings, About)
│   ├── crypto/             # Encryption key storage and rclone integration
│   ├── thumbnails/         # Encrypted thumbnail pipeline
│   ├── upload/             # WorkManager workers and scheduler
│   ├── camera/             # Camera integration
│   └── data/               # Room database and models
│
└── ca/pkay/rcloneexplorer/ # Legacy rclone infrastructure
    ├── RcloneRcd.java      # rclone API wrapper
    ├── Services/           # rclone daemon lifecycle
    └── util/               # Utilities
```

## Building

**Build via GitHub Actions only.** Local builds not supported for full APK.

```sh
# Trigger build
gh workflow run android.yml --ref main

# Check status
gh run list --workflow=android.yml

# Download artifacts
gh run download <run-id>
```

### Local Development

For code validation only (no full APK):

```sh
# Lint check
./gradlew lint -x :rclone:buildAll

# Unit tests
./gradlew test

# Check compilation
./gradlew compileDebugSources -x :rclone:buildAll
```

## Dependencies

- [rclone](https://rclone.org/) - Cloud storage sync engine
- [Room](https://developer.android.com/jetpack/androidx/releases/room) - Local database
- [SQLCipher](https://github.com/nicbell/CipherRoom) - Encrypted database
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Background tasks
- [Glide](https://github.com/bumptech/glide) - Image loading
- [OkHttp](https://square.github.io/okhttp/) - HTTP client

## Security

- All encryption happens client-side before upload
- Crypt keys stored in `EncryptedSharedPreferences`
- Local database encrypted with SQLCipher
- Cloud provider only sees encrypted blobs
- No plaintext data leaves your device

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This app is licensed under the GPLv3 License. See [LICENSE](LICENSE) for details.

## Acknowledgments

Built on top of [rclone](https://rclone.org/) - the amazing cloud storage sync tool.
Originally based on [Round Sync](https://github.com/newhinton/Round-Sync) by newhinton.
