package xy.onlasdan.galery.thumbnails

import android.graphics.Bitmap
import android.util.LruCache

/**
 * In-memory LRU cache for decrypted thumbnails.
 * Keyed by remote path (cloud path without the .enc suffix).
 */
object ThumbnailCache {

    private val cache = object : LruCache<String, Bitmap>(maxSize(50)) {
        override fun sizeOf(key: String, value: Bitmap) = 1
    }

    fun get(remotePath: String): Bitmap? = cache.get(remotePath)
    fun put(remotePath: String, bitmap: Bitmap) { cache.put(remotePath, bitmap) }
    fun evict() { cache.evictAll() }

    private fun maxSize(approxMb: Int): Int {
        // Each entry counts as 1; maxSize limits count, not bytes
        return approxMb
    }
}
