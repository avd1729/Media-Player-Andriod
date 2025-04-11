package com.example.assignment4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout

class MediaPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoView: VideoView
    private lateinit var audioControls: View
    private lateinit var videoControls: View
    private lateinit var statusText: TextView
    private lateinit var tabLayout: TabLayout

    private var audioUri: Uri? = null
    private var videoUri: Uri? = null

    companion object {
        private const val PICK_AUDIO_REQUEST = 1
        private const val PICK_VIDEO_REQUEST = 2
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        // Initialize views
        videoView = findViewById(R.id.videoView)
        audioControls = findViewById(R.id.audioControls)
        videoControls = findViewById(R.id.videoControls)
        statusText = findViewById(R.id.statusText)
        tabLayout = findViewById(R.id.tabLayout)

        // MediaPlayer for audio
        mediaPlayer = MediaPlayer()

        setupTabs()

        // Audio Buttons
        findViewById<Button>(R.id.btnSelectAudio).setOnClickListener { checkAndRequestPermission("audio") }
        findViewById<Button>(R.id.btnPlayAudio).setOnClickListener { playAudio() }
        findViewById<Button>(R.id.btnPauseAudio).setOnClickListener { pauseAudio() }
        findViewById<Button>(R.id.btnStopAudio).setOnClickListener { stopAudio() }

        // Video Buttons
        findViewById<Button>(R.id.btnSelectVideo).setOnClickListener { checkAndRequestPermission("video") }
        findViewById<Button>(R.id.btnPlayVideo).setOnClickListener { playVideo() }

        showAudioTab()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Audio"))
        tabLayout.addTab(tabLayout.newTab().setText("Video"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showAudioTab()
                    1 -> showVideoTab()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showAudioTab() {
        stopVideo()
        stopAudio()
        audioControls.visibility = View.VISIBLE
        videoControls.visibility = View.GONE
        videoView.visibility = View.GONE
        statusText.text = "Audio Mode"
    }

    private fun showVideoTab() {
        stopAudio()
        stopVideo()
        audioControls.visibility = View.GONE
        videoControls.visibility = View.VISIBLE
        videoView.visibility = View.VISIBLE
        statusText.text = "Video Mode"
    }

    private fun checkAndRequestPermission(type: String) {
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && type == "audio" ->
                Manifest.permission.READ_MEDIA_AUDIO
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && type == "video" ->
                Manifest.permission.READ_MEDIA_VIDEO
            else -> Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            if (type == "audio") pickAudioFile() else pickVideoFile()
        }
    }

    private fun pickAudioFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        startActivityForResult(intent, PICK_AUDIO_REQUEST)
    }

    private fun pickVideoFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data?.data != null) {
            when (requestCode) {
                PICK_AUDIO_REQUEST -> {
                    audioUri = data.data
                    contentResolver.takePersistableUriPermission(
                        audioUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    statusText.text = "Audio Selected"
                    Toast.makeText(this, "Audio file selected", Toast.LENGTH_SHORT).show()
                }
                PICK_VIDEO_REQUEST -> {
                    videoUri = data.data
                    contentResolver.takePersistableUriPermission(
                        videoUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    statusText.text = "Video Selected"
                    Toast.makeText(this, "Video file selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (tabLayout.selectedTabPosition == 0) pickAudioFile()
            else pickVideoFile()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAudio() {
        audioUri?.let { uri ->
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(this, uri)
                mediaPlayer.prepare()
                mediaPlayer.start()
                statusText.text = "Playing Audio"
            } catch (e: Exception) {
                statusText.text = "Error playing audio"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Select an audio file first", Toast.LENGTH_SHORT).show()
    }

    private fun pauseAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            statusText.text = "Audio Paused"
        }
    }

    private fun stopAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            statusText.text = "Audio Stopped"
        }
    }

    private fun playVideo() {
        videoUri?.let { uri ->
            try {
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)
                mediaController.setAnchorView(videoView)

                videoView.setVideoURI(uri)
                videoView.requestFocus()
                videoView.start()

                statusText.text = "Playing Video"
                videoView.setOnCompletionListener {
                    statusText.text = "Video Playback Completed"
                }
            } catch (e: Exception) {
                statusText.text = "Error playing video"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Select a video file first", Toast.LENGTH_SHORT).show()
    }

    private fun stopVideo() {
        videoView.stopPlayback()
        statusText.text = "Video Stopped"
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
        videoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
