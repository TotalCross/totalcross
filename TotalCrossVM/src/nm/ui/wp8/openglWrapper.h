#ifndef __openglWrapper_h__
#define __openglWrapper_h__
#pragma once

#include "tcvm.h"

#include "../GraphicsPrimitives.h"

#ifdef __cplusplus

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#endif

#ifdef __cplusplus
extern "C" {
#endif


#define IS_PIXEL (1<<28)
#define IS_DIAGONAL  (1<<27)

#ifdef USE_DX
typedef float TCGfloat;
typedef unsigned char TCGubyte;
typedef unsigned int TCGuint;
typedef int TCGint;
typedef char TCGchar;
#else
typedef GLfloat TCGfloat;
typedef GLubyte TCGubyte;
typedef GLuint TCGuint;
typedef GLint TCGint;
typedef GLchar TCGchar;
#endif

typedef TCGfloat mat4[16];
typedef union
{
	struct{ TCGubyte r, g, b, a; };
	Pixel pixel;
} glpixel;

extern int32 /*realAppH,*/ appW, appH, glShiftY;
extern int32 *needsPaint;

extern TCGfloat ftransp[16];
extern TCGfloat* glcoords;//[flen*2]; x,y
extern TCGfloat* glcolors;//[flen];   alpha
extern int32 desiredScreenShiftY;
extern int32 setShiftYonNextUpdateScreen;

extern VoidPs* imgTextures;

void setShiftYgl();

extern void setTimerInterval(int32 t);

int32 abs32(int32 a);

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr);
void glDrawPixels(int32 n, int32 rgb);
void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a);
bool graphicsCreateScreenSurface(ScreenSurface screen);
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen);
void glLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList, bool onlyAlpha);
void glDrawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, Pixel rgb, bool fill);
void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a);
void glDeleteTexture(TCObject img, int32* textureId, bool updateList);

void glDrawThickLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a);
int32 glGetPixel(int32 x, int32 y);
//void applyChanges(Context currentContext, TCObject obj, bool updateList);
void flushPixels();
bool checkGLfloatBuffer(Context c, int32 n);

void glDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv* color, int32* clip, int32 alphaMask);

void graphicsDestroy(ScreenSurface screen, bool isScreenChange);
void privateScreenChange(int32 w, int32 h);
void flushAll();
void glGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch);
void glSetLineWidth(int32 w);
void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a);
void glFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz);
#ifdef __cplusplus
}
#endif


#endif