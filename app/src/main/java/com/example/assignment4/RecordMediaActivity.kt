package com.example.assignment4

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.assignment4.R.*
import com.example.assignment4.R.id.*
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecordMediaActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var btnCapture: Button
    private lateinit var btnPreview: Button

    private var currentPhotoPath: String = ""
    private var currentVideoPath: String = ""
    private var currentTab: Int = 0 // 0 for image, 1 for video

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_record_media)

        statusText = findViewById(id.statusText)
        tabLayout = findViewById(id.tabLayout)
        btnCapture = findViewById(id.btnCapture)
        btnPreview = findViewById(id.btnPreview)

        if (!checkPermissions()) {
            requestPermissions()
        }

        // Set up tabs
        tabLayout.addTab(tabLayout.newTab().setText("Camera"))
        tabLayout.addTab(tabLayout.newTab().setText("Video"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                updateButtonText()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Set up button listeners
        btnCapture.setOnClickListener {
            if (currentTab == 0) {
                capturePhoto()
            } else {
                captureVideo()
            }
        }

        btnPreview.setOnClickListener {
            openPreviewActivity()
        }

        // Initialize button text
        updateButtonText()
    }

    private fun updateButtonText() {
        btnCapture.text = if (currentTab == 0) "Capture Photo" else "Record Video"
        btnPreview.isEnabled = (currentTab == 0 && currentPhotoPath.isNotEmpty()) ||
                (currentTab == 1 && currentVideoPath.isNotEmpty())
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                audioPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            101
        )
    }

    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            statusText.text = "Photo captured successfully"
            galleryAddPic()
            updateButtonText()
        } else {
            statusText.text = "Image capture failed"
        }
    }

    private val captureVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            statusText.text = "Video recorded successfully"
            updateButtonText()
        } else {
            statusText.text = "Video recording failed"
        }
    }

    private fun capturePhoto() {
        val photoFile: File? = try { createImageFile() } catch (e: IOException) { null }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.assignment4.fileprovider", it)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            captureImageLauncher.launch(takePictureIntent)
        }
    }

    private fun captureVideo() {
        val videoFile: File? = try { createVideoFile() } catch (e: IOException) { null }
        videoFile?.also {
            val videoURI: Uri = FileProvider.getUriForFile(this, "com.example.assignment4.fileprovider", it)
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
                putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            }
            captureVideoLauncher.launch(takeVideoIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir).apply {
            currentVideoPath = absolutePath
        }
    }

    private fun openPreviewActivity() {
        val intent = Intent(this, VideoPreviewActivity::class.java).apply {
            putExtra("IMAGE_PATH", currentPhotoPath)
            putExtra("VIDEO_PATH", currentVideoPath)
        }
        startActivity(intent)
    }

    private fun galleryAddPic() {
        android.media.MediaScannerConnection.scanFile(
            this, arrayOf(currentPhotoPath), null
        ) { _, uri -> runOnUiThread { statusText.text = "Image saved to gallery: $uri" } }
    }
}