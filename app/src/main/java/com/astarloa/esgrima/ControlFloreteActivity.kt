package com.astarloa.esgrima

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class ControlFloreteActivity : AppCompatActivity() {

    private var continuousSeconds = 0 // Contador para la pasividad

    private var state = MatchState()
    private val client = TcpClient()
    private var connected = false
    private lateinit var stateManager: MatchStateManager

    private var restTimer: CountDownTimer? = null
    private var remainingRestMs: Long = 60_000

    private var combatTimer: CountDownTimer? = null
    private var remainingCombatMs: Long = 180_000

    private lateinit var txtScore: TextView
    private lateinit var txtRound: TextView
    private lateinit var timerDisplay: TextView

    private lateinit var layoutNormal: View
    private lateinit var layoutRest: View

    private lateinit var restTimerText: TextView
    private lateinit var btnPauseRest: Button
    private lateinit var btnResumeRest: Button

    private lateinit var editIp: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button

    private lateinit var btnStartTimer: Button
    private lateinit var btnPauseTimer: Button
    private lateinit var btnPriority: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_florete)

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initializeViews()
        stateManager = MatchStateManager(this)

        state = stateManager.loadState()
        updateUI()
        updatePriorityButton()
        updatePCardButtonsVisibility() // Aplicar visibilidad inicial de botones P

        // Cargar última IP usada
        val lastIp = stateManager.getLastIp()
        if (lastIp.isNotEmpty()) {
            editIp.setText(lastIp)

            if (stateManager.shouldAutoReconnect()) {
                btnConnect.postDelayed({
                    Toast.makeText(this, "Reconectando automáticamente...", Toast.LENGTH_SHORT).show()
                    btnConnect.isEnabled = false
                    btnConnect.text = "RECONECTANDO..."
                    client.connect(lastIp)
                }, 500)
            }
        }

        setupClientCallbacks()
        setupButtons()
    }

    private fun initializeViews() {
        editIp = findViewById(R.id.editIp)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)

        txtScore = findViewById(R.id.txtScore)
        txtRound = findViewById(R.id.txtRound)
        timerDisplay = findViewById(R.id.timerDisplay)

        layoutNormal = findViewById(R.id.layoutNormal)
        layoutRest = findViewById(R.id.layoutRest)

        restTimerText = findViewById(R.id.restTimerControl)
        btnPauseRest = findViewById(R.id.btnPauseRest)
        btnResumeRest = findViewById(R.id.btnResumeRest)

        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnPauseTimer = findViewById(R.id.btnPauseTimer)
        btnPriority = findViewById(R.id.btnPriority)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupClientCallbacks() {
        client.onConnectionResult = { success, message ->
            runOnUiThread {
                if (success) {
                    connected = true
                    stateManager.clearDisconnectTime()
                    editIp.isEnabled = false
                    btnConnect.isEnabled = false
                    btnConnect.visibility = View.GONE
                    btnDisconnect.visibility = View.VISIBLE
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    client.sendState(state)
                } else {
                    connected = false
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    btnConnect.isEnabled = true
                    btnConnect.text = "CONECTAR"
                }
            }
        }

        client.onDisconnected = {
            runOnUiThread {
                connected = false
                stateManager.saveDisconnectTime()
                editIp.isEnabled = true
                btnConnect.isEnabled = true
                btnConnect.text = "CONECTAR"
                btnConnect.visibility = View.VISIBLE
                btnDisconnect.visibility = View.GONE
                Toast.makeText(this, "Desconectado del marcador", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        btnConnect.setOnClickListener {
            val ip = editIp.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Ingresa una IP válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConnect.isEnabled = false
            btnConnect.text = "CONECTANDO..."
            stateManager.saveLastIp(ip)
            client.connect(ip)
        }

        btnDisconnect.setOnClickListener {
            client.disconnect()
        }

        // Botón de configuración
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        // Timer
        findViewById<Button>(R.id.btnTimer1Min).setOnClickListener {
            ifCheckConnection {
                setTimerSeconds(60)
            }
        }

        findViewById<Button>(R.id.btnTimer3Min).setOnClickListener {
            ifCheckConnection {
                setTimerSeconds(180)
            }
        }

        findViewById<Button>(R.id.btnTimerCustom).setOnClickListener {
            ifCheckConnection {
                showCustomTimerDialog()
            }
        }

        btnStartTimer.setOnClickListener {
            ifCheckConnection {
                startCombatTimer()
            }
        }

        btnPauseTimer.setOnClickListener {
            ifCheckConnection {
                pauseCombatTimer()
            }
        }

        //Rondas
        findViewById<Button>(R.id.btnRoundPlus).setOnClickListener {
            ifCheckConnection {
                if (state.currentRound < 3) {
                    state.currentRound++
                    updateAndSend()
                }
            }
        }

        findViewById<Button>(R.id.btnRoundMinus).setOnClickListener {
            ifCheckConnection {
                if (state.currentRound > 1) {
                    state.currentRound--
                    updateAndSend()
                }
            }
        }

        btnPriority.setOnClickListener {
            ifCheckConnection {
                if (state.priorityActive) {
                    // Quitar prioridad
                    state.priorityActive = false
                    Toast.makeText(this, "Prioridad eliminada", Toast.LENGTH_SHORT).show()
                } else {
                    // Asignar prioridad
                    setPriority()
                }
                updatePriorityButton()
                updateAndSend()
            }
        }

        setupScoreButtons()
        setupCardButtons()
        setupRestButtons()

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            ifCheckConnection {
                resetAll()
            }
        }
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        val checkPassivityCard = dialogView.findViewById<CheckBox>(R.id.checkPassivityCard)
        val checkSounds = dialogView.findViewById<CheckBox>(R.id.checkSounds)

        // Cargar configuración actual
        checkPassivityCard.isChecked = state.passivityCardEnabled
        checkSounds.isChecked = state.soundsEnabled

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("GUARDAR") { _, _ ->
                state.passivityCardEnabled = checkPassivityCard.isChecked
                state.soundsEnabled = checkSounds.isChecked

                // Si se desactiva la función de pasividad, limpiar tarjetas P existentes
                if (!state.passivityCardEnabled) {
                    clearAllPCards()
                }

                // Mostrar u ocultar botones P según la configuración
                updatePCardButtonsVisibility()

                updateAndSend()
                Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun setupScoreButtons() {
        findViewById<Button>(R.id.btnLeftPlus).setOnClickListener {
            ifCheckConnection { if (state.leftScore < 99) state.leftScore++; updateAndSend() }
        }
        findViewById<Button>(R.id.btnLeftMinus).setOnClickListener {
            ifCheckConnection { if (state.leftScore > 0) state.leftScore--; updateAndSend() }
        }
        findViewById<Button>(R.id.btnRightPlus).setOnClickListener {
            ifCheckConnection { if (state.rightScore < 99) state.rightScore++; updateAndSend() }
        }
        findViewById<Button>(R.id.btnRightMinus).setOnClickListener {
            ifCheckConnection { if (state.rightScore > 0) state.rightScore--; updateAndSend() }
        }
    }

    private fun setupCardButtons() {
        val btnLeftYellow = findViewById<Button>(R.id.btnLeftYellow)
        val btnRightYellow = findViewById<Button>(R.id.btnRightYellow)
        val btnLeftRed = findViewById<Button>(R.id.btnLeftRed)
        val btnRightRed = findViewById<Button>(R.id.btnRightRed)

        // Nuevos botones para tarjetas P
        val btnLeftYellowP = findViewById<Button>(R.id.btnLeftYellowP)
        val btnRightYellowP = findViewById<Button>(R.id.btnRightYellowP)
        val btnLeftRedP = findViewById<Button>(R.id.btnLeftRedP)
        val btnRightRedP = findViewById<Button>(R.id.btnRightRedP)

        btnLeftYellow.setOnClickListener {
            ifCheckConnection {
                state.leftYellow = !state.leftYellow
                btnLeftYellow.alpha = if (state.leftYellow) 1f else 0.4f
                updateAndSend()
            }
        }

        btnRightYellow.setOnClickListener {
            ifCheckConnection {
                state.rightYellow = !state.rightYellow
                btnRightYellow.alpha = if (state.rightYellow) 1f else 0.4f
                updateAndSend()
            }
        }

        btnLeftRed.setOnClickListener {
            ifCheckConnection {
                state.leftRed = !state.leftRed
                btnLeftRed.alpha = if (state.leftRed) 1f else 0.4f
                if (state.leftRed && state.rightScore < 99) state.rightScore++
                if (!state.leftRed && state.rightScore > 0) state.rightScore--
                updateAndSend()
            }
        }

        btnRightRed.setOnClickListener {
            ifCheckConnection {
                state.rightRed = !state.rightRed
                btnRightRed.alpha = if (state.rightRed) 1f else 0.4f
                if (state.rightRed && state.leftScore < 99) state.leftScore++
                if (!state.rightRed && state.leftScore > 0) state.leftScore--
                updateAndSend()
            }
        }

        // Tarjetas P - Amarilla
        btnLeftYellowP.setOnClickListener {
            ifCheckConnection {
                state.leftYellowP = !state.leftYellowP
                btnLeftYellowP.alpha = if (state.leftYellowP) 1f else 0.4f
                updateAndSend()
            }
        }

        btnRightYellowP.setOnClickListener {
            ifCheckConnection {
                state.rightYellowP = !state.rightYellowP
                btnRightYellowP.alpha = if (state.rightYellowP) 1f else 0.4f
                updateAndSend()
            }
        }

        // Tarjetas P - Roja
        btnLeftRedP.setOnClickListener {
            ifCheckConnection {
                state.leftRedP = !state.leftRedP
                btnLeftRedP.alpha = if (state.leftRedP) 1f else 0.4f
                if (state.leftRedP && state.rightScore < 99) state.rightScore++
                if (!state.leftRedP && state.rightScore > 0) state.rightScore--
                updateAndSend()
            }
        }

        btnRightRedP.setOnClickListener {
            ifCheckConnection {
                state.rightRedP = !state.rightRedP
                btnRightRedP.alpha = if (state.rightRedP) 1f else 0.4f
                if (state.rightRedP && state.leftScore < 99) state.leftScore++
                if (!state.rightRedP && state.leftScore > 0) state.leftScore--
                updateAndSend()
            }
        }
    }

    private fun setupRestButtons() {
        findViewById<Button>(R.id.btnRest).setOnClickListener {
            ifCheckConnection { startRestMinute() }
        }

        btnPauseRest.setOnClickListener { pauseRest() }
        btnResumeRest.setOnClickListener { resumeRest() }

        findViewById<Button>(R.id.btnEndRest).setOnClickListener {
            endRestMinute()
        }
    }

    private fun setTimerSeconds(seconds: Int) {
        combatTimer?.cancel()

        state.timerSeconds = seconds
        state.timerRunning = false
        state.timerPaused = false
        remainingCombatMs = (seconds * 1000).toLong()

        btnStartTimer.visibility = View.VISIBLE
        btnPauseTimer.visibility = View.GONE

        updateAndSend()
    }

    private fun startCombatTimer() {
        state.timerRunning = true
        state.timerPaused = false
        state.soundCode = 0 // Reiniciar sonido al arrancar
        continuousSeconds = 0 // Reiniciar contador de pasividad

        btnStartTimer.visibility = View.GONE
        btnPauseTimer.visibility = View.VISIBLE

        combatTimer = object : CountDownTimer(remainingCombatMs, 1000) {
            override fun onTick(ms: Long) {
                remainingCombatMs = ms
                state.timerSeconds = (ms / 1000).toInt()

                continuousSeconds++

                // Solo aplicar tarjeta P si está habilitada la configuración
                if (state.passivityCardEnabled && continuousSeconds >= 60) {
                    pauseCombatTimer()

                    // Mandar señal de sonido y alerta
                    state.soundCode = 2
                    Toast.makeText(this@ControlFloreteActivity, "1 Minuto de Pasividad", Toast.LENGTH_LONG).show()
                } else {
                    state.soundCode = 0 // Asegurar que no suene nada
                }

                updateAndSend()
            }

            override fun onFinish() {
                state.timerSeconds = 0
                state.timerRunning = false
                state.soundCode = 1

                btnStartTimer.visibility = View.VISIBLE
                btnPauseTimer.visibility = View.GONE
                updateAndSend()
            }
        }.start()

        updateAndSend()
    }

    private fun pauseCombatTimer() {
        combatTimer?.cancel()
        state.timerPaused = true
        state.timerRunning = false
        btnStartTimer.visibility = View.VISIBLE
        btnPauseTimer.visibility = View.GONE
        updateAndSend()
    }

    private fun showCustomTimerDialog() {
        val input = EditText(this)
        input.hint = "Segundos (ej: 120 para 2 min)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Tiempo personalizado")
            .setMessage("Ingresa los segundos:")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val seconds = input.text.toString().toIntOrNull()
                if (seconds != null && seconds > 0) {
                    setTimerSeconds(seconds)
                } else {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setPriority() {
        state.priorityActive = true
        state.priorityLeft = Random.nextBoolean()

        val side = if (state.priorityLeft) "IZQUIERDA" else "DERECHA"
        Toast.makeText(this, "Prioridad: $side", Toast.LENGTH_SHORT).show()
    }

    private fun updatePriorityButton() {
        if (state.priorityActive) {
            val side = if (state.priorityLeft) "←" else "→"
            btnPriority.text = "QUITAR PRIORIDAD"
        } else {
            btnPriority.text = "ASIGNAR PRIORIDAD"
        }
    }

    private fun startRestMinute() {
        layoutNormal.visibility = View.GONE
        layoutRest.visibility = View.VISIBLE

        remainingRestMs = 60_000
        state.restActive = true
        state.restPaused = false

        restTimer = object : CountDownTimer(remainingRestMs, 1000) {
            override fun onTick(ms: Long) {
                remainingRestMs = ms
                state.restSecondsRemaining = (ms / 1000).toInt()
                restTimerText.text = formatTime(state.restSecondsRemaining)
                updateAndSend()
            }

            override fun onFinish() {
                state.restSecondsRemaining = 0
                restTimerText.text = "0:00"
                updateAndSend()

                restTimerText.postDelayed({
                    endRestMinute()
                }, 1000)
            }
        }.start()
    }

    private fun pauseRest() {
        restTimer?.cancel()
        state.restPaused = true
        btnPauseRest.visibility = View.GONE
        btnResumeRest.visibility = View.VISIBLE
        updateAndSend()
    }

    private fun resumeRest() {
        state.restPaused = false
        btnPauseRest.visibility = View.VISIBLE
        btnResumeRest.visibility = View.GONE

        restTimer = object : CountDownTimer(remainingRestMs, 1000) {
            override fun onTick(ms: Long) {
                remainingRestMs = ms
                state.restSecondsRemaining = (ms / 1000).toInt()
                restTimerText.text = formatTime(state.restSecondsRemaining)
                updateAndSend()
            }

            override fun onFinish() {
                state.restSecondsRemaining = 0
                endRestMinute()
            }
        }.start()
    }

    private fun endRestMinute() {
        restTimer?.cancel()
        state.restActive = false
        state.restPaused = false
        updateAndSend()

        layoutRest.visibility = View.GONE
        layoutNormal.visibility = View.VISIBLE
    }

    private fun resetAll() {
        combatTimer?.cancel()

        state.leftScore = 0
        state.rightScore = 0
        state.leftYellow = false
        state.rightYellow = false
        state.leftRed = false
        state.rightRed = false
        state.leftYellowP = false
        state.rightYellowP = false
        state.leftRedP = false
        state.rightRedP = false
        state.timerSeconds = 180
        state.timerRunning = false
        state.timerPaused = false
        state.currentRound = 1
        state.priorityActive = false

        remainingCombatMs = 180_000
        btnStartTimer.visibility = View.VISIBLE
        btnPauseTimer.visibility = View.GONE

        updateUI()
        updateCardButtons()
        updatePriorityButton()
        stateManager.saveState(state)
        client.sendState(state)
    }

    private fun updateUI() {
        txtScore.text = String.format("%02d - %02d", state.leftScore, state.rightScore)
        txtRound.text = "  |  ${state.currentRound}/3"
        timerDisplay.text = formatTime(state.timerSeconds)
    }

    private fun updateCardButtons() {
        findViewById<Button>(R.id.btnLeftYellow).alpha = if (state.leftYellow) 1f else 0.4f
        findViewById<Button>(R.id.btnRightYellow).alpha = if (state.rightYellow) 1f else 0.4f
        findViewById<Button>(R.id.btnLeftRed).alpha = if (state.leftRed) 1f else 0.4f
        findViewById<Button>(R.id.btnRightRed).alpha = if (state.rightRed) 1f else 0.4f

        findViewById<Button>(R.id.btnLeftYellowP).alpha = if (state.leftYellowP) 1f else 0.4f
        findViewById<Button>(R.id.btnRightYellowP).alpha = if (state.rightYellowP) 1f else 0.4f
        findViewById<Button>(R.id.btnLeftRedP).alpha = if (state.leftRedP) 1f else 0.4f
        findViewById<Button>(R.id.btnRightRedP).alpha = if (state.rightRedP) 1f else 0.4f
    }

    private fun updatePCardButtonsVisibility() {
        val visibility = if (state.passivityCardEnabled) View.VISIBLE else View.GONE

        findViewById<Button>(R.id.btnLeftYellowP).visibility = visibility
        findViewById<Button>(R.id.btnRightYellowP).visibility = visibility
        findViewById<Button>(R.id.btnLeftRedP).visibility = visibility
        findViewById<Button>(R.id.btnRightRedP).visibility = visibility
    }

    private fun clearAllPCards() {
        // Limpiar todas las tarjetas P del estado
        state.leftYellowP = false
        state.rightYellowP = false
        state.leftRedP = false
        state.rightRedP = false

        // Actualizar la opacidad de los botones P
        updateCardButtons()
    }

    private fun updateAndSend() {
        updateUI()
        stateManager.saveState(state)
        client.sendState(state)
    }

    private fun ifCheckConnection(action: () -> Unit) {
        if (!connected) {
            Toast.makeText(this, "Debe conectarse primero", Toast.LENGTH_SHORT).show()
        } else {
            action()
        }
    }

    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        restTimer?.cancel()
        combatTimer?.cancel()
        client.disconnect()
    }
}
