// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only


#if defined ANDROID || defined darwin || TC_WINDOWING_SDL
#include <tcvm/tcclass.h>
#endif
#include "tcvm.h"
#include "ImagePrimitives_c.h"
#include "io/File.h"
#include "JpegLoader.h"
#include "util/utils.h"
#include "ui/image/ImageEncodedBag.h"
#include "ui/image/ImageDecodeFormat.h"
#include <string.h>
#if POSIX
   #include <sys/mman.h>
   #include <errno.h>
#endif

#if defined darwin
#include "darwin/image_Image_c.h"
#endif

#if defined(WIN32) && !defined(WINCE)
#include <psapi.h>
#elif defined(darwin)
#include <mach/mach.h>
#include <sys/resource.h>
#elif defined(POSIX)
#include <stdio.h>
#include <string.h>
#endif

#if TC_RENDERER_SKIA
#include "ui/skia/skia.h"
#endif

ImageTestAccountingState imageTestAccountingState;

#define IMAGE_OPT_DECODE_ZERO_COPY (1 << 0)
#define IMAGE_OPT_RASTER_OPACITY_METADATA (1 << 1)
#define IMAGE_OPT_RASTER_OPAQUE_WRITE_PIXELS (1 << 2)

static int32 imageDecodeOptimizationMask(void)
{
   return imageOptimizationMaskForDecodePtr != null
      ? *imageOptimizationMaskForDecodePtr
      : 0;
}

static bool imageDecodeZeroCopyEnabled(void)
{
   return (imageDecodeOptimizationMask() & IMAGE_OPT_DECODE_ZERO_COPY) != 0;
}

static bool imageDecodeOpacityMetadataEnabled(void)
{
   return (imageDecodeOptimizationMask() & IMAGE_OPT_RASTER_OPACITY_METADATA) != 0;
}

ImageBackingFormat imageSelectDecodeStorageFormat(TCObject imageObj, bool sourceIsGray,
      bool sourceHasAlpha)
{
   (void)imageObj;
   return imageSelectDecodeStorageFormatWithMask(imageDecodeOptimizationMask(), sourceIsGray,
      sourceHasAlpha);
}

ImageBackingFormat imageSelectDecodeStorageFormatWithMask(int32 mask, bool sourceIsGray,
      bool sourceHasAlpha)
{
   if (sourceIsGray && !sourceHasAlpha && (mask & (1 << 6)) != 0)
      return IMAGE_BACKING_FORMAT_GRAY8;
   if (!sourceHasAlpha && (mask & (1 << 5)) != 0)
      return IMAGE_BACKING_FORMAT_RGB565;
   if (sourceHasAlpha && (mask & (1 << 7)) != 0)
      return IMAGE_BACKING_FORMAT_ARGB4444;
   return IMAGE_BACKING_FORMAT_RGBA8888;
}

void imageRecordOpacityFallbackScanForTest(int32 pixels)
{
   imageRecordTestCounter("opacityFallbackScansForTest");
   imageAddTestCounter("opacityFallbackPixelsForTest", pixels);
}

ImageDecodeStatus pngLoad(Context currentContext, TCObject imageInstance, TCObject inputStreamObj, TCObject bufObj,
      TCZFile tcz, char* first4, const uint8* mapped, int32 mappedLength, bool zeroCopy,
      bool opacityMetadata);

static bool failNextImageAllocationForTest;
static bool failNextFinalBufferAllocationForTest;

TC_API void tuiI_setDiagnosticAccountingTest(NMParams p) // totalcross/ui/image/Image native private static void setDiagnosticAccountingTestNative(boolean enabled);
{
   imageSetTestAccounting(p->currentContext, p->i32[0]);
#if TC_RENDERER_SKIA
   skia_image_backing_set_accounting_for_test(p->i32[0]);
#endif
}

TC_API void tuiI_nativeOptimizationMaskObser(NMParams p) // totalcross/ui/image/Image native private static int nativeOptimizationMaskObservedForTestNative(totalcross.ui.image.Image image, boolean draw);
{
   int32* featureMask = p->i32[0]
      ? imageOptimizationMaskForDrawPtr
      : imageOptimizationMaskForDecodePtr;
   p->retI = featureMask == null ? -1 : *featureMask;
}

#if defined(POSIX)
static int64 imageProcessStatusBytes(const char* key)
{
   FILE* status = fopen("/proc/self/status", "r");
   char line[128];
   if (status == null)
      return -1;
   while (fgets(line, sizeof(line), status) != null)
   {
      long long kilobytes;
      if (strncmp(line, key, strlen(key)) == 0
         && sscanf(line + strlen(key), "%lld", &kilobytes) == 1)
      {
         fclose(status);
         return (int64)(kilobytes * 1024);
      }
   }
   fclose(status);
   return -1;
}
#endif

