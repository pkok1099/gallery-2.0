# Contributing to Galery

Thank you for your interest in contributing to Galery! This document provides guidelines and information for contributors.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [GitHub Issues](https://github.com/pkok1099/gallery-2.0/issues)
2. If not, create a new issue with:
   - Clear description of the bug
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Device information (Android version, device model)

### Suggesting Features

1. Check existing feature requests in [GitHub Issues](https://github.com/pkok1099/gallery-2.0/issues)
2. Create a new issue with:
   - Clear description of the feature
   - Use case and benefits
   - Any implementation ideas

### Submitting Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes
4. Test thoroughly
5. Commit with clear messages: `git commit -m "Add: description of changes"`
6. Push to your fork: `git push origin feature/your-feature-name`
7. Create a Pull Request

## Development Setup

### Prerequisites

- Android Studio (latest stable)
- JDK 17
- Go 1.24+ (for rclone build)
- Android SDK with API 33

### Building

**Note:** Full APK builds are done via GitHub Actions only.

For local development:

```sh
# Lint check
./gradlew lint -x :rclone:buildAll

# Unit tests
./gradlew test

# Check compilation
./gradlew compileDebugSources -x :rclone:buildAll
```

### Code Style

- Follow Kotlin coding conventions for new code
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and concise

## Architecture

### Package Structure

```
app/src/main/java/
├── xy/onlasdan/galery/          # New gallery code
│   ├── ui/                      # Activities and UI components
│   ├── crypto/                  # Encryption and rclone integration
│   ├── thumbnails/              # Thumbnail pipeline
│   ├── upload/                  # Upload workers and scheduler
│   ├── camera/                  # Camera integration
│   └── data/                    # Database and models
│
└── ca/pkay/rcloneexplorer/      # Legacy rclone infrastructure
```

### Key Components

- **RcloneCryptProvider**: Handles all rclone operations
- **UploadWorker**: Background upload with WorkManager
- **ThumbnailLoader**: Glide ModelLoader for encrypted thumbnails
- **GalleryDatabase**: Room database with SQLCipher encryption

## Testing

### Unit Tests

```sh
./gradlew test
```

### Lint

```sh
./gradlew lint -x :rclone:buildAll
```

## Pull Request Guidelines

1. **Keep it focused**: One feature or fix per PR
2. **Test thoroughly**: Ensure all tests pass
3. **Update documentation**: If adding features, update README
4. **Follow code style**: Use consistent formatting
5. **Write clear commits**: Describe what and why

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Maintain a positive environment

## Questions?

If you have questions about contributing, feel free to open an issue or reach out to the maintainers.
