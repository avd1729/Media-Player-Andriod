package com.example.assignment4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.io.File

class VideoPreviewActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Get the image and video paths from the intent
        val imagePath = intent.getStringExtra("IMAGE_PATH") ?: ""
        val videoPath = intent.getStringExtra("VIDEO_PATH") ?: ""

        // Set up the ViewPager with the fragments
        val pagerAdapter = MediaPagerAdapter(this, imagePath, videoPath)
        viewPager.adapter = pagerAdapter

        // Connect the TabLayout with the ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Image"
                1 -> "Video"
                else -> ""
            }
        }.attach()

        // Set the initial tab based on what was captured
        if (imagePath.isNotEmpty() && File(imagePath).exists()) {
            viewPager.currentItem = 0
        } else if (videoPath.isNotEmpty() && File(videoPath).exists()) {
            viewPager.currentItem = 1
        }
    }

    // PagerAdapter to handle the image and video fragments
    private class MediaPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val imagePath: String,
        private val videoPath: String
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ImagePreviewFragment.newInstance(imagePath)
                1 -> VideoPreviewFragment.newInstance(videoPath)
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }
    }
}