package com.example.naturetrustbank.ui.rewards

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import kotlin.random.Random
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
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
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class RewardsFragment : Fragment() {

    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var usuario: Usuario
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private val deviceAddress = "00:22:09:01:C2:7D" // Dirección MAC del módulo HC-06
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID para SPP
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_BLUETOOTH_PERMISSIONS = 2
    private var isConnected = false // Bandera para mantener el estado de la conexión

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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BLUETOOTH_PERMISSIONS)
        } else {
            showBluetoothWaitingDialog()
        }
    }

    private fun showBluetoothWaitingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_bluetooth_waiting, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val checkMark = dialogView.findViewById<ImageView>(R.id.checkMark)
        val cancelMark = dialogView.findViewById<ImageView>(R.id.cancelMark)
        val textViewStatus = dialogView.findViewById<TextView>(R.id.textViewStatus)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        // Connect to Bluetooth device
        connectToBluetoothDevice()

        // Handle connection result
        Handler(Looper.getMainLooper()).postDelayed({
            if (isConnected) {
                progressBar.visibility = View.GONE
                checkMark.visibility = View.VISIBLE
                textViewStatus.text = "Conexión establecida"
                setupBluetoothCommunication()
                aumentarPuntos()
            } else {
                progressBar.visibility = View.GONE
                cancelMark.visibility = View.VISIBLE
                textViewStatus.text = "Error al conectar"
                Toast.makeText(requireContext(), "Error al conectar con el dispositivo Bluetooth", Toast.LENGTH_SHORT).show()
            }

            // Dismiss dialog after some time
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
            }, 2000)

        }, 5000) // Simulate 5 seconds delay
    }

    private fun connectToBluetoothDevice() {
        closeBluetoothConnection() // Ensure any previous connection is closed

        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device?.let {
            try {
                // Solicitar permisos BLUETOOTH y BLUETOOTH_ADMIN si no están concedidos
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BLUETOOTH_PERMISSIONS)
                } else {
                    // Los permisos están concedidos, proceder con la conexión Bluetooth
                    bluetoothSocket = it.createRfcommSocketToServiceRecord(uuid)
                    bluetoothSocket?.connect()
                    inputStream = bluetoothSocket!!.inputStream
                    outputStream = bluetoothSocket!!.outputStream
                    isConnected = true // Establecer la bandera de conexión a verdadero
                }
            } catch (e: IOException) {
                e.printStackTrace()
                closeBluetoothConnection()
                isConnected = false // Establecer la bandera de conexión a falso en caso de error
            }
        }
    }

    private fun closeBluetoothConnection() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bluetoothSocket = null
            isConnected = false
        }
    }

    private fun setupBluetoothCommunication() {
        if (isConnected) {
            // Aquí puedes manejar la comunicación Bluetooth, enviar y recibir datos según tu necesidad
            // Por ejemplo, enviar un comando al Arduino para solicitar confirmación
            val command = "C\n"
            try {
                outputStream.write(command.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al enviar datos por Bluetooth", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Manejar el caso donde no está conectado
            Toast.makeText(requireContext(), "No se ha establecido una conexión Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    private fun aumentarPuntos() {
        val uid = auth.currentUser?.uid
        uid?.let {
            val randomPoints = Random.nextInt(5, 26)
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentPoints = snapshot.child("puntos").getValue(Long::class.java) ?: 0
                    val newPoints = currentPoints + randomPoints

                    databaseReference.child(uid).child("puntos").setValue(newPoints)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.textViewPoints.text = newPoints.toString()
                                Toast.makeText(requireContext(), "Puntos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                agregarFila(randomPoints.toString(), currentDate, "UNMSM")
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

    private fun agregarFila(nPuntos: String, fecha: String, lugar: String) {
        val tableLayout: TableLayout = binding.root.findViewById(R.id.tableLayout)
        val tableRow = TableRow(requireContext())

        val nPuntosTextView = TextView(requireContext()).apply {
            text = nPuntos
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }

        val fechaTextView = TextView(requireContext()).apply {
            text = fecha
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }

        val lugarTextView = TextView(requireContext()).apply {
            text = lugar
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }

        tableRow.addView(nPuntosTextView)
        tableRow.addView(fechaTextView)
        tableRow.addView(lugarTextView)

        tableLayout.addView(tableRow)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
        closeBluetoothConnection()
    }
}