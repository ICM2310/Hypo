package com.pontimovil.hypo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

class ImageOverlayDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_IMAGE = "arg_image"

        fun newInstance(image: Bitmap): ImageOverlayDialogFragment {
            val fragment = ImageOverlayDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE, image)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var image: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        image = arguments?.getParcelable(ARG_IMAGE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_overlay_dialog, container, false)
        val imageView = view.findViewById<ImageView>(R.id.fullscreen_image)
        imageView.setImageBitmap(image)
        return view
    }
}
