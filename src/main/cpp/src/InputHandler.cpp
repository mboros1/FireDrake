//
// Created by martin on 11/26/20.
//

#include "InputHandler.h"

void InputHandler::handleEvent(SDL_Event &e, Scene& scene) {
    Camera& camera = scene.camera;
    while(SDL_PollEvent(&e)){
        if (e.type == SDL_QUIT){
            exit(EXIT_SUCCESS);
        }else if (e.type==SDL_MOUSEMOTION) {
            camera.mouseMove(e.motion.xrel, e.motion.yrel);
        } else if (e.type == SDL_MOUSEWHEEL) {
            camera.mouseWheel(e.wheel.x, e.wheel.y);
        } else if (e.type == SDL_KEYDOWN) {
            handleKeyDown(e.key.keysym.sym, scene);
        } else if (e.type == SDL_KEYUP) {
            handleKeyUp(e.key.keysym.sym, scene);
        }
    }
}

void InputHandler::handleKeyDown(SDL_Keycode key, Scene &scene) {
    Camera& camera = scene.camera;
    if (key == SDLK_ESCAPE)
        exit(EXIT_SUCCESS);
    else if (key == SDLK_w)
        camera.forward = true;
    else if (key == SDLK_a)
        camera.left = true;
    else if (key == SDLK_s)
        camera.backward = true;
    else if (key == SDLK_d)
        camera.right = true;
    else if (key == SDLK_LSHIFT)
        camera.holdMouse = false;
    else if (key == SDLK_UP){
        scene.addLight();
        std::cout << "Lights: " << scene.numLights() << '\n';
    }
    else if (key == SDLK_DOWN){
        scene.removeLight();
        std::cout << "Lights: " << scene.numLights() << '\n';
    }
    else if (key == SDLK_z){
        scene.debug = !scene.debug;
    }
    else if (key == SDLK_f){
        scene.debug_buffer = (scene.debug_buffer + 1) % 3;
    } else if (key == SDLK_x){
        scene.forward_render = !scene.forward_render;
    } else if (key == SDLK_c) {
        scene.compute = !scene.compute;
    } else if (key == SDLK_t) {
        scene.tiles = !scene.tiles;
    }
}

void InputHandler::handleKeyUp(SDL_Keycode key, Scene &scene) {
    Camera& camera = scene.camera;
    if (key == SDLK_w)
        camera.forward =false;
    else if (key == SDLK_a)
        camera.left =false;
    else if (key == SDLK_s)
        camera.backward =false;
    else if (key == SDLK_d)
        camera.right =false;
    else if (key == SDLK_LSHIFT)
        camera.holdMouse = true;
}
