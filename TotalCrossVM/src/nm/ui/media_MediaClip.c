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

#if defined WP8

#elif defined (WIN32) || defined (WINCE)
 #include "win\media_MediaClip_c.h"
#elif defined(darwin)
 #include "darwin/media_MediaClip_c.h"
#elif defined (linux) || defined(ANDROID)
 #include "linux/media_MediaClip_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_create(NMParams p) // totalcross/ui/media/MediaClip native private void create();
{
	//XXX don't know what to do with this function for WP8 yet...
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   TCObject mediaClipRef;
   TCObject mediaStream = MediaClip_mediaClipStream(mediaClip);
   MediaData media;

   if ((mediaClipRef = createByteArray(p->currentContext, sizeof(TMediaData))) != null)
   {
      media = (MediaData) ARRAYOBJ_START(mediaClipRef);
         media->readMethod    = getMethod((TCClass) OBJ_CLASS(mediaStream), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
         media->writeMethod   = getMethod((TCClass) OBJ_CLASS(mediaStream), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);
         media->setPosMethod  = getMethod((TCClass) OBJ_CLASS(mediaStream), true, "setPos", 1, J_INT);

         media->waveHeader = (WaveHeader) ARRAYOBJ_START(MediaClip_mediaHeader(mediaClip));
         media->mediaStream = mediaStream;

         MediaClip_mediaClipRef(mediaClip) = mediaClipRef;
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_nativeStart(NMParams p) // totalcross/ui/media/MediaClip native private void nativeStart();
{
	// XXX
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   TCObject mediaClipData = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipData);
   int32 currentState = MediaClip_state(mediaClip);
   TCObject mediaStream = MediaClip_mediaClipStream(mediaClip);
   Context currentContext = p->currentContext;

   if (currentState == PREFETCHED || currentState == UNREALIZED)
      postEvent(p->currentContext, MEDIACLIPEVENT_STARTED, 0, 0, 0, 0);
   if (currentState == PREFETCHED)
   {
      if (MediaClip_internalState(mediaClip) == mediaPaused)
         mediaClipResume(media);
      else if (MediaClip_internalState(mediaClip) == mediaFinished)
         mediaClipReset(mediaClip, media);
   }
   else if (currentState == UNREALIZED)
   {
      if (mediaStream != null)
      {
         media->dataPos = MediaClip_dataPos(mediaClip);
         p->retI = mediaClipPlay(currentContext, mediaClip, media);
      }
   }
   MediaClip_internalState(mediaClip) = mediaStarted;
   MediaClip_state(mediaClip) = STARTED;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_stop(NMParams p) // totalcross/ui/media/MediaClip native public void stop();
{
	// XXX
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   TCObject mediaClipData = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipData);
   Err err;

   if (MediaClip_isRecording(mediaClip))
   {
      MediaClip_stopped(mediaClip) = true;
   }
   else
   if (MediaClip_state(mediaClip) == STARTED)
   {
      if ((err = mediaClipPause(media)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, RuntimeException, err);
      else
      {
         MediaClip_internalState(mediaClip) = mediaPaused;
         MediaClip_state(mediaClip) = PREFETCHED;
         postEvent(p->currentContext, MEDIACLIPEVENT_STOPPED, 0, 0, 0, 0);
      }
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_reset(NMParams p) // totalcross/ui/media/MediaClip native public void reset();
{
	// XXX
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   TCObject mediaClipRef = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipRef);

   if (MediaClip_state(mediaClip) != UNREALIZED)
   {
      mediaClipReset(mediaClip, media);
      MediaClip_state(mediaClip) = PREFETCHED;
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_nativeClose(NMParams p) // totalcross/ui/media/MediaClip native private void nativeClose();
{
	// XXX
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   TCObject mediaClipRef = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipRef);

   mediaClipClose(mediaClip, media);

   MediaClip_state(mediaClip) = CLOSED;
   postEvent(p->currentContext, MEDIACLIPEVENT_CLOSED, 0, 0, 0, 0);
   MediaClip_stopped(mediaClip) = true;
   setObjectLock(media->byteBuffer, UNLOCKED);
   setObjectLock(mediaClipRef, UNLOCKED);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumMC_record_iib(NMParams p) // totalcross/ui/media/MediaClip native public void record(int samplesPerSecond, int bitsPerSample, boolean stereo);
{
	// XXX
#if !defined WP8
   TCObject mediaClip = p->obj[0];
   int32 samplesPerSecond = p->i32[0];
   int32 bitsPerSample = p->i32[1];
   bool stereo = p->i32[2];
   TCObject mediaClipData = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipData);

   if (samplesPerSecond != 8000 && samplesPerSecond != 11025 && samplesPerSecond != 22050 && samplesPerSecond != 44100)
      throwIllegalArgumentException(p->currentContext, "samplesPerSecond");
   else if (bitsPerSample != 8 && bitsPerSample != 16)
      throwIllegalArgumentException(p->currentContext, "bitsPerSample");
   else
   {
      MediaClip_isRecording(mediaClip) = true;
      mediaClipRecord(p->currentContext, mediaClip, media, samplesPerSecond, bitsPerSample, stereo);
   }
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "media_MediaClip_test.h"
#endif
