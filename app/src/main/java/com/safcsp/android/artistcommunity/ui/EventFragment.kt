package com.safcsp.android.artistcommunity.ui

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.Event
import com.safcsp.android.artistcommunity.data.Latlng

class EventFragment : Fragment() {
    private lateinit var firebaseDatabase: FirebaseDatabase


    private val callback = OnMapReadyCallback { googleMap ->

        firebaseDatabase = FirebaseDatabase.getInstance()

        val boundsBuilder = LatLngBounds.Builder()

        val events = MutableLiveData<List<Event>>()
        val ref = firebaseDatabase.reference.child("Events")
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach{
                        Log.i("DataSnapshot","$it")
                        val latlng = Latlng(it.child("location").child("lat").value.toString().toDouble() ,
                            it.child("location").child("lon").value.toString().toDouble())
                        val event = Event(it.child("publisher").value.toString(),
                                it.child("id").value.toString(),
                                it.child("photo").value.toString(),
                                it.child("title").value.toString(),
                                it.child("description").value.toString(),
                                it.child("startDate").value.toString().toLong(),
                                it.child("dueDate").value.toString().toLong(),latlng)
                        events.value = listOf(event)
                        Log.i("EVENT","$event")
                        val latLng = LatLng(event.location.lat,event.location.lon)
                        boundsBuilder.include(latLng)
                        googleMap.addMarker(MarkerOptions().position(latLng).snippet(it.key))
                        googleMap.moveCamera(CameraUpdateFactory
                            .newLatLngBounds(boundsBuilder.build(), 1000, 1000, 0))
                    }
                }


            })

        googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(marker: Marker?): Boolean {
                val id = marker?.snippet
                val action = EventFragmentDirections
                        .actionEventFragmentToEventDetailsFragment(id!!)
                findNavController().navigate(action)
                return false
            }

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        val addEvent = view.findViewById<FloatingActionButton>(R.id.add_event_fragment)
        addEvent.setOnClickListener {
            getView()?.let { it1 ->
                Navigation.findNavController(it1).navigate(R.id.action_eventFragment_to_addEventFragment)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}