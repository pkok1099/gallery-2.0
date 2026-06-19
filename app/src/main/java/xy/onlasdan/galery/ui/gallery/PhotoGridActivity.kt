package xy.onlasdan.galery.ui.gallery

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xy.onlasdan.galery.camera.CameraIntentHandler
import xy.onlasdan.galery.data.repo.GalleryRepository
import xy.onlasdan.galery.upload.UploadScheduler
import xy.onlasdan.galery.upload.MediaStoreObserver
import xy.onlasdan.galery.ui.setup.OnboardingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main gallery grid: shows encrypted thumbnails decrypted on-the-fly.
 */
class PhotoGridActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCamera: FloatingActionButton
    private lateinit var repository: GalleryRepository
    private lateinit var mediaStoreObserver: MediaStoreObserver
    private lateinit var adapter: PhotoAdapter
    private val scope = CoroutineScope(Dispatchers.Main)

    private val CAMERA_PERMISSION_REQUEST = 1001
    private val CAMERA_CAPTURE_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_grid)

        repository = GalleryRepository(this)
        mediaStoreObserver = MediaStoreObserver(this)

        recyclerView = findViewById(R.id.recycler_view)
        fabCamera = findViewById(R.id.fab_camera)

        setupRecyclerView()
        setupCameraButton()
        loadPhotos()

        // Start upload scheduler
        UploadScheduler.schedule(this)
    }

    override fun onResume() {
        super.onResume()
        mediaStoreObserver.register()
        loadPhotos()
    }

    override fun onPause() {
        super.onPause()
        mediaStoreObserver.unregister()
    }

    private fun setupRecyclerView() {
        adapter = PhotoAdapter { photo ->
            // Navigate to photo detail
            val intent = Intent(this, PhotoDetailActivity::class.java)
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_PATH, photo.remotePath)
            startActivity(intent)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }

    private fun setupCameraButton() {
        fabCamera.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                CameraIntentHandler.launch(this, CAMERA_CAPTURE_REQUEST)
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            }
        }
    }

    private fun loadPhotos() {
        scope.launch {
            try {
                val photos = repository.allPhotos()
                adapter.updatePhotos(photos)
                if (photos.isEmpty()) {
                    Toast.makeText(this@PhotoGridActivity, "No photos yet. Tap camera to add.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PhotoGridActivity, "Error loading photos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CameraIntentHandler.launch(this, CAMERA_CAPTURE_REQUEST)
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            // Photo captured, MediaStoreObserver will pick it up
            UploadScheduler.schedule(this)
        }
    }
}
