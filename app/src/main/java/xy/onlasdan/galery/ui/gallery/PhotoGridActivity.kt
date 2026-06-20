package xy.onlasdan.galery.ui.gallery

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main gallery grid: shows encrypted thumbnails decrypted on-the-fly.
 */
class PhotoGridActivity : AppCompatActivity() {

    private val TAG = "PhotoGridActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCamera: FloatingActionButton
    private var repository: GalleryRepository? = null
    private var mediaStoreObserver: MediaStoreObserver? = null
    private lateinit var adapter: PhotoAdapter
    private val scope = CoroutineScope(Dispatchers.Main)

    private val CAMERA_PERMISSION_REQUEST = 1001
    private val CAMERA_CAPTURE_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_grid)

        recyclerView = findViewById(R.id.recycler_view)
        fabCamera = findViewById(R.id.fab_camera)

        // Initialize repository in background to avoid main thread DB access
        scope.launch {
            try {
                repository = withContext(Dispatchers.IO) {
                    GalleryRepository(this@PhotoGridActivity)
                }
                mediaStoreObserver = MediaStoreObserver(this@PhotoGridActivity)
                loadPhotos()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to init repository", e)
                Toast.makeText(this@PhotoGridActivity, "Init error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        setupRecyclerView()
        setupCameraButton()
    }

    override fun onResume() {
        super.onResume()
        mediaStoreObserver?.register()
        if (repository != null) {
            loadPhotos()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaStoreObserver?.unregister()
    }

    private fun setupRecyclerView() {
        adapter = PhotoAdapter { photo ->
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
        val repo = repository ?: return
        scope.launch {
            try {
                val photos = withContext(Dispatchers.IO) { repo.allPhotos() }
                adapter.updatePhotos(photos)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading photos", e)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CameraIntentHandler.launch(this, CAMERA_CAPTURE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            UploadScheduler.schedule(this)
        }
    }
}
