#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

void preprocessImage(const uint8_t* inputData, int width, int height);

#ifdef __cplusplus
}
#endif

#endif // IMAGE_PROCESSOR_H
