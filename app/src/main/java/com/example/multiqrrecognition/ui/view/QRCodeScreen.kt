package com.example.multiqrrecognition.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.multiqrrecognition.data.model.QRCode
import com.example.multiqrrecognition.ui.viewmodel.QRCodeViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QRCodeScreen(viewModel: QRCodeViewModel = hiltViewModel()) {
    val qrCodes by viewModel.qrCodes.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExoPlayerView(viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        qrCodes.forEach { qrCode ->
            BasicText(text = qrCode.value)
        }
    }
}

@Composable
fun ExoPlayerView(viewModel: QRCodeViewModel) {
    val context = LocalContext.current
    val exoPlayer = remember {
        val trackSelector: TrackSelector = DefaultTrackSelector(context)
        val loadControl: LoadControl = DefaultLoadControl()
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build().apply {
                val mediaSource = buildMediaSource(context, Uri.parse("https://drive.google.com/uc?export=download&id=1d-jVvjyxuERGc-jOgyuPbGgtTksdsvaE"))
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

    val barcodeDetector = remember {
        BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build().apply {
                setProcessor(MultiProcessor.Builder(BarcodeTrackerFactory(viewModel)).build())
            }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withContext(Dispatchers.Default) {
                val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
                val frame = Frame.Builder().setBitmap(bitmap).build()
                barcodeDetector.receiveFrame(frame)
            }
        }
    }

    AndroidView(
        factory = { PlayerView(context).apply { player = exoPlayer } },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

private fun buildMediaSource(context: Context, uri: Uri): MediaSource {
    val dataSourceFactory = DefaultDataSourceFactory(context, "exoplayer-sample")
    return ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(uri)).also {
            Log.d("ExoPlayer", "MediaSource created for URI: $uri")
        }
}

class BarcodeTrackerFactory(private val viewModel: QRCodeViewModel) : MultiProcessor.Factory<Barcode> {
    override fun create(barcode: Barcode): BarcodeTracker {
        return BarcodeTracker(viewModel)
    }
}

class BarcodeTracker(private val viewModel: QRCodeViewModel) : Tracker<Barcode>() {
    override fun onUpdate(detections: Detector.Detections<Barcode>, barcode: Barcode) {
        viewModel.addQRCode(QRCode(barcode.displayValue))
    }
}

