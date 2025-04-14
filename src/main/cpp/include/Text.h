//
// Created by martin on 12/5/20.
//

#ifndef OPENGLFROMSCRATCH_TEXT_H
#define OPENGLFROMSCRATCH_TEXT_H

#include "Shader.h"
#include <string>
#include "glm/glm.hpp"
#include <glm/ext.hpp>
#include <map>
#include "Window.h"

/// Holds all state information relevant to a character as loaded using FreeType
struct Character {
    unsigned int TextureID; // ID handle of the glyph texture
    glm::ivec2   Size;      // Size of glyph
    glm::ivec2   Bearing;   // Offset from baseline to left/top of glyph
    unsigned int Advance;   // Horizontal offset to advance to next glyph
};

class Text {
    std::map<GLchar, Character> Characters;
public:
    void RenderText(Shader &shader, std::string text, float x, float y, float scale, glm::vec3 color);
    void addCharacter(std::pair<char, Character> character) {Characters.insert(character); }
    unsigned int VAO;
    unsigned int VBO;
};


#endif //OPENGLFROMSCRATCH_TEXT_H
