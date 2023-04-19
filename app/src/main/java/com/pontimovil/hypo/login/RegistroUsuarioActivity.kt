package com.pontimovil.hypo.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.pontimovil.hypo.MainActivity
import com.pontimovil.hypo.R
import com.pontimovil.hypo.databinding.ActivityRegistroUsuarioBinding

class RegistroUsuarioActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegistroUsuarioBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.botonRegistro.setOnClickListener{
            val email = binding.inputUsuarioLogin.text.toString()
            val password = binding.inputContrasenaLogin.text.toString()
            val confirmarPassword = binding.inputConfirmarContrasena.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmarPassword.isNotEmpty()){
                if(password == confirmarPassword){
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                        if(it.isSuccessful){
                            startActivity(Intent(baseContext, MainActivity::class.java))
                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Uno o más campos están vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}