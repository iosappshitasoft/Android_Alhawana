package com.app.Zego.BottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetPollBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PollBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BtmsheetPollBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Bottom_HashTagDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BtmsheetPollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStart.setOnClickListener {
            Toast.makeText(context, getString(R.string.poll_started), Toast.LENGTH_SHORT).show()
            dismiss()
        }
        
        binding.btnHelp.setOnClickListener {
            Toast.makeText(context, getString(R.string.poll_help_clicked), Toast.LENGTH_SHORT).show()
        }
        
        binding.btnAddOption.setOnClickListener {
            val createPollSheet = CreateNewPollBottomSheet()
            createPollSheet.show(parentFragmentManager, "CreateNewPollBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
