package xy.onlasdan.galery.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xy.onlasdan.galery.data.model.Photo

@Database(entities = [Photo::class], version = 1, exportSchema = false)
abstract class GalleryDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile private var INSTANCE: GalleryDatabase? = null

        fun getInstance(context: Context): GalleryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GalleryDatabase::class.java,
                    "galery.db",
                ).build().also { INSTANCE = it }
            }
    }
}
