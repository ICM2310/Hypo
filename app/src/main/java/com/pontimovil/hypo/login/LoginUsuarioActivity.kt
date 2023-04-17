package com.pontimovil.hypo.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pontimovil.hypo.MainActivity
import com.pontimovil.hypo.R
import com.pontimovil.hypo.databinding.ActivityLoginUsuarioBinding

private lateinit var binding: ActivityLoginUsuarioBinding
private lateinit var auth: FirebaseAuth;

class LoginUsuarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityLoginUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.botonLogin.setOnClickListener{
            val email = binding.inputUsuarioLogin.text.toString()
            val password = binding.inputUsuarioLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful){
                        startActivity(Intent(baseContext, MainActivity::class.java))

                    }else{
                        Toast.makeText(this, it.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Uno o más campos están vacíos",Toast.LENGTH_SHORT).show()
            }
        }
    }
}





