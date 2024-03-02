package com.example.foodgasm.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.foodgasm.data.User
import com.example.foodgasm.utils.Constants.USER_COLLECTION
import com.example.foodgasm.utils.RegisterFieldState
import com.example.foodgasm.utils.RegisterValidation
import com.example.foodgasm.utils.Resource
import com.example.foodgasm.utils.validateEmail
import com.example.foodgasm.utils.validatePassword
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseAuth:FirebaseAuth,
    private val fireStoreInstance:FirebaseFirestore
): ViewModel() {
    private val _register = MutableStateFlow<Resource<User>>(Resource.Initial())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldState>()
    val validation = _validation.receiveAsFlow()
    fun createAccountWithEmailAndPassword(user:User,password:String) {
        val validationResult=checkValidation(user, password)
        if(validationResult) {
            runBlocking {
                _register.value = Resource.Loading()
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    firebaseAuth.currentUser?.sendEmailVerification()
                    it.user?.let {
                        saveUserInfo(it.uid,user)
                        Log.d(TAG, "Success")
                    }
                }.addOnFailureListener {

                    Log.d(TAG, "Failed")
                }
        }else{
            val registerFieldState=RegisterFieldState(
                validateEmail(user.email), validatePassword(password)
            )
           runBlocking {
               _validation.send(registerFieldState)
           }
        }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        fireStoreInstance.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String):Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister = emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success
        return shouldRegister
    }
}
