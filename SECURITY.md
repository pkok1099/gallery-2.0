# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within Galery, please send an email to the maintainers. All security vulnerabilities will be promptly addressed.

**Please do NOT report security vulnerabilities through public GitHub issues.**

### What to include

When reporting a vulnerability, please include:

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

### Response timeline

- We will acknowledge receipt within 48 hours
- We will provide an initial assessment within 1 week
- We will work with you to understand and address the issue

## Security Features

### Client-Side Encryption

- All photos are encrypted client-side before upload
- Uses rclone crypt with AES-256 encryption
- Encryption keys never leave your device
- Cloud provider only sees encrypted blobs

### Key Storage

- Crypt keys stored in `EncryptedSharedPreferences`
- Uses Android Keystore for key protection
- Keys are tied to device and user authentication

### Data at Rest

- Local database encrypted with SQLCipher
- Temporary files cleaned up after use
- No plaintext data stored on device

### Network Security

- All communication uses HTTPS
- Certificate pinning for rclone API calls
- Configurable network constraints (Wi-Fi only mode)

## Best Practices

### For Users

1. **Keep your encryption password safe** - If you lose it, you cannot recover your photos
2. **Use a strong password** - Generate a random password and salt
3. **Back up your keys** - Store your encryption keys securely
4. **Keep the app updated** - Install security updates promptly

### For Developers

1. **Never commit secrets** - Use environment variables or secure storage
2. **Validate inputs** - Sanitize all user inputs
3. **Use secure defaults** - Enable security features by default
4. **Follow OWASP guidelines** - Refer to OWASP Mobile Security

## Cryptographic Details

### Encryption Algorithm

- **Algorithm**: AES-256-GCM
- **Key Derivation**: rclone crypt (based on PBKDF2)
- **Salt**: 32-byte random salt
- **IV**: Random initialization vector per file

### File Structure

```
Encrypted File = [IV (12 bytes)] [Encrypted Data] [Auth Tag (16 bytes)]
```

### Thumbnail Encryption

- Thumbnails encrypted separately from original
- Same encryption algorithm as original files
- Stored alongside encrypted originals

## Compliance

- **GDPR**: No personal data collected or stored
- **HIPAA**: Encryption meets HIPAA requirements for data at rest
- **SOC 2**: Encryption and access controls implemented

## Updates

This security policy is subject to change. We will notify users of any significant updates through:
- GitHub releases
- In-app notifications
- Documentation updates

Last updated: 2024
