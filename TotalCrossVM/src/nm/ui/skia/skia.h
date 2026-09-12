// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_H
#define SKIA_H

#include "tc_platform.h"

#include <stdint.h>

#include "ui/ImageBackingFormat.h"

#define SKIA_SCREEN_SURFACE_ID (-1)
#define SKIA_INVALID_SURFACE_ID (-2)
#define SKIA_TEST_COLOR_RGBA8888 0
#define SKIA_TEST_COLOR_BGRA8888 1
#define SKIA_TEST_COLOR_RGB565 2

enum SkiaRasterRejectReasonForTest {
    SKIA_RASTER_REJECT_CANVAS_STATE_FOR_TEST = 0,
    SKIA_RASTER_REJECT_SURFACE_DESTINATION_FOR_TEST = 1,
    SKIA_RASTER_REJECT_DEVICE_CLIP_FOR_TEST = 2,
    SKIA_RASTER_REJECT_PARTIAL_INTERSECTION_FOR_TEST = 3,
    SKIA_RASTER_REJECT_MAPPING_GEOMETRY_FOR_TEST = 4,
    SKIA_RASTER_REJECT_BACKING_INCOMPATIBLE_FOR_TEST = 5,
    SKIA_RASTER_REJECT_EXECUTION_FAILURE_FOR_TEST = 6,
    SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST = 7
};

#ifdef __cplusplus
extern "C"
{
#endif

typedef int int32;
typedef unsigned int uint32;
typedef uint32 Pixel32; // 32 bpp
typedef Pixel32 Pixel;

void imageRecordOpacityFallbackScanForTest(int32 pixels);

#if TC_WINDOWING_SDL
int32 colorType(uint32 pixelformat);
#endif
int64_t skia_benchmark_native_metric(int32 kind);
void initSkia(int w, int h, void * pixels, int pitch, uint32 pixelformat);
void destroySkiaScreen();
void flushSkia();

int skia_makeTypeface(char* name, void *data, int32 size);
int32 skia_getTypefaceIndex(char* name);
int32 skia_stringWidth(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold);
double skia_stringWidthD(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold);
void skia_fontMetrics(int32 typefaceIndex, double fontSize, int32 bold, double* ascent, double* descent, double* leading);

int skia_makeBitmap(int32 id, void *data, int32 w, int32 h);
void skia_deleteBitmap(int32 id);

void skia_setClip(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2);
void skia_restoreClip(int32 skiaSurface);
void skia_setSurfaceScale(int32 skiaSurface, double contentScale);

void skia_drawSurface(int32 skiaSurface, int32 id, float srcLeft, float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask);
void skia_drawDottedLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2);
Pixel skia_getPixel(int32 skiaSurface, int32 x, int32 y);
int skia_getPixelRow(int32 skiaSurface, void *output, int32 y, int32 width);
void skia_setPixel(int32 skiaSurface, int32 x, int32 y, Pixel pixel);
void skia_drawLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel);
void skia_drawRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_fillRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_drawText(int32 skiaSurface, const void *text, int32 chrCount, double x0, double y0, Pixel foreColor, int32 justifyWidth, double fontSize, int32 typefaceIndex, int32 bold);
void skia_ellipseDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, int32 fill, int32 gradient);
void skia_fillPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel c1, Pixel c2, int32 gradient, int32 isPie);
void skia_drawPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel pixel);
void skia_arcPiePointDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, int32 fill, int32 pie, int32 gradient);
void skia_drawRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_fillRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_drawRoundGradient(int32 skiaSurface, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, int32 vertical);
int32 skia_getsetRGB(int32 skiaSurface, void *dataObj, int32 offset, int32 x, int32 y, int32 w, int32 h, int32 isGet);
void skia_shiftScreen(float w, float h, float glShiftY);

int64_t skia_image_backing_create_empty(int32 width, int32 height);
int64_t skia_image_backing_create_empty_for_test(int32 width, int32 height, int32 colorType);
int64_t skia_image_backing_create_from_rgba_pixels(void* pixels, int32 width, int32 height);
int64_t skia_image_backing_create_from_owned_rgba_pixels(void* pixels, int32 width, int32 height);
int64_t skia_image_backing_create_from_owned_pixels(void* pixels, int32 width, int32 height,
    ImageBackingFormat format);
int64_t skia_image_backing_create_from_argb_pixels(const void* pixels, int32 width, int32 height);
int64_t skia_image_backing_create_detached_from_owned_rgba_pixels(void* pixels, int32 width, int32 height);
int64_t skia_image_backing_create_detached_from_owned_pixels(void* pixels, int32 width, int32 height,
    ImageBackingFormat format);
