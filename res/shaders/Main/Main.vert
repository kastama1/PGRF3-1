#version 330
in vec2 inPosition;

out vec3 color;
out vec2 texCoord;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;
uniform mat4 uOrtho;
uniform int uTypeGrid;
uniform int uTypeProjection;
uniform float uTime;

const float PI = 3.1416;

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
    if (uTypeGrid == 0){
        float offset = uTime;
        float z = 0.2 * sin(5.0 * position.x + offset);
        newPos = vec3(position.xy, z);
    } else if (uTypeGrid == 1){
        float z = 2 + position.x/4 + position.y/2;
        newPos = vec3(position.xy, z);
    } else if (uTypeGrid == 2){
        float z = position.x * position.y;
        newPos = vec3(position.xy, z);
    } else if (uTypeGrid == 3){
        vec2 sphere = getSphere(position);
        float r = 3 * cos(4 * sphere.y);
        newPos = getCartesianFromSphere(vec3(sphere, r));
    } else if (uTypeGrid == 4){
        vec2 sphere = getSphere(position);
        float r = 2 + sin(5 * sphere.x + 7 * sphere.y);
        newPos = getCartesianFromSphere(vec3(sphere, r));
    } else if (uTypeGrid == 5){
        vec2 cylinder = getCylinder(position);

        float a = 3;
        float b = 1;

        float x = cos(cylinder.x) * (a + b * cos(cylinder.y));
        float y = sin(cylinder.x) * (a + b * cos(cylinder.y));
        float z = b * sin(cylinder.y);

        newPos = vec3(x, y, z);
    } else if (uTypeGrid == 6){
        vec2 cylinder = getCylinder(position);

        float x = cylinder.x;
        float y = cylinder.y;
        float z = pow(cylinder.x, 2) + pow(cylinder.y, 2);

        newPos = vec3(x, y, z);
    }

    vec4 finalPos = vec4(newPos, 1.f);

    if (uTypeProjection == 0){
        gl_Position = uProj * uView * uModel * finalPos;
    } else if (uTypeProjection == 1) {
        gl_Position = uOrtho * uView * uModel * finalPos;
    }

    color = vec3(finalPos.xyz);
    texCoord = vec2(finalPos.xy);
}
