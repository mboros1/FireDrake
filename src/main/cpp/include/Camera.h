//
// Created by martin on 11/29/20.
//

#ifndef OPENGLFROMSCRATCH_CAMERA_H
#define OPENGLFROMSCRATCH_CAMERA_H


#include <glm/glm.hpp>

class Camera {
private:
    glm::vec3 pos = glm::vec3(0.0f, 0.0f, 3.0f);
    glm::vec3  front = glm::vec3(0.0f, 0.0f, -1.0f);
    glm::vec3  up = glm::vec3(0.0f, 1.0f, 0.0f);

    float deltaTime = 0.0f;
    float lastFrame = 0.0f;

    const float speed = 0.01f;

    float yaw = -90.0f;
    float pitch = 0.0f;

    float mouseSensitivity = 0.1f;
    bool firstMouse = true;

public:
    float fov = 45.0f;
    bool forward = false, backward = false, left = false, right = false;
    bool holdMouse = true;
    glm::mat4 view();

    // move based on keyboard input
    void keyboardMove();

    void updateTime(float currentFrame);

    // move based on mouse input
    void mouseMove(int xrel, int yrel);

    void mouseWheel(int x, int y);

    glm::vec3 position();
};


#endif //OPENGLFROMSCRATCH_CAMERA_H
