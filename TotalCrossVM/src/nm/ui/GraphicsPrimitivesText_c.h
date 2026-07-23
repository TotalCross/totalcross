// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_H
#define INTERP(j,f,shift) (j + (((f - j) * transparency) >> shift)) & 0xFF

static uint8 _ands8[8] = {0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x01};
uint8* getResizedCharPixels(Context currentContext, UserFont uf, JChar ch, int32 w, int32 h);

static void drawText(Context currentContext, TCObject g, JCharP text, int32 chrCount, int32 x0, int32 y0, Pixel foreColor, int32 justifyWidth)
{
   TCObject fontObj = Graphics_font(g);
   int32 startBit, currentBit, incY, y1, r, rmax, istart;
   uint8 *bitmapTable, *ands, *current, *start;
   uint16* bitIndexTable;
   int32 rowWIB, offset, xMin, xMax, yMin, yMax, x, y, yDif, width, width0, height, spaceW = 0, k, clipX1,clipX2,clipY1,clipY2, pitch;
   Pixel transparency, *row0, *row;
   PixelConv *i;
   bool isNibbleStartingLow, isLowNibble, isClipped;
   int aaType;
   JChar ch, first, last;
   UserFont uf = null;
   PixelConv fc;
   int32 extraPixelsPerChar = 0, extraPixelsRemaining = -1, rem;
   uint8 *ands8 = _ands8;
   int32 fcR, fcG, fcB;
#ifdef __gl2_h_
   int32 charXY[2];
   float *xya;
#endif
   int32 diffW;
   bool isVert = Graphics_isVerticalText(g);
   bool isGL = Graphics_useOpenGL(g);

   if (!text || chrCount == 0 || fontObj == null) return;

   fc.pixel = foreColor;
   fcR = fc.r;
   fcG = fc.g;
   fcB = fc.b;

   uf = loadUserFontFromFontObj(currentContext, fontObj, ' ');
   if (uf == null) return;
   diffW = uf->ubase && uf->isDefaultFont;
   rowWIB = uf->rowWidthInBytes;
   bitIndexTable = uf->bitIndexTable;
   bitmapTable = uf->bitmapTable;
   first = uf->fontP.firstChar;
   last = uf->fontP.lastChar;

   aaType = uf->fontP.antialiased;
   height = uf->fontP.maxHeight;
   incY = height + justifyWidth;

   x0 += Graphics_transX(g);
   y0 += Graphics_transY(g);

   if (justifyWidth > 0)
   {
      while (chrCount > 0 && text[chrCount - 1] <= (JChar)' ')
         chrCount--;
      if (chrCount == 0) return;
      rem = justifyWidth - getJCharPWidth(currentContext, fontObj, text, chrCount);
      if (rem > 0)
      {
         extraPixelsPerChar = rem / chrCount;
         extraPixelsRemaining = rem % chrCount;
      }
   }
   clipX1 = Graphics_clipX1(g);
   clipX2 = Graphics_clipX2(g);
   clipY1 = Graphics_clipY1(g);
   clipY2 = Graphics_clipY2(g);

   xMax = xMin = (x0 < clipX1) ? clipX1 : x0;
   yMax = y0 + (isVert ? chrCount * incY : height);
   yMin = (y0 < clipY1) ? clipY1 : y0;
   if (yMax >= clipY2)
      yMax = clipY2;
   if (getGraphicsPixels(g) == null)
      return;
   row0 = getGraphicsPixels(g) + yMin * Graphics_pitch(g);
   yDif = yMin - y0;
   y = y0;

   pitch = Graphics_pitch(g);
   for (k = 0; k < chrCount; k++) // guich@402
   {
      ch = *text++;
      if (ch <= ' ' || ch == 160)
      {
         if (ch == ' ' || ch == '\t' || ch == 160)
         {
            if (isVert)
               y += ch == '\t' ? incY * *tabSizeField : incY;
            else
            {
               x0 += getJCharWidth(currentContext, fontObj, ch)+extraPixelsPerChar;
               if (k <= extraPixelsRemaining)
                  x0++;
            }
         }
         continue;
      }
      if (uf == null || ch < first || ch > last)
      {
         uf = loadUserFontFromFontObj(currentContext, fontObj, ch);
         if (uf == null || ch < uf->fontP.firstChar || ch > uf->fontP.lastChar) // invalid char - guich@tc122_23: must also check the font's range
         {
            x0 += spaceW ? spaceW : (spaceW=getJCharWidth(currentContext, fontObj, ' ')) + extraPixelsPerChar;
            if (k <= extraPixelsRemaining)
               x0++;
            continue;
         }
         rowWIB = uf->rowWidthInBytes;
         bitIndexTable = uf->bitIndexTable;
         bitmapTable = uf->bitmapTable;
         first = uf->fontP.firstChar;
         last = uf->fontP.lastChar;
      }
#ifdef __gl2_h_
      if (!checkGLfloatBuffer(currentContext, uf->fontP.maxHeight * uf->fontP.maxWidth))
         return;
#endif
      // valid char, get its start
      offset = bitIndexTable[ch];
      width0 = width = bitIndexTable[ch+1] - offset - diffW;
      isClipped = false;

      if (uf->ubase != null) width = width * height / uf->ubase->fontP.maxHeight;

      if ((xMax = x0 + width) > clipX2)
      {
         isClipped = true;
         xMax = clipX2;
      }
      y1 = y; r=0;
      istart = 0;
      if (!isVert)
      {
         if (y0 < yMin) // guich@tc100b4_1: skip rows before yMin
            istart += yMin-y0;
         y = yMin;
      }
      else
      if (y < yMin)
      {
         r += yMin-y;
         istart += yMin-y; // guich@tc100b4_1: skip rows before yMin
         y = yMin;
      }
      row0 = getGraphicsPixels(g) + y * Graphics_pitch(g);
      rmax = (y+height > yMax) ? yMax - y : height;
      isClipped |= x0 < clipX1 || istart != 0 || rmax != height;

      switch (aaType)
      {
         case AA_NO:
         {
            start     = bitmapTable + (offset >> 3) + rowWIB * istart;
            startBit  = offset & 7;

            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {
               int32 nn=0;

               xya = glXYA;
               for (; r < rmax; start+=rowWIB, r++,row += pitch,y++)    // draw each row
               {
                  current = start;
                  ands = ands8 + (currentBit = startBit);
                  for (x=x0; x < xMax; x++)
                  {
                     if ((*current & *ands++) != 0 && x >= xMin)
                     {
                        *xya++ = (float)x;
                        *xya++ = (float)y;
                        *xya++ = 1;
                        nn++;
                     }
                     if (++currentBit == 8)   // finished this uint8?
                     {
                        currentBit = 0;       // reset counter
                        ands = ands8;         // reset test bit pointer
                        ++current;            // inc current uint8
                     }
                  }
               }
               if (nn > 0) // flush vertices buffer
                  glDrawPixels(nn,foreColor);
            }
            else
   #endif
            for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
            {
               current = start;
               ands = ands8 + (currentBit = startBit);
               for (x=x0; x < xMax; x++)
               {
                  if ((*current & *ands++) != 0 && x >= xMin)
                     row[x] = foreColor;
                  if (++currentBit == 8)   // finished this uint8?
                  {
                     currentBit = 0;       // reset counter
                     ands = ands8;         // reset test bit pointer
                     ++current;            // inc current uint8
                  }
               }
            }
            break;
         }
         case AA_4BPP:
         {
            start = bitmapTable + (offset >> 1) + rowWIB * istart;
            isNibbleStartingLow = (offset & 1) == 1;
            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {
               int32 nn=0;

               xya = glXYA;
               for (; r < rmax; start+=rowWIB, r++,y++)    // draw each row
               {
                  current = start;
                  isLowNibble = isNibbleStartingLow;
                  for (x=x0; x < xMax; x++)
                  {
                     transparency = isLowNibble ? (*current++ & 0xF) : ((*current >> 4) & 0xF);
                     isLowNibble = !isLowNibble;
                     if (transparency == 0 || x < xMin)
                        continue;

                     // alpha
                     // vertices
                     *xya++ = (float)x;
                     *xya++ = (float)y;
                     *xya++ = ftransp[transparency];
                     nn++;
                  }
               }
               if (nn > 0) // flush vertices buffer
                  glDrawPixels(nn,foreColor);
            }
            else
   #endif
               for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
               {
                  current = start;
                  isLowNibble = isNibbleStartingLow;
                  i = (PixelConv*)&row[x0];
                  for (x=x0; x < xMax; x++,i++)
                  {
                     transparency = isLowNibble ? (*current++ & 0xF) : ((*current >> 4) & 0xF);
                     isLowNibble = !isLowNibble;
                     if (transparency == 0 || x < xMin)
                        continue;
                     if (transparency == 0xF)
                        i->pixel = foreColor;
                     else
                     {
                        i->r = INTERP(i->r, fcR, 4);
                        i->g = INTERP(i->g, fcG, 4);
                        i->b = INTERP(i->b, fcB, 4);
                     }
                  }
               }
         }
         break;
         case AA_8BPP: // textured font files
         {
            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {
               if (!isClipped && getCharPosInTexture(currentContext, uf->ubase, ch, charXY))
/*text*/          glDrawTexture(uf->ubase->textureId,
                               charXY[0], charXY[1], width0, uf->ubase->fontP.maxHeight, // source char position
                               x0, y, width, height,                                     // target bitmap position
                               uf->ubase->maxW, uf->ubase->maxH, &fc, 255);              // total bitmap size
               else
               {
                  uint8* alpha = getResizedCharPixels(currentContext, uf->ubase, ch, width+diffW, height);
                  if (alpha)
                  {
                     int32 nn=0;
                     rowWIB = width+diffW;
                     start = alpha + istart * rowWIB;
                     xya = glXYA;
                     for (; r < rmax; start+=rowWIB, r++,y++)    // draw each row
                     {
                        current = start;
                        for (x=x0; x < xMax; x++)
                        {
                           transparency = *current++;
                           if (transparency == 0 || x < xMin)
                              continue;

                           // alpha
                           // vertices
                           *xya++ = (float)x;
                           *xya++ = (float)y;
                           *xya++ = f255[transparency];
                           nn++;
                        }
                     }
                     if (nn > 0) // flush vertices buffer
                        glDrawPixels(nn,foreColor);
                  }
               }
            }
            else
   #endif // case 2
            {
               uint8* alpha = getResizedCharPixels(currentContext, uf->ubase, ch, width+diffW, height);
               if (alpha)
               {
                  rowWIB = width+diffW;
                  start = alpha + istart * rowWIB;
                  for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
                  {
                     current = start;
                     i = (PixelConv*)&row[x0];
                     for (x=x0; x < xMax; x++,i++)
                     {
                        transparency = *current++;
                        if (transparency == 0 || x < xMin)
                           continue;
                        if (transparency == 0xFF)
                           i->pixel = foreColor;
                        else
                        {
                           i->r = INTERP(i->r, fcR, 8);
                           i->g = INTERP(i->g, fcG, 8);
                           i->b = INTERP(i->b, fcB, 8);
                        }
                     }
                  }
               }
            }
         }
      }
      if (isVert)
      {
         y = y1 + incY;
         if (y >= yMax)
            break;
      }
      else
      {
         if (xMax >= clipX2)
         {
            xMax = clipX2;
            break;
         }
         x0 = xMax; // next character
         x0 += extraPixelsPerChar;
         if (k <= extraPixelsRemaining)
            x0++;
      }
   }
#ifndef __gl2_h_
   if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, xMin, yMin, (xMax - xMin), (yMax - yMin));
#else
   if (Graphics_isImageSurface(g))
      Image_changed(Graphics_surface(g)) = true;
   else
      currentContext->fullDirty = true;
#endif
}
#else
static void drawText(Context currentContext, TCObject g, JCharP text, int32 chrCount, int32 x, int32 y, Pixel foreColor, int32 justifyWidth)
{
   TCObject fontObj = Graphics_font(g);
   int32 fontSize = (int)(Font_size(fontObj) * (*tcSettings.screenDensityPtr));
   int32 typefaceIndex = Font_skiaIndex(fontObj);

   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
   skia_drawText(skiaSurfaceForGraphics(g), text, chrCount * sizeof(JChar), x, y + fontSize, foreColor | Graphics_alpha(g), justifyWidth, fontSize, typefaceIndex);
   skia_restoreClip(skiaSurfaceForGraphics(g));

   markDirty(currentContext, g, x, y, skia_stringWidth(text, chrCount * sizeof(JChar), typefaceIndex, fontSize), fontSize);
}
#endif
