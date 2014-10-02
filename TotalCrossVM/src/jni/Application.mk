APP_PROJECT_PATH := $(call my-dir)
APP_ABI          := armeabi
APP_MODULES      := tcvm
APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/Android.mk
APP_OPTIM        := debug
APP_CFLAGS       += -O0
