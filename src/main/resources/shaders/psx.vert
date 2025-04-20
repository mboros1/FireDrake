#version 330 core
layout (location = 0) in vec3 aPos;   // the position variable has attribute position 0
layout (location = 1) in vec3 aNormal; // the normal variable has attribute position 1

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 vertexColor; // specify a color output to the fragment shader

void main() {
    // Transform the vertex position with model-view-projection matrices if provided
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(aPos, 1.0);
    
    // Use the normal as color (convert from [-1,1] to [0,1] range)
    vec3 normalColor = aNormal * 0.5 + 0.5;
    vertexColor = vec4(normalColor, 1.0);
}