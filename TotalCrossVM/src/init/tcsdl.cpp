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
#include "tcsdl.h"
#include "SDL2/SDL.h"
#include "SkImageInfo.h"

static SDL_Texture
    *texture; // even with SDL2, we can still bring ancient code back
SDL_Window *window;
static SDL_Renderer *renderer;
 SDL_Surface *sdlsurface;
Uint32 timeout;
SDL_Surface* surface2;

#define TICKS_FOR_NEXT_FRAME 22

int initSDL(ScreenSurface screen) {
  if(SDL_Init(SDL_INIT_VIDEO) < 0) printf("SDL_Init failed: init %s\n", SDL_GetError()); // init video
  SDL_Rect rect;
  if(SDL_GetDisplayBounds(0, &rect) < 0 ) printf("SDL_Init failed: display %s\n", SDL_GetError());

  // create the window like normal
  window = SDL_CreateWindow("TotalCross SDK", SDL_WINDOWPOS_UNDEFINED,
                            SDL_WINDOWPOS_UNDEFINED, rect.w, rect.h, 
                            SDL_WINDOW_FULLSCREEN);
  if(window == NULL) {
    printf("SDL_Init failed: window %s\n", SDL_GetError());
  }
   SDL_GetWindowSize(window, &screen->screenW, &screen->screenH);
   screen->bpp = 16;
   screen->pixels = (uint8*)1;

  

//   renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED |
//                                                 SDL_RENDERER_PRESENTVSYNC);

//   int texWidth;
//   int texHeight;
//   printf("3\n");
//   SDL_GetRendererOutputSize(renderer, &texWidth, &texHeight);

// Uint32 pixelformat = SDL_GetWindowPixelFormat(window);

//   printf("4 %s\n", SDL_GetPixelFormatName(pixelformat));
  // texture = SDL_CreateTexture(renderer, pixelformat,
  //                             SDL_TEXTUREACCESS_STREAMING, texWidth, texHeight);
sdlsurface = SDL_GetWindowSurface(window);
if(sdlsurface == NULL ) printf("SDL_Init failed: init %s\n", SDL_GetError());
  timeout = SDL_GetTicks() + TICKS_FOR_NEXT_FRAME;
  return 1;
}

void updateSDLScreen(int w, int h, void *pixels) {
  int i;
  // int* p = (int*) sdlsurface->pixels;

// memcpy(p, pixels, sizeof(int) * w * h * 2);
  // printf("20 pixels: %d \n", pixels);
  // for (i = 0 ; i < w * h * 2 ; i++) {
  //   p[i] = 0xFF;
  // }
  SDL_UpdateWindowSurface(window);
  
  // SDL_SetRenderTarget(renderer, texture);
  // SDL_SetRenderDrawColor(renderer, 0xFF, 0x00, 0x00, 0xFF);
  // // SDL_UpdateTexture(texture, NULL, pixels, w * 2);
  // SDL_RenderClear(renderer);
  // printf("21\n");
  // SDL_RenderCopy(renderer, texture, NULL, NULL);
  // printf("22\n");
  // SDL_RenderPresent(renderer);
  // printf("23\n");
  // SDL_RenderClear(renderer);
}

bool sdlPresent() {
  if (SDL_TICKS_PASSED(SDL_GetTicks(), timeout)) {
    // SDL_RenderPresent(renderer);
    SDL_UpdateWindowSurface(window);
    timeout = SDL_GetTicks() + TICKS_FOR_NEXT_FRAME;
    return 0;
  }
  return 1;
}

int ColorFormatSDL2Skia (int pixelFormat) {
  switch (pixelFormat)
  { 
    case SDL_PIXELFORMAT_UNKNOWN    	: return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_INDEX1LSB	  : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_INDEX1MSB		: return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_INDEX4LSB		: return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_INDEX4MSB	  : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_INDEX8		    : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB332		    : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB444		    : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB555		    : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_BGR555	      : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_ARGB4444	    : return 	kARGB_4444_SkColorType;
    case SDL_PIXELFORMAT_RGBA4444	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_ABGR4444		  : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_BGRA4444	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_ARGB1555	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGBA5551	    : return	kRGBA_8888_SkColorType;
    case SDL_PIXELFORMAT_ABGR1555	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_BGRA5551	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB565		    : return  kRGB_565_SkColorType;
    case SDL_PIXELFORMAT_BGR565	      : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB24		    : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_BGR24	      : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGB888	      : return	kBGRA_8888_SkColorType;
    case SDL_PIXELFORMAT_RGBX8888   	: return	kRGBA_8888_SkColorType;
    case SDL_PIXELFORMAT_BGR888	      : return	kBGRA_8888_SkColorType;
    case SDL_PIXELFORMAT_BGRX8888	    : return 	kBGRA_8888_SkColorType;
    case SDL_PIXELFORMAT_ARGB8888	    : return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_RGBA8888		  : return  kRGBA_8888_SkColorType;
    case SDL_PIXELFORMAT_ABGR8888		  : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_BGRA8888	    : return	kBGRA_8888_SkColorType;
    case SDL_PIXELFORMAT_ARGB2101010	: return	kUnknown_SkColorType;
    case SDL_PIXELFORMAT_YV12		      : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_IYUV		      : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_YUY2		      : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_UYVY		      : return  kUnknown_SkColorType;
    case SDL_PIXELFORMAT_YVYU		      : return  kUnknown_SkColorType;
    default                           : return  kUnknown_SkColorType;
  }
}
