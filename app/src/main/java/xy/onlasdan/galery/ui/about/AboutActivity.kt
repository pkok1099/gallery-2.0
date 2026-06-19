package xy.onlasdan.galery.ui.about

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R

/**
 * About screen with app credits and information.
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val versionText = findViewById<TextView>(R.id.text_version)
        versionText.text = "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
    }
}
