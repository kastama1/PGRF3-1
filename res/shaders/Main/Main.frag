#version 330

in vec3 color;
in vec2 texCoord;
in float intensity;

out vec4 outColor;

uniform sampler2D uTextureId;
uniform int uTypeColor;

void main() {

    if (uTypeColor == 0){
        // Color
        outColor.rgb = vec3(color);
    } else if (uTypeColor == 1) {
        // Texture
        vec2 coord = mod(texCoord * vec2(2f, 4f), vec2(1f, 1f));

        vec4 baseColor = texture(uTextureId, coord);

        outColor.rgb = vec3(baseColor.rgb);
    }

    outColor.a = 1f;
}
