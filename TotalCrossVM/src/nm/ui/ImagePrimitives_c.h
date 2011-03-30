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

// $Id: ImagePrimitives_c.h,v 1.25 2011-01-04 13:31:03 guich Exp $

#include <math.h>

static void setCurrentFrame(Object obj, int32 nr)
{
   int32 y,width,widthOfAllFrames;
   Pixel* pixelsOfAllFrames = (Pixel*)ARRAYOBJ_START(Image_pixelsOfAllFrames(obj));
   Pixel* pixels = (Pixel*)ARRAYOBJ_START(Image_pixels(obj));
   int32 frameCount = Image_frameCount(obj), mw;

   if (frameCount <= 1 || nr == Image_currentFrame(obj)) return;
   if (nr < 0)
      nr = frameCount-1;
   else
   if (nr >= frameCount)
      nr = 0;
   Image_currentFrame(obj) = nr;
   width = Image_width(obj);
   mw = width;
   widthOfAllFrames = Image_widthOfAllFrames(obj) - width;
   pixelsOfAllFrames += nr * width;
   for (y = Image_height(obj); --y >= 0; pixelsOfAllFrames += widthOfAllFrames, mw = width)
      while (mw-- > 0)
         *pixels++ = *pixelsOfAllFrames++;
}

static void applyColor(Object obj, Pixel color) // guich@tc112_24
{
   int32 frameCount = Image_frameCount(obj);
   Object pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj);
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   Pixel transp = makePixelRGB(Image_transparentColor(obj));
   PixelConv c;
   double k = 128;
   int32 mr,mg,mb;

   c.pixel = color;

   mr = (int32) (sqrt((c.r + k) / k) * 0x10000);
   mg = (int32) (sqrt((c.g + k) / k) * 0x10000);
   mb = (int32) (sqrt((c.b + k) / k) * 0x10000);

   for (; len-- > 0; pixels++)
      if (pixels->pixel != transp)
      {
         pixels->r = min32(255,(mr * pixels->r) >> 16);
         pixels->g = min32(255,(mg * pixels->g) >> 16);
         pixels->b = min32(255,(mb * pixels->b) >> 16);
      }
   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
}
   
