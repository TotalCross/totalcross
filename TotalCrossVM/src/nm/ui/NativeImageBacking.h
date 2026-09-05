// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef NATIVE_IMAGE_BACKING_H
#define NATIVE_IMAGE_BACKING_H

#include "tcvm.h"

bool imageInstallNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height);
bool imageReplaceNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height);

#endif
