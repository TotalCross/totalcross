// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../tcvm/tcvm.h"
#include "../tests/tc_testsuite.h"

#include <stdarg.h>
#include <stdio.h>
#include <string.h>

static bool testFail(struct TestSuite *tc, char *message, ...)
{
   (void)message;
   tc->failed++;
   return false;
}

static void testOutput(struct TestSuite *tc, char *message, ...)
{
   (void)tc;
   (void)message;
}

#define xmemzero(buffer, size) memset((buffer), 0, (size))
#include "../nm/ui/Window_test.h"

int main(void)
{
   struct TestSuite testSuite;

   memset(&testSuite, 0, sizeof(testSuite));
   testSuite.fail = testFail;
   testSuite.output = testOutput;
   test_windowResolveStartupConfiguration(&testSuite, null);
   test_windowResolveStartupFullscreenPolicy(&testSuite, null);
   if (testSuite.failed != 0)
   {
      fprintf(stderr, "WindowStartup tests failed\n");
      return 1;
   }
   puts("WindowStartup tests passed");
   return 0;
}
