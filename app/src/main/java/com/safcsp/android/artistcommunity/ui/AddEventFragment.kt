package com.safcsp.android.artistcommunity.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.safcsp.android.artistcommunity.R
import com.safcsp.android.artistcommunity.data.Event
import com.safcsp.android.artistcommunity.data.Latlng
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddEventFragment : Fragment() {
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val requestNum = 1
    private lateinit var clocation : Location

    private val PICK_IMAGE_REQUEST = 1
    lateinit var imageUri: Uri
    lateinit var eventPhoto : ImageView
    private val mclocation = MutableLiveData<Location>()
    val currentUser  = FirebaseAuth.getInstance().currentUser

    lateinit var latlng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
            ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),requestNum)
        }else{
            //main logic
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null){
                    mclocation.value = location
                    clocation = location
                    Toast.makeText(context,"lat : ${clocation.latitude}, lon: ${clocation.longitude}",
                        Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        val boundsBuilder = LatLngBounds.Builder()
        var clatlng : LatLng


        mclocation.observe(viewLifecycleOwner, Observer {location ->
            clatlng = LatLng(location.latitude,location.longitude)
            latlng = clatlng
            boundsBuilder.include(clatlng)
            googleMap.addMarker(MarkerOptions().position(clatlng).title("Marker in Sydney"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clatlng,14F))

        })

        googleMap.setOnMapClickListener {
            googleMap.clear()
            latlng = it
            boundsBuilder.include(latlng)
            googleMap.addMarker(MarkerOptions().position(latlng).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,14F))
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_add_event, container, false)

         eventPhoto = view.findViewById(R.id.event_photo)
        val eventTitle = view.findViewById<EditText>(R.id.title_et)
        val eventDescription = view.findViewById<EditText>(R.id.description_et)
        val addEventButton = view.findViewById<Button>(R.id.add_event_bt)
        val pickPhotoButton = view.findViewById<ImageButton>(R.id.pick_photo_bt)
        val datePicker = view.findViewById<ImageView>(R.id.date_picker_fab)
        val startDateTv = view.findViewById<TextView>(R.id.start_date_tv)
        val endDateTv = view.findViewById<TextView>(R.id.due_date_tv)
        var startDateLong : Long = 0
        var endDateLong : Long = 0


        val ref = FirebaseDatabase.getInstance().reference.child("Events")

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("SELECT RANGE DATE")
        val materialDatePicker = builder.build()

        datePicker.setOnClickListener {
            materialDatePicker.show(parentFragmentManager,"DATE_PICKER_RANGE")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            startDateLong = it.first!!
            endDateLong = it.second!!
            val startDate = it.first?.let { it1 -> Date(it1) }
            val endDate = it.second?.let { it1 -> Date(it1) }

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val startDateFormat = simpleDateFormat.format(startDate)
            val endDateFormat = simpleDateFormat.format(endDate)
            startDateTv.text = startDateFormat
            endDateTv.text = endDateFormat
        }

        pickPhotoButton.setOnClickListener {
            takePictureIntent()
        }

        addEventButton.setOnClickListener {
            val refId = ref.push().key

            if (!::imageUri.isInitialized){
                Toast.makeText(context,"إختر صورة",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (eventTitle.text.isNullOrEmpty()){
                Toast.makeText(context,"ادخل عنوان الحدث",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (eventDescription.text.isNullOrEmpty()){
                Toast.makeText(context,"ادخل نبذة عن الحدث",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!::latlng.isInitialized){
                Toast.makeText(context,"حدد الموقع",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (startDateLong == 0.toLong()){
                Toast.makeText(context,"حدد تاريخ الابتداء",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (endDateLong == 0.toLong()){
                Toast.makeText(context,"حدد تاريخ الانتهاء",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.i("track Pick Photo :","$imageUri")

            currentUser?.uid?.let { it1 ->
                val id = it1
                refId?.let {idRef->
                    val event = Event(id,idRef,"$imageUri",
                            eventTitle.text.toString(),
                            eventDescription.text.toString(),
                            startDateLong,endDateLong,
                            Latlng(latlng.latitude,latlng.longitude))

                    ref.child(idRef).setValue(event).addOnSuccessListener {
                        Toast.makeText(context,"add successful",Toast.LENGTH_LONG).show()
                        getView()?.let {
                                it2 -> Navigation.findNavController(it2)
                            .navigate(R.id.action_addEventFragment_to_eventFragment)
                        }
                    }
                }

            }

        }

        return view
    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {pictureIntent->
            pictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                startActivityForResult(pictureIntent,PICK_IMAGE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageAndSaveUri(imageBitmap)
        }
    }

    private fun uploadImageAndSaveUri(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage
            .getInstance()
            .reference.child("event images").child("${System.currentTimeMillis()}.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val image = baos.toByteArray()
        val upload = storageRef.putBytes(image)
        upload.addOnCompleteListener{ uploadTask ->
            if (uploadTask.isSuccessful){

                storageRef.downloadUrl.addOnCompleteListener { uriTask ->
                    uriTask.result?.let {
                        imageUri = it
                        Toast.makeText(context,"$imageUri",Toast.LENGTH_LONG).show()
                        eventPhoto.setImageBitmap(bitmap)

                    }
                }
            }else{
                uploadTask.exception?.let {
                    Toast.makeText(context,it.message.toString(),Toast.LENGTH_LONG).show()
                }
            }

        }
    }

}