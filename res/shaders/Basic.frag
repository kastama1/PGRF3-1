#version 330

in vec3 color;
in vec2 texCoord;

out vec4 outColor;

uniform sampler2D uTextureId;
uniform int uTypeColor;

void main() {

    if (uTypeColor == 0){
        outColor = vec4(color, 1.0);
    } else if (uTypeColor == 1) {
        vec2 coord = mod(texCoord * vec2(2.0, 4.0), vec2(1.0, 1.0));

        vec4 baseColor = texture(uTextureId, coord);

        outColor = vec4(baseColor.rgb, 1.0);
    }
}
