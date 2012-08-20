/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



void privateScreenChange(int32 w, int32 h)
{
   UNUSED(w)
   UNUSED(h)
}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
	UInt32 width=0, height=0;
   Err err;
   UInt32 depth=1;
   UInt16 cs;
   RectangleType r;

   screen->extension = newX(ScreenSurfaceEx);

   // guich@tc100: now we get the current display extent
   if (supportsDIA) // guich: i was unable to find the current extent of the window using WinGetBounds. It only works if you popup an alert BEFORE calling WinGetBounds. Given this, we force the input area to open; closing it afterwards makes everything work fine
      PINSetInputTriggerState(pinInputTriggerEnabled);
   cs = WinSetCoordinateSystem(kCoordinatesNative);
   WinGetBounds(WinGetDisplayWindow(), &r);
   WinSetCoordinateSystem(cs);

   screen->screenW = width  = r.extent.x;
   screen->screenH = height = r.extent.y;

   // Save the current video mode, to restore it
   SCREEN_EX(screen)->oldDensity = WinGetCoordinateSystem();
   err = WinScreenMode(winScreenModeGet, &SCREEN_EX(screen)->oldWidth, &SCREEN_EX(screen)->oldHeight, &SCREEN_EX(screen)->oldBpp, 0);
   if (err != errNone) return false;
   //alert("old: %d, %d, %d, %d", (int)SCREEN_EX(screen)->oldDensity, (int)SCREEN_EX(screen)->oldWidth, (int)SCREEN_EX(screen)->oldHeight, (int)SCREEN_EX(screen)->oldBpp);

   // retrieve a field of bits indicating the supported Bits Per Pixel
   err = WinScreenMode(winScreenModeGetSupportedDepths,0,0,&depth,0);
   if (err != errNone) return false;

   // choose the best available mode (defaults to 1)
   if (depth & 0x80000000) depth = 32; else
   if (depth & 0x8000)     depth = 16; else // 565, same as windows
   if (depth & 0x80)       depth = 8;  else
   if (depth & 0x08)       depth = 4;  else
   if (depth & 0x2)        depth = 2;

   screen->bpp = max32(8, depth); // we don't support less than 8bpp.
   screen->pitch = screen->screenW * screen->bpp / 8;

   // Change the screen mode to the maximum one
   SCREEN_EX(screen)->density = (72 * width / 160);
   err = WinScreenMode(winScreenModeSet, &width, &height, &depth, 0);
   // if the DIA is collapsed or the user requested full screen, collapse it (again) to generate the event
   if (supportsDIA && (PINGetInputAreaState() == pinInputAreaClosed || *tcSettings.isFullScreenPtr))
      PINSetInputAreaState(pinInputAreaClosed);
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   Err err;
   SCREEN_EX(screen)->bmp = BmpCreate(screen->screenW, screen->screenH, screen->bpp, 0, &err);
   if (SCREEN_EX(screen)->bmp == null || err != errNone)
   {
      alert("Cannot allocate screen memory!");
      return false;
   }

   if (SCREEN_EX(screen)->density > kCoordinatesStandard)
   {
      BitmapTypeV3 *bmpV3;
      bmpV3 = BmpCreateBitmapV3(SCREEN_EX(screen)->bmp, (screen->screenW==240)?kCoordinatesOneAndAHalf:kDensityDouble, BmpGetBits(SCREEN_EX(screen)->bmp), 0); // guich@583_16: XP30 is 240x240
      if (bmpV3 == null || err != errNone)
      {
         alert("Cannot create bitmap v3");
         return false;
      }
      SCREEN_EX(screen)->bmp2 = SCREEN_EX(screen)->bmp;
      SCREEN_EX(screen)->bmp = (BitmapType *)bmpV3;
   }

   screen->pixels = BmpGetBits(SCREEN_EX(screen)->bmp);
   SCREEN_EX(screen)->pixelsWin = WinCreateBitmapWindow(SCREEN_EX(screen)->bmp, &err);
   if (SCREEN_EX(screen)->pixelsWin == null || err != errNone)
   {
      alert("Cannot get bmp bits");
      return false;
   }

   if (screen->bpp == 8) // apply our 485 palette to the screen
   {
      int32 palette[256];
      fillWith8bppPalette(palette);
	   WinPalette(winPaletteSet,0,256,(RGBColorType *)palette);
   }
   return true;
}

