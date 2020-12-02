// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define DISPLAY_INDEX 0
#define NO_FLAGS 0
#define IS_NULL(x)      ((x) == NULL)
#define NOT_SUCCESS(x)  ((x) != 0)
#define SUCCESS(x)      ((x) == 0)

#include "tcsdl.h"
#include <iostream>
#include <vector>
#include <chrono>

static SDL_Renderer* renderer = NULL;
static SDL_Texture* texture = NULL;
static SDL_Window *window = NULL; 
static SDL_Surface *surface = NULL;
static bool usesTexture;

/**
 * Returns the current time in microseconds.
 */
long getMicrotime(){
	struct timeval currentTime;
	gettimeofday(&currentTime, NULL);
	return currentTime.tv_sec * (int)1e6 + currentTime.tv_usec;
}

inline auto getMicrotimeChrono() {
	return std::chrono::duration_cast<std::chrono::microseconds>(now.time_since_epoch()).count();
}
/*
 * Init steps to create a window and texture to Skia handling
 *
 * Args:
 * - ScreenSurface from TotalCross globals
 *
 * Return:
 * - false on failure
 * - true on success
 */
bool TCSDL_Init(ScreenSurface screen, const char* title, bool fullScreen) {

	std::cout << "Testing video drivers..." << '\n';
	std::vector< bool > drivers(SDL_GetNumVideoDrivers());

	for (int i = 0; i < drivers.size(); ++i) {
		drivers[i] = (0 == SDL_VideoInit(SDL_GetVideoDriver(i)));
		SDL_VideoQuit();
	}

	std::cout << "SDL_VIDEODRIVER available:";
	for (int i = 0; i < drivers.size(); ++i) {
		std::cout << " " << SDL_GetVideoDriver(i);
	}
	std::cout << '\n';

	std::cout << "SDL_VIDEODRIVER usable   :";
	for (int i = 0; i < drivers.size(); ++i) {
      if( !drivers[ i ] ) continue;
		std::cout << " " << SDL_GetVideoDriver(i);
	}
	std::cout << '\n';

	// Only init video (without audio)
	if (NOT_SUCCESS(SDL_Init(SDL_INIT_VIDEO))) {
		std::cerr << "SDL_Init(): " << SDL_GetError() << '\n';
		return false;
	}
	std::cout << "SDL_VIDEODRIVER selected : " << SDL_GetCurrentVideoDriver() << '\n';

	SDL_Rect viewport;
#if __APPLE__
	// Get the desktop area represented by a display, with the primary
	// display located at 0,0 based on viewport allocated on initial position
	int (*TCSDL_GetDisplayBounds)(int, SDL_Rect*) = 
#ifdef __arm__                  
	&SDL_GetDisplayBounds;
#else                           
	&SDL_GetDisplayUsableBounds;
#endif

	if(NOT_SUCCESS(TCSDL_GetDisplayBounds(DISPLAY_INDEX, &viewport))) {
		printf("SDL_GetDisplayBounds failed: %s\n", SDL_GetError());
		return false;
	}

	// Adjust height on desktop, it should not affect fullscreen (y should be 0)
	viewport.h -= viewport.y;
#else
	SDL_DisplayMode DM;
	// Get current display mode of all displays.
  	for(int i = 0; i < SDL_GetNumVideoDisplays(); ++i){
		int should_be_zero = SDL_GetCurrentDisplayMode(i, &DM);

		if(should_be_zero != 0) {
			std::cerr << "SDL_GetCurrentDisplayMode() failed for video display #" << i << ": " << SDL_GetError() << '\n';
		} else {
			std::cout << "SDL_DisplayMode #" << i << ": current display mode is " << DM.w << "x" << DM.h << "x" << DM.refresh_rate << '\n';
		}
	}
	viewport.x = SDL_WINDOWPOS_UNDEFINED; 
	viewport.y = SDL_WINDOWPOS_UNDEFINED;
	viewport.w = DM.w;
	viewport.h = DM.h;
#endif

	if (getenv("TC_WIDTH") != NULL) {
		viewport.w = std::stoi(getenv("TC_WIDTH"));
	}
	if (getenv("TC_HEIGHT") != NULL) {
		viewport.h = std::stoi(getenv("TC_HEIGHT"));
	}

	uint32 flags; 
#ifdef __APPLE__
	flags = SDL_WINDOW_SHOWN;
#else
	if(getenv("TC_FULLSCREEN") == NULL) {
		flags = SDL_WINDOW_FULLSCREEN;
	} else {
		flags = SDL_WINDOW_SHOWN;
		viewport.w -= viewport.w*0.09;
		viewport.h -= viewport.h*0.09;
	}
#endif

	// Create the window
	if (IS_NULL(window = SDL_CreateWindow(
							 title,
							 viewport.x,
							 viewport.y,
							 viewport.w,
							 viewport.h,
							 flags
						 ))) {
		std::cerr << "SDL_CreateWindow(): " << SDL_GetError() << '\n';
		return false;
	}

	std::cout << "SDL_RENDER_DRIVER available:";
	for (int i = 0; i < SDL_GetNumRenderDrivers(); ++i) {
		SDL_RendererInfo info;
		SDL_GetRenderDriverInfo(i, &info);
		std::cout << " " << info.name;
	}
	std::cout << '\n';

	// Create a 2D rendering context for a window
	if (IS_NULL(renderer = SDL_CreateRenderer(window, -1, NO_FLAGS))) {
		std::cerr << "SDL_CreateRenderer(): " << SDL_GetError() << '\n';
		std::cout << '\n' << "HINT: try to export SDL_RENDER_DRIVER environment variable with an available render driver!" << '\n';
		return false;
	}

	// Get renderer driver information
	SDL_RendererInfo rendererInfo;
	if (NOT_SUCCESS(SDL_GetRendererInfo(renderer, &rendererInfo))) {
		std::cerr << "SDL_GetRendererInfo(): " << SDL_GetError() << '\n';
		return 0;
	}
	std::cout << "SDL_RENDER_DRIVER selected : " << rendererInfo.name << '\n';

	// Get window pixel format
	Uint32 windowPixelFormat;
	if (SDL_PIXELFORMAT_UNKNOWN == (windowPixelFormat = SDL_GetWindowPixelFormat(window))) {
		std::cerr << "SDL_GetWindowPixelFormat(): " << SDL_GetError() << '\n';
		return false;
	}

	usesTexture = std::string(rendererInfo.name).compare(std::string("software"));

	if(usesTexture) {
		// MUST USE SDL_TEXTUREACCESS_STREAMING, CANNOT BE REPLACED WITH SDL_CreateTextureFromSurface
		if (IS_NULL(texture = SDL_CreateTexture(
								renderer,
								windowPixelFormat,
								SDL_TEXTUREACCESS_STREAMING,
								viewport.w,
								viewport.h))) {
			std::cerr << "SDL_CreateTexturet(): " << SDL_GetError() << '\n';
			return false;
		}
	} else {
		surface = SDL_GetWindowSurface(window);
	}

	// Get pixel format struct
	SDL_PixelFormat* pixelformat;
	if (IS_NULL(pixelformat = SDL_AllocFormat(windowPixelFormat))) {
		std::cerr << "SDL_AllocFormat(): " << SDL_GetError() << '\n';
		return false;
	}

	// Adjusts screen width to the viewport
	screen->screenW = viewport.w;
	// Adjusts screen height to the viewport
	screen->screenH = viewport.h;
	// Adjusts screen's BPP
	screen->bpp = pixelformat->BitsPerPixel;
	// Set surface pitch
	screen->pitch = pixelformat->BytesPerPixel * screen->screenW;
	// pixel order
	screen->pixelformat = windowPixelFormat;

	if (usesTexture) {
		// Adjusts screen's pixel surface
		if (IS_NULL(screen->pixels = (uint8*) malloc(screen->pitch * screen->screenH))) {
			std::cerr << "Failed to alloc " << (screen->pitch * screen->screenH) << " bytes for pixel surface" << '\n';
			return false;
		}
	}else {
		screen->pixels = (uint8*) surface->pixels;
	}

	if (IS_NULL(screen->extension = (ScreenSurfaceEx) malloc(sizeof(TScreenSurfaceEx)))) {
		if (usesTexture) {
			free(screen->pixels);
		}
		std::cerr << "Failed to alloc TScreenSurfaceEx of " << sizeof(TScreenSurfaceEx) << " bytes" << '\n';
		return false;
	}
	SCREEN_EX(screen)->window = window;
	SCREEN_EX(screen)->renderer = renderer;
	SCREEN_EX(screen)->texture = texture;
	SCREEN_EX(screen)->surface = surface;

	SDL_FreeFormat(pixelformat);

	return true;
}

