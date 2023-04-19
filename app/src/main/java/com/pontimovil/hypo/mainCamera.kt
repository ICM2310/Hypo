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
 * Fragmento principal de la cámara, contiene los fragments de la cámara y el selector de rollos
 *
 */
class mainCamera : Fragment(), polaroidSnaptouch.OnPictureTakenListener { // mainCamera es el fragmento principal de la cámara, contiene los fragments de la cámara y el selector de rollos, se encarga de la comunicación entre ellos

    // Array de los Rollos de la cámara
    var rolls: Array<Roll> = arrayOf() // Array de los Rollos de la cámara
    val selectedRoll: Int = 0 // Rollo seleccionado
    private var param1: String? = null // Parámetros de la cámara
    private var param2: String? = null // Parámetros de la cámara
    private lateinit var binding: FragmentMainCameraBinding // Binding del fragmento, contiene los layouts de la cámara y el selector de rollos
    override fun onCreate(savedInstanceState: Bundle?) { // Se ejecuta al crear el fragmento
        super.onCreate(savedInstanceState) // Se ejecuta el onCreate del padre
        arguments?.let { // Se comprueba si se han pasado argumentos
            param1 = it.getString(ARG_PARAM1) // Se asignan los argumentos a los parámetros
            param2 = it.getString(ARG_PARAM2) // Se asignan los argumentos a los parámetros
        }
        // Esconder la action bar
        (activity as MainActivity).supportActionBar?.hide() // Se esconde la action bar
    }

    override fun onPictureTaken() {
        // Update the modern rolls fragment
        val modernRollsFragment = childFragmentManager.findFragmentByTag("rollerSelector") as rollSelectorModern
        modernRollsFragment.updateRoll(rolls[selectedRoll])
    }

    /**
     * onCreateView se ejecuta al crear la vista del fragmento, se encarga de añadir los fragments de la cámara y el selector de rollos
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, // Se pasan los parámetros de la vista. Inflater es el layoutInflater, container es el ViewGroup que contiene el fragmento
        savedInstanceState: Bundle? // Se pasa el estado de la instancia, en este caso no se usa, pero es necesario para el override
    ): View? { // Se devuelve la vista del fragmento
        binding = FragmentMainCameraBinding.inflate(inflater, container, false) // Se infla el binding del fragmento (Recuerda que el binding es el archivo xml que contiene los layouts de la cámara y el selector de rollos, inflar es convertir el xml en un objeto de la clase View)
        val cameraContainLayout = binding.cameraContain // Se obtiene el FrameLayout que contiene la cámara
        val rollerContainLayout = binding.rollContain // Se obtiene el FrameLayout que contiene el selector de rollos

        childFragmentManager.beginTransaction().apply { // Se crea una transacción de fragmentos, se usa childFragmentManager porque el fragmento principal de la cámara es un fragmento dentro de otro fragmento

            add(cameraContainLayout.id, polaroidSnaptouch.newInstance("", ""), "polaroidSnaptouch") // Se añade el fragmento de la cámara al FrameLayout de la cámara

            add(rollerContainLayout.id, rollSelectorModern.newInstance("", ""), "rollerSelector") // Se añade el fragmento del selector de rollos al FrameLayout del selector de rollos
            commit() // Se ejecuta la transacción, es decir, se añaden los fragments a los FrameLayouts
        }

        // Si esta vacío el array de rollos, se crea un nuevo rollo
        if (rolls.isEmpty()) {
            rolls = arrayOf(Roll())
        }



        return binding.root // Se devuelve la vista del fragmento
    }

    /**
     * objeto companion, se usa para crear una instancia del fragmento
     */
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