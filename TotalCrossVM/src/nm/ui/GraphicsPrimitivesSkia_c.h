// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef GRAPHICSPRIMITIVESSKIA_HELPER_H
#define GRAPHICSPRIMITIVESSKIA_HELPER_H

#ifdef SKIA_H
#include "skia/skia_image_backing.h"
#include "ImageTestAccounting_c.h"
static bool isNativeImageBacking(TCObject backing)
{
   return backing != null && strEq(OBJ_CLASS(backing)->name,
      "totalcross.ui.image.NativeImageBacking");
}

static int32 skiaSurfaceForGraphics(TCObject g)
{
   int32 surfaceId;
   if (!Graphics_isImageSurface(g))
      surfaceId = SKIA_SCREEN_SURFACE_ID;
   else
   {
      TCObject image = Graphics_surface(g);
      if (image == null)
         return SKIA_INVALID_SURFACE_ID;

      surfaceId = SKIA_INVALID_SURFACE_ID;
      TCObject backing = Image_backing(image);
      if (isNativeImageBacking(backing)) {
         int64 nativeHandle = NativeImageBacking_nativeHandle(backing);
         if (skia_image_backing_make_mutable(nativeHandle)) {
            surfaceId = skia_image_backing_surface_id(nativeHandle);
         }
      }
      if (surfaceId == SKIA_INVALID_SURFACE_ID)
         surfaceId = Image_textureId(image);
      if (surfaceId == SKIA_INVALID_SURFACE_ID)
      {
         int32 frameCount = Image_frameCount(image);
         TCObject pixelsObj = frameCount > 1 ? RasterImageBacking_pixelsOfAllFrames(backing)
            : RasterImageBacking_pixels(backing);
         if (pixelsObj == null)
            return SKIA_INVALID_SURFACE_ID;
         Pixel* pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
         int32 width = frameCount > 1 ? Image_widthOfAllFrames(image) : Image_width(image);
         int32 height = Image_height(image);
         surfaceId = skia_makeBitmap(SKIA_SCREEN_SURFACE_ID, pixels, width, height);
         if (surfaceId >= 0) {
            Image_textureId(image) = surfaceId;
            Image_changed(image) = false;
         }
      }
   }

   if (surfaceId != SKIA_INVALID_SURFACE_ID)
      skia_setSurfaceScale(surfaceId, Graphics_contentScale(g));
   return surfaceId;
}

