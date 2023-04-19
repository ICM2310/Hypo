package com.pontimovil.hypo

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pontimovil.hypo.databinding.ActivityPolaroidTypeBinding

class polaroidType(val images: List<Bitmap>) : RecyclerView.Adapter<polaroidType.ViewHolder>() {


    private lateinit var binding: ActivityPolaroidTypeBinding
    private lateinit var imagen: ImageView
    val imageViews = mutableListOf<ImageView>()
    var isStyleApplied = false


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): polaroidType.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        binding = ActivityPolaroidTypeBinding.inflate(layoutInflater)
        imagen = binding.imagen
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: polaroidType.ViewHolder, position: Int) {
        binding.imagen.setImageBitmap(images[position])
        val imageView = holder.itemView.findViewById<ImageView>(binding.imagen.id)

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

        imageViews.add(imageView)

    }


    override fun getItemCount(): Int {
        return images.size
    }

    fun applyStyleToImages() {
        isStyleApplied = !isStyleApplied
        notifyDataSetChanged()
    }



}