static void getSmoothScaledInstance(Object thisObj, Object newObj, Pixel backColor) // guich@tc100b5_47: optimized. now 12x faster
{
   // Based on the ImageProcessor class on "KickAss Java Programming" (Tonny Espeset)
   PixelConv* p = (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   int32 frameCount = Image_frameCount(thisObj);
   int32 width = Image_width(thisObj) * frameCount;
   int32 height = Image_height(thisObj);
   int32 newWidth = Image_width(newObj);
   int32 newHeight = Image_height(newObj);
   Pixel transp = makePixelRGB(Image_transparentColor(thisObj));
   bool shrinkW = newWidth < width;
   bool shrinkH = newHeight < height;
   int32 srcCenterX = width << 8;
   int32 srcCenterY = height << 8;
   int32 dstCenterX = (shrinkW ? newWidth-1 : newWidth) << 8; // without -1, the imageScale buttons will miss the bottom/right pixels
   int32 dstCenterY = (shrinkH ? newHeight-1 : newHeight) << 8;
   int32 xScale = ((shrinkW ? newWidth-1 : newWidth)<<9)/(shrinkW ? width : width-1); // guich@tc112_28: use width/height-1 when expanding
   int32 yScale = ((shrinkH ? newHeight-1 : newHeight)<<9)/(shrinkH ? height : height-1);
   int32 xlimit = (width-1)<<9,  xlimit2 = (int32)((width-1.001) * (1<<9));
   int32 ylimit = (height-1)<<9, ylimit2 = (int32)((height-1.001) * (1<<9));
   int32 xs, ys;
   Object pixelsObj = (frameCount == 1) ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);
   int32 intPart,x,y,offsetY;
   int32 xFraction,yFraction;
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixelsObj),lowerLeft,lowerRight,upperLeft,upperRight,*t;
   int32 upperAverage, lowerAverage;
   bool useAlpha = Image_useAlpha(thisObj);
   dstCenterX += xScale>>1;
   dstCenterY += yScale>>1;

   for (y=0; y < newHeight; y++)
   {
      ys = (((((y<<9)-dstCenterY))<<9)/yScale) + srcCenterY;
      if (ys < 0) ys = 0; else
      if (ys >= ylimit) ys = ylimit2;

      intPart = (ys>>9)<<9;
      yFraction = ys - intPart;
      offsetY = intPart * width;

      for (x=0; x < newWidth; x++,p++)
      {
         xs = (((((x<<9)-dstCenterX))<<9)/xScale) + srcCenterX;
         if (xs < 0) xs = 0; else
         if (xs >= xlimit) xs = xlimit2;

         intPart = (xs>>9)<<9;
         xFraction = xs - intPart;
         t = pixels + ((offsetY + intPart) >> 9);

         lowerLeft  = *t;           if (lowerLeft.pixel  == transp) lowerLeft.pixel  = backColor;
         lowerRight = *(t+1);       if (lowerRight.pixel == transp) lowerRight.pixel = backColor;
         upperRight = *(t+width+1); if (upperRight.pixel == transp) upperRight.pixel = backColor;
         upperLeft  = *(t+width);   if (upperLeft.pixel  == transp) upperLeft.pixel  = backColor;

         upperAverage = (upperLeft.a<<9)  + xFraction * (upperRight.a - upperLeft.a);
         lowerAverage = (lowerLeft.a<<9)  + xFraction * (lowerRight.a - lowerLeft.a);
         p->a = (lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9;
         upperAverage = (upperLeft.r<<9)  + xFraction * (upperRight.r - upperLeft.r);
         lowerAverage = (lowerLeft.r<<9)  + xFraction * (lowerRight.r - lowerLeft.r);
         p->r = (lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9;
         upperAverage = (upperLeft.g<<9)  + xFraction * (upperRight.g - upperLeft.g);
         lowerAverage = (lowerLeft.g<<9)  + xFraction * (lowerRight.g - lowerLeft.g);
         p->g = (lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9;
         upperAverage = (upperLeft.b<<9)  + xFraction * (upperRight.b - upperLeft.b);
         lowerAverage = (lowerLeft.b<<9)  + xFraction * (lowerRight.b - lowerLeft.b);
         p->b = (lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9;
      }
   }
}

// Replace a color by another one
static void changeColors(Object obj, Pixel from, Pixel to)
{
   int32 frameCount = Image_frameCount(obj);
   Object pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj);
   Pixel *pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
   bool useAlpha = Image_useAlpha(obj);
   if (useAlpha)
   {
      for (; len-- > 0; pixels++)
         if ((*pixels & 0xFFFFFF) == from)
            *pixels = (*pixels & 0xFF000000) | to; // keep alpha unchanged
   }
   else
   {
      for (; len-- > 0; pixels++)
         if (*pixels == from)
            *pixels = to;
   }
   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
}

static void getScaledInstance(Object thisObj, Object newObj)
{
   Pixel* dstImageData = (Pixel*)ARRAYOBJ_START(Image_pixels(newObj));
   int32 frameCount = Image_frameCount(thisObj);
   Object pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);
   Pixel* srcImageData = (Pixel*)ARRAYOBJ_START(pixelsObj);
   int32 thisWidth = Image_width(thisObj) * frameCount;
   int32 thisHeight= Image_height(thisObj);
   int32 newWidth  = Image_width(newObj);
   int32 newHeight = Image_height(newObj);

   // guich: a modified version of the replicate scale algorithm.
   int32 h = newHeight << 1;
   int32 hi = thisHeight << 1;
   int32 hf = thisHeight / h;
   int32 wf = 0;
   int32 w = newWidth << 1;
   int32 wi = thisWidth << 1;
   int32 x,y;
   Pixel *dst,*src;

   for (y = 0; y < newHeight; y++, hf += hi)
   {
      wf = thisWidth / w;
      dst = dstImageData + y * newWidth;
      src = srcImageData + (hf / h) * thisWidth;
      for (x = 0; x < newWidth; x++, wf += wi)
         *dst++ = src[wf / w];
   }
}

static void getRotatedScaledInstance(Object thisObj, Object newObj, int32 percScale, int32 angle, Pixel color, int32 x0, int32 y0)
{
   int32 frameCount = Image_frameCount(thisObj);
   Pixel *pixelsIn = (Pixel*)ARRAYOBJ_START(Image_pixels(thisObj)), *pixelsIn0 = pixelsIn;
   Pixel *pixelsOut= (Pixel*)ARRAYOBJ_START(Image_pixels(newObj)),  *pixelsOut0= pixelsOut;
   int32 thisWidth = Image_width(thisObj);
   int32 thisHeight= Image_height(thisObj);
   int32 newWidth  = Image_width(newObj);
   int32 newHeight = Image_height(newObj);
   Pixel backColor;
   int32 sine=0;
   int32 cosine=0;
   int32 i,u,v, x,y, x00 = x0, y00 = y0,j,newHeight0 = newHeight,widthOfAllFrames;
   Pixel *pixelsOfAllFrames, *pixels, *out;

   /* xplying by 0x10000 allow integer math, while not loosing much prec. */
   backColor = color ? (Pixel)color : (Pixel)makePixelRGB(Image_transparentColor(thisObj));
   angle = angle % 360;
   if (angle < 0) angle += 360;
   switch (angle)
   {
      case 0:
         cosine = 0x640000 / percScale;
         break;
      case 90:
         sine = 0x640000 / percScale;
         break;
      case 180:
         cosine = -0x640000 / percScale;
         break;
      case 270:
         sine = -0x640000 / percScale;
         break;
      default:
      {
         double rad = angle * 0.0174532925;
         sine = (((int32)(sin(rad) * 0x10000)) * 100) / percScale;
         cosine = ((int32)(cos(rad) * 0x10000) * 100) / percScale;
      }
   }

   for (j = 0; j < frameCount; j++)
   {
      newHeight = newHeight0;
      x0 = x00;
      y0 = y00;
      if (frameCount > 1)
      {
         setCurrentFrame(thisObj, j);
         setCurrentFrame(newObj, j);
      }
      pixelsIn = pixelsIn0;
      pixelsOut= pixelsOut0;
      while (--newHeight >= 0)
      {
         out = pixelsOut;
         x = x0;
         y = y0;
         for (i=newWidth; --i >= 0; x += cosine, y += sine)
         {
            u = x>>16;
            v = y>>16;
            if (0 <= u && u < thisWidth && 0 <= v && v < thisHeight)
               *out++ = pixelsIn[v * thisWidth + u];
            else
               *out++ = backColor;
         }
         x0 -= sine;
         y0 += cosine;
         pixelsOut += newWidth;
      }
      // move pixels back
      if (frameCount > 1)
      {                                                   
         int32 n = newWidth;
         widthOfAllFrames = Image_widthOfAllFrames(newObj) - newWidth;
         pixelsOfAllFrames = (Pixel*)ARRAYOBJ_START(Image_pixelsOfAllFrames(newObj));
         pixels = (Pixel*)ARRAYOBJ_START(Image_pixels(newObj));
         pixelsOfAllFrames += j * newWidth;
         for (y = newHeight0; --y >= 0; pixelsOfAllFrames += widthOfAllFrames, n = newWidth)
            while (n-- > 0)
               *pixelsOfAllFrames++ = *pixels++;
      }
   }
}

// Generates 128 points from a kind of symmetrical yn = sum{1 to inf}(e-xt/tn)/dt,
// n being proportional to 'level'. The contrast level must be in the range -128 ... + 127
static void computeContrastTable(uint8 *table, int32 level)
{
   double factor;
   int32 i,v;
   uint8* tableEnd = table + 255;
   if (level < 0) // byte ranges -128 to +127
      factor = (level+128) / 128.0;
   else
      factor = 127.0 / max32(127 - level,1);
   for (i = 0; i <= 127; i++)
   {
      v = ((int32) (127.0 * pow(i / 127.0, factor))) & 0xff;
      *table++ = (uint8)v;
      *tableEnd-- = (uint8) (255 - v);
   }
}

static void getTouchedUpInstance(Object thisObj, Object newObj, int32 iBrightness, int32 iContrast) // (Lwaba/fx/Image;BB)Lwaba/fx/Image;
{
   enum
   {
      NO_TOUCHUP,
      BRITE_TOUCHUP,
      CONTRAST_TOUCHUP
   } touchup;

   PixelConv *in, *out, pc;
   int32 len;
   uint8 table[256];
   int32 m=0, k=0, max;
   int32 frameCount = Image_frameCount(thisObj);
   Object pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);
   bool useAlpha = Image_useAlpha(thisObj);

   touchup = NO_TOUCHUP;
   in = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   out= (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   len = ARRAYOBJ_LEN(pixelsObj);

   if (iContrast != 0)
   {
      touchup |= CONTRAST_TOUCHUP;
      computeContrastTable(table, (int8)iContrast);
   }
   if (iBrightness != 0)
   {
      double brightness = ((float)iBrightness+128.0)/128.0;  // [0.0 ... 2.0]
      touchup |= BRITE_TOUCHUP;
      if (brightness <= 1.0)
      {
         m = (int32)(sqrt(brightness) * 0x10000);
         k = 0;
         max = 0xFFFFFF;
      }
      else
      {
         double f;
         max = (int32)(0xFF / brightness);
         f = brightness - 1.0;
         f = f * f;
         k = (int32)(f * 0xFF0000);
         m = (int32)((1.0-f) * brightness * 0x10000);
      }
   }
   pc.pixel = makePixelRGB(Image_transparentColor(thisObj));
   switch (touchup)
   {
      case BRITE_TOUCHUP:
         for (; len-- > 0; in++,out++)
         {
            out->a = in->a;
            out->r = min32(255, (in->r * m + k) >> 16);
            out->g = min32(255, (in->g * m + k) >> 16);
            out->b = min32(255, (in->b * m + k) >> 16);
         }
         pc.r = min32(255, (pc.r * m + k) >> 16);
         pc.g = min32(255, (pc.g * m + k) >> 16);
         pc.b = min32(255, (pc.b * m + k) >> 16);
         break;
      case CONTRAST_TOUCHUP:
         for (; len-- > 0; in++,out++)
         {
            out->a = in->a;
            out->r = table[in->r];
            out->g = table[in->g];
            out->b = table[in->b];
         }
         pc.r = table[pc.r];
         pc.g = table[pc.g];
         pc.b = table[pc.b];
         break;
      default: // case CTRSTBRITE_TOUCHUP:
         for (; len-- > 0; in++,out++)
         {
            out->a = in->a;
            out->r = min32(255, (table[in->r] * m + k) >> 16);
            out->g = min32(255, (table[in->g] * m + k) >> 16);
            out->b = min32(255, (table[in->b] * m + k) >> 16);
         }
         pc.r = min32(255, (table[pc.r] * m + k) >> 16);
         pc.g = min32(255, (table[pc.g] * m + k) >> 16);
         pc.b = min32(255, (table[pc.b] * m + k) >> 16);
         break;
   }
   Image_transparentColor(newObj) = useAlpha ? -1 : (pc.r << 16) | (pc.g << 8) | pc.b;
}

static void getFadedInstance(Object thisObj, Object newObj, int32 backColor) // (Lwaba/fx/Image;BB)Lwaba/fx/Image; - guich@tc110_50
{
   PixelConv *in, *out, t,back;
   int32 len,r,g,b;
   int32 frameCount = Image_frameCount(thisObj);
   Object pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);

   in = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   out= (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   len = ARRAYOBJ_LEN(pixelsObj);

   t.pixel = makePixelRGB(Image_transparentColor(thisObj));
   back.pixel = backColor;
   r = back.r;
   g = back.g;
   b = back.b;
   for (; len-- > 0; in++,out++)
   {
      if (in->pixel == t.pixel) // don't change the transparent color
         out->pixel = t.pixel;
      else
      {
         out->a = in->a;
         out->r = (in->r + (int32)r) >> 1;
         out->g = (in->g + (int32)g) >> 1;
         out->b = (in->b + (int32)b) >> 1;
      }
   }
}

static void getPixelRow(Object obj, Object outObj, int32 y)
{
   Object pixObj = (Image_frameCount(obj) > 1) ? Image_pixelsOfAllFrames(obj) : Image_pixels(obj);
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixObj);
   int8* out = (int8*)ARRAYOBJ_START(outObj);
   int32 width = (Image_frameCount(obj) > 1) ? Image_widthOfAllFrames(obj) : Image_width(obj);
   for (pixels += y * width; width-- > 0; pixels++)
   {
      *out++ = pixels->r;
      *out++ = pixels->g;
      *out++ = pixels->b;
   }
}

