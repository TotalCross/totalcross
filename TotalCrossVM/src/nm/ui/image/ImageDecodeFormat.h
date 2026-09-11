// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef IMAGE_DECODE_FORMAT_H
#define IMAGE_DECODE_FORMAT_H

#include "tcvm.h"
#include "ui/ImageBackingFormat.h"

/* Chooses a compact format from structural source metadata and the decode mask. */
ImageBackingFormat imageSelectDecodeStorageFormat(TCObject imageObj, bool sourceIsGray,
      bool sourceHasAlpha);

#endif
