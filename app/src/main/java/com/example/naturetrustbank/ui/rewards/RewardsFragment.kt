package com.example.naturetrustbank.ui.rewards

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.naturetrustbank.MainActivity
import com.example.naturetrustbank.R
import com.example.naturetrustbank.databinding.FragmentRewardsBinding
import com.example.naturetrustbank.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.io.IOException
import java.util.*
import kotlin.random.Random

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var usuario: Usuario
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private val deviceAddress = "XX:XX:XX:XX:XX:XX" // Dirección MAC del módulo HC-06
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID para SPP
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_BLUETOOTH_PERMISSIONS = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        val root = binding.root

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")

        // Access MainActivity to get the user data
        val mainActivity = requireActivity() as MainActivity
        usuario = mainActivity.usuario

        // Update UI with user points
        binding.textViewPoints.text = usuario.puntos.toString()

        // Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        binding.bigButton.setOnClickListener {
            checkBluetoothPermissionsAndConnect()
        }

        return root
    }

    private fun checkBluetoothPermissionsAndConnect() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSIONS)
        } else {
            showBluetoothWaitingDialog()
        }
    }

    private fun showBluetoothWaitingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_bluetooth_waiting, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val checkMark = dialogView.findViewById<ImageView>(R.id.checkMark)
        val textViewStatus = dialogView.findViewById<TextView>(R.id.textViewStatus)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        // Connect to Bluetooth device
        connectToBluetoothDevice()

        // Simulate Bluetooth confirmation after a delay (replace with actual Bluetooth handling code)
        Handler(Looper.getMainLooper()).postDelayed({
            // Simulate receiving Bluetooth signal confirmation
            progressBar.visibility = View.GONE
            checkMark.visibility = View.VISIBLE
            textViewStatus.text = "Conexión establecida"

            // Aumentar puntos después de la confirmación
            aumentarPuntos()

            // Unregister receiver and dismiss dialog after some time
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
            }, 2000)

        }, 5000) // Simulate 5 seconds delay
    }

    private fun connectToBluetoothDevice() {
        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device?.let {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Si los permisos no están concedidos, retornamos
                    return
                }
                bluetoothSocket = it.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                // Aquí puedes manejar la conexión establecida, enviar/recibir datos, etc.
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al conectar con el dispositivo Bluetooth", Toast.LENGTH_SHORT).show()
                // Cierra el socket en caso de error
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    closeException.printStackTrace()
                }
            }
        }
    }

    private fun aumentarPuntos() {
        // Get current user's UID
        val uid = auth.currentUser?.uid

        // Ensure UID is not null before proceeding
        uid?.let {
            // Generate a random number between 25 and 120
            val randomPoints = Random.nextInt(25, 121)

            // Query current points from Firebase
            databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get current user's points
                    val currentPoints = snapshot.child("puntos").getValue(Long::class.java) ?: 0

                    // Calculate new points
                    val newPoints = currentPoints + randomPoints

                    // Update points in Firebase
                    databaseReference.child(uid).child("puntos").setValue(newPoints)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Update local UI
                                binding.textViewPoints.text = newPoints.toString()
                                Toast.makeText(requireContext(), "Puntos actualizados correctamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Error al actualizar los puntos", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error en la consulta de puntos", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showBluetoothWaitingDialog()
                } else {
                    Toast.makeText(requireContext(), "Permisos de Bluetooth denegados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Ensure receiver is unregistered to avoid memory leaks
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}