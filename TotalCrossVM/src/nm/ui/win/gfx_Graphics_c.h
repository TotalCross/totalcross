// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

void adjustWindowSizeWithBorders(int32 resizableWindow, int32* w, int32* h)
{
#ifndef WINCE // windows ce already does this for us
   *w += GetSystemMetrics(resizableWindow ? SM_CXSIZEFRAME : SM_CXFIXEDFRAME)*2;
   *h += GetSystemMetrics(resizableWindow ? SM_CYSIZEFRAME : SM_CYFIXEDFRAME)*2 + GetSystemMetrics(SM_CYCAPTION);
#endif
}
void privateScreenChange(int32 w, int32 h)
{
#ifndef WINCE // windows ce already does this for us
   adjustWindowSizeWithBorders(*tcSettings.resizableWindow,&w, &h);
   SetWindowPos(mainHWnd,0,0,0, w, h, SWP_NOMOVE);
#endif
}

#if defined (WINCE)
RECT defaultWorkingArea;
#endif

void restoreTaskbar()
{
#if defined (WINCE)
   HWND hWndTaskBar;

   if (!isWindowsMobile)
   {
      hWndTaskBar = FindWindow(TEXT("HHTaskBar"), TEXT(""));
      ShowWindow(hWndTaskBar, SW_SHOWNORMAL);
      SystemParametersInfo(SPI_SETWORKAREA, 0, &defaultWorkingArea, SPIF_SENDCHANGE); // flsobral@tc113_25: fixed bug - instead of restoring the taskbar, it was changing the default window size to the size of the taskbar.
   }
#endif
}

void getScreenSize(int32 *w, int32* h)
{
#ifdef WINCE
   RECT rect;
   SystemParametersInfo(SPI_GETWORKAREA, 0, &rect, 0);
   *w = GetSystemMetrics(SM_CXSCREEN);
   *h = GetSystemMetrics(SM_CYSCREEN);
   if (!*tcSettings.isFullScreenPtr)
   {
      // given the size of the client area, figure out the window size needed
      *w -= rect.left;
      if (isWindowsMobile)
         *h -= rect.top;
      else
         *h = rect.bottom;
   }
#else // guich@tc130: use the default values for win32
   *w = screen.screenW;
   *h = screen.screenH;
#endif
}

#if !defined(WINCE)
extern int32 defScrX,defScrY,defScrW,defScrH;
#endif

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
#ifndef WP8
   DWORD style;
   int32 width, height;
   RECT rect;
   TCHAR main[MAX_PATHNAME];
   HANDLE instance = GetModuleHandle(0);
   char* dot;
   HDC deviceContext;
   bool resizableWindow = appTczAttr & ATTR_RESIZABLE_WINDOW;

   screen->extension = (TScreenSurfaceEx*)xmalloc(sizeof(TScreenSurfaceEx));

   SystemParametersInfo(SPI_GETWORKAREA, 0, &rect, 0);
   style = WS_VISIBLE;
#if !defined (WINCE)
   deviceContext = GetDC(mainHWnd);
   screen->bpp = GetDeviceCaps(deviceContext,BITSPIXEL) * GetDeviceCaps(deviceContext,PLANES);
   DeleteDC(deviceContext);

   if (appTczAttr & ATTR_WINDOWSIZE_320X480) {defScrX=defScrY=-2; width = 320; height = 480;} else
   if (appTczAttr & ATTR_WINDOWSIZE_480X640) {defScrX=defScrY=-2; width = 480; height = 640;} else
   if (appTczAttr & ATTR_WINDOWSIZE_600X800) {defScrX=defScrY=-2; width = 600; height = min32(800,rect.bottom-rect.top);} else
   {
      width = defScrW == -1 ? 240 : defScrW;
      height = defScrH == -1 ? 320 : defScrH;
   }                                                                                                                          
#ifdef _DEBUG
   defScrX = defScrY = 0;
#endif

   rect.left = defScrX == -1 ? 0 : defScrX == -2 ? (rect.left+(rect.right -width )/2) : defScrX;
   rect.top  = defScrY == -1 ? 0 : defScrY == -2 ? (rect.top +(rect.bottom-height)/2) : defScrY;
   rect.bottom = height;
   rect.right = width;
   adjustWindowSizeWithBorders(resizableWindow,&rect.right,&rect.bottom);

   style |= WS_SYSMENU | WS_CAPTION | WS_MINIMIZEBOX;
   if (resizableWindow) style |= WS_THICKFRAME | WS_MAXIMIZEBOX;
#else
   SystemParametersInfo(SPI_GETWORKAREA, 0, &defaultWorkingArea, 0);
   deviceContext = GetDC(mainHWnd);
   screen->bpp = GetDeviceCaps(deviceContext, BITSPIXEL);

   width = GetSystemMetrics(SM_CXSCREEN);
   height = GetSystemMetrics(SM_CYSCREEN);

   // given the size of the client area, figure out the window size needed
   rect.right = width = width - rect.left;
   if (isWindowsMobile)
      rect.bottom = height = height - rect.top;
   else
      height = rect.bottom;
   AdjustWindowRectEx(&rect, style, FALSE, 0);
