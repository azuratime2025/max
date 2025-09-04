cmake_minimum_required(VERSION 3.10)
project(azura_face_lib)

set(CMAKE_CXX_STANDARD 17)

# Include current directory for header lookup
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

add_library(azura_face_lib SHARED
    jni_bridge.cpp
    face_recognizer.cpp
    image_processor.c
)

find_library(log-lib log)

target_link_libraries(azura_face_lib
    ${log-lib}
    atomic
    m
)
