export TARGET=iphone:4.3
GO_EASY_ON_ME = 1

ARCHS = armv7 armv6

Launcher_LDFLAGS = -lxml2
Launcher_CFLAGS = -DTHEOS -Dlinux -pthread -DPOSIX -Ddarwin -Ddarwin9 -I$(sourcedir)/util -I/usr/include/libxml2

include theos/makefiles/common.mk

APPLICATION_NAME = Launcher
Launcher_FILES = Launcher.m 

include $(THEOS_MAKE_PATH)/application.mk
