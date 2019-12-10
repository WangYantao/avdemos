#version 300 es

uniform mat4 uMatrix;
in vec3 aPosition;
in vec2 aTexCoord;
out vec2 vTexCoord;

void main() {
    gl_Position = uMatrix * vec4(aPosition, 1);
    vTexCoord = aTexCoord;
}
