package com.astarloa.esgrima

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class TcpServer(private val onStateReceived: (MatchState) -> Unit) {

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var running = true
    private val gson = Gson()

    fun start() {
        Thread {
            try {
                serverSocket = ServerSocket(8888)
                clientSocket = serverSocket!!.accept()

                val reader = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

                while (running) {
                    val line = reader.readLine() ?: break
                    val state = gson.fromJson(line, MatchState::class.java)
                    onStateReceived(state)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stop()
            }
        }.start()
    }

    fun stop() {
        running = false
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
