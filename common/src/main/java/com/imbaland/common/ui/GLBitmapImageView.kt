package com.imbaland.common.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import androidx.compose.animation.core.EaseInSine
import com.imbaland.common.tool.logDebug
import com.imbaland.common.ui.shader.compileShader
import com.imbaland.common.ui.shader.toBuffer
import com.imbaland.common.ui.shader.toFloatArray
import com.imbaland.common.ui.util.normalize
import com.imbaland.common.ui.util.project
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GLBitmapImageView(context: Context):GLSurfaceView(context) {

    private var renderer:BitmapRenderer? = null
    private var blurBoxes = hashMapOf<String, List<RectF>>()

    fun setBitmap(image: Bitmap) {
        if(renderer == null) {
            if(image.config != Bitmap.Config.ARGB_8888) {
                image.copy(Bitmap.Config.ARGB_8888,true)
            } else {
                image
            }?.let { bitmap ->
                setEGLContextClientVersion(3)
                renderer = BitmapRenderer(context, bitmap)
                setRenderer(renderer)
            }
        }
    }

    fun addBlurBatch(batch: HashMap<String, List<RectF>>, isNormalized: Boolean = false) {
        batch.keys.forEach { key ->
            blurBoxes[key] = if(!isNormalized) batch[key]!!.map { box -> box.normalize(this.width.toFloat(), this.height.toFloat()) } else batch[key]!!
        }
        requestRender()
    }

    fun addBlurBoxes(groupId: String = "", boxes: List<RectF>, isNormalized: Boolean = false) {
        addBlurBatch(hashMapOf(groupId to boxes), isNormalized)
    }

    fun addBlurBox(boxId: String = "", box: RectF, isNormalized: Boolean = false) {
        addBlurBatch(hashMapOf(boxId to listOf(box)), isNormalized)
    }

    fun destroy() {
        renderer?.destroy()
    }

    inner class BitmapRenderer(private val context: Context, private val bitmap: Bitmap) : Renderer {
        private val coordsPerVertex: Int = 2
        private  val vertexStride: Int = coordsPerVertex * 4

        private var program = 0
        private var vertexShader = 0
        private var fragmentShader = 0

        private var positionLocation = -1
        private var textureLocation = -1
        private var blurBoxLocation = -1
        private var blurBoxAmountLocation = -1
        private var imageTextureLocation: Int = -1
        private var maskTextureLocation: Int = -1
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            loadShaders()

            positionLocation = GLES32.glGetAttribLocation(program, "vPosition")
            textureLocation = GLES32.glGetAttribLocation(program, "aTextureCoord")
            blurBoxLocation = GLES32.glGetUniformLocation(program, "aBlurWindow")
            blurBoxAmountLocation = GLES32.glGetUniformLocation(program, "aBlurWindowAmount")
            imageTextureLocation = GLES32.glGetUniformLocation(program, "aTexture")
            maskTextureLocation = GLES32.glGetUniformLocation(program, "aBlurMask")

            loadTextures()
        }
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES32.glViewport(0, 0, width, height)
            viewBounds = RectF(0f,0f,width.toFloat(), height.toFloat())

            val projection = FloatArray(16)
            Matrix.orthoM(projection, 0, 0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)
            val projectionLocation = GLES32.glGetUniformLocation(program, "projection")
            GLES32.glUniformMatrix4fv(projectionLocation, 1, false, projection, 0)

            GLES32.glUseProgram(this.program)

            GLES32.glVertexAttribPointer(
                positionLocation,
                coordsPerVertex,
                GLES32.GL_FLOAT,
                false,
                vertexStride,
                vertexCoords
            )
            GLES32.glVertexAttribPointer(
                textureLocation,
                coordsPerVertex,
                GLES32.GL_FLOAT,
                false,
                vertexStride,
                textureCoords
            )
        }
        override fun onDrawFrame(gl: GL10?) {
            createMask()
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

            blurBoxes.values.find { it.isNotEmpty() }?.first()?.let { firstBox ->
                GLES32.glUniform4fv(blurBoxLocation, 1, firstBox.toFloatArray().toBuffer())
                logDebug("${getBlurValue(firstBox)}")
                GLES32.glUniform1fv(blurBoxAmountLocation, 1, floatArrayOf(getBlurValue(firstBox)).toBuffer())
            }


            ///
            GLES32.glEnableVertexAttribArray(positionLocation)
            GLES32.glEnableVertexAttribArray(textureLocation)
            GLES32.glDrawElements(GLES32.GL_TRIANGLE_STRIP, 4 ,GLES32.GL_UNSIGNED_SHORT, drawOrder);
            GLES32.glDisableVertexAttribArray(positionLocation)
            GLES32.glDisableVertexAttribArray(textureLocation)
        }
        private fun loadShaders() {
            vertexShader = compileShader(context, GLES32.GL_VERTEX_SHADER, "vertex_shader.glsl")
            fragmentShader = compileShader(context, GLES32.GL_FRAGMENT_SHADER, "fragment_shader.glsl")
            program = GLES32.glCreateProgram()
            this@GLBitmapImageView.renderMode = RENDERMODE_WHEN_DIRTY
            GLES32.glAttachShader(program, vertexShader)
            GLES32.glAttachShader(program, fragmentShader)
            GLES32.glLinkProgram(program)
            GLES32.glUseProgram(program)
        }
        val textureUnits = IntArray(2)
        private fun loadTextures() {
            GLES32.glUniform1i(imageTextureLocation, 0)
            GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
            GLES32.glGenTextures(1, textureUnits, 0)
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureUnits[0])
            GLES32.glTexParameterf(
                GLES32.GL_TEXTURE_2D,
                GLES32.GL_TEXTURE_MIN_FILTER,
                GLES32.GL_NEAREST.toFloat()
            )
            GLES32.glTexParameterf(
                GLES32.GL_TEXTURE_2D,
                GLES32.GL_TEXTURE_MAG_FILTER,
                GLES32.GL_LINEAR.toFloat()
            )

            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        private fun getBlurValue(box: RectF): Float { //Between 0.1 and 0.85
            val max = 0.85f
            val min = 0.05f
//            val amount = min + (1f - (box.width() * box.height()))*(max-min)
            val amount = min + (1f - (box.width()))*(max-min)

            return Math.pow(amount.toDouble(), 1.5).toFloat()//EaseOutQuint.transform(amount)
        }

        private fun createMask() {
            //DRAW MASK
            val blurMask = Bitmap.createBitmap(viewBounds.width().toInt()/3, viewBounds.height().toInt()/3, Bitmap.Config.ARGB_8888)
            blurMask.eraseColor(Color.BLACK)
            val canvas = Canvas(blurMask)
            blurBoxes["text"]?.forEach { box ->
                canvas.drawRect(box.project(blurMask.width.toFloat(), blurMask.height.toFloat()), Paint().apply { color = Color.RED })
            }
            //
            GLES32.glUniform1i(imageTextureLocation, 2)
            GLES32.glGenTextures(1, textureUnits, 1)
            GLES32.glActiveTexture(GLES32.GL_TEXTURE0+2)
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureUnits[1])
            GLES32.glTexParameterf(
                GLES32.GL_TEXTURE_2D,
                GLES32.GL_TEXTURE_MIN_FILTER,
                GLES32.GL_NEAREST.toFloat()
            )
            GLES32.glTexParameterf(
                GLES32.GL_TEXTURE_2D,
                GLES32.GL_TEXTURE_MAG_FILTER,
                GLES32.GL_LINEAR.toFloat()
            )
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, blurMask, 0)
        }
        private var viewBounds: RectF = RectF(0f,0f,0f,0f)
            set(value) {
                field = value
                vertexCoords = floatArrayOf(
                    viewBounds.left,viewBounds.top,
                    viewBounds.left, viewBounds.bottom,
                    viewBounds.right, viewBounds.bottom,
                    viewBounds.right, viewBounds.top).toBuffer()
                this@GLBitmapImageView.requestRender()
            }
        private var vertexCoords: FloatBuffer = FloatBuffer.allocate(0)
        private val textureCoords: FloatBuffer = floatArrayOf(
            0f,0f,
            0f,1f,
            1f,1f,
            1f,0f).toBuffer()
        private val drawOrder: ShortBuffer = shortArrayOf(0, 1, 3,2).toBuffer()

        fun destroy() {
            GLES32.glDeleteProgram(program)
            GLES32.glDeleteShader(fragmentShader)
            GLES32.glDeleteShader(vertexShader)
        }
    }
}