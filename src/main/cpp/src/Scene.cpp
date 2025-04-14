//
// Created by martin on 11/26/20.
//

#include <Text.h>
#include "Scene.h"

#define GLCall(x) GLClearErrorStates(); x ; GLCheckError(#x,__LINE__);

// New error handling routine
static void GLClearErrorStates(){
    // Return all of the errors that occur.
    while(glGetError() != GL_NO_ERROR){
        ;
    }
}

// Returns false if an error occurs
static bool GLCheckError(const char* function, int line){
    while(GLenum error = glGetError()){
        // __LINE__ is a special preprocessor macro that
        // Can tell us what line any errors occurred on.
        std::cout << "[OpenGL Error]" << error << "|" << function << " Line: " << line << "\n";
        return false;
    }
    return true;
}

void Scene::render() {
    // TODO: get view port from SDL window
    glViewport(0,0, WINDOW_WIDTH, WINDOW_HEIGHT);
//    glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    camera.updateTime(SDL_GetTicks());


    // geometry pass
    if (!forward_render){
        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);
    }
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glm::mat4 view;
    view = camera.view();

    glm::mat4 projection;
    projection = glm::perspective(glm::radians(camera.fov), WINDOW_WIDTH/WINDOW_HEIGHT, 0.1f, 100.0f);


    renderModels(view, projection);

    if (forward_render){
        configLightShaders(forwardRenderShader);
    } else {
        deferredRenderLighting();
    }

    if (!debug){
        renderLightCubes(view, projection);
    }
    camera.keyboardMove();
}

