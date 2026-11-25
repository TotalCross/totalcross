// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <stddef.h>
#include <stdio.h>

#include <png.h>
#include <zlib.h>

int main(void)
{
   printf("sizeof_void_pointer=%zu\n", sizeof(void *));
   printf("sizeof_int=%zu\n", sizeof(int));
   printf("sizeof_long=%zu\n", sizeof(long));
   printf("sizeof_size_t=%zu\n", sizeof(size_t));
   printf("sizeof_png_size_t=%zu\n", sizeof(png_size_t));
   printf("sizeof_z_stream=%zu\n", sizeof(z_stream));
   printf("PNG_LIBPNG_VER_STRING=%s\n", PNG_LIBPNG_VER_STRING);
   printf("ZLIB_VERSION=%s\n", ZLIB_VERSION);
   printf("ZLIB_VERNUM=0x%04x\n", ZLIB_VERNUM);
   return 0;
}
