package com.app.Zego.Adapter

import android.app.Activity
import android.content.Context
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.Zego.CallBacks.OnCommentClickListener
import com.app.addon.livestream.external.utils.StreamConstants
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.MlsItemCommentMultihostBinding
import com.app.alhawanajd.databinding.MlsItemLiveGiftMultiguestBinding
import com.app.alhawanajd.databinding.MlsItemUserJoinBinding
import com.app.alhawanajd.databinding.MlsItemSystemMessageBinding
import com.app.alhawanajd.databinding.MlsLayoutCohostMessageRequestBinding
import com.app.helper.LocaleManager
import com.app.utils.AdminData
import com.app.utils.Constants
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView

class CommentsAdapter(
    private val context: Context,
    private val commentList: ArrayList<HashMap<String, String>>,
    private val listener: OnCommentClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_JOIN = 0
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_GIFT = 2
        const val VIEW_TYPE_COHOST_ACCEPT = 3
        const val VIEW_TYPE_COHOST_REJECT = 4
        const val VIEW_TYPE_COHOST_SUCCESSFULLY_ACCEPT = 5
        const val VIEW_TYPE_WELCOME = 6
        const val VIEW_TYPE_SYSTEM_MESSAGE = 7
        const val SYSTEM_ICON_WELCOME = "welcome"
        const val SYSTEM_ICON_GIFT_SOUND = "gift_sound"
    }

    override fun getItemCount(): Int = commentList.size

    override fun getItemViewType(position: Int): Int {
        val map = commentList[position]
        val type = map[Constants.TAG_TYPE] ?: return VIEW_TYPE_TEXT
        return when (type) {
            Constants.TAG_TEXT -> VIEW_TYPE_TEXT
            StreamConstants.TAG_STREAM_JOINED -> VIEW_TYPE_JOIN
            Constants.TAG_GIFT -> VIEW_TYPE_GIFT
            Constants.TAG_HAND_RAISE -> VIEW_TYPE_COHOST_ACCEPT
            Constants.TAG_HAND_RAISE_REJECT -> VIEW_TYPE_COHOST_REJECT
            Constants.TAG_HAND_RAISE_ACCEPT -> VIEW_TYPE_COHOST_SUCCESSFULLY_ACCEPT
            "welcome" -> VIEW_TYPE_WELCOME
            "system_message" -> VIEW_TYPE_SYSTEM_MESSAGE
            else -> VIEW_TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_JOIN -> {
                val view = inflater.inflate(R.layout.mls_item_user_join, parent, false)
                JoiningViewHolder(view, MlsItemUserJoinBinding.bind(view))
            }
            VIEW_TYPE_TEXT -> {
                val view = inflater.inflate(R.layout.mls_item_comment_multihost, parent, false)
                MyViewHolder(view, MlsItemCommentMultihostBinding.bind(view))
            }
            VIEW_TYPE_GIFT -> {
                val view = inflater.inflate(R.layout.mls_item_live_gift_multiguest, parent, false)
                GiftViewHolder(view, MlsItemLiveGiftMultiguestBinding.bind(view))
            }
            VIEW_TYPE_COHOST_ACCEPT -> {
                val view = inflater.inflate(R.layout.mls_layout_cohost_message_request, parent, false)
                CohostRequestViewHolder(view, MlsLayoutCohostMessageRequestBinding.bind(view))
            }
            VIEW_TYPE_COHOST_REJECT -> {
                val view = inflater.inflate(R.layout.mls_layout_cohost_message_request, parent, false)
                CohostRequestRejectViewHolder(view, MlsLayoutCohostMessageRequestBinding.bind(view))
            }
            VIEW_TYPE_COHOST_SUCCESSFULLY_ACCEPT -> {
                val view = inflater.inflate(R.layout.mls_layout_cohost_message_request, parent, false)
                CohostRequestAcceptSuccViewHolder(view, MlsLayoutCohostMessageRequestBinding.bind(view))
            }
            VIEW_TYPE_WELCOME -> {
                val view = inflater.inflate(R.layout.mls_item_comment_multihost, parent, false)
                WelcomeViewHolder(view, MlsItemCommentMultihostBinding.bind(view))
            }
            VIEW_TYPE_SYSTEM_MESSAGE -> {
                val view = inflater.inflate(R.layout.mls_item_system_message, parent, false)
                SystemMessageViewHolder(view, MlsItemSystemMessageBinding.bind(view))
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val map = commentList[position]

        when (holder) {
            is MyViewHolder -> {
                holder.setIsRecyclable(false)
                holder.txtMessage.text = map[Constants.TAG_MESSAGE]
                holder.txtUserName.text = "@${map[Constants.TAG_USER_NAME]}"
                holder.itemLay.visibility = View.VISIBLE

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.imgCommenter)

                holder.itemLay.setOnClickListener { view ->
                    App.preventMultipleClick(view)
                    map[Constants.TAG_USER_ID]?.let { userId ->
                        listener.onProfileClick(userId)
                    }
                }

                holder.imgCommenter.setOnClickListener { view ->
                    App.preventMultipleClick(view)
                    map[Constants.TAG_USER_ID]?.let { userId ->
                        listener.onProfileClick(userId)
                    }
                }

            }

            is JoiningViewHolder -> {
                holder.setIsRecyclable(false)
                holder.txtJoined.text = "@${map[Constants.TAG_USER_NAME]}"
                holder.itemLay.visibility = View.VISIBLE

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.imgCommenter)
            }

            is GiftViewHolder -> {
                holder.setIsRecyclable(false)
                holder.txtUserName.text = "@${map[Constants.TAG_USER_NAME]}"
              //  holder.txtGiftName.text = map[Constants.TAG_GIFT_TITLE]
                val giftTitle = "\"" + map[Constants.TAG_GIFT_TITLE] + "\""
                var content: String
                val count = map["gift_count"] ?: "0"

                content = if (LocaleManager.isRTL()) {
                    if (count == "1") {
                        " أرسل أ $giftTitle هدية ل ${map["selected_user_name"]}"
                    } else {
                        " أرسل أ $count x $giftTitle هدية ل ${map["selected_user_name"]}"
                    }
                } else {
                    "${context.getString(R.string.text_sent)} $count x $giftTitle ${context.getString(R.string.text_gift_to)} ${map["selected_user_name"]}"
                }
                holder.txtGiftName.text = content

                    Glide.with(context)
                    .load(AdminData.mediaURL + map[Constants.TAG_GIFT_ICON])
                    .placeholder(R.drawable.gift)
                    .error(R.drawable.gift)
                    .into(holder.giftImage)

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.imgCommenter)
            }

            is CohostRequestViewHolder -> {
                holder.txtContent.text = map[Constants.TAG_USER_NAME]
                holder.itemLay.visibility = View.VISIBLE
                if (LocaleManager.isRTL()) {
                    holder.txtContent.text = " أرسل طلبًا لتكون في " +
                            if (map["user_type"].equals("host", ignoreCase = true)) {
                                "publisherName" //need to change this as publisherName, need to send pusher parama
                            } else {
                                map[Constants.TAG_USER_NAME]
                            } + " فيديو مباشر "

                    holder.txtRequestToJoin.text = "طلب الانضمام"
                } else {
                    holder.txtContent.text =
                        (if (map["user_type"].equals("host", ignoreCase = true)) {
                            "publisherName" //need to change this as publisherName
                        } else {
                            map[Constants.TAG_USER_NAME]
                        }) + " " + context.getString(R.string.text_live_video)
                }

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.likedImage)
            }

            is CohostRequestRejectViewHolder -> {
                holder.txtContent.text = map[Constants.TAG_USER_NAME]
                holder.itemLay.visibility = View.VISIBLE
                holder.txtRequestToJoin.visibility = View.GONE

                if (LocaleManager.isRTL()) {
                    holder.txtContent.text = "${map["user_name"]} رفض طلبك "
                } else {
                    holder.txtContent.text =
                        "${map["user_name"]} ${context.getString(R.string.text_rejected_your_request)}"
                }

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.likedImage)
            }

            is CohostRequestAcceptSuccViewHolder -> {
                holder.txtContent.text = map[Constants.TAG_USER_NAME]
                holder.itemLay.visibility = View.VISIBLE
                holder.txtRequestToJoin.visibility = View.GONE

                if (map["user_type"] == "host") {
                    if (LocaleManager.isRTL()) {
                        holder.txtContent.text = "${map["host_name"]}  قبل طلبك  "
                    } else {
                        holder.txtContent.text =
                            "${map["host_name"]} ${context.getString(R.string.text_accepted_your_request)}"
                    }
                } else {
                    if (LocaleManager.isRTL()) {
                        holder.txtContent.text = "${map["user_name"]}  قبل طلبك  "
                    } else {
                        holder.txtContent.text =
                            "${map["user_name"]} ${context.getString(R.string.text_accepted_your_request)}"
                    }
                }

                Glide.with(context)
                    .load(map[Constants.TAG_USER_IMAGE])
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(holder.likedImage)
            }

            is WelcomeViewHolder -> {
                holder.txtUserName.text = "@${map[Constants.TAG_USER_NAME]}"
                holder.txtMessage.text = map[Constants.TAG_MESSAGE]
                holder.itemLay.visibility = View.VISIBLE

                holder.itemLay.setOnClickListener { view ->
                    App.preventMultipleClick(view)
                    map[Constants.TAG_USER_ID]?.let { userId ->
                        listener.onProfileClick(userId)
                    }
                }

            }

            is SystemMessageViewHolder -> {
                holder.setIsRecyclable(false)
                holder.itemLay.visibility = View.VISIBLE

                val iconKey = map["system_icon"].orEmpty()
                
                if (iconKey == SYSTEM_ICON_WELCOME) {
                    holder.blurCardView.radius = 16f * context.resources.displayMetrics.density
                    holder.messageContainer.setBackgroundResource(R.drawable.bg_blur_radius_16)
                    holder.systemIcon.setImageResource(R.drawable.ic_rocket)
                    holder.systemIcon.setBackgroundResource(R.drawable.bg_system_icon_purple)
                    holder.systemIcon.imageTintList = null
                    holder.systemIcon.layoutParams.width = (24f * context.resources.displayMetrics.density).toInt()
                    holder.systemIcon.layoutParams.height = (24f * context.resources.displayMetrics.density).toInt()
                    holder.systemIcon.requestLayout()
                    holder.leftHighlight.visibility = View.VISIBLE
                } else {
                    holder.blurCardView.radius = 99f * context.resources.displayMetrics.density
                    holder.messageContainer.setBackgroundResource(R.drawable.bg_blur_radius_99)
                    holder.systemIcon.setImageResource(R.drawable.ic_baseline_settings_24)
                    holder.systemIcon.background = null
                    holder.systemIcon.layoutParams.width = (24f * context.resources.displayMetrics.density).toInt()
                    holder.systemIcon.layoutParams.height = (24f * context.resources.displayMetrics.density).toInt()
                    holder.systemIcon.requestLayout()
                    holder.systemIcon.setColorFilter(
                        context.getColor(R.color.colorWhite),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    holder.leftHighlight.visibility = View.GONE
                }

                val title = map["title"].orEmpty()
                if (title.isNotEmpty()) {
                    holder.txtTitle.visibility = View.VISIBLE
                    holder.txtTitle.text = title
                } else {
                    holder.txtTitle.visibility = View.GONE
                }
                holder.txtMessage.text = map[Constants.TAG_MESSAGE]
            }
        }
    }

    // --- ViewHolders ---
    class JoiningViewHolder(view: View, bind: MlsItemUserJoinBinding) : RecyclerView.ViewHolder(view) {
        val txtJoined: TextView = bind.txtJoined
        val itemLay: LinearLayout = bind.itemLay
        val txtJoinedText: TextView = bind.txtJoinedText
        val imgCommenter: RoundedImageView = bind.imgCommenter
     /*   val levelLay: LinearLayout = bind.levelLay
        val textLevel: TextView = bind.textLevel
        val levelIV: ImageView = bind.levelIV*/
    }

    class MyViewHolder(view: View, bind: MlsItemCommentMultihostBinding) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = bind.txtUserName
        val txtMessage: TextView = bind.txtMessage
        val textviewhost: TextView = bind.textviewhost
        val itemLay: RelativeLayout = bind.itemLay
        val imgCommenter: RoundedImageView = bind.imgCommenter
      /*  val textLevel: TextView = bind.textLevel
        val levelIV: ImageView = bind.levelIV
        val levelLay: LinearLayout = bind.levelLay
        val imgLay: RelativeLayout = bind.imgLay*/
    }

    class WelcomeViewHolder(view: View, bind: MlsItemCommentMultihostBinding) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = bind.txtUserName
        val txtMessage: TextView = bind.txtMessage
        val textviewhost: TextView = bind.textviewhost
        val imgCommenter: RoundedImageView = bind.imgCommenter
       /* val textLevel: TextView = bind.textLevel
        val levelIV: ImageView = bind.levelIV
        val levelLay: LinearLayout = bind.levelLay*/
        val itemLay: RelativeLayout = bind.itemLay
    }

    class SystemMessageViewHolder(view: View, bind: MlsItemSystemMessageBinding) : RecyclerView.ViewHolder(view) {
        val blurCardView: androidx.cardview.widget.CardView = bind.blurCardView
        val messageContainer: eightbitlab.com.blurview.BlurView = bind.messageContainer
        val systemIcon: ImageView = bind.systemIcon
        val txtTitle: TextView = bind.txtTitle
        val txtMessage: TextView = bind.txtMessage
        val itemLay: RelativeLayout = bind.itemLay
        val leftHighlight: View = bind.leftHighlight

        init {
            val activity = view.context as? Activity
            val decorView = activity?.window?.decorView
            val blurTarget = decorView?.findViewById<eightbitlab.com.blurview.BlurTarget>(com.app.alhawanajd.R.id.blurTarget)
            
            if (blurTarget != null) {
                messageContainer.setupWith(blurTarget)
                    .setFrameClearDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                    .setBlurRadius(24f)
            }
        }
    }

    class GiftViewHolder(view: View, bind: MlsItemLiveGiftMultiguestBinding) : RecyclerView.ViewHolder(view) {
        val txtGiftName: TextView = bind.txtGiftName
        val txtUserName: TextView = bind.txtUserName
        val giftImage: ImageView = bind.giftImage
        val giftLay: LinearLayout = bind.giftLay
        val imgCommenter: RoundedImageView = bind.imgCommenter
     /*   val textLevel: TextView = bind.textLevel
        val levelIV: ImageView = bind.levelIV
        val levelLay: LinearLayout = bind.levelLay
        val imgLay: RelativeLayout = bind.imgLay*/
    }

    class CohostRequestViewHolder(view: View, bind: MlsLayoutCohostMessageRequestBinding) : RecyclerView.ViewHolder(view) {
        val txtContent: TextView = bind.txtContent
        val txtRequestToJoin: TextView = bind.txtRequestToJoin
        val itemLay: LinearLayout = bind.itemLay
        val likedImage: RoundedImageView = bind.likedImage
        /*val txtUserName: TextView = bind.txtUserName
        val imgLay: RelativeLayout = bind.imgLay*/
    }

    class CohostRequestRejectViewHolder(view: View, bind: MlsLayoutCohostMessageRequestBinding) : RecyclerView.ViewHolder(view) {
        val txtContent: TextView = bind.txtContent
        val txtRequestToJoin: TextView = bind.txtRequestToJoin
      /*  val imgLay: RelativeLayout = bind.imgLay
      */  val itemLay: LinearLayout = bind.itemLay
        val likedImage: RoundedImageView = bind.likedImage
       /* val txtUserName: TextView = bind.txtUserName*/
    }

    class CohostRequestAcceptSuccViewHolder(view: View, bind: MlsLayoutCohostMessageRequestBinding) : RecyclerView.ViewHolder(view) {
        val txtContent: TextView = bind.txtContent
        val txtRequestToJoin: TextView = bind.txtRequestToJoin
        val itemLay: LinearLayout = bind.itemLay
      /*  val imgLay: RelativeLayout = bind.imgLay*/
        val likedImage: RoundedImageView = bind.likedImage
    /*    val txtUserName: TextView = bind.txtUserName*/
    }
}
