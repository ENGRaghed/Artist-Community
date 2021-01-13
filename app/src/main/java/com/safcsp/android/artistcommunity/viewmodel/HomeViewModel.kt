package com.safcsp.android.artistcommunity.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safcsp.android.artistcommunity.HomePhoto
import com.safcsp.android.artistcommunity.data.User
import com.safcsp.android.artistcommunity.repository.AppRepository
import com.safcsp.android.artistcommunity.ui.HomeItem

class HomeViewModel: ViewModel() {

    private var appRepository: AppRepository = AppRepository()
    var userMutableLiveData: MutableLiveData<FirebaseUser> = appRepository.userMutableLiveData
    var loggeduserMutableLiveData: MutableLiveData<Boolean> = appRepository.logoutMutableLiveData


    fun allPhotos(): LiveData<List<HomeItem>>{ //List<HomeItem>{
        val ref= FirebaseDatabase.getInstance().getReference("Photos")
        var items= MutableLiveData<List<HomeItem>>()
        var homeItems= mutableListOf<HomeItem>()

        ref.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            @SuppressLint("RestrictedApi")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(item in snapshot.children){



                        val ref= FirebaseDatabase.getInstance().getReference("Users")
                        var aUser: User?=null
                        ref.orderByKey().addValueEventListener(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                            }
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()){
                                    for (user in snapshot.children){
                                        if(item.child("publisher").value.toString()==user.key.toString()) {
                                            aUser = User(
                                                name = user.child("name").value.toString(),
                                                profileImage = user.child("profileImage").value.toString()
                                            )

                                            if (aUser!=null) {
                                                homeItems.add(
                                                    HomeItem(
                                                        id = item.key.toString(),
                                                        caption = item.child("caption").value.toString(),
                                                        photoUrl = item.child("photoUrl").value.toString(),
                                                        date = item.child("date").value.toString()
                                                            .toLong(),
                                                        publisher = item.child("publisher").value.toString(),
                                                        name = aUser!!.name,
                                                        profileImage = aUser!!.profileImage
                                                    )
                                                )
                                            }

                                            Log.i("userrrrrrrrrr", homeItems.toString())
                                        }
                                    }
                                }
                            }
                        })
                    }
                    items.value= homeItems.toList()
                }
            }
        })
        return items
    }

    private fun getUserInfo(uid: String): User? {
        val ref= FirebaseDatabase.getInstance().getReference("Users")
        var aUser: User?=null
        ref.orderByKey().addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (user in snapshot.children){
                        //User(var name:String ="", var profileImage:String = "" ,var bio:String = "",var phone:String="" )
                        if(uid==user.key.toString()) {
                         //aUser= user.getValue(User::class.java)
                            aUser = User(
                                name = user.child("name").value.toString(),
                                profileImage = user.child("profileImage").value.toString()
                            )

                            Log.i("userrrrrrrrrr", user.toString())
                        }
                    }
                }
            }
        })
           return aUser
    }
}