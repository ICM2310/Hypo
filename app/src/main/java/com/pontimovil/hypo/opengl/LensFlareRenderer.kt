package com.pontimovil.hypo.opengl

import android.annotation.SuppressLint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.google.android.filament.VertexBuffer
import com.pontimovil.hypo.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TexturedQuad {
    private var vertexBuffer: VertexBuffer? = null
    private var textureId = 0

    fun loadTexture(textureResourceId: Int) {
        textureId = TextureHelper.loadTexture(textureResourceId)
    }

    fun draw(vPMatrix: FloatArray, modelMatrix: FloatArray) {

    }
}


@SuppressLint("UnusedImport")
class LensFlareRenderer : GLSurfaceView.Renderer {
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vPMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private lateinit var lensFlareQuad: TexturedQuad

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        lensFlareQuad = TexturedQuad()
        lensFlareQuad.loadTexture(R.drawable.lens_flare)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        lensFlareQuad.draw(vPMatrix, modelMatrix)
    }
}
