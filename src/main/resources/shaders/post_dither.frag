#version 330 core
in  vec2 vUV;
out vec4 frag;

uniform sampler2D uScene;

/* 4×4 Bayer matrix */
float bayer(vec2 p)
{
    int x = int(mod(p.x, 4.0));
    int y = int(mod(p.y, 4.0));
    int idx = y * 4 + x;
    int[16] t = int[16]( 0,  8,  2, 10,
    12,  4, 14,  6,
    3, 11,  1,  9,
    15,  7, 13,  5);
    return float(t[idx]) / 16.0;
}

void main()
{
    vec3 c = texture(uScene, vUV).rgb;
    float d = bayer(gl_FragCoord.xy);
    c = floor(c * 31.0 + d) / 31.0;
    frag = vec4(c, 1.0);
}
