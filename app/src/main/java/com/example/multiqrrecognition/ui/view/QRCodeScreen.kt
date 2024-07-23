package com.example.multiqrrecognition.ui.view

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.multiqrrecognition.ui.viewmodel.QRCodeViewModel

@Composable
fun QRCodeScreen(viewModel: QRCodeViewModel = hiltViewModel()) {
    val qrCodes by viewModel.qrCodes.observeAsState(emptyList())

    var hasCameraPermission by remember { mutableStateOf(false) }
    var noQRCodeDetected by remember { mutableStateOf(true) }

    // Launcher to request camera permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    // Request camera permission on first composable launch
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (hasCameraPermission) {
            // Show the video and handle QR code detection
            ExoPlayerView(viewModel, onQRCodeDetection = { detected ->
                noQRCodeDetected = !detected
            })
        } else {
            BasicText(text = "Camera permission is required to scan QR codes.")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display a message if no QR codes are detected, otherwise list the detected QR codes
        if (noQRCodeDetected) {
            BasicText(text = "No QR codes detected")
        } else {
            qrCodes.forEach { qrCode ->
                BasicText(text = qrCode.value)
            }
        }
    }
}