package com.pontimovil.hypo.cameraFrames

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.pontimovil.hypo.R
import com.pontimovil.hypo.databinding.FragmentPolaroidSnaptouchBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [polaroidSnaptouch.newInstance] factory method to
 * create an instance of this fragment.
 */
class polaroidSnaptouch : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPolaroidSnaptouchBinding
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPolaroidSnaptouchBinding.inflate(layoutInflater)

        binding.shutter.setOnClickListener {
            dispatchTakePictureIntent()
            Toast.makeText(activity, "Foto!", Toast.LENGTH_SHORT).show()
        }

        binding.flash.setOnClickListener {
            Toast.makeText(activity, "Flash!", Toast.LENGTH_SHORT).show()
        }

        binding.flip.setOnClickListener {
            Toast.makeText(activity, "Switch!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            takePicture.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle exception
        }
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
}