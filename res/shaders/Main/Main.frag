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

// Mode
uniform int uModeColor;
uniform int uModeLight;

// Light
uniform vec4 uAmbient;
uniform vec4 uDiffuse;
uniform vec4 uSpecular;
uniform float uSpecularPower;
uniform float uConstantAttenuation, uLinearAttenuatuin, uQuadraticAttenuation;

float near = 0.1;
float far  = 100.0;

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
        outColor.rgb = color;
    } else if (uModeColor == 3) {
        // U, V
        outColor.rgb = vec3(texCoord, 0);
    } else if (uModeColor == 4) {
        // Depth
        float depth = linearizeDepth(gl_FragCoord.z) / far;
        outColor.rgb = vec3(depth);
    } else if (uModeColor == 5 || uModeColor == 6 || uModeColor == 7) {
        vec4 baseColor;

        // Texture
        if (uModeColor == 5 || uModeColor == 7){
            vec2 coord = mod(texCoord * vec2(2f, 4f), vec2(1f, 1f));

            baseColor = texture(uTextureId, coord);

            if (uModeColor == 5){
                outColor.rgb = vec3(baseColor.rgb);
            }
        }

        vec4 lighting;

        // Lighting
        if (uModeColor == 6 || uModeColor == 7) {
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

            vec4 totalAmbient = uAmbient * baseColor;
            vec4 totalDiffuse = uDiffuse * NDotL * baseColor;
            vec4 totalSpecular = uSpecular * (pow(NDotH, uSpecularPower * 4.0));

            float att = 1.0 / (uConstantAttenuation + uLinearAttenuatuin * dist + uQuadraticAttenuation * dist * dist);

            if (uModeColor == 6) {
                if (uModeLight == 0) {
                    outColor = totalAmbient;
                } else if (uModeLight == 1) {
                    outColor = totalAmbient + totalDiffuse;
                } else if (uModeLight == 2) {
                    outColor = totalAmbient + totalDiffuse + totalSpecular;
                } else if (uModeLight == 3) {
                    outColor = totalAmbient + att * (totalDiffuse + totalSpecular);
                }
            } else if (uModeColor == 7){
                if (uModeLight == 0) {
                    lighting = totalAmbient;
                } else if (uModeLight == 1) {
                    lighting = totalAmbient + totalDiffuse;
                } else if (uModeLight == 2) {
                    lighting = totalAmbient + totalDiffuse + totalSpecular;
                } else if (uModeLight == 3) {
                    lighting = totalAmbient + att * (totalDiffuse + totalSpecular);
                }
            }
        }

        if (uModeColor == 7) {
            outColor = vec4(lighting.xyz * baseColor.rgb, 1);
        }
    } else if (uModeColor == 8) {
        // Distance from light
        outColor.rgb = vec3(dist);
    }
}
