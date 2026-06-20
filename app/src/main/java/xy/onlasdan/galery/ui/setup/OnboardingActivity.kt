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
 * If already configured, redirects to gallery.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnStartSetup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already configured, go straight to gallery
        val cryptStore = CryptKeyStore(this)
        if (cryptStore.isConfigured()) {
            navigateToGallery()
            return
        }

        setContentView(R.layout.activity_onboarding)

        btnStartSetup = findViewById(R.id.btn_start_setup)

        btnStartSetup.setOnClickListener {
            startActivity(Intent(this, BackendPickerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
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
