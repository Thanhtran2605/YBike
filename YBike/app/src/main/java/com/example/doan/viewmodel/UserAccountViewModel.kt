package com.example.doan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.OldMotorApplication
import com.example.doan.data.User
import com.example.doan.util.RegisterValidation
import com.example.doan.util.Resource
import com.example.doan.util.validateEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,

    // Dùng để truy cập tốt trình phân giải nội dung
    app: Application
) : AndroidViewModel(app) {

    // Trạng thái người dùng
    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    // Luồng để cho chỉnh sửa thông tin cá nhân
    private val _updateInfo = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val updateInfo = _updateInfo.asStateFlow()

    init {
        getUser()
    }

    //TODO: this method is get information user from firestore
    fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }

        firestore.collection("user").document(auth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch {
                        _user.emit(Resource.Success(it))
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _user.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    //TODO: this method is mofidy information of user
    fun updateUser(user: User, imageUri: Uri?) {
        // Validation inputs user
        val areInputsValid = validateEmail(user.email) is RegisterValidation.Success
                && user.firstName.trim().isNotEmpty()
                && user.lastName.trim().isNotEmpty()

        // Nếu sai
        if (!areInputsValid) {
            viewModelScope.launch {
                _user.emit(Resource.Error("Dữ liệu không hợp lệ."))
            }
            return
        }

        // Trạng thái tải mỗi khi mới vào
        viewModelScope.launch {
            _updateInfo.emit(Resource.Loading())
        }

        if (imageUri == null) {
            saveUserInformation(user, true)
        } else {
            saveUserInformationWithNewImage(user, imageUri)
        }
    }

    //TODO: This method is using to save user information with new image
    private fun saveUserInformationWithNewImage(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                // Khi tải hình ảnh lên firebase, ta muốn lấy mảng byte của hình ảnh đó.
                // Đầu tiên, lấy bitmap của hình ảnh đó.
                // Dùng ta cần trình phần giải nội dung getApplication()
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<OldMotorApplication>().contentResolver,
                    imageUri
                )

                // Dùng nén hình ảnh và giảm chất lượng không có kích thước lớn.
                // Hình ảnh sẽ lưu dưới dạng mảng byte.
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)

                // Lấy mảng byte của hình ảnh đó.
                val imageByteArray = byteArrayOutputStream.toByteArray()

                // Tạo thư mục lưu hình ảnh trong thử mục với tên thư mục/id/id random
                val imageDirectory =
                    storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")

                // Đưa hình ảnh vào thư mục. Function await() tạm dừng hàm ở đây.
                val result = imageDirectory.putBytes(imageByteArray).await()

                // Nhận một chuỗi hình ảnh
                val imageUrl = result.storage.downloadUrl.await().toString()

                // Lưu thông tin người dùng, false chỉ lưu thông tin vì ảnh có rồi.
                saveUserInformation(user.copy(imagePath = imageUrl), false)

            } catch (e: java.lang.Exception) {
                viewModelScope.launch {
                    _user.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    //TODO: This method is using to save user information
    private fun saveUserInformation(user: User, b: Boolean) {
        // Sử dụng runTransaction(), nếu đúng thì chúng ta truy suất hình ảnh cũ trước,
        // sau đó mới cập nhật ảnh người dùng.
        firestore.runTransaction { transaction ->
            val documentRef = firestore.collection("user").document(auth.uid!!)

            // Nếu đúng là có thay đổi thông tin nhưng không thay đổi ảnh
            if (b) {
                val currentUser = transaction.get(documentRef).toObject(User::class.java)
                val newUser = user.copy(imagePath = currentUser?.imagePath ?: "")
                transaction.set(documentRef, newUser)
            } else {
                // Nếu không thay đổi bất cứ thứ gì
                transaction.set(documentRef, user)
            }

        }.addOnSuccessListener {
            viewModelScope.launch {
                _updateInfo.emit(Resource.Success(user))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _updateInfo.emit(Resource.Error(it.message.toString()))
            }
        }
    }
}