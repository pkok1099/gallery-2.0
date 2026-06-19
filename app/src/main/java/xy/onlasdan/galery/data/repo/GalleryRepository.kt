package xy.onlasdan.galery.data.repo

import android.content.Context
import xy.onlasdan.galery.data.db.GalleryDatabase
import xy.onlasdan.galery.data.model.Photo
import xy.onlasdan.galery.data.model.UploadStatus

/**
 * Single source of truth for photo data.
 */
class GalleryRepository(context: Context) {

    private val dao = GalleryDatabase.getInstance(context).photoDao()

    suspend fun allPhotos(): List<Photo> = dao.all()
    suspend fun pending(): List<Photo> = dao.pending()
    suspend fun insert(photo: Photo): Long = dao.upsert(photo)
    suspend fun markUploaded(id: Long) = dao.markUploaded(id)
    suspend fun markFailed(id: Long) = dao.markFailed(id)
    suspend fun delete(id: Long) = dao.delete(id)
}
