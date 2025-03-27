package com.example.assignment4

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class MediaEffectsActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {
    private lateinit var textureView: TextureView
    private lateinit var tabLayout: TabLayout

    private var videoPlayer: MediaPlayer? = null
    private var audioPlayer: MediaPlayer? = null

    private lateinit var bassBoostBar: SeekBar
    private lateinit var trebleBar: SeekBar
    private lateinit var brightnessBar: SeekBar
    private lateinit var contrastBar: SeekBar

    private lateinit var effectStatus: TextView
    private lateinit var videoStatus: TextView
    private lateinit var audioStatus: TextView

    private lateinit var btnSelectVideo: Button
    private lateinit var btnSelectAudio: Button
    private lateinit var btnPlayVideo: Button
    private lateinit var btnPauseVideo: Button
    private lateinit var btnPlayAudio: Button
    private lateinit var btnPauseAudio: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_effects)

        // Initialize views
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = this
        tabLayout = findViewById(R.id.tabLayout)

        // Initialize controls
        bassBoostBar = findViewById(R.id.bassBoostBar)
        trebleBar = findViewById(R.id.trebleBar)
        brightnessBar = findViewById(R.id.brightnessBar)
        contrastBar = findViewById(R.id.contrastBar)

        effectStatus = findViewById(R.id.effectStatus)
        videoStatus = findViewById(R.id.videoStatus)
        audioStatus = findViewById(R.id.audioStatus)

        // Initialize buttons
        btnSelectVideo = findViewById(R.id.btnSelectVideo)
        btnSelectAudio = findViewById(R.id.btnSelectAudio)
        btnPlayVideo = findViewById(R.id.btnPlayVideo)
        btnPauseVideo = findViewById(R.id.btnPauseVideo)
        btnPlayAudio = findViewById(R.id.btnPlayAudio)
        btnPauseAudio = findViewById(R.id.btnPauseAudio)

        // Setup media selection
        setupMediaSelection()

        // Setup player controls
        setupPlayerControls()

        // Setup tabs and effects
        setupTabs()
        setupEffectControls()
    }

    private fun setupMediaSelection() {
        btnSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            startActivityForResult(intent, VIDEO_PICK_REQUEST)
        }

        btnSelectAudio.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, AUDIO_PICK_REQUEST)
        }
    }

    private fun setupPlayerControls() {
        btnPlayVideo.setOnClickListener {
            videoPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    videoStatus.text = "Video: Playing"
                }
            }
        }

        btnPauseVideo.setOnClickListener {
            videoPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    videoStatus.text = "Video: Paused"
                }
            }
        }

        btnPlayAudio.setOnClickListener {
            audioPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    audioStatus.text = "Audio: Playing"
                }
            }
        }

        btnPauseAudio.setOnClickListener {
            audioPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    audioStatus.text = "Audio: Paused"
                }
            }
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Video"))
        tabLayout.addTab(tabLayout.newTab().setText("Audio"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        findViewById<LinearLayout>(R.id.videoSectionLayout).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.audioSectionLayout).visibility = View.GONE
                    }
                    1 -> {
                        findViewById<LinearLayout>(R.id.videoSectionLayout).visibility = View.GONE
                        findViewById<LinearLayout>(R.id.audioSectionLayout).visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupEffectControls() {
        // Video brightness control
        brightnessBar.max = 200
        brightnessBar.progress = 100
        brightnessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyVideoBrightnessEffect(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Video contrast control
        contrastBar.max = 200
        contrastBar.progress = 100
        contrastBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyVideoContrastEffect(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                data?.data?.let { uri ->
                    when (requestCode) {
                        VIDEO_PICK_REQUEST -> initializeVideoPlayer(uri)
                        AUDIO_PICK_REQUEST -> initializeAudioPlayer(uri)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error selecting file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeVideoPlayer(uri: Uri) {
        videoPlayer?.release()
        videoPlayer = MediaPlayer().apply {
            setDataSource(this@MediaEffectsActivity, uri)
            setOnPreparedListener {
                videoStatus.text = "Video: Ready"
                if (textureView.isAvailable) {
                    setSurface(Surface(textureView.surfaceTexture))
                }
            }
            prepareAsync()
        }
    }

    private fun initializeAudioPlayer(uri: Uri) {
        audioPlayer?.release()
        audioPlayer = MediaPlayer().apply {
            setDataSource(this@MediaEffectsActivity, uri)
            setOnPreparedListener {
                audioStatus.text = "Audio: Ready"
            }
            prepareAsync()
        }
    }

    private fun applyVideoBrightnessEffect(progress: Int) {
        val brightness = (progress - 100) / 100f
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness * 255,
                0f, 1f, 0f, 0f, brightness * 255,
                0f, 0f, 1f, 0f, brightness * 255,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        textureView.setLayerType(TextureView.LAYER_TYPE_HARDWARE, paint)
    }

    private fun applyVideoContrastEffect(progress: Int) {
        val contrast = progress / 100f
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, (1 - contrast) * 128f,
                0f, contrast, 0f, 0f, (1 - contrast) * 128f,
                0f, 0f, contrast, 0f, (1 - contrast) * 128f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        textureView.setLayerType(TextureView.LAYER_TYPE_HARDWARE, paint)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        videoPlayer?.setSurface(Surface(surface))
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        videoPlayer?.release()
        videoPlayer = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer?.release()
        audioPlayer?.release()
    }

    companion object {
        private const val VIDEO_PICK_REQUEST = 1
        private const val AUDIO_PICK_REQUEST = 2
    }
}