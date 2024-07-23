package com.example.multiqrrecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.multiqrrecognition.ui.theme.MultiQRRecognitionTheme
import com.example.multiqrrecognition.ui.view.QRCodeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiQRRecognitionTheme {
                QRCodeScreen()
            }
        }
    }
}