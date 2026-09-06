// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "png.h"
#include "tcvm.h"
#include "ui/image/ImageDecodeStatus.h"
#include "ui/ImageTestAccounting_c.h"
#include "ui/image/ImageDecodeFormat.h"
#include <stdlib.h>
#if TC_RENDERER_SKIA
#include "ui/NativeImageBacking.h"
#include "ui/skia/skia.h"
#endif

static void row_callback(png_structp, png_bytep, png_uint_32, int);
static void info_callback(png_structp png_ptr, png_infop info);
static void error_callback(png_structp, png_const_charp);

typedef struct
{
   Heap heap;
   TCObject imageObj;
   Pixel* pixels;
   Pixel* pixelStorage;
   uint8* rgbaStorage;
   uint8* compactStorage;
   TCZFile tcz; // if filled, we're reading from a tcz file, otherwise, from a totalcross.io.Stream
   // for fetching data
   TCObject inputStreamObj, bufObj, pixelsObj;
   const uint8* mapped;
   int32 mappedLength;
   int32 mappedCursor;
   Method readBytesMethod;
   TValue params[4];
   // the first 4 bytes
   char *first4;
   int32 lastPass;
   int32 width,height;
   int32 bytesPerRow;
   png_bytep upixels;
   bool quit;
   bool zeroCopy;
   bool opacityMetadata;
   bool sourceHasAlpha;
   ImageBackingFormat storageFormat;
   bool opacityAlphaOutput;
   bool pixelsOpaque;
   int32 rowsDecoded;
   volatile ImageDecodeStatus *decodeStatus;
   Context currentContext;

   png_infop info_ptr;
} UserData;

// Read the JPEG Input file.
int pngRead(void *buff, int count, UserData *in)
{
   int32 extra = 0,n;
   uint8* cur = (uint8*)buff;
   if (in->first4) // place the first 4 bytes in the output buffer
   {
      xmove4(cur, in->first4);
      in->first4 = null;
      cur += 4;
      count -= 4;
      extra = 4;
   }
   if (in->mapped != null)
   {
      int32 available = in->mappedLength - in->mappedCursor;
      if (available > count) available = count;
      if (available > 0)
      {
         xmemmove(cur, in->mapped + in->mappedCursor, available);
         in->mappedCursor += available;
         return available + extra;
      }
      return extra;
   }
   if (in->tcz != null)
      return tczRead(in->tcz, cur, count) + extra;
   else
   {
      uint8* start = (uint8*)buff;
      TCObject bufObj = in->params[1].asObj;
      int tempBufSize = ARRAYOBJ_LEN(bufObj);
      uint8 *tempBufStart = (uint8*)ARRAYOBJ_START(bufObj);

      while (count > 0)
      {
         n=0;
         in->params[3].asInt32 = (count < tempBufSize)? count : tempBufSize;
         n = executeMethod(in->currentContext, in->readBytesMethod, in->params[0].asObj, in->params[1].asObj, in->params[2].asInt32, in->params[3].asInt32).asInt32;
         if (n <= 0)
            break;
         xmemmove(cur, tempBufStart, n);
         cur += n;
         count -= n;
      }
      return (int32)(cur - start);
   }
}

static png_voidp usermalloc(png_structp png_ptr, png_size_t size)
{
   Heap h = (Heap) png_get_mem_ptr(png_ptr);
   return heapAlloc(h, (int)size);
}
void userfree(png_structp png_ptr, png_voidp ptr)
{
   // there's no need to call free because the Heap will destroy everthing for us.
   UNUSED(png_ptr)
   UNUSED(ptr)
   //UserData * userData = (UserData *)png_get_progressive_ptr(png_ptr);
   //heapFree(userData->heap, ptr);
}

