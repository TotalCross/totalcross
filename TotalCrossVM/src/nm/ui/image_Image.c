// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only


#if defined ANDROID || defined darwin || TC_WINDOWING_SDL
#include <tcvm/tcclass.h>
#endif
#include "tcvm.h"
#include "ImagePrimitives_c.h"
#include "io/File.h"
#include "JpegLoader.h"
#include "ui/image/ImageEncodedBag.h"
#if POSIX
   #include <sys/mman.h>
   #include <errno.h>
#endif

#if defined darwin
#include "darwin/image_Image_c.h"
#endif

ImageDecodeStatus pngLoad(Context currentContext, TCObject imageInstance, TCObject inputStreamObj, TCObject bufObj,
      TCZFile tcz, char* first4, const uint8* mapped, int32 mappedLength);

static bool failNextImageAllocationForTest;

int imageDecodeConsumeAllocationFailureForTest(void)
{
   bool fail = failNextImageAllocationForTest;
   failNextImageAllocationForTest = false;
   return fail;
}

static void throwImageDecodeStatus(Context context, ImageDecodeStatus status)
{
   if (status == IMAGE_DECODE_RESOURCE_FAILURE)
   {
      throwExceptionNamed(context, "totalcross.ui.image.TransientImageMaterializationException", null);
   }
   else if (status == IMAGE_DECODE_CORRUPT)
      throwExceptionNamed(context, "totalcross.ui.image.Image$DeterministicImageDecodeException", null);
}

static void captureEncodedBag(Context context, TCObject source, const uint8* bytes, int32 length) {
   ImageEncodedBag* bag = imageEncodedBagCreate(bytes, length);
   ImageEncodedInspection inspection;
   TCObject comment = null;
   if (!bytes || length <= 0) {
      throwException(context, ImageException, "Invalid encoded image buffer");
      return;
   }
   if (!bag) {
      throwException(context, OutOfMemoryError, null);
      return;
   }
   if (!imageEncodedBagInspect(bag, &inspection)) {
      imageEncodedBagRelease(&bag);
      throwException(context, ImageException, "Invalid or unsupported encoded image");
      return;
   }
   if (inspection.comment && inspection.commentLength > 0) {
      comment = createStringObjectFromCharP(context, (CharP)inspection.comment, inspection.commentLength);
      if (!comment) {
         imageEncodedBagRelease(&bag);
         throwException(context, OutOfMemoryError, null);
         return;
      }
      setObjectLock(comment, UNLOCKED);
   }
   if (EncodedImageSource_nativeBag(source)) {
      ImageEncodedBag* previous = (ImageEncodedBag*)EncodedImageSource_nativeBag(source);
      imageEncodedBagRelease(&previous);
   }
   EncodedImageSource_formatCode(source) = (int32)inspection.format;
   EncodedImageSource_length(source) = bag->length;
   EncodedImageSource_intrinsicWidth(source) = inspection.width;
   EncodedImageSource_intrinsicHeight(source) = inspection.height;
   EncodedImageSource_logicalWidth(source) = inspection.logicalWidth;
   EncodedImageSource_logicalHeight(source) = inspection.logicalHeight;
   EncodedImageSource_frameCount(source) = inspection.frameCount;
   EncodedImageSource_nativeBag(source) = (int64)bag;
   EncodedImageSource_bytes(source) = null;
   EncodedImageSource_comment(source) = comment;
}

TC_API void tuiEIS_captureNative_Bi(NMParams p) // totalcross/ui/image/EncodedImageSource private void captureNative(byte[] input, int length);
{
   TCObject input = p->obj[1];
   int32 length = p->i32[0];
   if (!input || length < 0 || length > ARRAYOBJ_LEN(input)) {
      throwException(p->currentContext, ImageException, "Invalid encoded image buffer");
      return;
   }
   captureEncodedBag(p->currentContext, p->obj[0], (uint8*)ARRAYOBJ_START(input), length);
}

