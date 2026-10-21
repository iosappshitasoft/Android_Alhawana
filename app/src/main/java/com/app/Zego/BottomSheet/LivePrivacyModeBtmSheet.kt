package com.app.Zego.BottomSheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BtmsheetLiveModeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


 class LivePrivacyModeBtmSheet(@get:JvmName("getAdapterContext")val context:Context,
     val onBtnClicked:(String)->Unit): BottomSheetDialogFragment() {

         lateinit var bind: BtmsheetLiveModeBinding
         companion object{
             private const val TAG = "LivePrivacyModeBtmSheet"
              lateinit var dialog : LivePrivacyModeBtmSheet

             fun getInstance(
                 context: Context,
                 fm:FragmentManager,
                 onBtnClicked: (String) -> Unit
             ):LivePrivacyModeBtmSheet{
                 dialog = LivePrivacyModeBtmSheet(context,onBtnClicked)
                 if (dialog.isVisible.not()) {
                     dialog.show(fm, TAG)
                 }
                 return dialog
             }
         }

    override fun onStart() {
        super.onStart()
        setStyle(STYLE_NORMAL, R.style.Bottom_HashTagDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = BtmsheetLiveModeBinding.inflate(inflater,container,false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.apply {
            publicLay.setOnClickListener {
                onBtnClicked(
                    "public"
                )
                dismiss()
            }
            privateLay.setOnClickListener {
                onBtnClicked(
                    "private"
                )
                dismiss()
            }
        }
    }

}