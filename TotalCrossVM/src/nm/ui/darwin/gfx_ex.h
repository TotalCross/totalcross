/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#ifndef GFX_EX_H
#define GFX_EX_H

// RRRR RRRR GGGG GGGG BBBB BBBB
// 0RRR RRGG GGGB BBBB

#define SETPIXEL32(r,g,b) (((r) << 16) | ((g) << 8) | (b))           // 00RRGGBB
#define SETPIXEL565(r,g,b) ((((r) >> 3) << 11) | (((g) >> 2) << 5) | (((b) >> 3))) // bits RRRRRGGGGGGBBBBB
#define SETPIXEL555(r,g,b) ((((r) >> 3) << 10) | (((g) >> 3) << 5) | (((b) >> 3))) // bits 0RRRRRGGGGGBBBBB

#if 1
//#define GL_CHECK_ERROR debug("%s (%d)",__FILE__,__LINE__);
#define GL_CHECK_ERROR checkGlError(__FILE__,__LINE__);
#else
#define GL_CHECK_ERROR 
#endif

#endif
