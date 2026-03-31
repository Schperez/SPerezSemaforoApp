package com.example.semaforo

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private var estadoBluetooth by mutableStateOf("Desconectado")
    private var conectado by mutableStateOf(false)

    private var led1Encendido by mutableStateOf(false)
    private var led2Encendido by mutableStateOf(false)
    private var led3Encendido by mutableStateOf(false)

    private val activarBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (bluetoothAdapter?.isEnabled == true) {
                    conectarModulo()
                } else {
                    estadoBluetooth = "Bluetooth apagado"
                }
            } catch (e: Exception) {
                estadoBluetooth = "Error al activar Bluetooth: ${e.message}"
            }
        }

    private val permisoBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                verificarBluetoothYConectar()
            } else {
                estadoBluetooth = "Permiso Bluetooth denegado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaSemaforo(
                        estadoBluetooth = estadoBluetooth,
                        conectado = conectado,
                        led1Encendido = led1Encendido,
                        led2Encendido = led2Encendido,
                        led3Encendido = led3Encendido,
                        onConectarClick = { iniciarConexion() },
                        onLed1On = { enviarComando('1') },
                        onLed1Off = { enviarComando('2') },
                        onLed2On = { enviarComando('3') },
                        onLed2Off = { enviarComando('4') },
                        onLed3On = { enviarComando('5') },
                        onLed3Off = { enviarComando('6') },
                        onApagarTodos = { enviarComando('0') }
                    )
                }
            }
        }
    }

    private fun iniciarConexion() {
        try {
            solicitarPermisosYConectar()
        } catch (e: Exception) {
            estadoBluetooth = "Error al iniciar conexión: ${e.message}"
        }
    }

    private fun solicitarPermisosYConectar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisoBluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
        }
        verificarBluetoothYConectar()
    }

    private fun verificarBluetoothYConectar() {
        try {
            val adapter = bluetoothAdapter

            if (adapter == null) {
                estadoBluetooth = "Este celular no soporta Bluetooth"
                return
            }

            if (!adapter.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activarBluetoothLauncher.launch(intent)
                return
            }

            conectarModulo()
        } catch (e: Exception) {
            estadoBluetooth = "Error Bluetooth: ${e.message}"
        }
    }

    @SuppressLint("MissingPermission")
    private fun conectarModulo() {
        estadoBluetooth = "Buscando SLAVE..."

        Thread {
            try {
                val adapter = bluetoothAdapter ?: throw Exception("Bluetooth no disponible")

                val dispositivo = adapter.bondedDevices.firstOrNull {
                    val nombre = it.name ?: ""
                    nombre.equals("SLAVE", ignoreCase = true)
                } ?: throw Exception("No encuentro el dispositivo SLAVE emparejado")

                adapter.cancelDiscovery()

                try {
                    outputStream?.close()
                    bluetoothSocket?.close()
                } catch (_: Exception) {
                }

                val uuidSPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = dispositivo.createRfcommSocketToServiceRecord(uuidSPP)
                socket.connect()

                bluetoothSocket = socket
                outputStream = socket.outputStream

                runOnUiThread {
                    conectado = true
                    estadoBluetooth = "Conectado a ${dispositivo.name}"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    conectado = false
                    estadoBluetooth = "Error: ${e.javaClass.simpleName}: ${e.message}"
                }
            }
        }.start()
    }

    private fun enviarComando(comando: Char) {
        if (!conectado || outputStream == null) {
            estadoBluetooth = "No hay conexión Bluetooth"
            return
        }

        try {
            outputStream?.write(comando.code)
            outputStream?.flush()

            when (comando) {
                '0' -> {
                    led1Encendido = false
                    led2Encendido = false
                    led3Encendido = false
                }
                '1' -> led1Encendido = true
                '2' -> led1Encendido = false
                '3' -> led2Encendido = true
                '4' -> led2Encendido = false
                '5' -> led3Encendido = true
                '6' -> led3Encendido = false
            }

        } catch (e: Exception) {
            conectado = false
            estadoBluetooth = "Se perdió la conexión: ${e.message}"
        }
    }

    override fun onDestroy() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (_: Exception) {
        }
        super.onDestroy()
    }
}

@Composable
fun PantallaSemaforo(
    estadoBluetooth: String,
    conectado: Boolean,
    led1Encendido: Boolean,
    led2Encendido: Boolean,
    led3Encendido: Boolean,
    onConectarClick: () -> Unit,
    onLed1On: () -> Unit,
    onLed1Off: () -> Unit,
    onLed2On: () -> Unit,
    onLed2Off: () -> Unit,
    onLed3On: () -> Unit,
    onLed3Off: () -> Unit,
    onApagarTodos: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Control de LEDs",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (conectado) Color.Green else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = estadoBluetooth)
                }

                Spacer(modifier = Modifier.size(12.dp))

                Button(onClick = onConectarClick) {
                    Text("Conectar")
                }
            }
        }

        Button(
            onClick = onApagarTodos,
            enabled = conectado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apagar todos")
        }

        TarjetaLed("LED 1", Color.Green, led1Encendido, onLed1On, onLed1Off, conectado)
        TarjetaLed("LED 2", Color.Yellow, led2Encendido, onLed2On, onLed2Off, conectado)
        TarjetaLed("LED 3", Color.Red, led3Encendido, onLed3On, onLed3Off, conectado)
    }
}

@Composable
fun TarjetaLed(
    nombreLed: String,
    colorActivo: Color,
    encendido: Boolean,
    onOnClick: () -> Unit,
    onOffClick: () -> Unit,
    habilitado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombreLed,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (encendido) colorActivo else Color.Gray,
                            shape = CircleShape
                        )
                )
            }

            Text(
                text = if (encendido) "Estado: ENCENDIDO" else "Estado: APAGADO"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onOnClick, enabled = habilitado) {
                    Text("ON")
                }

                Spacer(modifier = Modifier.size(8.dp))

                Button(onClick = onOffClick, enabled = habilitado) {
                    Text("OFF")
                }
            }
        }
    }
}