TC_API void tuiEIS_captureNativePath_s(NMParams p) // totalcross/ui/image/EncodedImageSource private void captureNativePath(String path);
{
   char path[256];
   TCObject pathObj = p->obj[1];
   TCZFile tcz;
   String2CharPBuf(pathObj, path);
   tcz = tczGetFile(path, false);
   if (!tcz) {
      throwException(p->currentContext, ImageException, "Could not open encoded image");
      return;
   }
   if (tcz->uncompressedSize <= 0) {
      tczClose(tcz);
      throwException(p->currentContext, ImageException, "Could not open encoded image");
      return;
   }
   {
      ImageEncodedBag* bag;
      int32 count;
      bag = imageEncodedBagCreateEmpty(tcz->uncompressedSize);
      if (!bag) {
         tczClose(tcz);
         throwException(p->currentContext, OutOfMemoryError, null);
         return;
      }
      count = tczRead(tcz, bag->bytes, tcz->uncompressedSize);
      if (count != tcz->uncompressedSize) {
         imageEncodedBagRelease(&bag);
         tczClose(tcz);
         throwException(p->currentContext, ImageException, "Could not read encoded image");
         return;
      }
      tczClose(tcz);
      {
         ImageEncodedInspection inspection;
         TCObject comment = null;
         if (!imageEncodedBagInspect(bag, &inspection)) {
            imageEncodedBagRelease(&bag);
            throwException(p->currentContext, ImageException, "Invalid or unsupported encoded image");
            return;
         }
         if (inspection.comment && inspection.commentLength > 0) {
            comment = createStringObjectFromCharP(p->currentContext, (CharP)inspection.comment, inspection.commentLength);
            if (!comment) {
               imageEncodedBagRelease(&bag);
               throwException(p->currentContext, OutOfMemoryError, null);
               return;
            }
            setObjectLock(comment, UNLOCKED);
         }
         if (EncodedImageSource_nativeBag(p->obj[0])) {
            ImageEncodedBag* previous = (ImageEncodedBag*)EncodedImageSource_nativeBag(p->obj[0]);
            imageEncodedBagRelease(&previous);
         }
         EncodedImageSource_formatCode(p->obj[0]) = (int32)inspection.format;
         EncodedImageSource_length(p->obj[0]) = bag->length;
         EncodedImageSource_intrinsicWidth(p->obj[0]) = inspection.width;
         EncodedImageSource_intrinsicHeight(p->obj[0]) = inspection.height;
         EncodedImageSource_logicalWidth(p->obj[0]) = inspection.logicalWidth;
         EncodedImageSource_logicalHeight(p->obj[0]) = inspection.logicalHeight;
         EncodedImageSource_frameCount(p->obj[0]) = inspection.frameCount;
         EncodedImageSource_comment(p->obj[0]) = comment;
      }
      EncodedImageSource_nativeBag(p->obj[0]) = (int64)bag;
      EncodedImageSource_bytes(p->obj[0]) = null;
   }
}

