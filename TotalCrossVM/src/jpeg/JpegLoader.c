// Copyright (C) 1991-1996, Thomas G. Lane.
// Copyright (C) 2003 Jaxo-Systems (Pierre G. Richard)
// Copyright (C) 2003-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
* This software is based in part on the work of the Independent JPEG Group.
*/

#include "cdjpeg.h"     /* Common decls for cjpeg/djpeg applications */

#if defined _WINDOWS || defined WINCE
#ifndef fmin
#define fmin(a, b) min(a,b)
#endif
#endif

// Read the JPEG Input file.
int jpegRead(void *buff, int count, JPEGFILE *in)
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
      return (int)(cur - start);
   }
}

// Write the JPEG Output file.
int jpegWrite(void *buff, int count, JPEGFILE *in)
{
   TCObject bufObj = in->params[1].asObj;
   int32 bufObjSize = ARRAYOBJ_LEN(in->bufObj);
   int32 remaining;
   int32 current = 0;
   int32 toCopy;

   while (current < count)
   {
      remaining = count - current;
      toCopy = in->params[3].asInt32 = bufObjSize < remaining ? bufObjSize : remaining;
      xmemmove(ARRAYOBJ_START(bufObj), (uint8*) buff + current, toCopy);
      executeMethod(in->currentContext, in->writeBytesMethod, in->params[0].asObj, in->params[1].asObj, in->params[2].asInt32, in->params[3].asInt32);
      current += toCopy;
   }
   return current;
}

#define F1_8 12.5
#define F1_4 25
#define F1_2 50

