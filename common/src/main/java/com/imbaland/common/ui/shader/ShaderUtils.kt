package com.imbaland.common.ui.shader

import android.content.Context
import android.opengl.GLES32
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun compileShader(context: Context, type: Int, fileName: String): Int {
    val shader = GLES32.glCreateShader(type)
    GLES32.glShaderSource(shader, context.readFromAssets(fileName))
    GLES32.glCompileShader(shader)

    val compileStatus = IntArray(1)
    GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compileStatus, 0)
    if (compileStatus[0] == 0) {
        Log.d("SHADERWTF", "Error compiling shader: " + GLES32.glGetShaderInfoLog(shader))
        GLES32.glDeleteShader(shader)
//        throw RuntimeException("Error compiling shader: " + GLES32.glGetShaderInfoLog(shader))
    }

    return shader
}

private fun Context.readFromAssets(fileName: String): String {
    try {
        val inputStream = assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()

        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }

        bufferedReader.close()
        inputStream.close()

        return stringBuilder.toString()
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}