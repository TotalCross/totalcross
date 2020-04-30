// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



extern TCObject testfont;
extern TCObject pngImage, jpegImage;

#define TEST_SLEEP 200
void blank(Context currentContext, TCObject g)
{
   Sleep(TEST_SLEEP);
   fillRect(currentContext, g, 0, 0, screen.screenW, screen.screenH, makePixel(255, 255, 255));
   updateScreen(mainContext);
}

static void testDrawHline(Context currentContext, TCObject g)
{
   int32 y;
   for (y = 0; y < screen.screenH; y++)
   {
      Pixel color = makePixel(255,y>255?255:y,255);
	   drawHLine(currentContext, g, y, y, screen.screenW - y, color, color);
      updateScreen(mainContext);
   }
}

static void testDrawVline(Context currentContext, TCObject g)
{
   int32 x;
   for (x = 0; x < screen.screenW; x++)
   {
      Pixel color = makePixel(x>255?255:x,255,255);
	   drawVLine(currentContext, g, x, 0, screen.screenH - x, color, color);
      updateScreen(mainContext);
   }
}

static void testFillRect(Context currentContext, TCObject g)
{
   int32 x,k;
   PixelConv rr,gg,bb;
   rr.pixel = gg.pixel = bb.pixel = 0;
   k = (screen.screenH - 20) / 3;

   for (x = 0; x < 256; x+=2)
   {
      bb.b = gg.g = rr.r = (uint8)x;
	   fillRect(currentContext, g, 10, 10 + k * 0, screen.screenW - 20, k, rr.pixel);
	   fillRect(currentContext, g, 10, 10 + k * 1, screen.screenW - 20, k, gg.pixel);
	   fillRect(currentContext, g, 10, 10 + k * 2, screen.screenW - 20, k, bb.pixel);
      updateScreen(mainContext);
   }
}

static void testFillCircle(Context currentContext, TCObject g)
{
   Pixel color;
   int32 r,mx,my;
   mx = screen.screenW >> 1;
   my = screen.screenH >> 1;

   for (r = 160; r >= 0; r-=3)
   {
      color = makePixel(r,r,0xAA);
	   ellipseDrawAndFill(currentContext, g, mx, my, r, r, color, color, true, false);
      updateScreen(mainContext);
   }
}

static void testDrawCircle(Context currentContext, TCObject g)
{
   Pixel color;
   int32 r,mx,my;
   mx = screen.screenW >> 1;
   my = screen.screenH >> 1;

   for (r = 0; r < 160; r++)
   {
      color = makePixel(0x55,r,0x88);
	   ellipseDrawAndFill(currentContext, g, mx, my, r, r, color, color, false, false);
      updateScreen(mainContext);
   }
}

static void testDrawEllipse(Context currentContext, TCObject g)
{
   Pixel color;
   int32 r,mx,my;
   mx = screen.screenW >> 1;
   my = screen.screenH >> 1;

   for (r = 0; r < 160; r++)
   {
      color = makePixel(0xAA,0xFF,r);
	   ellipseDrawAndFill(currentContext, g, mx, my, r / 2, r, color, color, false, false);
      updateScreen(mainContext);
   }
}

static void testFillEllipse(Context currentContext, TCObject g)
{
   Pixel color;
   int32 r,mx,my;
   mx = screen.screenW >> 1;
   my = screen.screenH >> 1;

   for (r = 160; r >= 0; r -= 2)
   {
      color = makePixel(0xAA,r,0xFF);
	   ellipseDrawAndFill(currentContext, g, mx, my, r, r / 2, color, color, true, false);
      updateScreen(mainContext);
   }
}

static void testPie(Context currentContext, TCObject g)
{
   Pixel color,white,black;
   int32 r=40,d,mx,my;
   mx = screen.screenW >> 1;
   my = screen.screenH >> 1;
   white = makePixel(0xFF, 0xFF, 0xFF);
   black = makePixel(0,0,0);

   color = makePixel(r,0xAA,0xFF);
   for (d = 0; d <= 360; d+=2)
   {
	   arcPiePointDrawAndFill(currentContext, g, mx, my, r, r, d - 45, d, black, color, true, true, false);
      updateScreen(mainContext);
	   fillRect(currentContext, g, mx - r - 1, my - r - 1, r + r + 2, r + r + 2, white);
   }
}

static void drawImage(Context currentContext, TCObject g, TCObject img)
{
   TCObject gimg;
   int32 w = Image_width(img);
   int32 h = Image_height(img);
   Pixel fore,back;
   fore = makePixel(0,255,255);
   back = makePixel(255,255,0);
   gimg = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.ui.gfx.Graphics");
   setObjectLock(gimg, UNLOCKED);
   Graphics_surface(gimg) = img;
   createGfxSurface(w, h, gimg, SURF_IMAGE);
   drawSurface(currentContext, g, gimg, 0, 0, w, h, 0, 0, true);
   updateScreen(mainContext);
}

