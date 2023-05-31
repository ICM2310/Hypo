package com.pontimovil.hypo.opengl

import android.annotation.SuppressLint
import android.opengl.GLES20

@SuppressLint("Recycle", "WrongConstant")
class LensFlare {
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 a_TexCoordinate;
        varying vec2 v_TexCoordinate;
        void main() {
            gl_Position = vPosition;
            v_TexCoordinate = a_TexCoordinate;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform sampler2D u_Texture;
        varying vec2 v_TexCoordinate;
        void main() {
            gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
        }
    """.trimIndent()

    private val vertices = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f
    )

    private val textureCoordinates = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )

    private val vertexStride = 2 * 4 // 2 bytes per vertex

    private val vertexCount = vertices.size / 2

    private val textureHandle = IntArray(1)

    private val mProgram: Int

    init {
        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)

            GLES20.glLinkProgram(it)
        }
    }

    fun draw(vPMatrix: FloatArray, modelMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)

        val positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            /*GLES20.glVertexAttribPointer(
                it,
                2,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertices
            )

             */
        }

        val textureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate").also {
            GLES20.glEnableVertexAttribArray(it)

            /*GLES20.glVertexAttribPointer(
                it,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * 4,
                textureCoordinates
            )

             */
        }

        val mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(
                it,
                1,
                false,
                vPMatrix,
                0
            )
        }

        val textureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        GLES20.glUniform1i(textureUniformHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)

        GLES20.glDisableVertexAttribArray(textureCoordinateHandle)
    }

    @SuppressLint("ResourceType")
    fun loadTexture(textureResourceId: Int) {
        textureHandle[0] = MyGLRenderer.loadTexture(textureResourceId)
    }
}
