// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(tuMW_exit_i) // totalcross/ui/MainWindow native public final void exit(int exitCode);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tuMW_setTimerInterval_i) // totalcross/ui/MainWindow native void setTimerInterval(int n);
{
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1000000000, 0, 0, 0, 16), 1016000000);
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1017000000, 0, 1016000000, 16, 16), 1032000000);
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1050000000, 0, 1032000000, 16, 16), 1064000000);
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1010000000, 1100000000, 0, 16, 1), 1011000000);
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1010000000, 0, 1016000000, 16, 20), 1030000000);
   ASSERT2_EQUALS(I64, calculateAbsoluteTimerDeadlineNs(
      1010000000, 1050000000, 1016000000, 16, 0), 0);
   ASSERT2_EQUALS(I32, parseTimerDeadlineMode(NULL), 0);
   ASSERT2_EQUALS(I32, parseTimerDeadlineMode("relative"), 0);
   ASSERT2_EQUALS(I32, parseTimerDeadlineMode("absolute"), 1);
   finish: ;
}
TESTCASE(tuMW_getCommandLine) // totalcross/ui/MainWindow native public String getCommandLine();
{
   TEST_SKIP;
   finish: ;
}
