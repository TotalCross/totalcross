// Copyright (C) 1991-1996, Thomas G. Lane.
// Copyright (C) 2003 Jaxo-Systems (Pierre G. Richard)
// Copyright (C) 2003-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
* This software is based in part on the work of the Independent JPEG Group.
*/

#include <string.h>

#include "JpegLoader.h"
#include "jerror-tc.h"
#include "jerror.h"
#include "ui/ImageTestAccounting_c.h"
#if TC_RENDERER_SKIA
#include "ui/NativeImageBacking.h"
#include "ui/skia/skia.h"
#endif

#include <stdlib.h>

#if defined _WINDOWS || defined WINCE
#ifndef fmin
#define fmin(a, b) min(a,b)
#endif
#endif

static size_t jpegReadCallback(void *opaque, void *buffer, size_t count)
{
   JPEGFILE *in = (JPEGFILE *)opaque;
   int32 extra = 0,n;
   int32 remaining = (int32)count;
   uint8* cur = (uint8*)buffer;

   if (in->mapped) {
      extra = in->size - in->cursor;
      if (extra == 0) {
         return 0;
      }
      if (extra > remaining) {
         extra = remaining;
      }
      memcpy(buffer, in->mapped + in->cursor, extra);
      in->cursor += extra;
      return (size_t)extra;
   }
   else {
      if (in->first4) // place the first 4 bytes in the output buffer
      {
         xmove4(cur, in->first4);
         in->first4 = null;
         cur += 4;
         remaining -= 4;
         extra = 4;
      }
      if (in->tcz != null)
         return (size_t)(tczRead(in->tcz, cur, remaining) + extra);
      else
      {
         uint8* start = (uint8*)buffer;
         TCObject bufObj = in->params[1].asObj;
         int tempBufSize = ARRAYOBJ_LEN(bufObj);
         uint8 *tempBufStart = (uint8*)ARRAYOBJ_START(bufObj);

         while (remaining > 0)
         {
            n=0;
            in->params[3].asInt32 = (remaining < tempBufSize)? remaining : tempBufSize;
            n = executeMethod(in->currentContext, in->readBytesMethod, in->params[0].asObj, in->params[1].asObj, in->params[2].asInt32, in->params[3].asInt32).asInt32;
            if (n <= 0)
               break;
            xmemmove(cur, tempBufStart, n);
            cur += n;
            remaining -= n;
         }
         return (size_t)(cur - start);
      }
   }
   return 0;
}

static size_t jpegWriteCallback(void *opaque, const void *buffer, size_t count)
{
   JPEGFILE *in = (JPEGFILE *)opaque;
   TCObject bufObj = in->params[1].asObj;
   int32 bufObjSize = ARRAYOBJ_LEN(in->bufObj);
   int32 remaining;
   int32 current = 0;
   int32 toCopy;
   int32 total = (int32)count;

   while (current < total)
   {
      remaining = total - current;
      toCopy = in->params[3].asInt32 = bufObjSize < remaining ? bufObjSize : remaining;
      xmemmove(ARRAYOBJ_START(bufObj), (uint8*) buffer + current, toCopy);
      executeMethod(in->currentContext, in->writeBytesMethod, in->params[0].asObj, in->params[1].asObj, in->params[2].asInt32, in->params[3].asInt32);
      current += toCopy;
   }
   return (size_t)current;
}

static bool jpegBestFitFits(JDIMENSION sourceDimension, int32 scaleDenominator, int32 targetDimension)
{
   uint64 decodedDimension = ((uint64)sourceDimension + scaleDenominator - 1) / scaleDenominator;
   return decodedDimension >= (uint64)targetDimension;
}

static int32 jpegBestFitScaleDenominatorForDimension(JDIMENSION sourceDimension, int32 targetDimension)
{
   if (jpegBestFitFits(sourceDimension, 8, targetDimension))
      return 8; // 1/8
   if (jpegBestFitFits(sourceDimension, 4, targetDimension))
      return 4; // 1/4
   if (jpegBestFitFits(sourceDimension, 2, targetDimension))
      return 2; // 1/2
   return 1; // original size
}

