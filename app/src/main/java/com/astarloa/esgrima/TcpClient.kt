package com.astarloa.esgrima

import android.util.Log
import com.google.gson.Gson
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class TcpClient {

    private val gson = Gson()
    private val queue = LinkedBlockingQueue<String>()
    private var writer: PrintWriter? = null
    private var socket: Socket? = null
    private var running = false

    var onConnectionResult: ((Boolean, String) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    fun connect(ip: String) {
        // Cerrar conexión anterior si existe
        disconnect()

        thread {
            try {
                // Validar IP
                if (ip.isBlank()) {
                    onConnectionResult?.invoke(false, "La IP no puede estar vacía")
                    return@thread
                }

                // Intentar conectar con timeout
                socket = Socket()
                socket?.soTimeout = 10000  // Timeout de lectura de 10 segundos
                socket?.tcpNoDelay = true   // Enviar datos inmediatamente
                socket?.keepAlive = true    // Mantener conexión viva
                socket?.connect(java.net.InetSocketAddress(ip, 8888), 5000)

                writer = PrintWriter(socket?.getOutputStream(), true)
                running = true

                onConnectionResult?.invoke(true, "Conectado exitosamente")

                // Thread para enviar mensajes
                thread {
                    try {
                        while (running && socket?.isConnected == true) {
                            val msg = queue.take()
                            writer?.println(msg)
                            writer?.flush()  // Forzar envío inmediato
                        }
                    } catch (e: Exception) {
                        Log.e("TcpClient", "Error enviando mensaje: ${e.message}")
                        disconnect()
                    }
                }

            } catch (e: java.net.SocketTimeoutException) {
                onConnectionResult?.invoke(false, "Tiempo de espera agotado. Verifica la IP.")
            } catch (e: java.net.ConnectException) {
                onConnectionResult?.invoke(false, "No se pudo conectar. Verifica que el marcador esté activo.")
            } catch (e: java.net.UnknownHostException) {
                onConnectionResult?.invoke(false, "IP inválida")
            } catch (e: Exception) {
                onConnectionResult?.invoke(false, "Error: ${e.message}")
                Log.e("TcpClient", "Error de conexión", e)
            }
        }
    }

    fun sendState(state: MatchState) {
        if (running && writer != null) {
            queue.offer(gson.toJson(state))
        }
    }

    fun disconnect() {
        running = false
        try {
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e("TcpClient", "Error al desconectar", e)
        }
        onDisconnected?.invoke()
    }

    fun isConnected(): Boolean = running && socket?.isConnected == true
}