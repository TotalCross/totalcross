// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if HAVE_CONFIG_H
#include "config.h"
#endif

#include <dlfcn.h>
#include <string.h>
#include <stdio.h>
#include "xtypes.h"

char *args = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

typedef int (*ExecuteProgramProc)(char* args);

typedef void *Handle;

static Handle tryOpen(const char *prefix)
{
   char path[MAX_PATHNAME];
   snprintf(path, MAX_PATHNAME, "%s.so", prefix);
   return dlopen(path, RTLD_LAZY);
}

static int executeProgram(char* cmdline)
{
   int ret = 0;
   ExecuteProgramProc fExecuteProgram = NULL;
   Handle tcvm;
   tcvm = tryOpen("./libtcvm");                        // load in current folder - otherwise, we'll not be able to debug
   
   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen("../libtcvm");                  // load in parent folder
   }

   if (!tcvm) {
      printf("%s\n", dlerror());
      tcvm = tryOpen("/usr/lib/totalcross/libtcvm"); // load in most common absolute path
   }

   if (!tcvm) {
      printf("%s\n", dlerror());
      return 10000;
   }

   fExecuteProgram = (ExecuteProgramProc)dlsym(tcvm, TEXT("executeProgram"));
   
   if (!fExecuteProgram)
      return 10001;
   ret = fExecuteProgram(cmdline); // call the function now
   dlclose(tcvm); // free the library
   return ret;
}

#if 0
#include <directfb.h>
#include <stdio.h>

static IDirectFB *dfb = NULL;
static IDirectFBSurface *primary = NULL;
static int screen_width  = 0;
static int screen_height = 0;

#define DFBCHECK(x...)                                         \
  {                                                            \
    DFBResult err = x;                                         \
                                                               \
    if (err != DFB_OK)                                         \
      {                                                        \
        fprintf( stderr, "%s <%d>:\n\t", __FILE__, __LINE__ ); \
        DirectFBErrorFatal( #x, err );                         \
      }                                                        \
  }
#endif

int main(int argc, const char *argv[])
{
   char cmdline[512];
   xmemzero(cmdline,sizeof(cmdline));
   
   if (argv)
   {
      xstrcpy(cmdline, argv[0]);
      xstrcat(cmdline, ".tcz");
   }

   if (argc > 1 || args[0] != '1') // if there's a commandline passed by the system or one passed by the user
   {
      xstrcat(cmdline, " /cmd ");
      if (args[0] != '1')
         xstrcat(cmdline, args);
      const char **p = argv + 1;
      int n = argc;
      while (n-- > 1)
      {
         xstrcat(cmdline, " ");
         xstrcat(cmdline, *p++);
      }
   }

#if 0
   DFBSurfaceDescription dsc;
   DFBCHECK (DirectFBInit (&argc, &argv));
   DFBCHECK (DirectFBCreate (&dfb));
   DFBCHECK (dfb->SetCooperativeLevel (dfb, DFSCL_FULLSCREEN));
   dsc.flags = DSDESC_CAPS;
   dsc.caps  = DSCAPS_PRIMARY | DSCAPS_FLIPPING;

   IDirectFBDisplayLayer *layer;
   DFBCHECK(dfb->GetDisplayLayer(dfb, DLID_PRIMARY, &layer));
   layer->EnableCursor(layer, 1);
   //DFBCHECK(layer->SetRotation(layer, 180));

   DFBCHECK (dfb->CreateSurface( dfb, &dsc, &primary ));
   DFBCHECK (primary->GetSize (primary, &screen_width, &screen_height));
   DFBCHECK (primary->FillRectangle (primary, 0, 0, screen_width, screen_height));
   DFBCHECK (primary->SetColor (primary, 0x80, 0x80, 0xff, 0xff));
   DFBCHECK (primary->DrawLine (primary,
                                  0, screen_height / 2,
                   screen_width - 1, screen_height / 2));
   DFBCHECK (primary->Flip (primary, NULL, 0));
   Sleep(5);
   primary->Release( primary );
   dfb->Release( dfb );
   exit(0);
#endif

   return executeProgram(cmdline); // in tcvm\startup.c
}
