#version 330
in vec2 inPosition;

out vec3 color;
out vec3 normal;
out vec3 lightDirection;
out vec3 viewDirection;
out float dist;

uniform mat4 uView;
uniform mat4 uProj;
uniform vec3 uLightSource;

const float PI = 3.1415;

vec3 func(float x, float y){
    float azimut = x * PI * 2;
    float zenit = y * PI;

    return vec3(3 * cos(azimut) * sin(zenit), 2 * sin(azimut) * sin (zenit), 1 * cos(zenit));
}

vec3 getNormal(float x, float y) {
    float azimut = x * PI * 2;
    float zenit = y * PI;

    vec3 dx = vec3(-3 * sin(azimut) * sin(zenit), 2 * cos(azimut) * sin(zenit), 0);
    vec3 dy = vec3(3 * cos(azimut) * cos(zenit), 2 * sin(azimut) * cos(zenit), -sin(zenit));

    return cross(dx, dy);
}

void main() {
    vec3 position = func(inPosition.x, inPosition.y);
    vec4 objectPosition = uView * vec4(position, 1.0);

    gl_Position = uProj * objectPosition;

    vec3 nor = normalize(getNormal(inPosition.x, inPosition.y));

    normal = inverse(transpose(mat3(uView))) * nor;

    vec4 lightPosition = uView * vec4(uLightSource, 1.0);
    //vec4 lightPosition = vec4(uLightSource, 1.0);

    lightDirection = lightPosition.xyz - objectPosition.xyz;

    viewDirection = objectPosition.xyz;

    dist = length(lightDirection);

    color = vec3(0.5, 0.5, 1);
}
