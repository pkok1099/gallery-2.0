package xy.onlasdan.galery.crypto

import androidx.annotation.StringRes

data class BackendConfig(
    val type: Type,
    val accessKey: String = "",
    val secretKey: String = "",
    val endpoint: String = "",
) {
    enum class Type {
        S3,
        GDRIVE,
        DROPBOX,
        WEBDAV,
        LOCAL,
    }
}
