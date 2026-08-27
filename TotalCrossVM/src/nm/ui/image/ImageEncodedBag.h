// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef IMAGE_ENCODED_BAG_H
#define IMAGE_ENCODED_BAG_H

#include <stdbool.h>

#include "xtypes.h"

typedef enum {
   IMAGE_ENCODED_PNG = 1,
   IMAGE_ENCODED_JPEG = 2
} ImageEncodedFormat;

typedef struct ImageEncodedBag {
   uint8* bytes;
   int32 length;
} ImageEncodedBag;

typedef struct ImageEncodedInspection {
   ImageEncodedFormat format;
   int32 width;
   int32 height;
} ImageEncodedInspection;

ImageEncodedBag* imageEncodedBagCreate(const uint8* bytes, int32 length);
ImageEncodedBag* imageEncodedBagCreateEmpty(int32 length);
void imageEncodedBagRelease(ImageEncodedBag** bag);
bool imageEncodedBagInspect(const ImageEncodedBag* bag, ImageEncodedInspection* inspection);

#endif
