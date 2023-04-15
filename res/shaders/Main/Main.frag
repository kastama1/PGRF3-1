#version 330

in vec3 color;
in vec2 texCoord;
in float intensity;
in vec3 normal;
in vec3 lightDirection;
in vec3 viewDirection;
in float dist;

out vec4 outColor;

// Texture
uniform sampler2D uTextureId;
uniform sampler2D uTextureNormal;

// Mode
uniform int uModeColor;
uniform int uModeLight;
uniform int uModeSpot;

// Light
uniform float uSpecularPower;
uniform float uConstantAttenuation, uLinearAttenuatuin, uQuadraticAttenuation;

uniform float uSpotCutOff;
uniform vec3 uSpotDirection;

float near = 0.1;
float far  = 100.0;

vec4 ambient = vec4(0.5, 0.5, 0.5, 1.f);
vec4 diffuse = vec4(0.7, 0.7, 0.7, 1.f);
vec4 specular = vec4(0.8, 0.8, 0.8, 1.f);

float linearizeDepth(float depth)
{
    float z = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - z * (far - near));
}

void main() {
    if (uModeColor == 0){
        // Color
        outColor.rgb = color;
    } else if (uModeColor == 1) {
        // Position X,Y,Z
        outColor.rgb = color;
    } else if (uModeColor == 2) {
        // Normal
        outColor.rgb = normalize(color);
    } else if (uModeColor == 3) {
        // U, V
        outColor.rgb = color;
    } else if (uModeColor == 4) {
        // Depth
        float depth = linearizeDepth(gl_FragCoord.z) / far;
        outColor.rgb = vec3(depth);
    } else if (uModeColor == 5 || uModeColor == 6 || uModeColor == 7 || uModeColor == 9) {
        vec4 baseColor;
        vec2 coord;

        // Texture
        if (uModeColor == 5 || uModeColor == 7 || uModeColor == 9){
            coord = mod(texCoord * vec2(2f, 4f), vec2(1f, 1f));

            baseColor = texture(uTextureId, coord);

            if (uModeColor == 5){
                outColor.rgb = vec3(baseColor.rgb);
            }
        }

        vec4 lighting;
        // Lighting
        if (uModeColor == 6 || uModeColor == 7 || uModeColor == 9) {
            if (uModeColor == 6){
                baseColor = vec4(color, 1);
            }

            vec3 nd = normalize(normal);
            vec3 ld = normalize(lightDirection);
            vec3 vd = normalize(viewDirection);

            float NDotL = max(dot(nd, ld), 0.0);

            vec3 reflection = normalize(((2.0 * nd) * NDotL) - ld);
            float RDOtV = max(0.0, dot(reflection, vd));

            vec3 halfVector = normalize(ld + vd);
            float NDotH = max(0.0, dot(nd, halfVector));

            vec4 totalAmbient = ambient * baseColor;
            vec4 totalDiffuse = diffuse * NDotL * baseColor;
            vec4 totalSpecular = specular * (pow(NDotH, uSpecularPower * 4.0));

            float att = 1.0 / (uConstantAttenuation + uLinearAttenuatuin * dist + uQuadraticAttenuation * dist * dist);

            float spotEffect = max(dot(normalize(uSpotDirection), normalize(-ld)), 0);

            float blend = clamp((spotEffect - uSpotCutOff) / (1 - uSpotCutOff), 0f, 1f);

            if (uModeSpot == 0 && uModeColor != 9){
                // Light
                if (uModeLight == 0) {
                    lighting = totalAmbient;
                } else if (uModeLight == 1) {
                    lighting = totalAmbient + totalDiffuse;
                } else if (uModeLight == 2) {
                    lighting = totalAmbient + totalDiffuse + totalSpecular;
                } else if (uModeLight == 3) {
                    lighting = totalAmbient + att * (totalDiffuse + totalSpecular);
                }
            } else if (uModeSpot == 1 && spotEffect > uSpotCutOff && uModeColor != 9) {
                // Spot light
                if (uModeLight == 0) {
                    lighting = mix(totalAmbient, totalAmbient, blend);
                } else if (uModeLight == 1) {
                    lighting = mix(totalAmbient, totalAmbient + totalDiffuse, blend);
                } else if (uModeLight == 2) {
                    lighting = mix(totalAmbient, totalAmbient + totalDiffuse + totalSpecular, blend);
                } else if (uModeLight == 3) {
                    lighting = mix(totalAmbient, totalAmbient + att * (totalDiffuse + totalSpecular), blend);
                }
            } else {
                lighting = totalAmbient;
            }
        }
        if (uModeColor == 6) {
            outColor = lighting;
        } else if (uModeColor == 7 || uModeColor == 9) {
            outColor.rgb = lighting.xyz * baseColor.rgb;
        }
    } else if (uModeColor == 8) {
        // Distance from light
        outColor.rgb = vec3(dist / 20);
    }

    outColor.a = 1;
}
