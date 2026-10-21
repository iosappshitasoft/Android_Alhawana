package com.app.camera.audioimage

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.R


class SelectedImagesAdapter(
    private val images: MutableList<Uri>,
    private val onClick: (position: Int, uri: Uri) -> Unit,
    private val onRemove: (position: Int, uri: Uri) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
        val txtBadge: TextView = itemView.findViewById(R.id.txtBadge)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val uri = images[position]
        val context = holder.itemView.context
        try {
            val mime = context.contentResolver.getType(uri) ?: ""
            if (mime.startsWith("video")) {
                // extract a frame as thumbnail
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val bmp = retriever.frameAtTime
                if (bmp != null) {
                    holder.imgThumb.setImageBitmap(bmp)
                } else {
                    holder.imgThumb.setImageResource(R.drawable.video_placeholder)
                }
                retriever.release()
            } else {
                holder.imgThumb.setImageURI(uri)
            }
        } catch (e: Exception) {
            holder.imgThumb.setImageURI(uri)
        }
        holder.txtBadge.text = (position + 1).toString()
        holder.itemView.setOnClickListener { onClick(position, uri) }
        holder.btnRemove.setOnClickListener { onRemove(position, uri) }
    }

    override fun getItemCount(): Int = images.size
}

