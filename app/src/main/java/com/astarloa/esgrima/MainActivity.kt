package com.astarloa.esgrima

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
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

        // SABLE
        btnSable.setOnClickListener {
            layoutWeapons.visibility = View.GONE
            layoutMode.visibility = View.VISIBLE
        }

        // FLORETE/ESPADA
        btnFloreteEspada.setOnClickListener {
            layoutWeapons.visibility = View.GONE
            layoutModeFloreteEspada.visibility = View.VISIBLE
        }

        // Botones de SABLE
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

        // Botones de FLORETE/ESPADA
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
    }
}
