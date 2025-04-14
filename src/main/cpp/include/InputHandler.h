//
// Created by martin on 11/26/20.
//

#ifndef OPENGLFROMSCRATCH_INPUTHANDLER_H
#define OPENGLFROMSCRATCH_INPUTHANDLER_H


#include <SDL2/SDL_events.h>
#include "Scene.h"

class InputHandler {
public:
    static void handleEvent(SDL_Event& e, Scene& scene);

    static void handleKeyDown(SDL_Keycode key, Scene &scene);

    static void handleKeyUp(SDL_Keycode key, Scene &scene);

    static void handleMouseMove(Sint32 xrel, Sint32 yrel, Camera &camera);
};


#endif //OPENGLFROMSCRATCH_INPUTHANDLER_H
