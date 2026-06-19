package xy.onlasdan.galery.ui.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import com.bumptech.glide.Glide
import xy.onlasdan.galery.data.model.Photo
import xy.onlasdan.galery.thumbnails.ThumbnailLoader

/**
 * RecyclerView adapter for displaying encrypted photos.
 */
class PhotoAdapter(
    private var photos: List<Photo> = emptyList(),
    private val onItemClick: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        // Load thumbnail using our custom ThumbnailLoader
        Glide.with(holder.itemView.context)
            .load(photo.remotePath)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_photo)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(photo)
        }
    }

    override fun getItemCount(): Int = photos.size

    fun updatePhotos(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
