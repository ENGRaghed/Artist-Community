package com.safcsp.android.artistcommunity.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.safcsp.android.artistcommunity.repository.AppRepository

class LoginViewModel() :ViewModel(){
    private var appRepository:AppRepository = AppRepository()
    var userMutableLiveData:MutableLiveData<FirebaseUser> = appRepository.userMutableLiveData
    var isLoggedInMutableLiveData:MutableLiveData<Boolean> = appRepository.logoutMutableLiveData

     fun register(email:String, password:String,name:String): LiveData<FirebaseUser> {
//appRepository.register(email,password,name)
       return  appRepository.register(email,password,name)
       // userMutableLiveData=appRepository.userMutableLiveData
    }
     fun login(email:String, password:String){
        appRepository.login(email,password)
        // userMutableLiveData=appRepository.userMutableLiveData
    }
     fun logout(){
        appRepository.logout()
        // userMutableLiveData=appRepository.userMutableLiveData
    }
}