static int64 imageBenchmarkNativeMetric(int32 kind)
{
   if (kind == 4)
   {
      uint16 marker = 1;
      return ((uint8*)&marker)[0] == 1 ? 1 : 2;
   }
   if (kind == 5)
   {
#if TC_RENDERER_SKIA
      return 1;
#else
      return 0;
#endif
   }
#if defined(WIN32) && !defined(WINCE)
   {
      PROCESS_MEMORY_COUNTERS_EX counters;
      memset(&counters, 0, sizeof(counters));
      if (!GetProcessMemoryInfo(GetCurrentProcess(),
         (PROCESS_MEMORY_COUNTERS*)&counters, sizeof(counters)))
         return -1;
      if (kind == 0)
         return (int64)counters.WorkingSetSize;
      if (kind == 1)
         return (int64)counters.PeakWorkingSetSize;
      if (kind == 2)
         return (int64)counters.PrivateUsage;
      return -1;
   }
#elif defined(darwin)
   {
      mach_task_basic_info_data_t basic;
      mach_msg_type_number_t basicCount = MACH_TASK_BASIC_INFO_COUNT;
      if (task_info(mach_task_self(), MACH_TASK_BASIC_INFO,
         (task_info_t)&basic, &basicCount) != KERN_SUCCESS)
         return -1;
      if (kind == 0)
         return (int64)basic.resident_size;
      if (kind == 1)
      {
         struct rusage usage;
         return getrusage(RUSAGE_SELF, &usage) == 0 ? (int64)usage.ru_maxrss : -1;
      }
      if (kind == 3)
      {
         task_vm_info_data_t vmInfo;
         mach_msg_type_number_t vmInfoCount = TASK_VM_INFO_COUNT;
         if (task_info(mach_task_self(), TASK_VM_INFO,
            (task_info_t)&vmInfo, &vmInfoCount) == KERN_SUCCESS)
            return (int64)vmInfo.phys_footprint;
      }
      return -1;
   }
#elif defined(POSIX)
   if (kind == 0)
      return imageProcessStatusBytes("VmRSS:");
   if (kind == 1)
      return imageProcessStatusBytes("VmHWM:");
   return -1;
#else
   return -1;
#endif
}

TC_API void tuiI_benchmarkMetricNative_il(NMParams p) // totalcross/ui/image/Image native private static long benchmarkMetricNative(int kind);
{
   if (p->i32[0] >= 6)
   {
#if TC_RENDERER_SKIA
      p->retL = skia_benchmark_native_metric(p->i32[0] - 6);
#else
      p->retL = -1;
#endif
      return;
   }
   p->retL = imageBenchmarkNativeMetric(p->i32[0]);
}

static int32 jpegTargetDecodeDenominatorForTest(int32 sourceWidth, int32 sourceHeight,
   int32 targetWidth, int32 targetHeight)
{
   const int32 denominators[] = { 8, 4, 2, 1 };
   int32 i;
   for (i = 0; i < 4; i++)
   {
      int32 denominator = denominators[i];
      if ((sourceWidth + denominator - 1) / denominator >= targetWidth
         && (sourceHeight + denominator - 1) / denominator >= targetHeight)
         return denominator;
   }
   return 1;
}

int imageDecodeConsumeAllocationFailureForTest(void)
{
   bool fail = failNextImageAllocationForTest;
   failNextImageAllocationForTest = false;
   return fail;
}

int imageDecodeConsumeFinalBufferFailureForTest(void)
{
   bool fail = failNextFinalBufferAllocationForTest;
   failNextFinalBufferAllocationForTest = false;
   return fail;
}

static void throwImageDecodeStatus(Context context, ImageDecodeStatus status)
{
   if (status == IMAGE_DECODE_RESOURCE_FAILURE)
   {
      throwExceptionNamed(context, "totalcross.ui.image.TransientImageMaterializationException", null);
   }
   else if (status == IMAGE_DECODE_CORRUPT)
      throwExceptionNamed(context, "totalcross.ui.image.Image$DeterministicImageDecodeException", null);
}

static bool installEncodedBag(Context context, TCObject source, ImageEncodedBag* bag) {
   ImageEncodedInspection inspection;
   TCObject comment = null;
   if (!imageEncodedBagInspect(bag, &inspection)) {
      imageEncodedBagRelease(&bag);
      throwException(context, ImageException, "Invalid or unsupported encoded image");
      return false;
   }
   if (inspection.comment && inspection.commentLength > 0) {
      comment = createStringObjectFromCharP(context, (CharP)inspection.comment, inspection.commentLength);
      if (!comment) {
         imageEncodedBagRelease(&bag);
         throwException(context, OutOfMemoryError, null);
         return false;
      }
      setObjectLock(comment, UNLOCKED);
   }
   if (EncodedImageSource_nativeBag(source)) {
      ImageEncodedBag* previous = (ImageEncodedBag*)EncodedImageSource_nativeBag(source);
      imageEncodedBagRelease(&previous);
   }
   EncodedImageSource_formatCode(source) = (int32)inspection.format;
   EncodedImageSource_length(source) = bag->length;
   EncodedImageSource_intrinsicWidth(source) = inspection.width;
   EncodedImageSource_intrinsicHeight(source) = inspection.height;
   EncodedImageSource_logicalWidth(source) = inspection.logicalWidth;
   EncodedImageSource_logicalHeight(source) = inspection.logicalHeight;
   EncodedImageSource_frameCount(source) = inspection.frameCount;
   EncodedImageSource_nativeBag(source) = (int64)bag;
   EncodedImageSource_bytes(source) = null;
   EncodedImageSource_comment(source) = comment;
   return true;
}

