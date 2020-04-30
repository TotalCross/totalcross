// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <math.h>
#if defined WP8
#include "openglWrapper.h"
#endif


static void setCurrentFrame(TCObject obj, int32 nr)
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

static void applyColor(TCObject obj, Pixel color) // guich@tc112_24
{
   int32 frameCount = Image_frameCount(obj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj);
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   PixelConv c;
   double k = 128;
   int32 mr,mg,mb;

   c.pixel = color;

   mr = (int32) (sqrt((c.r + k) / k) * 0x10000);
   mg = (int32) (sqrt((c.g + k) / k) * 0x10000);
   mb = (int32) (sqrt((c.b + k) / k) * 0x10000);

   for (; len-- > 0; pixels++)
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

#define BIAS_BITS 16
#define BIAS (1<<BIAS_BITS)

static bool getSmoothScaledInstance(TCObject thisObj, TCObject newObj) // guich@tc130: changed area-averaging to Catmull-Rom resampling
{
   bool fSuccess = false;
   PixelConv* ob = (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   int32 frameCount = Image_frameCount(thisObj);
   int32 width = Image_width(thisObj) * frameCount;
   int32 height = Image_height(thisObj);
   int32 newWidth = Image_width(newObj);
   int32 newHeight = Image_height(newObj);
   TCObject pixelsObj = (frameCount == 1) ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);
   PixelConv *ib = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   PixelConv pval;                                                                                                                                   

   int32 i=0, j, n, s, iweight,a,r,g,b;
   double xScale, yScale;

   // Temporary values
   int32 * v_weight = null; // Weight contribution    [newHeight][maxContribs]
   int32 * v_pixel = null;  // Pixel that contributes [newHeight][maxContribs]
   int32 * v_count = null;  // How many contribution for the pixel [newHeight]
   int32 * v_wsum = null;   // Sum of weights [newHeight]

   PixelConv * tb;        // Temporary (intermediate buffer)

   double center;         // Center of current sampling
   double weight;         // Current wight
   int32 left=0;           // Left of current sampling
   int32 right=0;          // Right of current sampling

   int32 * p_weight;     // Temporary pointer
   int32 * p_pixel;      // Temporary pointer

   int32 maxContribs,maxContribsXY;   // Almost-const: max number of contribution for current sampling
   double scaledRadius,scaledRadiusY;   // Almost-const: scaled radius for downsampling operations
   double filterFactor;   // Almost-const: filter factor for downsampling operations

   if (width <= 0 || height <= 0 || newWidth <= 0 || newHeight <= 0) 
      return true;

   setObjectLock(newObj, LOCKED);
   xScale = ((double)newWidth / width);
   yScale = ((double)newHeight / height);

   if (newWidth > width)
   {
      /* Horizontal upsampling */
      filterFactor = 1.0;
      scaledRadius = 2;
   }
   else
   { 
      /* Horizontal downsampling */
      filterFactor = xScale;
      scaledRadius = 2 / xScale;
   }                                                             
   
   /* The maximum number of contributions for a target pixel */
   maxContribs  = (int32) (2 * scaledRadius  + 1);

   scaledRadiusY = yScale > 1.0 ? 2 : 2 / yScale;
   maxContribsXY = (int32) (2 * (scaledRadiusY > scaledRadius ? scaledRadiusY : scaledRadius) + 1);

   /* Pre-allocating all of the needed memory */
   s = max32(newWidth,newHeight);
   tb  = (PixelConv * ) xmalloc (newWidth * height * sizeof( PixelConv ) );
   v_weight = (int32 *) xmalloc(s * maxContribsXY * sizeof(int32)); /* weights */
   v_pixel  = (int32 *) xmalloc(s * maxContribsXY * sizeof(int32)); /* the contributing pixels */
   v_count  = (int32 *) xmalloc(s * sizeof(int32)); /* how may contributions for the target pixel */
   v_wsum   = (int32 *) xmalloc(s * sizeof(int32)); /* sum of the weights for the target pixel */
      
   if (!tb || !v_weight || !v_pixel || !v_count || !v_wsum) goto Cleanup;
      
   /* Pre-calculate weights contribution for a row */
   for (i = 0; i < newWidth; i++)
   {
      p_weight = v_weight + i * maxContribs;
      p_pixel  = v_pixel  + i * maxContribs;

      center = ((double)i)/xScale;
      left = (int32)((center + .5) - scaledRadius);
      right = (int32)(left + 2 * scaledRadius);

      for (j = left; j <= right; j++)
      {
         double cc;
         if (j < 0 || j >= width)
            continue;
         // Catmull-rom resampling
         cc = (center-j) * filterFactor;
         if (cc < 0.0) cc = -cc;
         if (cc <= 1.0) weight =  1.5 * cc * cc * cc - 2.5 * cc * cc + 1; else
         if (cc <= 2.0) weight = -0.5 * cc * cc * cc + 2.5 * cc * cc - 4 * cc + 2;
         else continue;
         if (weight == 0)
            continue;
         iweight = (int32)(weight * BIAS);

         n = v_count[i]; // Since v_count[i] is our current index
         p_pixel[n] = j;
         p_weight[n] = iweight;
         v_wsum[i] += iweight;
         v_count[i]++; // Increment contribution count
      }
   }

   /* Filter horizontally from input to temporary buffer */
   for ( i = 0; i < newWidth; i++)
   {
      int32 wsum = v_wsum[i];
      int32 count = v_count[i];
      for (n = 0; n < height; n++)
      {
         p_weight = v_weight + i * maxContribs;
         p_pixel  = v_pixel  + i * maxContribs;

         a = r = g = b = 0;
         for (j=0; j < count; j++)
         {
            int32 iweight = *p_weight++;
            pval.pixel = ib[*p_pixel++ + n * width].pixel;
            // Acting on color components
            a += pval.a * iweight;
            r += pval.r * iweight;
            g += pval.g * iweight;
            b += pval.b * iweight;
         }
         if (wsum == 0) continue;
         a /= wsum; if (a > 255) a = 255; else if (a < 0) a = 0;
         r /= wsum; if (r > 255) r = 255; else if (r < 0) r = 0;
         g /= wsum; if (g > 255) g = 255; else if (g < 0) g = 0;
         b /= wsum; if (b > 255) b = 255; else if (b < 0) b = 0;
         pval.a = a;
         pval.r = r;
         pval.g = g;
         pval.b = b;
         tb[i+n*newWidth] = pval;
      }
   }

   /* Going to vertical stuff */
   if (newHeight > height)
   {
      filterFactor = 1.0;
      scaledRadius = 2;
   }
   else
   {
      filterFactor = yScale;
      scaledRadius = 2 / yScale;
   }
   maxContribs  = (int32) (2 * scaledRadius  + 1);

   p_weight = v_weight;
   p_pixel  = v_pixel;
   for (i = s*maxContribs; --i >= 0;)
      *p_weight++ = *p_pixel++ = 0;

   /* Pre-calculate filter contributions for a column */
   for (i = 0; i < newHeight; i++)
   {
      p_weight = v_weight + i * maxContribs;
      p_pixel  = v_pixel  + i * maxContribs;

      v_count[i] = 0;
      v_wsum[i] = 0;

      center = ((double) i) / yScale;
      left = (int32) (center+.5 - scaledRadius);
      right = (int32) (left + 2 * scaledRadius);

      for (j = left; j <= right; j++)
      {
         double cc;
         if (j < 0 || j >= height) continue;
         // Catmull-rom resampling
         cc = (center-j) * filterFactor;
         if (cc < 0.0) cc = -cc;
         if (cc <= 1.0) weight =  1.5 * cc * cc * cc - 2.5 * cc * cc + 1; else
         if (cc <= 2.0) weight = -0.5 * cc * cc * cc + 2.5 * cc * cc - 4 * cc + 2;
         else continue;
         if (weight == 0)
            continue;
         iweight = (int32)(weight * BIAS);

         n = v_count[i]; /* Our current index */
         p_pixel[n] = j;
         p_weight[n] = iweight;
         v_wsum[i] += iweight;
         v_count[i]++; /* Increment the contribution count */
      }
   }

   /* Filter vertically from work to output */
   for (i = 0; i < newHeight; i++)
   {
      int32 wsum = v_wsum[i];
      int32 count = v_count[i];
      for (n = 0; n < newWidth; n++)
      {
         p_weight = v_weight + i * maxContribs;
         p_pixel  = v_pixel  + i * maxContribs;

         a = r = g = b = 0;
         for (j = 0; j < count; j++)
         {
            int iweight = *p_weight++;
            pval = tb[ n + newWidth * *p_pixel++]; // Using val as temporary storage 
            // Acting on color components 
            a += pval.a * iweight;
            r += pval.r * iweight;
            g += pval.g * iweight;
            b += pval.b * iweight;
         }
         a /= wsum; if (a > 255) a = 255; else if (a < 0) a = 0;
         r /= wsum; if (r > 255) r = 255; else if (r < 0) r = 0;
         g /= wsum; if (g > 255) g = 255; else if (g < 0) g = 0;
         b /= wsum; if (b > 255) b = 255; else if (b < 0) b = 0;
         ob->a = a;
         ob->r = r;
         ob->g = g;
         ob->b = b;
         ob++;
      }
   }

   fSuccess = true;

Cleanup: /* CLEANUP */
   if (tb) xfree(tb);
   if (v_weight) xfree(v_weight);
   if (v_pixel) xfree(v_pixel);
   if (v_count) xfree(v_count);
   if (v_wsum) xfree(v_wsum);
   setObjectLock(newObj, UNLOCKED);
   return fSuccess;
}

// Replace a color by another one
static void changeColors(TCObject obj, Pixel from, Pixel to)
{
   int32 frameCount = Image_frameCount(obj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj);
   Pixel *pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
   for (; len-- > 0; pixels++)
      if (*pixels == from)
         *pixels = to;
   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
}

static void getScaledInstance(TCObject thisObj, TCObject newObj)
{
   Pixel* dstImageData = (Pixel*)ARRAYOBJ_START(Image_pixels(newObj));
   int32 frameCount = Image_frameCount(thisObj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);
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

   if (newWidth != 0 && newHeight != 0 && thisWidth != 0 && thisHeight != 0)
   for (y = 0; y < newHeight; y++, hf += hi)
   {
      wf = thisWidth / w;
      dst = dstImageData + y * newWidth;
      src = srcImageData + (hf / h) * thisWidth;
      for (x = 0; x < newWidth; x++, wf += wi)
         *dst++ = src[wf / w];
   }
}

static void getRotatedScaledInstance(TCObject thisObj, TCObject newObj, int32 percScale, int32 angle, Pixel color, int32 x0, int32 y0)
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
   backColor = (Pixel)color;
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

static void getTouchedUpInstance(TCObject thisObj, TCObject newObj, int32 iBrightness, int32 iContrast)
{
   enum
   {
      NO_TOUCHUP,
      BRITE_TOUCHUP,
      CONTRAST_TOUCHUP
   } touchup;

   PixelConv *in, *out;
   int32 len;
   uint8 table[256];
   int32 m=0, k=0, max;
   int32 frameCount = Image_frameCount(thisObj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);

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
      double brightness = ((double)iBrightness+128.0)/128.0;  // [0.0 ... 2.0]
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
         break;
      case CONTRAST_TOUCHUP:
         for (; len-- > 0; in++,out++)
         {
            out->a = in->a;
            out->r = table[in->r];
            out->g = table[in->g];
            out->b = table[in->b];
         }
         break;
      default: // case CTRSTBRITE_TOUCHUP:
         for (; len-- > 0; in++,out++)
         {
            out->a = in->a;
            out->r = min32(255, (table[in->r] * m + k) >> 16);
            out->g = min32(255, (table[in->g] * m + k) >> 16);
            out->b = min32(255, (table[in->b] * m + k) >> 16);
         }
         break;
   }
}

