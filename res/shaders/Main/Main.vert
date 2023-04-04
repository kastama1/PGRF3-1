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

// Mode
uniform int uModeGrid;
uniform int uModeProjection;
uniform int uModeColor;

// Time
uniform float uTime;

// Position of light source
uniform vec3 uLightSource;

const float PI = 3.1416;

vec3 getCartesianObject1(vec2 position) {
    float x = position.x;
    float y = position.y;
    float offset = uTime;
    float z = 0.2 * sin(5.0 * position.x + offset);

    return vec3(x, y, z);
}

vec3 getCartesianObject1Normal(vec2 position) {
    vec3 u = getCartesianObject1(position + vec2(0.001, 0)) - getCartesianObject1(position - vec2(0.001, 0));
    vec3 v = getCartesianObject1(position + vec2(0, 0.001)) - getCartesianObject1(position - vec2(0, 0.001));

    return cross(u, v);
}

vec3 getCartesianObject2(vec2 position) {
    float x = position.x;
    float y = position.y;
    float z = 2 + position.x/4 + position.y/2;

    return vec3(x, y, z);
}

vec3 getCartesianObject2Normal(vec2 position) {
    vec3 u = getCartesianObject2(position + vec2(0.001, 0)) - getCartesianObject2(position - vec2(0.001, 0));
    vec3 v = getCartesianObject2(position + vec2(0, 0.001)) - getCartesianObject2(position - vec2(0, 0.001));

    return cross(u, v);
}

vec3 getCartesianObject3(vec2 position) {
    float x = position.x;
    float y = position.y;
    float z = position.x * position.y;

    return vec3(x, y, z);
}

vec3 getCartesianObject3Normal(vec2 position) {
    vec3 u = getCartesianObject3(position + vec2(0.001, 0)) - getCartesianObject3(position - vec2(0.001, 0));
    vec3 v = getCartesianObject3(position + vec2(0, 0.001)) - getCartesianObject3(position - vec2(0, 0.001));

    return cross(u, v);
}

vec2 getSphere(vec2 position){
    float azimut = position.x * PI;
    float zenit = position.y * PI / 2;

    return vec2(azimut, zenit);
}

vec3 getSphereObject1(vec2 position) {
    vec2 sphere = getSphere(position);

    float r = 3 * cos(4 * sphere.y);

    float x = r * cos(sphere.x) * cos(sphere.y);
    float y = r * sin(sphere.x) * cos(sphere.y);
    float z = r * sin(sphere.y);

    return vec3(x, y, z);
}

vec3 getSphereObject1Normal(vec2 position) {
    vec3 u = getSphereObject1(position + vec2(0.001, 0)) - getSphereObject1(position - vec2(0.001, 0));
    vec3 v = getSphereObject1(position + vec2(0, 0.001)) - getSphereObject1(position - vec2(0, 0.001));

    return cross(u, v);
}

vec3 getSphereObject2(vec2 position) {
    vec2 sphere = getSphere(position);

    float r = 2 + sin(5 * sphere.x + 7 * sphere.y);

    float x = r * cos(sphere.x) * cos(sphere.y);
    float y = r * sin(sphere.x) * cos(sphere.y);
    float z = r * sin(sphere.y);

    return vec3(x, y, z);
}

vec3 getSphereObject2Normal(vec2 position) {
    vec3 u = getSphereObject2(position + vec2(0.001, 0)) - getSphereObject2(position - vec2(0.001, 0));
    vec3 v = getSphereObject2(position + vec2(0, 0.001)) - getSphereObject2(position - vec2(0, 0.001));

    return cross(u, v);
}

vec2 getCylinder(vec2 position){
    float azimut = position.x * PI;
    float zenit = position.y * PI;

    return vec2(azimut, zenit);
}

vec3 getCylinderObject1(vec2 position) {
    vec2 cylinder = getCylinder(position);

    float a = 3;
    float b = 1;

    float x = cos(cylinder.x) * (a + b * cos(cylinder.y));
    float y = sin(cylinder.x) * (a + b * cos(cylinder.y));
    float z = b * sin(cylinder.y);

    return vec3(x, y, z);
}

