// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../nm/ui/android_skia_egl_lifecycle.h"

#include <stdio.h>
#include <string.h>

typedef struct TMockEgl
{
   int valid;
   int current;
   int bindSucceeds;
   int currentAfterBind;
   int bindCalls;
} TMockEgl;

static int mockValid(void *userData)
{
   return ((TMockEgl *)userData)->valid;
}

static int mockCurrent(void *userData)
{
   return ((TMockEgl *)userData)->current;
}

static int mockMakeCurrent(void *userData)
{
   TMockEgl *egl = (TMockEgl *)userData;
   ++egl->bindCalls;
   if (egl->bindSucceeds)
      egl->current = egl->currentAfterBind;
   return egl->bindSucceeds;
}

static int expectCleanupPath(const char *name, TMockEgl egl, int expected,
      int expectedBindCalls)
{
   TAndroidSkiaEglCleanupOps ops;
   int actual;

   memset(&ops, 0, sizeof(ops));
   ops.userData = &egl;
   ops.contextIsValid = mockValid;
   ops.contextIsCurrent = mockCurrent;
   ops.makeContextCurrent = mockMakeCurrent;
   actual = androidSkiaEglContextReadyForCleanup(&ops);
   if (actual != expected || egl.bindCalls != expectedBindCalls)
   {
      fprintf(stderr, "%s: expected normal=%d binds=%d; got normal=%d binds=%d\n",
            name, expected, expectedBindCalls, actual, egl.bindCalls);
      return 0;
   }
   return 1;
}

int main(void)
{
   int passed = 0;
   passed += expectCleanupPath("valid current context",
         (TMockEgl){1, 1, 1, 1, 0}, 1, 0);
   passed += expectCleanupPath("valid non-current context rebinds",
         (TMockEgl){1, 0, 1, 1, 0}, 1, 1);
   passed += expectCleanupPath("lost context abandons",
         (TMockEgl){0, 0, 1, 1, 0}, 0, 0);
   passed += expectCleanupPath("unavailable context abandons",
         (TMockEgl){1, 0, 0, 0, 0}, 0, 1);
   passed += expectCleanupPath("binding verification failure abandons",
         (TMockEgl){1, 0, 1, 0, 0}, 0, 1);
   if (passed != 5)
      return 1;
   puts("Android EGL/Skia cleanup policy: 5 cases passed");
   return 0;
}
