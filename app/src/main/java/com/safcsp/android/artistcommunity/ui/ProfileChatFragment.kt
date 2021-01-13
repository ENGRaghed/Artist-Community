package com.safcsp.android.artistcommunity.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.UploadPhotoFragmentArgs
import com.safcsp.android.artistcommunity.UserPhoto
import com.safcsp.android.artistcommunity.data.User


class ProfileChatFragment : Fragment() {
    private val args by navArgs<ProfileChatFragmentArgs>()
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var chatBtn: ImageView
    private var photo = emptyList<UserPhoto>()
    private var adapter = ProfileAdapter(photo)
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_profile_chat, container, false)
        photoRecyclerView = view.findViewById(R.id.image_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
        photoRecyclerView.adapter = adapter
        val key = args.userId
        key?.let {
            FirebaseDatabase.getInstance().reference.child("UserPhotos").child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.i("UsersPhotos","$snapshot")
                        snapshot.children.forEach {
                            val userPhoto =
                                UserPhoto(
                                    it.child("caption").value.toString(),
                                    it.child("photoUrl").value.toString(),
                                    it.child("date").value.toString().toLong()
                                )
                            photo+=userPhoto
                        }
                        adapter.setData(photo)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
        return view
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var image_view = view.findViewById<ImageView>(R.id.image_view)
        val text_name = view.findViewById<TextView>(R.id.text_name)
        val text_bio = view.findViewById<TextView>(R.id.text_bio)
        chatBtn= view.findViewById(R.id.imageView2) as ImageView

        if (args.userId != null) {
            FirebaseDatabase.getInstance().reference.child("Users")
                .child(args.userId)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val bio = snapshot.child("bio").value.toString()
                            val user=snapshot.child("name").value.toString()
                            val imageurl=snapshot.child("profileImage").value.toString()

                                Glide.with(image_view)
                                    .load(imageurl)
                                    .circleCrop()
                                    .into(image_view)
                            text_name.setText(user).toString()
                            text_bio.setText(bio).toString()
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    }
                )
        }



        chatBtn.setOnClickListener {
      val action=
          ProfileChatFragmentDirections.actionProfileChatFragmentToChatFragment(args.userId)
            findNavController().navigate(action)
        }
    }
    private inner class ProfileHolder(view:View): RecyclerView.ViewHolder(view) {
        lateinit var image: UserPhoto
        var photoView: ImageView = view.findViewById(R.id.item_image_view)
        fun bind(photo: UserPhoto) {
            this.image = photo
            photoView.let { photo ->
                Glide.with(itemView)
                    .load(image.photoUrl)
                    .into(photoView)
            }
        }
    }
    private inner class ProfileAdapter(var image:List<UserPhoto>):
        RecyclerView.Adapter<ProfileHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileHolder {
            val view = layoutInflater.inflate(R.layout.profile_photo_item, parent, false)
            return ProfileHolder(view)
        }
        override fun onBindViewHolder(holder: ProfileHolder, position: Int) {
            val photo = this.image[position]
            holder.bind(photo)
        }
        override fun getItemCount(): Int {
            return image.size
        }
        fun setData(photo: List<UserPhoto>) {
            this.image = photo
            notifyDataSetChanged()
        }
    }
}