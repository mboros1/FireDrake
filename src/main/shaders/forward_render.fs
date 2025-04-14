#version 460 core
in vec2 TexCoords;
in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D texture_diffuse1;
uniform sampler2D texture_specular1;

struct Light {
    vec3 Position;
    vec3 Color;

    float Linear;
    float Quadratic;
};
const int MAX_LIGHTS = 1000;
uniform int NR_LIGHTS;
uniform Light lights[MAX_LIGHTS];
uniform vec3 viewPos;

void main()
{
    // retrieve data from gbuffer
    vec3 normal = normalize(Normal);
    vec3 Diffuse = texture(texture_diffuse1, TexCoords).rgb;
    float Specular = texture(texture_specular1, TexCoords).r;

    // then calculate lighting as usual
    vec3 lighting  = Diffuse * 0.3; // hard-coded ambient component
    vec3 viewDir  = normalize(viewPos - FragPos);
    for(int i = 0; i < NR_LIGHTS; ++i)
    {
        // diffuse
        vec3 lightDir = normalize(lights[i].Position - FragPos);
        vec3 diffuse = max(dot(normal, lightDir), 0.0) * Diffuse * lights[i].Color;
        // specular
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(normal, halfwayDir), 0.0), 16.0);
        vec3 specular = lights[i].Color * spec * Specular;
        // attenuation
        float distance = length(lights[i].Position - FragPos);
        float attenuation = 1.0 / (1.0 + lights[i].Linear * distance + lights[i].Quadratic * distance * distance);
        diffuse *= attenuation;
        specular *= attenuation;
        lighting += diffuse + specular;
    }
    FragColor = vec4(lighting, 1.0);
}
