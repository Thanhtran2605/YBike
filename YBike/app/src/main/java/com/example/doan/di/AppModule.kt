package com.example.doan.di

import com.example.doan.firebase.FirebaseCommon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Tất cả các thành phần phụ thuộc trong module vẫn phụ nếu ứng dụng sống.
// Tất cả đều gọi qua trang này để khởi tạo đối tượng.
@Module
@InstallIn((SingletonComponent::class))
object AppModule {

    // Cung cấp một phần phụ thuộc mới
    @Provides

    // Chỉ tạo một phiên bản trong toàn bộ ứng dụng
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestoreDatabase() = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseCommon(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ) = FirebaseCommon(firestore, firebaseAuth)

    // Cung cấp bộ lưu trữ firebase trong ứng dụng
    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().reference
}