// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_BACKING_H
#define SKIA_IMAGE_BACKING_H

#include "skia.h"

#ifdef __cplusplus
class SkCanvas;

/** Returns the mutable canvas owned by an opaque backing, or nullptr. */
SkCanvas* skia_image_backing_canvas(int64_t handle);
SkCanvas* skia_image_backing_canvas_for_surface_id(int32 surfaceId);
#endif

#endif
