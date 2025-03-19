package com.example.assignment4

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import java.io.File

class VideoPreviewFragment : Fragment() {

    private var videoPath: String? = null
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoPath = it.getString(ARG_VIDEO_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView = view.findViewById(R.id.videoPreview)
        val noVideoText = view.findViewById<TextView>(R.id.noVideoText)

        if (!videoPath.isNullOrEmpty() && File(videoPath!!).exists()) {
            // Set up the video view
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(videoView)

            videoView.setMediaController(mediaController)
            videoView.setVideoURI(Uri.fromFile(File(videoPath!!)))
            videoView.requestFocus()
            videoView.visibility = View.VISIBLE
            noVideoText.visibility = View.GONE

            // Start playing the video
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.setOnVideoSizeChangedListener { _, _, _ ->
                    // Adjust the size of the video
                    mediaController.setAnchorView(videoView)
                }
            }

            videoView.start()
        } else {
            videoView.visibility = View.GONE
            noVideoText.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.pause()
        }
    }

    companion object {
        private const val ARG_VIDEO_PATH = "video_path"

        @JvmStatic
        fun newInstance(videoPath: String) =
            VideoPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIDEO_PATH, videoPath)
                }
            }
    }
}