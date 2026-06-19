package xy.onlasdan.galery.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ca.pkay.rcloneexplorer.R
import xy.onlasdan.galery.upload.UploadScheduler

/**
 * Settings screen with preferences for upload behavior.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Wi-Fi only preference
            findPreference<SwitchPreferenceCompat>("wifi_only")?.setOnPreferenceChangeListener { _, newValue ->
                val wifiOnly = newValue as Boolean
                UploadScheduler.schedule(requireContext(), wifiOnly)
                true
            }
        }
    }
}
