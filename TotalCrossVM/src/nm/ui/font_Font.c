// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "PalmFont_c.h"
#if TC_RENDERER_SKIA
#include "skia/skia.h"
#endif

#define PLAIN 0
#define BOLD 1

#if TC_RENDERER_SKIA
static bool hasTTFSuffix(CharP name)
{
   int32 len = xstrlen(name);
   return len >= 4 && toLower(name[len - 4]) == '.' && toLower(name[len - 3]) == 't' &&
      toLower(name[len - 2]) == 't' && toLower(name[len - 1]) == 'f';
}

static CharP createSkiaResourceName(CharP fontName)
{
   CharP baseName = xstrcmp(fontName, "TCFont") == 0 ? "Roboto Regular" : fontName;
   int32 baseLen = xstrlen(baseName);
   bool hasSuffix = hasTTFSuffix(baseName);
   int32 resourceLen = baseLen + (hasSuffix ? 0 : 4);
   CharP resourceName = (CharP)xmalloc(resourceLen + 1);

   if (resourceName != null)
   {
      xstrcpy(resourceName, baseName);
      if (!hasSuffix)
         xstrcat(resourceName, ".ttf");
   }
   return resourceName;
}

static int32 loadSkiaTypeface(CharP resourceName)
{
   int32 fontIdx = skia_getTypefaceIndex(resourceName);
   TCZFile file;

   if (fontIdx < 0 && (file = tczGetFile(resourceName, false)) != null)
   {
      uint8 *buffer = (uint8 *)xmalloc(file->uncompressedSize);
      if (buffer != null)
      {
         tczRead(file, buffer, file->uncompressedSize);
         fontIdx = skia_makeTypeface(resourceName, buffer, file->uncompressedSize);
         xfree(buffer);
      }
      tczClose(file);
   }
   return fontIdx < 0 ? -1 : fontIdx;
}
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tufF_fontCreate(NMParams p) // totalcross/ui/font/Font native void fontCreate();
{
   TCObject obj = p->obj[0];
#if TC_RENDERER_SKIA
   CharP name = String2CharP(Font_name(obj));
   CharP resourceName;
   int32 fontIdx;
   bool useDefaultName = false;

   if (name == null)
   {
      throwException(p->currentContext, OutOfMemoryError, "Cannot convert font name");
      return;
   }
   resourceName = createSkiaResourceName(name);
   if (resourceName == null)
   {
      xfree(name);
      throwException(p->currentContext, OutOfMemoryError, "Cannot create font resource name");
      return;
   }
   fontIdx = loadSkiaTypeface(resourceName);
   if (fontIdx < 0)
   {
      useDefaultName = true;
      if (xstrcmp(name, defaultFontName) != 0)
      {
         xfree(resourceName);
         resourceName = createSkiaResourceName(defaultFontName);
         if (resourceName == null)
         {
            xfree(name);
            Font_skiaIndex(obj) = -1;
            throwException(p->currentContext, OutOfMemoryError, "Cannot create default font resource name");
            return;
         }
         fontIdx = loadSkiaTypeface(resourceName);
      }
   }
   if (useDefaultName)
   {
      TCObject defaultName = createStringObjectFromCharP(p->currentContext, defaultFontName, xstrlen(defaultFontName));
      if (defaultName == null)
      {
         xfree(resourceName);
         xfree(name);
         Font_skiaIndex(obj) = -1;
         throwException(p->currentContext, OutOfMemoryError, "Cannot set default font name");
         return;
      }
      Font_name(obj) = defaultName;
      setObjectLock(defaultName, UNLOCKED);
   }
   Font_skiaIndex(obj) = fontIdx < 0 ? -1 : fontIdx;
   xfree(resourceName);
   xfree(name);
#else
   char name[128];
   FontFile ff;

   String2CharPBuf(Font_name(obj), name);
   // The only thing we can store here is the font file, because the UserFont
   // will vary for char ranges.
   ff = name[0] == '$' ? null : loadFontFile(name); // bruno@tc114_37: native fonts always start with '$'
   if (ff == null)
   {
      // If the original font file was not found, use the default font.
      ff = defaultFont;
      // Replace the name so the user can know that the font was not found.
      Font_name(obj) = createStringObjectFromCharP(p->currentContext, defaultFontName, 6);
      setObjectLock(Font_name(obj), UNLOCKED);
   }
   if (Font_hvUserFont(obj) == null) // alloc space for the pointer
   {
      Font_hvUserFont(obj) = createByteArray(p->currentContext, TSIZE);
      setObjectLock(Font_hvUserFont(obj), UNLOCKED);
   }
   if (Font_hvUserFont(obj) != null) // alloc space for the pointer
      xmoveptr(ARRAYOBJ_START(Font_hvUserFont(obj)), &ff);
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "font_Font_test.h"
#endif
