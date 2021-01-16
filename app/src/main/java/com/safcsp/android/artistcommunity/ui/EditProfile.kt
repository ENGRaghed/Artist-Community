package com.safcsp.android.artistcommunity.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.User
import kotlinx.android.synthetic.main.fragment_edit_profile.*


class EditProfile : Fragment() {
    var currentPath: Uri? = null
    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"
    private lateinit var imageUri: Uri
    private val REQUEST_Gallery = 200
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var image_view = view.findViewById<ImageView>(R.id.image_view)
        val edit_text_name = view.findViewById<EditText>(R.id.edit_text_name)
        val edit_text_phone = view.findViewById<TextView>(R.id.edit_text_phone)
        val button_save = view.findViewById<Button>(R.id.button_save)
        val progressbar = view.findViewById<ProgressBar>(R.id.progressbar)
        val edit_text_bio = view.findViewById<EditText>(R.id.edit_text_bio)


        if (firebaseAuth.currentUser != null) {
            FirebaseDatabase.getInstance().reference.child("Users")
                .child(firebaseAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val bio = snapshot.child("bio").value.toString()
                            val phone = snapshot.child("phone").value.toString()
                            edit_text_bio.setText(bio).toString()
                            edit_text_phone.setText(phone).toString()
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
            edit_text_name.setText(user.displayName)
            edit_text_phone.text = user.phoneNumber
        }
        edit_user_photo.setOnClickListener {
            takePictureIntent()
        }

        button_save.setOnClickListener {

            val photo = when {
                ::imageUri.isInitialized -> imageUri
                currentUser?.photoUrl == null -> Uri.parse(DEFAULT_IMAGE_URL)
                else -> currentUser.photoUrl
            }

            val name = edit_text_name.text.toString().trim()


            if (name.isEmpty()) {
                edit_text_name.error = "name required"
                edit_text_name.requestFocus()
                return@setOnClickListener
            }

            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photo)
                .build()

            progressbar.visibility = View.VISIBLE

            currentUser?.updateProfile(updates)
                ?.addOnCompleteListener { task ->
                    progressbar.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        Snackbar.make(view,"تم تحديث الملف الشخصي",Snackbar.LENGTH_LONG).show()
                       // Toast.makeText(context, "Profile Updated", Toast.LENGTH_LONG).show()

                    } else {
                        Snackbar.make(view, task.exception?.message!!, Snackbar.LENGTH_LONG).show()

                    }
                }
            if (!::imageUri.isInitialized) {
                Toast.makeText(context, "اختر صورة", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.i("track Pick Photo :", "$imageUri")

            val user = User(
                name,
                "$imageUri",
                edit_text_bio.text.toString(),
                edit_text_phone.text.toString()
            )
            currentUser?.uid?.let { it1 ->
                FirebaseDatabase.getInstance().getReference("Users").child(it1).setValue(user)
            }
        }

    }

    private fun takePictureIntent() {

        val gallery = Intent(Intent.ACTION_PICK)
        gallery.type = "image/*"
        startActivityForResult(gallery, REQUEST_Gallery)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_Gallery && resultCode == Activity.RESULT_OK) {
            currentPath = data?.data
            currentPath?.let { uploadImageAndSaveUri(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun uploadImageAndSaveUri(bitmap: Uri) {
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        val upload = storageRef.putFile(bitmap)
        var progressbar_pic = view?.findViewById<ProgressBar>(R.id.progressbar_pic)

        if (progressbar_pic != null) {
            progressbar_pic.visibility = View.VISIBLE
        }
        upload.addOnCompleteListener { uploadTask ->
            if (progressbar_pic != null) {
                progressbar_pic.visibility = View.INVISIBLE
            }
            val image_view = view?.findViewById<ImageView>(R.id.image_view)
            image_view?.foreground?.alpha = 0

            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        imageUri = it

                        image_view?.foreground?.alpha = 0
                        image_view.let { image ->
                            Glide.with(this)
                                .load(bitmap)
                                .circleCrop()
                                .into(image_view!!)
                        }
                    }
                }
            } else {
                uploadTask.exception?.let {
                    Toast.makeText(context, it.message!!, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}