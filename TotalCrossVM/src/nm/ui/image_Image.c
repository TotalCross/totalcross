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



#include "tcvm.h"
#include "ImagePrimitives_c.h"

void jpegLoad(Context currentContext, Object imageInstance, Object inputStreamObj, Object bufObj, TCZFile tcz, char* first4);
void pngLoad(Context currentContext, Object imageInstance, Object inputStreamObj, Object bufObj, TCZFile tcz, char* first4);

//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageLoad_s(NMParams p) // totalcross/ui/image/Image native private void imageLoad(String path);
{
   char path[256];
   Object imageObj = p->obj[0];
   Object pathObj = p->obj[1];
   TCZFile tcz;

   String2CharPBuf(pathObj, path);
   tcz = tczGetFile(path, false);
   if (tcz != null)
   {
      uint8 magic[4]; // read the magic to find if its a png or a jpeg (note that jpeg has no magic)
      tczRead(tcz, magic, 4);
      if (magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G')
         pngLoad(p->currentContext, imageObj, null, null, tcz, magic);
      else
         jpegLoad(p->currentContext, imageObj, null, null, tcz, magic);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageParse_sB(NMParams p) // totalcross/ui/image/Image native private void imageParse(totalcross.io.Stream in, byte []buf);
{
   Object imageObj = p->obj[0];
   Object streamObj = p->obj[1];
   Object bufObj = p->obj[2];
   uint8* buf = ARRAYOBJ_START(bufObj);
   char magic[4];
   xmove4(magic, buf); // buf already comes filled from Java with the first 4 bytes
   if ((magic[0] & 0xFF) == 0x89 && magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G')
      pngLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic);
   else
      jpegLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_changeColors_ii(NMParams p) // totalcross/ui/image/Image native public void changeColors(int from, int to);
{
   Object thisObj = p->obj[0];
   Pixel from = makePixelRGB(p->i32[0]);
   Pixel to = makePixelRGB(p->i32[1]);
   changeColors(thisObj, from, to);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getPixelRow_Bi(NMParams p) // totalcross/ui/image/Image native protected void getPixelRow(byte []fillIn, int y);
{
   Object thisObj = p->obj[0];
   Object fillIn = p->obj[1];
   int32 y = p->i32[0];
   getPixelRow(thisObj, fillIn, y);
}
//////////////////////////////////////////////////////////////////////////
typedef enum
{
   SCALED_INSTANCE,
   SMOOTH_SCALED_INSTANCE,
   ROTATED_SCALED_INSTANCE,
   TOUCHEDUP_INSTANCE,
   FADED_INSTANCE
} FuncType;
TC_API void tuiI_getModifiedInstance_iiiiiii(NMParams p) // totalcross/ui/image/Image native private void getModifiedInstance(totalcross.ui.image.Image newImg, int angle, int percScale, int color, int brightness, int contrast, int type);
{
   Object thisObj = p->obj[0];
   Object newObj = p->obj[1];
   int32 percScale = p->i32[0];
   int32 angle = p->i32[1];
   Pixel color = makePixelRGB(p->i32[2]);
   FuncType type = (FuncType)p->i32[5];
   switch (type)
   {
      case SCALED_INSTANCE:
         getScaledInstance(thisObj, newObj);
         break;
      case SMOOTH_SCALED_INSTANCE:
         Image_transparentColor(newObj) = p->i32[2]; // replace transparent color
         if (!getSmoothScaledInstance(thisObj, newObj, color))
            throwException(p->currentContext, OutOfMemoryError, null);
         break;
      case ROTATED_SCALED_INSTANCE:
         getRotatedScaledInstance(thisObj, newObj, percScale, angle, color, p->i32[3], p->i32[4]);
         break;
      case TOUCHEDUP_INSTANCE:
         getTouchedUpInstance(thisObj, newObj, p->i32[3], p->i32[4]);
         break;
      case FADED_INSTANCE: // guich@tc110_50
         getFadedInstance(thisObj, newObj, color);
         break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setCurrentFrame_i(NMParams p) // totalcross/ui/image/Image native public void setCurrentFrame(int nr);
{
   Object obj = p->obj[0];
   setCurrentFrame(obj, p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColor_i(NMParams p) // totalcross/ui/image/Image native public void applyColor(int color);
{
   Object thisObj = p->obj[0];
   Pixel color = makePixelRGB(p->i32[0]);
   applyColor(thisObj, color);
}

#ifdef ENABLE_TEST_SUITE
#include "image_Image_test.h"
#endif
