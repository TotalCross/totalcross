// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
#include "../Window.h"
#include "../../init/tcsdl.h"

static bool windowBackendSetSizeImpl(int32 width, int32 height)
{
   return TCSDL_SetWindowSize(width, height);
}

static void windowBackendSetDeviceTitle(TCObject titleObj)
{
   JCharP chars = String_charsStart(titleObj);
   int32 length = String_charsLen(titleObj);
   char *title = (char*)xmalloc((length * 4) + 1);
   char *out = title;
   int32 i;

   for (i = 0; i < length; i++)
   {
      uint32 codePoint = chars[i];
      if (codePoint >= 0xD800 && codePoint <= 0xDBFF && i + 1 < length
         && chars[i + 1] >= 0xDC00 && chars[i + 1] <= 0xDFFF)
      {
         codePoint = 0x10000 + ((codePoint - 0xD800) << 10)
            + (chars[++i] - 0xDC00);
      }

      if (codePoint < 0x80)
         *out++ = (char)codePoint;
      else if (codePoint < 0x800)
      {
         *out++ = (char)(0xC0 | (codePoint >> 6));
         *out++ = (char)(0x80 | (codePoint & 0x3F));
      }
      else if (codePoint < 0x10000)
      {
         *out++ = (char)(0xE0 | (codePoint >> 12));
         *out++ = (char)(0x80 | ((codePoint >> 6) & 0x3F));
         *out++ = (char)(0x80 | (codePoint & 0x3F));
      }
      else
      {
         *out++ = (char)(0xF0 | (codePoint >> 18));
         *out++ = (char)(0x80 | ((codePoint >> 12) & 0x3F));
         *out++ = (char)(0x80 | ((codePoint >> 6) & 0x3F));
         *out++ = (char)(0x80 | (codePoint & 0x3F));
      }
   }
   *out = '\0';

   if (SCREEN_EX(&screen) != null && SCREEN_EX(&screen)->window != null)
      SDL_SetWindowTitle(SCREEN_EX(&screen)->window, title);
   xfree(title);
}
