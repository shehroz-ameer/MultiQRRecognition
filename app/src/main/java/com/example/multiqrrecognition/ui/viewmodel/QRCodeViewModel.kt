package com.example.multiqrrecognition.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.multiqrrecognition.data.model.QRCode
import com.example.multiqrrecognition.data.repository.QRCodeRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(private val repository: QRCodeRepository) : ViewModel() {

    private val _qrCodes = MutableLiveData<List<QRCode>>()
    val qrCodes: LiveData<List<QRCode>> = _qrCodes

    fun addQRCode(qrCode: QRCode) {
        repository.addQRCode(qrCode)
        _qrCodes.value = repository.getQRCodes()
    }
}