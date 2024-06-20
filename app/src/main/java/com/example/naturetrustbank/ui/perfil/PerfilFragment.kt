package com.example.naturetrustbank.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.naturetrustbank.LoginActivity
import com.example.naturetrustbank.UserDetailsActivity
import com.example.naturetrustbank.Usuario
import com.example.naturetrustbank.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid : String
    private lateinit var usuario : Usuario

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val perfilViewModel =
                ViewModelProvider(this).get(PerfilViewModel::class.java)

        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        perfilViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")

        val usuarioTitleLabel : TextView = binding.usuarioValorTextView
        val nombreTitleLabel : TextView = binding.nombreValorTextView
        val emailTitleLabel : TextView = binding.emailValorTextView
        val nacimientoTitleLabel : TextView = binding.nacimientoValorTextView
        val telefonoTitleLabel : TextView = binding.telefonoValorTextView
        val puntosTitleLabel : TextView = binding.puntosValorTextView

        if(uid.isNotEmpty()) {
            databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuario = snapshot.getValue(Usuario::class.java)!!
                    usuarioTitleLabel.setText(usuario.nombreUsuario)
                    nombreTitleLabel.setText(usuario.nombre)
                    emailTitleLabel.setText(auth.currentUser?.email.toString())
                    nacimientoTitleLabel.setText(usuario.nacimiento)
                    telefonoTitleLabel.setText(usuario.telefono)
                    puntosTitleLabel.setText(usuario.puntos.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        binding.editButton.setOnClickListener {
            showEdit()
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            showLogin()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showEdit() {
        val editIntent = Intent(this@PerfilFragment.requireContext(), UserDetailsActivity::class.java)
        editIntent.putExtra("nombreUsuario", binding.usuarioValorTextView.text)
        editIntent.putExtra("nombre", binding.nombreValorTextView.text)
        editIntent.putExtra("nacimiento", binding.nacimientoValorTextView.text)
        editIntent.putExtra("telefono", binding.telefonoValorTextView.text)
        editIntent.putExtra("EDIT", true)
        startActivity(editIntent)
    }

    private fun showLogin() {
        val loginIntent = Intent(this@PerfilFragment.requireContext(), LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginIntent.putExtra("EXIT", true)
        startActivity(loginIntent)
    }
}