void Scene::deferredRenderLighting() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    if (debug)
        debugShader.bind();
    else
        lightShader.bind();

    if (debug){
        if (debug_buffer == 0){
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, gPosition);
        } else if (debug_buffer == 1) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, gNormal);
        } else if (debug_buffer == 2){
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, gAlbedoSpec);
        }
    } else {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, gPosition);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, gNormal);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, gAlbedoSpec);
    }

    if (!debug){
        if (compute){
            computeShader.bind();
            configLightShaders(computeShader);
            if (tex_output == 0){
                glGenTextures(1, &tex_output);
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, tex_output);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGBA, GL_FLOAT, NULL);
                glBindImageTexture(0, tex_output, 0, GL_FALSE, 0, GL_WRITE_ONLY, GL_RGBA16F);
                glUniform1i(glGetUniformLocation(computeShader.get(), "destTex"), 0);

                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, gPosition);
                glBindImageTexture(1, gPosition, 0, GL_FALSE, 0, GL_READ_ONLY, GL_RGBA16F);
                glUniform1i(glGetUniformLocation(computeShader.get(), "gPosition"), 1);

                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, gNormal);
                glBindImageTexture(2, gNormal, 0, GL_FALSE, 0, GL_READ_ONLY, GL_RGBA16F);
                glUniform1i(glGetUniformLocation(computeShader.get(), "gNormal"), 2);

                glActiveTexture(GL_TEXTURE3);
                glBindTexture(GL_TEXTURE_2D, gAlbedoSpec);
                glBindImageTexture(3, gAlbedoSpec, 0, GL_FALSE, 0, GL_READ_ONLY, GL_RGBA16F);
                glUniform1i(glGetUniformLocation(computeShader.get(), "gAlbedoSpec"), 3);


            } else {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, tex_output);
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, gPosition);
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, gNormal);
                glActiveTexture(GL_TEXTURE3);
                glBindTexture(GL_TEXTURE_2D, gAlbedoSpec);
                if (tiles){
                    glUniform1i(glGetUniformLocation(computeShader.get(), "showTiles"), 1);
                } else {
                    glUniform1i(glGetUniformLocation(computeShader.get(), "showTiles"), 0);
                }
            }
            glDispatchCompute(WINDOW_WIDTH/16, WINDOW_HEIGHT/16, 1);
            glMemoryBarrier(GL_COMPUTE_SHADER_BIT);
            debugShader.bind();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, tex_output);
        } else {
            configLightShaders(lightShader);
        }
    }
    renderQuad();

    glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    glBlitFramebuffer(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void Scene::renderModels(glm::mat4 &view, glm::mat4 &projection) {
    for(auto& obj : models){
        for(auto& modelPosition : modelPositions[obj.getPath()]){
            glm::mat4 model = glm::mat4(1.0f);
            model = glm::translate(model, modelPosition);
            float angle = 20.0f;

            model = glm::rotate(model, glm::radians(angle), glm::vec3(1.0f, 0.3f, 0.5f));
            if (forward_render){
                obj.Draw(forwardRenderShader, model, view, projection);
            } else {
                obj.Draw(geometryShader, model, view, projection);
            }
        }
    }
}

void Scene::configLightShaders(Shader& shader) {
    for(auto i = 0; i < lightPositions.size(); ++i){
        std:: string s = std::to_string(i);
        shader.setVec3("lights[" + s + "].Position", lightPositions[i]);
        shader.setVec3("lights[" + s + "].Color", lightColors[i]);

        const float constant = 1.0f;
        const float linear = 0.7f;
        const float quadratic = 1.0f;
        const float maxBrightness = std::fmaxf(std::fmaxf(lightColors[i].r, lightColors[i].g), lightColors[i].b);
        float radius = (-linear + std::sqrt(linear * linear - 4 * quadratic * (constant - (256.0f / 5.0f) * maxBrightness))) / (2.0f * quadratic);

        shader.setFloat(("lights[" + s + "].Linear").c_str(), linear);
        shader.setFloat(("lights[" + s + "].Quadratic").c_str(), quadratic);
        shader.setFloat(("lights[" + s + "].Radius").c_str(), radius);
    }
    shader.setVec3("viewPos", camera.position());
    shader.setInt("NR_LIGHTS", lightPositions.size());
}

void Scene::renderLightCubes(glm::mat4 &view, glm::mat4 &projection) {
    lightCubeShader.bind();
    lightCubeShader.setMat4("projection", projection);
    lightCubeShader.setMat4("view", view);

    for(auto i = 0; i < lightPositions.size(); ++i){
        glm::mat4 model = glm::mat4(1.0f);
        model = glm::translate(model, lightPositions.at(i));
        float angle = 20.0f; //ticks * 25.0f / 100.0f;
        model = glm::rotate(model, glm::radians(angle), glm::vec3(1.0f, 0.3f, 0.5f));
        model = glm::scale(model, glm::vec3(0.125f));
        lightCubeShader.setMat4("model", model);
        lightCubeShader.setVec3("color", lightColors.at(i));
        objects.at(0).Draw();
    }
}


void Scene::setModelPosition(int model_index, std::vector<glm::vec3>& positions) {
    modelPositions[models.at(model_index).getPath()] = positions;
}

void Scene::configureGBuffer() {

    glGenFramebuffers(1, &gBuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);

    attachments[0] =GL_COLOR_ATTACHMENT0;
    attachments[1] =GL_COLOR_ATTACHMENT1;
    attachments[2] =GL_COLOR_ATTACHMENT2;

    genFramebufferTexture(gPosition, attachments[0]);
    genFramebufferTexture(gNormal, attachments[1]);
    genFramebufferTexture(gAlbedoSpec, attachments[2]);

    glDrawBuffers(3, attachments);

    glGenRenderbuffers(1, &rboDepth);
    glBindRenderbuffer(GL_RENDERBUFFER, rboDepth);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, WINDOW_WIDTH, WINDOW_HEIGHT);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
        std::cerr << "ERROR: Framebuffer not complete\n";
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void Scene::genFramebufferTexture(unsigned int &bufferId, unsigned int colorAttachmentId) {
    glGenTextures(1, &bufferId);
    glBindTexture(GL_TEXTURE_2D, bufferId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGBA, GL_FLOAT, nullptr);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glFramebufferTexture2D(GL_FRAMEBUFFER, colorAttachmentId, GL_TEXTURE_2D, bufferId, 0);
}

void Scene::configureLighting(int numLights) {
    srand(13);
    for (unsigned int i = 0; i < numLights; i++)
    {
        addLight();
    }

    lightShader.bind();
    lightShader.setInt("gPosition", 0);
    lightShader.setInt("gNormal", 1);
    lightShader.setInt("gAlbedoSpec", 2);
    lightShader.unbind();
}

void Scene::addLight(){
    if (lightPositions.size() >= 1000)
        return;
    // calculate slightly random offsets
    float xPos = ((rand() % 100) / 100.0) * 18.0 - 9.0;
    float yPos = ((rand() % 100) / 100.0) * 18.0 - 9.0;
    float zPos = ((rand() % 100) / 100.0) * -30.0;
    lightPositions.emplace_back(xPos, yPos, zPos);
    // also calculate random color
    float rColor = ((rand() % 100) / 200.0f) + 0.5; // between 0.5 and 1.0
    float gColor = ((rand() % 100) / 200.0f) + 0.5; // between 0.5 and 1.0
    float bColor = ((rand() % 100) / 200.0f) + 0.5; // between 0.5 and 1.0
    lightColors.emplace_back(rColor, gColor, bColor);
}

void Scene::renderQuad() {
    if (quadVAO == 0)
    {
        float quadVertices[] = {
                // positions        // texture Coords
                -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        };
        // setup plane VAO
        glGenVertexArrays(1, &quadVAO);
        glGenBuffers(1, &quadVBO);
        glBindVertexArray(quadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), &quadVertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), nullptr);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)(3 * sizeof(float)));
    }
    glBindVertexArray(quadVAO);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glBindVertexArray(0);
}

