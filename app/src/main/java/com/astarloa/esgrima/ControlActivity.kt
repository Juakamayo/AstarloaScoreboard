package com.astarloa.esgrima

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity() {

    private var state = MatchState()
    private val client = TcpClient()
    private var connected = false
    private lateinit var stateManager: MatchStateManager

    private var restTimer: CountDownTimer? = null
    private var remainingMs: Long = 60_000

    private lateinit var txtScore: TextView
    private lateinit var layoutNormal: View
    private lateinit var layoutRest: View
    private lateinit var restTimerText: TextView
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var editIp: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        editIp = findViewById(R.id.editIp)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)

        txtScore = findViewById(R.id.txtScore)
        layoutNormal = findViewById(R.id.layoutNormal)
        layoutRest = findViewById(R.id.layoutRest)
        restTimerText = findViewById(R.id.restTimerControl)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)

        stateManager = MatchStateManager(this)


        state = stateManager.loadState()
        updateScoreText()
        updateCardButtons()


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

        btnConnect.setOnClickListener {
            val ip = editIp.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Ingresa una IP válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConnect.isEnabled = false
            btnConnect.text = "CONECTANDO..."
            Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show()


            stateManager.saveLastIp(ip)

            client.connect(ip)
        }

        btnDisconnect.setOnClickListener {
            client.disconnect()
        }

        setupScoreButtons()
        setupCardButtons()
        setupBottomButtons()

        updateScoreText()


        checkAutoReconnect()
    }

    private fun setupScoreButtons() {
        findViewById<Button>(R.id.btnLeftPlus).setOnClickListener {
            ifCheckConnection { if (state.leftScore < 99) state.leftScore++ }
        }
        findViewById<Button>(R.id.btnLeftMinus).setOnClickListener {
            ifCheckConnection { if (state.leftScore > 0) state.leftScore-- }
        }
        findViewById<Button>(R.id.btnRightPlus).setOnClickListener {
            ifCheckConnection { if (state.rightScore < 99) state.rightScore++ }
        }
        findViewById<Button>(R.id.btnRightMinus).setOnClickListener {
            ifCheckConnection { if (state.rightScore > 0) state.rightScore-- }
        }
    }

    private fun setupCardButtons() {
        val btnLeftYellow = findViewById<Button>(R.id.btnLeftYellow)
        val btnRightYellow = findViewById<Button>(R.id.btnRightYellow)
        val btnLeftRed = findViewById<Button>(R.id.btnLeftRed)
        val btnRightRed = findViewById<Button>(R.id.btnRightRed)

        btnLeftYellow.setOnClickListener {
            ifCheckConnection {
                state.leftYellow = !state.leftYellow
                btnLeftYellow.alpha = if (state.leftYellow) 1f else 0.4f
            }
        }

        btnRightYellow.setOnClickListener {
            ifCheckConnection {
                state.rightYellow = !state.rightYellow
                btnRightYellow.alpha = if (state.rightYellow) 1f else 0.4f
            }
        }

        btnLeftRed.setOnClickListener {
            ifCheckConnection {
                state.leftRed = !state.leftRed
                btnLeftRed.alpha = if (state.leftRed) 1f else 0.4f
                if (state.leftRed && state.rightScore < 99) state.rightScore++
                if (!state.leftRed && state.rightScore > 0) state.rightScore--
            }
        }

        btnRightRed.setOnClickListener {
            ifCheckConnection {
                state.rightRed = !state.rightRed
                btnRightRed.alpha = if (state.rightRed) 1f else 0.4f
                if (state.rightRed && state.leftScore < 99) state.leftScore++
                if (!state.rightRed && state.leftScore > 0) state.leftScore--
            }
        }
    }

    private fun setupBottomButtons() {
        findViewById<Button>(R.id.btnRest).setOnClickListener {
            ifCheckConnection { startRestMinute() }
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            ifCheckConnection {
                state.leftScore = 0
                state.rightScore = 0
                state.leftYellow = false
                state.rightYellow = false
                state.leftRed = false
                state.rightRed = false

                updateScoreText()
                updateCardButtons()

                stateManager.saveState(state)
                client.sendState(state)
            }
        }

        btnPause.setOnClickListener { pauseRest() }
        btnResume.setOnClickListener { resumeRest() }

        findViewById<Button>(R.id.btnEndRest).setOnClickListener {
            endRestMinute()
        }
    }

    private fun startRestMinute() {
        layoutNormal.visibility = View.GONE
        layoutRest.visibility = View.VISIBLE

        remainingMs = 60_000
        state.restActive = true
        state.restPaused = false

        restTimer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(ms: Long) {
                remainingMs = ms
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
        btnPause.visibility = View.GONE
        btnResume.visibility = View.VISIBLE
        updateAndSend()
    }

    private fun resumeRest() {
        state.restPaused = false
        btnPause.visibility = View.VISIBLE
        btnResume.visibility = View.GONE

        restTimer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(ms: Long) {
                remainingMs = ms
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

    private fun updateScoreText() {
        txtScore.text = String.format("%02d - %02d", state.leftScore, state.rightScore)
    }

    private fun updateCardButtons() {
        findViewById<Button>(R.id.btnLeftYellow).alpha = if (state.leftYellow) 1f else 0.4f
        findViewById<Button>(R.id.btnRightYellow).alpha = if (state.rightYellow) 1f else 0.4f
        findViewById<Button>(R.id.btnLeftRed).alpha = if (state.leftRed) 1f else 0.4f
        findViewById<Button>(R.id.btnRightRed).alpha = if (state.rightRed) 1f else 0.4f
    }

    private fun updateAndSend() {
        updateScoreText()
        stateManager.saveState(state)
        client.sendState(state)
    }

    private fun ifCheckConnection(action: () -> Unit) {
        if (!connected) {
            Toast.makeText(this, "Debe conectarse primero", Toast.LENGTH_SHORT).show()
        } else {
            action()
            updateAndSend()
        }
    }

    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%d:%02d", m, s)
    }

    private fun checkAutoReconnect() {

        if (stateManager.shouldAutoReconnect()) {
            val lastIp = stateManager.getLastIp()
            if (lastIp.isNotEmpty()) {

                Toast.makeText(this, "Reconectando automáticamente...", Toast.LENGTH_SHORT).show()


                btnConnect.postDelayed({
                    btnConnect.isEnabled = false
                    btnConnect.text = "RECONECTANDO..."
                    client.connect(lastIp)
                }, 500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        restTimer?.cancel()
        client.disconnect()
    }
}