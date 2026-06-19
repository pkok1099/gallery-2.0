package xy.onlasdan.galery.ui.setup

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R

/**
 * First-run wizard: pick a cloud backend + set crypt password.
 */
class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }
}
