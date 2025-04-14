//
// Created by martin on 11/26/20.
//

#include <Shader.h>
#include <fstream>
#include <sstream>
#include <iostream>
#include <Object.h>

Shader::Shader(const char* vertPath, const char* fragPath) {
    programID = glCreateProgram();
    unsigned int vertex = compileShader(vertPath, GL_VERTEX_SHADER);
    unsigned int frag = compileShader(fragPath, GL_FRAGMENT_SHADER);

    glAttachShader(programID, vertex);
    glAttachShader(programID, frag);
    glLinkProgram(programID);

    GLint success;
    glGetProgramiv(programID, GL_LINK_STATUS, &success);
    if (!success){
        char logBuffer[512];
        glGetProgramInfoLog(programID, 512, NULL, logBuffer);
        std::cerr << "ERROR: shader program failed to link\n" << logBuffer << '\n';
    }

    glDeleteShader(vertex); glDeleteShader(frag);
}

unsigned int Shader::compileShader(const char* path, unsigned int shaderType){
    std::ifstream shaderFile;
    std::stringstream shaderStream;

    shaderFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);

    shaderFile.open(path);

    try {
        shaderStream << shaderFile.rdbuf();
        shaderFile.close();
    } catch(std::ifstream::failure e) {
        std::cerr << "ERROR: shader file failed to read\n";
    }

    std::string code = shaderStream.str();
    const char* shaderCode = code.c_str();

    unsigned int shader;
    int success;

    shader = glCreateShader(shaderType);
    glShaderSource(shader, 1, &shaderCode, NULL);
    glCompileShader(shader);

    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success){
        char logBuffer[512];
        glGetShaderInfoLog(shader, 512, NULL, logBuffer);
        std::cerr << "Shader failed to compile\n" << logBuffer << '\n';
    }
    return shader;
}

void Shader::setFloat(const char *name, float data) {
    glUniform1f(glGetUniformLocation(get(), name), data);
}

void Shader::setInt(const char *name, int data) {
    glUniform1i(glGetUniformLocation(get(), name), data);
}

void Shader::setVec3(std::string name, glm::vec3 data) {
    glUniform3fv(glGetUniformLocation(get(), name.c_str()), 1, glm::value_ptr(data));
}

void Shader::setMat4(const char *name, glm::mat4 data) {
    glUniformMatrix4fv(glGetUniformLocation(get(), name), 1, GL_FALSE, glm::value_ptr(data));
}

Shader::Shader(const char *compute_path) {
    programID = glCreateProgram();
    unsigned int shader = compileShader(compute_path, GL_COMPUTE_SHADER);

    glAttachShader(programID, shader);
    glLinkProgram(programID);

    GLint success;
    glGetProgramiv(programID, GL_LINK_STATUS, &success);
    if (!success){
        char logBuffer[512];
        glGetProgramInfoLog(programID, 512, NULL, logBuffer);
        std::cerr << "ERROR: shader program failed to link\n" << logBuffer << '\n';
    }

    glDeleteShader(shader);
}
