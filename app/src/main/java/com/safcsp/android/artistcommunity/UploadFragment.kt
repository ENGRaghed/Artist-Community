package com.safcsp.android.artistcommunity

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import java.io.File
import java.net.URL
import java.util.*


const val REQUEST_CAMERA = 1
const val ACTIVITY_SELECT_IMAGE = 1234

class UploadFragment : Fragment() {

    private lateinit var takeButton: AppCompatButton
    private lateinit var galleryButton: Button
    private lateinit var textureView: TextureView
    private val orientation = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }
    private lateinit var cameraId: String
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequest: CaptureRequest
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imgDimentions: Size
    private lateinit var imgReader: ImageReader
    private lateinit var file: File
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    val statrCallback = object : CameraDevice.StateCallback() {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onOpened(p0: CameraDevice) {
            cameraDevice = p0
            createCameraPreview()
        }

        override fun onDisconnected(p0: CameraDevice) {
            p0.close()
        }

        override fun onError(p0: CameraDevice, p1: Int) {
            p0.close()
            cameraDevice = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCameraPreview() {
        val texture = textureView.surfaceTexture
        texture?.setDefaultBufferSize(imgDimentions.width, imgDimentions.height)
        val surface = Surface(texture)

        if (cameraDevice != null) {
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        Log.e("onConfigureFailed", "onConfigureFailed error")
                    }

                    override fun onConfigured(p0: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        cameraCaptureSession = p0
                        updatePreview()
                    }
                },
                null
            )
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) return
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        cameraCaptureSession.setRepeatingRequest(
            captureRequestBuilder.build(),
            null, mBackgroundHandler
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        takeButton = view.findViewById(R.id.take_photo_button)
        galleryButton = view.findViewById(R.id.gallery_button)
        textureView = view.findViewById(R.id.texture_view)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
            }
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }
        }

        takeButton.setOnClickListener {
            takePicture()
        }
        galleryButton.setOnClickListener {
            openGallery()
        }
        return view
    }

    private fun openGallery() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(i, ACTIVITY_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTIVITY_SELECT_IMAGE -> if (resultCode == RESULT_OK) {
                val selectedImage: Uri? = data?.data
                val filePathColumn =
                    arrayOf(MediaStore.Images.Media.DATA)
                val cursor: Cursor? =
                    selectedImage?.let {
                        requireActivity().contentResolver.query(
                            it, filePathColumn,
                            null, null, null
                        )
                    }

                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                    val filePath: String = cursor.getString(columnIndex)
                    cursor.close()
                    val selectedImage = BitmapFactory.decodeFile(filePath)
                    val action = UploadFragmentDirections
                        .actionUploadFragmentToUploadPhotoFragment(selectedImage)
                    view?.findNavController()?.navigate(action)
                }
            }
        }
    }

    private fun openCamera() {
        val manager: CameraManager =
            requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = manager.cameraIdList[0]
        val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
        val map = cameraCharacteristics
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map != null) {
            imgDimentions = map.getOutputSizes(SurfaceTexture::class.java)[0]
        }

        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CAMERA
            )
            return
        }
        manager.openCamera(cameraId, statrCallback, null)
    }

    private fun takePicture() {

        if (cameraDevice == null) return
        val manager: CameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE)
                as CameraManager
        val cameraCharacteristics = manager
            .getCameraCharacteristics(cameraDevice!!.id)
        var jpegSizes: Array<Size>? = null
        jpegSizes = cameraCharacteristics
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.JPEG)
        var width = 640
        var height = 480
        if (jpegSizes != null && jpegSizes.isNotEmpty()) {
            width = jpegSizes[0].width
            height = jpegSizes[0].height
        }

        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurface = mutableListOf<Surface>()
        outputSurface.add(reader.surface)
        outputSurface.add(Surface(textureView.surfaceTexture))

        if (cameraDevice != null) {
            val captureBuilder: CaptureRequest.Builder = cameraDevice!!
                .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(
                CaptureRequest.CONTROL_MODE,
                CameraMetadata.CONTROL_MODE_AUTO
            )

            val rotation = requireActivity().windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation.get(rotation))
            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()
            file = File("${Environment.getExternalStorageDirectory()}/$ts.jpg")

            val readerListener: ImageReader.OnImageAvailableListener =
                ImageReader.OnImageAvailableListener {
                    var image: Image? = null
                    image = reader.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    val bitmapImage: Bitmap =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                    val matrix = Matrix()
                    matrix.postRotate(orientation.get(rotation).toFloat())
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmapImage,
                        width,
                        height,
                        true
                    )
                    val rotatedBitmap = Bitmap.createBitmap(
                        scaledBitmap, 0, 0,
                        scaledBitmap.width,
                        scaledBitmap.height, matrix, true
                    )

                    val action = UploadFragmentDirections
                        .actionUploadFragmentToUploadPhotoFragment(rotatedBitmap)
                    view?.findNavController()?.navigate(action)
                }

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener = object : CameraCaptureSession.CaptureCallback() {

                @RequiresApi(Build.VERSION_CODES.P)
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    try {
                        createCameraPreview()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            cameraDevice!!.createCaptureSession(
                outputSurface,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                    }

                    override fun onConfigured(p0: CameraCaptureSession) {
                        try {
                            p0.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                },
                mBackgroundHandler
            )
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    openCamera()
                }
            }
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("camera")
        mBackgroundThread?.start()
        mBackgroundHandler = mBackgroundThread?.looper?.let { Handler(it) }
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        mBackgroundThread?.join()
        mBackgroundThread = null
        mBackgroundHandler = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // toast msg
            }
        }
    }
}