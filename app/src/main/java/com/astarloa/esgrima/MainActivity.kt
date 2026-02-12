package com.astarloa.esgrima

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton // Importante importar esto
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
            <b>Última Novedad:</b><br>
            - Efecto Flash al anotar puntos<br>
            - Corrección de crash en modo Sable<br>
            - Boton "about it"<br>
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
}