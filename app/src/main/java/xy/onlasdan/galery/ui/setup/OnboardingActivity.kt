package xy.onlasdan.galery.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R
import xy.onlasdan.galery.crypto.CryptKeyStore
import xy.onlasdan.galery.ui.gallery.PhotoGridActivity

/**
 * First-run wizard: pick a cloud backend + set crypt password.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnStartSetup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        btnStartSetup = findViewById(R.id.btn_start_setup)

        // Check if already configured
        val cryptStore = CryptKeyStore(this)
        if (cryptStore.isConfigured()) {
            // Already configured, go to gallery
            navigateToGallery()
            return
        }

        btnStartSetup.setOnClickListener {
            // TODO: Show backend selection dialog
            // For now, just navigate to backend picker
            startActivity(Intent(this, BackendPickerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if configuration was completed while we were away
        val cryptStore = CryptKeyStore(this)
        if (cryptStore.isConfigured()) {
            navigateToGallery()
        }
    }

    private fun navigateToGallery() {
        startActivity(Intent(this, PhotoGridActivity::class.java))
        finish()
    }
}
