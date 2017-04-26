#define HAS_TCHAR

#include <stdint.h>

#if (_MSC_VER >= 1800)
#include "C:\Program Files (x86)\Windows Phone Kits\8.1\Include\D3D11_2.h" //<d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "openglWrapper.h"
#include "datastructures.h"
#include "tcvm.h"

using namespace Windows::UI::Core;

TC_API void throwException(Context currentContext, Throwable t, CharP message, ...);

#define GL_CHECK_ERROR checkGlError(__FILE__,__LINE__);
#define stringfy(x) #x

#pragma region StaticVariables

static TCGuint pointsPosition;
static int32 desiredglShiftY;

#pragma endregion

#pragma region NonStaticVars

int32 setShiftYonNextUpdateScreen;

TCGfloat ftransp[16];
int32 appW, appH, glShiftY;
int32 flen;
TCGfloat* glXYA;//[flen*2]; x,y

#pragma endregion

int32 abs32(int32 a)
{
	return a < 0 ? -a : a;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
	screen->extension = deviceCtx;
	screen->pitch = screen->screenW * screen->bpp / 8;
	screen->pixels = (uint8*)1;
   screen->hRes = (int)getDpiX();
   screen->vRes = (int)getDpiY();
	return screen->pixels != null;
}

void flushPixels()
{
}

bool checkGLfloatBuffer(Context c, int32 n)
{
	if (n > flen)
	{
      xfree(glXYA);
		flen = n * 3 / 2;
		int len = flen * 3;
      glXYA = (TCGfloat*)xmalloc(sizeof(TCGfloat)*len);
      if (!glXYA)
		{
			throwException(c, OutOfMemoryError, "Cannot allocate buffer for drawPixels");
			flen = 0;
			return false;
		}
	}
	return true;
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{
   dxUpdateScreen();
}


void glDrawThickLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
}

int32 glGetPixel(int32 x, int32 y)
{
   /* getPixel and getPixels does not work due to the use of DeferredContext
   Pixel p;
   dxGetPixels(&p, x, y, 1, 1, 1);*/
   return 0;
}

void glGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch)
{
   // getPixel and getPixels does not work due to the use of DeferredContext
   //dxGetPixels(dstPixels, srcX, srcY, width, height, pitch);
}

void flushAll()
{
}


void privateScreenChange(int32 w, int32 h)
{
   dxprivateScreenChange();
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
	if (!isScreenChange)
	{
		xfree(screen->extension);
      xfree(glXYA);
	}
}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
	screen->bpp = 32;
	screen->screenX = screen->screenY = 0;
	screen->hRes = ascrHRes;
	screen->vRes = ascrVRes;

   screen->screenW = appW;
   screen->screenH = appH;

   return checkGLfloatBuffer(mainContext, 1000);
}

DWORD32 getGlColor(int32 rgb, int32 a)
{
   PixelConv pc;
   pc.pixel = rgb;
   return (a << 24) | (pc.r << 16) | (pc.g << 8) | pc.b;
}

void glDrawPixelColors(Context currentContext, int32* x, int32* y, PixelConv* colors, int32 n)
{
   dxDrawPixelColors(x, y, colors, n);
}

void glDrawPixels(int32 n, int32 rgb)
{
   Pixel colour = getGlColor(rgb,0xFF);
   dxDrawPixels(glXYA + pointsPosition, n, colour);
}

void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a)
{
   Pixel colour = getGlColor(rgb, a);
   float coords[3] = { (float)x, (float)y, a/255.0f };
   dxDrawPixels(coords, 1, colour);
}

void glDrawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, Pixel rgb, bool fill)
{
   Pixel colour = getGlColor(rgb, 0xFF);
   dxDrawLines(currentContext, g, x, y, n, tx, ty, colour, fill);
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
   Pixel colour = getGlColor(rgb, a);
   dxDrawLine(x1, y1, x2, y2, colour);
}

void glFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
   dxFillShadedRect(g, x, y, w, h, c1, c2, horiz);
}

extern "C" {extern int32 *shiftYfield, *lastShiftYfield, *shiftHfield; }
void setTimerInterval(int32 t);
void setShiftYgl(int32 shiftY)
{
	if (setShiftYonNextUpdateScreen) 
   {
      int32 sipHeight = dxGetSipHeight();
		int32 componentPos = shiftY;
      if (appW > appH) // in landscape we work a bit different
         componentPos += *shiftHfield/4*5;
		setShiftYonNextUpdateScreen = false;
		if (sipHeight == 0 || componentPos <= sipHeight)
         glShiftY = 0;
		else
			glShiftY = -(componentPos - sipHeight);
      *shiftYfield = *lastShiftYfield = glShiftY;
	}
}

void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
   dxFillRect(x, y, x + w, y + h, getGlColor(rgb,a));
}

void glSetLineWidth(int32 w)
{
}

void glDeleteTexture(TCObject img, int32* textureId)
{
   dxDeleteTexture(img, textureId);
}

bool glLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool onlyAlpha)
{
   return dxLoadTexture(currentContext, img, textureId, pixels, width, height, onlyAlpha);
}

void glDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask)
{
   dxDrawTexture(textureId, x, y, w, h, dstX, dstY, dstW, dstH, imgW, imgH, color, alphaMask);
}
