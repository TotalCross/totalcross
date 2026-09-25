// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



TESTCASE(jlT_yield) // java/lang/Thread native public static void yield();
{
#if TC_OS_DESKTOP
   ASSERT2_EQUALS(I32, 0, parseNativeThreadYieldMode(NULL));
   ASSERT2_EQUALS(I32, 0, parseNativeThreadYieldMode("legacy"));
   ASSERT2_EQUALS(I32, 0, parseNativeThreadYieldMode("invalid"));
   ASSERT2_EQUALS(I32, 1, parseNativeThreadYieldMode("native"));
#else
   TEST_SKIP;
#endif
   finish: ;
}
TESTCASE(jlT_start) // java/lang/Thread native public void start();
{
   TEST_SKIP;
   finish: ;
}
