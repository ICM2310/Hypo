package com.pontimovil.hypo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.pontimovil.hypo.cameraFrames.polaroidSnaptouch
import com.pontimovil.hypo.databinding.FragmentMainCameraBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [mainCamera.newInstance] factory method to
 * create an instance of this fragment.
 */
class mainCamera : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentMainCameraBinding
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
        binding = FragmentMainCameraBinding.inflate(inflater, container, false)
        val cameraContainLayout = binding.cameraContain
        val rollerContainLayout = binding.rollContain

        // Begin a transaction for the FragmentManager
        childFragmentManager.beginTransaction().apply {

            // Add the polaroidSnaptouch fragment to the cameraContain FrameLayout
            add(cameraContainLayout.id, polaroidSnaptouch.newInstance("", ""), "polaroidSnaptouch")
            // Add the rollerSelector fragment to the rollerContain FrameLayout
            add(rollerContainLayout.id, rollSelectorModern.newInstance("", ""), "rollerSelector")
            // Commit the transaction
            commit()
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment mainCamera.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            mainCamera().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}