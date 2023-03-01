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
import com.pontimovil.hypo.cameraFrames.polaroidSnaptouch

// TODO: Crear fragmentos de decoraciones de camaras.

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        window.statusBarColor = getColor(R.color.black)
        supportFragmentManager.commit {
            replace<polaroidSnaptouch>(binding.frameContainer.id)
            setReorderingAllowed(true)
            addToBackStack("replacement")
        }
    }
}