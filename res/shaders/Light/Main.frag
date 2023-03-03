#version 330

in float intensity;
in vec3 normal;
in vec3 lightDirection;
in vec3 color;
in vec3 viewDirection;
in float dist;

out vec4 outColor;

uniform vec4 uDiffuse;
uniform vec4 uAmbient;
uniform vec4 uSpecular;
uniform float uSpecularPower;
uniform float uConstantAttenuation, uLinearAttenuatuin, uQuadraticAttenuation;

void main() {
    vec4 baseColor = vec4(color, 1);
    vec3 nd = normalize(normal);
    vec3 ld = normalize(lightDirection);
    vec3 vd = normalize(viewDirection);

    float NDotL = max(dot(nd, ld), 0.0);

    vec3 reflection = normalize(((2.0 * nd) * NDotL) - ld);
    float RDOtV = max(0.0, dot(reflection, vd));

    vec3 halfVector = normalize(ld + vd);
    float NDotH = max(0.0, dot(nd, halfVector));

    vec4 totalAmbient = uAmbient * baseColor;
    vec4 totalDiffuse = uDiffuse * NDotL * baseColor;
    vec4 totalSpecular = uSpecular * (pow(NDotH, uSpecularPower * 4.0));

    float att = 1.0 / (uConstantAttenuation + uLinearAttenuatuin * dist + uQuadraticAttenuation * dist * dist);

    outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
}
