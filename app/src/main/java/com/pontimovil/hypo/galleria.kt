    package com.pontimovil.hypo

    import android.Manifest
    import android.content.Context
    import android.content.Context.SENSOR_SERVICE
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
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.pontimovil.hypo.databinding.FragmentGalleriaBinding
    import kotlin.math.sqrt


    /**
     * An example full-screen fragment that shows and hides the system UI (i.e.
     * status bar and navigation/system bar) with user interaction.
     */
    class galleria : Fragment(), SensorEventListener {
        private val SHAKE_THRESHOLD_GRAVITY = 50.0f
        private var _binding: FragmentGalleriaBinding? = null

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        // Request code for READ_EXTERNAL_STORAGE. It can be any number > 0.
        private val REQUEST_CODE_PERMISSIONS = 10

        // Imagelayout
        private var imageLayout: RecyclerView? = null
        // Acelerometro
        private var mSensorManager: SensorManager? = null
        private var mSensor: Sensor? = null

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
            var permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
            var granted = checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
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
                        options.inSampleSize = 10
                        val bitmap = BitmapFactory.decodeFile(imagePath, options)
                        images.add(bitmap)
                    }
                }
            }
            val adapter = polaroidType(images)
            // Change the style of the images to 0 sat, 0 brightness, 0 contrast
            imageLayout?.layoutManager = GridLayoutManager(requireContext(), 3) // Change the number 3 to set the number of columns in the grid
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

                var lastShakeTime = 0L

                // Calculate the magnitude of acceleration using the Pythagorean theorem
                val acceleration = sqrt(x * x + y * y + z * z)

                // Check if the acceleration exceeds the shake threshold
                if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                    val currentTime = System.currentTimeMillis()

                    // Check if the last shake event was more than 500ms ago, to prevent multiple shake events from being triggered in rapid succession.
                    if (currentTime - lastShakeTime > 500) {
                        // Trigger the shake event
                        onShake()
                        lastShakeTime = currentTime
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing
        }

        private fun onShake() {
            Toast.makeText(requireContext(), "Shake!", Toast.LENGTH_SHORT).show()
            changeImageStyle()
        }

        private fun changeImageStyle() {
            val adapter = imageLayout?.adapter as? polaroidType ?: return
            adapter.applyStyleToImages()
            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()
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


        private fun createNewLinearLayout(): LinearLayout {
            val linearLayout = LinearLayout(requireContext())
            linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            return linearLayout
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

    }

