package com.astarloa.esgrima

import com.google.gson.Gson
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class TcpClient {

    private val gson = Gson()
    private val queue = LinkedBlockingQueue<String>()
    private lateinit var writer: PrintWriter

    fun connect(ip: String) {
        thread {
            val socket = Socket(ip, 8888)
            writer = PrintWriter(socket.getOutputStream(), true)

            thread {
                while (true) {
                    val msg = queue.take()
                    writer.println(msg)
                }
            }
        }
    }

    fun sendState(state: MatchState) {
        queue.offer(gson.toJson(state))
    }
}
