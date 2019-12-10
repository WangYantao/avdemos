#version 300 es

precision mediump float;

uniform sampler2D uS2dTex;
in vec2 vTexCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(uS2dTex, vTexCoord);
}
