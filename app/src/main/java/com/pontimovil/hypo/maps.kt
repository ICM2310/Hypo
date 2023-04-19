package com.pontimovil.hypo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import com.pontimovil.hypo.databinding.FragmentMapsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [maps.newInstance] factory method to
 * create an instance of this fragment.
 */
class maps : Fragment() {
    private lateinit var binding: FragmentMapsBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        // Create gradient background for topBar
        val colors = intArrayOf(0x00FFFFFF.toInt(), 0xFFFFFFFF.toInt()) // transparent to white

        val gradient = GradientDrawable(Orientation.TOP_BOTTOM, colors)
        binding.topBar.background = gradient

        val topBar = binding.topBar
        val fragmentName = "Mapa" // replace with the fragment name you want to use
        val bundle = Bundle().apply {
            putString("fragmentName", fragmentName) //
        }
        val topBarFragment = topBar().apply {
            arguments = bundle
        }
        childFragmentManager.beginTransaction().apply {
            add(topBar.id, topBarFragment, "topBar")
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
         * @return A new instance of fragment maps.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            maps().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}