package xy.onlasdan.galery.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import xy.onlasdan.galery.data.db.GalleryDatabase
import xy.onlasdan.galery.thumbnails.ThumbnailEncryptor
import java.io.File

/**
 * WorkManager worker: picks up pending uploads from the DB,
 * encrypts photo + thumbnail, uploads both via rclone crypt remote.
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = GalleryDatabase.getInstance(applicationContext)
        val provider = RcloneCryptProvider(applicationContext)
        val pending = db.photoDao().pending()
        for (photo in pending) {
            val src = File(photo.localPath)
            if (!src.exists()) { db.photoDao().markFailed(photo.id); continue }
            val thumb = ThumbnailEncryptor.downscaleFromBytes(src.readBytes())
            val ok = provider.upload(src, photo.remotePath) &&
                     provider.upload(thumb, "${photo.remotePath}.thumb")
            if (ok) db.photoDao().markUploaded(photo.id) else db.photoDao().markFailed(photo.id)
        }
        return Result.success()
    }
}
