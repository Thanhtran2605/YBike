package com.example.doan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.doan.data.User
import com.example.doan.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
// You don't need create view model and dependencies
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore

) : ViewModel() {

    // status could view data at private mode, have three state, loading,
    // success and failure
    // this is flow
    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    // this is channel. It isn't receive parameters
    // this is using validation
    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User, password: String) {
        /**
         * Call Validation function in here
         * Validation if true, then check create user with email and
         * password.
         * */
        if (checkValidation(user, password)) {

            /* Fix button loading into default. (Using block loading state) */
            runBlocking {
                _register.emit(Resource.Loading())
            }

            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        /* This function is using to save the user data in firebase
                        * firestore */
                        saveUserInfo(it.uid, user)
                    }
                }.addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFieldsState = RegisterFieldsState(
                validateEmail(user.email),
                validatePassword(password)
            )

            runBlocking {
                _validation.send(registerFieldsState)
            }
        }
    }

    // TODO: this method is save User Info
    private fun saveUserInfo(userUid: String, user: User) {
        db.collection("user")
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            } .addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }

    // TODO: this method is check Validation
    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidation is RegisterValidation.Success
                && passwordValidation is RegisterValidation.Success

        return shouldRegister
    }
}