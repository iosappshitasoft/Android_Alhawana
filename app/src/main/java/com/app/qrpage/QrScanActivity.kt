package com.app.qrpage

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.app.alhawanajd.BaseFragmentActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.profile.MyProfileActivity
import com.app.alhawanajd.profile.OthersProfileActivity
import com.app.model.GetSet
import com.app.utils.Constants
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.android.gms.common.util.CollectionUtils.mapOf
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.HashMap

class QrScanActivity : BaseFragmentActivity(), QRCodeReaderView.OnQRCodeReadListener {

    private lateinit var qrReaderView: QRCodeReaderView
    private var scannedOnce = false
    private var scanBoxRect: RectF? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                decodeImageQr(uri)
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startScanner()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        qrReaderView = findViewById(R.id.qrReaderView)
        qrReaderView.setOnQRCodeReadListener(this)
        qrReaderView.setBackCamera()
        qrReaderView.setAutofocusInterval(1500L)

        val scanBox = findViewById<View>(R.id.scanBox)
        scanBox.post {
            scanBoxRect = RectF(
                scanBox.left.toFloat(),
                scanBox.top.toFloat(),
                scanBox.right.toFloat(),
                scanBox.bottom.toFloat()
            )
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.btnMyQr).setOnClickListener {
            startActivity(Intent(this, QrCode::class.java))
        }

        findViewById<LinearLayout>(R.id.btnGallery).setOnClickListener {
            pickImage.launch("image/*")
        }

        startScanAnimation()
    }

    override fun onResume() {
        super.onResume()
        ensurePermissionAndStart()
    }

    override fun onPause() {
        super.onPause()
        qrReaderView.stopCamera()
    }

    override fun onQRCodeRead(text: String?, points: Array<out android.graphics.PointF>?) {
        if (scannedOnce || text.isNullOrBlank()) return

        if (!isInsideScanBox(points)) return

        handleScanResult(text)
    }

    override fun onNetworkChanged(isConnected: Boolean) {
        // no-op
    }

    private fun ensurePermissionAndStart() {
        val hasCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCamera) {
            startScanner()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanner() {
        scannedOnce = false
        qrReaderView.startCamera()
    }

    private fun isInsideScanBox(points: Array<out android.graphics.PointF>?): Boolean {
        val box = scanBoxRect ?: return true
        if (points == null || points.isEmpty()) return false
        for (p in points) {
            if (!box.contains(p.x, p.y)) return false
        }
        return true
    }

    private fun handleScanResult(text: String?) {
        if (text.isNullOrBlank()) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
            return
        }

        scannedOnce = true

        Log.e("hiii", "handleScanResult: "+text )

        if (text == GetSet.getUserId()) {
            val profile = Intent(this, MyProfileActivity::class.java)
            profile.putExtra(Constants.TAG_EDIT, true)
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(profile)
        } else {
            val profile = Intent(this, OthersProfileActivity::class.java)
            profile.putExtra(Constants.TAG_USER_ID, text)
            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(profile)
        }
        finish()
    }

    private fun decodeImageQr(uri: Uri) {
        val bitmap = loadBitmap(uri)
        if (bitmap == null) {
            Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show()
            return
        }
         decodeQrFromImage(bitmap){
            if (it != null) {
                handleScanResult(it)
            } else {
                Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBitmap(uri: Uri): android.graphics.Bitmap? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeQrFromImage(bitmap: Bitmap, onResult: (String?) -> Unit) {

        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        val input = InputImage.fromBitmap(bitmap, 0)

        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val value = barcodes[0].rawValue
                    onResult(value)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }




    private fun startScanAnimation() {
        val scanLine = findViewById<View>(R.id.scanLine)

        val scanBox = findViewById<View>(R.id.scanBox)

        scanBox.post {
            val height = scanBox.height.toFloat()

            val animator = ValueAnimator.ofFloat(0f, height)
            animator.duration = 1800
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE

            animator.addUpdateListener {
                val value = it.animatedValue as Float
                scanLine.translationY = value
            }

            animator.start()
        }
    }
}

