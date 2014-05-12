APP_PROJECT_PATH := $(call my-dir)
APP_ABI          := armeabi armeabi-v7a
APP_MODULES      := tcvm
APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/Android.mk
APP_OPTIM        := release
APP_CFLAGS       += -O3 -fpeel-loops -fprefetch-loop-arrays
