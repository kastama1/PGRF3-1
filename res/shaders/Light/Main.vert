#version 330
in vec2 inPosition;

// Model, view, projection
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;

const float PI = 3.1416;

vec3 getObject(vec2 position) {
    float azimut = position.y * 2.f * PI;
    float zenit = position.x * PI;

    float r = 1.f;

    float x = r * sin(zenit) * cos(azimut);
    float y = r * sin(zenit) * sin(azimut);
    float z = r * cos(zenit);

    return vec3(x, y, z) / 3;
}

void main() {
    vec3 newPos = getObject(inPosition);

    gl_Position = uProj * uView * uModel * vec4(newPos, 1.f);
}
