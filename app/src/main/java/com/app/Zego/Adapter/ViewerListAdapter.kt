package com.app.Zego.Adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.Zego.Additional.StrokeTextView
import com.app.addon.livestream.external.model.StreamDetails
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.utils.AdminData
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView

class ViewerListAdapter(
    private val context: Context,
    private val viewersList: List<StreamDetails.LiveViewers>,
    private val type: String
) : RecyclerView.Adapter<ViewerListAdapter.ViewerViewHolder>() {

    // Always max 3 viewers
    private val maxCount = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.content_recycler, parent, false)
        return ViewerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewerViewHolder, position: Int) {
        if (position < viewersList.size) {
            val viewer = viewersList[position]
            Glide.with(context)
                .load(viewer.userImage)
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .into(holder.userImage)
        } else {
            holder.userImage.setImageResource(R.drawable.default_profile_image)
        }
    }

    override fun getItemCount(): Int = minOf(maxCount, viewersList.size)

    inner class ViewerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: RoundedImageView = itemView.findViewById(R.id.userImage)
    }
}

/*
this code is working fine same for chobi with 1 st and 2nd and 3rd icons is showing fine
class ViewerListAdapter(
    private val context: Context,
    private val viewersList: ArrayList<StreamDetails.LiveViewers>,
    private val type: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_EMPTY_SEAT = 0
        const val VIEW_TYPE_HOST_SEAT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < viewersList.size) VIEW_TYPE_HOST_SEAT else VIEW_TYPE_EMPTY_SEAT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.content_pk_top_gifter, parent, false)
        return if (viewType == VIEW_TYPE_HOST_SEAT) {
            MyViewHolder(itemView)
        } else {
            EmptyViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val viewer = viewersList[position]

            holder.rankTV.visibility = View.VISIBLE
            Glide.with(context)
                .load(*/
/*AdminData.mediaURL +*//*
 viewer.userImage)
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .into(holder.userImage)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.userImage.borderColor = context.getColor(R.color.colorSecondaryVariant)
            }

            holder.rankTV.text = (position + 1).toString()

            if (position == 0) {
                holder.mvpTV.visibility = View.VISIBLE
            } else {
                holder.mvpTV.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { v ->
                App.preventMultipleClick(v)
                // TODO: add callback if you want to open details
                // showPkContributorsViewersList("selfHost", liveID)
            }
        } else if (holder is EmptyViewHolder) {
            holder.mvpTV.visibility = View.GONE
            holder.rankTV.text = ""
            holder.userImage.setImageDrawable(null)
        }
    }

    override fun getItemCount(): Int {
        // Always show 3 slots
        return 3
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: RoundedImageView = view.findViewById(R.id.userImage)
        val mvpTV: TextView = view.findViewById(R.id.mvpTV)
        val rankTV: StrokeTextView = view.findViewById(R.id.rankTV)
        val itemLay: ConstraintLayout = view.findViewById(R.id.itemLay)
    }

    inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: RoundedImageView = view.findViewById(R.id.userImage)
        val mvpTV: TextView = view.findViewById(R.id.mvpTV)
        val rankTV: StrokeTextView = view.findViewById(R.id.rankTV)
        val itemLay: ConstraintLayout = view.findViewById(R.id.itemLay)
    }
}
*/