static void testJpegImage(Context currentContext, TCObject g)
{
   drawImage(currentContext, g, jpegImage);
}

static void testPngImage(Context currentContext, TCObject g)
{
   drawImage(currentContext, g, pngImage);
}

static void testPalette(Context currentContext, TCObject g)
{
   int32 x=0,y=0,i,wh;
   uint32 c;
   uint32 pal[256];
   fillWith8bppPalette(pal);
   wh = min32(screen.screenW / 16, screen.screenH / 16);
   for (i = 0; i < 256; i++)
   {
      c = pal[i];
      if (i && (i % 16) == 0)
      {
         x = 0; y += wh;
      }
	   fillRect(currentContext, g, x, y, wh, wh, makePixelRGB(c));
      x += wh;
   }
   updateScreen(mainContext);
}

static void testText(Context currentContext, TCObject g)
{
   JChar text[50];
   int32 x,x1,y1,x2,y2,y,dy,i;
   Pixel white = makePixel(255,255,255);
   Pixel p = makePixel(0,0,255);
   Graphics_font(g) = testfont;
   dy = 12;
   CharP2JCharPBuf("Barbara",7, text, true);
   for (x = -50; x < (50+screen.screenW); x++)
   {
      for (i=0,y = 10; i++ < 10 && y < (screen.screenH-20); y+=dy) // max 10 lines
         drawText(currentContext, g, text, 7, x, y, p,0);
      x1 = x; x2 = currentContext->dirtyX2; y1 = currentContext->dirtyY1; y2 = currentContext->dirtyY2; // save dirty area to erase it
      updateScreen(mainContext);
	   fillRect(currentContext, g, x1, y1, x2 - x1, y2 - y1, white); // erase dirty area
   }
   Graphics_clipX1(g) = 0;
   Graphics_clipX2(g) = screen.screenW;
}

//#define DUMP_TIME

static void debugTime(int32 n, int32 ini)
{
#ifdef DUMP_TIME
   debug(" %02d.Elapsed: %d ms",n, getTimeStamp()-ini);
#else
   UNUSED(n)
   UNUSED(ini)
#endif
}

TESTCASE(Graphics) // #DEPENDS(tuiI_imageLoad_s)
{
   TCObject g;
   int32 s;
   TNMParams p;
   TCObject obj[2];

   ASSERT1_EQUALS(NotNull, screen.pixels);
   // create a graphics object and call its native constructor
   g = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.ui.gfx.Graphics");
   setObjectLock(g, UNLOCKED);
   ASSERT1_EQUALS(NotNull, g);
   p.currentContext = currentContext;
   p.obj = obj;
   obj[0] = g;
   obj[1] = null;
   tugG_create_g(&p);

   fillRect(currentContext, g, 0, 0, screen.screenW, screen.screenH, makePixel(0xFF, 0xFF, 0xFF));
   updateScreen(mainContext);

   ASSERT1_EQUALS(NotNull, defaultFont);
   ASSERT1_EQUALS(NotNull, pngImage);
   ASSERT1_EQUALS(NotNull, jpegImage);

   s = getTimeStamp();  testJpegImage(currentContext, g);  debugTime(1, s);  blank(currentContext, g);
   s = getTimeStamp();  testPngImage(currentContext, g);  debugTime(2, s);  blank(currentContext, g);
   s = getTimeStamp();  testText(currentContext, g);  debugTime(3, s);  blank(currentContext, g);
   s = getTimeStamp();  testPalette(currentContext, g);  debugTime(4, s);  blank(currentContext, g);
   s = getTimeStamp();  testPie(currentContext, g);  debugTime(5, s);  blank(currentContext, g);
   s = getTimeStamp();  testDrawEllipse(currentContext, g);  debugTime(6, s);  blank(currentContext, g);
   s = getTimeStamp();  testFillEllipse(currentContext, g);  debugTime(7, s);  blank(currentContext, g);
   s = getTimeStamp();  testFillCircle(currentContext, g);  debugTime(8, s);  blank(currentContext, g);
   s = getTimeStamp();  testDrawCircle(currentContext, g);  debugTime(9, s);  blank(currentContext, g);
   s = getTimeStamp();  testDrawVline(currentContext, g);  debugTime(10, s);  blank(currentContext, g);
   s = getTimeStamp();  testDrawHline(currentContext, g);  debugTime(11, s);  blank(currentContext, g);
   s = getTimeStamp();  testFillRect(currentContext, g);  debugTime(12, s);  Sleep(TEST_SLEEP); // no blank
   finish: ;
}
