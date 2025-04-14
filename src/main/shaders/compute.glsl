#version 460 core

#define WORK_GROUP_SIZE 16
// TODO: pass window dimensions as uniform rather then hardcode
#define WINDOW_WIDTH 1200
#define WINDOW_HEIGHT 800

layout(rgba16f, binding = 0) uniform writeonly  image2D destTex;
layout(rgba16f, binding = 1) uniform readonly image2D gPosition;
layout(rgba16f, binding = 2) uniform readonly image2D gNormal;
layout(rgba16f, binding = 3) uniform readonly image2D gAlbedoSpec;

layout (local_size_x=WORK_GROUP_SIZE, local_size_y =WORK_GROUP_SIZE) in;

struct Light {
    vec3 Position;
    vec3 Color;

    float Linear;
    float Quadratic;
    float Radius;
};

const int MAX_LIGHTS = 1000;
const int MAX_LIGHTS_PER_TILE = 40;
uniform int NR_LIGHTS;
uniform Light lights[MAX_LIGHTS];
uniform vec3 viewPos;
uniform int showTiles;

shared uint pointLightIndex[MAX_LIGHTS];
shared uint pointLightCount = 0;

void main() {
    ivec2 TexCoords = ivec2(gl_GlobalInvocationID.xy);

    vec3 FragPos = imageLoad(gPosition, TexCoords).rgb;
    vec3 Normal = imageLoad(gNormal, TexCoords).rgb;
    vec3 Diffuse = imageLoad(gAlbedoSpec, TexCoords).rgb;
    float Specular = imageLoad(gAlbedoSpec, TexCoords).a;

    vec3 lighting  = Diffuse * 0.3; // hard-coded ambient component
    vec3 viewDir  = normalize(viewPos - FragPos);

    int threadsPerTile = WORK_GROUP_SIZE * WORK_GROUP_SIZE;

    for(uint i = 0; i < NR_LIGHTS; i+= threadsPerTile)
    {
        uint il = gl_LocalInvocationIndex + i;
        if (il < NR_LIGHTS){
            Light p = lights[il];
            float r = p.Radius;
            vec3 pos = p.Position;
            if (distance(pos, FragPos) < r) {
                uint id = atomicAdd(pointLightCount, 1);
                pointLightIndex[id] = il;
            }
        }
    }
    barrier();

    for(uint i = 0; i < pointLightCount; ++i){
        Light p = lights[pointLightIndex[i]];
        float distance = length(p.Position - FragPos);
        // diffuse
        vec3 lightDir = normalize(p.Position - FragPos);
        vec3 diffuse = max(dot(Normal, lightDir), 0.0) * Diffuse * p.Color;
        // specular
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(Normal, halfwayDir), 0.0), 16.0);
        vec3 specular = p.Color * spec * Specular;
        // attenuation
        float attenuation = 1.0 / (1.0 + p.Linear * distance + p.Quadratic * distance * distance);
        diffuse *= attenuation;
        specular *= attenuation;
        lighting += diffuse + specular;
    }
    barrier();

    if (showTiles == 1) {
        if (gl_LocalInvocationID.x == 0 || gl_LocalInvocationID.y == 0 || gl_LocalInvocationID.x == 16 || gl_LocalInvocationID.y == 16){
            imageStore(destTex, TexCoords, vec4(0.2, 0.2, 0.2, 0.2));
        }
        else
        {
            imageStore(destTex, TexCoords, vec4(lighting, 1.0));
        }
    } else {
        imageStore(destTex, TexCoords, vec4(lighting, 1.0));
    }
}