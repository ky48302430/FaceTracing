//
// Created by MS on 2020/11/22.
//
#ifndef FACETRACING_LM_ANDROIDLOG_H
#define FACETRACING_LM_ANDROIDLOG_H
#include <android/log.h>


#define TAG "JNI_TAG"
#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,TAG,FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);
#endif //FACETRACING_LM_ANDROIDLOG_H
