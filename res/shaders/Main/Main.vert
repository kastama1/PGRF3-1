#version 330
in vec2 inPosition;

out vec3 color;
out vec2 texCoord;
out vec3 normal;
out vec3 lightDirection;
out vec3 viewDirection;
out float dist;

// Model, view, projection
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;
uniform mat4 uModelLight;

// Mode
uniform int uModeObject;
uniform int uModeProjection;
uniform int uModeColor;

// Time
uniform float uTime;

// Position of light source
uniform vec3 uLightSource;

const float PI = 3.1416;
const float delta = 0.001;

vec3 getCartesianObject1(vec2 position) {
    float x = position.x;
    float y = position.y;
    float offset = uTime;
    float z = 0.f;

    return vec3(x, y, z);
}

vec3 getCartesianObject2(vec2 position) {
    float x = position.x;
    float y = position.y;
    float z = 2.f + position.x / 4.f + position.y / 2.f;

    return vec3(x, y, z);
}

vec3 getCartesianObject3(vec2 position) {
    float x = position.x;
    float y = position.y;
    float z = position.x * position.y;

    return vec3(x, y, z);
}

vec3 getSphereObject1(vec2 position) {
    float azimut = position.y * 2.f * PI;
    float zenit = position.x * PI;

    float r = 1.f;

    float x = r * sin(zenit) * cos(azimut);
    float y = 2 * r * sin(zenit) * sin(azimut);
    float z = 1 * r * cos(zenit);

    return vec3(x, y, z);
}

vec3 getSphereObject2(vec2 position) {
    float azimut = position.y * 2.f * PI;
    float zenit = position.x * PI;

    float r = 2.f + sin(5.f * azimut + 7.f * zenit);

    float x = r * sin(zenit) * cos(azimut);
    float y = r * sin(zenit) * sin(azimut);
    float z = r * cos(zenit);

    return vec3(x, y, z);
}

vec3 getCylinderObject1(vec2 position) {
    float azimut = position.y * 2.f * PI;
    float zenit = position.x * 2.f * PI;

    float r = 2.f + cos(azimut);

    float x = r * cos(zenit);
    float y = r * sin(zenit);
    float z = sin(azimut);

    return vec3(x, y, z);
}

vec3 getCylinderObject2(vec2 position) {
    float azimut = position.y * 2.f * PI;
    float zenit = position.x * 2.f * PI;

    float r = azimut;

    float x = r * cos(zenit);
    float y = r * sin(zenit);
    float z = sin(azimut);

    return vec3(x, y, z);
}

vec3 getObject(vec2 position){
    switch (uModeObject){
        case 0:
        return getCartesianObject1(position);
        case 1:
        return getCartesianObject2(position);
        case 2:
        return getCartesianObject3(position);
        case 3:
        return getSphereObject1(position);
        case 4:
        return getSphereObject2(position);
        case 5:
        return getCylinderObject1(position);
        case 6:
        return getCylinderObject2(position);
    }
    return vec3(0, 0, 0);
}

vec3 getNormal(vec2 position){
    vec3 u = getObject(vec2(position.x + delta, position.y)) - getObject(vec2(position.x - delta, position.y));
    vec3 v = getObject(vec2(position.x, position.y + delta)) - getObject(vec2(position.x, position.y - delta));

    return cross(u, v);
}

vec3 getTangent(vec2 position){
    vec3 du = getObject(vec2(position.x + delta, position.y)) - getObject(vec2(position.x - delta, position.y));
    return vec3(0);
}

void main() {
    vec3 newPos = getObject(inPosition);
    vec3 nor = getNormal(inPosition);

    vec4 objectPosition = uView * uModel * vec4(newPos, 1.f);
    if (uModeColor == 0) {
        color = vec3(0.5, 0.5, 0.5);
    } else if (uModeColor == 1) {
        color = vec3(newPos);
    } else if (uModeColor == 2) {
        color = inverse(transpose(mat3(uView))) * nor;
    } else if (uModeColor == 3) {
        color = vec3(inPosition.xy, 0.f);
    } else if (uModeColor == 5) {
        texCoord = inPosition.xy;
    } else if (uModeColor == 6 || uModeColor == 7 || uModeColor == 8 || uModeColor == 9) {
        normal = transpose(inverse(mat3(uView * uModel))) * nor;

        vec4 lightPosition = uView * uModelLight * vec4(uLightSource, 1.f);

        lightDirection = lightPosition.xyz - objectPosition.xyz;

        viewDirection = -objectPosition.xyz;

        dist = length(lightDirection);

        if (uModeColor == 6) {
            color = vec3(0.5, 0.5, 0.5);
        } else if (uModeColor == 7){
            texCoord = vec2(inPosition.xy);
        }
    }

    gl_Position = uProj * objectPosition;
}
