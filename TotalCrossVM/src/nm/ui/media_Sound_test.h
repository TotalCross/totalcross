// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#define TEST_SOUND false

TESTCASE(tumS_beep) // totalcross/ui/media/Sound native public static void beep();
{
#if TEST_SOUND
   TNMParams p;
   p.currentContext = currentContext;
   tumS_beep(&p);
#endif
   UNUSED(tc);
}
TESTCASE(tumS_tone_ii) // totalcross/ui/media/Sound native public static void tone(int freq, int duration);
{
#if TEST_SOUND
   TNMParams p;
   int32 i32buf[2];
   p.currentContext = currentContext;
   p.i32 = i32buf;
   p.i32[1] = 100;

   p.i32[0] = 350;
   tumS_tone_ii(&p);
   p.i32[0] = 700;
   tumS_tone_ii(&p);

   p.i32[0] = 500;
   tumS_tone_ii(&p);
   p.i32[0] = 1000;
   tumS_tone_ii(&p);
   p.i32[0] = 1500;
   tumS_tone_ii(&p);
#endif
   UNUSED(tc);
}
TESTCASE(tumS_setEnabled_b) // totalcross/ui/media/Sound native public static void setEnabled(boolean on);
{
#if TEST_SOUND
   TNMParams p;
   bool boolBuf[1];
   p.currentContext = currentContext;
   p.i32 = boolBuf;

   boolBuf[0] = true;
   tumS_setEnabled_b(&p);
   boolBuf[0] = false;
   tumS_setEnabled_b(&p);
   boolBuf[0] = true;
   tumS_setEnabled_b(&p);
#endif
   UNUSED(tc);
}
