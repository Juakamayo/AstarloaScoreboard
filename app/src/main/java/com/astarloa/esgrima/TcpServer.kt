package com.astarloa.esgrima

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class TcpServer(private val onStateReceived: (MatchState) -> Unit) {

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var running = true
    private val gson = Gson()

    var onClientConnected: (() -> Unit)? = null
    var onClientDisconnected: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun start() {
        Thread {
            try {
                serverSocket = ServerSocket(8888)
                serverSocket?.reuseAddress = true  // ← IMPORTANTE: Permite reutilizar el puerto
                Log.d("TcpServer", "Servidor iniciado en puerto 8888")

                while (running) {
                    try {
                        // Cerrar conexión anterior si existe
                        try {
                            clientSocket?.close()
                        } catch (e: Exception) {
                            // Ignorar errores al cerrar (sino crashea :((( )
                        }

                        // Esperar nueva conexión
                        clientSocket = serverSocket?.accept()
                        clientSocket?.soTimeout = 30000  // Timeout de 30 segundos (ms)

                        Log.d("TcpServer", "Cliente conectado: ${clientSocket?.inetAddress?.hostAddress}")
                        onClientConnected?.invoke()

                        val reader = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

                        while (running && clientSocket?.isConnected == true) {
                            try {
                                val line = reader.readLine()

                                if (line == null) {
                                    Log.d("TcpServer", "Cliente desconectado")
                                    onClientDisconnected?.invoke()
                                    break
                                }

                                try {
                                    val state = gson.fromJson(line, MatchState::class.java)
                                    onStateReceived(state)
                                } catch (e: Exception) {
                                    Log.e("TcpServer", "Error parseando JSON: ${e.message}")
                                }
                            } catch (e: java.net.SocketTimeoutException) {
                                // Timeout normal, continuar esperando
                                continue
                            } catch (e: Exception) {
                                Log.e("TcpServer", "Error leyendo datos: ${e.message}")
                                break
                            }
                        }

                    } catch (e: SocketException) {
                        if (running) {
                            Log.e("TcpServer", "Error de socket: ${e.message}")
                            onClientDisconnected?.invoke()
                        }
                    } catch (e: Exception) {
                        if (running) {
                            Log.e("TcpServer", "Error en servidor: ${e.message}", e)
                            onError?.invoke("Error: ${e.message}")
                        }
                    } finally {
                        try {
                            clientSocket?.close()
                        } catch (e: Exception) {
                            // Ignorar errores al cerrar
                        }
                    }
                }

            } catch (e: Exception) {
                if (running) {
                    Log.e("TcpServer", "Error fatal en servidor", e)
                    onError?.invoke("Error iniciando servidor: ${e.message}")
                }
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
            Log.d("TcpServer", "Servidor detenido")
        } catch (e: Exception) {
            Log.e("TcpServer", "Error al detener servidor", e)
        }
    }
}