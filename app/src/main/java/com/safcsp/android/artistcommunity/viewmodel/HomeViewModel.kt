package com.safcsp.android.artistcommunity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.safcsp.android.artistcommunity.repository.AppRepository

class HomeViewModel: ViewModel() {

    private var appRepository: AppRepository = AppRepository()
    var userMutableLiveData: MutableLiveData<FirebaseUser> = appRepository.userMutableLiveData
    var loggeduserMutableLiveData: MutableLiveData<Boolean> = appRepository.logoutMutableLiveData

}