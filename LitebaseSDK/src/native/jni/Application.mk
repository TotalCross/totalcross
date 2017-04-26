APP_PROJECT_PATH := $(call my-dir)
APP_ABI          := armeabi
APP_MODULES      := litebase
APP_BUILD_SCRIPT := $(APP_PROJECT_PATH)/Android.mk
APP_OPTIM        := release
APP_CFLAGS       += -O3