static bool skiaDrawPlanData(TCObject plan, SkiaImageDrawPlanData* data)
{
   TCObject root;
   TCObject operations;
   TCObject parameters;
   TCObject dimensions;
   if (!plan || !data) {
      return false;
   }
   root = ImageDrawPlan_root(plan);
   operations = ImageDrawPlan_operations(plan);
   parameters = ImageDrawPlan_parameters(plan);
   dimensions = ImageDrawPlan_dimensions(plan);
   if (!root || !isNativeImageBacking(Image_backing(root)) || !operations || !parameters || !dimensions) {
      return false;
   }
   data->rootHandle = NativeImageBacking_nativeHandle(Image_backing(root));
   data->rootWidth = ImageDrawPlan_rootWidth(plan);
   data->rootHeight = ImageDrawPlan_rootHeight(plan);
   data->rootLogicalWidth = ImageDrawPlan_rootLogicalWidth(plan);
   data->rootLogicalHeight = ImageDrawPlan_rootLogicalHeight(plan);
   data->rootFrameCount = ImageDrawPlan_rootFrameCount(plan);
   data->rootWidthOfAllFrames = ImageDrawPlan_rootWidthOfAllFrames(plan);
   data->rootContentScale = ImageDrawPlan_rootContentScale(plan);
   data->operations = (const int32*)ARRAYOBJ_START(operations);
   data->parameters = (const int32*)ARRAYOBJ_START(parameters);
   data->dimensions = (const int32*)ARRAYOBJ_START(dimensions);
   data->sourceDecodeGeneration = ImageDrawPlan_sourceDecodeGeneration(plan);
   data->operationCount = ARRAYOBJ_LEN(operations);
   data->outputWidth = ImageDrawPlan_outputWidth(plan);
   data->outputHeight = ImageDrawPlan_outputHeight(plan);
   data->outputFrameCount = ImageDrawPlan_outputFrameCount(plan);
   data->outputWidthOfAllFrames = ImageDrawPlan_outputWidthOfAllFrames(plan);
   data->currentFrame = ImageDrawPlan_currentFrame(plan);
   data->alphaMask = ImageDrawPlan_alphaMask(plan);
   data->transparentColor = ImageDrawPlan_transparentColor(plan);
   data->materializeAlphaMask = ImageDrawPlan_materializeAlphaMask(plan);
   data->outputAlphaMask = ImageDrawPlan_outputAlphaMask(plan);
   data->destinationScale = ImageDrawPlan_destinationScale(plan);
   data->outputContentScale = ImageDrawPlan_outputContentScale(plan);
   data->hwScaleW = ImageDrawPlan_hwScaleW(plan);
   data->hwScaleH = ImageDrawPlan_hwScaleH(plan);
   data->rootHwScaleW = ImageDrawPlan_rootHwScaleW(plan);
   data->rootHwScaleH = ImageDrawPlan_rootHwScaleH(plan);
   data->optimizationMask = 0;
   TCObject presentation = ImageDrawPlan_presentation(plan);
   if (!presentation) {
      presentation = root;
   }
   if (presentation) {
      const int32 presentationAlpha = Image_alphaMask(presentation);
      data->currentFrame = Image_currentFrame(presentation);
      data->transparentColor = Image_transparentColor(presentation);
      data->outputAlphaMask = presentationAlpha;
      data->alphaMask = data->materializeAlphaMask == 255
         ? presentationAlpha
         : (data->materializeAlphaMask * presentationAlpha + 127) / 255;
      data->hwScaleW = Image_hwScaleW(presentation);
      data->hwScaleH = Image_hwScaleH(presentation);
      int32* optimizationMaskField = getStaticFieldInt(
         OBJ_CLASS(presentation), "nativeOptimizationMaskForDraw");
      if (optimizationMaskField) {
         data->optimizationMask = *optimizationMaskField;
      }
   }
   return data->operationCount > 0 && data->operationCount * 4 <= ARRAYOBJ_LEN(parameters)
      && data->operationCount * 2 <= ARRAYOBJ_LEN(dimensions);
}

static bool skiaDrawGeometryPlan(Context currentContext, TCObject dstSurf, TCObject plan,
                                 int32 srcX, int32 srcY, int32 width, int32 height,
                                 int32 dstX, int32 dstY, int32 doClip)
{
   SkiaImageDrawPlanData data;
   int32 surfaceId;
   bool clipSet = false;
   if (!skiaDrawPlanData(plan, &data) || width <= 0 || height <= 0
      || data.hwScaleW != 1 || data.hwScaleH != 1) {
      return false;
   }
   surfaceId = skiaSurfaceForGraphics(dstSurf);
   if (surfaceId == SKIA_INVALID_SURFACE_ID) {
      return false;
   }
   dstX += Graphics_transX(dstSurf);
   dstY += Graphics_transY(dstSurf);
   if (doClip) {
      if (srcX < 0) {
         dstX -= srcX;
         width += srcX;
         srcX = 0;
      }
      if (srcY < 0) {
         dstY -= srcY;
         height += srcY;
         srcY = 0;
      }
      if (srcX + width > data.outputWidth) {
         width = data.outputWidth - srcX;
      }
      if (srcY + height > data.outputHeight) {
         height = data.outputHeight - srcY;
      }
      if (dstX < Graphics_clipX1(dstSurf)) {
         int32 delta = Graphics_clipX1(dstSurf) - dstX;
         dstX = Graphics_clipX1(dstSurf);
         srcX += delta;
         width -= delta;
      }
      if (dstY < Graphics_clipY1(dstSurf)) {
         int32 delta = Graphics_clipY1(dstSurf) - dstY;
         dstY = Graphics_clipY1(dstSurf);
         srcY += delta;
         height -= delta;
      }
      if (dstX + width > Graphics_clipX2(dstSurf)) {
         width = Graphics_clipX2(dstSurf) - dstX;
      }
      if (dstY + height > Graphics_clipY2(dstSurf)) {
         height = Graphics_clipY2(dstSurf) - dstY;
      }
      if (width <= 0 || height <= 0) {
         return true;
      }
      skia_setClip(surfaceId, Get_Clip(dstSurf));
      clipSet = true;
   }
   int result = skia_image_backing_draw_geometry_to_surface(surfaceId, &data,
      (float)srcX, (float)srcY, (float)(srcX + width), (float)(srcY + height),
      (float)dstX, (float)dstY, (float)(dstX + width), (float)(dstY + height));
   if (clipSet) {
      skia_restoreClip(surfaceId);
   }
   UNUSED(currentContext)
   return result != 0;
}
#endif

