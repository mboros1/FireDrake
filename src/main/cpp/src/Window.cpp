//
// Created by martin on 11/26/20.
//

#include <glad/glad.h>
#include "Window.h"

Window::Window(float w, float h) {
    if (SDL_Init(SDL_INIT_VIDEO) < 0){
        SDL_LogError(SDL_LOG_CATEGORY_ERROR, "SDL could not initialise: %s", SDL_GetError());
        exit(EXIT_FAILURE);
    }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1);
    SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 24);

    window = SDL_CreateWindow("My Final Project",
                              SDL_WINDOWPOS_CENTERED,
                              SDL_WINDOWPOS_CENTERED,
                              w, h, SDL_WINDOW_OPENGL);

    if (!window){
        SDL_LogError(SDL_LOG_CATEGORY_ERROR, "Window could not be created: %s", SDL_GetError());
        exit(EXIT_FAILURE);
    }

    context = SDL_GL_CreateContext(window);
    if (!context){
        SDL_LogError(SDL_LOG_CATEGORY_ERROR, "Failed to initialize OpenGL: %s", SDL_GetError());
        exit(EXIT_FAILURE);
    }

    if(!gladLoadGLLoader(SDL_GL_GetProcAddress)){
        SDL_LogError(SDL_LOG_CATEGORY_ERROR, "Failed to iniitalize GLAD: %s", SDL_GetError());
    }

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_TEXTURE_2D);
}

Window::Window():Window(WINDOW_WIDTH, WINDOW_HEIGHT){};

Window::~Window() {
    if (window){
        SDL_DestroyWindow(window);
        SDL_Quit();
    }
}

void Window::updateFrameRate() {
    const int frame_count = 30;
    frames++;
    ticks.push_back(SDL_GetTicks() / 1000.0f);
    if (ticks.size() < frame_count){
        framerate = 0.0f;
        return;
    }
    while (ticks.size() > frame_count){
        ticks.pop_front();
    }

    auto t0 = ticks.front();
    auto t1 = ticks.back();
    auto t = t1 - t0;

    framerate = ticks.size() / t;

//    if (frames %  frame_count== 0){
//        SDL_Log("Framerate: %f", framerate);
//    }

}
