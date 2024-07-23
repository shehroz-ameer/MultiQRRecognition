package com.example.multiqrrecognition.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multiqrrecognition.data.model.QRCode
import com.example.multiqrrecognition.ui.viewmodel.QRCodeViewModel
import com.example.multiqrrecognition.utils.Constants.VIDEO_URI
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
import timber.log.Timber

@Composable
fun ExoPlayerView(viewModel: QRCodeViewModel, onQRCodeDetection: (Boolean) -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaSource = buildMediaSource(context, Uri.parse(VIDEO_URI))
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
                    Timber.tag("ExoPlayerDebug").d("SurfaceTexture available, setting Surface")
                    exoPlayer.setVideoSurface(Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    Timber.tag("ExoPlayerDebug").d("SurfaceTexture destroyed")
                    exoPlayer.setVideoSurface(null)
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        } ?: Timber.tag("ExoPlayerDebug").d("TextureView is null")
    }

    LaunchedEffect(textureView) {
        while (true) {
            delay(1000L)
            textureView?.let { tv ->
                val bitmap = tv.bitmap
                if (bitmap != null) {
                    Timber.tag("QRCodeProcessing")
                        .d("Captured frame for processing, bitmap size: %s x %s", bitmap.width, bitmap.height)
                    processFrame(bitmap, viewModel, onQRCodeDetection)
                } else {
                    Timber.tag("QRCodeProcessing").d("TextureView bitmap is null")
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
            Timber.tag("ExoPlayer").d("MediaSource created for URI: %s", uri)
        }
}

private fun processFrame(bitmap: Bitmap, viewModel: QRCodeViewModel, onQRCodeDetection: (Boolean) -> Unit) {
    Timber.tag("QRCodeProcessing").d("Processing frame")
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            if (barcodes.isEmpty()) {
                Timber.tag("QRCodeProcessing").d("No QR codes detected")
                onQRCodeDetection(false)
            } else {
                Timber.tag("QRCodeProcessing").d("QR codes detected")
                onQRCodeDetection(true)
            }
            viewModel.clearQRCodes()
            for (barcode in barcodes) {
                Timber.tag("QRCodeProcessing").d("QR Code detected: %s", barcode.displayValue)
                viewModel.addQRCode(QRCode(barcode.displayValue ?: ""))
            }
        }
        .addOnFailureListener { e ->
            Timber.tag("QRCodeProcessing").e(e, "Failed to process QR codes")
        }
}