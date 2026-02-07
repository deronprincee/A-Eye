package com.example.aeye

import kotlinx.coroutines.flow.MutableStateFlow
import  kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ViewModel to manage user authentication using Firebase Auth and Firestore
class AuthViewModel : ViewModel(){

    // Initialize Firebase Authentication instance
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    //Initialize tracking for login state in real-time
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    // Initialize liveData to track authentication state and respond in UI
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // Checks current user status when ViewModel is created
    init{
        checkAuthStatus()
        _isLoggedIn.value = auth.currentUser != null
    }

    // Logs in a user and updates login state
    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoggedIn.value = task.isSuccessful
            }
    }

    // Signs out the current user and updates auth state
    fun logoutUser() {
        auth.signOut()
        _authState.postValue(AuthState.Unauthenticated)
        _isLoggedIn.value = false
    }

    // Determines if a user is already logged in and updates state accordingly
    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.postValue(AuthState.Unauthenticated)
        }else{
            _authState.postValue(AuthState.Authenticated)
        }
    }

    // Determines if a user is already logged in and updates state accordingly
    fun login(email : String, password : String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.postValue(AuthState.Error("Email or password cant be empty"))
            return

        }
        _authState.postValue(AuthState.Loading)

        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.postValue(AuthState.Authenticated)

                }else{
                    _authState.postValue(
                        AuthState.Error(task.exception?.message ?: "Something went wrong"))
                }

            }

    }

    // Registers a new user and creates their Firestore profile document
    fun signup(email : String, password : String,name:String){
        if(email.isEmpty() || password.isEmpty()||name.isEmpty()){
            _authState.postValue(AuthState.Error("All fields are required"))
            return

        }
        _authState.postValue(AuthState.Loading)

        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "created_at" to System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            _authState.postValue(AuthState.Authenticated)
                        }
                        .addOnFailureListener {
                            _authState.postValue(AuthState.Error("Signup succeeded but profile save failed."))
                        }
                } else {
                    _authState.postValue(AuthState.Error(task.exception?.message ?: "Signup failed."))
                }
            }
    }

    // Signs out the user and updates auth state
    fun signout(){
        auth.signOut()
        _authState.postValue(AuthState.Unauthenticated)
    }

    // Returns the currently authenticated user's UID
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }


}

// Class for different authentication states for UI rendering
sealed class  AuthState{
    object  Authenticated : AuthState()
    object  Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}