// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <png.h>

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static uint32_t fnv1a(const unsigned char *bytes, size_t length)
{
   uint32_t hash = 2166136261U;
   size_t i;
   for (i = 0; i < length; i++)
   {
      hash ^= bytes[i];
      hash *= 16777619U;
   }
   return hash;
}

int main(int argc, char **argv)
{
   FILE *input;
   long file_size;
   unsigned char *bytes;
   unsigned char *pixels;
   png_image image;
   size_t pixel_size;

   if (argc != 2)
   {
      fprintf(stderr, "usage: standalone-png-decode file.png\n");
      return 2;
   }
   input = fopen(argv[1], "rb");
   if (input == NULL || fseek(input, 0, SEEK_END) != 0 || (file_size = ftell(input)) <= 0
       || fseek(input, 0, SEEK_SET) != 0)
   {
      fprintf(stderr, "input-open-or-size-failed\n");
      if (input != NULL)
         fclose(input);
      return 2;
   }
   bytes = (unsigned char *)malloc((size_t)file_size);
   if (bytes == NULL || fread(bytes, 1, (size_t)file_size, input) != (size_t)file_size)
   {
      fprintf(stderr, "input-read-failed\n");
      free(bytes);
      fclose(input);
      return 2;
   }
   fclose(input);

   fprintf(stderr, "standalone-feed length=%ld fnv1a=%08x\n", file_size,
      (unsigned int)fnv1a(bytes, (size_t)file_size));
   fflush(stderr);
   memset(&image, 0, sizeof(image));
   image.version = PNG_IMAGE_VERSION;
   if (!png_image_begin_read_from_memory(&image, bytes, (size_t)file_size))
   {
      fprintf(stderr, "standalone-begin=failed error=%s\n", image.message);
      fflush(stderr);
      png_image_free(&image);
      free(bytes);
      return 1;
   }

   image.format = PNG_FORMAT_RGBA;
   pixel_size = PNG_IMAGE_SIZE(image);
   pixels = (unsigned char *)malloc(pixel_size);
   if (pixels == NULL)
   {
      fprintf(stderr, "standalone-pixels=allocation-failed size=%zu\n", pixel_size);
      png_image_free(&image);
      free(bytes);
      return 1;
   }
   fprintf(stderr, "standalone-decode=starting width=%u height=%u bytes=%zu\n",
      image.width, image.height, pixel_size);
   fflush(stderr);
   if (!png_image_finish_read(&image, NULL, pixels, 0, NULL))
   {
      fprintf(stderr, "standalone-finish=failed error=%s\n", image.message);
      fflush(stderr);
      png_image_free(&image);
      free(pixels);
      free(bytes);
      return 1;
   }

   fprintf(stderr, "standalone-finish=passed width=%u height=%u\n", image.width, image.height);
   fflush(stderr);
   png_image_free(&image);
   free(pixels);
   free(bytes);
   return 0;
}
