package com.example.foodgasm.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgasm.utils.Resource
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
):ViewModel() {

    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    private val _login= MutableSharedFlow<Resource<FirebaseUser>>()
    val login:SharedFlow<Resource<FirebaseUser>> = _login

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    fun login(email:String, password:String){
        viewModelScope.launch {
            _login.emit(Resource.Loading())
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                if (firebaseAuth.currentUser?.isEmailVerified == true) {
                    viewModelScope.launch {
                        it.user?.let {
                            _login.emit(Resource.Success(it))
                        }
                    }
                }else{
                    viewModelScope.launch {
                        _login.emit(Resource.Verify())
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun resetPassword(email: String){
        viewModelScope.launch{
            _resetPassword.emit(Resource.Loading())
        }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Success(email))
                }
        }.addOnFailureListener {
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Error(it.message.toString()))
                }
            }

    }
}