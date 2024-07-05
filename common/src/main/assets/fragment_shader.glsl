#version 300 es

precision mediump float;
in vec2 vTextureCoord;
uniform sampler2D bitmap;
layout (location = 0) out vec4 FragColor;
float size = 10.0;

vec2 pixelFloat(float a, float b) {
        return vec2(round(a*size)/size, floor(b*size)/size);
    }

void main() {
    FragColor = texture(bitmap, pixelFloat(vTextureCoord.x, vTextureCoord.y));
}