package xy.onlasdan.galery.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ca.pkay.rcloneexplorer.RcloneRcd
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

/**
 * Glide ModelLoader that fetches an encrypted thumbnail from cloud via rclone,
 * decrypts it in-memory, and returns a Bitmap.
 */
class ThumbnailLoader(private val context: Context) : com.bumptech.glide.load.model.ModelLoader<String, Bitmap> {

    override fun buildLoadData(model: String, width: Int, height: Int, options: com.bumptech.glide.load.Options): com.bumptech.glide.load.model.ModelLoader.LoadData<Bitmap>? {
        val thumbRemotePath = "${model}.thumb.enc"
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
            // TODO: fetch encrypted thumbnail via rclone, decrypt, return bitmap
            // Placeholder: return transparent bitmap
            val bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            callback.onDataReady(bmp)
        }

        override fun cleanup() {}
        override fun cancel() { cancelled = true }
        override fun getDataClass() = Bitmap::class.java
        override fun getDataSource() = DataSource.REMOTE
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
