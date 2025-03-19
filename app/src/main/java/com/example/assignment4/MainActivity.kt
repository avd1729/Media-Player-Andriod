package com.example.assignment4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // ✅ Ensure this is before accessing views

        // Initialize buttons correctly
        val btnMediaPlayer = findViewById<Button>(R.id.btnMediaPlayer)
        val btnMediaEffects = findViewById<Button>(R.id.btnMediaEffects)
        val btnRecordMedia = findViewById<Button>(R.id.btnRecordMedia)

        // ✅ Remove references to non-existent buttons OR add them to XML
//       val btnPreviewVideo = findViewById<Button>(R.id.btnPreviewVideo)
         val btnCameraControl = findViewById<Button>(R.id.btnCameraControl)

        // Set click listeners to navigate to respective activities
        btnMediaPlayer.setOnClickListener {
            val intent = Intent(this, MediaPlayerActivity::class.java)
            startActivity(intent)
        }

        btnMediaEffects.setOnClickListener {
            val intent = Intent(this, MediaEffectsActivity::class.java)
            startActivity(intent)
        }

        btnRecordMedia.setOnClickListener {
            val intent = Intent(this, RecordMediaActivity::class.java)
            startActivity(intent)
        }


        btnCameraControl.setOnClickListener {
            val intent = Intent(this, CameraControlActivity::class.java)
            startActivity(intent)
        }

    }
}
