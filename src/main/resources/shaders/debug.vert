#version 330 core
layout(location = 0) in vec3 aPos;   // Fullscreen quad position
layout(location = 1) in vec2 aUV;    // Fullscreen quad UVs

out vec2 vUV;

void main()
{
    gl_Position = vec4(aPos.xy, 0.0, 1.0); // already clip‑space
    vUV = aUV;
}
