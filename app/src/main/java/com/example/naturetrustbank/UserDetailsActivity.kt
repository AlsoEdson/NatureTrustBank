package com.example.naturetrustbank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.naturetrustbank.databinding.ActivityRegisterBinding
import com.example.naturetrustbank.databinding.ActivityUserDetailsBinding
import com.example.naturetrustbank.ui.user_details.UserDetailsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.ref.Reference

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var flag = 0
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, UserDetailsFragment.newInstance())
                .commitNow()
        }

        if(getIntent().getBooleanExtra("EDIT", false)) {
            flag = 1
            binding.indicationTextView.setText("Modifica los campos necesarios:")
            binding.continueButton.setText("Guardar")

            val bundle = intent.extras
            val nombreUsuario = bundle?.getString("nombreUsuario")
            val nombre = bundle?.getString("nombre")
            val nacimiento = bundle?.getString("nacimiento")
            val telefono = bundle?.getString("telefono")

            binding.usuarioEditText.setText(nombreUsuario)
            binding.nombreEditText.setText(nombre)
            binding.nacimientoEditText.setText(nacimiento)
            binding.telefonoEditText.setText(telefono)
        }

        setup()
    }

    private fun setup() {

        title = "Información de usuario"

        auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser
        val uid = auth.currentUser?.uid
        databaseReference = Firebase.database.reference

        binding.continueButton.setOnClickListener {
            if (binding.usuarioEditText.text.isNotEmpty() && binding.nombreEditText.text.isNotEmpty() && binding.nacimientoEditText.text.isNotEmpty() && binding.telefonoEditText.text.isNotEmpty()) {

                val nombreUsuario = binding.usuarioEditText.text.toString();
                val nombre = binding.nombreEditText.text.toString();
                val nacimiento = binding.nacimientoEditText.text.toString();
                val telefono = binding.telefonoEditText.text.toString();

                val usuario = Usuario(nombreUsuario, nombre, nacimiento, telefono, 0)

                if(uid != null) {
                    databaseReference.child("Usuarios").child(uid).setValue(usuario).addOnCompleteListener {
                        if(it.isSuccessful) {
                            Toast.makeText(this, "Información del usuario actualizada", Toast.LENGTH_SHORT).show()
                            showHome()
                        } else {
                            Toast.makeText(this, "No se pudo actualizar la información del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

        }
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
    }
}