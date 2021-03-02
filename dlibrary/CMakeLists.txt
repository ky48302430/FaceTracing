cmake_minimum_required(VERSION 3.4.1)

# 添加 define  -DNO_CRYPTO
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

AUX_SOURCE_DIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp SRC_LIST)
add_library(
           native-lib
           SHARED
           ${SRC_LIST}
)


include_directories(src/main/cpp/include)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")

target_link_libraries(
        native-lib
        opencv_java3
        android
        # Links the target library to the log library
        log
)