package com.app.Zego.BottomSheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ZegoRoomLayoutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RoomLayoutBottomSheets(
    @get:JvmName("getAdapterContext") val context: Context,
    val onConfirmClicked: (String) -> Unit
) : BottomSheetDialogFragment() {

    lateinit var bind: ZegoRoomLayoutBottomSheetBinding
    private var selectedOption = "Default"

    companion object {
        private const val TAG = "RoomLayoutBottomSheets"
        lateinit var dialog: RoomLayoutBottomSheets

        fun getInstance(
            context: Context,
            fm: FragmentManager,
            onConfirmClicked: (String) -> Unit
        ): RoomLayoutBottomSheets {
            dialog = RoomLayoutBottomSheets(context, onConfirmClicked)
            if (!dialog.isVisible) {
                dialog.show(fm, TAG)
            }
            return dialog
        }
    }

    override fun getTheme(): Int = BottomSheetSystemBarsHelper.applyTheme()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetSystemBarsHelper.configure(dialog, maxHeightFraction = 0.9f)
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
        bind = ZegoRoomLayoutBottomSheetBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetSystemBarsHelper.applyParentBackground(view)

        bind.btnBack.setOnClickListener {
            dismiss()
        }

        bind.btnConfirm.setOnClickListener {
            onConfirmClicked(selectedOption)
            dismiss()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val options = listOf(
            LayoutOption("Default", getString(R.string.default_layout), R.drawable.ic_layout_default, R.drawable.ic_layout_default_selected, R.drawable.ic_layout_mockup_default_selected),
            LayoutOption("1 Guest", getString(R.string.guest_1), R.drawable.ic_layout_1_guest, R.drawable.ic_layout_1_guest_selected, R.drawable.ic_layout_mockup_1_guest_selected),
            LayoutOption("3 Guests", getString(R.string.guests_3), R.drawable.ic_layout_3_guests, R.drawable.ic_layout_3_guests_selected, R.drawable.ic_layout_mockup_3_guests_selected),
            LayoutOption("6 Guests", getString(R.string.guests_6), R.drawable.ic_layout_6_guests, R.drawable.ic_layout_6_guests_selected, R.drawable.ic_layout_mockup_6_guests_selected),
            LayoutOption("9 Guests", getString(R.string.guests_9), R.drawable.ic_layout_9_guests, R.drawable.ic_layout_9_guests_selected, R.drawable.ic_layout_mockup_9_guests_selected),
            LayoutOption("16 Guests", getString(R.string.guests_16), R.drawable.ic_layout_16_guests, R.drawable.ic_layout_16_guests_selected, R.drawable.ic_layout_mockup_16_guests_selected)
        )

        val adapter = LayoutOptionsAdapter(options) { selected ->
            if (selectedOption != selected.id) {
                selectedOption = selected.id

                bind.ivPhoneMockupLayout.animate()
                    .alpha(0f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(150)
                    .withEndAction {
                        bind.ivPhoneMockupLayout.setImageResource(selected.mockupResId)
                        bind.ivPhoneMockupLayout.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
        }

        bind.rvOptions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    data class LayoutOption(val id: String, val name: String, val iconResId: Int, val selectedIconResId: Int, val mockupResId: Int)

    inner class LayoutOptionsAdapter(
        private val options: List<LayoutOption>,
        private val onOptionSelected: (LayoutOption) -> Unit
    ) : RecyclerView.Adapter<LayoutOptionsAdapter.ViewHolder>() {

        private var selectedPosition = 0

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_option_icon)
            val name: TextView = view.findViewById(R.id.tv_option_name)
            val radio: ImageView = view.findViewById(R.id.iv_radio)
            val root: View = view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_layout_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val option = options[position]
            holder.name.text = option.name

            val isSelected = position == selectedPosition

            if (isSelected) {
                holder.icon.setBackgroundResource(R.drawable.bg_option_unselected)
                holder.icon.setImageResource(option.selectedIconResId)
                holder.radio.setImageResource(R.drawable.bg_radio_selected)
                holder.name.setTextColor(android.graphics.Color.WHITE)
                holder.name.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                holder.icon.setBackgroundResource(R.drawable.bg_option_unselected)
                holder.icon.setImageResource(option.iconResId)
                holder.radio.setImageResource(R.drawable.bg_radio_unselected)
                holder.name.setTextColor(android.graphics.Color.parseColor("#B3FFFFFF"))
                holder.name.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            holder.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onOptionSelected(option)
            }
        }

        override fun getItemCount(): Int = options.size
    }
}
