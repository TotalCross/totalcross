// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TESTCASE(tumMC_play_b) // totalcross/ui/media/MediaClip native public boolean play(boolean wait);
{
#if 1
   TEST_CANNOT_RUN;
#else
#if defined (WIN32)
#if 1
   TNMParams p;
   TCObject obj;

   p.currentContext = currentContext;
   p.obj = &obj;

   //p.obj = createObject("totalcross.ui.media.SoundClip");

   //tumSC_play_b(&p);
   mediaClipPlay(&mediaClipRef, &waveHdr, TEXT("/totalcross/test.wav"), false);
   ///*
   Sleep(1000);
   isPaused = false;
   mediaClipPause(mediaClipRef, isPaused);
   isPaused = true;
   Sleep(3000);
   mediaClipPause(mediaClipRef, isPaused);
   isPaused = false;
   Sleep(1000);
   mediaClipStop(mediaClipRef, &waveHdr);
   //Sleep(1000);
   //soundClipPlay(TEXT("/totalcross/test.wav"), false);

   //soundClipPlay(TEXT("/totalcross/test.wav"), true);
   //*/



   //ASSERT1_EQUALS(True, p.retI);
#endif
   TEST_SKIP;
#endif
#endif
   finish: ;
}
TESTCASE(tumMC_pause_b) // totalcross/ui/media/MediaClip native public boolean pause(boolean enable);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tumMC_stop) // totalcross/ui/media/MediaClip native public boolean stop();
{
   TEST_SKIP;
   finish: ;
}