static void getFadedInstance(TCObject thisObj, TCObject newObj, int32 backColor) // guich@tc110_50
{
   PixelConv *in, *out,back;
   int32 len,r,g,b;
   int32 frameCount = Image_frameCount(thisObj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);

   in = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   out= (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   len = ARRAYOBJ_LEN(pixelsObj);

   back.pixel = backColor;
   r = back.r;
   g = back.g;
   b = back.b;
   for (; len-- > 0; in++,out++)
   {
      out->a = in->a;
      out->r = (in->r + (int32)r) >> 1;
      out->g = (in->g + (int32)g) >> 1;
      out->b = (in->b + (int32)b) >> 1;
      {
         out->a = in->a;
         out->r = (in->r + (int32)r) >> 1;
         out->g = (in->g + (int32)g) >> 1;
         out->b = (in->b + (int32)b) >> 1;
      }
   }
}

static void getAlphaInstance(TCObject thisObj, TCObject newObj, int32 delta) // guich@tc110_50
{
   PixelConv *in, *out;
   int32 len;
   int32 frameCount = Image_frameCount(thisObj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);

   in = (PixelConv*)ARRAYOBJ_START(pixelsObj);
   out= (PixelConv*)ARRAYOBJ_START(Image_pixels(newObj));
   len = ARRAYOBJ_LEN(pixelsObj);

   for (; len-- > 0; in++,out++)
   {                         
      int32 a = in->a;
      out->pixel = in->pixel;
      if (a != 0)
      {
         a += delta;
         if (a < 0) a = 0; else if (a > 255) a = 255;
            out->a = a;
      }
   }
}

static void getPixelRow(Context currentContext, TCObject obj, TCObject outObj, int32 y)
{
   TCObject pixObj = (Image_frameCount(obj) > 1) ? Image_pixelsOfAllFrames(obj) : Image_pixels(obj);
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixObj);
   int8* out = (int8*)ARRAYOBJ_START(outObj);
   int32 width = (Image_frameCount(obj) > 1) ? Image_widthOfAllFrames(obj) : Image_width(obj);
   if (checkArrayRange(currentContext, outObj, 0, width*4))
      for (pixels += y * width; width-- > 0; pixels++)
      {
         *out++ = pixels->r;
         *out++ = pixels->g;
         *out++ = pixels->b;                                   
         *out++ = pixels->a;
      }
}

