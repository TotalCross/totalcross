// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "ImageEncodedBag.h"

#include <setjmp.h>
#include <stddef.h>
#include <stdio.h>

#include "jpeglib.h"
#include "util/mem.h"

static uint32 readBE32(const uint8* p) {
   return ((uint32)p[0] << 24) | ((uint32)p[1] << 16) | ((uint32)p[2] << 8) | p[3];
}

static uint16 readBE16(const uint8* p) {
   return (uint16)(((uint16)p[0] << 8) | p[1]);
}

static uint32 crc32Bytes(const uint8* p, int32 length) {
   uint32 crc = 0xffffffffU;
   int32 i;
   for (i = 0; i < length; i++) {
      uint32 value = crc ^ p[i];
      int32 bit;
      for (bit = 0; bit < 8; bit++) value = (value >> 1) ^ (0xedb88320U & -(value & 1));
      crc = value;
   }
   return crc ^ 0xffffffffU;
}

static bool typeIs(const uint8* p, const char* type) {
   return p[0] == (uint8)type[0] && p[1] == (uint8)type[1] && p[2] == (uint8)type[2] && p[3] == (uint8)type[3];
}

static bool pngInspect(const uint8* b, int32 n, ImageEncodedInspection* out) {
   static const uint8 signature[] = {0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};
   int32 p = 8, width = 0, height = 0;
   bool ihdr = false, idat = false, idatEnded = false, iend = false, palette = false;
   int32 color = -1;
   int32 i;
   for (i = 0; i < 8; i++) if (b[i] != signature[i]) return false;
   while (p + 12 <= n) {
      uint32 length = readBE32(b + p);
      int32 dataLength;
      const uint8* type;
      const uint8* data;
      if (length > 0x7fffffffU || length > (uint32)(n - p - 12)) return false;
      dataLength = (int32)length;
      type = b + p + 4;
      data = b + p + 8;
      if (!ihdr && !typeIs(type, "IHDR")) return false;
      if (crc32Bytes(type, dataLength + 4) != readBE32(data + dataLength)) return false;
      if (typeIs(type, "IHDR")) {
         int32 depth;
         if (ihdr || dataLength != 13) return false;
         if (readBE32(data) == 0 || readBE32(data + 4) == 0 || readBE32(data) > 0x7fffffffU
               || readBE32(data + 4) > 0x7fffffffU) return false;
         width = (int32)readBE32(data);
         height = (int32)readBE32(data + 4);
         depth = data[8];
         color = data[9];
         if (!((color == 0 && (depth == 1 || depth == 2 || depth == 4 || depth == 8 || depth == 16))
               || (color == 2 && (depth == 8 || depth == 16))
               || (color == 3 && (depth == 1 || depth == 2 || depth == 4 || depth == 8))
               || (color == 6 && (depth == 8 || depth == 16)))
               || data[10] != 0 || data[11] != 0 || data[12] > 1) return false;
         ihdr = true;
      } else if (typeIs(type, "PLTE")) {
         if (palette || idat || dataLength == 0 || dataLength % 3 != 0 || dataLength > 768) return false;
         palette = true;
      } else if (typeIs(type, "IDAT")) {
         if (!ihdr || idatEnded || (color == 3 && !palette)) return false;
         idat = true;
      } else if (typeIs(type, "IEND")) {
         if (dataLength != 0 || !ihdr || !idat) return false;
         iend = true;
         break;
      } else if (idat && !(type[0] & 0x20)) {
         return false;
      }
      if (idat && !typeIs(type, "IDAT")) idatEnded = true;
      p += dataLength + 12;
   }
   if (!ihdr || !idat || !iend) return false;
   out->format = IMAGE_ENCODED_PNG;
   out->width = width;
   out->height = height;
   return true;
}

static bool isSof(uint8 marker) {
   return marker >= 0xc0 && marker <= 0xcf && marker != 0xc4 && marker != 0xc8 && marker != 0xcc;
}

