// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define DISPLAY_INDEX 0
#define NO_FLAGS 0

#include "tcsdl.h"

SDL_Window *window;
SDL_Surface *surfaceSDL;

static SDL_Renderer *renderer;
static SDL_Texture *texture;

int initSDL(ScreenSurface screen) {
  // Only init video (without audio)
  if(SDL_Init(SDL_INIT_VIDEO) < 0) {
    printf("SDL_Init failed: %s\n", SDL_GetError()); 
  }

  // Get the desktop area represented by a display, with the primary
  // display located at 0,0 based on rect allocated on initial position
  SDL_Rect rect;
  if(SDL_GetDisplayBounds(DISPLAY_INDEX, &rect) < 0 ) {
    printf("SDL_GetDisplayBounds failed: %s\n", SDL_GetError());
  }

  // Create the window
  window = SDL_CreateWindow("TotalCross SDK", 
                            SDL_WINDOWPOS_UNDEFINED,
                            SDL_WINDOWPOS_UNDEFINED, 
                            rect.w, 
                            rect.h, 
                            SDL_WINDOW_FULLSCREEN);
  if(window == NULL) {
    printf("SDL_CreateWindow failed: %s\n", SDL_GetError());
  }

  // Create a 2D rendering context for a window.
  renderer = SDL_CreateRenderer(window, -1, NO_FLAGS);
  if(renderer == NULL) {
    printf("SDL_CreateRenderer failed: %s\n", SDL_GetError());
  }

  // Get renderer driver information
  SDL_RendererInfo rendererInfo;
  SDL_GetRendererInfo(renderer, &rendererInfo);
 
  // Set render driver 
  SDL_SetHint(SDL_HINT_RENDER_DRIVER, rendererInfo.name);
  
  // Get the SDL surface associated with the window.
  // MUST BE DONE AFTER THE RENDERER
  surfaceSDL = SDL_GetWindowSurface(window);
  if(surfaceSDL == NULL) {
    printf("SDL_GetWindowSurface failed: %s\n", SDL_GetError());
  }

  // Set renderer dimensions
  SDL_GetRendererOutputSize(renderer, &screen->screenW, &screen->screenH);
	
  // MUST USE SDL_TEXTUREACCESS_STREAMING, CANNOT BE REPLACED WITH SDL_CreateTextureFromSurface
  texture = SDL_CreateTexture(renderer, surfaceSDL->format->format, SDL_TEXTUREACCESS_STREAMING, (int)screen->screenW, (int)screen->screenH);

  // Adjusts screen's BPP
  screen->bpp = surfaceSDL->format->BitsPerPixel;
  // Adjusts screen's pixel format
  screen->pixels = (uint8*)surfaceSDL->pixels;
  // Set surface pitch 
  screen->pitch = surfaceSDL->pitch;

  return 1;
}

void updateScreenSDL(int w, int h, int pitch,void *pixels) {
  // Update the given texture rectangle with new pixel data.
  SDL_UpdateTexture(texture, NULL, pixels, pitch);
  // Call SDL render present 
  presentSDL();
}

void presentSDL() {
  // Copy a portion of the texture to the current rendering target
  SDL_RenderCopy(renderer, texture, NULL, NULL);
  // Update the screen with rendering performed
  SDL_RenderPresent(renderer);
  // Clears the entire rendering targe
  SDL_RenderClear(renderer);
}

int pixelFormatSDL (int pixelFormat) {
  switch (pixelFormat) { 
    case SDL_PIXELFORMAT_UNKNOWN    	: return  0;
    case SDL_PIXELFORMAT_INDEX1LSB	  : return  1;
    case SDL_PIXELFORMAT_INDEX1MSB		: return  2;
    case SDL_PIXELFORMAT_INDEX4LSB		: return  3;
    case SDL_PIXELFORMAT_INDEX4MSB	  : return  4;
    case SDL_PIXELFORMAT_INDEX8		    : return  5;
    case SDL_PIXELFORMAT_RGB332		    : return  6;
    case SDL_PIXELFORMAT_RGB444		    : return  7;
    case SDL_PIXELFORMAT_RGB555		    : return  8;
    case SDL_PIXELFORMAT_BGR555	      : return  9;
    case SDL_PIXELFORMAT_ARGB4444	    : return 10;
    case SDL_PIXELFORMAT_RGBA4444	    : return 11;
    case SDL_PIXELFORMAT_ABGR4444		  : return 12;
    case SDL_PIXELFORMAT_BGRA4444	    : return 13;
    case SDL_PIXELFORMAT_ARGB1555	    : return 14;
    case SDL_PIXELFORMAT_RGBA5551	    : return 15;
    case SDL_PIXELFORMAT_ABGR1555	    : return 16;
    case SDL_PIXELFORMAT_BGRA5551	    : return 17;
    case SDL_PIXELFORMAT_RGB565		    : return 18;
    case SDL_PIXELFORMAT_BGR565	      : return 19;
    case SDL_PIXELFORMAT_RGB24		    : return 20;
    case SDL_PIXELFORMAT_BGR24	      : return 21;
    case SDL_PIXELFORMAT_RGB888	      : return 22;
    case SDL_PIXELFORMAT_RGBX8888   	: return 23;
    case SDL_PIXELFORMAT_BGR888	      : return 24;
    case SDL_PIXELFORMAT_BGRX8888	    : return 25;
    case SDL_PIXELFORMAT_ARGB8888	    : return 26;
    case SDL_PIXELFORMAT_RGBA8888		  : return 27;
    case SDL_PIXELFORMAT_ABGR8888		  : return 28;
    case SDL_PIXELFORMAT_BGRA8888	    : return 29;
    case SDL_PIXELFORMAT_ARGB2101010	: return 30;
    case SDL_PIXELFORMAT_YV12		      : return 31;
    case SDL_PIXELFORMAT_IYUV		      : return 32;
    case SDL_PIXELFORMAT_YUY2		      : return 33;
    case SDL_PIXELFORMAT_UYVY		      : return 34;
    case SDL_PIXELFORMAT_YVYU		      : return 35;
  }
}