// imageObj+tcz+first4, if reading from a tcz; imageObj+inputStream+bufObj+bufCount, if reading from a totalcross.io.Stream
void jpegLoad(Context currentContext, TCObject imageObj, TCObject inputStreamObj, TCObject bufObj, TCZFile tcz, char* first4, int32 targetWidthOrScaleNum, int32 targetHeightOrScaleDenom)
{
   JPEGFILE file;
   Pixel *pixels;
   Heap heap;
   struct jpeg_error_mgr errbase;
   JSAMPARRAY buffer0; // Output pixel-row buffer
   uint8* buffer;
   int32 x,width,height;
   struct jpeg_decompress_struct cinfo;
   TCObject pixelsObj;

   xmemzero(&errbase, sizeof(errbase));
   xmemzero(&cinfo, sizeof(cinfo));
   xmemzero(&file, sizeof(file));

   heap = heapCreate();
   if (!heap)
      return; // throwImageException("Out of memory");

   file.currentContext = currentContext;
   if (tcz != null)
   {
      file.tcz = tcz;
      tcz->tempHeap = heap;
   }
   else
   {
      file.inputStreamObj = inputStreamObj;  // JPEG stream
      file.bufObj = bufObj;    // a byte array for readBytes()
   }
   file.first4 = first4;

   /* initialize default error handling. */
   errbase.first_addon_message = JMSG_FIRSTADDONCODE;
   errbase.last_addon_message = JMSG_LASTADDONCODE;
   errbase.heap = heap;

   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      if (tcz != null)
         tczClose(tcz);
	  throwException(currentContext, ImageException, null);
      return; // throwImageException(...);
   }
   /* Start decompressor */
   cinfo.err = jpeg_std_error(&errbase);
   jpeg_create_decompress(&cinfo);

   if (tcz == null)
   {
      Method readBytesMethod = getMethod(OBJ_CLASS(file.inputStreamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
      if (readBytesMethod == null)
         HEAP_ERROR(heap, 999);
      file.readBytesMethod = readBytesMethod;
      file.params[0].asObj = file.inputStreamObj;
      file.params[1].asObj = file.bufObj;
   }
   jpeg_stdio_src(&cinfo, &file); /* Specify data source for decompression */
   jpeg_read_header(&cinfo, TRUE); /* Read file header, set default decompression parameters */
   /* override with specified decompression parameters */
   cinfo.dither_mode = JDITHER_NONE; // 8580 -> 5360
   cinfo.dct_method = JDCT_IFAST;
   if (targetWidthOrScaleNum > 0 && targetHeightOrScaleDenom > 0) {
      double p1 = targetWidthOrScaleNum * 100 / cinfo.image_width;
      double p2 = targetHeightOrScaleDenom * 100 / cinfo.image_height;
      double p = fmin(p1, p2);
      int32 scale_num2 = 1;
      int32 scale_denom2;
      
      if (p < F1_8) {
         scale_denom2 = 8; // 1/8
      } else if (p < F1_4) {
         scale_denom2 = 4; // 1/4
      } else if (p < F1_2) {
         scale_denom2 = 2; // 1/2
      } else {
         scale_denom2 = 1; // original size
      }

      cinfo.scale_num = scale_num2;
      cinfo.scale_denom = scale_denom2;
   } else if (targetWidthOrScaleNum < 0 && targetHeightOrScaleDenom < 0) {
      cinfo.scale_num = -targetWidthOrScaleNum;
      cinfo.scale_denom = -targetHeightOrScaleDenom;
   }

   jpeg_calc_output_dimensions(&cinfo); /* Calculate output image dimensions so we can allocate space */

   /* Create space for the pixels. and get the drawRow method */
   width = cinfo.output_width;
   height = cinfo.output_height;
   if (width > 65535 || height > 65535)  // bad width/height?
      HEAP_ERROR(heap, 998);

   Image_pixels(imageObj) = pixelsObj = createIntArray(currentContext, width*height);
   if (!pixelsObj)
      HEAP_ERROR(heap, 997);
   setObjectLock(pixelsObj, UNLOCKED);
   pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);

   /* Create decompressor output buffer. */
   buffer0 = (*cinfo.mem->alloc_sarray)((j_common_ptr) &cinfo, JPOOL_IMAGE, (width * cinfo.output_components+3) & ~3, (JDIMENSION)1);
   jpeg_start_decompress(&cinfo); /* Start decompressor */

   while (cinfo.output_scanline < cinfo.output_height)  /* Process data */
   {
      buffer = buffer0[0];
      jpeg_read_scanlines(&cinfo, buffer0, 1);
      if (cinfo.out_color_components == 1) // guich@tc114_12
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
   if (tcz != null)
      tczClose(tcz);
   heapDestroy(heap);
}

bool rgb565_2jpeg(Context currentContext, TCObject srcStreamObj, TCObject dstStreamObj, int32 width, int32 height)
{
   JPEGFILE srcFile, dstFile;
   struct jpeg_error_mgr errbase;
   struct jpeg_compress_struct cinfo;
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

   /* initialize default error handling. */
   errbase.first_addon_message = JMSG_FIRSTADDONCODE;
   errbase.last_addon_message = JMSG_LASTADDONCODE;
   errbase.heap = heap;

   // initialize error handler and compressor.
   cinfo.err = jpeg_std_error(&errbase);
   jpeg_create_compress(&cinfo);

   // set the compressor output to dstFile
   jpeg_stdio_dest(&cinfo, &dstFile);

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
   struct jpeg_error_mgr errbase;
   struct jpeg_compress_struct cinfo;
   volatile Heap heap;
   TCObject bufObj;
   uint8* bufAux;
   int32 i, scanLineOut;
   volatile bool ret = false;                  
   
   TCObject pixObj = (Image_frameCount(srcImageObj) > 1) ? Image_pixelsOfAllFrames(srcImageObj) : Image_pixels(srcImageObj);
   PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(pixObj);
   int32 width = (Image_frameCount(srcImageObj) > 1) ? Image_widthOfAllFrames(srcImageObj) : Image_width(srcImageObj);
   int32 height = Image_height(srcImageObj);
   scanLineOut = width * 3;

   // initialize structs
   xmemzero(&errbase, sizeof(errbase));
   xmemzero(&cinfo, sizeof(cinfo));
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

   /* initialize default error handling. */
   errbase.first_addon_message = JMSG_FIRSTADDONCODE;
   errbase.last_addon_message = JMSG_LASTADDONCODE;
   errbase.heap = heap;

   // initialize error handler and compressor.
   cinfo.err = jpeg_std_error(&errbase);
   jpeg_create_compress(&cinfo);

   // set the compressor output to dstFile
   jpeg_stdio_dest(&cinfo, &dstFile);

	cinfo.image_width = width; 	/* image width and height, in pixels */
	cinfo.image_height = height;
	cinfo.input_components = 3;	/* # of color components per pixel */
	cinfo.in_color_space = JCS_RGB; /* colorspace of input image */

   // set required parameters to default values
	jpeg_set_defaults(&cinfo);

   jpeg_set_quality(&cinfo, quality, TRUE);

   /* Make optional parameter settings here */
   jpeg_default_colorspace(&cinfo);

    /* Start compressor */
   jpeg_start_compress(&cinfo, true);

   while (cinfo.next_scanline < cinfo.image_height) /* Process data */
   {
      for (i = width ; --i >= 0; pixels++)
      {
         *bufAux++ = pixels->r;
         *bufAux++ = pixels->g;
         *bufAux++ = pixels->b;
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

