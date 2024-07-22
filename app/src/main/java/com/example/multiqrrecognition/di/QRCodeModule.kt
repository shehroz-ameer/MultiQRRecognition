package com.example.multiqrrecognition.di

import com.example.multiqrrecognition.data.repository.QRCodeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QRCodeModule {

    @Provides
    @Singleton
    fun provideQRCodeRepository(): QRCodeRepository {
        return QRCodeRepository()
    }
}