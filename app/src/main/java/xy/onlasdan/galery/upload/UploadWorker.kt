package xy.onlasdan.galery.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.pkay.rcloneexplorer.util.FLog
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import xy.onlasdan.galery.data.db.GalleryDatabase
import xy.onlasdan.galery.thumbnails.ThumbnailEncryptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * WorkManager worker: picks up pending uploads from the DB,
 * encrypts photo + thumbnail, uploads both via rclone crypt remote.
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val TAG = "UploadWorker"

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = GalleryDatabase.getInstance(applicationContext)
                val provider = RcloneCryptProvider(applicationContext)

                // Initialize rclone daemon
                provider.initRcd()

                // Wait for rclone to be ready
                Thread.sleep(2000)

                val pending = db.photoDao().pending()
                for (photo in pending) {
                    val src = File(photo.localPath)
                    if (!src.exists()) {
                        db.photoDao().markFailed(photo.id)
                        continue
                    }

                    // Create thumbnail and write to temp file
                    val thumbBytes = ThumbnailEncryptor.downscaleFromBytes(src.readBytes())
                    val thumbFile = File.createTempFile("thumb_", ".jpg", applicationContext.cacheDir)
                    FileOutputStream(thumbFile).use { it.write(thumbBytes) }

                    // Upload original and thumbnail
                    val uploadOk = provider.upload(src, photo.remotePath)
                    val thumbOk = provider.upload(thumbFile, "${photo.remotePath}.thumb")

                    // Clean up temp thumb file
                    thumbFile.delete()

                    if (uploadOk && thumbOk) {
                        db.photoDao().markUploaded(photo.id)
                    } else {
                        db.photoDao().markFailed(photo.id)
                    }
                }

                // Stop rclone when done
                provider.stopRcd()

                Result.success()
            } catch (e: Exception) {
                FLog.e(TAG, "Upload failed: ${e.message}")
                Result.failure()
            }
        }
        }
        return Result.success()
    }
}
