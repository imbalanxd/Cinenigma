#version 300 es

precision mediump float;
in vec2 vTextureCoord;
uniform vec4 aBlurWindow;
uniform float aBlurWindowAmount;
uniform sampler2D aTexture;
uniform sampler2D aBlurMask;
layout (location = 0) out vec4 fragColor;
float size = 100.0;

vec2 pixelFloat(float a, float b, vec2 blur) {
if (blur.y > 0.0) {
            float blurAmount = 120.0-(blur.y * size);
                            return vec2(round(a * blurAmount) / blurAmount, floor(b * blurAmount) / blurAmount);
         }
        else if (a > aBlurWindow.x && a < aBlurWindow.z && b > aBlurWindow.y && b < aBlurWindow.w) {
        if (blur.x > 0.0) {
                float blurAmount = 120.0-(blur.x * size);
                return vec2(round(a * blurAmount) / blurAmount, floor(b * blurAmount) / blurAmount);
            } else {
                float blurAmountWindow = 180.0 * aBlurWindowAmount;
                return vec2(round(a * blurAmountWindow) / blurAmountWindow, round(b * blurAmountWindow) / blurAmountWindow);
            }
        }else {
            return vec2(a, b);
        }
}

void main() {
    fragColor = texture(aBlurMask, pixelFloat(vTextureCoord.x, vTextureCoord.y, texture(aTexture, vTextureCoord).xy));
}