static void applyColor2(TCObject obj, Pixel color)
{
   int32 frameCount = Image_frameCount(obj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len0 = ARRAYOBJ_LEN(pixelsObj), len;
   PixelConv *pixels0 = (PixelConv*)ARRAYOBJ_START(pixelsObj), *pixels;
   PixelConv c;
   int32 r2,g2,b2,hi=0,hiR,hiG,hiB,m;
   PixelConv hip;
   bool changeA;

   hip.pixel = 0;
   c.pixel = color;

   r2 = c.r;
   g2 = c.g;
   b2 = c.b;
   changeA = c.a == 0xAA;                   

   // the given color argument will be equivalent to the brighter color of this image. Here we search for that color
   for (len = len0, pixels = pixels0; len-- > 0; pixels++)
      if (pixels->a == 0xFF) // consider only opaque pixels
      {
         m = (pixels->r + pixels->g + pixels->b) / 3;
         if (m > hi) {hi = m; hip = *pixels;}
      }
   hiR = hip.r;
   hiG = hip.g;
   hiB = hip.b;        
   if (hiR == 0) hiR = 255;
   if (hiG == 0) hiG = 255;
   if (hiB == 0) hiB = 255;
   hi = hiR > hiG ? hiR : hiG; hi = hi > hiB ? hi : hiB;
   
   for (len = len0, pixels = pixels0; len-- > 0; pixels++)
   {
      int32 r = pixels->r * r2 / hiR;
      int32 g = pixels->g * g2 / hiG;
      int32 b = pixels->b * b2 / hiB;
      if (r > 255) r = 255;
      if (g > 255) g = 255;
      if (b > 255) b = 255;
      if (changeA)                                      
      {
         int32 a = pixels->r > pixels->g ? pixels->r : pixels->g; if (pixels->b > a) a = pixels->b;
         pixels->a = a*255/hi;
      }
      pixels->r = r;
      pixels->g = g;
      pixels->b = b;
   }

   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
}

void setTransparentColor(TCObject obj, Pixel color);
void setTransparentColor(TCObject obj, Pixel color)
{
   int32 frameCount = Image_frameCount(obj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj);
   Pixel *pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
   if ((int32)color == -1) // no transparent pixels?
      for (; len-- > 0;)
         *pixels++ |= 0xFF000000;
   else
      for (; len-- > 0; pixels++)
      {
         Pixel p = *pixels & 0xFFFFFF;
         *pixels = (p == color) ? color : p | 0xFF000000; // if is the transparent color, set the alpha to 0, otherwise, set to full bright
      }
   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
}

static int timestampOldLimit;
void onImage(int32 it, VoidP ptr)
{
   TCObject img = (TCObject)ptr;
   if (!Surface_isImage(ptr)) // Image can be created and an exception be thrown before the constructor is called
      return;

   Image_changed(img) = true; //applyChanges(lifeContext, img); - update only when the image is going to be painted
}

static bool nativeEquals(TCObject thisObj, TCObject otherObj)
{
   Pixel *p1,*p2;
   int32 len;
   int32 frameCount = Image_frameCount(thisObj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(thisObj) : Image_pixelsOfAllFrames(thisObj);

   p1 = (Pixel*)ARRAYOBJ_START(pixelsObj);
   p2 = (Pixel*)ARRAYOBJ_START(Image_pixels(otherObj));
   len = ARRAYOBJ_LEN(pixelsObj);

   for (; len-- > 0; p1++,p2++)
      if (*p1 != *p2)
         return false;
   return true;
}

static void applyFade(TCObject obj, int32 fadeValue)
{
   int32 frameCount = Image_frameCount(obj);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
   int32 len = ARRAYOBJ_LEN(pixelsObj), r,g,b;
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixelsObj);

   for (; len-- > 0; pixels++)
   {
      r = pixels->r * fadeValue; pixels->r = (r+1 + (r >> 8)) >> 8;
      g = pixels->g * fadeValue; pixels->g = (g+1 + (g >> 8)) >> 8;
      b = pixels->b * fadeValue; pixels->b = (b+1 + (b >> 8)) >> 8;
   }

   if (frameCount != 1)
   {
      Image_currentFrame(obj) = 2;
      setCurrentFrame(obj, 0);
   }
   Image_changed(obj) = true;
}
