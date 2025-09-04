#include "image_processor.h"
#include <android/log.h>
#include <stddef.h> // ✅ untuk NULL

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

void preprocessImage(const uint8_t* inputData, int width, int height) {
    if (inputData == NULL || width <= 0 || height <= 0) {
        LOGE("Invalid image data or dimensions");
        return;
    }

    LOGI("Preprocessing image: width=%d, height=%d", width, height);
    // You can add actual preprocessing here
}
