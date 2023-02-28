#version 330

in vec3 color;

out vec4 outColor;

uniform vec4 uColor;

void main() {
    outColor = vec4(color, 1);
}
