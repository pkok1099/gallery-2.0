package xy.onlasdan.galery.data.db

import androidx.room.*
import xy.onlasdan.galery.data.model.Photo
import xy.onlasdan.galery.data.model.UploadStatus

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun pending(): List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: Photo): Long

    @Query("UPDATE photos SET status = 'UPLOADED' WHERE id = :id")
    suspend fun markUploaded(id: Long)

    @Query("UPDATE photos SET status = 'FAILED' WHERE id = :id")
    suspend fun markFailed(id: Long)

    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    suspend fun all(): List<Photo>

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)
}
