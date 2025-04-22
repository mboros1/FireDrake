// debug.frag
#version 330 core
in vec4 vertexColor;  // Input from vertex shader
out vec4 FragColor;   // Output color

void main() {
    // Just use the color passed from vertex shader
    FragColor = vertexColor;
}
