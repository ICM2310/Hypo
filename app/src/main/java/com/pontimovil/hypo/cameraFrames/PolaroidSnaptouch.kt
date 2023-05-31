package com.pontimovil.hypo.cameraFrames

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import com.pontimovil.hypo.databinding.FragmentPolaroidSnaptouchBinding
import java.util.concurrent.ExecutorService

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [polaroidSnaptouch.newInstance] factory method to
 * create an instance of this fragment.
 */
class polaroidSnaptouch : Fragment(), SensorEventListener {

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private val TAG = "Hypo"
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private var sensorManager: SensorManager? = null
    private var gyroscope: Sensor? = null


    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val REQUEST_CODE_PERMISSIONS = 10
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPolaroidSnaptouchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private class LuminosityAnalyzer(private val listener: (Any) -> Int) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }


    private fun startCamera() {
        val context = requireContext()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.v("Lumin", "Average luminosity: $luma")
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        cameraExecutor.shutdown()
        sensorManager?.unregisterListener(this)
    }

    private fun applyLensFlareEffect(x: Float, y: Float, z: Float) {
        // Apply the lens flare effect using the gyroscope values
        // You can modify this method to customize the lens flare effect based on your requirements
        // For example, you can adjust the brightness or position of the lens flare based on the gyroscope values
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    activity,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        Log.d(TAG, "takePhoto")
        val imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }
        val imageCapture = imageCapture ?: return
        Log.d(TAG, "imageCapture")
        Toast.makeText(activity, "Taking photo", Toast.LENGTH_SHORT).show()
        val context = requireContext()
        val contentResolver = context.contentResolver
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Hypo") //
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)
                    listener?.onPictureTaken()
                }
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPolaroidSnaptouchBinding.inflate(layoutInflater)

        binding.shutter.setOnClickListener {
            takePhoto()
        }

        binding.flash.setOnClickListener {
            Toast.makeText(activity, "Flash!", Toast.LENGTH_SHORT).show()
        }

        binding.flip.setOnClickListener {
            Toast.makeText(activity, "Switch!", Toast.LENGTH_SHORT).show()
        }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment polaroidSnaptouch.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            polaroidSnaptouch().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var listener: OnPictureTakenListener? = null

    interface OnPictureTakenListener {
        fun onPictureTaken()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnPictureTakenListener) {
            listener = parentFragment as OnPictureTakenListener
        }
        sensorManager?.unregisterListener(this) // Unregister the listener from previous fragment
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            // Access gyroscope values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Apply lens flare effect based on gyroscope values
            applyLensFlareEffect(x, y, z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }


}

class LumaListener {
    operator fun invoke(luma: Double) {
        //Log.d("LumaListener", "Average luminosity: $luma")
    }
}
