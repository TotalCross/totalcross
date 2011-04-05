/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#define SETPIXEL32(r,g,b) (((b) << 24) | ((g) << 16) | ((r) << 8))   // BBGGRR00
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 3) | ((g) >> 5) | ((((g) >> 2) & 7) << 13) | (((b) >> 3) << 8)) // bits RRRRR123 456BBBBB -> 456BBBBB RRRRR123 , 1-6=G (same as for win32 but 16-bit swapped)
#define SETPIXEL565_(dest,p) \
        { \
           Pixel565 tt = ((Pixel565)(p>>16)&0xF800) | (Pixel565)((p>>13)&0x7E0) | (Pixel565)((p>>11)&0x1F); \
           *dest++ = (tt >> 8) | (tt << 8); \
        }

typedef struct
{
   UInt32 oldWidth, oldHeight, oldBpp;
   UInt16 oldDensity;
   WinHandle pixelsWin;
   DensityType density;
   int32 maxWidth, maxHeight;
   BitmapType *bmp,*bmp2;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#define NO_GRAPHICS_LOCK_NEEDED
#define graphicsLock(screenSurface, on) true
