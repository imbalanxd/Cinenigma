#version 300 es

in vec2 vPosition;
uniform mat4 projection;
layout (location = 3) in vec2 aTextureCoord;
out vec2 vTextureCoord;
void main() {
    gl_Position = projection * vec4(vPosition, 0.0, 1.0);
    vTextureCoord = aTextureCoord.xy;
}