void Scene::removeLight() {
    if (lightPositions.size() > 1){
        lightPositions.pop_back();
        lightColors.pop_back();
    }
}

size_t Scene::numLights() {
    return lightPositions.size();
}

void Scene::configureText() {
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(WINDOW_WIDTH), 0.0f, static_cast<float>(WINDOW_HEIGHT));
    textShader.bind();
    glUniformMatrix4fv(glGetUniformLocation(textShader.get(), "projection"), 1, GL_FALSE, glm::value_ptr(projection));

    // FreeType
    // --------
    FT_Library ft;
    // All functions return a value different than 0 whenever an error occurred
    if (FT_Init_FreeType(&ft))
    {
        std::cerr << "ERROR::FREETYPE: Could not init FreeType Library" << std::endl;
        return;
    }

    // find path to font
    std::string font_name = "resources/fonts/Antonio-Bold.ttf";
    if (font_name.empty())
    {
        std::cerr << "ERROR::FREETYPE: Failed to load font_name" << std::endl;
        return;
    }

    // load font as face
    FT_Face face;
    if (FT_New_Face(ft, font_name.c_str(), 0, &face)) {
        std::cerr << "ERROR::FREETYPE: Failed to load font" << std::endl;
        return;
    }
    else {
        // set size to load glyphs as
        FT_Set_Pixel_Sizes(face, 0, 48);

        // disable byte-alignment restriction
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // load first 128 characters of ASCII set
        for (unsigned char c = 0; c < 128; c++)
        {
            // Load character glyph
            if (FT_Load_Char(face, c, FT_LOAD_RENDER))
            {
                std::cout << "ERROR::FREETYTPE: Failed to load Glyph" << std::endl;
                continue;
            }
            // generate texture
            unsigned int texture;
            glGenTextures(1, &texture);
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RED,
                    face->glyph->bitmap.width,
                    face->glyph->bitmap.rows,
                    0,
                    GL_RED,
                    GL_UNSIGNED_BYTE,
                    face->glyph->bitmap.buffer
            );
            // set texture options
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            // now store character for later use
            Character character = {
                    texture,
                    glm::ivec2(face->glyph->bitmap.width, face->glyph->bitmap.rows),
                    glm::ivec2(face->glyph->bitmap_left, face->glyph->bitmap_top),
                    static_cast<unsigned int>(face->glyph->advance.x)
            };
            text.addCharacter(std::pair<char, Character>(c, character));
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    // destroy FreeType once we're finished
    FT_Done_Face(face);
    FT_Done_FreeType(ft);


    // configure VAO/VBO for texture quads
    // -----------------------------------
    glGenVertexArrays(1, &(text.VAO));
    glGenBuffers(1, &(text.VBO));
    glBindVertexArray(text.VAO);
    glBindBuffer(GL_ARRAY_BUFFER, text.VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float) * 6 * 4, NULL, GL_DYNAMIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), 0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    glDisable(GL_CULL_FACE);
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);
}

void Scene::renderHUD(float framerate) {
    char *s = new char[50];

    if (forward_render){
        sprintf(s, "Forward rendering");
        text.RenderText(textShader, s, 25.0f, 85.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));
    } else if (compute) {
        sprintf(s, "Tile based deferred rendering");
        text.RenderText(textShader, s, 25.0f, 85.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));
    } else {
        sprintf(s, "Deferred rendering");
        text.RenderText(textShader, s, 25.0f, 85.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));
    }

    sprintf(s, "Framerate: %.1f", framerate);
    text.RenderText(textShader, s, 25.0f, 55.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));

    if (debug){
        if (debug_buffer == 0){
            sprintf(s, "Debug mode: lighting off: Geometry");
        } else if (debug_buffer == 1) {
            sprintf(s, "Debug mode: lighting off: Normal");
        } else if (debug_buffer == 2) {
            sprintf(s, "Debug mode: lighting off: Albedo/Spec");
        } else {
            sprintf(s, "shouldn't get here");
        }
        text.RenderText(textShader, s, 25.0f, 25.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));
    } else {
        sprintf(s, "Lights: %zu", lightPositions.size());
        text.RenderText(textShader, s, 25.0f, 25.0f, 0.5f, glm::vec3(0.5, 0.8f, 0.2f));
    }
    delete[] s;
}

