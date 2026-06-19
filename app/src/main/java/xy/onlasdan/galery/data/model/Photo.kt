package xy.onlasdan.galery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localPath: String,       // device source path (original before encrypt)
    val remotePath: String,      // cloud path e.g. "2025/06/abc.enc"
    val mimeType: String,
    val size: Long,
    val timestamp: Long,         // epoch millis from MediaStore
    val status: UploadStatus = UploadStatus.PENDING,
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED,
}
