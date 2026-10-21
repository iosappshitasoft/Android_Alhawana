package com.app.Zego.BottomSheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.Zego.Adapter.GiftAdapter
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.model.Gift
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SendGiftBottomSheet(
    private val gifts: List<Gift>,
    private val onGiftSelected: (Gift) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var recyclerGifts: RecyclerView
    private lateinit var tvGiftName: TextView
    private lateinit var tvGiftCount: TextView
    private lateinit var tvSendGift: TextView
    private lateinit var rechargeLay: LinearLayout

    private var selectedGift: Gift? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(
            requireContext(),
            R.style.VideoCommentNewDialog
        ) // your custom theme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.mls_layout_send_gift, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerGifts = view.findViewById(R.id.recycler_gifts)
        tvGiftName = view.findViewById(R.id.tv_gift_name)
        tvGiftCount = view.findViewById(R.id.tv_gift_count)
        tvSendGift = view.findViewById(R.id.tv_send_gift)
        rechargeLay = view.findViewById(R.id.rechargeLay)
        val rechargeLayTop = view.findViewById<LinearLayout>(R.id.rechargeLayTop)

        val rechargeClickListener = View.OnClickListener {
            App.preventMultipleClick(it)
            dismiss()
            // Open Recharge BottomSheet
            val rechargeSheet = RechargeBottomSheet()
            rechargeSheet.show(parentFragmentManager, "RechargeBottomSheet")
        }

        rechargeLay.setOnClickListener(rechargeClickListener)
        rechargeLayTop?.setOnClickListener(rechargeClickListener)

        recyclerGifts.layoutManager = GridLayoutManager(requireContext(), 4)

        recyclerGifts.adapter = GiftAdapter(gifts) { gift ->
            selectedGift = gift
            tvGiftName.text = gift.giftTitle
            tvGiftCount.text = "${gift.giftGems} gems"
        }

        tvSendGift.setOnClickListener {
            if (selectedGift == null) {
                Toast.makeText(requireContext(), getString(R.string.select_gift_tosend), Toast.LENGTH_SHORT).show()
            } else {
                onGiftSelected(selectedGift!!)
                dismiss()
            }
        }
    }
}