#endif

#ifndef SKIA_H
static void drawSurface(Context currentContext, TCObject dstSurf, TCObject srcSurf, int32 srcX, int32 srcY, int32 width, int32 height,
                       int32 dstX, int32 dstY, int32 doClip)
{
   uint32 i;
   Pixel * srcPixels;
   Pixel * dstPixels;
   int32 srcPitch, srcWidth, srcHeight, alphaMask = 0;
   bool isSrcScreen = !Surface_isImage(srcSurf);
   bool unlockSrc = false;
   if (Surface_isImage(srcSurf))
   {
#ifdef __gl2_h_ // for opengl, we will use the smoothScaled only if we will draw on an image. for win32, we will always use smoothScale
      bool forcedSmoothScale = Graphics_isImageSurface(dstSurf); // the destination is always a Graphics object
#else
      bool forcedSmoothScale = true;
#endif
      srcPitch = srcWidth = (int32)(Image_width(srcSurf) * Image_hwScaleW(srcSurf));
      srcHeight = (int32)(Image_height(srcSurf) * Image_hwScaleH(srcSurf));
      if (forcedSmoothScale && (Image_hwScaleW(srcSurf) != 1 || Image_hwScaleH(srcSurf) != 1))
      {
         static Method mGetScaledInstance, mGetSmoothScaledInstance;
         TCObject newSurf;
         if (mGetScaledInstance == null)
         {
            mGetScaledInstance       = getMethod(OBJ_CLASS(srcSurf), false, "getScaledInstance", 2, J_INT, J_INT);
            mGetSmoothScaledInstance = getMethod(OBJ_CLASS(srcSurf), false, "getSmoothScaledInstance", 2, J_INT, J_INT);
         }
         disableGC = true; // the gc may collect the image before we lock it here (*)
         newSurf = executeMethod(currentContext, Image_hwScaleW(srcSurf) < 1 && Image_hwScaleH(srcSurf) < 1 ? mGetSmoothScaledInstance : mGetScaledInstance, srcSurf, srcWidth, srcHeight).asObj;
         if (newSurf == null || newSurf == (TCObject)0xFFFFFFFF)
         {
            currentContext->thrownException = null;
            return;
         }
         else
         {
            srcSurf = newSurf;
            setObjectLock(newSurf, LOCKED); // (*)
            unlockSrc = true;
         }
         disableGC = false;
         srcPitch = srcWidth = Image_width(srcSurf);
         srcHeight = Image_height(srcSurf);
      }
      alphaMask = Image_alphaMask(srcSurf);
   }
   else
   {
      srcPitch = srcWidth = screen.screenW;
      srcHeight = screen.screenH;
   }
   dstPixels = getSurfacePixels(dstSurf);
   srcPixels = getSurfacePixels(srcSurf);
   if (!doClip)
   {
      /*
      | Even if no clip is required, we still have to make sure that the
      | area of the bitmap that we want to copy is inside its area.
      */
      if (srcX <= -width  || srcX >= srcWidth || srcY <= -height || srcY >= srcHeight)
         goto end;
      dstX += Graphics_transX(dstSurf);
      dstY += Graphics_transY(dstSurf);
   }
   else
   {
      dstX += Graphics_transX(dstSurf);
      dstY += Graphics_transY(dstSurf);

      /* clip the source rectangle to the source surface */
      if (srcX < 0)
      {
         width += srcX;
         dstX -= srcX;
         srcX = 0;
      }
      i = srcWidth - srcX;
      if (width > (int32)i)
         width = i;
      if (srcY < 0)
      {
         height += srcY;
         dstY -= srcY;
         srcY = 0;
      }
      i = srcHeight - srcY;
      if (height > (int32)i)
         height = i;

      /* clip the destination rectangle against the clip rectangle */
      if (dstX < Graphics_clipX1(dstSurf))
      {
         i = Graphics_clipX1(dstSurf) - dstX;
         dstX = Graphics_clipX1(dstSurf);
         srcX += i;
         width -= i;
      }
      if ((dstX + width) > Graphics_clipX2(dstSurf))
         width = Graphics_clipX2(dstSurf) - dstX;
      if (dstY < Graphics_clipY1(dstSurf))
      {
         i = Graphics_clipY1(dstSurf) - dstY;
         dstY = Graphics_clipY1(dstSurf);
         srcY += i;
         height -= i;
      }
      if ((dstY + height) > Graphics_clipY2(dstSurf))
         height = Graphics_clipY2(dstSurf) - dstY;

      /* check the validity */
      if (width <= 0 || height <= 0)
         goto end;
   }

   srcPixels += srcY * srcPitch + srcX;
   dstPixels += dstY * Graphics_pitch(dstSurf) + dstX;
#ifdef __gl2_h_
   if (isSrcScreen)
      glGetPixels(dstPixels,srcX,srcY,width,height,Graphics_pitch(dstSurf));
   else
   if (Graphics_useOpenGL(dstSurf))
   {
      int32 fc;
      int frame;

      if (Image_changed(srcSurf))
         applyChanges(currentContext, srcSurf);
      fc = Image_frameCount(srcSurf);
      frame = (fc <= 1) ? 0 : Image_currentFrame(srcSurf);
      Image_lastAccess(srcSurf) = getTimeStamp();
/*img*/ glDrawTexture(Image_textureId(srcSurf), srcX+frame*srcPitch,srcY,width,height, dstX,dstY, 0,0, fc > 1 ? (int32)(Image_widthOfAllFrames(srcSurf) * Image_hwScaleW(srcSurf)) : srcWidth,srcHeight, null, alphaMask);
   }
   else
#endif
   for (i=0; i < (uint32)height; i++) // in opengl, only case of image drawing on image
   {
      PixelConv *ps = (PixelConv*)srcPixels;
      PixelConv *pt = (PixelConv*)dstPixels;
      uint32 count = width;
      if (isSrcScreen)
         for (;count != 0; pt++,ps++, count--)
         {
            pt->pixel = ps->pixel;
            pt->a = 0xFF;
         }
      else
         for (;count != 0; pt++,ps++, count--)
         {
            int32 a = ps->a * alphaMask;
            a = (a+1 + (a >> 8)) >> 8; // alphaMask * a / 255
            if (a == 0xFF)
               pt->pixel = ps->pixel;
            else
            if (a != 0)
            {
               int32 ma = 0xFF-a;
               int32 r = (a * ps->r + ma * pt->r);
               int32 g = (a * ps->g + ma * pt->g);
               int32 b = (a * ps->b + ma * pt->b);
               pt->r = (r+1 + (r >> 8)) >> 8; // fast way to divide by 255
               pt->g = (g+1 + (g >> 8)) >> 8;
               pt->b = (b+1 + (b >> 8)) >> 8;
            }
         }
      srcPixels += srcPitch;
      dstPixels += Graphics_pitch(dstSurf);
   }
#ifndef __gl2_h_
   if (!currentContext->fullDirty && !Graphics_isImageSurface(dstSurf)) markScreenDirty(currentContext, dstX, dstY, width, height);
#else
   if (Graphics_isImageSurface(dstSurf))
      Image_changed(Graphics_surface(dstSurf)) = true;
   else
      currentContext->fullDirty = true;
#endif
end:
   if (unlockSrc)
      setObjectLock(srcSurf, UNLOCKED);
}
#else
static void drawSurface(Context currentContext, TCObject dstSurf, TCObject srcSurf, int32 srcX, int32 srcY, int32 w, int32 h,
   int32 dstX, int32 dstY, int32 doClip) {
   if (Surface_isImage(srcSurf)) {
      double contentScale = Image_contentScale(srcSurf);
      double scaleW;
      double scaleH;
      int32 srcWidth;
      int32 srcHeight;
      int32 frameCount = Image_frameCount(srcSurf);
      int32 frame = 0;
      bool clipSet = false;

      if (contentScale <= 0) {
         contentScale = 1;
      }
      srcWidth = (int32)ceil(Image_width(srcSurf) * Image_hwScaleW(srcSurf) / contentScale);
      srcHeight = (int32)ceil(Image_height(srcSurf) * Image_hwScaleH(srcSurf) / contentScale);
      scaleW = Image_hwScaleW(srcSurf) / contentScale;
      scaleH = Image_hwScaleH(srcSurf) / contentScale;
      if (scaleW <= 0 || scaleH <= 0 || w <= 0 || h <= 0 || srcWidth <= 0 || srcHeight <= 0) {
         return;
      }

      if (!doClip) {
         if (srcX <= -w || srcX >= srcWidth || srcY <= -h || srcY >= srcHeight) {
            return;
         }
         dstX += Graphics_transX(dstSurf);
         dstY += Graphics_transY(dstSurf);
      }
      else {
         int32 i;

         dstX += Graphics_transX(dstSurf);
         dstY += Graphics_transY(dstSurf);

         if (srcX < 0) {
            w += srcX;
            dstX -= srcX;
            srcX = 0;
         }
         i = srcWidth - srcX;
         if (w > i) {
            w = i;
         }
         if (srcY < 0) {
            h += srcY;
            dstY -= srcY;
            srcY = 0;
         }
         i = srcHeight - srcY;
         if (h > i) {
            h = i;
         }

         if (dstX < Graphics_clipX1(dstSurf)) {
            i = Graphics_clipX1(dstSurf) - dstX;
            dstX = Graphics_clipX1(dstSurf);
            srcX += i;
            w -= i;
         }
         if ((dstX + w) > Graphics_clipX2(dstSurf)) {
            w = Graphics_clipX2(dstSurf) - dstX;
         }
         if (dstY < Graphics_clipY1(dstSurf)) {
            i = Graphics_clipY1(dstSurf) - dstY;
            dstY = Graphics_clipY1(dstSurf);
            srcY += i;
            h -= i;
         }
         if ((dstY + h) > Graphics_clipY2(dstSurf)) {
            h = Graphics_clipY2(dstSurf) - dstY;
         }

         if (w <= 0 || h <= 0) {
            return;
         }
      }

      if (doClip) {
         skia_setClip(skiaSurfaceForGraphics(dstSurf), Get_Clip(dstSurf));
         clipSet = true;
      }
      if (frameCount > 1) {
         frame = Image_currentFrame(srcSurf);
         if (frame < 0) {
            frame = 0;
         }
         else if (frame >= frameCount) {
            frame = frameCount - 1;
         }
      }

      TCObject backing = Image_backing(srcSurf);
      if (isNativeImageBacking(backing)) {
         int32* optimizationMaskField = getStaticFieldInt(
            OBJ_CLASS(srcSurf), "nativeOptimizationMaskForDraw");
         const int32 optimizationMask = optimizationMaskField ? *optimizationMaskField : 0;
         if (!skia_image_backing_draw_to_surface(skiaSurfaceForGraphics(dstSurf),
               NativeImageBacking_nativeHandle(backing),
               (float)(srcX / scaleW + frame * Image_width(srcSurf)),
               (float)(srcY / scaleH),
               (float)((srcX + w) / scaleW + frame * Image_width(srcSurf)),
               (float)((srcY + h) / scaleH),
               (float)dstX, (float)dstY, (float)(dstX + w), (float)(dstY + h),
               Image_alphaMask(srcSurf), optimizationMask)) {
            if (clipSet) {
               skia_restoreClip(skiaSurfaceForGraphics(dstSurf));
            }
            return;
         }
      } else {
         int32 id = Image_textureId(srcSurf);
         if (id < 0) {
            TCObject pixelsObj = frameCount > 1 ? RasterImageBacking_pixelsOfAllFrames(backing)
               : RasterImageBacking_pixels(backing);
            Pixel* pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
            int32 width = frameCount > 1 ? Image_widthOfAllFrames(srcSurf) : Image_width(srcSurf);
            int32 height = Image_height(srcSurf);
            Image_textureId(srcSurf) = skia_makeBitmap(SKIA_SCREEN_SURFACE_ID, pixels, width, height);
            if (Image_textureId(srcSurf) >= 0) {
               Image_changed(srcSurf) = false;
            }
            id = Image_textureId(srcSurf);
         }
         skia_drawSurface(skiaSurfaceForGraphics(dstSurf), id,
            (float)(srcX / scaleW + frame * Image_width(srcSurf)),
            (float)(srcY / scaleH),
            (float)((srcX + w) / scaleW + frame * Image_width(srcSurf)),
            (float)((srcY + h) / scaleH),
            (float)dstX, (float)dstY, (float)(dstX + w), (float)(dstY + h),
            Image_alphaMask(srcSurf));
      }
      if (clipSet) {
         skia_restoreClip(skiaSurfaceForGraphics(dstSurf));
      }
   }
   else {
      LOGD("Trying to draw a control surface into some other surface");
   }

   markDirty(currentContext, dstSurf, dstX, dstY, w, h);
}
#endif

