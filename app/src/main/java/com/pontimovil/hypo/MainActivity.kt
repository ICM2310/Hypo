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


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the bar
        supportActionBar?.hide()
        // Set the color of the status bar
        window.statusBarColor = resources.getColor(R.color.black)
        navController = Navigation.findNavController(this, R.id.activity_main_nav_host_fragment)
        setupWithNavController(binding.bottomNavigationView, navController)


    }
}