package com.safcsp.android.artistcommunity.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.Event
import com.safcsp.android.artistcommunity.data.Latlng
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class EventDetailsFragment : BottomSheetDialogFragment() {

    val args by navArgs<EventDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_details, container, false)

        val eventImageView = view.findViewById<ImageView>(R.id.event_imageView)
        val eventTitleTv = view.findViewById<TextView>(R.id.event_title_tv)
        val eventDecTv = view.findViewById<TextView>(R.id.event_dec_tv)
        val startDateTv = view.findViewById<TextView>(R.id.event_start_date_tv)
        val endDateTv = view.findViewById<TextView>(R.id.event_end_date_tv)

        var firebaseDatabase = FirebaseDatabase.getInstance().reference
                .child("Events").child(args.id)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i("DataSnapshot","$snapshot")
                            val latlng = Latlng(snapshot.child("location").child("lat").value.toString().toDouble() ,
                                    snapshot.child("location").child("lon").value.toString().toDouble())
                            val event = Event(snapshot.child("publisher").value.toString(),
                                    snapshot.child("id").value.toString(),
                                    snapshot.child("photo").value.toString(),
                                    snapshot.child("title").value.toString(),
                                    snapshot.child("description").value.toString(),
                                    snapshot.child("startDate").value.toString().toLong(),
                                    snapshot.child("dueDate").value.toString().toLong(),latlng)
                            Log.i("ImageLink","${event.photo}")
                        eventImageView.let {image ->
                            Glide.with(this@EventDetailsFragment)
                                    .load(event.photo)
                                    .into(eventImageView)
                        }
                        eventTitleTv.text = event.title
                        eventDecTv.text = event.description
                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                        val startDateFormat = simpleDateFormat.format(event.startDate)
                        val endDateFormat = simpleDateFormat.format(event.dueDate)
                        startDateTv.text = startDateFormat
                        endDateTv.text = endDateFormat

                    }

                })



        return view
    }

}