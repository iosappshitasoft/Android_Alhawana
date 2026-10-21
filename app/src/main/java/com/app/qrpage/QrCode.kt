package com.app.qrpage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import com.app.alhawanajd.BaseFragmentActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ActivityQrCodeBinding
import com.app.model.GetSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.math.roundToInt

class QrCode : BaseFragmentActivity() {

    private lateinit var binding: ActivityQrCodeBinding
    private var profileLink: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindProfileData()
        bindClicks()
    }

    private fun bindProfileData() {
        val name = GetSet.getName().orEmpty()
        val userName = GetSet.getUserName().orEmpty()
        profileLink = getIntent().getStringExtra("profile_line").orEmpty()

        binding.nameText.text = name.ifBlank { userName }
        binding.handleText.text = if (userName.isNotBlank()) "@$userName" else ""
        binding.usernameText.text = getString(R.string.app_name)

        Glide.with(this)
            .load(GetSet.getUserImage())
            .placeholder(R.drawable.default_profile_image)
            .error(R.drawable.default_profile_image)
            .transform(CircleCrop())
            .into(binding.avatarImage)

        renderQr(GetSet.getUserId())
    }

    private fun bindClicks() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnScan.setOnClickListener {
            // Hook up your scan screen here when available
            val intent = Intent(this@QrCode, QrScanActivity::class.java)
            startActivity(intent)
        }
        binding.btnCopyCard.setOnClickListener { copyLinkToClipboard(profileLink) }
        binding.btnShareCard.setOnClickListener { shareProfileLink(profileLink) }
    }



    private fun renderQr(content: String) {
        val size = dpToPx(220f)
        val bitmap = generateQrBitmap(content, size)
        if (bitmap != null) {
            binding.qrImage.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Could not generate QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun dpToPx(dp: Float): Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).roundToInt()
    }

    private fun copyLinkToClipboard(link: String) {
        if (link.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("profile_link", link))
        Toast.makeText(this, getString(R.string.linkCopied), Toast.LENGTH_SHORT).show()
    }

    private fun shareProfileLink(link: String) {
        if (link.isBlank()) return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareTo)))
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        // no-op for now
    }
}