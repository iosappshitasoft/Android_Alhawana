package com.app.Zego.BottomSheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ZegoAddCategoryBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCategoryBottomSheet(
    @get:JvmName("getAdapterContext") val context: Context,
    val onCategorySelected: (CategoryOption) -> Unit
) : BottomSheetDialogFragment() {

    lateinit var bind: ZegoAddCategoryBottomSheetBinding

    companion object {
        private const val TAG = "AddCategoryBottomSheet"
        private lateinit var dialog: AddCategoryBottomSheet

        fun getInstance(
            context: Context,
            fm: FragmentManager,
            onCategorySelected: (CategoryOption) -> Unit
        ): AddCategoryBottomSheet {
            dialog = AddCategoryBottomSheet(context, onCategorySelected)
            if (!dialog.isVisible) {
                dialog.show(fm, TAG)
            }
            return dialog
        }
    }

    override fun getTheme(): Int = BottomSheetSystemBarsHelper.applyTheme()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        BottomSheetSystemBarsHelper.configure(dialog, maxHeightFraction = 0.8f)
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
        bind = ZegoAddCategoryBottomSheetBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetSystemBarsHelper.applyParentBackground(view)

        bind.tvAddMore.setOnClickListener {
            dismiss()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val categories = listOf(
            CategoryOption("entertainment", getString(R.string.cat_entertainment_fun), R.drawable.ic_cat_entertainment),
            CategoryOption("esports", getString(R.string.cat_esports_gaming), R.drawable.ic_cat_esports, hasSubcategories = true),
            CategoryOption("culture", getString(R.string.cat_culture_religion), R.drawable.ic_cat_culture),
            CategoryOption("sports", getString(R.string.cat_sports), R.drawable.ic_cat_sports),
            CategoryOption("music", getString(R.string.cat_music), R.drawable.ic_cat_music),
            CategoryOption("finance", getString(R.string.cat_finance_business), R.drawable.ic_cat_finance),
            CategoryOption("food", getString(R.string.cat_food_cooking), R.drawable.ic_cat_food),
            CategoryOption("art", getString(R.string.cat_art_crafting), R.drawable.ic_cat_art),
            CategoryOption("news", getString(R.string.cat_news_politics), R.drawable.ic_cat_news),
            CategoryOption("education", getString(R.string.cat_society_education), R.drawable.ic_cat_education),
            CategoryOption("other", getString(R.string.cat_other_content), R.drawable.ic_cat_other, isGreyIcon = true)
        )

        val adapter = CategoryOptionsAdapter(categories) { selected ->
            onCategorySelected(selected)
            dismiss()
        }

        bind.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    data class CategoryOption(
        val id: String,
        val name: String,
        val iconResId: Int,
        val hasSubcategories: Boolean = false,
        val isGreyIcon: Boolean = false
    )

    inner class CategoryOptionsAdapter(
        private val categories: List<CategoryOption>,
        private val onCategorySelected: (CategoryOption) -> Unit
    ) : RecyclerView.Adapter<CategoryOptionsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconContainer: FrameLayout = view.findViewById(R.id.fl_icon_container)
            val glowView: View = view.findViewById(R.id.v_glow)
            val icon: ImageView = view.findViewById(R.id.iv_category_icon)
            val name: TextView = view.findViewById(R.id.tv_category_name)
            val chevron: ImageView = view.findViewById(R.id.iv_chevron)
            val divider: View = view.findViewById(R.id.divider)
            val root: View = view.findViewById(R.id.ll_category_row)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_category_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.name.text = category.name
            holder.icon.setImageResource(category.iconResId)

            holder.iconContainer.setBackgroundResource(
                if (category.isGreyIcon) R.drawable.bg_category_icon_grey
                else R.drawable.bg_category_icon_purple
            )

            holder.glowView.visibility = if (category.isGreyIcon) View.GONE else View.VISIBLE

            holder.chevron.visibility = if (category.hasSubcategories) View.VISIBLE else View.GONE
            holder.divider.visibility = if (position == categories.lastIndex) View.GONE else View.VISIBLE

            holder.root.setOnClickListener {
                onCategorySelected(category)
            }
        }

        override fun getItemCount(): Int = categories.size
    }
}