static void captureEncodedBag(Context context, TCObject source, const uint8* bytes, int32 length) {
   ImageEncodedBag* bag;
   if (!bytes || length <= 0) {
      throwException(context, ImageException, "Invalid encoded image buffer");
      return;
   }
   bag = imageEncodedBagCreate(bytes, length);
   if (!bag) {
      throwException(context, OutOfMemoryError, null);
      return;
   }
   installEncodedBag(context, source, bag);
}

static ImageEncodedBag* readEncodedFile(CharP path) {
   FILE* file = findFile(path, null);
   long length;
   ImageEncodedBag* bag;
   size_t count;
   if (!file || fseek(file, 0, SEEK_END) != 0) {
      if (file) {
         fclose(file);
      }
      return null;
   }
   length = ftell(file);
   if (length <= 0 || length > 0x7FFFFFFF || fseek(file, 0, SEEK_SET) != 0) {
      fclose(file);
      return null;
   }
   bag = imageEncodedBagCreateEmpty((int32)length);
   if (!bag) {
      fclose(file);
      return null;
   }
   count = fread(bag->bytes, 1, (size_t)length, file);
   fclose(file);
   if (count != (size_t)length) {
      imageEncodedBagRelease(&bag);
      return null;
   }
   return bag;
}

TC_API void tuiEIS_captureNative_Bi(NMParams p) // totalcross/ui/image/EncodedImageSource private void captureNative(byte[] input, int length);
{
   TCObject input = p->obj[1];
   int32 length = p->i32[0];
   if (!input || length < 0 || length > ARRAYOBJ_LEN(input)) {
      throwException(p->currentContext, ImageException, "Invalid encoded image buffer");
      return;
   }
   captureEncodedBag(p->currentContext, p->obj[0], (uint8*)ARRAYOBJ_START(input), length);
}

TC_API void tuiEIS_captureNativePath_s(NMParams p) // totalcross/ui/image/EncodedImageSource private void captureNativePath(String path);
{
   char path[256];
   TCObject pathObj = p->obj[1];
   TCZFile tcz;
   String2CharPBuf(pathObj, path);
   tcz = tczGetFile(path, false);
   if (!tcz) {
      ImageEncodedBag* bag = readEncodedFile(path);
      if (!bag) {
         throwException(p->currentContext, ImageException, "Could not open encoded image");
         return;
      }
      installEncodedBag(p->currentContext, p->obj[0], bag);
      return;
   }
   if (tcz->uncompressedSize <= 0) {
      tczClose(tcz);
      throwException(p->currentContext, ImageException, "Could not open encoded image");
      return;
   }
   {
      ImageEncodedBag* bag;
      int32 count;
      bag = imageEncodedBagCreateEmpty(tcz->uncompressedSize);
      if (!bag) {
         tczClose(tcz);
         throwException(p->currentContext, OutOfMemoryError, null);
         return;
      }
      count = tczRead(tcz, bag->bytes, tcz->uncompressedSize);
      if (count != tcz->uncompressedSize) {
         imageEncodedBagRelease(&bag);
         tczClose(tcz);
         throwException(p->currentContext, ImageException, "Could not read encoded image");
         return;
      }
      tczClose(tcz);
      installEncodedBag(p->currentContext, p->obj[0], bag);
   }
}

