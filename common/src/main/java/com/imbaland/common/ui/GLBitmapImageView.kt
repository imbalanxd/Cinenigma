package com.imbaland.common.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import coil.util.CoilUtils
import com.imbaland.common.ui.shader.compileShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GLBitmapImageView private constructor(context: Context, resourceId: Int?, url: String?):GLSurfaceView(context),
    LifecycleOwner {

    private lateinit var renderer:BitmapRenderer
    constructor(context: Context, url: String):
            this(context, null, url)
    constructor(context: Context, resourceId: Int):
            this(context, resourceId, null)
    init {
        this.lifecycle
        setEGLContextClientVersion(3)
        if(url == null) {
            renderer = BitmapRenderer(context,  BitmapFactory.decodeResource(context.resources, resourceId!!))
            setRenderer(renderer)
        } else {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                renderer = BitmapRenderer(context,  BitmapFactory.decodeStream(URL(url). openConnection().getInputStream()))
                launch(Dispatchers.Main){
                    setRenderer(renderer)
                }
            }
        }
    }

    override val lifecycle: Lifecycle
        get() {
            return if(findViewTreeLifecycleOwner() != null) {
                findViewTreeLifecycleOwner()!!.lifecycle
            } else {
                backingLifecycle = backingLifecycle?: LifecycleRegistry(this)
                backingLifecycle!!
            }
        }
    private var backingLifecycle: LifecycleRegistry? = null

    fun destroy() {
        renderer.destroy()
    }

    internal class BitmapRenderer(val context: Context, val bitmap: Bitmap) : GLSurfaceView.Renderer {
        private var program = 0
        private var vertexShader = 0
        private var fragmentShader = 0
        private var vertexBuffer: FloatBuffer = FloatBuffer.allocate(0)
        private var texBuffer: FloatBuffer = FloatBuffer.allocate(0)
        private var drawListBuffer: ShortBuffer = ShortBuffer.allocate(0)
        private var textureUniformHandle: Int = -1
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            vertexShader = compileShader(context, GLES32.GL_VERTEX_SHADER, "vertex_shader.glsl")
            fragmentShader = compileShader(context, GLES32.GL_FRAGMENT_SHADER, "fragment_shader.glsl")
            program = GLES32.glCreateProgram()
            GLES32.glAttachShader(program, vertexShader)
            GLES32.glAttachShader(program, fragmentShader)
            GLES32.glLinkProgram(program)
            GLES32.glUseProgram(program)
            loadTextures(gl)
        }
        val texCoords = floatArrayOf(0f,0f,0f,1f,1f,1f,1f,0f)
        var drawOrder: ShortArray = shortArrayOf(0, 1, 3,2)
        var bounds = RectF(0f,0f,0f,0f)
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES32.glViewport(0, 0, width, height)
            bounds = RectF(0f,0f,width.toFloat(), height.toFloat())
            val squareCoords = floatArrayOf(
                bounds.left,bounds.top,
                bounds.left, bounds.bottom,
                bounds.right, bounds.bottom,
                bounds.right, bounds.top
            )
            val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
            bb.order(ByteOrder.nativeOrder())

            vertexBuffer = bb.asFloatBuffer()
            vertexBuffer.put(squareCoords)
            vertexBuffer.position(0)

            val tt = ByteBuffer.allocateDirect(texCoords.size * 4)
            tt.order(ByteOrder.nativeOrder())
            texBuffer = tt.asFloatBuffer()
            texBuffer.put(texCoords)
            texBuffer.position(0)

            val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
            dlb.order(ByteOrder.nativeOrder())
            drawListBuffer = dlb.asShortBuffer()
            drawListBuffer.put(drawOrder)
            drawListBuffer.position(0)
            val projection = FloatArray(16)
            Matrix.orthoM(projection, 0, 0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)
            val projectionLocation = GLES32.glGetUniformLocation(program, "projection")
            GLES32.glUniformMatrix4fv(projectionLocation, 1, false, projection, 0)


            GLES32.glUseProgram(this.program)
            GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 1);
            posHandle = GLES32.glGetAttribLocation(program, "vPosition")
            texHandle = GLES32.glGetAttribLocation(program, "aTextureCoord")

            GLES32.glVertexAttribPointer(
                posHandle,
                coordsPerVertex,
                GLES32.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
            GLES32.glVertexAttribPointer(
                texHandle,
                coordsPerVertex,
                GLES32.GL_FLOAT,
                false,
                vertexStride,
                texBuffer
            )
        }
        protected var coordsPerVertex: Int = 2
        protected var vertexCount: Int = 12 / coordsPerVertex
        protected val vertexStride: Int = coordsPerVertex * 4
        var posHandle = 0
        var texHandle = 0
        override fun onDrawFrame(gl: GL10?) {
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
            GLES32.glUniform1i(textureUniformHandle, 0)
            GLES32.glEnableVertexAttribArray(posHandle)
            GLES32.glEnableVertexAttribArray(texHandle)
            GLES32.glDrawElements(GLES32.GL_TRIANGLE_STRIP,drawOrder.size,GLES32.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES32.glDisableVertexAttribArray(posHandle)
            GLES32.glDisableVertexAttribArray(texHandle)
        }

        private val textureUnit = IntArray(1)
        private fun loadTextures(gl: GL10?) {
            GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
            GLES32.glGenTextures(1, textureUnit, 0)
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureUnit[0])
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
            textureUniformHandle = GLES32.glGetUniformLocation(program, "bitmap")
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        fun destroy() {
            GLES32.glDeleteProgram(program)
            GLES32.glDeleteShader(fragmentShader)
            GLES32.glDeleteShader(vertexShader)
        }
    }
}