package com.astarloa.esgrima

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

class MarcadorActivity : AppCompatActivity() {

    private lateinit var server: TcpServer

    private lateinit var scoreLeft: TextView
    private lateinit var scoreRight: TextView
    private lateinit var cardLeftYellow: View
    private lateinit var cardLeftRed: View
    private lateinit var cardRightYellow: View
    private lateinit var cardRightRed: View

    private lateinit var restOverlay: View
    private lateinit var restTimerText: TextView
    private lateinit var restPausedText: TextView
    private lateinit var txtIp: TextView

    private var isClientConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcador)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        scoreLeft = findViewById(R.id.scoreLeft)
        scoreRight = findViewById(R.id.scoreRight)
        cardLeftYellow = findViewById(R.id.cardLeftYellow)
        cardLeftRed = findViewById(R.id.cardLeftRed)
        cardRightYellow = findViewById(R.id.cardRightYellow)
        cardRightRed = findViewById(R.id.cardRightRed)

        restOverlay = findViewById(R.id.restOverlay)
        restTimerText = findViewById(R.id.restTimerText)
        restPausedText = findViewById(R.id.restPausedText)
        txtIp = findViewById(R.id.txtIp)

        val localIp = getLocalIpAddress()
        txtIp.text = "IP: $localIp"

        server = TcpServer { state ->
            runOnUiThread { updateUI(state) }
        }

        // Configurar callbacks del servidor
        server.onClientConnected = {
            runOnUiThread {
                isClientConnected = true
                Toast.makeText(this, "Control conectado", Toast.LENGTH_SHORT).show()
                txtIp.setBackgroundColor(getColor(R.color.green))
            }
        }

        server.onClientDisconnected = {
            runOnUiThread {
                isClientConnected = false
                Toast.makeText(this, "Control desconectado", Toast.LENGTH_SHORT).show()
                txtIp.setBackgroundColor(getColor(R.color.surface_1))
            }
        }

        server.onError = { message ->
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        server.start()

        Toast.makeText(this, "Esperando conexión del control...", Toast.LENGTH_LONG).show()
    }

    private fun updateUI(state: MatchState) {
        scoreLeft.text = String.format("%02d", state.leftScore)
        scoreRight.text = String.format("%02d", state.rightScore)

        cardLeftYellow.visibility = if (state.leftYellow) View.VISIBLE else View.GONE
        cardLeftRed.visibility = if (state.leftRed) View.VISIBLE else View.GONE
        cardRightYellow.visibility = if (state.rightYellow) View.VISIBLE else View.GONE
        cardRightRed.visibility = if (state.rightRed) View.VISIBLE else View.GONE

        if (state.restActive) {
            restOverlay.visibility = View.VISIBLE
            restTimerText.text = formatTime(state.restSecondsRemaining)
            restPausedText.visibility = if (state.restPaused) View.VISIBLE else View.GONE
        } else {
            restOverlay.visibility = View.GONE
        }
    }

    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%d:%02d", m, s)
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "0.0.0.0"
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
    }
}
