#version 330
in  vec2 vUV;
out vec4 frag;

uniform sampler2D uScene;

float bayer(vec2 p) {
    int x = int(mod(p.x,4));
    int y = int(mod(p.y,4));
    int index = y*4 + x;
    int[16] t = int[16]( 0,  8, 2,10,
    12, 4,14, 6,
    3,11, 1, 9,
    15, 7,13, 5);
    return float(t[index]) / 16.0;
}

void main() {
    vec3 c = texture(uScene, vUV).rgb;
    float d  = bayer(gl_FragCoord.xy);
    c = floor(c * 31.0 + d) / 31.0;   // 5‑bit colour + dithering
    frag = vec4(c, 1.0);
}