TC_API void tuiEIS_releaseNativeBag(NMParams p) // totalcross/ui/image/EncodedImageSource private void releaseNativeBag();
{
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(p->obj[0]);
   imageEncodedBagRelease(&bag);
   EncodedImageSource_nativeBag(p->obj[0]) = 0;
}

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
      if (magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G') {
         throwImageDecodeStatus(p->currentContext,
            pngLoad(p->currentContext, imageObj, null, null, tcz, magic, null, 0));
      } else
         throwImageDecodeStatus(p->currentContext,
            jpegLoad(p->currentContext, imageObj, null, null, tcz, magic, 0, JPEG_DECODE_FULL, 0, 0));
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
   if ((magic[0] & 0xFF) == 0x89 && magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G') {
      throwImageDecodeStatus(p->currentContext,
         pngLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic, null, 0));
   } else
      throwImageDecodeStatus(p->currentContext,
         jpegLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic, 0, JPEG_DECODE_FULL, 0, 0));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSource_e(NMParams p) // totalcross/ui/image/Image private void decodeEncodedSource(totalcross.ui.image.EncodedImageSource source);
{
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   if (!bag || !bag->bytes || bag->length <= 0)
   {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   ImageDecodeStatus status;
   if (EncodedImageSource_formatCode(sourceObj) == IMAGE_ENCODED_PNG)
      status = pngLoad(p->currentContext, imageObj, null, null, null, null, bag->bytes, bag->length);
   else if (EncodedImageSource_formatCode(sourceObj) == IMAGE_ENCODED_JPEG)
      status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
         JPEG_DECODE_FULL, 0, 0);
   else {
      throwException(p->currentContext, ImageException, "Unsupported deployed encoded image format");
      return;
   }
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSourceTargeted(NMParams p) // totalcross/ui/image/Image private void decodeEncodedSourceTargeted(totalcross.ui.image.EncodedImageSource source, int targetWidth, int targetHeight);
{
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   int32 targetWidth = p->i32[0];
   int32 targetHeight = p->i32[1];
   ImageDecodeStatus status;
   TCClass imageClass = loadClass(p->currentContext, "totalcross.ui.image.Image", false);
   int32* targetedCount = imageClass == null ? null
      : getStaticFieldInt(imageClass, "targetedDecodeInvocationCountForTest");
   int32* targetedWidth = imageClass == null ? null
      : getStaticFieldInt(imageClass, "targetedDecodeWidthForTest");
   int32* targetedHeight = imageClass == null ? null
      : getStaticFieldInt(imageClass, "targetedDecodeHeightForTest");
   int32* infrastructureFailure = imageClass == null ? null
      : getStaticFieldInt(imageClass, "targetedDecodeInfrastructureFailureForTest");
   if (!bag || !bag->bytes || bag->length <= 0) {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   if (EncodedImageSource_formatCode(sourceObj) != IMAGE_ENCODED_JPEG || targetWidth <= 0 || targetHeight <= 0) {
      throwException(p->currentContext, ImageException, "Targeted decode requires a JPEG image and positive dimensions");
      return;
   }
   if (targetedCount != null)
      (*targetedCount)++;
   if (infrastructureFailure != null && *infrastructureFailure) {
      *infrastructureFailure = false;
      throwExceptionNamed(p->currentContext, "totalcross.ui.image.TransientImageMaterializationException", null);
      return;
   }
   status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
      JPEG_DECODE_TARGET_DECODE, targetWidth, targetHeight);
   if (status == IMAGE_DECODE_SUCCESS) {
      if (targetedWidth != null)
         (*targetedWidth) = Image_width(imageObj);
      if (targetedHeight != null)
         (*targetedHeight) = Image_height(imageObj);
   }
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_failNextNativeMaterializati(NMParams p) // totalcross/ui/image/Image native private static void failNextNativeMaterializationForTestNative();
{
   failNextImageAllocationForTest = true;
   UNUSED(p);
}
//////////////////////////////////////////////////////////////////////////
#if TC_RENDERER_SKIA
static bool imageUsesNativeBacking(TCObject imageObj);
static bool applyNativeColorMutation(TCObject imageObj, int32 operation, int32 parameter1,
                                     int32 parameter2);
#endif
TC_API void tuiI_changeColorsNative_ii(NMParams p) // totalcross/ui/image/Image private void changeColorsNative(int from, int to);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(thisObj, SKIA_IMAGE_COLOR_CHANGE_COLORS,
            p->i32[0], p->i32[1])) {
         throwException(p->currentContext, ImageException, "Could not change native image colors");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel from = makePixelARGB(p->i32[0]);
   Pixel to = makePixelARGB(p->i32[1]);
   changeColors(thisObj, from, to);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getPixelRowNative_Bi(NMParams p) // totalcross/ui/image/Image private void getPixelRowNative(byte []fillIn, int y);
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

#if TC_RENDERER_SKIA
static bool imageUsesNativeBacking(TCObject imageObj)
{
   TCObject backing = imageObj ? Image_backing(imageObj) : null;
   return backing != null && strEq(OBJ_CLASS(backing)->name,
      "totalcross.ui.image.NativeImageBacking");
}

static bool applyNativeColorMutation(TCObject imageObj, int32 operation, int32 parameter1,
                                     int32 parameter2)
{
   int32 frameCount;
   int32 visibleWidth;
   if (!imageUsesNativeBacking(imageObj)) {
      return false;
   }
   frameCount = Image_frameCount(imageObj);
   visibleWidth = Image_width(imageObj);
   if (!skia_image_backing_apply_color_mutation(
         NativeImageBacking_nativeHandle(Image_backing(imageObj)), operation, parameter1,
         parameter2, frameCount, visibleWidth, Image_currentFrame(imageObj))) {
      return false;
   }
   Image_changed(imageObj) = true;
   return true;
}
#endif

TC_API void tuiI_getModifiedNative_iiiiiii(NMParams p) // totalcross/ui/image/Image private void getModifiedNative(totalcross.ui.image.Image newImg, int angle, int percScale, int color, int brightness, int contrast, int type);
{
   TCObject thisObj = p->obj[0];
   TCObject newObj = p->obj[1];
   int32 percScale = p->i32[0];
   int32 angle = p->i32[1];
   Pixel color = p->i32[2] == 0 ? (Pixel)0 : makePixelRGB(p->i32[2]);
   FuncType type = (FuncType)p->i32[5];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj) && (type == TOUCHEDUP_INSTANCE
         || type == FADED_INSTANCE || type == ALPHA_INSTANCE)) {
      int32 operation;
      int32 parameter1;
      int32 parameter2;
      int64 handle;
      switch (type) {
         case TOUCHEDUP_INSTANCE:
            operation = SKIA_IMAGE_COLOR_TOUCH_UP_INSTANCE;
            parameter1 = p->i32[3];
            parameter2 = p->i32[4];
            break;
         case FADED_INSTANCE:
            operation = SKIA_IMAGE_COLOR_FADE_INSTANCE;
            parameter1 = p->i32[2];
            parameter2 = 0;
            break;
         default:
            operation = SKIA_IMAGE_COLOR_ALPHA_INSTANCE;
            parameter1 = p->i32[2];
            parameter2 = 0;
            break;
      }
      handle = skia_image_backing_create_color_instance(
         NativeImageBacking_nativeHandle(Image_backing(thisObj)), operation, parameter1, parameter2);
      if (handle == 0) {
         throwException(p->currentContext, OutOfMemoryError, null);
         return;
      }
      if (!imageReplaceNativeBacking(p->currentContext, newObj, handle,
            skia_image_backing_width(handle), skia_image_backing_height(handle))) {
         return;
      }
      return;
   }
