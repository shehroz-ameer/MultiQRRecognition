package com.example.multiqrrecognition.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.multiqrrecognition.data.model.QRCode
import com.example.multiqrrecognition.data.repository.QRCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(private val repository: QRCodeRepository) : ViewModel() {

    private val _qrCodes = MutableLiveData<List<QRCode>>()
    val qrCodes: LiveData<List<QRCode>> = _qrCodes

    fun addQRCode(qrCode: QRCode) {
        repository.addQRCode(qrCode)
        _qrCodes.value = repository.getQRCodes()
    }

    fun clearQRCodes() {
        repository.clearQRCodes()
        _qrCodes.value = repository.getQRCodes()
    }
}