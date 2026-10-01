// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef NATIVE_IMAGE_BACKING_H
#define NATIVE_IMAGE_BACKING_H

#include "tcvm.h"

enum
{
   IMAGE_BACKING_OPACITY_UNKNOWN = 0,
   IMAGE_BACKING_OPACITY_OPAQUE = 1,
   IMAGE_BACKING_OPACITY_HAS_ALPHA = 2
};

bool imageInstallNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height);
bool imageReplaceNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height);
void imageBackingRecordMutation(TCObject imageObj, int32 opacityState);
void imageBackingSetOpacity(TCObject imageObj, int32 opacityState);
bool imageCompactStorageEnabled(Context context);

#endif
