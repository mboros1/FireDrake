//
// Created by martin on 11/30/20.
//

#ifndef OPENGLFROMSCRATCH_MODEL_H
#define OPENGLFROMSCRATCH_MODEL_H


#include <vector>
#include "Shader.h"
#include "Mesh.h"
#include <assimp/scene.h>
#include <assimp/Importer.hpp>
#include <assimp/postprocess.h>
#include <memory>
#include <iostream>
#include <stb_image.h>
#include <glm/ext.hpp>

class Model {
public:
    std::vector<Texture> textures_loaded;
    std::vector<Mesh> meshes;
    explicit Model(const char *path, bool gamma = false);

    void Draw(Shader& shader, glm::mat4 model, glm::mat4 view, glm::mat4 projection);

    // TODO: replace with a function that creates unique hash string
    std::string getPath() {return directory;}
private:

    std::string directory;
    bool gammaCorrection;

    void loadModel(std::string path);
    void processNode(aiNode *node, const aiScene* scene);
    std::vector<Texture> loadMaterialTextures(aiMaterial* mat, aiTextureType type, std::string typeName);

    Mesh processMesh(aiMesh *pMesh, const aiScene *pScene);

    static unsigned int TextureFromFile(const char *path, const std::string &directory, bool gamma = false);
};


#endif //OPENGLFROMSCRATCH_MODEL_H
