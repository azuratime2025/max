#include "face_recognizer.h"
#include <android/log.h>

#define LOG_TAG "FaceRecognizer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

bool init_face_model(const char* model_path) {
    LOGI("init_face_model() called with path: %s", model_path);
    return true;
}

bool get_face_embedding(const float* input, int input_len, float* output, int output_len) {
    LOGI("get_face_embedding() dummy run");
    for (int i = 0; i < output_len; i++) {
        output[i] = 0.01f * i; // dummy data
    }
    return true;
}

void close_face_model() {
    LOGI("close_face_model() called");
}
