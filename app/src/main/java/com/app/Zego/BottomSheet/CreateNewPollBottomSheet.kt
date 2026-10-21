package com.app.Zego.BottomSheet

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetCreateNewPollBinding
import com.app.alhawanajd.databinding.ItemPollOptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CreateNewPollBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BtmsheetCreateNewPollBinding? = null
    private val binding get() = _binding!!
    
    private val optionsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Bottom_HashTagDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val layoutParams = it.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * 0.95).toInt()
                it.layoutParams = layoutParams
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BtmsheetCreateNewPollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add initial options A and B
        addOption()
        addOption()

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.btnStartPoll.setOnClickListener {
            Toast.makeText(context, getString(R.string.poll_new_started), Toast.LENGTH_SHORT).show()
            dismiss()
        }
        
        binding.btnSave.setOnClickListener {
            Toast.makeText(context, getString(R.string.poll_saved), Toast.LENGTH_SHORT).show()
            dismiss()
        }
        
        binding.btnAddOption.setOnClickListener {
            addOption()
        }
    }

    private fun refreshOptionLabels() {
        optionsList.clear()
        for (i in 0 until binding.optionsContainer.childCount) {
            val view = binding.optionsContainer.getChildAt(i)
            val itemBinding = ItemPollOptionBinding.bind(view)
            val nextLetter = (65 + i).toChar().toString()
            itemBinding.tvOptionLabel.text = nextLetter
            itemBinding.etOptionText.hint = getString(R.string.poll_option_hint, nextLetter)
            optionsList.add(itemBinding.etOptionText.text.toString())
        }
        checkAddButtonVisibility()
    }

    private fun checkAddButtonVisibility() {
        if (binding.optionsContainer.childCount >= 4) {
            binding.btnAddOption.visibility = View.GONE
        } else {
            binding.btnAddOption.visibility = View.VISIBLE
        }
    }

    private fun addOption() {
        if (binding.optionsContainer.childCount >= 4) {
            Toast.makeText(context, getString(R.string.poll_max_options), Toast.LENGTH_SHORT).show()
            return
        }

        val nextIndex = binding.optionsContainer.childCount
        val nextLetter = (65 + nextIndex).toChar().toString()

        val itemBinding = ItemPollOptionBinding.inflate(layoutInflater, binding.optionsContainer, false)
        itemBinding.tvOptionLabel.text = nextLetter
        itemBinding.etOptionText.hint = getString(R.string.poll_option_hint, nextLetter)
        itemBinding.tvCharCount.text = getString(R.string.poll_char_count, 0)

        if (nextIndex >= 2) {
            itemBinding.ivRemoveOption.visibility = View.VISIBLE
            itemBinding.ivRemoveOption.setOnClickListener {
                binding.optionsContainer.removeView(itemBinding.root)
                refreshOptionLabels()
            }
        }

        itemBinding.etOptionText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                itemBinding.tvCharCount.text = getString(R.string.poll_char_count, length)
                val currentIndex = binding.optionsContainer.indexOfChild(itemBinding.root)
                if (currentIndex in 0 until optionsList.size) {
                    optionsList[currentIndex] = s.toString()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.optionsContainer.addView(itemBinding.root)
        optionsList.add("")
        checkAddButtonVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
