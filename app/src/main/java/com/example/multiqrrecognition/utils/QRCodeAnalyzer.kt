package com.example.multiqrrecognition.utils

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyzer(private val onQRCodeDetected: (List<Barcode>) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    override fun  analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                onQRCodeDetected(barcodes)
            }
            .addOnFailureListener { exception ->
                Log.e("QRCodeAnalyzer", "QR code detection failed", exception)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}