static int32 skipJpegScan(const uint8* b, int32 p, int32 n) {
   while (p < n) {
      if (b[p++] != 0xff) continue;
      while (p < n && b[p] == 0xff) p++;
      if (p >= n) return n;
      if (b[p] == 0) { p++; continue; }
      if (b[p] >= 0xd0 && b[p] <= 0xd7) { p++; continue; }
      return p - 1;
   }
   return n;
}

typedef struct ImageJpegError {
   struct jpeg_error_mgr manager;
   jmp_buf jump;
} ImageJpegError;

static void jpegErrorExit(j_common_ptr cinfo) {
   ImageJpegError* error = (ImageJpegError*)cinfo->err;
   longjmp(error->jump, 1);
}

static bool jpegHeaderIsValid(const uint8* b, int32 n) {
   struct jpeg_decompress_struct decoder;
   ImageJpegError error;
   decoder.err = jpeg_std_error(&error.manager);
   error.manager.error_exit = jpegErrorExit;
   if (setjmp(error.jump)) {
      jpeg_destroy_decompress(&decoder);
      return false;
   }
   jpeg_create_decompress(&decoder);
   jpeg_mem_src(&decoder, (unsigned char*)b, (unsigned long)n);
   if (jpeg_read_header(&decoder, TRUE) != JPEG_HEADER_OK) {
      jpeg_destroy_decompress(&decoder);
      return false;
   }
   jpeg_destroy_decompress(&decoder);
   return true;
}

static bool jpegInspect(const uint8* b, int32 n, ImageEncodedInspection* out) {
   int32 p = 2, width = 0, height = 0, scans = 0;
   bool sof = false, eoi = false;
   if (n < 2 || b[0] != 0xff || b[1] != 0xd8) return false;
   while (p < n) {
      uint8 marker;
      int32 length;
      if (b[p++] != 0xff) return false;
      while (p < n && b[p] == 0xff) p++;
      if (p >= n) return false;
      marker = b[p++];
      if (marker == 0xd9) { eoi = true; break; }
      if (marker == 0xd8 || (marker >= 0xd0 && marker <= 0xd7) || marker == 0) return false;
      if (p + 2 > n) return false;
      length = readBE16(b + p);
      if (length < 2 || length > n - p) return false;
      if (isSof(marker)) {
         if (sof || length < 8 || readBE16(b + p + 3) == 0 || readBE16(b + p + 5) == 0) return false;
         height = readBE16(b + p + 3);
         width = readBE16(b + p + 5);
         sof = true;
      }
      p += length;
      if (marker == 0xda) {
         if (!sof) return false;
         scans++;
         p = skipJpegScan(b, p, n);
         if (p >= n || b[p] != 0xff) return false;
      }
   }
   if (!sof || scans == 0 || !eoi || !jpegHeaderIsValid(b, n)) return false;
   out->format = IMAGE_ENCODED_JPEG;
   out->width = width;
   out->height = height;
   return true;
}

ImageEncodedBag* imageEncodedBagCreateEmpty(int32 length) {
   ImageEncodedBag* bag;
   if (length <= 0) return null;
   bag = (ImageEncodedBag*)xmalloc(sizeof(ImageEncodedBag));
   if (!bag) return null;
   bag->bytes = (uint8*)xmalloc((uint32)length);
   if (!bag->bytes) {
      xfree(bag);
      return null;
   }
   bag->length = length;
   return bag;
}

ImageEncodedBag* imageEncodedBagCreate(const uint8* bytes, int32 length) {
   ImageEncodedBag* bag;
   if (!bytes || length <= 0) return null;
   bag = imageEncodedBagCreateEmpty(length);
   if (bag) xmemmove(bag->bytes, bytes, length);
   return bag;
}

void imageEncodedBagRelease(ImageEncodedBag** bag) {
   if (!bag || !*bag) return;
   xfree((*bag)->bytes);
   xfree(*bag);
}

bool imageEncodedBagInspect(const ImageEncodedBag* bag, ImageEncodedInspection* inspection) {
   if (!bag || !inspection || !bag->bytes || bag->length < 2) return false;
   if (bag->length >= 8 && pngInspect(bag->bytes, bag->length, inspection)) return true;
   return jpegInspect(bag->bytes, bag->length, inspection);
}
