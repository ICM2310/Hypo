package com.pontimovil.hypo.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pontimovil.hypo.MainActivity
import com.pontimovil.hypo.databinding.ActivityLoginUsuarioBinding
import com.pontimovil.hypo.databinding.ActivityRegistroUsuarioBinding


class LoginUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginUsuarioBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.botonLogin.setOnClickListener{
            val email = binding.inputUsuarioLogin.text.toString()
            val password = binding.inputContrasenaLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful){
                        startActivity(Intent(baseContext, MainActivity::class.java))
                    }else{
                        Toast.makeText(this, it.exception.toString(),Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this, "Uno o más campos están vacíos",Toast.LENGTH_SHORT).show()
            }
        }
        binding.botonRegistro.setOnClickListener{
            startActivity(Intent(baseContext, RegistroUsuarioActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()

        if(auth.currentUser != null){
            startActivity(Intent(baseContext, MainActivity::class.java))
        }
    }
}





