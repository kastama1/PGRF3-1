#version 330
in vec2 inPosition;

out vec3 color;

uniform mat4 uView;
uniform mat4 uProj;
uniform float uType;

const float PI = 3.1415;

vec2 getSphere(vec2 position) {
    float azimut = position.x * PI;
    float zenit = position.y * (PI / 2);

    return vec2(azimut, zenit);
}

vec2 getCylinder(vec2 position) {
    float azimut = position.x * PI;
    float zenit = position.y * PI;

    return vec2(azimut, zenit);
}

vec3 getCartesianFromSphere(vec3 sphere) {
    float x = sphere.z * cos(sphere.x) * cos(sphere.y);
    float y = sphere.z * sin(sphere.x) * cos(sphere.y);
    float z = sphere.z * sin(sphere.y);

    return vec3(x, y, z);
}

void main() {
    vec2 position = inPosition * 2 - 1;
    vec3 newPos;
    if (uType == 0){
        newPos = vec3(inPosition, 0);
    } else if (uType == 1){
        float z = 2 + inPosition.x/4 + inPosition.y/2;
        newPos = vec3(inPosition.xy, z);
    } else if (uType == 2){
        float z = position.x * position.y;
        newPos = vec3(position.xy, z);
    } else if (uType == 3){
        vec2 sphere = getSphere(position);
        float r = 3 * cos(4 * sphere.y);
        newPos = getCartesianFromSphere(vec3(sphere, r));
    } else if (uType == 4){
        vec2 sphere = getSphere(position);
        float r = 2 + sin(5 * sphere.x + 7 * sphere.y);
        newPos = getCartesianFromSphere(vec3(sphere, r));
    } else if (uType == 5){
        vec2 cylinder = getCylinder(position);

        float a = 3;
        float b = 1;

        float x = cos(cylinder.x) * (a + b * cos(cylinder.y));
        float y = sin(cylinder.x) * (a + b * cos(cylinder.y));
        float z = b * sin(cylinder.y);

        newPos = vec3(x, y, z);
    } else if (uType == 6){
        vec2 cylinder = getCylinder(position);

        float x = cylinder.x;
        float y = cylinder.y;
        float z = pow(cylinder.x, 2) + pow(cylinder.y, 2);

        newPos = vec3(x, y, z);
    }

    vec4 finalPos = vec4(newPos, 1.f);

    gl_Position = uProj * uView * finalPos;

    color = vec3(finalPos.xyz);
}
