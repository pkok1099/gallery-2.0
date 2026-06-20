
  # Gallery Encrypted untuk Round-Sync (Final — PhotoView dep corrected)

  ## Summary

  Gallery terenkripsi di Round-Sync. File asli tetap individual via rclone crypt (existing
  remote). Thumbnail disimpan dalam pack terenkripsi per-bin (100MB chunks, satu unit encrypted
  blob) di remote root, dengan manifest index terenkripsi terpisah. Gallery baca thumbnail dari
  pack (decrypt sekali per session ke app sandbox). Video play via ExoPlayer (Media3 1.6.1)
  langsung ke rclone crypt serve HTTP. Thumbnail generation lazy (on first view), upload via
  WorkManager 6 jam atau on-resume.

  Session model: Master key di-cache in-memory selama app process hidup. Wrapped master key
  disimpan di EncryptedSharedPreferences (androidx.security:security-crypto:1.1.0, deprecated
  tapi functional per user choice). Auto-unlock via BiometricPrompt.

  ## Key Changes

  ### 1. Thumbnail Pack System (NEW) — per-bin encryption

  Pack file di remote root: pack_<8-byte-hex>.enc

  File body (single AES-256-GCM encryption unit):
    [magic:4 "RSPK"][version:1 = 0x01][reserved:3 = 0x00][salt:32][nonce:12][ciphertext:NB]
    [auth_tag:16]
    key      = HKDF-SHA256(master_key, salt=pack_id_raw_bytes(8), info="pack_v1", L=32)
    nonce    = random 12 bytes
    plaintext = concatenated_entry_blobs (NOT individually encrypted)

  Per-entry plaintext format:
    [magic:2 "TE"][entry_size:int32 LE][mime_type_byte:1][data_size:int32 LE][jpg_bytes...]
    mime_type_byte: 0x01=image/jpeg, 0x02=video/jpg_frame

  - pack_id = 8 bytes raw random. Filename = hex(pack_id). HKDF salt = pack_id raw bytes (NOT
    hex).

  - Single encryption per pack — one salt, one nonce, one AEAD seal covers entire pack body.

  Manifest di remote root: manifest.json.enc

  File body:
    [magic:4 "RSMN"][version:1 = 0x01][reserved:3 = 0x00][salt:32][nonce:12][ciphertext]
    [auth_tag:16]
    key   = HKDF-SHA256(master_key, salt=random 32 bytes, info="manifest_v1", L=32)
    plaintext = UTF-8 JSON:
      {
        "version": 1,
        "last_modified": <unix_ms>,
        "packs": [...],
        "files": [
          {
            "file_hash": "<sha256-hex>",
            "remote_path": "<path on rclone crypt remote>",
            "file_size": <int>,
            "file_mtime": <unix_ms>,
            "file_type": "image" | "video",
            "pack_id_hex": "<hex>",
            "entry_offset": <int within pack plaintext>,
            "entry_size": <int>
          }
        ]
      }

  gallery.key di remote root — corrected key flow:

  File body:
    [magic:4 "RSGK"][version:1 = 0x01][reserved:3 = 0x00][argon2_salt:16][argon2_hash:32]
    [nonce:12][ciphertext:32][auth_tag:16]

  Derivation flow (corrected per spec):
    1. derived_key = Argon2id(password_utf8_bytes,
                               salt=argon2_salt,
                               memory=64MB, iterations=3, parallelism=4,
                               outputLen=32)
       # Argon2id output IS the AES-256 key DIRECTLY.
       # NO additional HKDF — Argon2id already provides
       # key stretching + uniform distribution.

    2. AES-256-GCM encrypt:
         key      = derived_key (the Argon2id output)
         nonce    = random 12 bytes
         plaintext = master_key (32 random bytes, generated once at setup)
         produces: ciphertext + auth_tag

  Verification: Argon2id.verify(password, argon2_salt, stored_argon2_hash) must return true.
  Wrong password → reject decryption.

  ### 2. Key Management (Per-session + biometric auto-unlock)

  - First-time setup (extend OnboardingActivity):
      1. User sets gallery password
      2. Generate random 32-byte master_key (SecureRandom)
      3. Generate 16-byte argon2_salt (SecureRandom)
      4. derived_key = Argon2id(password, argon2_salt) (32 bytes — used directly as AES key)
      5. argon2_hash = Argon2id.hash(password, argon2_salt) for verification
      6. Encrypt master_key with AES-256-GCM(key=derived_key, nonce=random)
      7. Write gallery.key to remote crypt root (atomic write-rename via rclone crypt)
      8. Wrap master_key with Android Keystore non-extractable AES-256 GCM key → store
         ciphertext in EncryptedSharedPreferences["master_key_wrapped"]

  - Per-session unlock:
      1. App launch → if EncryptedSharedPreferences["master_key_wrapped"] exists → trigger
         BiometricPrompt

      2. Biometric success → unwrap → master_key in memory
      3. Biometric fail / no wrapped key → GalleryAuthActivity password prompt → download
         gallery.key from remote → Argon2id verify → decrypt master_key → offer wrap & save

  - Master key lifetime: cleared on app process death, user "Lock gallery" action, logout
    (destroy wrapped key + clear memory)

  - New device recovery: download gallery.key → password prompt → Argon2id verify → decrypt
    master_key → ask "Save biometric unlock on this device?" → optional wrap+save via Keystore

  - Deprecation note: androidx.security:security-crypto:1.1.0 EncryptedSharedPreferences API is
    deprecated per Google recommendation. Tetap dipakai per user choice — zero migration
    effort, functional solution. Documented as tech debt.

  Dependencies:

  - com.lambdapioneer.argon2kt:argon2kt:1.6.0 for Argon2id (NOT androidx.security which lacks
    Argon2id)

  - androidx.security:security-crypto:1.1.0 (deprecated but functional) for
    EncryptedSharedPreferences

  - androidx.biometric:biometric:1.1.0 (latest stable, no newer stable; 1.4.0-alpha05 not
    recommended for production)

  ### 3. Thumbnail Generation (lazy, JPG universal)

  - Saat FileExplorerRecyclerViewAdapter bind image/video item + showThumbnails=true:
      1. file_hash = SHA-256(file_bytes). First-view: download via rclone crypt serve byte-
         range, hash + cache file_hash → file_size/mtime.

      2. Check local cache: /data/data/.../cache/thumbs/<file_hash>.jpg
      3. If miss → generate thumbnail:
          - Image: BitmapFactory.decodeStream → scale to fit 320x320 (aspect-preserved) →
            Bitmap.compress(JPEG, 80)

          - Video: MediaMetadataRetriever.setDataSource(http_url_to_original) →
            getFrameAtTime(0) → scale → compress

      4. Save plain JPG to local cache
      5. Stage entry to current active pack buffer
      6. Glide load from file:///data/data/.../cache/thumbs/<file_hash>.jpg

  - Pack flush triggers (any one):
      - Buffer ≥ 100MB
      - Buffer > 50MB AND app going to background
      - WorkManager periodic sync (6h)
      - On-resume if unsynced > 0
      - Manual "Sync gallery now"

  ### 4. Sync Pipeline (WorkManager)

  - PackSyncWorker.kt (CoroutineWorker):
      1. Read in-memory master_key from KeyManager.session
      2. Scan local thumbs vs manifest (files in cache/thumbs/ not referenced by manifest)
      3. Build current pack buffer (or new pack if existing + new > 100MB)
      4. Concatenate entry plaintexts (NOT individually encrypted)
      5. Encrypt pack (single AES-256-GCM with HKDF key from pack_id_bytes)
      6. Atomic upload via rclone:
          - Write pack_<hex>.tmp → on success, rename pack_<hex>.enc
          - Same write-rename for manifest.json.enc

      7. Update unsyncedCount via MutableSharedFlow in GalleryRepository

  - Scheduling:
      - PeriodicWorkRequest every 6h, constraint NetworkType.CONNECTED
      - OneTimeWorkRequest from MainActivity.onResume() if unsyncedCount > 0
      - Manual trigger from menu

  - Dashboard badge: MainActivity collects unsyncedCount dari GalleryRepository.unsyncedState.

  ### 5. Gallery View (NEW activities)

  - GalleryActivity.kt: RecyclerView grid 3 kolom (GridLayoutManager). Loads from
    GalleryRepository.observeFiles() — decrypts manifest on create.

  - GalleryAdapter.kt: binds thumb via Glide dari local cache
    file://.../cache/thumbs/<hash>.jpg. Click → MediaPreviewActivity.

  - MediaPreviewActivity.kt:
      - Image: full-screen view via io.getstream:photoview:1.0.3 (actively maintained GetStream
        fork; replaces com.github.chrisbanes:PhotoView:2.3.0 unmaintained since 2019). API
        compatible, native Glide support.

      - Video: ExoPlayer via androidx.media3:media3-exoplayer:1.6.1 + media3-ui:1.6.1.
        DefaultHttpDataSource.Factory points ke:

        http://127.0.0.1:{crypt_serve_port}/{crypt_auth}/{remote_name}/{filename}
        via existing Rclone.serve(SERVE_PROTOCOL_HTTP, port, false, null, null, remote, "",
        baseUrl) with --baseurl /<crypt_auth>/<remote_name>. Byte-range enabled.

      - Auto-start playback (no seek requirement satisfied).

  ### 6. Service Modernization (immediate fix)

  - ThumbnailsLoadingService.java → rewrite as ForegroundService:
      - Extend Service, NOT IntentService (deprecated API 30+, target SDK 34 tidak reliable)
      - onCreate: create low-priority notification
      - onStartCommand: startForeground(NOTIF_ID, notification,
        FOREGROUND_SERVICE_TYPE_DATA_SYNC)

      - Handle intent extras in onStartCommand
      - Run rclone.serve(...) in HandlerThread/coroutine
      - Fixes existing thumbnail bug + foundation for gallery rclone crypt serve.

  ## Test Plan

  Unit tests (app/src/test/java/):

  - PackCodecTest: 100 random entries → encrypt entire pack (per-bin, single salt+nonce) →
    decrypt → byte equality.

  - ManifestCodecTest: serialize → encrypt (manifest_v1 HKDF info) → decrypt → JSON equality.
    Empty, 1 file, 1000 files.

  - KeyManagerTest:
      - Argon2id roundtrip: same password+salt → same 32-byte output
      - Argon2id verify: correct password true, wrong password false
      - Gallery.key: magic "RSGK" + version 0x01 verified
      - Corrected derivation flow: derived_key = Argon2id(password, salt) used DIRECTLY as AES-
        256 key (NO HKDF). Decrypt → expected master_key.

  - ThumbnailGeneratorTest: image generation, video frame extraction, dimension scaling.
  - PackSyncWorkerTest: atomic write-rename simulation, network failure rollback.

  Integration tests (manual):

  1. Setup: install fresh, complete gallery wizard. Verify gallery.key uploaded to remote crypt
     root.

  2. Upload + thumbnail: 50 image + 5 video files. Open gallery → all thumbs visible. Kill app,
     relaunch → biometric unlock → thumbs persist.

  3. Cross-device: second device, same password → gallery downloads manifest → thumbs visible.
  4. Wrong password: graceful error, no data leak.
  5. Mid-sync failure: kill app during pack upload → relaunch → no corrupt packs, manifest
     consistent.

  6. Video play: tap video → ExoPlayer auto-plays to end.
  7. Existing thumbnail bug regression: gallery disabled, file explorer thumbnails work after
     Service → ForegroundService rewrite.

  8. EncryptedSharedPreferences deprecation: app functional with deprecated API, no runtime
     errors.

  Stress:

  - 1000+ files gallery scroll
  - 100MB pack upload resume after network drop
  - Background sync with battery saver

  ## Files to Touch

  Build (app/build.gradle):

  - androidx.media3:media3-exoplayer:1.6.1
  - androidx.media3:media3-ui:1.6.1
  - com.lambdapioneer.argon2kt:argon2kt:1.6.0
  - androidx.security:security-crypto:1.1.0 (deprecated, used per user choice)
  - androidx.biometric:biometric:1.1.0 (latest stable)
  - io.getstream:photoview:1.0.3 (replaces com.github.chrisbanes:PhotoView:2.3.0)

  New package ca.pkay.rcloneexplorer.gallery/ (all new):

  - KeyManager.kt — master_key, Argon2id (NO HKDF on password), session state
  - PackCodec.kt — AES-256-GCM pack encrypt/decrypt (per-bin)
  - ManifestCodec.kt — manifest JSON + encrypt/decrypt
  - ThumbnailGenerator.kt — image/video extraction
  - PackSyncWorker.kt — WorkManager CoroutineWorker
  - GalleryRepository.kt — orchestration + unsyncedState SharedFlow
  - GalleryAuthActivity.kt — biometric + password entry

  Modified:

  - app/src/main/java/ca/pkay/rcloneexplorer/Services/ThumbnailsLoadingService.java — convert
    IntentService → ForegroundService

  - app/src/main/java/ca/pkay/rcloneexplorer/Activities/MainActivity.java — add Gallery tab +
    dashboard badge

  - app/src/main/java/ca/pkay/rcloneexplorer/Activities/OnboardingActivity.kt — extend with
    gallery wizard

  - app/src/main/java/ca/pkay/rcloneexplorer/RecyclerViewAdapters/
    FileExplorerRecyclerViewAdapter.java — gallery thumb loading

  - app/src/main/res/layout/activity_gallery.xml (new)
  - app/src/main/res/layout/activity_media_preview.xml (new)
  - app/src/main/res/layout/item_gallery.xml (new)
  - app/src/main/res/xml/settings_general_preferences.xml — add "Enable gallery" toggle
  - app/src/main/AndroidManifest.xml — register GalleryActivity, MediaPreviewActivity,
    GalleryAuthActivity

  - app/src/main/res/values/strings.xml — gallery strings

  ## Assumptions & Defaults

  - User must have rclone crypt remote configured before gallery enabled. Gallery tab hidden +
    onboarding prompt if no crypt remote exists.

  - Master key recovery: lupa password = data unrecoverable, no backdoor. Displayed clearly di
    setup wizard dan Settings (clear warning).

  - Pack size: 100MB hard limit. Flush trigger lebih awal kalau buffer > 50MB.
  - Video codec: ExoPlayer default H.264/H.265/AAC/AV1. No transcoding. Codec unsupported →
    error toast, user can download original via rclone.

  - No seek requirement: confirmed by user. Byte-range tetap enabled (zero cost extra, future-
    proof).

  - Thumbnail dimensions: 320x320 px max edge, JPEG quality 80. ~5-15KB per thumb.
  - Local cache location: app sandbox /data/data/.../cache/thumbs/. Plain JPG (decrypted).
    System cleans on storage pressure.

  - Network requirement: gallery functional dengan crypt remote reachable. Offline → use local
    cache only (unviewed files show placeholder).

  - Existing thumbnail bug: independent fix included (Service → ForegroundService). Bisa ship
    dulu sebagai point release sebelum gallery feature.

  - Gallery opt-in: feature flag in settings. Existing users unaffected sampai enable.
  - Manifest versioning: version: 1. Future migrations via new version + converter.
  - HKDF info strings (untuk derivation separation, used on master_key → per-data keys):
      - "pack_v1" for master_key → per-pack AES key
      - "manifest_v1" for master_key → manifest AES key

  - Argon2id parameters: memory=64MB, iterations=3, parallelism=4, outputLen=32. Tuned untuk
    ~500ms derivation time di mid-range Android device. Configurable di advanced settings jika
    device lambat.

  - EncryptedSharedPreferences deprecation: accepted tech debt per user choice. Zero functional
    impact, Google recommends Keystore direct but EncryptedSharedPreferences still maintained
    di v1.1.0 dan functional.

  - PhotoView library: io.getstream:photoview:1.0.3 (actively maintained GetStream fork,
    replaces com.github.chrisbanes:PhotoView:2.3.0 unmaintained since 2019). API compatible,
    supports Glide directly.