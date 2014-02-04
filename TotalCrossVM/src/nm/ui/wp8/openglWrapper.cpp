#define HAS_TCHAR

#include <stdint.h>

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "openglWrapper.h"
#include "datastructures.h"
#include "tcvm.h"

//#define TOGGLE_BUFFER


using namespace Windows::UI::Core;

TC_API void throwException(Context currentContext, Throwable t, CharP message, ...);

#define GL_CHECK_ERROR checkGlError(__FILE__,__LINE__);
#define stringfy(x) #x

#pragma region StaticVariables

static TCGuint pointsPosition;
static int32 *pixcoords, *pixcolors, *pixEnd;
static int32 desiredglShiftY;

#pragma endregion

#pragma region NonStaticVars

bool setShiftYonNextUpdateScreen;

VoidPs* imgTextures;
int32 realAppH, appW, appH, glShiftY;
TCGfloat ftransp[16], f255[256];
int32 flen;
TCGfloat* glcoords;//[flen*2]; x,y
TCGfloat* glcolors;//[flen];   alpha

#pragma endregion

int32 abs32(int32 a)
{
	return a < 0 ? -a : a;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
#ifndef ANDROID
	screen->extension = deviceCtx;
#endif
	screen->pitch = screen->screenW * screen->bpp / 8;
	screen->pixels = (uint8*)1;
	return screen->pixels != null;
}

void flushPixels(int q)
{
}

bool checkGLfloatBuffer(Context c, int32 n)
{
	if (n > flen)
	{
		xfree(glcoords);
		xfree(glcolors);
		flen = n * 3 / 2;
		int len = flen * 2;
		glcoords = (TCGfloat*)xmalloc(sizeof(TCGfloat)*len); pixcoords = (int32*)glcoords;
		glcolors = (TCGfloat*)xmalloc(sizeof(TCGfloat)*flen); pixcolors = (int32*)glcolors;
		pixEnd = pixcoords + len;
		if (!glcoords || !glcolors)
		{
			throwException(c, OutOfMemoryError, "Cannot allocate buffer for drawPixels");
			xfree(glcoords);
			xfree(glcolors);
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
   return 0;
}

void glGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch)
{
}

void flushAll()
{
}


void privateScreenChange(int32 w, int32 h)
{
	appW = w;
	appH = h;
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
	if (!isScreenChange)
	{
		xfree(screen->extension);
		xfree(glcoords);
		xfree(glcolors);
	}
}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
	screen->bpp = 32;
	screen->screenX = screen->screenY = 0;
	screen->hRes = ascrHRes;
	screen->vRes = ascrVRes;

   screen->screenW = 480; //XXX lastW must be initialized with the screen bounds
   screen->screenH = 800;

   dxSetup();
   return checkGLfloatBuffer(mainContext, 10000);
}

DWORD32 getGlColor(int32 rgb, int32 a)
{
   PixelConv pc;
   pc.pixel = rgb;
   return (a << 24) | (pc.r << 16) | (pc.g << 8) | pc.b;// (a << 24) | rgb;// MAKE_RGBA(rgb, a);
}

void glDrawPixels(int32 n, int32 rgb)
{
#if 0 // CORRIGIR PARA LEVAR EM CONSIDERACAO O ALPHA!!!
   Pixel colour = getGlColor(rgb,0xFF);

   int* x;
   int* y;

   x = (int*) xmalloc(n * sizeof(int));
   y = (int*) xmalloc(n * sizeof(int));

   if (x != null && y != null)
   {
      for (int i = 0; i < n; i++)
      {
         x[i] = glcoords[pointsPosition + (i * 2)];
         y[i] = glcoords[pointsPosition + (i * 2 + 1)];
      }

      dxDrawPixels(x, y, n, colour);
      xfree(x);
      xfree(y);
   }
#endif
}

void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a)
{
   Pixel colour = getGlColor(rgb, a);
   dxDrawPixels(&x, &y, 1, colour);
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
   Pixel colour = getGlColor(rgb, a);
   dxDrawLine(x1, y1, x2, y2, colour);
}

void glFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
}

void setTimerInterval(int32 t);
void setShiftYgl()
{
	if (setShiftYonNextUpdateScreen) 
   {
		int32 componentPos;
		int siph = 100;//XXX MainView::GetLastInstance()->GetSIPHeight();
		componentPos = -(desiredglShiftY - desiredScreenShiftY);     // set both at once
		setShiftYonNextUpdateScreen = false;

		if (componentPos <= 100)//XXX MainView::GetLastInstance()->GetSIPHeight())
         glShiftY = 0;
		else
			glShiftY = -(componentPos - 100);//XXX MainView::GetLastInstance()->GetSIPHeight());
	}
}

void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
   dxFillRect(x, y, x + w, y + h, getGlColor(rgb,a));
}

void glSetLineWidth(int32 w)
{
}

void glDeleteTexture(TCObject img, int32* textureId, bool updateList)
{
   dxDeleteTexture(img, textureId, updateList);
}

void glLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList)
{
   dxLoadTexture(currentContext, img, textureId, pixels, width, height, updateList);
}

void glDrawTexture(int32 textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv *color, int32* clip)
{
   dxDrawTexture(textureId, x, y, w, h, dstX, dstY, imgW, imgH, color, clip);
}