#endif
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
TC_API void tuiI_setCurrentFrameNative_i(NMParams p) // totalcross/ui/image/Image private void setCurrentFrameNative(int nr);
{
   TCObject obj = p->obj[0];
   setCurrentFrame(obj, p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColorNative_i(NMParams p) // totalcross/ui/image/Image private void applyColorNative(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(thisObj, SKIA_IMAGE_COLOR_APPLY_COLOR, p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image color");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelRGB(p->i32[0]);
   applyColor(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_nativeEqualsNative_i(NMParams p) // totalcross/ui/image/Image private boolean nativeEqualsNative(totalcross.ui.image.Image other);
{
   TCObject thisObj = p->obj[0];
   TCObject otherObj = p->obj[1];
   p->retI = nativeEquals(thisObj, otherObj);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColor2Native_i(NMParams p) // totalcross/ui/image/Image private void applyColor2Native(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(thisObj, SKIA_IMAGE_COLOR_APPLY_COLOR2, p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image color2");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelARGB(p->i32[0]);
   applyColor2(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setTransparentColorNative_i(NMParams p) // totalcross/ui/image/Image private void setTransparentColorNative(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(thisObj, SKIA_IMAGE_COLOR_SET_TRANSPARENT_COLOR,
            p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not set native image transparent color");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelRGB(p->i32[0]);
   setTransparentColor(thisObj, color);
   p->retO = thisObj;
}

#if TC_RENDERER_SKIA
#include "skia/skia.h"
#endif
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyChangesNative(NMParams p) // totalcross/ui/image/Image private void applyChangesNative();
{
#ifndef SKIA_H
#ifdef __gl2_h_    
   TCObject thisObj = p->obj[0];
   applyChanges(p->currentContext,thisObj);
#endif 
#else
   TCObject img = p->obj[0];
   int32 id = Image_textureId(img);
   if (id >= 0) {
      skia_deleteBitmap(id);
      Image_textureId(img) = -1;
   }
   int32 frameCount = Image_frameCount(img);
   TCObject backing = Image_backing(img);
   TCObject pixelsObj = frameCount == 1 ? Image_pixels(img) : Image_pixelsOfAllFrames(img);

   if (backing != null && strEq(OBJ_CLASS(backing)->name, "totalcross.ui.image.NativeImageBacking")) {
      Image_changed(img) = false;
      return;
   }
   
   if (pixelsObj != NULL) {
      int32 width = (frameCount > 1) ? Image_widthOfAllFrames(img) : Image_width(img);
      int32 height = Image_height(img);
      Pixel *pixels = (Pixel *)ARRAYOBJ_START(pixelsObj);

      id = skia_makeBitmap(SKIA_SCREEN_SURFACE_ID, pixels, width, height);
      if (id >= 0) {
         Image_textureId(img) = id;
      }
   }
   if (Image_textureId(img) >= 0) {
      Image_changed(img) = false;
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_freeTextureNative(NMParams p) // totalcross/ui/image/Image private void freeTextureNative();
{
#ifndef SKIA_H
#ifdef __gl2_h_                         
   freeTexture(p->obj[0]);
#endif
#else
      TCObject img = p->obj[0];
      int32 id = Image_textureId(img);
      if (id >= 0) {
         skia_deleteBitmap(id);
         Image_textureId(img) = -1;
      }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_createJpgNative_si(NMParams p) // totalcross/ui/image/Image private void createJpgNative(totalcross.io.Stream s, int quality);
{
   TCObject stream = p->obj[1];
   int32 quality = p->i32[0];
   /* The Java wrapper has already completed canonical materialization. */
   image2jpeg(p->currentContext, p->obj[0], stream, quality);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyFadeNative_i(NMParams p) // totalcross/ui/image/Image private void applyFadeNative(int fadeValue);
{
   TCObject thisObj = p->obj[0];
   int32 fadeValue = p->i32[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(thisObj, SKIA_IMAGE_COLOR_APPLY_FADE, fadeValue, 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image fade");
      }
      return;
   }
#endif
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
static bool validateJpegScaleArguments(Context currentContext, int32 numerator, int32 denominator)
{
   if (numerator <= 0 || denominator <= 0) {
      throwException(currentContext, ImageException, null);
      return false;
   }
   return true;
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

   p->retO = null;
   if (!validateJpegScaleArguments(p->currentContext, targetWidth, targetHeight)) {
      return;
   }
   String2CharPBuf(pathObj, szPath);
   tcz = tczGetFile(szPath, false);

   if ((imageObj = createObject(p->currentContext, "totalcross.ui.image.Image")) != NULL
         && (initMethod = getMethod(OBJ_CLASS(imageObj), false, "init", 0)) != NULL ) {

      // Found in tcz?
      if (tcz != null) {
         throwImageDecodeStatus(p->currentContext,
            jpegLoad(p->currentContext, imageObj, null, null, tcz, null, 0, JPEG_DECODE_BEST_FIT,
               targetWidth, targetHeight));
         goto finish;
      }

      // Try with mmap
#if POSIX
      NATIVE_FILE fd;
      TCHARP sztPath;
      Err err;
   #if TCHAR == char
      sztPath = szPath;
   #else
      TCHAR szPathAux[MAX_PATHNAME];
      String2TCHARPBuf(pathObj, szPathAux);
      sztPath = szPathAaux;
   #endif
      int32 size = 0;
      const char * mapped;
      if ((err = fileCreate(&fd, sztPath, READ_ONLY, NULL)) == NO_ERROR) {
         if ((err = fileGetSize(fd, NULL, &size)) == NO_ERROR) {
            mapped = mmap(NULL, size, PROT_READ, MAP_PRIVATE, fileno(fd.handle), 0);
            if (mapped == MAP_FAILED) {
               err = errno;
            } else {
               throwImageDecodeStatus(p->currentContext,
                  jpegLoad(p->currentContext, imageObj, fileObj, null, null, mapped, size, JPEG_DECODE_BEST_FIT,
                     targetWidth, targetHeight));
               munmap((void*) mapped, size);
            }
         }
         fileClose(&fd);
      }
      if (err == NO_ERROR) {
         goto finish;
      }
#endif

      // Try opening using a File object
      if ((fileObj = createObject(p->currentContext, "totalcross.io.File")) != NULL) {
         fileConstructor = getMethod(OBJ_CLASS(fileObj), false, CONSTRUCTOR_NAME, 2, "java.lang.String", J_INT);
         if (fileConstructor != null) {
            executeMethod(p->currentContext, fileConstructor, fileObj, pathObj, READ_ONLY);
            if (p->currentContext->thrownException == null) {
               if ((bufferObj = createByteArray(p->currentContext, 512)) != NULL) {
                  throwImageDecodeStatus(p->currentContext,
                     jpegLoad(p->currentContext, imageObj, fileObj, bufferObj, null, null, 0, JPEG_DECODE_BEST_FIT,
                        targetWidth, targetHeight));
               }
            }
         }
      }
   }

finish:
   p->retO = null;
   if (imageObj != null && initMethod != null
         && p->currentContext->thrownException == null
         && Image_width(imageObj) > 0 && Image_height(imageObj) > 0) {
      executeMethod(p->currentContext, initMethod, imageObj);
      if (p->currentContext->thrownException == null) {
         p->retO = imageObj;
      }
   }
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

   p->retO = null;
   if (!validateJpegScaleArguments(p->currentContext, scaleNumerator, scaleDenominator)) {
      return;
   }
   String2CharPBuf(pathObj, szPath);
   tcz = tczGetFile(szPath, false);

   if ((imageObj = createObject(p->currentContext, "totalcross.ui.image.Image")) != NULL
         && (initMethod = getMethod(OBJ_CLASS(imageObj), false, "init", 0)) != NULL ) {
      if (tcz != null) {
         throwImageDecodeStatus(p->currentContext,
            jpegLoad(p->currentContext, imageObj, null, null, tcz, null, 0, JPEG_DECODE_EXPLICIT_RATIO,
               scaleNumerator, scaleDenominator));
      } else if ((fileObj = createObject(p->currentContext, "totalcross.io.File")) != NULL) {
         fileConstructor = getMethod(OBJ_CLASS(fileObj), false, CONSTRUCTOR_NAME, 2, "java.lang.String", J_INT);
         if (fileConstructor != null) {
            executeMethod(p->currentContext, fileConstructor, fileObj, pathObj, READ_ONLY);
            if (p->currentContext->thrownException == null) {
               if ((bufferObj = createByteArray(p->currentContext, 512)) != NULL) {
                  throwImageDecodeStatus(p->currentContext,
                     jpegLoad(p->currentContext, imageObj, fileObj, bufferObj, null, null, 0,
                        JPEG_DECODE_EXPLICIT_RATIO, scaleNumerator, scaleDenominator));
               }
            }
         }
      }
   }

   p->retO = null;
   if (imageObj != null && initMethod != null
         && p->currentContext->thrownException == null
         && Image_width(imageObj) > 0 && Image_height(imageObj) > 0) {
      executeMethod(p->currentContext, initMethod, imageObj);
      if (p->currentContext->thrownException == null) {
         p->retO = imageObj;
      }
   }
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
