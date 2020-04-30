// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "PalmFont_c.h"
#if defined ANDROID || defined darwin || defined HEADLESS
#include "android/skia.h"
#endif

#define PLAIN 0
#define BOLD 1
//////////////////////////////////////////////////////////////////////////
TC_API void tufF_fontCreate(NMParams p) // totalcross/ui/font/Font native void fontCreate();
{
   char name[128];
   TCObject obj, fontName;
   FontFile ff;

   obj = p->obj[0];
   fontName = Font_name(obj);
   String2CharPBuf(fontName, name);
#ifdef SKIA_H
	// get ttf from tcz
	TCZFile file;
   char nameTTF[128];
   if (xstrcmp(name, "TCFont") == 0) {
      xstrcpy(nameTTF, "Roboto Regular");
   } else {
      xstrcpy(nameTTF, name);
   }
   xstrcat(nameTTF, ".ttf");
   
   int32 fontIdx = skia_getTypefaceIndex(nameTTF);
   if (fontIdx == -1) {
       if ((file = tczGetFile(nameTTF, false)) != null) {
           uint8 buffer[file->uncompressedSize];
           tczRead(file, buffer, file->uncompressedSize);
           fontIdx = skia_makeTypeface(nameTTF, buffer, file->uncompressedSize);
       }
       tczClose(file);
   }
   Font_skiaIndex(obj) = fontIdx;
   // save reference to SkTypeFace at Font_hvUserFont
#endif  
   // the only thing we can store here is the font file, because the UserFont will vary for char ranges
   ff = name[0] == '$' ? null : loadFontFile(name); //  bruno@tc114_37: native fonts always start with '$'
   if (ff == null)
   {
      // if the original font file was not found, use the default font.
      ff = defaultFont;
      // replace the name so the user can know that the font was not found
      Font_name(obj) = createStringObjectFromCharP(p->currentContext, defaultFontName,6);
      setObjectLock(Font_name(obj), UNLOCKED);
   }
   if (Font_hvUserFont(obj) == null) // alloc space for the pointer
   {
      Font_hvUserFont(obj) = createByteArray(p->currentContext, TSIZE);
      setObjectLock(Font_hvUserFont(obj), UNLOCKED);
   }
   if (Font_hvUserFont(obj) != null) // alloc space for the pointer
      xmoveptr(ARRAYOBJ_START(Font_hvUserFont(obj)), &ff);
}

#ifdef ENABLE_TEST_SUITE
#include "font_Font_test.h"
#endif
