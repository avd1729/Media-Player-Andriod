package com.example.assignment4

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import android.content.res.AssetFileDescriptor
import android.view.View
import android.widget.TextView
import com.google.android.material.tabs.TabLayout

class MediaPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoView: VideoView
    private lateinit var audioControls: View
    private lateinit var videoControls: View
    private lateinit var statusText: TextView
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        // Initialize views
        videoView = findViewById(R.id.videoView)
        audioControls = findViewById(R.id.audioControls)
        videoControls = findViewById(R.id.videoControls)
        statusText = findViewById(R.id.statusText)
        tabLayout = findViewById(R.id.tabLayout)

        // Initialize MediaPlayer for audio
        mediaPlayer = MediaPlayer()

        // Set up buttons for audio playback
        val btnPlayAudio = findViewById<Button>(R.id.btnPlayAudio)
        val btnPauseAudio = findViewById<Button>(R.id.btnPauseAudio)
        val btnStopAudio = findViewById<Button>(R.id.btnStopAudio)

        btnPlayAudio.setOnClickListener {
            playAudio()
        }

        btnPauseAudio.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                statusText.text = "Audio Paused"
            }
        }

        btnStopAudio.setOnClickListener {
            stopAudio()
        }

        // Set up buttons for video playback
        val btnPlayVideo = findViewById<Button>(R.id.btnPlayVideo)

        btnPlayVideo.setOnClickListener {
            playVideo()
        }

        // Setup Tab Layout with Audio and Video tabs
        setupTabs()

        // Default to audio mode
        showAudioTab()
    }

    private fun setupTabs() {
        // Add Audio Tab
        tabLayout.addTab(tabLayout.newTab().setText("Audio"))
        // Add Video Tab
        tabLayout.addTab(tabLayout.newTab().setText("Video"))

        // Add tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showAudioTab()
                    1 -> showVideoTab()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Not needed for this implementation
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Not needed for this implementation
            }
        })
    }

    private fun showAudioTab() {
        stopVideo()
        audioControls.visibility = View.VISIBLE
        videoControls.visibility = View.GONE
        videoView.visibility = View.GONE
        statusText.text = "Audio Mode"
    }

    private fun showVideoTab() {
        stopAudio()
        audioControls.visibility = View.GONE
        videoControls.visibility = View.VISIBLE
        videoView.visibility = View.VISIBLE
        statusText.text = "Video Mode"
    }

    private fun playAudio() {
        try {
            stopAudio() // Reset any existing playback

            // For demo purposes, we're using a raw resource
            // You would typically use a real audio file path
            val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.sample_audio)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mediaPlayer.prepare()
            mediaPlayer.start()
            statusText.text = "Playing Audio"

            // Set a completion listener
            mediaPlayer.setOnCompletionListener {
                statusText.text = "Audio Playback Completed"
            }
        } catch (e: Exception) {
            statusText.text = "Error playing audio: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun stopAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        statusText.text = "Audio Stopped"
    }

    private fun playVideo() {
        try {
            // Set media controller
            val mediaController = MediaController(this)
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)

            // Set video path - in a real app, you would use a real video file
            val videoPath = "android.resource://" + packageName + "/" + R.raw.sample_video
            videoView.setVideoURI(Uri.parse(videoPath))

            // Start playing
            videoView.requestFocus()
            videoView.start()
            statusText.text = "Playing Video"

            // Set a completion listener
            videoView.setOnCompletionListener {
                statusText.text = "Video Playback Completed"
            }
        } catch (e: Exception) {
            statusText.text = "Error playing video: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun stopVideo() {
        videoView.stopPlayback()
        statusText.text = "Video Stopped"
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}