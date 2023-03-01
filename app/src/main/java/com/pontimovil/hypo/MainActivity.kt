package com.pontimovil.hypo

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputBinding
import androidx.fragment.app.commit
import com.pontimovil.hypo.databinding.ActivityMainBinding
import com.pontimovil.hypo.databinding.FragmentPolaroidSnaptouchBinding
// Import replace extension function
import androidx.fragment.app.replace
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController

import com.pontimovil.hypo.cameraFrames.polaroidSnaptouch

// TODO: Crear fragmentos de decoraciones de camaras.

/**
 * Main activity es la actividad principal de la app.
 * Contiene un bottom navigation view que permite navegar entre las diferentes pantallas de la app.
 *
 */
class MainActivity : AppCompatActivity() { // Main acitivity es la actividad principal de la app
    private lateinit var binding: ActivityMainBinding // Crear un binding para el layout activity_main. Esto permite acceder a los elementos del layout desde el código.
    private lateinit var navController: NavController // Crear un navController para el bottom navigation view. Esto permite navegar entre los diferentes fragmentos de la app.

    /**
     * Método onCreate de la actividad principal.
     * En este método se infla el layout activity_main y se establece como el layout de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) { // Método onCreate de la actividad principal
        super.onCreate(savedInstanceState) // Llamar al método onCreate de la superclase
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflar el layout activity_main
        setContentView(binding.root) // Establecer el layout activity_main como el layout de la actividad
        supportActionBar?.hide() // Ocultar la action bar
        window.statusBarColor = resources.getColor(R.color.black) // Establecer el color de la barra de estado
        navController = Navigation.findNavController(this, R.id.activity_main_nav_host_fragment) // Establecer el navController
        setupWithNavController(binding.bottomNavigationView, navController) // Establecer el bottom navigation view con el navController


    }
}