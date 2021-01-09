package com.safcsp.android.artistcommunity.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.User


class ProfileFragment : Fragment() {


    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"
    private lateinit var imageUri: Uri

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var image_view = view.findViewById<ImageView>(R.id.image_view)
        val text_name = view.findViewById<TextView>(R.id.text_name)
        val progressbar = view.findViewById<ProgressBar>(R.id.progressbar)
        val text_bio = view.findViewById<TextView>(R.id.text_bio)


        if (firebaseAuth.currentUser != null) {
            FirebaseDatabase.getInstance().reference.child("Users")
                    .child(firebaseAuth.currentUser!!.uid)
                    .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val bio = snapshot.child("bio").value.toString()
                                    val phone = snapshot.child("phone").value.toString()
                                    text_bio.setText(bio).toString()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            }
                    )
        }
        currentUser?.let { user ->
            Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop()
                    .into(image_view)
            text_name.setText(user.displayName)
        }
    }
}