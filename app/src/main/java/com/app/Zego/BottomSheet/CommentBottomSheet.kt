package com.app.Zego.BottomSheet

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class CommentBottomSheet(
    private val context: Context,
    private val isHostChatDisabled: Boolean,
    private val onSendClick: (message: String) -> Unit
) {

    private lateinit var commentEditDialog: BottomSheetDialog
    private lateinit var edtMessageNew: EditText
    private lateinit var btnSendNew: ImageView

    fun show() {
        val commentSentDialog = LayoutInflater.from(context).inflate(R.layout.bottom_lay, null)
        commentEditDialog = BottomSheetDialog(context, R.style.DialogKeyBoardStyle)

        commentEditDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentEditDialog.setContentView(commentSentDialog)
        commentEditDialog.behavior.isHideable = true
        commentEditDialog.setCanceledOnTouchOutside(true)

        edtMessageNew = commentSentDialog.findViewById(R.id.edtMessage_new)
        btnSendNew = commentSentDialog.findViewById(R.id.btnSend_new)

        edtMessageNew.requestFocus()
        edtMessageNew.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                commentEditDialog.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                )
            } else {
                commentEditDialog.dismiss()
            }
        }

        // Disable BottomSheet if host chat is disabled
        if (isHostChatDisabled) {
            Toast.makeText(context, context.getString(R.string.host_disabled_chat), Toast.LENGTH_SHORT).show()
            commentEditDialog.dismiss()
            return
        }

        btnSendNew.setOnClickListener {
            val message = edtMessageNew.text.toString()
            if (message.isEmpty()) {
                App.makeToast(context.getString(R.string.enter_your_message))
            } else {
                onSendClick(message)
                commentEditDialog.dismiss()
            }
        }

        commentEditDialog.show()
    }
}