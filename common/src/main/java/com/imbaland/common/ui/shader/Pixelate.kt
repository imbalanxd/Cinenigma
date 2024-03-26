package com.imbaland.common.ui.shader

import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Modifier.pixelate(pixelSize: Float, topLeft: Offset, size: Size): Modifier {
    val shader = remember { RuntimeShader(PIXELATE_SHADER) }
    return graphicsLayer {
        shader.setFloatUniform("start", topLeft.x, topLeft.y)
        shader.setFloatUniform("end", topLeft.x + size.width, topLeft.y + size.height)
        shader.setFloatUniform("size", pixelSize)
        clip = true
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, "composable")
            .asComposeRenderEffect()
    }
}

private const val PIXELATE_SHADER = """
    uniform float2 start;
    uniform float2 end;
    uniform float size;
    uniform shader composable;
    
    float2 pixelFloat(in float a, in float b) {
        return float2(floor(a/size) * size + size/2, floor(b/size) * size + size/2);
    }
//    bool contained(in float a, in float b) {
//        return (a > start.x && a < end.x && b > start.y && b < end.y);
//    }
    half4 main(float2 fragCoord){
        if(end.x == 0 || (fragCoord.x > start.x && fragCoord.x < end.x && fragCoord.y > start.y && fragCoord.y < end.y))
            return composable.eval(pixelFloat(fragCoord.x, fragCoord.y));
        else
            return composable.eval(float2(fragCoord.x, fragCoord.y));
    }
"""