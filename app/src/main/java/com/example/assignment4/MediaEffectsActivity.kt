package com.example.assignment4

import android.annotation.SuppressLint
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

    // Separate media players for video and audio
    private var videoPlayer: MediaPlayer? = null
    private var audioPlayer: MediaPlayer? = null

    private var videoEqualizer: Equalizer? = null
    private var audioEqualizer: Equalizer? = null
    private var visualizer: Visualizer? = null

    // Audio controls
    private lateinit var bassBoostBar: SeekBar
    private lateinit var trebleBar: SeekBar
    private lateinit var audioControlsLayout: LinearLayout

    // Video controls
    private lateinit var brightnessBar: SeekBar
    private lateinit var contrastBar: SeekBar
    private lateinit var videoControlsLayout: LinearLayout
    private lateinit var videoSectionLayout: LinearLayout
    private lateinit var audioSectionLayout: LinearLayout

    private lateinit var effectStatus: TextView
    private lateinit var videoStatus: TextView
    private lateinit var audioStatus: TextView
    private lateinit var audioPlaceholder: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_effects)

        // Initialize views
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = this
        tabLayout = findViewById(R.id.tabLayout)
        audioPlaceholder = findViewById(R.id.audioPlaceholder)

        // Audio controls
        bassBoostBar = findViewById(R.id.bassBoostBar)
        trebleBar = findViewById(R.id.trebleBar)

        // Video controls
        brightnessBar = findViewById(R.id.brightnessBar)
        contrastBar = findViewById(R.id.contrastBar)

        effectStatus = findViewById(R.id.effectStatus)
        videoStatus = findViewById(R.id.videoStatus)
        audioStatus = findViewById(R.id.audioStatus)

        // Get layout containers
        videoSectionLayout = findViewById(R.id.videoSectionLayout)
        audioSectionLayout = findViewById(R.id.audioSectionLayout)
        videoControlsLayout = findViewById(R.id.videoControlsLayout)
        audioControlsLayout = findViewById(R.id.audioControlsLayout)

        // Media control buttons
        val btnPlayVideo = findViewById<Button>(R.id.btnPlayVideo)
        val btnPauseVideo = findViewById<Button>(R.id.btnPauseVideo)
        val btnPlayAudio = findViewById<Button>(R.id.btnPlayAudio)
        val btnPauseAudio = findViewById<Button>(R.id.btnPauseAudio)
        val btnResetVideoEffects = findViewById<Button>(R.id.btnResetVideoEffects)
        val btnResetAudioEffects = findViewById<Button>(R.id.btnResetAudioEffects)

        // Setup tabs
        setupTabs()

        // Initialize media players
        setupAudioPlayer()
        // Video player will be initialized when surface is available

        // Video player controls
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

        // Audio player controls
        btnPlayAudio.setOnClickListener {
            audioPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    visualizer?.enabled = true
                    audioStatus.text = "Audio: Playing"
                }
            }
        }

        btnPauseAudio.setOnClickListener {
            audioPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    visualizer?.enabled = false
                    audioStatus.text = "Audio: Paused"
                }
            }
        }

        btnResetVideoEffects.setOnClickListener {
            resetVideoEffects()
            effectStatus.text = "Video effects reset"
        }

        btnResetAudioEffects.setOnClickListener {
            resetAudioEffects()
            effectStatus.text = "Audio effects reset"
        }

        // Setup video effect controls
        setupVideoEffectControls()

        // Show video section by default and select the first tab
        tabLayout.selectTab(tabLayout.getTabAt(0))
        showVideoSection()
    }

    private fun setupTabs() {
        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Video"))
        tabLayout.addTab(tabLayout.newTab().setText("Audio"))

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showVideoSection()
                    1 -> showAudioSection()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showVideoSection() {
        videoSectionLayout.visibility = View.VISIBLE
        audioSectionLayout.visibility = View.GONE
        effectStatus.text = "Video effects ready"

        // Pause audio if it's playing
        audioPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                audioStatus.text = "Audio: Paused"
                visualizer?.enabled = false
            }
        }
    }

    private fun showAudioSection() {
        videoSectionLayout.visibility = View.GONE
        audioSectionLayout.visibility = View.VISIBLE
        effectStatus.text = "Audio effects ready"

        // Pause video if it's playing
        videoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                videoStatus.text = "Video: Paused"
            }
        }
    }

    private fun setupAudioPlayer() {
        try {
            audioPlayer = MediaPlayer().apply {
                val audioPath = "android.resource://${packageName}/${R.raw.sample_audio}"
                setDataSource(this@MediaEffectsActivity, Uri.parse(audioPath))
                setOnPreparedListener { mp ->
                    setupAudioEqualizer()
                    setupVisualizer()
                    audioStatus.text = "Audio: Ready"
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Audio Error: ${e.message}", Toast.LENGTH_SHORT).show()
            audioStatus.text = "Audio: Error"
        }
    }

    private fun setupVideoEffectControls() {
        // Setup brightness control
        brightnessBar.max = 200
        brightnessBar.progress = 100
        brightnessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyVideoBrightnessEffect(progress)
                effectStatus.text = "Brightness: $progress/200"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup contrast control
        contrastBar.max = 200
        contrastBar.progress = 100
        contrastBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyVideoContrastEffect(progress)
                effectStatus.text = "Contrast: $progress/200"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        try {
            videoPlayer = MediaPlayer().apply {
                val videoPath = "android.resource://${packageName}/${R.raw.sample_video}"
                setDataSource(this@MediaEffectsActivity, Uri.parse(videoPath))
                setSurface(Surface(surface))
                setOnPreparedListener { mp ->
                    // Video is ready to play
                    videoStatus.text = "Video: Ready"
                    mp.isLooping = true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this@MediaEffectsActivity, "Video Error: ${e.message}", Toast.LENGTH_SHORT).show()
            videoStatus.text = "Video: Error loading"
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        videoPlayer?.release()
        videoPlayer = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun setupAudioEqualizer() {
        try {
            audioPlayer?.audioSessionId?.let { audioSessionId ->
                audioEqualizer = Equalizer(0, audioSessionId)
                audioEqualizer?.enabled = true

                // Setup bass boost control
                bassBoostBar.max = 1000
                bassBoostBar.progress = 0
                bassBoostBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        audioEqualizer?.let { eq ->
                            val minLevel = eq.bandLevelRange[0]
                            val maxLevel = eq.bandLevelRange[1]
                            val bandLevel = minLevel + (progress * (maxLevel - minLevel) / 1000)
                            // Apply to lowest frequency band (bass)
                            eq.setBandLevel(0.toShort(), bandLevel.toShort())
                            effectStatus.text = "Bass: $progress/1000"
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                // Setup treble boost control
                trebleBar.max = 1000
                trebleBar.progress = 0
                trebleBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        audioEqualizer?.let { eq ->
                            val minLevel = eq.bandLevelRange[0]
                            val maxLevel = eq.bandLevelRange[1]
                            val bandLevel = minLevel + (progress * (maxLevel - minLevel) / 1000)
                            // Apply to highest frequency band (treble) - assuming last band is treble
                            val lastBand = (eq.numberOfBands - 1).toShort()
                            eq.setBandLevel(lastBand, bandLevel.toShort())
                            effectStatus.text = "Treble: $progress/1000"
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Equalizer error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVisualizer() {
        try {
            audioPlayer?.audioSessionId?.let { audioSessionId ->
                visualizer = Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1] // Use maximum capture size
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer, waveform: ByteArray, samplingRate: Int
                        ) {
                            runOnUiThread {
                                // Only update if audio is playing and in audio tab
                                if (audioPlayer?.isPlaying == true &&
                                    audioSectionLayout.visibility == View.VISIBLE &&
                                    !effectStatus.text.toString().contains(":")) {
                                    effectStatus.text = "Audio Signal: ${calculateAmplitude(waveform)}"
                                }
                            }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer, fft: ByteArray, samplingRate: Int
                        ) {}
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                    enabled = false // Will be enabled when audio is playing
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Visualizer error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAmplitude(waveform: ByteArray): Int {
        return waveform.sumOf { Math.abs(it.toInt()) } / waveform.size
    }

    private fun applyVideoBrightnessEffect(progress: Int) {
        val brightness = (progress - 100) / 100f  // -1 to 1 range
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness * 255,  // Red channel
                0f, 1f, 0f, 0f, brightness * 255,  // Green channel
                0f, 0f, 1f, 0f, brightness * 255,  // Blue channel
                0f, 0f, 0f, 1f, 0f                // Alpha channel
            ))
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        textureView.setLayerType(TextureView.LAYER_TYPE_HARDWARE, paint)
    }

    private fun applyVideoContrastEffect(progress: Int) {
        val contrast = progress / 100f  // 0 to 2 range
        val colorMatrix = ColorMatrix().apply {
            // Apply contrast: scale RGB around mid-gray (128)
            val scale = contrast
            val translation = (1 - scale) * 128f

            set(floatArrayOf(
                scale, 0f, 0f, 0f, translation,    // Red
                0f, scale, 0f, 0f, translation,    // Green
                0f, 0f, scale, 0f, translation,    // Blue
                0f, 0f, 0f, 1f, 0f                // Alpha stays the same
            ))
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        textureView.setLayerType(TextureView.LAYER_TYPE_HARDWARE, paint)
    }

    private fun resetVideoEffects() {
        // Reset video effects
        brightnessBar.progress = 100
        contrastBar.progress = 100

        // Apply reset to texture view
        val resetMatrix = ColorMatrix()
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(resetMatrix)
        }
        textureView.setLayerType(TextureView.LAYER_TYPE_HARDWARE, paint)
    }

    private fun resetAudioEffects() {
        // Reset audio effects
        bassBoostBar.progress = 0
        trebleBar.progress = 0

        audioEqualizer?.let { eq ->
            for (i in 0 until eq.numberOfBands) {
                eq.setBandLevel(i.toShort(), 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Re-initialize players if needed
        if (videoPlayer == null && textureView.isAvailable) {
            val surface = textureView.surfaceTexture
            if (surface != null) {
                onSurfaceTextureAvailable(surface, textureView.width, textureView.height)
            }
        }

        if (audioPlayer == null) {
            setupAudioPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    override fun onPause() {
        super.onPause()

        // Pause players when activity is paused
        videoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                videoStatus.text = "Video: Paused"
            }
        }

        audioPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                audioStatus.text = "Audio: Paused"
                visualizer?.enabled = false
            }
        }
    }

    private fun releaseResources() {
        videoPlayer?.release()
        videoPlayer = null

        audioPlayer?.release()
        audioPlayer = null

        visualizer?.release()
        visualizer = null

        audioEqualizer?.release()
        audioEqualizer = null

        videoEqualizer?.release()
        videoEqualizer = null
    }
}