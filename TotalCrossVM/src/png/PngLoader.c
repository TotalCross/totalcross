// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "png.h"
#include "tcvm.h"
#include "pngstruct.h"
#include "pnginfo.h"

static void row_callback(png_structp, png_bytep, png_uint_32, int);
static void info_callback(png_structp png_ptr, png_infop info);
static void error_callback(png_structp, png_const_charp);

typedef struct
{
   Heap heap;
   TCObject imageObj;
   Pixel* pixels;
   TCZFile tcz; // if filled, we're reading from a tcz file, otherwise, from a totalcross.io.Stream
   // for fetching data
   TCObject inputStreamObj, bufObj, pixelsObj;
   Method readBytesMethod;
   TValue params[4];
   // the first 4 bytes
   char *first4;
   int32 lastPass;
   int32 width,height;
   int32 bytesPerRow;
   png_bytep upixels;
   bool quit;
   Context currentContext;
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
   Heap h = (Heap)png_ptr->mem_ptr;
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
// imageObj+tcz+first4, if reading from a tcz; imageObj+inputStream+bufObj+bufCount, if reading from a totalcross.io.Stream
void pngLoad(Context currentContext, TCObject imageObj, TCObject inputStreamObj, TCObject bufObj, TCZFile tcz, char* first4)
{
   Heap heap;
   int32 count;
   uint8 buffer[512];
   int32 transp = -1;
   bool isAlpha = false;

   UserData userData;
   png_structp png_ptr;
   png_infop info_ptr;

   xmemzero(&png_ptr, sizeof(png_ptr));
   xmemzero(&info_ptr, sizeof(info_ptr));
   xmemzero(&userData, sizeof(userData));

   heap = heapCreate();
   if (!heap)
      return; // throwImageException("Out of memory");

   userData.currentContext = currentContext;
   userData.heap = heap;
   if (tcz != null)
   {
      userData.tcz = tcz;
      tcz->tempHeap = heap;
   }
   else
   {
      userData.inputStreamObj = inputStreamObj;  // JPEG stream
      userData.bufObj = bufObj;    // a byte array for readBytes()
   }
   userData.first4 = first4;
   userData.imageObj = imageObj;

   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      if (tcz != null)
         tczClose(tcz);
      return; // throwImageException(...);
   }
   /* Start decompressor */
   /* Create and initialize the png_struct. */
   png_ptr = png_create_read_struct_2(PNG_LIBPNG_VER_STRING, heap, error_callback, null, heap, usermalloc, userfree);
   if (png_ptr == NULL)
      HEAP_ERROR(heap, 999);
   info_ptr = png_create_info_struct(png_ptr);

   if (tcz == null)
   {
      Method readBytesMethod = getMethod(OBJ_CLASS(userData.inputStreamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
      if (readBytesMethod == null)
         HEAP_ERROR(heap, 999);
      userData.readBytesMethod = readBytesMethod;
      userData.params[0].asObj = userData.inputStreamObj;
      userData.params[1].asObj = userData.bufObj;
   }

   png_set_progressive_read_fn(png_ptr,&userData,info_callback,row_callback,null);

   /* Create decompressor output buffer. */
   while (!userData.quit && (count=pngRead(buffer, sizeof(buffer), &userData)) > 0)
      png_process_data(png_ptr, info_ptr, buffer, count);

   // guich@tc100: check if a comment came with the png
   if (info_ptr->text && strEq("Comment",info_ptr->text->key))
   {
      Image_comment(imageObj) = createStringObjectFromCharP(currentContext, info_ptr->text->text, (int)info_ptr->text->text_length);
      setObjectLock(Image_comment(imageObj), UNLOCKED);
   }

   // guich@tc100: set the transparent color
   if (png_ptr->color_type == 6)
      isAlpha = true;
   else
   if (png_ptr->num_trans > 0)
   {             
      if (png_ptr->color_type == PNG_COLOR_TYPE_PALETTE) // palettized?
      {
         int32 i;
         if (png_ptr->num_trans == 256 && png_ptr->color_type == 3)
            isAlpha = true;
         for (i = png_ptr->num_trans; --i >= 0;) // guich@tc120_60: must find the entry that has 0 in trans array
            if (png_ptr->trans_alpha[i] == 0)
            {
               png_color c = png_ptr->palette[i];
               transp = (c.red << 16) | (c.green << 8) | c.blue;
               isAlpha = false;
               break;
            }
      }
      else
         transp = (info_ptr->trans_color.red << 16) | (info_ptr->trans_color.green << 8) | info_ptr->trans_color.blue;
   }

   // Finish decompression and release memory. Do it in this order because output module
   // has allocated memory of lifespan JPOOL_IMAGE; it needs to finish before releasing memory.
   if (userData.upixels) png_free(png_ptr, userData.upixels);
   png_destroy_read_struct(&png_ptr, &info_ptr, NULL);

   Image_width(imageObj) = userData.width;
   Image_height(imageObj) = userData.height;
   if (tcz != null)
      tczClose(tcz);
   heapDestroy(heap);

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
   png_uint_32 width;
   png_uint_32 height;
   int bit_depth;
   int color_type;
   int interlace_type;
   int compression_type;
   int filter_method;
   UserData * userData = (UserData *)png_get_progressive_ptr(png_ptr);

   png_get_IHDR(png_ptr,info_ptr,&width,&height,&bit_depth,&color_type,&interlace_type,&compression_type,&filter_method);
   
   /*
   | set up transformation params:
   | expand images of all color-type and bit-depth to 3x8 bit RGB images
   | let the library process things like alpha, transparency, background
   */
   if (bit_depth == 16)
      png_set_strip_16(png_ptr);

   if (color_type == PNG_COLOR_TYPE_RGB_ALPHA || color_type == PNG_COLOR_TYPE_PALETTE || bit_depth < 8 || png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS))
      png_set_expand(png_ptr);
   if ((color_type == PNG_COLOR_TYPE_GRAY) || (color_type == PNG_COLOR_TYPE_GRAY_ALPHA))
      png_set_gray_to_rgb(png_ptr);

   userData->lastPass = png_set_interlace_handling(png_ptr) - 1;

   // get updated info, and start the image
   png_read_update_info(png_ptr, info_ptr);
   if (png_ptr->color_type != PNG_COLOR_TYPE_PALETTE && png_ptr->num_trans != 0) // we don't support transparent palettes
      png_set_strip_alpha(png_ptr);
   userData->width = (int32)width;
   userData->height = (int32)height;
   userData->bytesPerRow = (int32)png_get_rowbytes(png_ptr, info_ptr);
   userData->upixels = png_malloc(png_ptr, userData->bytesPerRow);
   if (width > 65535 || height > 65535)  // bad width/height?
      HEAP_ERROR(userData->heap, 998);

   Image_pixels(userData->imageObj) = userData->pixelsObj = createIntArray(userData->currentContext, (int32)(width*height));
   if (!userData->pixelsObj)
      HEAP_ERROR(userData->heap, 997);
   setObjectLock(Image_pixels(userData->imageObj), UNLOCKED);
   userData->pixels = (Pixel*)ARRAYOBJ_START(userData->pixelsObj);
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
static void row_callback(png_structp png_ptr, png_bytep new_row, png_uint_32 row_num, int pass)
{
   UserData * userData = (UserData *)png_get_progressive_ptr(png_ptr);
   png_bytep old_row = userData->upixels;
   png_progressive_combine_row(png_ptr, old_row, new_row);

   if (pass == userData->lastPass)
   {
      uint8* buffer = old_row;
      int32 x;
      if (png_ptr->channels == 4 || (png_ptr->color_type == PNG_COLOR_TYPE_PALETTE && png_ptr->num_trans > 6))
         for (x = 0; x < userData->width; x++, buffer += 4)
            *userData->pixels++ = makePixelA((uint8)buffer[3], (uint8)buffer[0], (uint8)buffer[1], (uint8)buffer[2]);
      else
         for (x = 0; x < userData->width; x++, buffer += 3)
            *userData->pixels++ = makePixel((uint8)buffer[0], (uint8)buffer[1], (uint8)buffer[2]);
      userData->quit = (int32)row_num == (userData->height-1);
   }
}

static void error_callback(png_structp png_ptr, png_const_charp msg)
{
   Heap h = (Heap)png_ptr->error_ptr;
   HEAP_ERROR(h, 996);
   UNUSED(msg)
}
