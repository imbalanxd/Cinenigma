package com.imbaland.common.ui.util

import android.graphics.RectF
import androidx.compose.ui.geometry.Rect
import com.imbaland.common.ui.shader.area

fun RectF.toComposeRect(): Rect {
    return Rect(this.left,this.top,this.right,this.bottom)
}

fun RectF.normalize(width: Float, height: Float): RectF {
    return RectF(this.left/width,this.top/height,this.right/width,this.bottom/height)
}
fun RectF.project(width: Float, height: Float): RectF {
    return RectF(this.left*width,this.top*height,this.right*width,this.bottom*height)
}

fun RectF.combine(box: RectF): RectF {
    val left = this.left.coerceAtMost(box.left)
    val right = this.right.coerceAtLeast(box.right)
    val top = this.top.coerceAtMost(box.top)
    val bottom = this.bottom.coerceAtLeast(box.bottom)
    return RectF(left,top,right,bottom)
}

fun List<RectF>.combine(): RectF {
    if(this.isEmpty()) {
        return RectF()
    }
    var container: RectF = this.first()
    this.drop(1).forEach { box ->
        container = container.combine(box)
    }
    return container
}

fun List<RectF>.areaSort(): List<RectF> {
    if(this.isEmpty()) {
        return this
    }
    return this.sortedBy { it.width() }.reversed()
}