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

static SDL_Renderer* renderer;
static SDL_Texture* texture;

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

	int width = (getenv("TC_WIDTH")  == NULL) ? 640 : std::stoi(getenv("TC_WIDTH"));
	int height = (getenv("TC_HEIGHT") == NULL) ? 400 : std::stoi(getenv("TC_HEIGHT"));

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
		if (!drivers[ i ]) {
			continue;
		}
		std::cout << " " << SDL_GetVideoDriver(i);
	}
	std::cout << '\n';

	// Only init video (without audio)
	if (NOT_SUCCESS(SDL_Init(SDL_INIT_VIDEO))) {
		printf("SDL_Init failed: %s\n", SDL_GetError());
		return false;
	}
	std::cout << "SDL_VIDEODRIVER selected : " << SDL_GetCurrentVideoDriver() << '\n';

	// Create the window
	SDL_Window* window;
	if (IS_NULL(window = SDL_CreateWindow(
							 title,
							 SDL_WINDOWPOS_UNDEFINED,
							 SDL_WINDOWPOS_UNDEFINED,
							 width,
							 height,
							 (getenv("TC_FULLSCREEN") == NULL) ? SDL_WINDOW_SHOWN : SDL_WINDOW_FULLSCREEN
						 ))) {
		printf("SDL_CreateWindow failed: %s\n", SDL_GetError());
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
		printf("SDL_CreateRenderer failed: %s\n", SDL_GetError());
		printf("\nHINT: try to export SDL_RENDER_DRIVER environment variable with an available render driver!\n");
		return false;
	}

	// Get renderer driver information
	SDL_RendererInfo rendererInfo;
	if (NOT_SUCCESS(SDL_GetRendererInfo(renderer, &rendererInfo))) {
		printf("SDL_GetRendererInfo failed: %s\n", SDL_GetError());
		return 0;
	}
	std::cout << "SDL_RENDER_DRIVER selected : " << rendererInfo.name << '\n';

	// Get window pixel format
	Uint32 windowPixelFormat;
	if (SDL_PIXELFORMAT_UNKNOWN == (windowPixelFormat = SDL_GetWindowPixelFormat(window))) {
		printf("SDL_GetWindowPixelFormat failed: %s\n", SDL_GetError());
		return false;
	}

	// MUST USE SDL_TEXTUREACCESS_STREAMING, CANNOT BE REPLACED WITH SDL_CreateTextureFromSurface
	if (IS_NULL(texture = SDL_CreateTexture(
							  renderer,
							  windowPixelFormat,
							  SDL_TEXTUREACCESS_STREAMING,
							  width,
							  height))) {
		printf("SDL_CreateTexture failed: %s\n", SDL_GetError());
		return false;
	}
	// Get pixel format struct
	SDL_PixelFormat* pixelformat;
	if (IS_NULL(pixelformat = SDL_AllocFormat(windowPixelFormat))) {
		printf("SDL_AllocFormat failed: %s\n", SDL_GetError());
		return false;
	}

	// Adjusts screen width to the viewport
	screen->screenW = width;
	// Adjusts screen height to the viewport
	screen->screenH = height;
	// Adjusts screen's BPP
	screen->bpp = pixelformat->BitsPerPixel;
	// Set surface pitch
	screen->pitch = pixelformat->BytesPerPixel * screen->screenW;
	// pixel order
	screen->pixelformat = windowPixelFormat;
	// Adjusts screen's pixel surface
	if (IS_NULL(screen->pixels = (uint8*) malloc(screen->pitch * screen->screenH))) {
		printf("Failed to alloc %d bytes for pixel surface\n", (screen->pitch * screen->screenH));
		return false;
	}

	if (IS_NULL(screen->extension = (ScreenSurfaceEx) malloc(sizeof(TScreenSurfaceEx)))) {
		free(screen->pixels);
		printf("Failed to alloc TScreenSurfaceEx of %l bytes\n", sizeof(TScreenSurfaceEx));
		return false;
	}
	SCREEN_EX(screen)->window = window;
	SCREEN_EX(screen)->renderer = renderer;
	SCREEN_EX(screen)->texture = texture;

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
	// Update the given texture rectangle with new pixel data.
	SDL_UpdateTexture(texture, NULL, pixels, pitch);
	// Call SDL render present
	TCSDL_Present();
}

/*
 * Update the screen with rendering performed
 */
void TCSDL_Present() {
	// Copy a portion of the texture to the current rendering target
	SDL_RenderCopy(renderer, texture, NULL, NULL);
	// Update the screen with rendering performed
	SDL_RenderPresent(renderer);
	// Clears the entire rendering targe
	SDL_RenderClear(renderer);
}

/*
 * Destroy all SDL allocated variables
 */
void TCSDL_Destroy(ScreenSurface screen) {
	if (screen->pixels != NULL) {
		free(screen->pixels);
	}

	if (SCREEN_EX(screen) != NULL) {
		SDL_DestroyTexture(SCREEN_EX(screen)->texture);
		SDL_DestroyRenderer(SCREEN_EX(screen)->renderer);
		SDL_DestroyWindow(SCREEN_EX(screen)->window);
		free(SCREEN_EX(screen));
	}
	SDL_Quit();
}

void TCSDL_GetWindowSize(ScreenSurface screen, int32* width, int32* height) {
	SDL_GetWindowSize(SCREEN_EX(screen)->window, width, height);
}
