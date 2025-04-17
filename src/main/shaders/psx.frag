#version 330
in  vec2 vUV;
out vec4 frag;

uniform sampler2D uTex;

void main() {
    frag = texture(uTex, vUV);
}
