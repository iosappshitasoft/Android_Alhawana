package com.app.Zego.BottomSheet

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.Zego.Model.StreamersList
import com.app.addon.livestream.external.utils.ApiClient
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetStreamInviteBinding
import com.app.alhawanajd.databinding.ItemStreamInviteBinding
import com.app.model.GetSet
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StreamInviteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BtmsheetStreamInviteBinding? = null
    private val binding get() = _binding!!

    private val liveId: String
        get() = arguments?.getString("liveId") ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BtmsheetStreamInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = BottomSheetSystemBarsHelper.applyTheme()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        apiInterface.getStreamersList(
            GetSet.getAuthToken(),
            GetSet.getUserId(),
            liveId
        ).enqueue(object : Callback<StreamersList> {
            override fun onResponse(call: Call<StreamersList>, response: Response<StreamersList>) {
                if (isAdded && response.isSuccessful && response.body()?.status == "true") {
                    val list = response.body()?.result
                    if (!list.isNullOrEmpty()) {
                        val adapter = StreamInviteAdapter()
                        binding.rvStreamers.adapter = adapter
                        adapter.submitList(list)
                    } else {
                        showNullTxt(getString(R.string.no_user_txt))
                    }
                } else {
                    /*  if (isAdded) {
                          showNullTxt(getString(R.string.something_went_wrong))
                      }*/
                }
            }

            override fun onFailure(call: Call<StreamersList>, t: Throwable) {
                if (isAdded) {
                    showNullTxt(getString(R.string.unable_to_reach_server))
                }
            }
        })

        binding.tvExpectedWaitingTime.text = getString(R.string.expected_waiting_seconds, "39")

        // Setup RecyclerView
        binding.rvStreamers.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showNullTxt(msg: String) {
        if (isAdded) {
            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Adapter ---
    inner class StreamInviteAdapter : RecyclerView.Adapter<StreamInviteAdapter.ViewHolder>() {

        private val items = mutableListOf<StreamersList.Result>()

        fun submitList(newItems: List<StreamersList.Result>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemStreamInviteBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(
            private val itemBinding: ItemStreamInviteBinding
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(item: StreamersList.Result) {
                itemBinding.tvName.text = item.hostName
                itemBinding.tvScore.text = item.txtStatus

                val imageUrl = if (item.hostImage != null && (item.hostImage.startsWith("http://") || item.hostImage.startsWith("https://"))) {
                    item.hostImage
                } else {
                    com.app.utils.AdminData.mediaURL + item.hostImage
                }

                if (context is Activity) {
                    val activity = context as Activity
                    if (activity.isFinishing || activity.isDestroyed()) {
                        return
                    }
                }

                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(itemBinding.ivAvatar)

                itemBinding.btnInvite.setOnClickListener {
                    itemBinding.btnInvite.isEnabled = false
                    val progressDialog = android.app.ProgressDialog(itemView.context).apply {
                        setMessage("Sending invitation...")
                        setCancelable(false)
                        show()
                    }
                    com.app.Zego.internal.ZEGOLiveStreamingManager.getInstance().invitePKBattle(item.hostId, object : com.app.Zego.internal.business.UserRequestCallback {
                        override fun onUserRequestSend(errorCode: Int, requestID: String?) {
                            if (isAdded) {
                                itemBinding.btnInvite.isEnabled = true
                                progressDialog.dismiss()
                                if (errorCode == 0) {
                                    android.widget.Toast.makeText(requireContext(), "Invitation sent successfully", android.widget.Toast.LENGTH_SHORT).show()
                                    dismiss()
                                } else {
                                    android.widget.Toast.makeText(requireContext(), "Failed to send invitation: $errorCode", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                progressDialog.dismiss()
                            }
                        }
                    })
                }
            }
        }
    }

    companion object {
        fun newInstance(liveId: String): StreamInviteBottomSheet {
            val fragment = StreamInviteBottomSheet()
            val args = Bundle()
            args.putString("liveId", liveId)
            fragment.arguments = args
            return fragment
        }
    }
}
