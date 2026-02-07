package com.astarloa.esgrima

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSable = findViewById<Button>(R.id.btnSable)
        val btnEspada = findViewById<Button>(R.id.btnEspada)
        val btnFlorete = findViewById<Button>(R.id.btnFlorete)

        val layoutMode = findViewById<View>(R.id.layoutMode)
        val layoutWeapons = findViewById<View>(R.id.layoutWeapons)

        btnSable.setOnClickListener {
            layoutWeapons.visibility = View.GONE
            layoutMode.visibility = View.VISIBLE
        }

        btnEspada.setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
        }

        btnFlorete.setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
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
    }
}