void setTransparentColor(TCObject obj, Pixel color);
// imageObj+tcz+first4, imageObj+inputStream+bufObj+bufCount, or imageObj+mapped bytes
ImageDecodeStatus pngLoad(Context currentContext, TCObject imageObj, TCObject inputStreamObj, TCObject bufObj,
      TCZFile tcz, char* first4, const uint8* mapped, int32 mappedLength, bool zeroCopy,
      bool opacityMetadata)
{
   Heap heap;
   int32 count;
   uint8 buffer[512];
   int32 transp = -1;
   bool isAlpha = false;

   UserData *userData;
   png_structp png_ptr;
   volatile ImageDecodeStatus decodeStatus = IMAGE_DECODE_SUCCESS;

   png_textp text = null;
   png_byte color_type;

   xmemzero(&png_ptr, sizeof(png_ptr));
   userData = (UserData*)calloc(1, sizeof(*userData));
   if (!userData)
      return IMAGE_DECODE_RESOURCE_FAILURE;

   heap = heapCreate();
   if (!heap)
   {
      free(userData);
      return IMAGE_DECODE_RESOURCE_FAILURE;
   }

   userData->currentContext = currentContext;
   userData->heap = heap;
   userData->decodeStatus = &decodeStatus;
   if (tcz != null)
   {
      userData->tcz = tcz;
      tcz->tempHeap = heap;
   }
   else if (mapped != null)
   {
      userData->mapped = mapped;
      userData->mappedLength = mappedLength;
      userData->mappedCursor = 0;
   }
   else
   {
      userData->inputStreamObj = inputStreamObj;  // JPEG stream
      userData->bufObj = bufObj;    // a byte array for readBytes()
   }
   userData->first4 = first4;
   userData->imageObj = imageObj;
   userData->zeroCopy = zeroCopy;
   userData->opacityMetadata = opacityMetadata;

   IF_HEAP_ERROR(heap)
   {
      if (decodeStatus == IMAGE_DECODE_SUCCESS)
         decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
      if (userData->rgbaStorage) free(userData->rgbaStorage);
#if TC_RENDERER_SKIA
      if (userData->pixelStorage) xfree(userData->pixelStorage);
      if (userData->compactStorage) free(userData->compactStorage);
#endif
      free(userData);
      heapDestroy(heap);
      if (tcz != null)
         tczClose(tcz);
      return decodeStatus;
   }
   /* Start decompressor */
   /* Create and initialize the png_struct. */
   png_ptr = png_create_read_struct_2(PNG_LIBPNG_VER_STRING, heap, error_callback, null, heap, usermalloc, userfree);
   if (png_ptr == NULL)
      HEAP_ERROR(heap, 999);
   userData->info_ptr = png_create_info_struct(png_ptr);

   if (tcz == null && mapped == null)
   {
      Method readBytesMethod = getMethod(OBJ_CLASS(userData->inputStreamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
      if (readBytesMethod == null)
         HEAP_ERROR(heap, 999);
      userData->readBytesMethod = readBytesMethod;
      userData->params[0].asObj = userData->inputStreamObj;
      userData->params[1].asObj = userData->bufObj;
   }

   png_set_progressive_read_fn(png_ptr,userData,info_callback,row_callback,null);

   /* Create decompressor output buffer. */
   while (!userData->quit && (count = pngRead(buffer, sizeof(buffer), userData)) > 0)
      png_process_data(png_ptr, userData->info_ptr, buffer, count);

   if ((userData->pixelsObj == null && userData->pixels == null && userData->rgbaStorage == null
         && userData->compactStorage == null)
         || userData->rowsDecoded < userData->height)
   {
      if (userData->upixels) png_free(png_ptr, userData->upixels);
#if TC_RENDERER_SKIA
      if (userData->pixelStorage) xfree(userData->pixelStorage);
      if (userData->rgbaStorage) free(userData->rgbaStorage);
      if (userData->compactStorage) free(userData->compactStorage);
#endif
      png_destroy_read_struct(&png_ptr, &userData->info_ptr, NULL);
      Image_backing(imageObj) = null;
      Image_width(imageObj) = 0;
      Image_height(imageObj) = 0;
      if (tcz != null)
         tczClose(tcz);
      heapDestroy(heap);
      free(userData);
      return decodeStatus == IMAGE_DECODE_RESOURCE_FAILURE ? decodeStatus : IMAGE_DECODE_CORRUPT;
   }

   // guich@tc100: check if a comment came with the png
   if (png_get_text(png_ptr, userData->info_ptr, &text, null) != 0 && text && strEq("Comment", text->key)) {
      Image_comment(imageObj) = createStringObjectFromCharP(currentContext, text->text, (int)text->text_length);
      setObjectLock(Image_comment(imageObj), UNLOCKED);
   }

   // guich@tc100: set the transparent color
   color_type = png_get_color_type(png_ptr, userData->info_ptr);
   if (color_type == PNG_COLOR_TYPE_RGB_ALPHA) {
      isAlpha = true;
   } else {
      png_bytep trans_alpha = null;
      int32 num_trans = 0;
      png_color_16p trans_color = null;

      if (png_get_tRNS(png_ptr, userData->info_ptr, &trans_alpha, &num_trans, &trans_color) != 0 && num_trans > 0) {
         if (color_type == PNG_COLOR_TYPE_PALETTE) { // palettized?
            int32 i;
            if (num_trans == 256 && color_type == PNG_COLOR_TYPE_PALETTE) {
               isAlpha = true;
            }
            for (i = num_trans; --i >= 0;) { // guich@tc120_60: must find the entry that has 0 in trans array
               if (trans_alpha[i] == 0) {
                  png_colorp palette = null;
                  int32 num_palette = 0;
                  if(png_get_PLTE(png_ptr, userData->info_ptr, &palette, &num_palette) != 0) {
                     png_color c = palette[i];
                     transp = (c.red << 16) | (c.green << 8) | c.blue;
                     isAlpha = false;
                     break;
                  }
               }
            }
         }
         else {
            transp = (trans_color->red << 16) | (trans_color->green << 8) | trans_color->blue;
         }
      }
   }

   // Finish decompression and release memory. Do it in this order because output module
   // has allocated memory of lifespan JPOOL_IMAGE; it needs to finish before releasing memory.
   if (userData->upixels) png_free(png_ptr, userData->upixels);
   png_destroy_read_struct(&png_ptr, &userData->info_ptr, NULL);

   Image_width(imageObj) = userData->width;
   Image_height(imageObj) = userData->height;
#if TC_RENDERER_SKIA
   {
      int64 handle;
      const int32 pixelBytes = (int32)((uint64)userData->width * userData->height * 4);
      if (userData->compactStorage) {
         handle = skia_image_backing_create_from_owned_pixels(userData->compactStorage,
            userData->width, userData->height, userData->storageFormat);
         userData->compactStorage = null;
      } else if (userData->zeroCopy) {
         handle = skia_image_backing_create_from_owned_rgba_pixels(userData->rgbaStorage,
            userData->width, userData->height);
         userData->rgbaStorage = null;
      } else {
         handle = skia_image_backing_create_from_argb_pixels(userData->pixelStorage,
            userData->width, userData->height);
         xfree(userData->pixelStorage);
         userData->pixelStorage = null;
      }
      userData->pixels = null;
      const int32 compactBytes = (int32)((uint64)userData->width * userData->height
         * (userData->storageFormat == IMAGE_BACKING_FORMAT_GRAY8 ? 1 : 2));
      if (handle && userData->storageFormat != IMAGE_BACKING_FORMAT_RGBA8888) {
         skia_image_backing_set_opacity(handle,
            userData->storageFormat == IMAGE_BACKING_FORMAT_ARGB4444
               ? (userData->pixelsOpaque ? SKIA_IMAGE_OPACITY_OPAQUE
                                          : SKIA_IMAGE_OPACITY_TRANSLUCENT)
               : SKIA_IMAGE_OPACITY_OPAQUE);
      } else if (handle && userData->opacityMetadata) {
         const int32 opacity = !userData->sourceHasAlpha
            ? SKIA_IMAGE_OPACITY_OPAQUE
            : userData->opacityAlphaOutput
               ? (userData->pixelsOpaque ? SKIA_IMAGE_OPACITY_OPAQUE
                                        : SKIA_IMAGE_OPACITY_TRANSLUCENT)
               : SKIA_IMAGE_OPACITY_UNKNOWN;
         skia_image_backing_set_opacity(handle, opacity);
         if (!userData->sourceHasAlpha) {
            imageRecordTestCounter("opacityKnownFromSourceForTest");
         } else if (opacity != SKIA_IMAGE_OPACITY_UNKNOWN) {
            imageRecordTestCounter("opacityDeterminedDuringDecodeForTest");
         }
      }
      if (!handle || !imageInstallNativeBacking(currentContext, imageObj, handle,
            userData->width, userData->height)) {
         Image_width(imageObj) = 0;
         Image_height(imageObj) = 0;
         if (tcz != null)
            tczClose(tcz);
         heapDestroy(heap);
         free(userData);
         return IMAGE_DECODE_RESOURCE_FAILURE;
      }
      if (userData->storageFormat != IMAGE_BACKING_FORMAT_RGBA8888) {
         skia_image_backing_record_compact_decode_for_test(userData->storageFormat,
            (uint64_t)compactBytes);
         imageAddTestCounter("decodeFinalBufferBytesForTest", compactBytes);
      } else if (userData->zeroCopy) {
         imageRecordTestCounter("zeroCopyDecodeCountForTest");
      } else {
         imageRecordTestCounter("copiedDecodeCountForTest");
         imageAddTestCounter("decodeCopiedBytesForTest", pixelBytes);
      }
      imageAddTestCounter("decodeFinalBufferBytesForTest", pixelBytes);
   }
#endif
   if (tcz != null)
      tczClose(tcz);
   heapDestroy(heap);
   free(userData);

   return IMAGE_DECODE_SUCCESS;

//   if (!isAlpha && transp != -1) // guich@tc200rc1: added a test for -1, otherwise a png with rgb will apply a pink mask to the image
//      setTransparentColor(imageObj, (Pixel)transp);
}

/**   do any setup here, including setting any of the transformations
   mentioned in the Reading PNG files section.  For now, you _must_
   call either png_start_read_image() or png_read_update_info()
   after all the transformations are set (even if you don't set
   any).  You may start getting rows before png_process_data()
   returns, so this is your last chance to prepare for that.
*/
static void info_callback(png_structp png_ptr, png_infop info_ptr)
{
   png_uint_32 width = 0;
   png_uint_32 height = 0;
   int bit_depth = 0;
   int color_type = 0;
   int interlace_type = 0;
   int compression_type = 0;
   int filter_method = 0;
   UserData * userData = (UserData *)png_get_progressive_ptr(png_ptr);
   int32 num_trans = 0;

   png_get_IHDR(png_ptr,info_ptr,&width,&height,&bit_depth,&color_type,&interlace_type,&compression_type,&filter_method);
   userData->sourceHasAlpha = color_type == PNG_COLOR_TYPE_RGB_ALPHA
      || color_type == PNG_COLOR_TYPE_GRAY_ALPHA
      || png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS) != 0;
   userData->storageFormat = imageSelectDecodeStorageFormat(userData->imageObj,
      color_type == PNG_COLOR_TYPE_GRAY && !userData->sourceHasAlpha,
      userData->sourceHasAlpha);
   
   /*
   | set up transformation params:
   | expand images of all color-type and bit-depth to 3x8 bit RGB images
   | let the library process things like alpha, transparency, background
   */
   if (bit_depth == 16)
      png_set_strip_16(png_ptr);

   if (color_type == PNG_COLOR_TYPE_RGB_ALPHA || color_type == PNG_COLOR_TYPE_PALETTE || bit_depth < 8 || png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS))
      png_set_expand(png_ptr);
   if (userData->storageFormat != IMAGE_BACKING_FORMAT_GRAY8
         && ((color_type == PNG_COLOR_TYPE_GRAY) || (color_type == PNG_COLOR_TYPE_GRAY_ALPHA)))
      png_set_gray_to_rgb(png_ptr);

   userData->lastPass = png_set_interlace_handling(png_ptr) - 1;

   // get updated info, and start the image
   png_read_update_info(png_ptr, userData->info_ptr);
   info_ptr = userData->info_ptr;
   color_type = png_get_color_type(png_ptr, info_ptr);
   num_trans = 0; // MUST BE INITIALIZED BEFORE png_get_tRNS
   userData->opacityAlphaOutput = userData->sourceHasAlpha && png_get_channels(png_ptr, info_ptr) == 4;
   userData->pixelsOpaque = true;
   userData->width = (int32)width;
   userData->height = (int32)height;
   userData->bytesPerRow = (int32)png_get_rowbytes(png_ptr, info_ptr);
   userData->upixels = png_malloc(png_ptr, userData->bytesPerRow);
   if (width > 65535 || height > 65535)  // bad width/height?
   {
      *userData->decodeStatus = IMAGE_DECODE_CORRUPT;
      HEAP_ERROR(userData->heap, 998);
   }

   if (imageDecodeConsumeAllocationFailureForTest())
   {
      *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
      userData->quit = true;
      return;
   }
#if TC_RENDERER_SKIA
   if ((uint64)width * height > (uint64)0x7FFFFFFF / sizeof(Pixel))
   {
      *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
      userData->quit = true;
      return;
   }
   if (userData->storageFormat != IMAGE_BACKING_FORMAT_RGBA8888) {
      const size_t bytesPerPixel = userData->storageFormat == IMAGE_BACKING_FORMAT_GRAY8 ? 1 : 2;
      userData->compactStorage = (uint8*)malloc((size_t)width * height * bytesPerPixel);
      if (!userData->compactStorage) {
         *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
         userData->quit = true;
         return;
      }
      if (imageDecodeConsumeFinalBufferFailureForTest()) {
         *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
         HEAP_ERROR(userData->heap, 997);
      }
   } else if (userData->zeroCopy) {
      userData->rgbaStorage = (uint8*)malloc((size_t)width * height * 4);
      if (!userData->rgbaStorage) {
         *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
         userData->quit = true;
         return;
      }
      if (imageDecodeConsumeFinalBufferFailureForTest()) {
         *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
         HEAP_ERROR(userData->heap, 997);
      }
   } else {
      userData->pixelStorage = userData->pixels =
         (Pixel*)xmalloc((int32)((uint64)width * height * sizeof(Pixel)));
      if (!userData->pixelStorage)
      {
         *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
         userData->quit = true;
         return;
      }
   }
#else
   TCObject backing;
   userData->pixelsObj = createIntArray(userData->currentContext, (int32)(width*height));
   if (!userData->pixelsObj)
   {
      *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
      userData->quit = true;
      return;
   }
   backing = createObject(userData->currentContext, "totalcross.ui.image.RasterImageBacking");
   if (!backing)
   {
      *userData->decodeStatus = IMAGE_DECODE_RESOURCE_FAILURE;
      userData->quit = true;
      return;
   }
   RasterImageBacking_pixels(backing) = userData->pixelsObj;
   RasterImageBacking_pixelsOfAllFrames(backing) = null;
   RasterImageBacking_width(backing) = (int32)width;
   RasterImageBacking_height(backing) = (int32)height;
   RasterImageBacking_frameCount(backing) = 1;
   RasterImageBacking_widthOfAllFrames(backing) = (int32)width;
   setObjectLock(backing, UNLOCKED);
   Image_backing(userData->imageObj) = backing;
   userData->pixels = (Pixel*)ARRAYOBJ_START(userData->pixelsObj);
#endif
}

