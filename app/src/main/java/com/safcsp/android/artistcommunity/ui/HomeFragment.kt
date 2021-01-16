package com.safcsp.android.artistcommunity.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.User
import com.safcsp.android.artistcommunity.viewmodel.HomeViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        if ((requireActivity() as AppCompatActivity).supportActionBar != null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                true
            )
        }
        recyclerView= view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter= HomeAdapter(listOf())

        val ref= FirebaseDatabase.getInstance().getReference("Photos")
        var homeItems= mutableListOf<HomeItem>()

        ref.addValueEventListener(object: ValueEventListener {
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
                                            Log.i("HomeFragment", homeItems.toString())
                                        }
                                    }
                                    updateUI(homeItems)
                                }
                            }
                        })
                    }
                }
            }
        })

        return view
    }

    private fun updateUI(list: List<HomeItem>) {
        val homeList = list.reversed()
        val adapter=HomeAdapter(homeList)
        recyclerView.adapter= adapter
        adapter.setData(homeList)
    }

    private inner class HomeHolder(view: View) : RecyclerView.ViewHolder(view)  {

        lateinit var homeItem: HomeItem
        var accPhoto: ImageView = view.findViewById(R.id.acc_image_view) as ImageView
        var photo: ImageView = view.findViewById(R.id.image_view) as ImageView
        var acc: TextView = view.findViewById(R.id.account)
        var caption: TextView = view.findViewById(R.id.caption)
        var date: TextView = view.findViewById(R.id.date)
        fun bind(item: HomeItem) {
            homeItem= item
            Log.i("profileImage",item.profileImage.toString())
            if(!item.profileImage.isNullOrEmpty()) {
                val transformation: Transformation = RoundedTransformationBuilder()
                   // .borderColor(Color.BLACK)
                    .borderWidthDp(3F)
                    .cornerRadiusDp(30F)
                    .oval(false)
                    .build()
                Picasso.get()
                    .load(item.profileImage.toString())
                    .resize(150, 150)
                    .centerCrop()
                    .transform(transformation)
                    .into(accPhoto)
            }

            Glide
                .with(itemView)
                .load(item.photoUrl)
                .into(photo)

            acc.text= item.name
            caption.text=item.caption
            date.text= Date(item.date).toString()

            acc.setOnClickListener {
                Toast.makeText(requireContext(),homeItem.publisher,Toast.LENGTH_LONG).show()

            }

            accPhoto.setOnClickListener {
                Toast.makeText(requireContext(),homeItem.publisher,Toast.LENGTH_LONG).show()
            }

        }

    }

    private inner class HomeAdapter(var photos: List<HomeItem>) :
        RecyclerView.Adapter<HomeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
            val view = layoutInflater.inflate(R.layout.home_item, parent, false)
            return HomeHolder(view)
        }

        override fun onBindViewHolder(holder: HomeHolder, position: Int) {
            val photo = this.photos[position]
            holder.bind(photo)
        }

        override fun getItemCount(): Int {
            return photos.size
        }

        fun setData(photo: List<HomeItem>) {
            this.photos = photo
            notifyDataSetChanged()
        }
    }
}

data class HomeItem(
    val id: String="",
    var caption: String = "",
    val photoUrl: String="",
    val date: Long=0,
    val publisher: String="",
    val name: String?="",
    val profileImage: String?=""
)