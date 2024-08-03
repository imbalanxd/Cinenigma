#version 300 es

precision mediump float;
in vec2 vTextureCoord;
uniform vec4 aBlurWindow;
uniform float aBlurWindowAmount;
uniform float aClipOutside;
uniform sampler2D aTexture;
uniform vec2 aTextureSize;
uniform sampler2D aBlurMask;
layout (location = 0) out vec4 fragColor;

//blur.x is for text blurring
//blur.y is for hint blurring
vec4 pixelFloat(float a, float b, vec2 blur)
{
    //Hint blur is always active
    if (blur.y > 0.0) {
        float blurAmount = blur.y;
        return texture(aTexture, vec2(floor(a / blurAmount) * blurAmount + blurAmount / 2.0, floor(b / blurAmount) * blurAmount) + blurAmount / 2.0);
    }

    if (a > aBlurWindow.x && a < aBlurWindow.z && b > aBlurWindow.y && b < aBlurWindow.w) {
        float blurAmount = aBlurWindowAmount;
        if (blur.x > 0.0) {
            blurAmount = 0.05;
        }
        return texture(aTexture, vec2(floor(a / blurAmount) * blurAmount + blurAmount / 2.0, floor(b / blurAmount) * blurAmount) + blurAmount / 2.0);
    }

    if (aClipOutside == 1.0) {
        return vec4(0.0, 0.0, 0.0, 0.0);
    }

    return normalize(texture(aTexture, vec2(a, b)));
}

void main() {
    fragColor = pixelFloat(vTextureCoord.x, vTextureCoord.y, texture(aBlurMask, vTextureCoord).xy);
}