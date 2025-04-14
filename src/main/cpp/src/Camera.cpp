//
// Created by martin on 11/29/20.
//

#include <glm/gtc/matrix_transform.hpp>
#include "Camera.h"

glm::mat4 Camera::view() {
    return glm::lookAt(pos,pos + front, up);
}

void Camera::keyboardMove() {
    glm::vec3 moveVector = glm::vec3(0.0f, 0.0f, 0.0f);
    if (left)
        moveVector -= glm::normalize(glm::cross(front, up));
    if (right)
        moveVector += glm::normalize(glm::cross(front, up));
    if (forward)
        moveVector += front;
    if (backward)
        moveVector -= front;

    if (moveVector.x || moveVector.y || moveVector.z){
        float cameraSpeed = speed * deltaTime;
        pos += glm::normalize(moveVector) * cameraSpeed;
    }
}

void Camera::updateTime(float currentFrame) {
    deltaTime = currentFrame - lastFrame;
    lastFrame = currentFrame;
}

void Camera::mouseMove(int xrel, int yrel) {
    if (firstMouse){
        firstMouse = false;
        return;
    }
    if (holdMouse){
        return;
    }
    glm::vec3 direction;

    yaw += xrel;
    pitch -= yrel;
    if (pitch > 89.0f) pitch = 89.0f;
    if (pitch < -89.0f) pitch = -89.0f;

    direction.x = cos(glm::radians(yaw)) * cos(glm::radians(pitch));
    direction.y = sin(glm::radians(pitch));
    direction.z = sin(glm::radians(yaw)) * cos(glm::radians(pitch));

    front = glm::normalize(direction);
}

void Camera::mouseWheel(int x, int y) {
    fov -= (float)y;
    if (fov < 1.0f) fov = 1.0f;
    if (fov > 75.0f) fov = 75.0f;
}

// TODO: position is always origin now, maybe make it adjustable later
glm::vec3 Camera::position() {
    return glm::vec3{0.0f, 0.0f, 0.0f};
}
