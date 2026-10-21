package com.app.Zego.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.R
import com.app.model.Gift
import com.bumptech.glide.Glide

class GiftAdapter(
    private val gifts: List<Gift>,
    private val onGiftClick: (Gift) -> Unit
) : RecyclerView.Adapter<GiftAdapter.GiftViewHolder>() {

    inner class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val giftImage: ImageView = itemView.findViewById(R.id.giftImage)
        private val txtGiftName: TextView = itemView.findViewById(R.id.txtGiftName)
        private val txtGiftPrice: TextView = itemView.findViewById(R.id.txtGiftPrice)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val itemLay: RelativeLayout = itemView.findViewById(R.id.itemLay)

        fun bind(gift: Gift) {
            txtGiftName.text = gift.giftTitle
            txtGiftPrice.text = gift.giftGems.toString()

            progressBar.visibility = View.VISIBLE
            Glide.with(itemView.context)
                .load(gift.giftIcon)
                .error(R.drawable.gift_primary)
                .into(giftImage)

            progressBar.visibility = View.GONE
            giftImage.visibility = View.VISIBLE

            itemLay.setOnClickListener {
                Log.e("GIFT","gift clicked")
                onGiftClick(gift)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gifts, parent, false)
        return GiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(gifts[position])
    }

    override fun getItemCount() = gifts.size
}