/** Description:
   This function is called for every row in the image.  If the
   image is interlaced, and you turned on the interlace handler,
   this function will be called for every row in every pass.

   In this function you will receive a pointer to new row data from
   libpng called new_row that is to replace a corresponding row (of
   the same data format) in a buffer allocated by your application.

   The new row data pointer new_row may be NULL, indicating there is
   no new data to be replaced (in cases of interlace loading).

   If new_row is not NULL then you need to call
   png_progressive_combine_row() to replace the corresponding row as
   shown below:
*/
static uint8 pngQuantize4(uint8 value)
{
   return (uint8)(((uint32)value * 15 + 127) / 255);
}

static uint8 pngPremultiply4(uint8 value, uint8 alpha4)
{
   return pngQuantize4((uint8)(((uint32)value * alpha4 + 7) / 15));
}

static void row_callback(png_structp png_ptr, png_bytep new_row, png_uint_32 row_num, int pass)
{
   UserData * userData = (UserData *)png_get_progressive_ptr(png_ptr);
   if (!userData->pixelsObj && !userData->pixels && !userData->rgbaStorage
         && !userData->compactStorage)
      return;
   png_bytep old_row = userData->upixels;
   png_progressive_combine_row(png_ptr, old_row, new_row);

   if (pass == userData->lastPass)
   {
      uint8* buffer = old_row;
      int32 x;
      png_byte color_type = png_get_color_type(png_ptr, userData->info_ptr);
      int32 num_trans = 0;
      png_byte channels = png_get_channels(png_ptr, userData->info_ptr);
      png_get_tRNS(png_ptr, userData->info_ptr, null, &num_trans, null);
      if (userData->opacityMetadata && userData->opacityAlphaOutput && channels == 4) {
         png_bytep alpha = buffer + 3;
         for (x = 0; x < userData->width; x++, alpha += 4) {
            if (*alpha != 0xFF) {
               userData->pixelsOpaque = false;
               break;
            }
         }
      }
      if (userData->compactStorage) {
         const size_t rowOffset = (size_t)row_num * userData->width
            * (userData->storageFormat == IMAGE_BACKING_FORMAT_GRAY8 ? 1 : 2);
         uint8* destination = userData->compactStorage + rowOffset;
         if (userData->storageFormat == IMAGE_BACKING_FORMAT_GRAY8) {
            if (channels == 1) {
               for (x = 0; x < userData->width; x++)
                  destination[x] = buffer[x];
            } else {
               for (x = 0; x < userData->width; x++, buffer += channels)
                  destination[x] = buffer[0];
            }
         } else if (userData->storageFormat == IMAGE_BACKING_FORMAT_RGB565) {
            for (x = 0; x < userData->width; x++, buffer += channels) {
               const uint16 packed = (uint16)((((uint16)buffer[0] * 31 + 127) / 255) << 11)
                  | (uint16)((((uint16)buffer[1] * 63 + 127) / 255) << 5)
                  | (uint16)(((uint16)buffer[2] * 31 + 127) / 255);
               destination[x * 2] = (uint8)(packed & 0xFF);
               destination[x * 2 + 1] = (uint8)(packed >> 8);
            }
         } else {
            for (x = 0; x < userData->width; x++, buffer += channels) {
               uint8 red = buffer[0];
               uint8 green = buffer[1];
               uint8 blue = buffer[2];
               uint8 alpha = channels == 4 ? buffer[3] : 0xFF;
               uint8 alpha4 = pngQuantize4(alpha);
               uint16 packed = (uint16)pngPremultiply4(red, alpha4) << 12;
               packed |= (uint16)pngPremultiply4(green, alpha4) << 8;
               packed |= (uint16)pngPremultiply4(blue, alpha4) << 4;
               packed |= alpha4;
               destination[x * 2] = (uint8)(packed & 0xFF);
               destination[x * 2 + 1] = (uint8)(packed >> 8);
            }
         }
      } else if (userData->zeroCopy) {
         uint8* destination = userData->rgbaStorage + (size_t)row_num * userData->width * 4;
         if (channels == 4 || (color_type == PNG_COLOR_TYPE_PALETTE && num_trans > 6)) {
            for (x = 0; x < userData->width; x++, buffer += 4, destination += 4) {
               destination[0] = (uint8)buffer[0];
               destination[1] = (uint8)buffer[1];
               destination[2] = (uint8)buffer[2];
               destination[3] = (uint8)buffer[3];
            }
         } else {
            for (x = 0; x < userData->width; x++, buffer += 3, destination += 4) {
               destination[0] = (uint8)buffer[0];
               destination[1] = (uint8)buffer[1];
               destination[2] = (uint8)buffer[2];
               destination[3] = 0xFF;
            }
         }
      } else if (channels == 4 || (color_type == PNG_COLOR_TYPE_PALETTE && num_trans > 6))
         for (x = 0; x < userData->width; x++, buffer += 4)
            *userData->pixels++ = makePixelA((uint8)buffer[3], (uint8)buffer[0], (uint8)buffer[1], (uint8)buffer[2]);
      else
         for (x = 0; x < userData->width; x++, buffer += 3)
            *userData->pixels++ = makePixel((uint8)buffer[0], (uint8)buffer[1], (uint8)buffer[2]);
      userData->rowsDecoded++;
      userData->quit = (int32)row_num == (userData->height-1);
   }
}

static void error_callback(png_structp png_ptr, png_const_charp msg)
{
   Heap h = (Heap) png_get_error_ptr(png_ptr);
   UserData *userData = (UserData *)png_get_progressive_ptr(png_ptr);
   if (userData)
      *userData->decodeStatus = IMAGE_DECODE_CORRUPT;
   HEAP_ERROR(h, 996);
   UNUSED(msg)
}
