#include <Window.h>
#include <InputHandler.h>
#include "Scene.h"


#include <Model.h>

#define NR_LIGHTS 10

std::vector<glm::vec3> genModelPositions(int depth);

int main() {
    if (std::filesystem::current_path().string().ends_with("cmake-build-debug")){
        std::filesystem::current_path("..");
    }
    std::cout << std::filesystem::current_path() << '\n';
    // Create SDL2 Window; set up events and text input
    Window window{}; SDL_Event e; SDL_StartTextInput();

    // Configure scene;
    Scene scene;

    Shader geometryShader{"shaders/g_buffer.vs", "shaders/g_buffer.fs"};
    Shader lightShader{"shaders/light_buffer.vs", "shaders/light_buffer.fs"};
    Shader lightCubeShader{"shaders/light_cube.vs", "shaders/light_cube.fs"};
    Shader debugShader("shaders/fbo_debug.vs", "shaders/fbo_debug.fs");
    Shader forwardRenderShader("shaders/forward_render.vs", "shaders/forward_render.fs");
    Shader textShader("shaders/text.vs", "shaders/text.fs");
    Shader computeShader("shaders/compute.glsl");

    scene.geometryShader = geometryShader;
    scene.lightShader = lightShader;
    scene.lightCubeShader = lightCubeShader;
    scene.debugShader = debugShader;
    scene.forwardRenderShader = forwardRenderShader;
    scene.textShader = textShader;
    scene.computeShader = computeShader;

    // importing backpack model and generating light cubes
    Model backpackModel{"resources/objects/backpack/backpack.obj"};
    scene.addModel( backpackModel);

    Object obj{};
    scene.addObject(obj);

    std::vector<glm::vec3> modelPositions = genModelPositions(3);

    scene.setModelPosition(0, modelPositions);

    // for deferred rendering
    scene.configureGBuffer();
    scene.configureLighting(NR_LIGHTS);
    scene.configureText();

    // main program loop
    while(true){
        InputHandler::handleEvent(e, scene);

        scene.render();
        scene.renderHUD(window.framerate);
        SDL_GL_SwapWindow(window.get());
        window.updateFrameRate();
    }
}

std::vector<glm::vec3> genModelPositions(int depth) {
    std::vector<glm::vec3> modelPositions;

        for(int i = 1; i <= depth; ++i){
            auto offset = static_cast<float>(i);
            modelPositions.emplace_back(-2.0f, 2.0f, -5.0f*offset);
            modelPositions.emplace_back(2.0f, 2.0f, -5.0f*offset);
            modelPositions.emplace_back(-2.0f, -2.0f, -5.0f*offset);
            modelPositions.emplace_back(2.0f, -2.0f, -5.0f*offset);
        }
    return modelPositions;
}

