// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only


#if defined ANDROID || defined darwin || defined HEADLESS
#include <tcvm/tcclass.h>
#endif
#include "tcvm.h"
#include "ImagePrimitives_c.h"
#include "io/File.h"

#if defined darwin
#include "darwin/image_Image_c.h"
#endif

void jpegLoad(Context currentContext, TCObject imageInstance, TCObject inputStreamObj, TCObject bufObj, TCZFile tcz, char* first4, int32 scale_num, int32 scale_denom);
void pngLoad(Context currentContext, TCObject imageInstance, TCObject inputStreamObj, TCObject bufObj, TCZFile tcz, char* first4);

//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageLoad_s(NMParams p) // totalcross/ui/image/Image native private void imageLoad(String path);
{
   char path[256];
   TCObject imageObj = p->obj[0];
   TCObject pathObj = p->obj[1];
   TCZFile tcz;

   String2CharPBuf(pathObj, path);
   tcz = tczGetFile(path, false);
   if (tcz != null)
   {
      char magic[4]; // read the magic to find if its a png or a jpeg (note that jpeg has no magic)
      tczRead(tcz, magic, 4);
      if (magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G')
         pngLoad(p->currentContext, imageObj, null, null, tcz, magic);
      else
         jpegLoad(p->currentContext, imageObj, null, null, tcz, magic, 0, 0);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageParse_sB(NMParams p) // totalcross/ui/image/Image native private void imageParse(totalcross.io.Stream in, byte []buf);
{
   TCObject imageObj = p->obj[0];
   TCObject streamObj = p->obj[1];
   TCObject bufObj = p->obj[2];
   uint8* buf = ARRAYOBJ_START(bufObj);
   char magic[4];
   xmove4(magic, buf); // buf already comes filled from Java with the first 4 bytes
   if ((magic[0] & 0xFF) == 0x89 && magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G')
      pngLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic);
   else
      jpegLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic, 0, 0);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_changeColors_ii(NMParams p) // totalcross/ui/image/Image native public void changeColors(int from, int to);
{
   TCObject thisObj = p->obj[0];
   Pixel from = makePixelARGB(p->i32[0]);
   Pixel to = makePixelARGB(p->i32[1]);
   changeColors(thisObj, from, to);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getPixelRow_Bi(NMParams p) // totalcross/ui/image/Image native protected void getPixelRow(byte []fillIn, int y);
{
   TCObject thisObj = p->obj[0];
   TCObject fillIn = p->obj[1];
   int32 y = p->i32[0];
   getPixelRow(p->currentContext,thisObj, fillIn, y);
}
//////////////////////////////////////////////////////////////////////////
typedef enum
{
   SCALED_INSTANCE,
   SMOOTH_SCALED_INSTANCE,
   ROTATED_SCALED_INSTANCE,
   TOUCHEDUP_INSTANCE,
   FADED_INSTANCE,
   ALPHA_INSTANCE
} FuncType;
TC_API void tuiI_getModifiedInstance_iiiiiii(NMParams p) // totalcross/ui/image/Image native private void getModifiedInstance(totalcross.ui.image.Image newImg, int angle, int percScale, int color, int brightness, int contrast, int type);
{
   TCObject thisObj = p->obj[0];
   TCObject newObj = p->obj[1];
   int32 percScale = p->i32[0];
   int32 angle = p->i32[1];
   Pixel color = p->i32[2] == 0 ? (Pixel)0 : makePixelRGB(p->i32[2]);
   FuncType type = (FuncType)p->i32[5];
   switch (type)
   {
      case SCALED_INSTANCE:
         getScaledInstance(thisObj, newObj);
         break;
      case SMOOTH_SCALED_INSTANCE:
         if (!getSmoothScaledInstance(thisObj, newObj))
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
      case ALPHA_INSTANCE: // guich@tc200
         getAlphaInstance(thisObj, newObj, p->i32[2]);
         break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setCurrentFrame_i(NMParams p) // totalcross/ui/image/Image native public void setCurrentFrame(int nr);
{
   TCObject obj = p->obj[0];
   setCurrentFrame(obj, p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColor_i(NMParams p) // totalcross/ui/image/Image native public void applyColor(int color);
{
   TCObject thisObj = p->obj[0];
   Pixel color = makePixelRGB(p->i32[0]);
   applyColor(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_nativeEquals_i(NMParams p) // totalcross/ui/image/Image native private boolean nativeEquals(totalcross.ui.image.Image other);
{
   TCObject thisObj = p->obj[0];
   TCObject otherObj = p->obj[1];
   p->retI = nativeEquals(thisObj, otherObj);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColor2_i(NMParams p) // totalcross/ui/image/Image native public void applyColor2(int color);
{
   TCObject thisObj = p->obj[0];
   Pixel color = makePixelARGB(p->i32[0]);
   applyColor2(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setTransparentColor_i(NMParams p) // totalcross/ui/image/Image native public totalcross.ui.image.Image setTransparentColor(int color);
{
   TCObject thisObj = p->obj[0];
   Pixel color = makePixelRGB(p->i32[0]);
   setTransparentColor(thisObj, color);
   p->retO = thisObj;
}

#if defined ANDROID || defined darwin || defined HEADLESS
#include "android/skia.h"
#endif
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyChanges(NMParams p) // totalcross/ui/image/Image native public void applyChanges();
{
#ifndef SKIA_H
#ifdef __gl2_h_    
   TCObject thisObj = p->obj[0];
   applyChanges(p->currentContext,thisObj);
#endif 
#else
      TCObject img = p->obj[0];

      int32 frameCount = Image_frameCount(img);
      TCObject pixelsObj = frameCount == 1 ? Image_pixels(img) : Image_pixelsOfAllFrames(img);
      Pixel *pixels = (Pixel *)ARRAYOBJ_START(pixelsObj);
      int32 width = (Image_frameCount(img) > 1) ? Image_widthOfAllFrames(img) : Image_width(img);
      int32 height = Image_height(img);
      int32 id = Image_textureId(img);

      Image_textureId(img) = skia_makeBitmap(id, pixels, width, height);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_freeTexture(NMParams p) // totalcross/ui/image/Image native private void freeTexture();
{
#ifndef SKIA_H
#ifdef __gl2_h_                         
   freeTexture(p->obj[0]);
#endif
#else
      TCObject img = p->obj[0];
      int32 id = Image_textureId(img);

      skia_deleteBitmap(id);
      Image_textureId(img) = -1;
#endif
}
//////////////////////////////////////////////////////////////////////////
bool image2jpeg(Context currentContext, TCObject srcImageObj, TCObject dstStreamObj, int32 quality); // JpegLoader.c
TC_API void tuiI_createJpg_si(NMParams p) // totalcross/ui/image/Image native public void createJpg(totalcross.io.Stream s, int quality);
{
   TCObject thisObj = p->obj[0];
   TCObject stream = p->obj[1];
   int32 quality = p->i32[0];
   /*bool ret = */image2jpeg(p->currentContext, thisObj, stream, quality);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyFade_i(NMParams p) // totalcross/ui/image/Image native public void applyFade(int fadeValue);
{
   TCObject thisObj = p->obj[0];
   int32 fadeValue = p->i32[0];
   applyFade(thisObj, fadeValue);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_nativeResizeJpeg_ssi(NMParams p) // totalcross/ui/image/Image native public static void nativeResizeJpeg(String inputPath, String outputPath, int maxPixelSize);
{
   TCObject inputPathObj = p->obj[0];
   TCObject outputPathObj = p->obj[1];
   int32 maxPixelSize = p->i32[0];
   
#if defined (darwin)
   char input_path[512];
   char output_path[512];

   String2CharPBuf(inputPathObj, input_path);
   String2CharPBuf(outputPathObj, output_path);
   
   resizeImageAtPath(input_path, output_path, maxPixelSize);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getJpegBestFit_sii(NMParams p) // totalcross/ui/image/Image native public static totalcross.ui.image.Image getJpegBestFit(String path, int targetWidth, int targetHeight) throws java.io.IOException, totalcross.ui.image.ImageException;
{
   TCObject pathObj = p->obj[0];
   int32 targetWidth = p->i32[0];
   int32 targetHeight = p->i32[1];
   TCObject bufferObj = null; 
   TCObject imageObj = null;
   TCObject fileObj = null;
   Method initMethod;
   Method fileConstructor;
   char szPath[MAX_PATHNAME];
   TCZFile tcz;

   String2CharPBuf(pathObj, szPath);
   tcz = tczGetFile(szPath, false);

   if ((imageObj = createObject(p->currentContext, "totalcross.ui.image.Image")) != NULL
         && (initMethod = getMethod(OBJ_CLASS(imageObj), false, "init", 0)) != NULL ) {
      if (tcz != null) {
         jpegLoad(p->currentContext, imageObj, null, null, tcz, null, targetWidth, targetHeight);
      } else if ((fileObj = createObject(p->currentContext, "totalcross.io.File")) != NULL) {
         fileConstructor = getMethod(OBJ_CLASS(fileObj), false, CONSTRUCTOR_NAME, 2, "java.lang.String", J_INT);
         if (fileConstructor != null) {
            executeMethod(p->currentContext, fileConstructor, fileObj, pathObj, READ_ONLY);
            if (p->currentContext->thrownException == null) {
               if ((bufferObj = createByteArray(p->currentContext, 512)) != NULL) {
                  jpegLoad(p->currentContext, imageObj, fileObj, bufferObj, null, null, targetWidth, targetHeight);
               }
            }
         }
      }
   }

   p->retO = imageObj;
   if (imageObj != null) {
      setObjectLock(imageObj, UNLOCKED);
   }
   if (bufferObj != null) {
      setObjectLock(bufferObj, UNLOCKED);
   }
   if (fileObj != null) {
      setObjectLock(fileObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getJpegScaled_sii(NMParams p) // totalcross/ui/image/Image native public static totalcross.ui.image.Image getJpegScaled(String path, int scaleNumerator, int scaleDenominator) throws java.io.IOException, totalcross.ui.image.ImageException;
{
   TCObject pathObj = p->obj[0];
   int32 scaleNumerator = p->i32[0];
   int32 scaleDenominator = p->i32[1];
   TCObject bufferObj = null; 
   TCObject imageObj = null;
   TCObject fileObj = null;
   Method initMethod;
   Method fileConstructor;
   char szPath[MAX_PATHNAME];
   TCZFile tcz;

   String2CharPBuf(pathObj, szPath);
   tcz = tczGetFile(szPath, false);

   if ((imageObj = createObject(p->currentContext, "totalcross.ui.image.Image")) != NULL
         && (initMethod = getMethod(OBJ_CLASS(imageObj), false, "init", 0)) != NULL ) {
      if (tcz != null) {
         jpegLoad(p->currentContext, imageObj, null, null, tcz, null, -scaleNumerator, -scaleDenominator);
      } else if ((fileObj = createObject(p->currentContext, "totalcross.io.File")) != NULL) {
         fileConstructor = getMethod(OBJ_CLASS(fileObj), false, CONSTRUCTOR_NAME, 2, "java.lang.String", J_INT);
         if (fileConstructor != null) {
            executeMethod(p->currentContext, fileConstructor, fileObj, pathObj, READ_ONLY);
            if (p->currentContext->thrownException == null) {
               if ((bufferObj = createByteArray(p->currentContext, 512)) != NULL) {
                  jpegLoad(p->currentContext, imageObj, fileObj, bufferObj, null, null, -scaleNumerator, -scaleDenominator);
               }
            }
         }
      }
   }

   p->retO = imageObj;
   if (imageObj != null) {
      setObjectLock(imageObj, UNLOCKED);
   }
   if (bufferObj != null) {
      setObjectLock(bufferObj, UNLOCKED);
   }
   if (fileObj != null) {
      setObjectLock(fileObj, UNLOCKED);
   }
}

#ifdef ENABLE_TEST_SUITE
#include "image_Image_test.h"
#endif
