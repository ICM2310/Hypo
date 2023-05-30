package com.pontimovil.hypo.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.pontimovil.hypo.MainActivity
import com.pontimovil.hypo.databinding.ActivityLoginUsuarioBinding
import java.util.concurrent.Executor


class LoginUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginUsuarioBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        executor =ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Error de autenticación: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Bienvendio!", Toast.LENGTH_SHORT
                    )
                        .show()
                    startActivity(Intent(baseContext, MainActivity::class.java))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authenticación fallida",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Ingresa a Hypo con tu huella dactliar")
            .setNegativeButtonText("Cancelar")
            .build()
        binding.botonHuella.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

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