#endif
   dot = xstrrchr(mainClassName, '.');
   CharP2TCHARPBuf(dot ? dot+1 : mainClassName, main); // remove the package from the name
   mainHWnd = CreateWindow(exeName, main, style, rect.left, rect.top, rect.right, rect.bottom, NULL, NULL, instance, NULL ); // guich@400_62: move window to desired user position
   if (!mainHWnd)
      return false;

   // store the x, y, width, height, hRes and vRes
   screen->screenY = rect.top;
   GetClientRect(mainHWnd, &rect);
   screen->screenX = rect.left;
   screen->minScreenW = screen->screenW = width;
   screen->minScreenH = screen->screenH = height;
   screen->hRes = GetDeviceCaps(deviceContext, LOGPIXELSX);
   screen->vRes = GetDeviceCaps(deviceContext, LOGPIXELSY);

#if !defined (WINCE)
   DeleteDC(deviceContext);
#else
   ReleaseDC(mainHWnd, deviceContext);
#endif

#endif
#if defined(_DEBUG) && !defined(WINCE)
   SetProcessAffinityMask(GetCurrentProcess(), 1);
#endif
   return true;
}

struct
{
   BITMAPINFO bi;
	RGBQUAD	 bmiColors[256];
} dibInfo;
struct
{
   LOGPALETTE lp;
   PALETTEENTRY pe[256];
} curPal;
HPALETTE hPalette;

void applyPalette()
{
   if (SCREEN_EX(&screen) != null)
   {
      SelectPalette(SCREEN_EX(&screen)->dc, hPalette, 0);
      RealizePalette(SCREEN_EX(&screen)->dc);
   }
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   uint32 *ptr;

   screen->pitch = screen->screenW * screen->bpp / 8;
   SCREEN_EX(screen)->dc = GetDC(mainHWnd);

   ptr = (uint32 *)dibInfo.bi.bmiColors;

	//3.1 Initilize DIBINFO structure
   xmemzero(&dibInfo,sizeof(dibInfo));
	dibInfo.bi.bmiHeader.biBitCount = (uint16)screen->bpp;
   dibInfo.bi.bmiHeader.biCompression = (screen->bpp == 16) ? BI_BITFIELDS : BI_RGB;
	dibInfo.bi.bmiHeader.biPlanes = 1;
	dibInfo.bi.bmiHeader.biSize = 40;
	dibInfo.bi.bmiHeader.biWidth = screen->screenW;
	dibInfo.bi.bmiHeader.biHeight = -(int32)screen->screenH;
	dibInfo.bi.bmiHeader.biSizeImage = screen->screenW * screen->screenH * screen->bpp / 8;
   if (screen->bpp == 16)
   {
      // setup the bit masks
      dibInfo.bi.bmiHeader.biClrUsed = dibInfo.bi.bmiHeader.biClrImportant = 3;
      ptr[0] = 0xf800;
      ptr[1] = 0x07e0;
      ptr[2] = 0x001F;
   }
   else
   if (screen->bpp == 8) // apply our 485 palette to the screen
   {
      // create the custom 685 palette
      dibInfo.bi.bmiHeader.biClrUsed = dibInfo.bi.bmiHeader.biClrImportant = 256;
      fillWith8bppPalette(ptr);

      curPal.lp.palNumEntries = 256;
      curPal.lp.palVersion = 0x0300;
      xmemmove(&curPal.lp.palPalEntry, ptr, 1024);
      hPalette = CreatePalette(&curPal.lp);
      SelectPalette(SCREEN_EX(screen)->dc, hPalette, 0);
      RealizePalette(SCREEN_EX(screen)->dc);
   }

	//3.2 Create bitmap and receive pointer to points into pBuffer
   SCREEN_EX(screen)->hbmp = CreateDIBSection(SCREEN_EX(screen)->dc, &dibInfo.bi, DIB_RGB_COLORS, (void**)&screen->pixels, NULL, 0);
   if (!SCREEN_EX(screen)->hbmp || !screen->pixels)
      return false; // put @err,hr in your watch window to see GetLastError()
   return true;
}

inline static void drawImageLine(ScreenSurface screen, HDC targetDC, int32 minx, int32 miny, int32 maxx, int32 maxy)
{
   BitBlt(SCREEN_EX(screen)->dc, minx,miny, maxx-minx, maxy-miny, targetDC, minx,miny, SRCCOPY);
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen) // screen's already locked
{                         
   HDC targetDC = CreateCompatibleDC(NULL);
   HBITMAP hOldBitmap = (HBITMAP)SelectObject(targetDC, SCREEN_EX(screen)->hbmp);
   BitBlt(SCREEN_EX(screen)->dc, currentContext->dirtyX1, currentContext->dirtyY1, currentContext->dirtyX2-currentContext->dirtyX1, currentContext->dirtyY2-currentContext->dirtyY1, targetDC, currentContext->dirtyX1, currentContext->dirtyY1, SRCCOPY);
   SelectObject(targetDC, hOldBitmap);
   DeleteDC(targetDC);
#ifdef WINCE // guich@tc113_20
   if (oldAutoOffValue != 0) // guich@450_33: since the autooff timer function don't work on wince, we must keep resetting the idle timer so that the device will never go sleep - guich@554_7: reimplemented this feature
      SystemIdleTimerReset();
#endif
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   DeleteObject(SCREEN_EX(screen)->hbmp);
   if (!isScreenChange)
   {
      xfree(screen->extension);
      screen->extension = null;
      DestroyWindow(mainHWnd);
      mainHWnd = null;
      restoreTaskbar();
   }
   UnregisterClass(exeName, null);
}
