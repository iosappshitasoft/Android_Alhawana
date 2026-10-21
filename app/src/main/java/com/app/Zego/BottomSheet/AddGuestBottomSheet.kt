package com.app.Zego.BottomSheet

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetAddGuestBinding
import com.app.alhawanajd.databinding.ItemAddGuestMemberBinding
import com.app.alhawanajd.databinding.ItemAddGuestRequestBinding
import com.app.utils.AdminData
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddGuestBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BtmsheetAddGuestBinding? = null
    private val binding get() = _binding!!

    private var selectedTab = TAB_REQUEST
    private var requestItems: List<GuestRequestItem> = emptyList()
    private var guestItems: List<GuestMemberItem> = emptyList()
    private var filteredRequests: List<GuestRequestItem> = emptyList()

    private lateinit var requestAdapter: GuestRequestAdapter
    private lateinit var guestAdapter: GuestMemberAdapter

    var onAcceptRequest: ((GuestRequestItem, Int) -> Unit)? = null
    var onDeclineRequest: ((GuestRequestItem, Int) -> Unit)? = null
    var onInviteGuestsClicked: (() -> Unit)? = null
    var onMicToggle: ((GuestMemberItem) -> Unit)? = null
    var onCameraToggle: ((GuestMemberItem) -> Unit)? = null
    var onRemoveGuest: ((GuestMemberItem) -> Unit)? = null
    var onLayoutClicked: (() -> Unit)? = null
    var onSettingsClicked: (() -> Unit)? = null

    companion object {
        private const val TAG = "AddGuestBottomSheet"
        private const val TAB_REQUEST = 0
        private const val TAB_GUEST = 1

        fun show(
            fragmentManager: FragmentManager,
            requestItems: List<GuestRequestItem>,
            guestItems: List<GuestMemberItem>,
            initialTab: Int = TAB_REQUEST,
            onAcceptRequest: ((GuestRequestItem, Int) -> Unit)? = null,
            onDeclineRequest: ((GuestRequestItem, Int) -> Unit)? = null,
            onInviteGuestsClicked: (() -> Unit)? = null,
            onMicToggle: ((GuestMemberItem) -> Unit)? = null,
            onCameraToggle: ((GuestMemberItem) -> Unit)? = null,
            onRemoveGuest: ((GuestMemberItem) -> Unit)? = null,
            onLayoutClicked: (() -> Unit)? = null,
            onSettingsClicked: (() -> Unit)? = null
        ): AddGuestBottomSheet {
            val sheet = AddGuestBottomSheet().apply {
                this.requestItems = requestItems
                this.guestItems = guestItems
                this.selectedTab = initialTab
                this.onAcceptRequest = onAcceptRequest
                this.onDeclineRequest = onDeclineRequest
                this.onInviteGuestsClicked = onInviteGuestsClicked
                this.onMicToggle = onMicToggle
                this.onCameraToggle = onCameraToggle
                this.onRemoveGuest = onRemoveGuest
                this.onLayoutClicked = onLayoutClicked
                this.onSettingsClicked = onSettingsClicked
            }
            sheet.show(fragmentManager, TAG)
            return sheet
        }
    }

    override fun getTheme(): Int = BottomSheetSystemBarsHelper.applyTheme()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetSystemBarsHelper.configure(
            dialog,
            maxHeightFraction = 0.9f,
            peekHeightFraction = 0.4f,
            skipCollapsed = false
        )
        return dialog
    }

    override fun onStart() {
        super.onStart()
        BottomSheetSystemBarsHelper.applyNavigationBarColor(dialog as? BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BtmsheetAddGuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetSystemBarsHelper.applyParentBackground(view)

        setupTabs()
        setupRequestList()
        setupGuestList()
        setupActions()
        selectTab(selectedTab)
    }

    fun updateRequestItems(items: List<GuestRequestItem>) {
        requestItems = items
        filteredRequests = items
        if (_binding != null && ::requestAdapter.isInitialized) {
            requestAdapter.submitList(filteredRequests)
            updateRequestEmptyState()
            updateRequestTabLabel()
        }
    }

    fun updateGuestItems(items: List<GuestMemberItem>) {
        guestItems = items
        if (_binding != null && ::guestAdapter.isInitialized) {
            guestAdapter.submitList(items)
            updateGuestEmptyState()
        }
    }

    fun removeRequestByUserId(userId: String) {
        requestItems = requestItems.filter { it.userId != userId }
        filteredRequests = filteredRequests.filter { it.userId != userId }
        if (_binding != null && ::requestAdapter.isInitialized) {
            requestAdapter.submitList(filteredRequests)
            updateRequestEmptyState()
            updateRequestTabLabel()
        }
    }

    private fun setupTabs() {
        binding.tabRequest.setOnClickListener {
            App.preventMultipleClick(it)
            selectTab(TAB_REQUEST)
        }
        binding.tabGuest.setOnClickListener {
            App.preventMultipleClick(it)
            selectTab(TAB_GUEST)
        }
        updateRequestTabLabel()
    }

    private fun setupActions() {
        binding.btnLayout.setOnClickListener {
            App.preventMultipleClick(it)
            // onLayoutClicked?.invoke() // Do nothing for now
        }
        binding.btnSettings.setOnClickListener {
            App.preventMultipleClick(it)
            // onSettingsClicked?.invoke() // Do nothing for now
        }
    }

    private fun setupRequestList() {
        val requestRoot = binding.requestContent.root
        val rvRequests = requestRoot.findViewById<RecyclerView>(R.id.rv_requests)
        val etSearch = requestRoot.findViewById<EditText>(R.id.et_search)

        filteredRequests = requestItems
        requestAdapter = GuestRequestAdapter(
            onAccept = { item, position ->
                onAcceptRequest?.invoke(item, position)
            },
            onDecline = { item, position ->
                onDeclineRequest?.invoke(item, position)
            }
        )
        rvRequests.layoutManager = LinearLayoutManager(requireContext())
        rvRequests.adapter = requestAdapter
        requestAdapter.submitList(filteredRequests)
        updateRequestEmptyState()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()?.lowercase().orEmpty()
                filteredRequests = if (query.isEmpty()) {
                    requestItems
                } else {
                    requestItems.filter {
                        it.userName.lowercase().contains(query)
                    }
                }
                requestAdapter.submitList(filteredRequests)
                updateRequestEmptyState()
            }
        })
    }

    private fun setupGuestList() {
        val guestRoot = binding.guestContent.root
        val rvGuests = guestRoot.findViewById<RecyclerView>(R.id.rv_guests)
        val btnInvite = guestRoot.findViewById<FrameLayout>(R.id.btn_invite_guests)

        guestAdapter = GuestMemberAdapter(
            onMicToggle = { item -> onMicToggle?.invoke(item) },
            onCameraToggle = { item -> onCameraToggle?.invoke(item) },
            onRemoveGuest = { item -> onRemoveGuest?.invoke(item) }
        )
        rvGuests.layoutManager = LinearLayoutManager(requireContext())
        rvGuests.adapter = guestAdapter
        guestAdapter.submitList(guestItems)
        updateGuestEmptyState()

        btnInvite.setOnClickListener {
            App.preventMultipleClick(it)
            onInviteGuestsClicked?.invoke()
        }
    }

    private fun selectTab(tab: Int) {
        selectedTab = tab
        val isRequestTab = tab == TAB_REQUEST

        binding.tvTabRequest.setTextColor(
            if (isRequestTab) Color.WHITE else Color.parseColor("#8F8F8F")
        )
        binding.tvTabGuest.setTextColor(
            if (isRequestTab) Color.parseColor("#8F8F8F") else Color.WHITE
        )
        binding.indicatorRequest.isInvisible = !isRequestTab
        binding.indicatorGuest.isInvisible = isRequestTab

        binding.requestContent.root.isVisible = isRequestTab
        binding.guestContent.root.isVisible = !isRequestTab
    }

    private fun updateRequestTabLabel() {
        val count = requestItems.size
        binding.tvTabRequest.text = if (count > 0) {
            getString(R.string.add_guest_tab_request_count, count)
        } else {
            getString(R.string.add_guest_tab_request)
        }
    }

    private fun updateRequestEmptyState() {
        val requestRoot = binding.requestContent.root
        val emptyLay = requestRoot.findViewById<View>(R.id.empty_request_lay)
        val rvRequests = requestRoot.findViewById<RecyclerView>(R.id.rv_requests)
        // Force show list for static items when empty
        val isEmpty = false
        emptyLay.isVisible = isEmpty
        rvRequests.isVisible = !isEmpty
    }

    private fun updateGuestEmptyState() {
        val guestRoot = binding.guestContent.root
        val emptyLay = guestRoot.findViewById<View>(R.id.empty_guest_lay)
        val rvGuests = guestRoot.findViewById<RecyclerView>(R.id.rv_guests)
        // Force show list for static items when empty
        val isEmpty = false
        emptyLay.isVisible = isEmpty
        rvGuests.isVisible = !isEmpty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class GuestRequestItem(
        val userId: String,
        val userName: String,
        val userImage: String,
        val userType: String,
        val isFriend: Boolean = false,
        val sourceMap: Map<String, String>? = null
    )

    data class GuestMemberItem(
        val userId: String,
        val userName: String,
        val userImage: String,
        val role: GuestRole,
        var isMicOn: Boolean = true,
        var isCameraOn: Boolean = true
    )

    enum class GuestRole {
        HOST,
        CO_HOST
    }

    private inner class GuestRequestAdapter(
        private val onAccept: (GuestRequestItem, Int) -> Unit,
        private val onDecline: (GuestRequestItem, Int) -> Unit
    ) : RecyclerView.Adapter<GuestRequestAdapter.RequestVH>() {

        private var items: List<GuestRequestItem> = emptyList()

        fun submitList(list: List<GuestRequestItem>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestVH {
            val itemBinding = ItemAddGuestRequestBinding.inflate(layoutInflater, parent, false)
            return RequestVH(itemBinding)
        }

        override fun onBindViewHolder(holder: RequestVH, position: Int) {
            if (position < items.size) {
                holder.bind(items[position], position)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class RequestVH(
            private val itemBinding: ItemAddGuestRequestBinding
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(item: GuestRequestItem, position: Int) {
                itemBinding.tvUserName.text = item.userName
                itemBinding.friendBadge.isVisible = item.isFriend

                Glide.with(requireContext())
                    .load(AdminData.mediaURL.plus(item.userImage))
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(itemBinding.ivAvatar)

                itemBinding.btnAccept.setOnClickListener {
                    App.preventMultipleClick(it)
                    onAccept(item, position)
                }
                itemBinding.btnDecline.setOnClickListener {
                    App.preventMultipleClick(it)
                    onDecline(item, position)
                }
                itemBinding.ivAvatar.setOnClickListener {
                    App.preventMultipleClick(it)
                    // Do nothing for now
                }
            }
        }
    }

    private inner class GuestMemberAdapter(
        private val onMicToggle: (GuestMemberItem) -> Unit,
        private val onCameraToggle: (GuestMemberItem) -> Unit,
        private val onRemoveGuest: (GuestMemberItem) -> Unit
    ) : RecyclerView.Adapter<GuestMemberAdapter.MemberVH>() {

        private var items: List<GuestMemberItem> = emptyList()

        fun submitList(list: List<GuestMemberItem>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberVH {
            val itemBinding = ItemAddGuestMemberBinding.inflate(layoutInflater, parent, false)
            return MemberVH(itemBinding)
        }

        override fun onBindViewHolder(holder: MemberVH, position: Int) {
            if (position < items.size) {
                holder.bind(items[position])
            }
        }

        override fun getItemCount(): Int = items.size

        inner class MemberVH(
            private val itemBinding: ItemAddGuestMemberBinding
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(item: GuestMemberItem) {
                itemBinding.tvUserName.text = item.userName
                itemBinding.tvRole.text = when (item.role) {
                    GuestRole.HOST -> getString(R.string.text_host)
                    GuestRole.CO_HOST -> getString(R.string.add_guest_co_host)
                }

                val isHost = item.role == GuestRole.HOST
                itemBinding.btnRemove.isVisible = !isHost

                Glide.with(requireContext())
                    .load(AdminData.mediaURL.plus(item.userImage))
                    .placeholder(R.drawable.alhawana)
                    .error(R.drawable.alhawana)
                    .into(itemBinding.ivAvatar)

                updateMicState(item)
                updateCameraState(item)

                itemBinding.btnMic.setOnClickListener {
                    App.preventMultipleClick(it)
                    item.isMicOn = !item.isMicOn
                    updateMicState(item)
                    onMicToggle(item)
                }
                itemBinding.btnCamera.setOnClickListener {
                    App.preventMultipleClick(it)
                    item.isCameraOn = !item.isCameraOn
                    updateCameraState(item)
                    onCameraToggle(item)
                }
                itemBinding.btnRemove.setOnClickListener {
                    App.preventMultipleClick(it)
                    onRemoveGuest(item)
                }
                itemBinding.ivAvatar.setOnClickListener {
                    App.preventMultipleClick(it)
                    // Do nothing for now
                }
            }

            private fun updateMicState(item: GuestMemberItem) {
                if (item.isMicOn) {
                    itemBinding.btnMic.setBackgroundResource(R.drawable.bg_guest_action_circle)
                    itemBinding.ivMic.setImageResource(R.drawable.mls_mic_on)
                } else {
                    itemBinding.btnMic.setBackgroundResource(R.drawable.bg_guest_action_circle_red)
                    itemBinding.ivMic.setImageResource(R.drawable.mls_mic_off)
                }
            }

            private fun updateCameraState(item: GuestMemberItem) {
                if (item.isCameraOn) {
                    itemBinding.btnCamera.setBackgroundResource(R.drawable.bg_guest_action_circle)
                    itemBinding.ivCamera.setImageResource(R.drawable.video_call_permission)
                } else {
                    itemBinding.btnCamera.setBackgroundResource(R.drawable.bg_guest_action_circle_red)
                    itemBinding.ivCamera.setImageResource(R.drawable.ic_guest_video_off)
                }
            }
        }
    }
}
