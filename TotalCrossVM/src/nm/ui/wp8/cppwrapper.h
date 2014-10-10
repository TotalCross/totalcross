#ifndef __CPPWRAPPER_H__
#define __CPPWRAPPER_H__
#pragma once

#define HAS_TCHAR
#include <basetsd.h>
#include "../Window.h"
#include "tcclass.h"

#include "../GraphicsPrimitives.h"

#ifdef __cplusplus
extern "C" {
#endif

struct eventQueueMember 
{
	int type;
	int key;
	int x;
	int y;
	int modifiers;
   int count;
};

char *GetAppPathWP8();
char *GetVmPathWP8();
char *GetDisplayNameWP8();

// WP8 keyboard functions
void windowSetSIP(enum TCSIP kb);

// Dummy functions?
void windowSetDeviceTitle(TCObject titleObj);

// Vm
DWORD32 getRemainingBatery();
void vibrate(DWORD32 milliseconds);
DWORD32 getFreeMemoryWP8();
void alertCPP(JCharP jCharStr);
void vmSetAutoOffCPP(bool enable);

// Dial
void dialNumberCPP(JCharP number);

// SMS
void smsSendCPP(JCharP szMessage, JCharP szDestination);

// RadioDevice
int rdGetStateCPP(int type);

// ConnectionManager
bool isAvailableCPP(int type);

// GPS
bool nativeStartGPSCPP();
int nativeUpdateLocationCPP(Context context, TCObject gpsObject);
void nativeStopGPSCPP();

// File
bool fileIsCardInsertedCPP();

//Event Queue functions
void eventQueuePush(int type, int key, int x, int y, int modifiers);
struct eventQueueMember eventQueuePop(void);
int eventQueueEmpty(void);

void dxGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch);
void dxUpdateScreen();
void dxDrawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, int32 color, bool fill);
void dxDrawLine(int x1, int y1, int x2, int y2, int color);
void dxFillRect(int x1, int y1, int x2, int y2, int color);
void dxDrawPixelColors(int32* x, int32* y, PixelConv* colors, int32 n);
void dxDrawPixels(float *glXYA, int count, int color);
void dxLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool onlyAlpha);
void dxDeleteTexture(TCObject img, int32* textureId);
void dxDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask);
void dxFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz);
int32 dxGetSipHeight();
int32 dxGetScreenSize();
void dxprivateScreenChange();
void cameraClick(NMParams p);

// Media
void nativeSoundPlayCPP(CharP filename);

// Etc
double getFontHeightCPP();
void privateWindowSetSIP(bool visible);
void appExit();
void appSetFullScreen();

// Settings                   
bool isVirtualKeyboard();
int getOSVersion();
double getDpiX();
double getDpiY();
void getDeviceIdCPP(CharP buf);
void getRomSerialNumberCPP(CharP buf);

// Map
bool showMap(JCharP originStr, int32 originLen, JCharP destinationStr, int32 destinationLen);

#ifdef __cplusplus
}
#endif


#endif
