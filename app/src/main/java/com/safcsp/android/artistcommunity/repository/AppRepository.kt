package com.safcsp.android.artistcommunity.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.safcsp.android.artistcommunity.data.User


class AppRepository() {
    //  private  var app:Application = application
    private var firebaseAuth = FirebaseAuth.getInstance()
    var userMutableLiveData = MutableLiveData<FirebaseUser>()
    var logoutMutableLiveData = MutableLiveData<Boolean>()

init {
    updateStatus()
}

    fun register(email : String,password:String,name : String ) : LiveData<FirebaseUser> {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {task ->
                val user = User(name = name)
                Log.i("SEC : ","sign up seccessful")
                userMutableLiveData.value = firebaseAuth.currentUser
                firebaseAuth.currentUser?.uid?.let {
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(it).setValue(user).addOnSuccessListener {
                            Log.i("SEC1 : ", "sign up with additonal info successfully")
                        }.addOnFailureListener{exception ->
                            Log.i("SEC fail : ", "$exception")
                        }
                }
            }
        return  userMutableLiveData
    }

    fun login(email: String, password: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(OnCompleteListener<AuthResult>() {
                    if (it.isSuccessful) {
                       // userMutableLiveData.postValue(FirebaseAuth.getInstance().currentUser)
                        userMutableLiveData.setValue(FirebaseAuth.getInstance().currentUser)
                        Log.d("rawan", "login successful")
                    } else {
                        Log.d("rawan", "login failed")
                        // Toast.makeText(this,"registration failed",Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        logoutMutableLiveData.postValue(true)
    }
    fun updateStatus(){
        if(firebaseAuth.currentUser !=null){
            userMutableLiveData.postValue(firebaseAuth.currentUser)
            logoutMutableLiveData.postValue(false)
        }
    }
}