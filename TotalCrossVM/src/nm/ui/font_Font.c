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
//////////////////////////////////////////////////////////////////////////
TC_API void tufF_fontCreate(NMParams p) // totalcross/ui/font/Font native void fontCreate();
{
   TCObject obj = p->obj[0];
#if TC_RENDERER_SKIA
   char name[128];
   TCZFile file;
   char nameTTF[128];
   int32 len;
   int32 fontIdx;

   String2CharPBuf(Font_name(obj), name);
   // Get the TTF from a resource in an already loaded TCZ.
   if (xstrcmp(name, "TCFont") == 0)
      xstrcpy(nameTTF, "Roboto Regular");
   else
      xstrcpy(nameTTF, name);
   len = xstrlen(nameTTF);
   // If it doesn't end with .ttf, append the resource suffix.
   if (!(nameTTF[len - 4] == '.' && nameTTF[len - 3] == 't' && nameTTF[len - 2] == 't' && nameTTF[len - 1] == 'f'))
      xstrcat(nameTTF, ".ttf");

   fontIdx = skia_getTypefaceIndex(nameTTF);
   if (fontIdx == -1)
   {
      file = tczGetFile(nameTTF, false);
      if (file != null)
      {
         uint8 *buffer = (uint8 *)xmalloc(file->uncompressedSize);
         if (buffer != null)
         {
            tczRead(file, buffer, file->uncompressedSize);
            fontIdx = skia_makeTypeface(nameTTF, buffer, file->uncompressedSize);
            xfree(buffer);
         }
         tczClose(file);
      }
   }
   Font_skiaIndex(obj) = fontIdx;
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
