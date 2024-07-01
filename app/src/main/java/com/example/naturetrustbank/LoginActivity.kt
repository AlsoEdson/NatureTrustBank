package com.example.naturetrustbank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.naturetrustbank.databinding.ActivityLoginBinding
import com.example.naturetrustbank.databinding.ActivityMainBinding
import com.example.naturetrustbank.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitNow()
        }

        auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser
        if(usuarioActual!=null) {
            showHome()
        }

        //Setup
        setup()

        /*if(getIntent().getBooleanExtra("EXIT", false)) {
            finish()
        }*/
    }

    private fun setup() {

        title = "Autenticación"

        binding.logInButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()).addOnCompleteListener {

                        if(it.isSuccessful) {
                            showHome()
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        binding.registerTextView.setOnClickListener {
            showRegisterForm()
        }

    }

    private fun showAlert() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
    }

    private fun showRegisterForm() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }
}