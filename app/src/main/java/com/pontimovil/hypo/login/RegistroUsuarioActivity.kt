package com.pontimovil.hypo.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pontimovil.hypo.MainActivity
import com.pontimovil.hypo.R
import com.pontimovil.hypo.databinding.ActivityRegistroUsuarioBinding
import com.pontimovil.hypo.modelo.Usuario

class RegistroUsuarioActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegistroUsuarioBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.botonLogin.setOnClickListener{
            val email = binding.inputUsuarioLogin.text.toString()
            val password = binding.inputContrasenaLogin.text.toString()
            val confirmarPassword = binding.inputConfirmarContrasena.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmarPassword.isNotEmpty()){
                if(password == confirmarPassword){
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                        if(it.isSuccessful){
                            val userId = auth.currentUser?.uid
                            val user = Usuario(email, "0", "0") // Create a User object with desired data
                            if (userId != null) {
                                db.collection("chatusuarios").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        startActivity(Intent(baseContext, MainActivity::class.java))
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_LONG).show()
                                    }
                            }else{
                                Toast.makeText(this, "Failed to get user ID", Toast.LENGTH_SHORT).show()
                            }
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
