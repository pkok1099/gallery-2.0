package xy.onlasdan.galery.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Downscales a bitmap to a thumbnail then returns the bytes.
 * Encryption happens upstream — this class only resizes.
 */
object ThumbnailEncryptor {

    private const val MAX_SIZE = 512

    fun downscale(bitmap: Bitmap): ByteArray {
        val ratio = minOf(
            MAX_SIZE.toFloat() / bitmap.width,
            MAX_SIZE.toFloat() / bitmap.height,
            1f,
        )
        val newW = (bitmap.width * ratio).toInt()
        val newH = (bitmap.height * ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        return baos.toByteArray()
    }

    fun downscaleFromBytes(src: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(src, 0, src.size)
            ?: return byteArrayOf()
        return downscale(bitmap)
    }
}
