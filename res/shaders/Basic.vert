#version 330
in vec2 inPosition;
in vec3 inColor;

uniform mat4 uView;
uniform mat4 uProj;

out vec3 color;

void main() {
    color = inColor;
    float z = 0.5 * cos(sqrt(20 * inPosition.x * inPosition.x + 20 * inPosition.y * inPosition.y));
    vec4 newPos = vec4(inPosition * 2 - 1, z, 1.f);
    gl_Position = uProj * uView * newPos;
}
