// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GFX_EX_H
#define GFX_EX_H

// RRRR RRRR GGGG GGGG BBBB BBBB
// 0RRR RRGG GGGB BBBB

#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB
#define SETPIXEL555(r,g,b) ((((r) >> 3) << 10) | (((g) >> 3) << 5) | (((b) >> 3))) // bits 0RRRRRGGGGGBBBBB

#define GL_CHECK_ERROR //checkGlError(__FILE__,__LINE__);

typedef enum
{
   INVTEX_INVALIDATE,
   INVTEX_DEL_ALL,
   INVTEX_DEL_ONLYOLD
} INVTEX;
void invalidateTextures(INVTEX it); // imagePrimitives_c.h

#endif
