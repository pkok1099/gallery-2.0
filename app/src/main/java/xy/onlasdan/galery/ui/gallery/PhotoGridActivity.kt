package xy.onlasdan.galery.ui.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R

/**
 * Main gallery grid: shows encrypted thumbnails decrypted on-the-fly.
 */
class PhotoGridActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_grid)
    }
}
