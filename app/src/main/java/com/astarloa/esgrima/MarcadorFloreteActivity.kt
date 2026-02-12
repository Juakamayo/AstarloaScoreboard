package com.astarloa.esgrima

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator

class MarcadorFloreteActivity : AppCompatActivity() {

    private val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100) //sonido volumen 100

    private lateinit var layoutLeft: View
    private lateinit var layoutRight: View

    private lateinit var server: TcpServer
    private lateinit var stateManager: MatchStateManager
    private var currentState = MatchState()

    private lateinit var scoreLeft: TextView
    private lateinit var scoreRight: TextView
    private lateinit var cardLeftYellow: View
    private lateinit var cardLeftRed: View
    private lateinit var cardRightYellow: View
    private lateinit var cardRightRed: View

    private lateinit var timerText: TextView
    private lateinit var roundText: TextView
    private lateinit var priorityLight: View
    private lateinit var priorityCircle: View

    private lateinit var restOverlay: View
    private lateinit var restTimerText: TextView
    private lateinit var restPausedText: TextView
    private lateinit var txtIp: TextView

    private var priorityLightReady = false

    private fun flashBackground(view: View, originalColorRes: Int, flashColorRes: Int) {

        val bgDrawable = view.background as? GradientDrawable ?: return

        val colorOriginal = getColor(originalColorRes)
        val colorFlash = getColor(flashColorRes)

        // Animación: Original, Flash, Original
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorOriginal, colorFlash, colorOriginal)
        colorAnimation.duration = 700 //(ms)

        colorAnimation.addUpdateListener { animator ->
            bgDrawable.setColor(animator.animatedValue as Int)
        }

        colorAnimation.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcador_florete)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        layoutLeft = findViewById(R.id.layoutLeft)
        layoutRight = findViewById(R.id.layoutRight)

        scoreLeft = findViewById(R.id.scoreLeft)
        scoreRight = findViewById(R.id.scoreRight)
        cardLeftYellow = findViewById(R.id.cardLeftYellow)
        cardLeftRed = findViewById(R.id.cardLeftRed)
        cardRightYellow = findViewById(R.id.cardRightYellow)
        cardRightRed = findViewById(R.id.cardRightRed)

        timerText = findViewById(R.id.timerText)
        roundText = findViewById(R.id.roundText)
        priorityLight = findViewById(R.id.priorityLight)
        priorityCircle = findViewById(R.id.priorityCircle)

        restOverlay = findViewById(R.id.restOverlay)
        restTimerText = findViewById(R.id.restTimerText)
        restPausedText = findViewById(R.id.restPausedText)
        txtIp = findViewById(R.id.txtIp)

        // Esperar a que el layout esté listo
        priorityLight.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                priorityLight.viewTreeObserver.removeOnGlobalLayoutListener(this)
                priorityLightReady = true
                // Si ya hay un estado con prioridad activa, actualizarlo ahora
                if (currentState.priorityActive) {
                    updatePriorityUI(currentState)
                }
            }
        })

        stateManager = MatchStateManager(this)

        // Cargar estado guardado
        currentState = stateManager.loadState()
        updateUI(currentState)

        val localIp = getLocalIpAddress()
        txtIp.text = "IP: $localIp"

        server = TcpServer { newState ->
            runOnUiThread {
                // DETECTAR SUBIDA DE PUNTOS
                // Si el nuevo puntaje es mayor al actual, lanzar flash
                if (newState.leftScore > currentState.leftScore) {
                    flashBackground(layoutLeft, R.color.surface_2, R.color.flash_red)
                }
                if (newState.rightScore > currentState.rightScore) {
                    flashBackground(layoutRight, R.color.surface_1, R.color.flash_green)
                }
                if (newState.soundCode != 0 && newState.soundCode != currentState.soundCode) {
                    when (newState.soundCode) {
                        1 -> toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000) // Fin: Largo (2s)
                        2 -> toneGen.startTone(ToneGenerator.TONE_SUP_BUSY, 1000) // Pasividad: Pitido repetido (1s)
                    }
                }

                // Actualizar estado
                currentState = newState
                updateUI(newState)
                stateManager.saveState(newState)
            }
        }

        server.onClientConnected = {
            runOnUiThread {
                Toast.makeText(this, "Control conectado", Toast.LENGTH_SHORT).show()
                txtIp.setBackgroundColor(getColor(R.color.green))
            }
        }

        server.onClientDisconnected = {
            runOnUiThread {
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


        timerText.text = formatTime(state.timerSeconds)

        // Ronda
        roundText.text = "${state.currentRound}/3"

        // Prioridad
        updatePriorityUI(state)

        // Descanso (minute)
        if (state.restActive) {
            restOverlay.visibility = View.VISIBLE
            restTimerText.text = formatTime(state.restSecondsRemaining)
            restPausedText.visibility = if (state.restPaused) View.VISIBLE else View.GONE
        } else {
            restOverlay.visibility = View.GONE
        }
    }

    private fun updatePriorityUI(state: MatchState) {
        if (state.priorityActive) {
            priorityLight.visibility = View.VISIBLE

            // Solo animar si el layout ya está listo
            if (priorityLightReady && priorityLight.width > 0) {
                // Animar la luz moviéndose al lado correspondiente
                val targetX = if (state.priorityLeft) {
                    -priorityLight.width.toFloat() * 2
                } else {
                    priorityLight.width.toFloat() * 2
                }

                ObjectAnimator.ofFloat(priorityLight, "translationX", targetX).apply {
                    duration = 800
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }

                // Efecto de pulso
                ObjectAnimator.ofFloat(priorityCircle, "alpha", 1f, 0.3f, 1f).apply {
                    duration = 1000
                    repeatCount = 2
                    start()
                }
            } else {
                // Si el layout no está listo, posicionar directamente sin animación
                priorityLight.post {
                    if (priorityLight.width > 0) {
                        val targetX = if (state.priorityLeft) {
                            -priorityLight.width.toFloat() * 2
                        } else {
                            priorityLight.width.toFloat() * 2
                        }
                        priorityLight.translationX = targetX

                        // Efecto de pulso
                        ObjectAnimator.ofFloat(priorityCircle, "alpha", 1f, 0.3f, 1f).apply {
                            duration = 1000
                            repeatCount = 2
                            start()
                        }
                    }
                }
            }
        } else {
            priorityLight.visibility = View.GONE
            priorityLight.translationX = 0f
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