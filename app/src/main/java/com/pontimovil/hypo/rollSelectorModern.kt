package com.pontimovil.hypo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.pontimovil.hypo.databinding.FragmentRollSelectorModernBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [rollSelectorModern.newInstance] factory method to
 * create an instance of this fragment.
 */
class rollSelectorModern : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRollSelectorModernBinding

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
        Log.d("RollSelectorModern", "Creating view")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roll_selector_modern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRollSelectorModernBinding.bind(view)

        binding.photoWidget.setOnClickListener {
            // Crear una instancia del fragmento de la galería
            val galleryFragment = galleria.newInstance()
            // Obtener el administrador de fragmentos
            val fragmentManager = requireActivity().supportFragmentManager
            // Abrir el fragmento de la galería en la actividad actual
            fragmentManager.beginTransaction()
                .replace(R.id.mainCameraFragmentContainer, galleryFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment rollSelectorModern.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            rollSelectorModern().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }


    }

    // function to udpdate the linear layout with the new roll
    fun updateRoll(roll: Roll) {
        Log.d("RollSelectorModern", "Updating roll")
        val images = roll.images
        val layout = binding.photoLayout
        for (image in images) {
            val imageView = ImageView(context)
            imageView.setImageURI(Uri.parse(image))
            layout.addView(imageView)
        }
    }
}

private fun ImageView.setImageURI(image: String) {
    this.setImageURI(image)
}


