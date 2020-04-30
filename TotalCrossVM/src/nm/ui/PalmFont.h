// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef PALMFONT_H
#define PALMFONT_H

#define AA_NO 0
#define AA_4BPP 1
#define AA_8BPP 2

#pragma pack(2)
typedef struct
{
   uint16 antialiased;      // 0 - aa_no, 1: aa_4bpp, 2: aa_8bpp
   uint16 firstChar;        // ASCII code of first character
   uint16 lastChar;         // ASCII code of last character
   uint16 spaceWidth;       // width of the space character
   uint16 maxWidth;         // width of font rectangle
   uint16 maxHeight;        // height of font rectangle
   uint16 owTLoc;           // offset to offset/width table
   uint16 ascent;           // ascent
   uint16 descent;          // descent
   uint16 rowWords;         // row width of bit image / 2
}  __attribute_packed__ *PalmFont, TPalmFont;
#pragma pack()

typedef struct
{
   TCZFile tcz;
   char name[32];
} *FontFile, TFontFile;

typedef struct TUserFont TUserFont;
typedef TUserFont* UserFont;

typedef struct
{
   uint8* alpha;
   uint16 size;
   JChar ch;
} *CharSizeCache, TCharSizeCache;

struct TUserFont
{
   uint8 *bitmapTable;
   TPalmFont fontP;
   uint16 rowWidthInBytes;
   uint16 *bitIndexTable;
   // gl fonts: used by the base font
#ifdef __gl2_h_
   int32 textureId[2];
   int32 maxW,maxH;
   uint8* textureAlphas;
   int16 charX[256], charY[256]; // value limited by texture's width (2048)
#endif   
   int32 *charPixels; // for one char
   // gl fonts: used by the inherited font. fontP.maxHeight will contain the target size
   struct TUserFont* ubase;
   // used only when drawing on images
   VoidPs* charSizeCache[256];
   int32 tempbufssize;
   uint8* tempbufs;          
   bool isDefaultFont;
};

int32 getJCharWidth(Context currentContext, TCObject fontObj, JChar ch);
int32 getJCharPWidth(Context currentContext, TCObject fontObj, JCharP s, int32 len); // len is NOT optional, it must be given
UserFont loadUserFontFromFontObj(Context currentContext, TCObject fontObj, JChar ch);
FontFile loadFontFile(char *fontName);
UserFont loadUserFont(Context currentContext, FontFile ff, bool plain, int32 size, JChar c);  // use size=-1 to load the normal size
bool fontInit(Context currentContext);
void fontDestroy();

#endif
