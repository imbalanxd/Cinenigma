package com.imbaland.common.tool

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.imbaland.common.ui.util.normalize
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext


class TextVisionFinder() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val _textBoxFlow = MutableSharedFlow<List<RectF>>(1, 1, BufferOverflow.DROP_OLDEST)
    val textBoxFlow: Flow<List<RectF>> = _textBoxFlow.asSharedFlow()
    suspend fun findText(bitmap: Bitmap): List<RectF> = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<List<RectF>>()
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { textVision ->
            deferred.complete(textVision.textBlocks.mapNotNull { it.boundingBox?.toRectF() })
        }.addOnFailureListener {
            deferred.completeExceptionally(it)
        }
        return@withContext deferred.await()
    }

    fun findTextFlow(bitmap: Bitmap, normalize: Boolean = false) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { textVision ->
            val textBoxes = textVision.textBlocks.mapNotNull {
                it.boundingBox?.toRectF()?.let { box -> if(normalize) box.normalize(image.width.toFloat(), image.height.toFloat()) else box } }
            _textBoxFlow.tryEmit(textBoxes)
        }.addOnFailureListener {
            logDebug("TextFinder error occurred: ${it.message}")
//            deferred.completeExceptionally(it)
        }
    }
}