/*
 * Update the given texture rectangle with new pixel data.
 *
 * # MUST BE REVIEWED
 *
 * SDL_UpdateTexture it's too slow and our implementation
 * depends on it
 */
void TCSDL_UpdateTexture(int w, int h, int pitch, void* pixels) {
	PROFILE_START
	if(usesTexture) {
		// Update the given texture rectangle with new pixel data.
		SDL_UpdateTexture(texture, NULL, pixels, pitch);
	}
	PROFILE_STOP
	// Call SDL render present
	TCSDL_Present();
}

/*
 * Update the screen with rendering performed
 */
void TCSDL_Present() {
	PROFILE_START
  if(usesTexture) {
    // Copy a portion of the texture to the current rendering target
    SDL_RenderCopy(renderer, texture, NULL, NULL);
    // Update the screen with rendering performed
    SDL_RenderPresent(renderer);
    // Clears the entire rendering targe
    SDL_RenderClear(renderer);
  } else {
    SDL_UpdateWindowSurface(window);
  }
  PROFILE_STOP
}

/*
 * Destroy all SDL allocated variables
 */
void TCSDL_Destroy(ScreenSurface screen) {
	if (usesTexture && screen->pixels != NULL) {
		free(screen->pixels);
	}

	if (SCREEN_EX(screen) != NULL) {
		if (SCREEN_EX(screen)->surface != NULL) {
			SDL_FreeSurface(SCREEN_EX(screen)->surface);
		}
		if (SCREEN_EX(screen)->texture != NULL) {
			SDL_DestroyTexture(SCREEN_EX(screen)->texture);
		}
		if (SCREEN_EX(screen)->renderer != NULL) {
			SDL_DestroyRenderer(SCREEN_EX(screen)->renderer);
		}
		if (SCREEN_EX(screen)->window != NULL) {
			SDL_DestroyWindow(SCREEN_EX(screen)->window);
		}
		free(SCREEN_EX(screen));
	}
	SDL_Quit();
}

void TCSDL_GetWindowSize(ScreenSurface screen, int32* width, int32* height) {
	SDL_GetWindowSize(SCREEN_EX(screen)->window, width, height);
}
