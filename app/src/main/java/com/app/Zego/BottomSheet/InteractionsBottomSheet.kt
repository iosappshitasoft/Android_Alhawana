package com.app.Zego.BottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetInteractionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InteractionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BtmsheetInteractionsBinding? = null
    private val binding get() = _binding!!

    var onPollClicked: (() -> Unit)? = null
    var onGiftsClicked: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Bottom_HashTagDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BtmsheetInteractionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPoll.setOnClickListener {
            dismiss()
            onPollClicked?.invoke()
        }

        binding.btnGifts.setOnClickListener {
            dismiss()
            onGiftsClicked?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