//   Device specific routine.
//   Gets the color value of the pixel, using the current translation
//   Returns -1 if error (out of clip bounds)
static int32 getPixel(TCObject g, int32 x, int32 y)
{
   int32 ret = -1;
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
      PixelConv p;
#ifdef SKIA_H
        return skia_getPixel(skiaSurfaceForGraphics(g), x, y);
#else
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
         return glGetPixel(x,y);
      else
#endif
         p.pixel = getGraphicsPixels(g)[y * Graphics_pitch(g) + x];
      ret = (p.r << 16) | (p.g << 8) | p.b;
#endif
   }
   return ret;
}

#ifndef SKIA_H
static PixelConv getPixelConv(TCObject g, int32 x, int32 y)
{
   PixelConv p;
   p.pixel = -1;
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
         p.pixel = glGetPixel(x,y);
      else
#endif
         p.pixel = getGraphicsPixels(g)[y * Graphics_pitch(g) + x];
   }
   return p;
}
#endif

// Device specific routine.
// Sets the pixel to the given color, translating and clipping
#ifndef SKIA_H
static void setPixel(Context currentContext, TCObject g, int32 x, int32 y, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
      {
         glDrawPixel(x,y,pixel,255);
         if (Graphics_isImageSurface(g))
            Image_changed(Graphics_surface(g)) = true;
         else
            currentContext->fullDirty = true;
      }
      else
#endif
      {
         getGraphicsPixels(g)[y * Graphics_pitch(g) + x] = pixel;
         if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x, y, 1, 1);
      }
   }
}
#else
static void setPixel(Context currentContext, TCObject g, int32 x, int32 y, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
   skia_setPixel(skiaSurfaceForGraphics(g), x, y, pixel | Graphics_alpha(g));
   skia_restoreClip(skiaSurfaceForGraphics(g));

   markPhysicalDirty(currentContext, g, x, y, 1, 1);
}
#endif
