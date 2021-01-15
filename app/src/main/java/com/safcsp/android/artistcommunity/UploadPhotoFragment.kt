package com.safcsp.android.artistcommunity

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val STORAGE_PHOTOS_PATH = "user photos/"
const val DATABASE_PHOTOS_PATH = "UserPhotos"
const val DATABASE_HOME_PATH = "Photos"

class UploadPhotoFragment : Fragment() {

    private val args by navArgs<UploadPhotoFragmentArgs>()
    private lateinit var imageView: ImageView
    private lateinit var caption: EditText
    private lateinit var submit: AppCompatButton
    private val reference = FirebaseStorage.getInstance().reference
    private val root = FirebaseDatabase.getInstance().getReference(DATABASE_PHOTOS_PATH)
    private val homeRoot = FirebaseDatabase.getInstance().getReference(DATABASE_HOME_PATH)
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(
            R.layout.fragment_upload_photo, container,
            false
        )

        imageView = view.findViewById(R.id.uploaded_image)
        caption = view.findViewById(R.id.caption)
        submit = view.findViewById(R.id.submit)
        imageView.setImageBitmap(args.bitmap)

        submit.setOnClickListener {

            val imageUri = getImageUri(requireContext(), args.bitmap)
            val ref = reference
                .child("$STORAGE_PHOTOS_PATH${System.currentTimeMillis()}." +
                        "${imageUri?.let { uri -> getImageExt(uri) }}"
                )
            if (imageUri != null) {
                ref.putFile(imageUri).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        //val format = SimpleDateFormat("yyyy-MM-dd")
                        //val now: String = format.format(Date().time)
                        if (currentUser != null) {
                            //user photo:
                            val photo = UserPhoto(caption.text.toString(), it.toString(),Date().time)
                            root.child(currentUser.uid)
                                .push()
                                .setValue(photo)
                            //Home:
                            val photo2 = HomePhoto(caption.text.toString(),
                                it.toString(),
                                Date().time,
                                currentUser.uid)
                            homeRoot.push().setValue(photo2)
                        }
                    }
                }.addOnProgressListener {
                    //progressbar
                }.addOnFailureListener {
                    Log.e("UploadPhotoFragment", it.toString())
                }
            }
        }
        return view
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    fun getImageExt(uri: Uri): String? {
        val contentResolver = requireActivity().contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}