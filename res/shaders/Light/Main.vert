#version 330
in vec2 inPosition;

out vec3 color;
out vec3 normal;
out vec3 lightDirection;
out vec3 viewDirection;
out float dist;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;
uniform vec3 uLightSource;

const float PI = 3.1416;

vec3 func(float x, float y){
    float azimut = x * PI * 2;
    float zenit = y * PI;

    return vec3(5 * cos(azimut) * sin(zenit), 5 * sin(azimut) * sin(zenit), 5 * cos(zenit));
}

vec3 getNormal(float x, float y) {
    float azimut = x * PI * 2;
    float zenit = y * PI;

    vec3 dx = vec3(-4 * sin(azimut) * sin(zenit), 4 * cos(azimut) * sin(zenit), 0);
    vec3 dy = vec3(4 * cos(azimut) * cos(zenit), 4 * sin(azimut) * cos(zenit), -4 * sin(zenit));

    return cross(dx, dy);
}

void main() {
    vec3 position = func(inPosition.x, inPosition.y);
    vec4 objectPosition = uView * uModel * vec4(position, 1.0);

    gl_Position = uProj * objectPosition;

    vec3 nor = normalize(getNormal(inPosition.x, inPosition.y));

    normal = inverse(transpose(mat3(uView * uModel))) * nor;

    vec4 lightPosition = uView * uModel * vec4(uLightSource, 1.0);

    lightDirection = lightPosition.xyz - objectPosition.xyz;

    viewDirection = objectPosition.xyz;

    dist = length(lightDirection);

    color = vec3(0.5, 0.5, 0.5);
}