TC_API void tuiEIS_releaseNativeBag(NMParams p) // totalcross/ui/image/EncodedImageSource private void releaseNativeBag();
{
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(p->obj[0]);
   imageEncodedBagRelease(&bag);
   EncodedImageSource_nativeBag(p->obj[0]) = 0;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageLoad_s(NMParams p) // totalcross/ui/image/Image native private void imageLoad(String path);
{
   char path[256];
   TCObject imageObj = p->obj[0];
   TCObject pathObj = p->obj[1];
   TCZFile tcz;

   String2CharPBuf(pathObj, path);
   tcz = tczGetFile(path, false);
   if (tcz != null)
   {
      char magic[4]; // read the magic to find if its a png or a jpeg (note that jpeg has no magic)
      tczRead(tcz, magic, 4);
      if (magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G') {
         throwImageDecodeStatus(p->currentContext,
            pngLoad(p->currentContext, imageObj, null, null, tcz, magic, null, 0,
               imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled()));
      } else
         throwImageDecodeStatus(p->currentContext,
            jpegLoad(p->currentContext, imageObj, null, null, tcz, magic, 0, JPEG_DECODE_FULL, 0, 0,
               imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled()));
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_imageParse_sB(NMParams p) // totalcross/ui/image/Image native private void imageParse(totalcross.io.Stream in, byte []buf);
{
   TCObject imageObj = p->obj[0];
   TCObject streamObj = p->obj[1];
   TCObject bufObj = p->obj[2];
   uint8* buf = ARRAYOBJ_START(bufObj);
   char magic[4];
   xmove4(magic, buf); // buf already comes filled from Java with the first 4 bytes
   if ((magic[0] & 0xFF) == 0x89 && magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G') {
      throwImageDecodeStatus(p->currentContext,
         pngLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic, null, 0,
            imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled()));
   } else
      throwImageDecodeStatus(p->currentContext,
         jpegLoad(p->currentContext, imageObj, streamObj, bufObj, null, magic, 0, JPEG_DECODE_FULL, 0, 0,
            imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled()));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSource_e(NMParams p) // totalcross/ui/image/Image private void decodeEncodedSource(totalcross.ui.image.EncodedImageSource source);
{
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   imageRecordTestCounter("fullDecodeInvocationCountForTest");
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   if (!bag || !bag->bytes || bag->length <= 0)
   {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   ImageDecodeStatus status;
   if (EncodedImageSource_formatCode(sourceObj) == IMAGE_ENCODED_PNG)
      status = pngLoad(p->currentContext, imageObj, null, null, null, null, bag->bytes, bag->length,
         imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled());
   else if (EncodedImageSource_formatCode(sourceObj) == IMAGE_ENCODED_JPEG)
      status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
         JPEG_DECODE_FULL, 0, 0, imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled());
   else {
      throwException(p->currentContext, ImageException, "Unsupported deployed encoded image format");
      return;
   }
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
static void decodeEncodedSourceAtDenominator(NMParams p, int32 denominator, bool explicitRatio) {
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   int32 targetWidth = p->i32[0];
   int32 targetHeight = p->i32[1];
   ImageDecodeStatus status;
   TCClass imageClass = loadClass(p->currentContext, "totalcross.ui.image.Image", false);
   int32* targetedCount = imageTestAccountingField(
      "targetedDecodeInvocationCountForTest");
   int32* targetedRequestWidth = imageTestAccountingField(
      "targetedDecodeRequestWidthForTest");
   int32* targetedRequestHeight = imageTestAccountingField(
      "targetedDecodeRequestHeightForTest");
   int32* targetedDenominator = imageTestAccountingField(
      "targetedDecodeDenominatorForTest");
   int32* targetedWidth = imageTestAccountingField(
      "targetedDecodeWidthForTest");
   int32* targetedHeight = imageTestAccountingField(
      "targetedDecodeHeightForTest");
   int32* infrastructureFailure = imageClass == null ? null
      : getStaticFieldInt(imageClass, "targetedDecodeInfrastructureFailureForTest");
   if (!bag || !bag->bytes || bag->length <= 0) {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   if (EncodedImageSource_formatCode(sourceObj) != IMAGE_ENCODED_JPEG || targetWidth <= 0 || targetHeight <= 0
         || (explicitRatio && denominator != 2 && denominator != 4 && denominator != 8)) {
      throwException(p->currentContext, ImageException, "Targeted decode requires a JPEG image and positive dimensions");
      return;
   }
   if (targetedCount != null)
      (*targetedCount)++;
   if (targetedRequestWidth != null)
      (*targetedRequestWidth) = targetWidth;
   if (targetedRequestHeight != null)
      (*targetedRequestHeight) = targetHeight;
   if (targetedDenominator != null)
      (*targetedDenominator) = denominator;
   if (infrastructureFailure != null && *infrastructureFailure) {
      *infrastructureFailure = false;
      throwExceptionNamed(p->currentContext, "totalcross.ui.image.TransientImageMaterializationException", null);
      return;
   }
   status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
      explicitRatio ? JPEG_DECODE_EXPLICIT_RATIO : JPEG_DECODE_TARGET_DECODE,
      explicitRatio ? 1 : targetWidth, explicitRatio ? denominator : targetHeight,
      imageDecodeZeroCopyEnabled(), imageDecodeOpacityMetadataEnabled());
   if (status == IMAGE_DECODE_SUCCESS) {
      if (targetedWidth != null)
         (*targetedWidth) = Image_width(imageObj);
      if (targetedHeight != null)
         (*targetedHeight) = Image_height(imageObj);
   }
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSourceBestFit(NMParams p) // totalcross/ui/image/Image native private void decodeEncodedSourceBestFit(totalcross.ui.image.EncodedImageSource source, int targetWidth, int targetHeight);
{
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   int32 targetWidth = p->i32[0];
   int32 targetHeight = p->i32[1];
   ImageDecodeStatus status;
   int32* targetedRequestWidth = imageTestAccountingField("targetedDecodeRequestWidthForTest");
   int32* targetedRequestHeight = imageTestAccountingField("targetedDecodeRequestHeightForTest");
   if (!bag || !bag->bytes || bag->length <= 0) {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   if (EncodedImageSource_formatCode(sourceObj) != IMAGE_ENCODED_JPEG || targetWidth <= 0 || targetHeight <= 0) {
      throwException(p->currentContext, ImageException, "JPEG best-fit decode requires a JPEG image and positive dimensions");
      return;
   }
   imageRecordTestCounter("targetedDecodeInvocationCountForTest");
   if (targetedRequestWidth != null)
      (*targetedRequestWidth) = targetWidth;
   if (targetedRequestHeight != null)
      (*targetedRequestHeight) = targetHeight;
   status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
      JPEG_DECODE_BEST_FIT, targetWidth, targetHeight, imageDecodeZeroCopyEnabled(),
      imageDecodeOpacityMetadataEnabled());
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSourceExplicit(NMParams p) // totalcross/ui/image/Image native private void decodeEncodedSourceExplicitRatio(totalcross.ui.image.EncodedImageSource source, int numerator, int denominator);
{
   TCObject imageObj = p->obj[0];
   TCObject sourceObj = p->obj[1];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   int32 numerator = p->i32[0];
   int32 denominator = p->i32[1];
   ImageDecodeStatus status;
   int32* targetedRequestWidth = imageTestAccountingField("targetedDecodeRequestWidthForTest");
   int32* targetedRequestHeight = imageTestAccountingField("targetedDecodeRequestHeightForTest");
   int32* targetedDenominator = imageTestAccountingField("targetedDecodeDenominatorForTest");
   uint64 outputWidth;
   uint64 outputHeight;
   if (!bag || !bag->bytes || bag->length <= 0) {
      throwException(p->currentContext, ImageException, "Encoded source has no native backing");
      return;
   }
   if (EncodedImageSource_formatCode(sourceObj) != IMAGE_ENCODED_JPEG || numerator <= 0 || denominator <= 0) {
      throwException(p->currentContext, ImageException, "JPEG explicit-ratio decode requires a JPEG image and positive ratio");
      return;
   }
   outputWidth = ((uint64)EncodedImageSource_intrinsicWidth(sourceObj) * (uint64)numerator
      + (uint64)denominator - 1) / (uint64)denominator;
   outputHeight = ((uint64)EncodedImageSource_intrinsicHeight(sourceObj) * (uint64)numerator
      + (uint64)denominator - 1) / (uint64)denominator;
   if (outputWidth == 0 || outputHeight == 0 || outputWidth > 0x7FFFFFFF || outputHeight > 0x7FFFFFFF) {
      throwException(p->currentContext, ImageException, "Image dimensions are too large.");
      return;
   }
   imageRecordTestCounter("targetedDecodeInvocationCountForTest");
   if (targetedRequestWidth != null)
      (*targetedRequestWidth) = (int32)outputWidth;
   if (targetedRequestHeight != null)
      (*targetedRequestHeight) = (int32)outputHeight;
   if (targetedDenominator != null)
      (*targetedDenominator) = denominator;
   status = jpegLoad(p->currentContext, imageObj, null, null, null, (const char*)bag->bytes, bag->length,
      JPEG_DECODE_EXPLICIT_RATIO, numerator, denominator, imageDecodeZeroCopyEnabled(),
      imageDecodeOpacityMetadataEnabled());
   throwImageDecodeStatus(p->currentContext, status);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSourceTargeted(NMParams p) // totalcross/ui/image/Image private void decodeEncodedSourceTargeted(totalcross.ui.image.EncodedImageSource source, int targetWidth, int targetHeight);
{
   int32 denominator = jpegTargetDecodeDenominatorForTest(
      EncodedImageSource_intrinsicWidth(p->obj[1]), EncodedImageSource_intrinsicHeight(p->obj[1]),
      p->i32[0], p->i32[1]);
   decodeEncodedSourceAtDenominator(p, denominator, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_decodeEncodedSourceTiered_e(NMParams p) // totalcross/ui/image/Image private void decodeEncodedSourceTiered(totalcross.ui.image.EncodedImageSource source, int targetWidth, int targetHeight, int denominator);
{
   decodeEncodedSourceAtDenominator(p, p->i32[2], true);
}

TC_API void tuiI_decodeEncodedSourceCandidat(NMParams p) // totalcross/ui/image/Image private static long decodeEncodedSourceCandidateHandle(totalcross.ui.image.EncodedImageSource source, int targetWidth, int targetHeight, int denominator, int optimizationMask);
{
#if TC_RENDERER_SKIA
   TCObject sourceObj = p->obj[0];
   ImageEncodedBag* bag = (ImageEncodedBag*)EncodedImageSource_nativeBag(sourceObj);
   int32 targetWidth = p->i32[0];
   int32 targetHeight = p->i32[1];
   int32 denominator = p->i32[2];
   int32 optimizationMask = p->i32[3];
   int32* capturedMask = imageTestAccountingField("detachedDecodeOptimizationMaskForTest");
   int64 detachedHandle = 0;
   ImageDecodeStatus status;

   p->retL = 0;
   if (capturedMask != null)
      *capturedMask = optimizationMask;
   if (!bag || !bag->bytes || bag->length <= 0
         || EncodedImageSource_formatCode(sourceObj) != IMAGE_ENCODED_JPEG
         || targetWidth <= 0 || targetHeight <= 0
         || (denominator != 1 && denominator != 2 && denominator != 4 && denominator != 8)) {
      throwException(p->currentContext, ImageException,
         "Image preparation requires a JPEG and positive dimensions");
      return;
   }
   if (denominator == 1)
      imageRecordTestCounter("fullDecodeInvocationCountForTest");
   else {
      imageRecordTestCounter("targetedDecodeInvocationCountForTest");
      {
         int32* targetedRequestWidth = imageTestAccountingField("targetedDecodeRequestWidthForTest");
         int32* targetedRequestHeight = imageTestAccountingField("targetedDecodeRequestHeightForTest");
         int32* targetedDenominator = imageTestAccountingField("targetedDecodeDenominatorForTest");
         if (targetedRequestWidth != null)
            *targetedRequestWidth = targetWidth;
         if (targetedRequestHeight != null)
            *targetedRequestHeight = targetHeight;
         if (targetedDenominator != null)
            *targetedDenominator = denominator;
      }
   }
   status = jpegLoadDetached(p->currentContext, null, null, null, null,
      (const char*)bag->bytes, bag->length,
      denominator == 1 ? JPEG_DECODE_FULL : JPEG_DECODE_EXPLICIT_RATIO,
      denominator == 1 ? 0 : 1, denominator == 1 ? 0 : denominator,
      (optimizationMask & IMAGE_OPT_DECODE_ZERO_COPY) != 0,
      (optimizationMask & IMAGE_OPT_RASTER_OPACITY_METADATA) != 0,
      optimizationMask,
      &detachedHandle);
   if (status != IMAGE_DECODE_SUCCESS || detachedHandle == 0) {
      if (detachedHandle != 0)
         skia_image_backing_release_detached(detachedHandle);
      throwImageDecodeStatus(p->currentContext, status == IMAGE_DECODE_SUCCESS
         ? IMAGE_DECODE_RESOURCE_FAILURE : status);
      return;
   }
   p->retL = detachedHandle;
#else
   p->retL = 0;
   UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_failNextNativeMaterializati(NMParams p) // totalcross/ui/image/Image native private static void failNextNativeMaterializationForTestNative();
{
   failNextImageAllocationForTest = true;
   UNUSED(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_failNextZeroCopyDecodeAfter(NMParams p) // totalcross/ui/image/Image native private static void failNextZeroCopyDecodeAfterAllocationForTestNative();
{
   failNextFinalBufferAllocationForTest = true;
   UNUSED(p);
}
//////////////////////////////////////////////////////////////////////////
#if TC_RENDERER_SKIA
static bool imageUsesNativeBacking(TCObject imageObj);
static int32 applyNativeColorMutation(Context currentContext, TCObject imageObj, int32 operation, int32 parameter1,
                                     int32 parameter2);
#endif
TC_API void tuiI_changeColorsNative_ii(NMParams p) // totalcross/ui/image/Image private void changeColorsNative(int from, int to);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(p->currentContext, thisObj, SKIA_IMAGE_COLOR_CHANGE_COLORS,
            p->i32[0], p->i32[1])) {
         throwException(p->currentContext, ImageException, "Could not change native image colors");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel from = makePixelARGB(p->i32[0]);
   Pixel to = makePixelARGB(p->i32[1]);
   changeColors(thisObj, from, to);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_getPixelRowNative_Bi(NMParams p) // totalcross/ui/image/Image private void getPixelRowNative(byte []fillIn, int y);
{
   TCObject thisObj = p->obj[0];
   TCObject fillIn = p->obj[1];
   int32 y = p->i32[0];
   getPixelRow(p->currentContext,thisObj, fillIn, y);
}
//////////////////////////////////////////////////////////////////////////
typedef enum
{
   SCALED_INSTANCE,
   SMOOTH_SCALED_INSTANCE,
   ROTATED_SCALED_INSTANCE,
   TOUCHEDUP_INSTANCE,
   FADED_INSTANCE,
   ALPHA_INSTANCE
} FuncType;

#if TC_RENDERER_SKIA
static bool imageUsesNativeBacking(TCObject imageObj)
{
   TCObject backing = imageObj ? Image_backing(imageObj) : null;
   return backing != null && strEq(OBJ_CLASS(backing)->name,
      "totalcross.ui.image.NativeImageBacking");
}

static int32 applyNativeColorMutation(Context currentContext, TCObject imageObj, int32 operation, int32 parameter1,
                                     int32 parameter2)
{
   int32 frameCount;
   int32 visibleWidth;
   int32 optimizationMask;
   int32 result;
   if (!imageUsesNativeBacking(imageObj)) {
      return false;
   }
   frameCount = Image_frameCount(imageObj);
   visibleWidth = Image_width(imageObj);
   optimizationMask = imageOptimizationMaskForDrawPtr != null
      ? *imageOptimizationMaskForDrawPtr
      : 0;
   result = skia_image_backing_apply_color_mutation(
         NativeImageBacking_nativeHandle(Image_backing(imageObj)), operation, parameter1,
         parameter2, frameCount, visibleWidth, Image_currentFrame(imageObj),
         optimizationMask);
   if (result == 0) {
      return 0;
   }
   Image_changed(imageObj) = true;
   if (result == 2) {
      imageRecordTestCounter("directColorMaterializationCountForTest");
   } else {
      imageRecordTestCounter("nativeColorReadbackCountForTest");
   }
   return result;
}
#endif

TC_API void tuiI_getModifiedNative_iiiiiii(NMParams p) // totalcross/ui/image/Image private void getModifiedNative(totalcross.ui.image.Image newImg, int angle, int percScale, int color, int brightness, int contrast, int type);
{
   TCObject thisObj = p->obj[0];
   TCObject newObj = p->obj[1];
   int32 percScale = p->i32[0];
   int32 angle = p->i32[1];
   Pixel color = p->i32[2] == 0 ? (Pixel)0 : makePixelRGB(p->i32[2]);
   FuncType type = (FuncType)p->i32[5];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj) && (type == TOUCHEDUP_INSTANCE
         || type == FADED_INSTANCE || type == ALPHA_INSTANCE)) {
      int32 operation;
      int32 parameter1;
      int32 parameter2;
      int64 handle;
      switch (type) {
         case TOUCHEDUP_INSTANCE:
            operation = SKIA_IMAGE_COLOR_TOUCH_UP_INSTANCE;
            parameter1 = p->i32[3];
            parameter2 = p->i32[4];
            break;
         case FADED_INSTANCE:
            operation = SKIA_IMAGE_COLOR_FADE_INSTANCE;
            parameter1 = p->i32[2];
            parameter2 = 0;
            break;
         default:
            operation = SKIA_IMAGE_COLOR_ALPHA_INSTANCE;
            parameter1 = p->i32[2];
            parameter2 = 0;
            break;
      }
      handle = skia_image_backing_create_color_instance(
         NativeImageBacking_nativeHandle(Image_backing(thisObj)), operation, parameter1, parameter2);
      if (handle == 0) {
         throwException(p->currentContext, OutOfMemoryError, null);
         return;
      }
      if (!imageReplaceNativeBacking(p->currentContext, newObj, handle,
            skia_image_backing_width(handle), skia_image_backing_height(handle))) {
         return;
      }
      imageRecordTestCounter("nativeColorReadbackCountForTest");
      return;
   }
#endif
   switch (type)
   {
      case SCALED_INSTANCE:
         getScaledInstance(thisObj, newObj);
         break;
      case SMOOTH_SCALED_INSTANCE:
         if (!getSmoothScaledInstance(thisObj, newObj))
            throwException(p->currentContext, OutOfMemoryError, null);
         break;
      case ROTATED_SCALED_INSTANCE:
         getRotatedScaledInstance(thisObj, newObj, percScale, angle, color, p->i32[3], p->i32[4]);
         break;
      case TOUCHEDUP_INSTANCE:
         getTouchedUpInstance(thisObj, newObj, p->i32[3], p->i32[4]);
         break;
      case FADED_INSTANCE: // guich@tc110_50
         getFadedInstance(thisObj, newObj, color);
         break;
      case ALPHA_INSTANCE: // guich@tc200
         getAlphaInstance(thisObj, newObj, p->i32[2]);
         break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setCurrentFrameNative_i(NMParams p) // totalcross/ui/image/Image private void setCurrentFrameNative(int nr);
{
   TCObject obj = p->obj[0];
   setCurrentFrame(obj, p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColorNative_i(NMParams p) // totalcross/ui/image/Image private void applyColorNative(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(p->currentContext, thisObj, SKIA_IMAGE_COLOR_APPLY_COLOR, p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image color");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelRGB(p->i32[0]);
   applyColor(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyColor2Native_i(NMParams p) // totalcross/ui/image/Image private void applyColor2Native(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(p->currentContext, thisObj, SKIA_IMAGE_COLOR_APPLY_COLOR2, p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image color2");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelARGB(p->i32[0]);
   applyColor2(thisObj, color);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_setTransparentColorNative_i(NMParams p) // totalcross/ui/image/Image private void setTransparentColorNative(int color);
{
   TCObject thisObj = p->obj[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(p->currentContext, thisObj, SKIA_IMAGE_COLOR_SET_TRANSPARENT_COLOR,
            p->i32[0], 0)) {
         throwException(p->currentContext, ImageException, "Could not set native image transparent color");
         return;
      }
      if (Image_frameCount(thisObj) > 1) {
         Image_currentFrame(thisObj) = 0;
      }
      return;
   }
#endif
   Pixel color = makePixelRGB(p->i32[0]);
   setTransparentColor(thisObj, color);
   p->retO = thisObj;
}

#if TC_RENDERER_SKIA
#include "skia/skia.h"
#endif
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyChangesNative(NMParams p) // totalcross/ui/image/Image private void applyChangesNative();
{
#ifndef SKIA_H
#ifdef __gl2_h_    
   TCObject thisObj = p->obj[0];
   applyChanges(p->currentContext,thisObj);
#endif 
#else
   TCObject img = p->obj[0];
   int32 id = Image_textureId(img);
   if (id >= 0) {
      skia_deleteBitmap(id);
      Image_textureId(img) = -1;
   }
   int32 frameCount = Image_frameCount(img);
   TCObject backing = Image_backing(img);
   TCObject pixelsObj;

   if (backing != null && strEq(OBJ_CLASS(backing)->name, "totalcross.ui.image.NativeImageBacking")) {
      Image_changed(img) = false;
      return;
   }
   pixelsObj = frameCount == 1 ? RasterImageBacking_pixels(backing)
      : RasterImageBacking_pixelsOfAllFrames(backing);
   
   if (pixelsObj != NULL) {
      int32 width = (frameCount > 1) ? Image_widthOfAllFrames(img) : Image_width(img);
      int32 height = Image_height(img);
      Pixel *pixels = (Pixel *)ARRAYOBJ_START(pixelsObj);

      id = skia_makeBitmap(SKIA_SCREEN_SURFACE_ID, pixels, width, height);
      if (id >= 0) {
         Image_textureId(img) = id;
      }
   }
   if (Image_textureId(img) >= 0) {
      Image_changed(img) = false;
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_freeTextureNative(NMParams p) // totalcross/ui/image/Image private void freeTextureNative();
{
#ifndef SKIA_H
#ifdef __gl2_h_                         
   freeTexture(p->obj[0]);
#endif
#else
      TCObject img = p->obj[0];
      int32 id = Image_textureId(img);
      if (id >= 0) {
         skia_deleteBitmap(id);
         Image_textureId(img) = -1;
      }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_createJpgNative_si(NMParams p) // totalcross/ui/image/Image private void createJpgNative(totalcross.io.Stream s, int quality);
{
   TCObject stream = p->obj[1];
   int32 quality = p->i32[0];
   /* The Java wrapper has already completed canonical materialization. */
   image2jpeg(p->currentContext, p->obj[0], stream, quality);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_applyFadeNative_i(NMParams p) // totalcross/ui/image/Image private void applyFadeNative(int fadeValue);
{
   TCObject thisObj = p->obj[0];
   int32 fadeValue = p->i32[0];
#if TC_RENDERER_SKIA
   if (imageUsesNativeBacking(thisObj)) {
      if (!applyNativeColorMutation(p->currentContext, thisObj, SKIA_IMAGE_COLOR_APPLY_FADE, fadeValue, 0)) {
         throwException(p->currentContext, ImageException, "Could not apply native image fade");
      }
      return;
   }
#endif
   applyFade(thisObj, fadeValue);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuiI_nativeResizeJpeg_ssi(NMParams p) // totalcross/ui/image/Image native public static void nativeResizeJpeg(String inputPath, String outputPath, int maxPixelSize);
{
   TCObject inputPathObj = p->obj[0];
   TCObject outputPathObj = p->obj[1];
   int32 maxPixelSize = p->i32[0];
   
#if defined (darwin)
   char input_path[512];
   char output_path[512];

   String2CharPBuf(inputPathObj, input_path);
   String2CharPBuf(outputPathObj, output_path);
   
   resizeImageAtPath(input_path, output_path, maxPixelSize);
#endif
}
#ifdef ENABLE_TEST_SUITE
#include "image_Image_test.h"
#endif
