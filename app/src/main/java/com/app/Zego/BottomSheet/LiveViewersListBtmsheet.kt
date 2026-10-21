package com.app.Zego.BottomSheet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.Zego.ApiBase.ZegoApiClient
import com.app.addon.livestream.external.model.StreamDetails
import com.app.addon.livestream.external.utils.ApiClient
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetLiveViewersBinding
import com.app.alhawanajd.databinding.NewLiveUserlistContentBinding
import com.app.model.GetSet
import com.app.utils.AdminData
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
┏━━━┓━━━━━━━━┏┓┏━┓━━━━━━┏┓━━━━━━━━
┃┏━┓┃━━━━━━━━┃┃┃┏┛━━━━━━┃┃━━━━━━━━
┃┃━┃┃┏━━┓┏━━┓┃┗┛┛━┏━━┓┏━┛┃┏━━┓┏━━┓
┃┗━┛┃┃┏┓┃┃┏┓┃┃┏┓┃━┃┏┓┃┃┏┓┃┃┏┓┃┃━━┫
┃┏━┓┃┃┗┛┃┃┗┛┃┃┃┃┗┓┃┗┛┃┃┗┛┃┃┃━┫┣━━┃
┗┛━┗┛┃┏━┛┃┏━┛┗┛┗━┛┗━━┛┗━━┛┗━━┛┗━━┛
━━━━━┃┃━━┃┃━━━━━━━━━━━━━━━━━━━━━━━
━━━━━┗┛━━┗┛━━━━━━━━━━━━━━━━━━━━━━━

 * Copyright © 2025 HitaSoft
 * All rights reserved.
 *
 * Created by Karthik Muru on 21/01/25.
 * cashgate-chobi-android project.
 */
class LiveViewersListBtmsheet : BottomSheetDialogFragment() {

    private lateinit var bind: BtmsheetLiveViewersBinding

    // these come from arguments
    private lateinit var from: String
    private lateinit var liveId: String
    private var invitedUsers: List<String> = emptyList()

    // these are set directly in newInstance
    private lateinit var onUserClicked: (StreamDetails.LiveViewers) -> Unit
    private lateinit var onInviteClicked: (StreamDetails.LiveViewers) -> Unit
    private lateinit var onGiftClicked: () -> Unit

    companion object {
        private const val TAG = "LiveViewersListBtmsheet"

        fun getInstance(
            from: String,
            liveId: String,
            invitedUsers: List<String>,
            onUserClicked: (StreamDetails.LiveViewers) -> Unit,
            onInviteClicked: (StreamDetails.LiveViewers) -> Unit,
            onGiftClicked: () -> Unit
        ): LiveViewersListBtmsheet {
            val fragment = LiveViewersListBtmsheet()
            fragment.arguments = Bundle().apply {
                putString("from", from)
                putString("liveId", liveId)
                putStringArrayList("invitedUsers", ArrayList(invitedUsers))
            }
            fragment.onUserClicked = onUserClicked
            fragment.onInviteClicked = onInviteClicked
            fragment.onGiftClicked = onGiftClicked
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Bottom_HashTagDialog)

        arguments?.let {
            from = it.getString("from", "")
            liveId = it.getString("liveId", "")
            invitedUsers = it.getStringArrayList("invitedUsers") ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = BtmsheetLiveViewersBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.title.text =
            if (from == "request") getString(R.string.newviewers) else getString(R.string.top_viewers)

        bind.apply {
            //commented onsep20
           // giftLay.visibility = if (from == "request") View.GONE else View.VISIBLE
            userName.text = GetSet.getName()
            Glide.with(requireContext())
                .load(GetSet.getUserImage())
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .into(userImage)
            btnGift.setOnClickListener {
                App.preventMultipleClick(it)
                onGiftClicked.invoke()
                dismiss()
            }
        }

        apiCall()
    }

    private fun apiCall() {
        bind.pbar.visibility = View.VISIBLE
        ZegoApiClient.getClient(context).create(ApiInterface::class.java)
            .getStreamViewersInfo(liveId, GetSet.getUserId())
            .enqueue(object : Callback<StreamDetails> {
                override fun onResponse(
                    call: Call<StreamDetails>,
                    response: Response<StreamDetails>
                ) {
                    Log.e(TAG, "onResponse: :::liveViewers:::" + Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val viewer_list = response.body()?.getLiveViewers()
                            if (!viewer_list.isNullOrEmpty()) {
                                if (isAdded != null) {
                                    requireContext().let {
                                        val layoutManager =
                                            LinearLayoutManager(it, RecyclerView.VERTICAL, false)
                                        bind.recyclerview.layoutManager = layoutManager
                                        val adapter = ViewersAdapter(viewer_list!!)
                                        bind.recyclerview.adapter = adapter
                                        bind.recyclerview.setHasFixedSize(true)
                                    }
                                }
                            }
                            showNullLay(viewer_list.isNullOrEmpty())
                        } else {
                            showNullLay(true)
                        }
                    }
                }

                override fun onFailure(call: Call<StreamDetails>, t: Throwable) {
                    Log.e(TAG, "onFailure: ::::::", t)
                    showNullLay(true)
                }
            })
    }

    private fun showNullLay(isShow: Boolean) {
        bind.pbar.visibility = View.GONE
        bind.nullLay.visibility = if (isShow) View.VISIBLE else View.GONE
        bind.recyclerview.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    inner class ViewersAdapter(private val list: List<StreamDetails.LiveViewers>) :
        RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val bind = NewLiveUserlistContentBinding.inflate(layoutInflater, parent, false)
            return VH(bind)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.onBind(list[position])
        }
    }

    inner class VH(private val bind: NewLiveUserlistContentBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun onBind(viewer: StreamDetails.LiveViewers) {
            bind.apply {
                Glide.with(requireContext())
                    .load(AdminData.mediaURL.plus(viewer.getUserImage()))
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(userImage)

                userName.text = viewer.userName
                btnInvite.visibility = if (from == "request") View.VISIBLE else View.GONE
                if (from == "request") {
                    btnInvite.text =
                        if (invitedUsers.contains(viewer.getUserId())) getString(R.string.invite_sent)
                        else getString(R.string.invite_txt)
                }

                root.setOnClickListener {
                    App.preventMultipleClick(it)
                    onUserClicked.invoke(viewer)
                    dismiss()
                }

                btnInvite.setOnClickListener {
                    App.preventMultipleClick(it)
                    onInviteClicked.invoke(viewer)
                    dismiss()
                }
            }
        }
    }
}