// debug.vert
#version 330 core
layout (location = 0) in vec3 aPos;   // Position
layout (location = 1) in vec3 aColor; // Color

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 vertexColor; // Pass color to fragment shader

void main() {
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(aPos, 1.0);
    
    // Use the input color directly
    vertexColor = vec4(aColor, 1.0);
}
