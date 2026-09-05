// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "instancefields.h"
#include "NativeImageBacking.h"
#include "ImageTestAccounting_c.h"

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

bool imageReplaceNativeBacking(Context context, TCObject imageObj, int64 handle,
                               int32 width, int32 height)
{
#if TC_RENDERER_SKIA
   TCObject backing;
   if (!imageObj || handle == 0 || width <= 0 || height <= 0) {
      skia_image_backing_release(handle);
      throwException(context, ImageException, "Could not replace native image backing");
      return false;
   }
   backing = Image_backing(imageObj);
   if (backing != null && strEq(OBJ_CLASS(backing)->name,
         "totalcross.ui.image.NativeImageBacking")) {
      int64 oldHandle = NativeImageBacking_nativeHandle(backing);
      if (oldHandle != handle) {
         skia_image_backing_release(oldHandle);
      }
      NativeImageBacking_nativeHandle(backing) = handle;
      NativeImageBacking_width(backing) = width;
      NativeImageBacking_height(backing) = height;
      return true;
   }
   return imageInstallNativeBacking(context, imageObj, handle, width, height);
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

TC_API void tuiNIB_resetAccountingTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static void resetAccountingTestNative();
{
#if TC_RENDERER_SKIA
   skia_image_backing_reset_accounting_for_test();
#endif
   UNUSED(p);
}

TC_API void tuiNIB_clearAccountingTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static void clearAccountingTestNative();
{
#if TC_RENDERER_SKIA
   skia_image_backing_clear_accounting_counters_for_test();
#endif
   UNUSED(p);
}

TC_API void tuiNIB_backingCreatedTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingCreatedTestNative();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_records_created_for_test();
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_backingReleasedTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingReleasedTestNative();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_records_released_for_test();
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_backingLiveTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingLiveTestNative();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_records_live_for_test();
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_backingPeakLiveTestNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingPeakLiveTestNative();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_records_peak_live_for_test();
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_backingBytesLiveTest(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingBytesLiveTest();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_bytes_live_for_test();
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_backingPeakBytesTest(NMParams p) // totalcross/ui/image/NativeImageBacking private static long backingPeakBytesTest();
{
#if TC_RENDERER_SKIA
   p->retL = skia_image_backing_bytes_peak_live_for_test();
#else
   p->retL = 0;
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
   int64_t snapshotHandle = 0;
   int status = skia_image_backing_snapshot_status(NativeImageBacking_nativeHandle(p->obj[0]),
      &snapshotHandle);
   p->retL = status == SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE ? -1 : snapshotHandle;
#else
   p->retL = 0;
#endif
}

TC_API void tuiNIB_failNextSnapshotNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static void failNextSnapshotNative();
{
#if TC_RENDERER_SKIA
   skia_image_backing_fail_next_snapshot_for_test();
#endif
   UNUSED(p);
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

TC_API void tuiNIB_materializeGeometryNative(NMParams p) // totalcross/ui/image/NativeImageBacking private static long materializeGeometryNative(totalcross.ui.image.ImageDrawPlan plan);
{
#if TC_RENDERER_SKIA
   TCObject plan = p->obj[0];
   TCObject root;
   TCObject operations;
   TCObject parameters;
   TCObject dimensions;
   TCObject presentation;
   SkiaImageDrawPlanData data;
   if (!plan || !(root = ImageDrawPlan_root(plan)) ||
       !Image_backing(root) || !strEq(OBJ_CLASS(Image_backing(root))->name,
          "totalcross.ui.image.NativeImageBacking") ||
       !(operations = ImageDrawPlan_operations(plan)) ||
       !(parameters = ImageDrawPlan_parameters(plan)) ||
       !(dimensions = ImageDrawPlan_dimensions(plan))) {
      p->retL = 0;
      return;
   }
   data.rootHandle = NativeImageBacking_nativeHandle(Image_backing(root));
   data.rootWidth = ImageDrawPlan_rootWidth(plan);
   data.rootHeight = ImageDrawPlan_rootHeight(plan);
   data.rootLogicalWidth = ImageDrawPlan_rootLogicalWidth(plan);
   data.rootLogicalHeight = ImageDrawPlan_rootLogicalHeight(plan);
   data.rootFrameCount = ImageDrawPlan_rootFrameCount(plan);
   data.rootWidthOfAllFrames = ImageDrawPlan_rootWidthOfAllFrames(plan);
   data.rootContentScale = ImageDrawPlan_rootContentScale(plan);
   data.operations = (const int32*)ARRAYOBJ_START(operations);
   data.parameters = (const int32*)ARRAYOBJ_START(parameters);
   data.dimensions = (const int32*)ARRAYOBJ_START(dimensions);
   data.sourceDecodeGeneration = ImageDrawPlan_sourceDecodeGeneration(plan);
   data.operationCount = ARRAYOBJ_LEN(operations);
   data.outputWidth = ImageDrawPlan_outputWidth(plan);
   data.outputHeight = ImageDrawPlan_outputHeight(plan);
   data.outputFrameCount = ImageDrawPlan_outputFrameCount(plan);
   data.outputWidthOfAllFrames = ImageDrawPlan_outputWidthOfAllFrames(plan);
   data.currentFrame = ImageDrawPlan_currentFrame(plan);
   data.alphaMask = ImageDrawPlan_alphaMask(plan);
   data.transparentColor = ImageDrawPlan_transparentColor(plan);
   data.materializeAlphaMask = ImageDrawPlan_materializeAlphaMask(plan);
   data.outputAlphaMask = ImageDrawPlan_outputAlphaMask(plan);
   data.destinationScale = ImageDrawPlan_destinationScale(plan);
   data.outputContentScale = ImageDrawPlan_outputContentScale(plan);
   data.hwScaleW = ImageDrawPlan_hwScaleW(plan);
   data.hwScaleH = ImageDrawPlan_hwScaleH(plan);
   data.rootHwScaleW = ImageDrawPlan_rootHwScaleW(plan);
   data.rootHwScaleH = ImageDrawPlan_rootHwScaleH(plan);
   presentation = ImageDrawPlan_presentation(plan);
   if (!presentation) {
      presentation = root;
   }
   if (presentation) {
      const int32 presentationAlpha = Image_alphaMask(presentation);
      data.currentFrame = Image_currentFrame(presentation);
      data.transparentColor = Image_transparentColor(presentation);
      data.outputAlphaMask = presentationAlpha;
      data.alphaMask = data.materializeAlphaMask == 255
         ? presentationAlpha
         : (data.materializeAlphaMask * presentationAlpha + 127) / 255;
      data.hwScaleW = Image_hwScaleW(presentation);
      data.hwScaleH = Image_hwScaleH(presentation);
   }
   if (data.operationCount <= 0 || data.operationCount * 4 > ARRAYOBJ_LEN(parameters)
       || data.operationCount * 2 > ARRAYOBJ_LEN(dimensions)) {
      p->retL = 0;
      return;
   }
   p->retL = skia_image_backing_materialize_geometry(&data);
   if (p->retL != 0) {
      imageRecordTestCounter("nativeGeometryMaterializationCountForTest");
   }
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

TC_API void tuiNIB_readRgbaRowNative_Bii(NMParams p) // totalcross/ui/image/NativeImageBacking private boolean readRgbaRowNative(byte[] output, int y, int width);
{
#if TC_RENDERER_SKIA
   TCObject output = p->obj[1];
   int32 y = p->i32[0];
   int32 width = p->i32[1];
   if (!output || y < 0 || width <= 0 || (int64)width * 4 > ARRAYOBJ_LEN(output))
   {
      p->retI = 0;
      return;
   }
   p->retI = skia_image_backing_read_rgba_row(NativeImageBacking_nativeHandle(p->obj[0]),
      ARRAYOBJ_START(output), y, width);
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