static int32 jpegBestFitScaleDenominator(JDIMENSION sourceWidth, JDIMENSION sourceHeight,
      int32 targetWidth, int32 targetHeight)
{
   if ((uint64)targetWidth * sourceHeight <= (uint64)targetHeight * sourceWidth)
      return jpegBestFitScaleDenominatorForDimension(sourceWidth, targetWidth);
   return jpegBestFitScaleDenominatorForDimension(sourceHeight, targetHeight);
}

static int32 jpegTargetDecodeScaleDenominator(JDIMENSION sourceWidth, JDIMENSION sourceHeight,
      int32 targetWidth, int32 targetHeight)
{
   if (jpegBestFitFits(sourceWidth, 8, targetWidth)
         && jpegBestFitFits(sourceHeight, 8, targetHeight))
      return 8;
   if (jpegBestFitFits(sourceWidth, 4, targetWidth)
         && jpegBestFitFits(sourceHeight, 4, targetHeight))
      return 4;
   if (jpegBestFitFits(sourceWidth, 2, targetWidth)
         && jpegBestFitFits(sourceHeight, 2, targetHeight))
      return 2;
   return 1;
}

// imageObj+tcz+first4, if reading from a tcz; imageObj+inputStream+bufObj+bufCount, if reading from a totalcross.io.Stream
ImageDecodeStatus jpegLoad(Context currentContext, TCObject imageObj, TCObject inputStreamObj, TCObject bufObj,
      TCZFile tcz, const char* first4, int32 size, JpegDecodeMode mode, int32 modeArg1, int32 modeArg2,
      bool zeroCopy)
{
   JPEGFILE file;
   Pixel *pixels;
   Pixel *pixelStorage;
   uint8* rgbaStorage = null;
   Heap heap;
   TCJpegErrorManager errbase;
   JSAMPARRAY buffer0; // Output pixel-row buffer
   uint8* buffer;
   int32 x,width,height;
   struct jpeg_decompress_struct cinfo;
   TCJpegIOContext io;
   TCObject pixelsObj;
#if TC_RENDERER_SKIA
   int64 nativeHandle = 0;
#endif
   volatile ImageDecodeStatus status = IMAGE_DECODE_SUCCESS;

   xmemzero(&errbase, sizeof(errbase));
   xmemzero(&cinfo, sizeof(cinfo));
   xmemzero(&io, sizeof(io));
   xmemzero(&file, sizeof(file));

   heap = heapCreate();
   if (!heap)
      return IMAGE_DECODE_RESOURCE_FAILURE;

   file.currentContext = currentContext;
   if (tcz != null)
   {
      file.tcz = tcz;
      tcz->tempHeap = heap;
   }
   else if (inputStreamObj != null)
   {
      file.inputStreamObj = inputStreamObj;  // JPEG stream
      file.bufObj = bufObj;    // a byte array for readBytes()
   } else {
      file.mapped = first4;
      file.size = size;
      file.cursor = 0;
   }
   file.first4 = first4;

   IF_HEAP_ERROR(heap)
   {
      if (status == IMAGE_DECODE_SUCCESS)
         status = IMAGE_DECODE_RESOURCE_FAILURE;
      heapDestroy(heap);
      if (tcz != null)
         tczClose(tcz);
      return status;
   }
   errbase.decodeStatus = &status;
   /* Start decompressor */
   cinfo.err = tc_jpeg_std_error(&errbase, heap);
   jpeg_create_decompress(&cinfo);
   io.opaque = &file;
   io.read = jpegReadCallback;
   cinfo.client_data = &io;

   if (inputStreamObj != null)
   {
      Method readBytesMethod = getMethod(OBJ_CLASS(file.inputStreamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
      if (readBytesMethod == null)
         HEAP_ERROR(heap, 999);
      file.readBytesMethod = readBytesMethod;
      file.params[0].asObj = file.inputStreamObj;
      file.params[1].asObj = file.bufObj;
   }
   jpeg_tc_src(&cinfo); /* Specify data source for decompression */
   jpeg_read_header(&cinfo, TRUE); /* Read file header, set default decompression parameters */
   /* override with specified decompression parameters */
   cinfo.dither_mode = JDITHER_NONE; // 8580 -> 5360
   cinfo.dct_method = JDCT_IFAST;
   switch (mode) {
      case JPEG_DECODE_BEST_FIT:
         cinfo.scale_num = 1;
         cinfo.scale_denom = jpegBestFitScaleDenominator(cinfo.image_width, cinfo.image_height,
               modeArg1, modeArg2);
         break;
      case JPEG_DECODE_TARGET_DECODE:
         cinfo.scale_num = 1;
         cinfo.scale_denom = jpegTargetDecodeScaleDenominator(cinfo.image_width, cinfo.image_height,
               modeArg1, modeArg2);
         break;
      case JPEG_DECODE_EXPLICIT_RATIO:
         cinfo.scale_num = modeArg1;
         cinfo.scale_denom = modeArg2;
         break;
      case JPEG_DECODE_FULL:
      default:
         break;
   }

   jpeg_calc_output_dimensions(&cinfo); /* Calculate output image dimensions so we can allocate space */

   /* Create space for the pixels. and get the drawRow method */
   width = cinfo.output_width;
   height = cinfo.output_height;
   if (width > 65535 || height > 65535)  // bad width/height?
   {
      status = IMAGE_DECODE_CORRUPT;
      HEAP_ERROR(heap, 998);
   }

   if (imageDecodeConsumeAllocationFailureForTest())
   {
      status = IMAGE_DECODE_RESOURCE_FAILURE;
      jpeg_abort_decompress(&cinfo);
      jpeg_destroy_decompress(&cinfo);
      if (tcz != null)
         tczClose(tcz);
      heapDestroy(heap);
      Image_backing(imageObj) = null;
      return status;
   }
#if TC_RENDERER_SKIA
   if ((uint64)width * height > (uint64)0x7FFFFFFF / sizeof(Pixel))
   {
      status = IMAGE_DECODE_RESOURCE_FAILURE;
      jpeg_abort_decompress(&cinfo);
      jpeg_destroy_decompress(&cinfo);
      if (tcz != null)
         tczClose(tcz);
      heapDestroy(heap);
      Image_backing(imageObj) = null;
      return status;
   }
   if (zeroCopy) {
      rgbaStorage = (uint8*)malloc((size_t)width * height * 4);
      if (!rgbaStorage)
      {
         status = IMAGE_DECODE_RESOURCE_FAILURE;
         jpeg_abort_decompress(&cinfo);
         jpeg_destroy_decompress(&cinfo);
         if (tcz != null)
            tczClose(tcz);
         heapDestroy(heap);
         Image_backing(imageObj) = null;
         return status;
      }
   } else {
      pixelStorage = pixels = (Pixel*)xmalloc((int32)((uint64)width * height * sizeof(Pixel)));
      if (!pixelStorage)
      {
         status = IMAGE_DECODE_RESOURCE_FAILURE;
         jpeg_abort_decompress(&cinfo);
         jpeg_destroy_decompress(&cinfo);
         if (tcz != null)
            tczClose(tcz);
         heapDestroy(heap);
         Image_backing(imageObj) = null;
         return status;
      }
   }
#else
   pixelsObj = createIntArray(currentContext, width*height);
   if (!pixelsObj)
   {
      status = IMAGE_DECODE_RESOURCE_FAILURE;
      jpeg_abort_decompress(&cinfo);
      jpeg_destroy_decompress(&cinfo);
      if (tcz != null)
         tczClose(tcz);
      heapDestroy(heap);
      Image_backing(imageObj) = null;
      return status;
   }
   setObjectLock(pixelsObj, UNLOCKED);
   TCObject backing = createObject(currentContext, "totalcross.ui.image.RasterImageBacking");
   if (!backing)
   {
      status = IMAGE_DECODE_RESOURCE_FAILURE;
      Image_backing(imageObj) = null;
      return status;
   }
   RasterImageBacking_pixels(backing) = pixelsObj;
   RasterImageBacking_pixelsOfAllFrames(backing) = null;
   RasterImageBacking_width(backing) = width;
   RasterImageBacking_height(backing) = height;
   RasterImageBacking_frameCount(backing) = 1;
   RasterImageBacking_widthOfAllFrames(backing) = width;
   setObjectLock(backing, UNLOCKED);
   Image_backing(imageObj) = backing;
   pixelStorage = pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
#endif

   /* Create decompressor output buffer. */
   buffer0 = (*cinfo.mem->alloc_sarray)((j_common_ptr) &cinfo, JPOOL_IMAGE, (width * cinfo.output_components+3) & ~3, (JDIMENSION)1);
   jpeg_start_decompress(&cinfo); /* Start decompressor */

   while (cinfo.output_scanline < cinfo.output_height)  /* Process data */
   {
      buffer = buffer0[0];
      jpeg_read_scanlines(&cinfo, buffer0, 1);
      if (zeroCopy) {
         uint8* destination = rgbaStorage + (size_t)(cinfo.output_scanline - 1) * width * 4;
         if (cinfo.out_color_components == 1) {
            for (x = 0; x < width; x++, buffer++, destination += 4) {
               destination[0] = (uint8)buffer[0];
               destination[1] = (uint8)buffer[0];
               destination[2] = (uint8)buffer[0];
               destination[3] = 0xFF;
            }
         } else {
            for (x = 0; x < width; x++, buffer += 3, destination += 4) {
               destination[0] = (uint8)buffer[0];
               destination[1] = (uint8)buffer[1];
               destination[2] = (uint8)buffer[2];
               destination[3] = 0xFF;
            }
         }
      } else if (cinfo.out_color_components == 1) // guich@tc114_12
         for (x = 0; x < width; x++, buffer++)
            *pixels++ = makePixelA(0xFF,(uint8)buffer[0], (uint8)buffer[0], (uint8)buffer[0]);
      else
         for (x = 0; x < width; x++, buffer += 3)
            *pixels++ = makePixelA(0xFF,(uint8)buffer[0], (uint8)buffer[1], (uint8)buffer[2]);
   }

   // now that everything went fine, set the image's width/height
   Image_width(imageObj) = width;
   Image_height(imageObj) = height;
   // Finish decompression and release memory. Do it in this order because output module
   // has allocated memory of lifespan JPOOL_IMAGE; it needs to finish before releasing memory.
   jpeg_finish_decompress(&cinfo);
   jpeg_destroy_decompress(&cinfo);
#if TC_RENDERER_SKIA
   {
      const int32 pixelBytes = (int32)((uint64)width * height * 4);
      if (zeroCopy) {
         nativeHandle = skia_image_backing_create_from_owned_rgba_pixels(rgbaStorage, width, height);
         rgbaStorage = null;
      } else {
         nativeHandle = skia_image_backing_create_from_argb_pixels(pixelStorage, width, height);
         xfree(pixelStorage);
         pixelStorage = null;
      }
      if (nativeHandle) {
         if (zeroCopy) {
            imageRecordTestCounter("zeroCopyDecodeCountForTest");
         } else {
            imageRecordTestCounter("copiedDecodeCountForTest");
            imageAddTestCounter("decodeCopiedBytesForTest", pixelBytes);
         }
         imageAddTestCounter("decodeFinalBufferBytesForTest", pixelBytes);
      }
   }
   if (!nativeHandle || !imageInstallNativeBacking(currentContext, imageObj, nativeHandle, width, height)) {
      status = IMAGE_DECODE_RESOURCE_FAILURE;
      Image_width(imageObj) = 0;
      Image_height(imageObj) = 0;
   }
#endif
   if (tcz != null)
      tczClose(tcz);
   heapDestroy(heap);

   return status;
}

bool rgb565_2jpeg(Context currentContext, TCObject srcStreamObj, TCObject dstStreamObj, int32 width, int32 height)
{
   JPEGFILE srcFile, dstFile;
   TCJpegErrorManager errbase;
   struct jpeg_compress_struct cinfo;
   TCJpegIOContext dstIO;
   volatile Heap heap;
   TCObject bufObj;
   uint8* bufP;
   uint8* bufAux;
   int32 i, p;
   int32 scanLineIn = width * 2;
   int32 scanLineOut = width * 3;
   volatile bool ret = false;

   // initialize structs
   xmemzero(&errbase, sizeof(errbase));
   xmemzero(&cinfo, sizeof(cinfo));
   xmemzero(&dstIO, sizeof(dstIO));
   xmemzero(&srcFile, sizeof(srcFile));
   xmemzero(&dstFile, sizeof(dstFile));

   if ((bufObj = createByteArray(currentContext, scanLineIn)) == null)
      return false;

   // initialize srcFile structure
   srcFile.currentContext = currentContext;
   srcFile.inputStreamObj = srcStreamObj;
   srcFile.bufObj = bufObj;    // a byte array for readBytes()
   srcFile.readBytesMethod = getMethod(OBJ_CLASS(srcFile.inputStreamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   srcFile.params[0].asObj = srcFile.inputStreamObj;
   srcFile.params[1].asObj = srcFile.bufObj;

   // initialize dstFile structure
   dstFile.currentContext = currentContext;
   dstFile.outputStreamObj = dstStreamObj;
   dstFile.bufObj = bufObj;    // a byte array for writeBytes()
   dstFile.writeBytesMethod = getMethod(OBJ_CLASS(dstFile.outputStreamObj), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   dstFile.params[0].asObj = dstFile.outputStreamObj;
   dstFile.params[1].asObj = dstFile.bufObj;

   // heap creation
   heap = heapCreate();
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      throwException(currentContext, OutOfMemoryError, null);
      goto finish;
   }

   bufAux = (uint8*) heapAlloc(heap, scanLineOut);

   // initialize error handler and compressor.
   cinfo.err = tc_jpeg_std_error(&errbase, heap);
   jpeg_create_compress(&cinfo);
   dstIO.opaque = &dstFile;
   dstIO.write = jpegWriteCallback;
   cinfo.client_data = &dstIO;

   // set the compressor output to dstFile
   jpeg_tc_dest(&cinfo);

	cinfo.image_width = width; 	/* image width and height, in pixels */
	cinfo.image_height = height;
	cinfo.input_components = 3;	/* # of color components per pixel */
	cinfo.in_color_space = JCS_RGB; /* colorspace of input image */

   // set required parameters to default values
	jpeg_set_defaults(&cinfo);

   jpeg_set_quality(&cinfo, 85, TRUE);

   /* Make optional parameter settings here */
   jpeg_default_colorspace(&cinfo);

    /* Start compressor */
   jpeg_start_compress(&cinfo, true);

   while (cinfo.next_scanline < cinfo.image_height) /* Process data */
   {
      executeMethod(srcFile.currentContext, srcFile.readBytesMethod, srcFile.params[0].asObj, srcFile.params[1].asObj, 0, scanLineIn);
      if (currentContext->thrownException != null)
      {
         jpeg_abort_compress(&cinfo);
         goto finish;
      }

      bufP = ARRAYOBJ_START(bufObj);
      for (i = width ; --i >= 0; bufP += 2)
      {
         p = (bufP[0] & 0xFF) | ((bufP[1] & 0xFF) << 8); // aaaaabbbbbbccccc

         *bufAux++ = ((p & 0xF800) >> 8) | 7;
         *bufAux++ = ((p & 0x07E0) >> 3) | 3;
         *bufAux++ = ((p & 0x001F) << 3) | 7;
      }
      bufP -= scanLineIn;
      bufAux -= scanLineOut;

      jpeg_write_scanlines(&cinfo, &bufAux, 1);
   }

   // Finish decompression and release memory. Do it in this order because output module
   // has allocated memory of lifespan JPOOL_IMAGE; it needs to finish before releasing memory.
   jpeg_finish_compress(&cinfo);
   jpeg_destroy_compress(&cinfo);
   ret = true; // finished successfully

finish:
   if (bufObj != null)
      setObjectLock(bufObj, UNLOCKED);
   if (heap != null)
      heapDestroy(heap);

   return ret;
}

bool image2jpeg(Context currentContext, TCObject srcImageObj, TCObject dstStreamObj, int32 quality)
{
   JPEGFILE dstFile;
   TCJpegErrorManager errbase;
   struct jpeg_compress_struct cinfo;
   TCJpegIOContext dstIO;
   volatile Heap heap;
   TCObject bufObj;
   uint8* bufAux;
   int32 i, scanLineOut;
   volatile bool ret = false;                  
   
   PixelConv *pixels = null;
   TCObject backing = Image_backing(srcImageObj);
   TCObject pixObj = null;
#if TC_RENDERER_SKIA
   Pixel *nativeRow = null;
   bool nativeBacking = backing != null && strEq(OBJ_CLASS(backing)->name,
      "totalcross.ui.image.NativeImageBacking");
#else
   bool nativeBacking = false;
#endif
   if (!nativeBacking) {
      pixObj = (Image_frameCount(srcImageObj) > 1)
         ? RasterImageBacking_pixelsOfAllFrames(backing)
         : RasterImageBacking_pixels(backing);
      if (!pixObj)
         return false;
      pixels = (PixelConv*)ARRAYOBJ_START(pixObj);
   }
   int32 width = (Image_frameCount(srcImageObj) > 1) ? Image_widthOfAllFrames(srcImageObj) : Image_width(srcImageObj);
   int32 height = Image_height(srcImageObj);
   scanLineOut = width * 3;

   // initialize structs
   xmemzero(&errbase, sizeof(errbase));
   xmemzero(&cinfo, sizeof(cinfo));
   xmemzero(&dstIO, sizeof(dstIO));
   xmemzero(&dstFile, sizeof(dstFile));

   if ((bufObj = createByteArray(currentContext, scanLineOut)) == null)
      return false;

   // initialize srcFile structure

   // initialize dstFile structure
   dstFile.currentContext = currentContext;
   dstFile.outputStreamObj = dstStreamObj;
   dstFile.bufObj = bufObj;    // a byte array for writeBytes()
   dstFile.writeBytesMethod = getMethod(OBJ_CLASS(dstFile.outputStreamObj), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   dstFile.params[0].asObj = dstFile.outputStreamObj;
   dstFile.params[1].asObj = dstFile.bufObj;

   // heap creation
   heap = heapCreate();
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      throwException(currentContext, OutOfMemoryError, null);
      goto finish;
   }

   bufAux = (uint8*) heapAlloc(heap, scanLineOut);
#if TC_RENDERER_SKIA
   if (nativeBacking) {
      if (width <= 0 || width > (int32)(0x7FFFFFFF / sizeof(Pixel)))
         goto finish;
      nativeRow = (Pixel*)heapAlloc(heap, width * sizeof(Pixel));
      if (!nativeRow)
         goto finish;
   }
#endif

   // initialize error handler and compressor.
   cinfo.err = tc_jpeg_std_error(&errbase, heap);
   jpeg_create_compress(&cinfo);
   dstIO.opaque = &dstFile;
   dstIO.write = jpegWriteCallback;
   cinfo.client_data = &dstIO;

   // set the compressor output to dstFile
   jpeg_tc_dest(&cinfo);

	cinfo.image_width = width; 	/* image width and height, in pixels */
	cinfo.image_height = height;
	cinfo.input_components = 3;	/* # of color components per pixel */
	cinfo.in_color_space = JCS_RGB; /* colorspace of input image */

   // set required parameters to default values
	jpeg_set_defaults(&cinfo);

   jpeg_set_quality(&cinfo, quality, TRUE);

   /* Make optional parameter settings here */
   jpeg_default_colorspace(&cinfo);
   cinfo.dct_method = JDCT_FLOAT;
   cinfo.optimize_coding = TRUE;

    /* Start compressor */
   jpeg_start_compress(&cinfo, true);

   while (cinfo.next_scanline < cinfo.image_height) /* Process data */
   {
      if (nativeBacking) {
#if TC_RENDERER_SKIA
         if (!skia_image_backing_read_row(NativeImageBacking_nativeHandle(backing),
               nativeRow, (int32)cinfo.next_scanline, width)) {
            jpeg_abort_compress(&cinfo);
            jpeg_destroy_compress(&cinfo);
            goto finish;
         }
         for (i = 0; i < width; ++i) {
            *bufAux++ = (uint8)((nativeRow[i] >> 16) & 0xFF);
            *bufAux++ = (uint8)((nativeRow[i] >> 8) & 0xFF);
            *bufAux++ = (uint8)(nativeRow[i] & 0xFF);
         }
#endif
      } else {
         for (i = width ; --i >= 0; pixels++)
         {
            *bufAux++ = pixels->r;
            *bufAux++ = pixels->g;
            *bufAux++ = pixels->b;
         }
      }
      bufAux -= scanLineOut;

      jpeg_write_scanlines(&cinfo, &bufAux, 1);
   }

   // Finish decompression and release memory. Do it in this order because output module
   // has allocated memory of lifespan JPOOL_IMAGE; it needs to finish before releasing memory.
   jpeg_finish_compress(&cinfo);
   jpeg_destroy_compress(&cinfo);
   ret = true; // finished successfully

finish:
   if (bufObj != null)
      setObjectLock(bufObj, UNLOCKED);
   if (heap != null)
      heapDestroy(heap);

   return ret;
}
