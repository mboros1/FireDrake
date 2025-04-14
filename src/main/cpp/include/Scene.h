//
// Created by martin on 11/26/20.
//

#ifndef OPENGLFROMSCRATCH_SCENE_H
#define OPENGLFROMSCRATCH_SCENE_H

#include <vector>
#include <unordered_map>
#include "Object.h"
#include "Window.h"
#include "Camera.h"
#include "Model.h"
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include "Text.h"

#include <ft2build.h>
#include FT_FREETYPE_H
#include <freetype/freetype.h>

class Scene {
public:
    void render();

    Camera camera;

    inline void addObject(Object& object) {objects.push_back(object);}
    inline void addModel(Model& model) {models.push_back(model);}

    void setModelPosition(int model_index, std::vector<glm::vec3>& positions);

    Shader geometryShader;
    Shader lightShader;
    Shader lightCubeShader;
    Shader debugShader;
    Shader forwardRenderShader;
    Shader textShader;
    Shader computeShader;

    void configureGBuffer();

    void configureLighting(int numLights);

    void addLight();
    void removeLight();

    size_t numLights();

    bool debug = false;
    int debug_buffer=0;
    bool forward_render = true;
    bool compute = false;
    bool tiles = false;

    void configureText();

    Text text;

    void renderHUD(float framerate);

private:
    // OpenGL ID's for gBuffer
    unsigned int gBuffer, gPosition, gNormal, gAlbedoSpec;
    unsigned int rboDepth;
    unsigned int quadVAO=0, quadVBO;
    unsigned int attachments[3];

    unsigned int tex_output = 0;
    unsigned int posImage;

    std::vector<Object> objects;
    std::vector<Model> models;
    std::unordered_map<std::string, std::vector<glm::vec3>> modelPositions{};
    std::vector<glm::vec3> lightPositions{};
    std::vector<glm::vec3> lightColors{};

    static void genFramebufferTexture(unsigned int &bufferId, unsigned int colorAttachmentId);

    void renderQuad();

    void renderLightCubes(glm::mat4 &view, glm::mat4 &projection);

    void configLightShaders(Shader& shader);

    void renderModels(glm::mat4 &view, glm::mat4 &projection);

    void deferredRenderLighting();

};


#endif //OPENGLFROMSCRATCH_SCENE_H