inline static void drawImageLine(ScreenSurface screen, RectangleType *bounds, int32 minx, int32 miny, int32 maxx, int32 maxy)
{
   bounds->topLeft.x = minx;
   bounds->topLeft.y = miny;
   bounds->extent.x  = maxx - minx;
   bounds->extent.y  = maxy - miny;
   WinCopyRectangle(SCREEN_EX(screen)->pixelsWin, WinGetDisplayWindow(), bounds, minx,miny, winPaint);
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen, int32 transitionEffect) // screen's already locked
{                                 
   RectangleType bounds;

   WinSetCoordinateSystem(SCREEN_EX(screen)->density);
   switch (transitionEffect)
   {
      case TRANSITION_NONE:
         bounds.topLeft.x = currentContext->dirtyX1;
         bounds.topLeft.y = currentContext->dirtyY1;
         bounds.extent.x  = currentContext->dirtyX2 - currentContext->dirtyX1;
         bounds.extent.y  = currentContext->dirtyY2 - currentContext->dirtyY1;
         WinCopyRectangle(SCREEN_EX(screen)->pixelsWin, WinGetDisplayWindow(), &bounds, currentContext->dirtyX1, currentContext->dirtyY1, winPaint);
         break;
      case TRANSITION_CLOSE:
      case TRANSITION_OPEN:
      {       
         int32 i0,iinc,i;
         int32 w = screen->screenW;
         int32 h = screen->screenH;
         float incX=1,incY=1;
         int32 n = min32(w,h);
         int32 mx = w/2,ww=1,hh=1;
         int32 my = h/2;
         if (w > h)
            {incX = (float)w/h; ww = (int)incX+1;}
          else
            {incY = (float)h/w; hh = (int)incY+1;}
         i0 = transitionEffect == TRANSITION_CLOSE ? n : 0;
         iinc = transitionEffect == TRANSITION_CLOSE ? -1 : 1;
         for (i =i0; --n >= 0; i+=iinc)
         {
            int32 minx = (int32)(mx - i*incX);
            int32 miny = (int32)(my - i*incY);
            int32 maxx = (int32)(mx + i*incX);
            int32 maxy = (int32)(my + i*incY);
            drawImageLine(screen,&bounds,minx-ww,miny-hh,maxx+ww,miny+hh);
            drawImageLine(screen,&bounds,minx-ww,miny-hh,minx+ww,maxy+hh);
            drawImageLine(screen,&bounds,maxx-ww,miny-hh,maxx+ww,maxy+hh);
            drawImageLine(screen,&bounds,minx-ww,maxy-hh,maxx+ww,maxy+hh);
         }
         break;
      }
   }
   WinSetCoordinateSystem(kCoordinatesStandard);
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   if (SCREEN_EX(screen)->pixelsWin) WinDeleteWindow(SCREEN_EX(screen)->pixelsWin, false);
   if (SCREEN_EX(screen)->bmp2) BmpDelete(SCREEN_EX(screen)->bmp2);
   if (SCREEN_EX(screen)->bmp) BmpDelete(SCREEN_EX(screen)->bmp);
   // Restore the initial video mode.
   if (!isScreenChange)
   {
      WinScreenMode(winScreenModeSet, &SCREEN_EX(screen)->oldWidth, &SCREEN_EX(screen)->oldHeight, &SCREEN_EX(screen)->oldBpp, 0);
      xfree(screen->extension);
   }
}
