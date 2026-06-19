package xy.onlasdan.galery.ui.gallery

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R
import com.bumptech.glide.Glide
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import xy.onlasdan.galery.data.model.Photo
import xy.onlasdan.galery.thumbnails.ThumbnailLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Full-res photo viewer: decrypts from cloud on demand.
 */
class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        imageView = findViewById(R.id.image_view)

        val photoPath = intent.getStringExtra(EXTRA_PHOTO_PATH)
        if (photoPath != null) {
            loadPhoto(photoPath)
        } else {
            Toast.makeText(this, "No photo path provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadPhoto(remotePath: String) {
        scope.launch {
            try {
                // Initialize rclone if needed
                val provider = RcloneCryptProvider(this@PhotoDetailActivity)
                if (!provider.isConfigured()) {
                    provider.initRcd()
                    withContext(Dispatchers.IO) {
                        Thread.sleep(1000)
                    }
                }

                // Download and decrypt the full photo
                val tempFile = File.createTempFile("photo_", ".jpg", cacheDir)
                val success = provider.download(remotePath, tempFile)

                if (success && tempFile.exists()) {
                    // Load into ImageView using Glide
                    Glide.with(this@PhotoDetailActivity)
                        .load(tempFile)
                        .into(imageView)
                } else {
                    Toast.makeText(this@PhotoDetailActivity, "Failed to load photo", Toast.LENGTH_SHORT).show()
                    finish()
                }

                // Clean up
                tempFile.delete()
            } catch (e: Exception) {
                Toast.makeText(this@PhotoDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_PHOTO_PATH = "photo_path"
    }
}
