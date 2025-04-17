#version 330
layout(location=0) in vec3 aPos;
layout(location=1) in vec2 aUV;

uniform mat4 model, view, proj;

out vec2 vUV;

void main() {
    vUV = aUV;                // affine: no div by w later
    gl_Position = proj * view * model * vec4(aPos, 1.0);
}
