// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Keep the real desktop parser in this test translation unit while avoiding
 * duplicate VM entry points from the linked tcvm library. */
#define exitProgram tcWindowStartupIntegration_exitProgram
#define startProgram tcWindowStartupIntegration_startProgram
#define startVM tcWindowStartupIntegration_startVM
#define executeProgram tcWindowStartupIntegration_executeProgram
#define wokeUp tcWindowStartupIntegration_wokeUp
#include "../init/startup.c"
#undef exitProgram
#undef startProgram
#undef startVM
#undef executeProgram
#undef wokeUp

#include "../init/tcsdl.h"

static int failures;

static void failCase(const char *name, const char *reason)
{
   fprintf(stderr, "FAIL case=%s reason=%s\n", name, reason);
   failures++;
}

static void clearWindowEnvironment(void)
{
   unsetenv("TC_WIDTH");
   unsetenv("TC_HEIGHT");
   unsetenv("TC_FULLSCREEN");
}

static void runCase(const char *name, const char *commandLine,
   const char *widthEnvironment, const char *heightEnvironment,
   int expectedWidth, int expectedHeight, bool expectCentered)
{
   char vmCommandLine[512];
   char applicationCommandLine[256];
   DesktopCommandLineOptions commandOptions;
   TScreenSurface screen;
   SDL_Rect displayBounds;
   int width = 0;
   int height = 0;
   int x = 0;
   int y = 0;

   clearWindowEnvironment();
   if (widthEnvironment != NULL)
      setenv("TC_WIDTH", widthEnvironment, 1);
   if (heightEnvironment != NULL)
      setenv("TC_HEIGHT", heightEnvironment, 1);

   memset(&screen, 0, sizeof(screen));
   strcpy(vmCommandLine, commandLine);
   applicationCommandLine[0] = '\0';
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      sizeof(applicationCommandLine), &commandOptions))
   {
      failCase(name, "startup command-line parsing failed");
      return;
   }

   if (strstr(vmCommandLine, "/scr") != NULL
      || strstr(applicationCommandLine, "/scr") != NULL)
   {
      failCase(name, "/scr was not removed from the delivered command line");
      return;
   }

   if (!TCSDL_Init(&screen, "TotalCross startup integration", false, 0))
   {
      failCase(name, "TCSDL_Init failed");
      return;
   }

   TCSDL_GetWindowSize(&screen, &width, &height);
   if (SCREEN_EX(&screen) == NULL || SCREEN_EX(&screen)->window == NULL)
   {
      failCase(name, "SDL window handle was not available");
      TCSDL_DestroyWindow(&screen);
      return;
   }
   SDL_GetWindowPosition(SCREEN_EX(&screen)->window, &x, &y);
   SDL_GetDisplayBounds(0, &displayBounds);

   printf("case=%s expected=%dx%d obtained=%dx%d position=%d,%d vm=\"%s\" app=\"%s\"\n",
      name, expectedWidth, expectedHeight, width, height, x, y,
      vmCommandLine, applicationCommandLine);

   if (width != expectedWidth || height != expectedHeight)
      failCase(name, "created SDL window dimensions differed");
   if (expectCentered
      && (abs((x * 2 + width) - (displayBounds.x * 2 + displayBounds.w)) > 4
         || abs((y * 2 + height) - (displayBounds.y * 2 + displayBounds.h)) > 4))
      failCase(name, "created SDL window was not centered");

   TCSDL_DestroyWindow(&screen);
}

int main(void)
{
   SDL_Rect displayBounds;
   int defaultWidth;
   int defaultHeight;

   if (SDL_Init(SDL_INIT_VIDEO) != 0
      || SDL_GetDisplayBounds(0, &displayBounds) != 0
      || displayBounds.w <= 0 || displayBounds.h <= 0)
   {
      fprintf(stderr, "Unable to query SDL display bounds: %s\n", SDL_GetError());
      SDL_Quit();
      return 2;
   }
   defaultWidth = displayBounds.w / 2;
   defaultHeight = displayBounds.h / 2;
   SDL_Quit();

   runCase("scr-explicit", "App.tcz /scr -1,-1,1024,768 /cmd appArg",
      NULL, NULL, 1024, 768, false);
   runCase("scr-centered", "App.tcz /scr -2,-2,800,600 /cmd appArg",
      NULL, NULL, 800, 600, true);
   runCase("scr-precedence", "App.tcz /scr -1,-1,1024,768 /cmd appArg",
      "900", "700", 1024, 768, false);
   runCase("env-both", "App.tcz /cmd appArg", "900", "700",
      900, 700, false);
   runCase("env-width-only", "App.tcz /cmd appArg", "900", NULL,
      900, defaultHeight, false);
   runCase("env-height-only", "App.tcz /cmd appArg", NULL, "700",
      defaultWidth, 700, false);

   clearWindowEnvironment();
   if (failures != 0)
   {
      fprintf(stderr, "integration_failures=%d\n", failures);
      return 1;
   }
   puts("WindowStartup integration passed");
   return 0;
}
