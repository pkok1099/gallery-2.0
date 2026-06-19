package xy.onlasdan.galery.ui.setup

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R
import xy.onlasdan.galery.crypto.CryptKeyStore
import xy.onlasdan.galery.crypto.RcloneCryptProvider
import xy.onlasdan.galery.ui.gallery.PhotoGridActivity
import android.content.Intent

/**
 * Select cloud backend (S3/Drive/Dropbox/etc.) then configure crypt password + salt.
 */
class BackendPickerActivity : AppCompatActivity() {

    private val backends = arrayOf(
        "S3 (Amazon S3, Backblaze B2, etc.)",
        "Google Drive",
        "Dropbox",
        "WebDAV",
        "Local Storage"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backend_picker)

        val listView = findViewById<ListView>(R.id.list_backends)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, backends)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            showPasswordDialog(position)
        }
    }

    private fun showPasswordDialog(backendIndex: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_generator, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.edit_text_password)
        val saltInput = dialogView.findViewById<EditText>(R.id.edit_text_salt)

        // Set random password and salt
        val randomPassword = generateRandomString(32)
        val randomSalt = generateRandomString(32)
        passwordInput.setText(randomPassword)
        saltInput.setText(randomSalt)

        AlertDialog.Builder(this)
            .setTitle("Configure Encryption")
            .setMessage("Set your encryption password and salt. Save these securely!")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val password = passwordInput.text.toString()
                val salt = saltInput.text.toString()

                if (password.isNotEmpty() && salt.isNotEmpty()) {
                    // Save to encrypted preferences
                    val cryptStore = CryptKeyStore(this)
                    cryptStore.save(password, salt)

                    // Setup crypt remote
                    val provider = RcloneCryptProvider(this)
                    provider.initRcd()
                    provider.setupCryptRemote(password, salt)

                    Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show()

                    // Navigate to gallery
                    startActivity(Intent(this, PhotoGridActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Password and salt cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
