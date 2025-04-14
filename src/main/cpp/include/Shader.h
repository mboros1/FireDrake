//
// Created by martin on 11/26/20.
//

#ifndef OPENGLFROMSCRATCH_SHADER_H
#define OPENGLFROMSCRATCH_SHADER_H

#include <string>
#include <glad/glad.h>
#include <glm/vec3.hpp>
#include "Camera.h"

class Shader {
public:
    Shader()=default;
    Shader(const char* vertPath, const char* fragPath);

    Shader(const char *string);

    inline void bind() const {glUseProgram(programID);}
    inline void unbind() const {glUseProgram(0);}
    inline GLuint get() const {return programID;}

    void setFloat(const char *name, float data);
    void setInt(const char *name, int data);
    void setVec3(std::string name, glm::vec3 data);

    void setMat4(const char* name, glm::mat4 data);

private:
    unsigned int compileShader(const char* path, unsigned int shaderType);

    GLuint programID;
};


#endif //OPENGLFROMSCRATCH_SHADER_H
