package com.example.multiqrrecognition.data.repository

import com.example.multiqrrecognition.data.model.QRCode
import javax.inject.Inject

class QRCodeRepository @Inject constructor() {
    private val qrCodes = mutableListOf<QRCode>()

    fun addQRCode(qrCode: QRCode) {
        qrCodes.add(qrCode)
    }

    fun getQRCodes(): List<QRCode> {
        return qrCodes
    }

    fun clearQRCodes() {
        qrCodes.clear()
    }
}