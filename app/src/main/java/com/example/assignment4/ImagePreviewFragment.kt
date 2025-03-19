package com.example.assignment4

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File

class ImagePreviewFragment : Fragment() {

    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imagePath = it.getString(ARG_IMAGE_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.imagePreview)
        val noImageText = view.findViewById<TextView>(R.id.noImageText)

        if (!imagePath.isNullOrEmpty() && File(imagePath!!).exists()) {
            imageView.setImageURI(Uri.fromFile(File(imagePath!!)))
            imageView.visibility = View.VISIBLE
            noImageText.visibility = View.GONE
        } else {
            imageView.visibility = View.GONE
            noImageText.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val ARG_IMAGE_PATH = "image_path"

        @JvmStatic
        fun newInstance(imagePath: String) =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_PATH, imagePath)
                }
            }
    }
}