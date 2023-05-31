package com.pontimovil.hypo

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pontimovil.hypo.databinding.FragmentGalleriaBinding
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class galleria : Fragment(), SensorEventListener {
    private val SHAKE_THRESHOLD_GRAVITY = 20.0f
    private var _binding: FragmentGalleriaBinding? = null

    private var mPreferences: SharedPreferences? = null
    private var mPreferencesEditor: SharedPreferences.Editor? = null

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    // Request code for READ_EXTERNAL_STORAGE. It can be any number > 0.
    private val REQUEST_CODE_PERMISSIONS = 10

    // Imagelayout
    private var imageLayout: RecyclerView? = null
    // Acelerometro
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null

    private var adapter: polaroidType? = null

    private val cleanImages = mutableListOf<Bitmap>()




    // Crea un nuevo mapa para almacenar los niveles de desarrollo de las imágenes
    private val imageDevelopmentLevels = mutableMapOf<String, Float>()
    // Crea una variable para almacenar la última vez que se agitó el dispositivo
    private var lastShakeTime: Long = 0
    // Add this new flag
    private var shakeDetected = false



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleriaBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        var granted = checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        mSensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mPreferences = requireActivity().getSharedPreferences("HypoApp", Context.MODE_PRIVATE)
        mPreferencesEditor = mPreferences?.edit()

        mPreferences?.all?.forEach { (key, value) ->
            if (value is Float) {
                imageDevelopmentLevels[key] = value
            }
        }

        Toast.makeText(requireContext(), "Permission: $permission", Toast.LENGTH_SHORT).show()
        if (granted == 0) {
            // run on ui thread
            Handler(Looper.getMainLooper()).post {
                loadImages()
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        // Get a reference to the system's sensor manager
        mSensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager

        // Get a reference to the accelerometer sensor
        mSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        imageLayout = binding.imageLayout

    }

    private fun loadImages() {
        val images = mutableListOf<Bitmap>()
        val imagePaths = mutableListOf<String>()
        val targetDirectory = "/Pictures/Hypo"

        val imageProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                if (imagePath.contains(targetDirectory)) {
                    Log.d("TAG", "loadImages: $imagePath")
                    // Load bitmaps with a third of the original size
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 3

                    // Add the bitmap to cleanImages before applying the color matrix
                    cleanImages.add(BitmapFactory.decodeFile(imagePath, options))
                    val originalBitmap = BitmapFactory.decodeFile(imagePath, options)

                    // Check the shared preferences for the development level of each image
                    val developmentLevel = mPreferences?.getFloat(imagePath, 0f) ?: 0f
                    // Apply the color matrix with the development level
                    val bitmap = convertToBlack(originalBitmap, developmentLevel)
                    images.add(bitmap)
                    imagePaths.add(imagePath)
                }
            }
        }

        adapter = polaroidType(imagePaths, images)
        imageLayout?.layoutManager = GridLayoutManager(requireContext(), 3)
        imageLayout?.addItemDecoration(SpaceItemDecoration(10))
        imageLayout?.adapter = adapter

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()

        // Register the sensor listener to listen for accelerometer events
        mSensorManager?.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        // Unregister the sensor listener to stop listening for accelerometer events
        mSensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate the magnitude of acceleration using the Pythagorean theorem
            val acceleration = sqrt(x * x + y * y + z * z)

            // Check if the acceleration exceeds the shake threshold
            if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                val currentTime = System.currentTimeMillis()

                // Check if the last shake event was more than 500ms ago, to prevent multiple shake events from being triggered in rapid succession.
                if (currentTime - lastShakeTime > 500) {
                    // Trigger the shake event
                    // onShake()
                    CoroutineScope(Dispatchers.IO).launch {
                        applyDevelopmentLevel()
                    }
                    lastShakeTime = currentTime
                    Log.d("TAG", "onSensorChanged: Shake detected")
                }
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //loadImages()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        fun newInstance(): Fragment {
            return galleria()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space
            }
        }


    }


    private fun applyDevelopmentLevel() {
        CoroutineScope(Dispatchers.IO).launch {
            cleanImages.forEachIndexed { index, bitmap ->
                val imagePath = getImagePathAtIndex(index)
                val developmentLevel = mPreferences?.getFloat(imagePath, 0f) ?: 0f // Get the development level from shared preferences. The :? 0f is a null check
                if (developmentLevel < 0.9f) {
                    val newDevelopmentLevel = developmentLevel + 0.1f
                    // Update the development level in the shared preferences
                    mPreferences?.edit {
                        putFloat(imagePath, newDevelopmentLevel)
                    }

                    // Apply the color matrix with the development level
                    val newBitmap = convertToBlack(bitmap, newDevelopmentLevel)
                    Log.d("Galleria", "Updated image at index $index, updated to development level $newDevelopmentLevel")
                    withContext(Dispatchers.Main) {

                        // Update the view on the main thread
                        adapter?.updateImage(newBitmap, index)

                        Log.d("Galleria", "Updated image at index $index, updated to development level $newDevelopmentLevel")
                    }
                }
            }
        }
    }

    private fun getImagePathAtIndex(index: Int): String {
        val imageProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        cursor?.use {
            if (cursor.moveToPosition(index)) {
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
        }

        return ""
    }



    private fun convertToBlack(bitmap: Bitmap, developmentLevel: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(outputBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, 0f, // Red
            0f, developmentLevel+0.2f, 0f, 0f, 0f, // Green
            0f, 0f, developmentLevel+0.2f, 0f, 0f, // Blue
            0f, 0f, 0f, developmentLevel, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return outputBitmap
    }

}

