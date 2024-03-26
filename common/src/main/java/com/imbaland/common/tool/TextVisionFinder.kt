package com.imbaland.common.tool

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
    private val _textBoxFlow = MutableSharedFlow<List<Rect>>(1, 1, BufferOverflow.DROP_OLDEST)
    val textBoxFlow: Flow<List<Rect>> = _textBoxFlow.asSharedFlow()
    suspend fun findText(bitmap: Bitmap): List<Rect> = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<List<Rect>>()
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { textVision ->
            deferred.complete(textVision.textBlocks.mapNotNull { it.boundingBox })
        }.addOnFailureListener {
            deferred.completeExceptionally(it)
        }
        return@withContext deferred.await()
    }

    fun findTextFlow(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { textVision ->
            val textBoxes = textVision.textBlocks.mapNotNull { it.boundingBox }
            _textBoxFlow.tryEmit(textBoxes)
        }.addOnFailureListener {
//            deferred.completeExceptionally(it)
        }
    }
}