int64_t skia_image_backing_create_detached_from_argb_pixels(const void* pixels, int32 width, int32 height);
int64_t skia_image_backing_adopt_detached(int64_t handle);
void skia_image_backing_release_detached(int64_t handle);
#define SKIA_IMAGE_OPACITY_UNKNOWN 0
#define SKIA_IMAGE_OPACITY_OPAQUE 1
#define SKIA_IMAGE_OPACITY_TRANSLUCENT 2
void skia_image_backing_set_detached_opacity(int64_t handle, int32 opacity);
void skia_image_backing_set_opacity(int64_t handle, int32 opacity);
int32 skia_image_backing_opacity(int64_t handle);
#define SKIA_IMAGE_BACKING_SNAPSHOT_OK 0
#define SKIA_IMAGE_BACKING_SNAPSHOT_INVALID 1
#define SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE 2
int64_t skia_image_backing_snapshot(int64_t handle);
int skia_image_backing_snapshot_status(int64_t handle, int64_t* snapshotHandle);
void skia_image_backing_fail_next_snapshot_for_test(void);
int skia_image_backing_make_mutable(int64_t handle);
int skia_image_backing_mutate_for_test(int64_t handle);
void skia_image_backing_fail_next_promotion_for_test(void);
void skia_image_backing_fail_next_adoption_for_test(void);
int64_t skia_image_backing_scale(int64_t handle, int32 outputWidth, int32 outputHeight, bool smooth);
#define SKIA_IMAGE_COLOR_APPLY_FADE 0
#define SKIA_IMAGE_COLOR_FADE_INSTANCE 1
#define SKIA_IMAGE_COLOR_ALPHA_INSTANCE 2
#define SKIA_IMAGE_COLOR_TOUCH_UP_INSTANCE 3
#define SKIA_IMAGE_COLOR_APPLY_COLOR 4
#define SKIA_IMAGE_COLOR_APPLY_COLOR2 5
#define SKIA_IMAGE_COLOR_CHANGE_COLORS 6
#define SKIA_IMAGE_COLOR_SET_TRANSPARENT_COLOR 7
#define SKIA_IMAGE_DRAW_SCALE 0
#define SKIA_IMAGE_DRAW_SMOOTH_SCALE 1
#define SKIA_IMAGE_DRAW_ROTATE_SCALE 2
#define SKIA_IMAGE_DRAW_TOUCH_UP 3
#define SKIA_IMAGE_DRAW_FADE 4
#define SKIA_IMAGE_DRAW_ALPHA 5
#define SKIA_IMAGE_DRAW_APPLY_COLOR 6
#define SKIA_IMAGE_DRAW_APPLY_COLOR2 7
#define SKIA_IMAGE_DRAW_APPLY_FADE 8
#define SKIA_IMAGE_DRAW_CHANGE_COLORS 9
#define SKIA_IMAGE_DRAW_SET_TRANSPARENT_COLOR 10
#define SKIA_IMAGE_DRAW_FRAME_SELECT 11
#define SKIA_IMAGE_DRAW_CROP 12
#define SKIA_IMAGE_DRAW_FRAME_LAYOUT 13
int skia_image_backing_apply_color_mutation(int64_t handle, int32 operation, int32 parameter1,
    int32 parameter2, int32 frameCount, int32 visibleWidth, int32 currentFrame,
    int32 optimizationMask);
int64_t skia_image_backing_create_color_instance(int64_t handle, int32 operation, int32 parameter1,
    int32 parameter2);
typedef struct SkiaImageDrawPlanData {
    int64_t rootHandle;
    int32 rootWidth;
    int32 rootHeight;
    int32 rootLogicalWidth;
    int32 rootLogicalHeight;
    int32 rootFrameCount;
    int32 rootWidthOfAllFrames;
    double rootContentScale;
    const int32* operations;
    const int32* parameters;
    const int32* dimensions;
    int64_t sourceDecodeGeneration;
    int32 operationCount;
    int32 outputWidth;
    int32 outputHeight;
    int32 outputFrameCount;
    int32 outputWidthOfAllFrames;
    int32 currentFrame;
    int32 alphaMask;
    int32 transparentColor;
    int32 materializeAlphaMask;
    int32 outputAlphaMask;
    double destinationScale;
    double outputContentScale;
    double hwScaleW;
    double hwScaleH;
    double rootHwScaleW;
    double rootHwScaleH;
    int32 optimizationMask;
} SkiaImageDrawPlanData;
int skia_image_backing_try_write_pixels(void* targetCanvas, int64_t sourceHandle,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask,
    int32 optimizationMask);
int skia_image_backing_draw_geometry_to_surface(int32 targetSurface,
    const SkiaImageDrawPlanData* plan, float srcLeft, float srcTop, float srcRight,
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom);
int skia_image_backing_try_physical_geometry_to_surface(int32 targetSurface,
    const SkiaImageDrawPlanData* plan, float srcLeft, float srcTop, float srcRight,
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom,
    float clipLeft, float clipTop, float clipRight, float clipBottom);
int skia_image_backing_draw_generic_geometry_to_surface(int32 targetSurface,
    const SkiaImageDrawPlanData* plan, float srcLeft, float srcTop, float srcRight,
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom);
int64_t skia_image_backing_materialize_geometry(const SkiaImageDrawPlanData* plan);
void skia_image_backing_clear_physical_variant_if_equivalent(
    const SkiaImageDrawPlanData* plan);
