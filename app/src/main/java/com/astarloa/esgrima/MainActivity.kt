package com.astarloa.esgrima

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSable = findViewById<Button>(R.id.btnSable)
        val btnFloreteEspada = findViewById<Button>(R.id.btnFloreteEspada)

        val layoutMode = findViewById<View>(R.id.layoutMode)
        val layoutModeFloreteEspada = findViewById<View>(R.id.layoutModeFloreteEspada)
        val layoutWeapons = findViewById<View>(R.id.layoutWeapons)


        btnSable.setOnClickListener {
            layoutWeapons.visibility = View.GONE
            layoutMode.visibility = View.VISIBLE
        }


        btnFloreteEspada.setOnClickListener {
            layoutWeapons.visibility = View.GONE
            layoutModeFloreteEspada.visibility = View.VISIBLE
        }


        findViewById<Button>(R.id.btnMarcador).setOnClickListener {
            startActivity(Intent(this, MarcadorActivity::class.java))
        }

        findViewById<Button>(R.id.btnControl).setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
        }

        findViewById<Button>(R.id.btnAtras).setOnClickListener {
            layoutMode.visibility = View.GONE
            layoutWeapons.visibility = View.VISIBLE
        }


        findViewById<Button>(R.id.btnMarcadorFloreteEspada).setOnClickListener {
            startActivity(Intent(this, MarcadorFloreteActivity::class.java))
        }

        findViewById<Button>(R.id.btnControlFloreteEspada).setOnClickListener {
            startActivity(Intent(this, ControlFloreteActivity::class.java))
        }

        findViewById<Button>(R.id.btnAtrasFloreteEspada).setOnClickListener {
            layoutModeFloreteEspada.visibility = View.GONE
            layoutWeapons.visibility = View.VISIBLE
        }

        val btnAbout = findViewById<ImageButton>(R.id.btnAbout)
        btnAbout.setOnClickListener {
            showAboutDialog()
        }

        val btnTutorial = findViewById<ImageButton>(R.id.btnTutorial)
        btnTutorial.setOnClickListener {
            showTutorialDialog()
        }
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "Desconocida"
        }

        val message = """
            <b>Versión Actual:</b> $versionName<br>
            <br>
            <b>VERSIÓN 2.0 </b><br>
            <br>
            <b>Novedades v2.0:</b><br>
            - Nuevo botón de <b>Tutorial</b> con guía completa de conexión<br>
            - Tutorial explica 2 métodos: WiFi común y Hotspot (sin WiFi)<br>
            - Interfaz mejorada y más profesional<br>
            <br>
            <b>Sistema de Tarjetas P (v1.10-1.11.1):</b><br>
            - Tarjetas "P" (Pasividad/Penalización) con letra P diferenciada<br>
            - Sistema de configuración para activar/desactivar funciones<br>
            - Tarjeta P automática por inactividad (1 minuto configurable)<br>
            - Botones P se ocultan automáticamente al desactivar función<br>
            - Limpieza automática de tarjetas P al desactivar sistema<br>
            - Control de sonidos del marcador<br>
            <br>
            <b>Características principales:</b><br>
            - Detección automática de 1 minuto sin interrupciones<br>
            - Alertas auditivas en el marcador de Florete/Espada<br>
            - Modo "Pantalla Encendida" en los Controles<br>
            - Soporte para Sable, Florete y Espada<br>
            <br>
            <b>Creado por:</b><br>
            Joaquin Pizarro (Sablista)<br>
            <br>
            <a href="https://github.com/juakamayo/astarloascoreboard">Ver en GitHub</a>
        """.trimIndent()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Acerca de Astarloa Scoreboard")
        builder.setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY))
        builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.show()

        val msgView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
        msgView?.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun showTutorialDialog() {
        val message = """
            <b>Cómo Usar Astarloa Scoreboard</b><br>
            <br>
            Existen <b>2 formas</b> de conectar el Control con el Marcador:<br>
            <br>
            <b>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</b><br>
            <br>
            <b>MÉTODO 1: WiFi Común</b><br>
            <br>
            <b>1.</b> Conecta <b>ambos dispositivos</b> a la <b>misma red WiFi</b><br>
            <b>2.</b> En el dispositivo <b>Marcador</b>:<br>
            &nbsp;&nbsp;&nbsp;• Abre la app y selecciona modo "Marcador"<br>
            &nbsp;&nbsp;&nbsp;• Aparecerá la <b>dirección IP</b> en pantalla (ej: 192.168.1.50)<br>
            <b>3.</b> En el dispositivo <b>Control</b>:<br>
            &nbsp;&nbsp;&nbsp;• Abre la app y selecciona modo "Control"<br>
            &nbsp;&nbsp;&nbsp;• Ingresa la <b>IP del Marcador</b><br>
            &nbsp;&nbsp;&nbsp;• Presiona <b>"CONECTAR"</b><br>
            <b>4.</b> ¡Listo! Ya puedes controlar el combate<br>
            <br>
            <b>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</b><br>
            <br>
            <b>MÉTODO 2: Hotspot (Sin WiFi)</b><br>
            <br>
            <b>1.</b> En el dispositivo <b>Control</b>:<br>
            &nbsp;&nbsp;&nbsp;• Activa el <b>"Punto de acceso WiFi"</b> o <b>"Compartir Internet"</b><br>
            &nbsp;&nbsp;&nbsp;• Anota el <b>nombre de la red</b> y la <b>contraseña</b><br>
            <b>2.</b> En el dispositivo <b>Marcador</b>:<br>
            &nbsp;&nbsp;&nbsp;• Conéctate a la <b>red WiFi del Control</b><br>
            &nbsp;&nbsp;&nbsp;• Abre la app y selecciona modo "Marcador"<br>
            &nbsp;&nbsp;&nbsp;• Aparecerá la <b>dirección IP</b> en pantalla<br>
            <b>3.</b> En el dispositivo <b>Control</b>:<br>
            &nbsp;&nbsp;&nbsp;• Abre la app y selecciona modo "Control"<br>
            &nbsp;&nbsp;&nbsp;• Ingresa la <b>IP del Marcador</b><br>
            &nbsp;&nbsp;&nbsp;• Presiona <b>"CONECTAR"</b><br>
            <b>4.</b> ¡Listo! Funciona sin WiFi externo<br>
            <br>
            <b>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</b><br>
            <br>
            <b>Consejos:</b><br>
            • La IP siempre se muestra en el <b>Marcador</b><br>
            • Verifica que ambos dispositivos estén en la <b>misma red</b><br>
            • Si no conecta, revisa el <b>firewall</b> o permisos de red<br>
            • El Método 2 es ideal para entrenamientos sin WiFi disponible<br>
            <br>
            <b>¿Problemas?</b> Revisa la configuración de red de tus dispositivos.
        """.trimIndent()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Tutorial de Conexión")
        builder.setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY))
        builder.setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }
}