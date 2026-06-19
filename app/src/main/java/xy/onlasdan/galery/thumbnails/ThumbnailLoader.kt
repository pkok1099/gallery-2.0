package xy.onlasdan.galery.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import ca.pkay.rcloneexplorer.util.FLog
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Glide ModelLoader that fetches an encrypted thumbnail from cloud via rclone,
 * decrypts it in-memory, and returns a Bitmap.
 */
class ThumbnailLoader(private val context: Context) : com.bumptech.glide.load.model.ModelLoader<String, Bitmap> {

    private val TAG = "ThumbnailLoader"
    private val provider = RcloneCryptProvider(context)

    override fun buildLoadData(model: String, width: Int, height: Int, options: com.bumptech.glide.load.Options): com.bumptech.glide.load.model.ModelLoader.LoadData<Bitmap>? {
        val thumbRemotePath = "${model}.thumb"
        val cacheKey = md5(thumbRemotePath)
        return com.bumptech.glide.load.model.ModelLoader.LoadData(
            com.bumptech.glide.load.Key { cacheKey.toByteArray() },
            ThumbFetcher(thumbRemotePath),
        )
    }

    override fun handles(model: String) = model.endsWith(".enc")

    inner class ThumbFetcher(private val remotePath: String) : DataFetcher<Bitmap> {
        private var cancelled = false

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
            try {
                // Initialize rclone if needed
                if (!provider.isConfigured()) {
                    provider.initRcd()
                    Thread.sleep(1000)
                }

                // Download encrypted thumbnail to temp file
                val tempFile = File.createTempFile("thumb_", ".enc", context.cacheDir)
                val success = provider.download(remotePath, tempFile)

                if (success && tempFile.exists()) {
                    // Decrypt and decode the thumbnail
                    val decryptedBytes = decryptFile(tempFile)
                    val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
                    tempFile.delete()

                    if (bitmap != null) {
                        callback.onDataReady(bitmap)
                    } else {
                        callback.onLoadFailed(Exception("Failed to decode thumbnail"))
                    }
                } else {
                    tempFile.delete()
                    callback.onLoadFailed(Exception("Failed to download thumbnail"))
                }
            } catch (e: Exception) {
                FLog.e(TAG, "Thumbnail load failed: ${e.message}")
                callback.onLoadFailed(e)
            }
        }

        override fun cleanup() {
            // Clean up any temp files if needed
        }

        override fun cancel() {
            cancelled = true
        }

        override fun getDataClass() = Bitmap::class.java
        override fun getDataSource() = DataSource.REMOTE
    }

    /**
     * Decrypt a file using rclone crypt.
     * For now, this is a placeholder - actual decryption will be handled by rclone.
     */
    private fun decryptFile(encryptedFile: File): ByteArray {
        // The file is already decrypted by rclone during download
        // Just read the bytes
        return encryptedFile.readBytes()
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
