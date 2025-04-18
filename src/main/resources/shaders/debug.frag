#version 330 core
in  vec2 vUV;
out vec4 FragColor;

void main()
{
    // 8×8 checkerboard in magenta/cyan
    vec2   c      = floor(vUV * 8.0);
    float  toggle = mod(c.x + c.y, 2.0);
    FragColor     = toggle > 0.5 ? vec4(1.0, 0.0, 1.0, 1.0)
    : vec4(0.0, 1.0, 1.0, 1.0);
}
