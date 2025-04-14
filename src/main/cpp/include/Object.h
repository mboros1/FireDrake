//
// Created by martin on 11/26/20.
//

#ifndef OPENGLFROMSCRATCH_OBJECT_H
#define OPENGLFROMSCRATCH_OBJECT_H

#include <filesystem>
#include "stb_image.h"
#include "Shader.h"
#include <glad/glad.h>
#include <iostream>
#include <SDL2/SDL_timer.h>
#include <glm/glm.hpp>
#include <glm/ext.hpp>

class Object {
public:
    Object()=default;

    void Draw();

private:
    unsigned int VBO, VAO;

};


#endif //OPENGLFROMSCRATCH_OBJECT_H