int32 skia_image_backing_width(int64_t handle);
int32 skia_image_backing_height(int64_t handle);
int skia_image_backing_read_pixels(int64_t handle, void* output, int32 x, int32 y, int32 width, int32 height);
int skia_image_backing_read_row(int64_t handle, void* output, int32 y, int32 width);
int skia_image_backing_read_rgba_row(int64_t handle, void* output, int32 y, int32 width);
int skia_image_backing_read_argb_rows(int64_t handle, Pixel* output, int32 y, int32 width, int32 height);
int skia_image_backing_draw(int64_t targetHandle, int64_t sourceHandle,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask);
int32 skia_image_backing_surface_id(int64_t handle);
int skia_image_backing_draw_to_surface(int32 targetSurface, int64_t sourceHandle,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask,
    int32 optimizationMask);
void skia_image_backing_mark_surface_mutated(int32 surfaceId);
void skia_image_backing_release(int64_t handle);
void skia_image_backing_reset_accounting_for_test(void);
void skia_image_backing_clear_accounting_counters_for_test(void);
void skia_image_backing_set_accounting_for_test(int enabled);
void screen_diagnostics_set_for_test(int enabled);
void screen_diagnostics_clear_for_test(void);
uint64_t screen_diagnostics_update_calls_for_test(void);
uint64_t screen_diagnostics_present_calls_for_test(void);
uint64_t skia_image_backing_records_created_for_test(void);
uint64_t skia_image_backing_records_released_for_test(void);
uint64_t skia_image_backing_records_live_for_test(void);
uint64_t skia_image_backing_records_peak_live_for_test(void);
uint64_t skia_image_backing_bytes_live_for_test(void);
uint64_t skia_image_backing_bytes_peak_live_for_test(void);
uint64_t skia_image_backing_write_pixels_attempts_for_test(void);
uint64_t skia_image_backing_write_pixels_hits_for_test(void);
uint64_t skia_image_backing_write_pixels_fallbacks_for_test(void);
uint64_t skia_image_backing_write_pixels_copied_bytes_for_test(void);
uint64_t skia_image_backing_generic_geometry_draws_for_test(void);
uint64_t skia_image_backing_smooth_resample_draws_for_test(void);
void skia_image_backing_record_physical_identity_attempt_for_test(void);
void skia_image_backing_record_physical_identity_hit_for_test(void);
void skia_image_backing_record_physical_identity_fallback_for_test(void);
void skia_image_backing_record_physical_identity_resample_avoided_for_test(void);
void skia_image_backing_record_physical_identity_rejection_for_test(int32 reason);
void skia_image_backing_record_generic_geometry_draw_for_test(void);
void skia_image_backing_record_smooth_resample_draw_for_test(void);
uint64_t skia_image_backing_physical_identity_attempts_for_test(void);
uint64_t skia_image_backing_physical_identity_hits_for_test(void);
uint64_t skia_image_backing_physical_identity_fallbacks_for_test(void);
uint64_t skia_image_backing_physical_identity_resamples_avoided_for_test(void);
uint64_t skia_image_backing_physical_identity_rejections_for_test(int32 reason);
uint64_t skia_image_backing_target_color_attempts_for_test(void);
uint64_t skia_image_backing_target_color_materializations_for_test(void);
uint64_t skia_image_backing_target_color_hits_for_test(void);
uint64_t skia_image_backing_target_color_fallbacks_for_test(void);
uint64_t skia_image_backing_target_color_converted_bytes_for_test(void);
uint64_t skia_image_backing_physical_variant_lookups_for_test(void);
uint64_t skia_image_backing_physical_variant_hits_for_test(void);
uint64_t skia_image_backing_physical_variant_misses_for_test(void);
uint64_t skia_image_backing_physical_variant_materializations_for_test(void);
uint64_t skia_image_backing_physical_variant_evictions_for_test(void);
uint64_t skia_image_backing_physical_variant_bytes_for_test(void);
int32 skia_image_backing_format_for_test(int64_t handle);
uint64_t skia_image_backing_generation_for_test(int64_t handle);
uint64_t skia_image_backing_bytes_for_format_for_test(ImageBackingFormat format);
uint64_t skia_image_backing_compact_direct_decode_count_for_test(void);
uint64_t skia_image_backing_compact_direct_decode_bytes_for_test(void);
uint64_t skia_image_backing_temporary_rgba_decode_bytes_for_test(void);
uint64_t skia_image_backing_compact_readback_count_for_test(void);
uint64_t skia_image_backing_compact_row_scratch_peak_bytes_for_test(void);
uint64_t skia_image_backing_promotion_attempts_for_test(void);
uint64_t skia_image_backing_promotion_successes_for_test(void);
uint64_t skia_image_backing_promotion_failures_for_test(void);
uint64_t skia_image_backing_promotion_bytes_for_test(void);
void skia_image_backing_record_compact_decode_for_test(ImageBackingFormat format, uint64_t bytes);
void skia_image_backing_record_temporary_rgba_decode_for_test(uint64_t bytes);
#ifdef __cplusplus
}
#endif

#endif
