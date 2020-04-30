// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "media_MediaClip.h"

typedef struct
{
   Method readMethod;
   Method writeMethod;
   Method setPosMethod;

   WaveHeader waveHeader;
   TCObject mediaStream;
   TCObject byteBuffer;
   int32 dataPos;

   Heap h;

} TMediaData, *MediaData;

static bool mediaClipPlay(Context currentContext, TCObject mediaClip, MediaData media)
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

/*static Err mediaClipStop(MediaData media, bool pause)
{
   return NO_ERROR;
}*/

static Err mediaClipReset(TCObject mediaClip, MediaData media)
{
   return NO_ERROR;
}

static bool mediaClipClose(TCObject mediaClip, MediaData media)
{
   return NO_ERROR;
}

static void mediaClipRecord(Context currentContext, TCObject mediaClip, MediaData media, int32 samplesPerSecond, int32 bitsPerSample, bool stereo)
{
}
