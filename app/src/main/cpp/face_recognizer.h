#ifndef FACE_RECOGNIZER_H
#define FACE_RECOGNIZER_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

bool init_face_model(const char* model_path);
bool get_face_embedding(const float* input, int input_len, float* output, int output_len);
void close_face_model();

#ifdef __cplusplus
}
#endif

#endif // FACE_RECOGNIZER_H
