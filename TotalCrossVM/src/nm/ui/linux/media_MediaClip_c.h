/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "../media_MediaClip.h"

typedef struct
{
   Method readMethod;
   Method writeMethod;
   Method setPosMethod;

   WaveHeader waveHeader;
   Object mediaStream;
   Object byteBuffer;
   int32 dataPos;

   Heap h;

} TMediaData, *MediaData;

static bool mediaClipPlay(Context currentContext, Object mediaClip, MediaData media)
{
   return false;
}

static Err mediaClipPause(MediaData media)
{
   return NO_ERROR;
}

static Err mediaClipResume(MediaData media)
{
   return NO_ERROR;
}

static Err mediaClipStop(MediaData media, bool pause)
{
   return NO_ERROR;
}

static Err mediaClipReset(Object mediaClip, MediaData media)
{
   return NO_ERROR;
}

static bool mediaClipClose(Object mediaClip, MediaData media)
{
   return NO_ERROR;
}

static void mediaClipRecord(Context currentContext, Object mediaClip, MediaData media, int32 samplesPerSecond, int32 bitsPerSample, bool stereo)
{
}
