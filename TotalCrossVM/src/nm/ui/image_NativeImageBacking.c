// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "instancefields.h"
#include "NativeImageBacking.h"

#if TC_RENDERER_SKIA
#include "skia/skia.h"
#endif

bool imageInstallNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height)
{
#if TC_RENDERER_SKIA
   TCObject backing;
   if (!imageObj || handle == 0 || width <= 0 || height <= 0) {
      skia_image_backing_release(handle);
      throwException(context, ImageException, "Could not create native image backing");
      return false;
   }
   backing = createObject(context, "totalcross.ui.image.NativeImageBacking");
   if (!backing) {
      skia_image_backing_release(handle);
      throwException(context, OutOfMemoryError, null);
      return false;
   }
   NativeImageBacking_nativeHandle(backing) = handle;
   NativeImageBacking_width(backing) = width;
   NativeImageBacking_height(backing) = height;
   setObjectLock(backing, UNLOCKED);
   Image_backing(imageObj) = backing;
   Image_pixels(imageObj) = null;
   Image_pixelsOfAllFrames(imageObj) = null;
   return true;
#else
   UNUSED(context)
   UNUSED(imageObj)
   UNUSED(width)
   UNUSED(height)
   UNUSED(handle)
   return false;
#endif
}

TC_API void tuiNIB_createEmptyNative_ii(NMParams p) // totalcross/ui/image/NativeImageBacking private static long createEmptyNative(int width, int height);
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_create_empty(p->i32[0], p->i32[1]);
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_isAvailableNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static boolean isAvailableNative();
{
#if TC_RENDERER_SKIA
   p->retI = 1;
#else
   p->retI = 0;
#endif
}

TC_API void tuiNIB_createFromArgbPixels_Iii(NMParams p) // totalcross/ui/image/NativeImageBacking private static long createFromArgbPixelsNative(int[] pixels, int width, int height);
{
#if TC_RENDERER_SKIA
   TCObject pixels = p->obj[0];
   int32 width = p->i32[0];
   int32 height = p->i32[1];
   if (!pixels || width <= 0 || height <= 0 || (int64)width * height > ARRAYOBJ_LEN(pixels)) {
      p->retL = 0;
      return;
   }
   p->retL = skia_image_backing_create_from_argb_pixels(ARRAYOBJ_START(pixels), width, height);
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_snapshotNative(NMParams p) // totalcross/ui/image/NativeImageBacking private long snapshotNative();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_snapshot(NativeImageBacking_nativeHandle(p->obj[0]));
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_makeMutableNative(NMParams p) // totalcross/ui/image/NativeImageBacking private boolean makeMutableNative();
{
#if TC_RENDERER_SKIA
   p->retI = skia_image_backing_make_mutable(NativeImageBacking_nativeHandle(p->obj[0]));
#else
   p->retI = 0;
#endif
}

TC_API void tuiNIB_scaleNative_iib(NMParams p) // totalcross/ui/image/NativeImageBacking private long scaleNative(int width, int height, boolean smooth);
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_scale(NativeImageBacking_nativeHandle(p->obj[0]),
      p->i32[0], p->i32[1], (bool)p->i32[2]);
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_readPixelsNative_Iiiiii(NMParams p) // totalcross/ui/image/NativeImageBacking private boolean readPixelsNative(int[] output, int offset, int x, int y, int width, int height);
{
#if TC_RENDERER_SKIA
   TCObject output = p->obj[1];
   int32 offset = p->i32[0];
   int32 x = p->i32[1];
   int32 y = p->i32[2];
   int32 width = p->i32[3];
   int32 height = p->i32[4];
   if (!output || offset < 0 || width < 0 || height < 0 ||
       (int64)offset + (int64)width * height > ARRAYOBJ_LEN(output))
   {
      p->retI = 0;
      return;
   }
   p->retI = skia_image_backing_read_pixels(NativeImageBacking_nativeHandle(p->obj[0]),
      (Pixel*)ARRAYOBJ_START(output) + offset, x, y, width, height);
#else
   p->retI = 0;
#endif
}

TC_API void tuiNIB_releaseNativeHandle_l(NMParams p) // totalcross/ui/image/NativeImageBacking private static void releaseNativeHandle(long nativeHandle);
{
#if TC_RENDERER_SKIA
   skia_image_backing_release(p->i64[0]);
#endif
}
