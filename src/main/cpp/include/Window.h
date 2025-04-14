//
// Created by martin on 11/26/20.
//

#ifndef OPENGLFROMSCRATCH_WINDOW_H
#define OPENGLFROMSCRATCH_WINDOW_H

#include <SDL2/SDL.h>
#include <vector>
#include <list>

#define WINDOW_WIDTH 1200.0f
#define WINDOW_HEIGHT 800.0f

class Window {
public:
    Window();
    Window(float w, float h);
    ~Window();

    inline SDL_Window* get() {return window;}

    void updateFrameRate();

    float framerate = 0.0f;
private:
    SDL_Window* window;
    SDL_GLContext context;
    size_t frames = 0;
    std::list<float> ticks;
};


#endif //OPENGLFROMSCRATCH_WINDOW_H
