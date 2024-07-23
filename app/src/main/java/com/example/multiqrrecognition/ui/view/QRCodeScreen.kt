package com.example.multiqrrecognition.ui.view

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.multiqrrecognition.data.model.QRCode
import com.example.multiqrrecognition.ui.viewmodel.QRCodeViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay

@Composable
fun QRCodeScreen(viewModel: QRCodeViewModel = hiltViewModel()) {
    val qrCodes by viewModel.qrCodes.observeAsState(emptyList())

    var hasCameraPermission by remember { mutableStateOf(false) }
    var noQRCodeDetected by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (hasCameraPermission) {
            ExoPlayerView(viewModel, onQRCodeDetection = { detected ->
                noQRCodeDetected = !detected
            })
        } else {
            BasicText(text = "Camera permission is required to scan QR codes.")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (noQRCodeDetected) {
            BasicText(text = "No QR codes detected")
        } else {
            qrCodes.forEach { qrCode ->
                BasicText(text = qrCode.value)
            }
        }
    }
}

@Composable
fun ExoPlayerView(viewModel: QRCodeViewModel, onQRCodeDetection: (Boolean) -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaSource = buildMediaSource(context, Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    var textureView: TextureView? by remember { mutableStateOf(null) }

    LaunchedEffect(textureView) {
        textureView?.let { tv ->
            tv.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    Log.d("ExoPlayerDebug", "SurfaceTexture available, setting Surface")
                    exoPlayer.setVideoSurface(Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    Log.d("ExoPlayerDebug", "SurfaceTexture destroyed")
                    exoPlayer.setVideoSurface(null)
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        } ?: Log.d("ExoPlayerDebug", "TextureView is null")
    }

    LaunchedEffect(textureView) {
        while (true) {
            delay(1000L)
            textureView?.let { tv ->
                val bitmap = tv.bitmap
                if (bitmap != null) {
                    Log.d("QRCodeProcessing", "Captured frame for processing, bitmap size: ${bitmap.width}x${bitmap.height}")
                    processFrame(bitmap, viewModel, onQRCodeDetection)
                } else {
                    Log.d("QRCodeProcessing", "TextureView bitmap is null")
                }
            }
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                useController = true
                textureView = TextureView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                (this.videoSurfaceView as? TextureView)?.visibility = View.GONE
                addView(textureView)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

private fun buildMediaSource(context: Context, uri: Uri): MediaSource {
    val dataSourceFactory = DefaultDataSource.Factory(context)
    return ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(uri)).also {
            Log.d("ExoPlayer", "MediaSource created for URI: $uri")
        }
}

private fun processFrame(bitmap: Bitmap, viewModel: QRCodeViewModel, onQRCodeDetection: (Boolean) -> Unit) {
    Log.d("QRCodeProcessing", "Processing frame")
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            if (barcodes.isEmpty()) {
                Log.d("QRCodeProcessing", "No QR codes detected")
                onQRCodeDetection(false)
            } else {
                Log.d("QRCodeProcessing", "QR codes detected")
                onQRCodeDetection(true)
            }
            viewModel.clearQRCodes()
            for (barcode in barcodes) {
                Log.d("QRCodeProcessing", "QR Code detected: ${barcode.displayValue}")
                viewModel.addQRCode(QRCode(barcode.displayValue ?: ""))
            }
        }
        .addOnFailureListener { e ->
            Log.e("QRCodeProcessing", "Failed to process QR codes", e)
        }
}