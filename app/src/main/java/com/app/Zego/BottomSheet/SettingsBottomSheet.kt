package com.app.Zego.BottomSheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ZegoSettingsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomSheet(
    @get:JvmName("getAdapterContext") val context: Context,
    val onDoneClicked: () -> Unit
) : BottomSheetDialogFragment() {

    lateinit var bind: ZegoSettingsBottomSheetBinding

    companion object {
        private const val TAG = "SettingsBottomSheet"
        private lateinit var dialog: SettingsBottomSheet

        fun getInstance(
            context: Context,
            fm: FragmentManager,
            onDoneClicked: () -> Unit = {}
        ): SettingsBottomSheet {
            dialog = SettingsBottomSheet(context, onDoneClicked)
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
        bind = ZegoSettingsBottomSheetBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetSystemBarsHelper.applyParentBackground(view)

        bind.btnDone.setOnClickListener {
            onDoneClicked()
            dismiss()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val settings = listOf(
            SettingsOption("comment", context.getString(R.string.settings_comment), R.drawable.ic_settings, type = OptionType.CHEVRON),
            SettingsOption("costreamer", context.getString(R.string.settings_costreamer), R.drawable.ic_category, type = OptionType.VALUE_CHEVRON, value = context.getString(R.string.settings_everyone)),
            SettingsOption("reservation", context.getString(R.string.settings_reservation), R.drawable.ic_settings, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("multiguest", context.getString(R.string.settings_multiguest), R.drawable.ic_category, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("topic", context.getString(R.string.settings_topic), R.drawable.ic_settings, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("layout", context.getString(R.string.settings_layout), R.drawable.ic_layout, type = OptionType.VALUE_CHEVRON, value = context.getString(R.string.settings_default)),
            SettingsOption("unmute_gift", context.getString(R.string.settings_unmute_gift), R.drawable.ic_gift, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("allow_gift", context.getString(R.string.settings_allow_gift), R.drawable.ic_gift, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("luckybox", context.getString(R.string.settings_luckybox), R.drawable.ic_gift, type = OptionType.TOGGLE, isChecked = true),
            SettingsOption("moderators", context.getString(R.string.settings_moderators), R.drawable.ic_settings, type = OptionType.CHEVRON)
        )

        val adapter = SettingsOptionsAdapter(settings)

        bind.rvSettings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    enum class OptionType {
        CHEVRON,
        VALUE_CHEVRON,
        TOGGLE
    }

    data class SettingsOption(
        val id: String,
        val title: String,
        val iconResId: Int,
        val type: OptionType,
        val value: String? = null,
        var isChecked: Boolean = false
    )

    inner class SettingsOptionsAdapter(
        private val options: List<SettingsOption>
    ) : RecyclerView.Adapter<SettingsOptionsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_setting_icon)
            val title: TextView = view.findViewById(R.id.tv_setting_title)
            val value: TextView = view.findViewById(R.id.tv_setting_value)
            val chevron: ImageView = view.findViewById(R.id.iv_chevron)
            val switchToggle: SwitchCompat = view.findViewById(R.id.switch_toggle)
            val divider: View = view.findViewById(R.id.divider)
            val root: View = view.findViewById(R.id.ll_setting_row)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_settings_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val option = options[position]
            holder.title.text = option.title
            holder.icon.setImageResource(option.iconResId)

            when (option.type) {
                OptionType.CHEVRON -> {
                    holder.value.visibility = View.GONE
                    holder.chevron.visibility = View.VISIBLE
                    holder.switchToggle.visibility = View.GONE
                }
                OptionType.VALUE_CHEVRON -> {
                    holder.value.visibility = View.VISIBLE
                    holder.value.text = option.value
                    holder.chevron.visibility = View.VISIBLE
                    holder.switchToggle.visibility = View.GONE
                }
                OptionType.TOGGLE -> {
                    holder.value.visibility = View.GONE
                    holder.chevron.visibility = View.GONE
                    holder.switchToggle.visibility = View.VISIBLE
                    holder.switchToggle.isChecked = option.isChecked
                    
                    holder.switchToggle.setOnCheckedChangeListener { _, isChecked ->
                        option.isChecked = isChecked
                    }
                }
            }

            holder.divider.visibility = if (position == options.lastIndex) View.GONE else View.VISIBLE
        }

        override fun getItemCount(): Int = options.size
    }
}