vec3 getCylinderObject1Normal(vec2 position) {
    vec3 u = getCylinderObject1(position + vec2(0.001, 0)) - getCylinderObject1(position - vec2(0.001, 0));
    vec3 v = getCylinderObject1(position + vec2(0, 0.001)) - getCylinderObject1(position - vec2(0, 0.001));

    return cross(u, v);
}

vec3 getCylinderObject2(vec2 position) {
    vec2 cylinder = getCylinder(position);

    float x = cylinder.x;
    float y = cylinder.y;
    float z = pow(cylinder.x, 2) + pow(cylinder.y, 2);

    return vec3(x, y, z);
}

vec3 getCylinderObject2Normal(vec2 position) {
    vec3 u = getCylinderObject2(position + vec2(0.001, 0)) - getCylinderObject2(position - vec2(0.001, 0));
    vec3 v = getCylinderObject2(position + vec2(0, 0.001)) - getCylinderObject2(position - vec2(0, 0.001));

    return cross(u, v);
}

void main() {
    vec2 position = inPosition * 2 - 1;
    vec3 newPos;
    vec3 nor;


    if (uModeGrid == 0){
        // Cartesian 1
        // Object transform in time
        newPos = getCartesianObject1(position);
        nor = normalize(getCartesianObject1Normal(position));
    } else if (uModeGrid == 1){
        // Cartesian 2
        newPos = getCartesianObject2(position);
        nor = normalize(getCartesianObject2Normal(position));
    } else if (uModeGrid == 2){
        // Cartesian 3
        newPos = getCartesianObject3(position);
        nor = normalize(getCartesianObject3Normal(position));
    } else if (uModeGrid == 3){
        // Sphere 1
        newPos = getSphereObject1(position);
        nor = normalize(getSphereObject1Normal(position));
    } else if (uModeGrid == 4){
        // Sphere 2
        newPos = getSphereObject2(position);
        nor = normalize(getSphereObject2Normal(position));
    } else if (uModeGrid == 5){
        // Cylinder1
        newPos = getCylinderObject1(position);
        nor = normalize(getCylinderObject1Normal(position));
    } else if (uModeGrid == 6){
        // Cylinder2
        newPos = getCylinderObject2(position);
        nor = normalize(getCylinderObject2Normal(position));
    }

    if (uModeColor == 0) {
        color = vec3(0.5, 0.5, 0.5);
    } else if (uModeColor == 1) {
        color = newPos;
    } else if (uModeColor == 2) {
        color = nor;
    } else if (uModeColor == 3) {
        texCoord = vec2(inPosition.xy);
    } else if (uModeColor == 5) {
        texCoord = vec2(inPosition.xy);
    } else if (uModeColor == 6) {
        vec4 objectPosition = uView * uModel * vec4(newPos, 1f);

        normal = inverse(transpose(mat3(uView * uModel))) * nor;

        vec4 lightPosition = vec4(uLightSource, 1f);

        lightDirection = lightPosition.xyz - objectPosition.xyz;

        viewDirection = objectPosition.xyz;

        dist = length(lightDirection);

        color = vec3(0.5, 0.5, 0.5);
    } else if (uModeColor == 7) {
        texCoord = vec2(inPosition.xy);

        vec4 objectPosition = uView * uModel * vec4(newPos, 1f);

        normal = inverse(transpose(mat3(uView * uModel))) * nor;

        vec4 lightPosition = vec4(uLightSource, 1.0);

        lightDirection = lightPosition.xyz - objectPosition.xyz;

        viewDirection = objectPosition.xyz;

        dist = length(lightDirection);
    } else if (uModeColor == 8) {
        vec4 objectPosition = uView * uModel * vec4(newPos, 1f);
        vec4 lightPosition = vec4(uLightSource, 1.0);
        lightDirection = lightPosition.xyz - objectPosition.xyz;
        dist = length(lightDirection);
    }

    gl_Position = uProj * uView * uModel * vec4(newPos, 1f);
}
