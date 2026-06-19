package xy.onlasdan.galery.crypto

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores rclone crypt password + salt in EncryptedSharedPreferences.
 * All access goes through this class — never read the keys directly.
 */
class CryptKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun save(password: String, salt: String) {
        prefs.edit()
            .putString(KEY_PASSWORD, password)
            .putString(KEY_SALT, salt)
            .apply()
    }

    fun password(): String = prefs.getString(KEY_PASSWORD, "") ?: ""
    fun salt(): String = prefs.getString(KEY_SALT, "") ?: ""
    fun isConfigured(): Boolean = password().isNotEmpty() && salt().isNotEmpty()

    companion object {
        private const val PREFS_NAME = "galery_crypt_keys"
        private const val KEY_PASSWORD = "crypt_password"
        private const val KEY_SALT = "crypt_salt"
    }
}
