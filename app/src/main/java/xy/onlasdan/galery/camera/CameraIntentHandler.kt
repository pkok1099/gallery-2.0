package xy.onlasdan.galery.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Launches system camera, then writes captured photo to MediaStore
 * so MediaStoreObserver picks it up for encrypted upload.
 */
object CameraIntentHandler {

    private const val AUTHORITY_SUFFIX = ".export"

    fun launch(activity: Activity, requestCode: Int) {
        val appId = activity.packageName
        val photoFile = File(
            activity.cacheDir,
            "capture_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg",
        )
        val uri = FileProvider.getUriForFile(activity, "${appId}${AUTHORITY_SUFFIX}", photoFile)
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
        }
        activity.startActivityForResult(intent, requestCode)
    }

    /** Call in onActivityResult to write the cached capture to MediaStore. */
    fun handleResult(activity: Activity, data: Intent?) {
        // TODO: insert into MediaStore so observer triggers upload
    }
}
