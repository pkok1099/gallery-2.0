package xy.onlasdan.galery.upload

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore

/**
 * Watches MediaStore.Images and .Video for new rows.
 * When new media appears, enqueues it for encrypted upload.
 */
class MediaStoreObserver(context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val appContext = context.applicationContext

    override fun onChange(selfChange: Boolean) {
        // TODO: scan MediaStore for new rows, insert into Room DB as pending uploads
        UploadScheduler.schedule(appContext)
    }

    fun register() {
        appContext.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            this,
        )
        appContext.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            this,
        )
    }

    fun unregister() {
        appContext.contentResolver.unregisterContentObserver(this)
    }
}
