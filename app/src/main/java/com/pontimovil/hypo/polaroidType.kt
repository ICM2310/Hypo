package com.pontimovil.hypo

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.pontimovil.hypo.databinding.ActivityPolaroidTypeBinding

class polaroidType(val imagePaths: MutableList<String>, var images: MutableList<Bitmap>) : RecyclerView.Adapter<polaroidType.ViewHolder>() {

    private lateinit var binding: ActivityPolaroidTypeBinding
    private lateinit var imagen: ImageView
    val imageViews = mutableListOf<ImageView>()
    var isStyleApplied = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): polaroidType.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        binding = ActivityPolaroidTypeBinding.inflate(layoutInflater)
        imagen = binding.imagen
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: polaroidType.ViewHolder, position: Int) {

        val imageView = holder.itemView.findViewById<ImageView>(binding.imagen.id)

        imageView.setImageBitmap(images[position])
        /*


        imageView.setOnClickListener {
            var context = imageView.context
            while (context is ContextWrapper) {
                if (context is FragmentActivity) {
                    val dialog = ImageOverlayDialogFragment.newInstance(images[position])
                    dialog.show(context.supportFragmentManager, "ImageOverlay")
                    return@setOnClickListener
                } else {
                    context = context.baseContext
                }
            }
        }

         */

        if (isStyleApplied) {
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)

            val scale = 1f
            val translate = 0f
            colorMatrix.set(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            val filter = ColorMatrixColorFilter(colorMatrix)
            imageView.colorFilter = filter
        } else {
            imageView.clearColorFilter()
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun updateImage(bitmap: Bitmap, position: Int) {
        Log.d("Hypo", "updateImage: $position")
        images[position] = bitmap
        notifyItemChanged(position)
    }
}

