/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: media_MediaClip_c.h,v 1.23 2011-03-21 20:07:25 guich Exp $

static Err streamOutCallback (void *userData, SndStreamRef stream, void *buffer, UInt32 frameCount);
static Err streamInCallback  (void *userData, SndStreamRef stream, void *buffer, UInt32 frameCount);

typedef struct
{
   SndStreamRef mediaClipHandle;

   FileRef waveFile;
   WaveHeader waveHeader;
   int32 dataSize;
   int32 waveSize;
   int32 dataPos;

   Method readMethod;
   Method writeMethod;
   Method setPosMethod;

   Object byteBuffer;
   Object mediaStream;

   Context currentContext;

   uint32 got;

} TMediaData, *MediaData;

static bool mediaClipPlay(Context currentContext, Object mediaClip, MediaData media)
{
   Err err;
   register uint32 got asm("r10");

   /* Check if the device is capable of playing wave
   err = FtrGet(sysFileCSoundMgr, sndFtrIDVersion, NULL);
   if (err != errNone)
      return false;
   */

   media->byteBuffer = createByteArray(currentContext, 4096); //1024 * media->waveHeader->BlockAlign);
   media->currentContext = currentContext;
   media->got = got;

   err = SndStreamCreate(&media->mediaClipHandle, sndOutput, media->waveHeader->SampleRate, media->waveHeader->BitsPerSample != 16 ? sndUInt8 : sndInt16Little, media->waveHeader->NumChannels == 1 ? sndMono : sndStereo, streamOutCallback, mediaClip, 1024, true);
   err = SndStreamSetVolume(media->mediaClipHandle, sndGameVolume);
   err = SndStreamSetPan(media->mediaClipHandle, 0);
   err = SndStreamStart(media->mediaClipHandle);

   return NO_ERROR;
}

static Err mediaClipPause(MediaData media)
{
   return SndStreamPause(media->mediaClipHandle, true);
}

static Err mediaClipResume(MediaData media)
{
   return SndStreamPause(media->mediaClipHandle, false);
}

static Err mediaClipReset(Object mediaClip, MediaData media)
{
   executeMethod(media->currentContext, media->setPosMethod, MediaClip_mediaClipStream(mediaClip), media->dataPos);
   return SndStreamStart(media->mediaClipHandle);
}

static bool mediaClipClose(Object mediaClip, MediaData media)
{
   uint32 dataSize;
   UInt32 bytesWritten;
   FileRef* fref;
   Err err;

   err = SndStreamStop(media->mediaClipHandle);
   if (MediaClip_isRecording(mediaClip) == true)
   {
      dataSize = media->waveHeader->Subchunk2Size;
      media->waveHeader->ChunkSize = dataSize + 38;
      executeMethod(media->currentContext, media->setPosMethod, MediaClip_mediaClipStream(mediaClip), 0);
      executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), MediaClip_mediaHeader(mediaClip), 0, 42);

      fref = (FileRef*) ARRAYOBJ_START(File_fileRef(MediaClip_mediaClipStream(mediaClip)));
      VFSFileWrite(*fref, sizeof(uint32), &dataSize, &bytesWritten);
      //executeMethod(media->currentContext, media->setPosMethod, MediaClip_mediaClipStream(mediaClip), 42);
      //executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), MediaClip_mediaHeader(mediaClip), 42, 4);

      MediaClip_isRecording(mediaClip) = false;
   }

   return true;
}

static void mediaClipRecord(Context currentContext, Object mediaClip, MediaData media, int32 samplesPerSecond, int32 bitsPerSample, bool stereo)
{
   Err err;
   register uint32 got asm("r10");

   if (!strEq(OBJ_CLASS(mediaClip)->name,"totalcross.io.File"))
   {
      throwException(currentContext, IOException, "Palm OS supports recording only to a File.");
      return;
   }
   
   if (samplesPerSecond >= 22050 && bitsPerSample > 8)
      stereo = false;
   if (samplesPerSecond == 44100)
      bitsPerSample = 8;

   media->currentContext = currentContext;

   xmemmove(media->waveHeader, "RIFF    WAVEfmt                       data", 42);
   media->waveHeader->ChunkSize = 38;
   media->waveHeader->Subchunk1Size = 18; // PCM + empty ExtraParamSize
   media->waveHeader->AudioFormat = 1;
   media->waveHeader->NumChannels = stereo ? 2 : 1;
   media->waveHeader->SampleRate = samplesPerSecond;
   media->waveHeader->BitsPerSample = bitsPerSample;
   media->waveHeader->BlockAlign = media->waveHeader->NumChannels * bitsPerSample / 8;
   media->waveHeader->ByteRate = samplesPerSecond * media->waveHeader->BlockAlign;

   media->waveHeader->ExtraParamSize = 0;
   media->waveHeader->Subchunk2Size = 0;

   executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), MediaClip_mediaHeader(mediaClip), 0, 46);

   media->byteBuffer = createByteArray(media->currentContext, 4096); // frame should never exceed 4096 bytes
   media->got = got;

   if ((err = SndStreamCreate(&media->mediaClipHandle, sndInput, samplesPerSecond, (bitsPerSample == 8 ? sndInt8 : sndInt16), (stereo ? sndStereo : sndMono), streamInCallback, mediaClip, 1024, true)) != errNone)
      return;
   err = SndStreamStart(media->mediaClipHandle);
}

static Err streamOutCallback (void *userData, SndStreamRef stream, void *buffer, UInt32 frameCount)
{
   Object mediaClip = (Object) userData;
   Object mediaClipData = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipData);
   int32 bufferSize = frameCount * media->waveHeader->BlockAlign;
   int32 bytesRead;
   register uint32 got asm("r10");
   got = media->got;
   UNUSED(stream);

   if (!destroyingApplication)
   {
      bytesRead = executeMethod(media->currentContext, media->readMethod, MediaClip_mediaClipStream(mediaClip), media->byteBuffer, 0, bufferSize).asInt32;
      if (bytesRead <= 0)
      {
         SndStreamStop(stream);
         MediaClip_internalState(mediaClip) = mediaFinished;
         MediaClip_state(mediaClip) = PREFETCHED;
         postEvent(media->currentContext, MEDIACLIPEVENT_END_OF_MEDIA, 0, 0, 0, 0);
      }
      else
         xmemmove(buffer, ARRAYOBJ_START(media->byteBuffer), bytesRead);
   }

   return errNone;
}

static Err streamInCallback(void *userData, SndStreamRef stream, void *buffer, UInt32 frameCount)
{
   Object mediaClip = (Object) userData;
   Object mediaClipData = MediaClip_mediaClipRef(mediaClip);
   MediaData media = (MediaData) ARRAYOBJ_START(mediaClipData);
   FileRef* fref = (FileRef*) ARRAYOBJ_START(File_fileRef(MediaClip_mediaClipStream(mediaClip)));
   int32 bytesReceived = frameCount;
   int32 bufferSize = ARRAYOBJ_LEN(media->byteBuffer);
   int32 bytesWritten = 0;
   int32 toWrite = 0;
   register uint32 got asm("r10");
   got = media->got;
   UNUSED(stream);

   if (!destroyingApplication)
   {
      while (bytesReceived > 0)
      {
         toWrite = (bufferSize > bytesReceived ? bytesReceived : bufferSize);
         VFSFileWrite(*fref, (UInt32) toWrite, buffer, (UInt32*) &bytesWritten);
         //xmemmove(ARRAYOBJ_START(media->byteBuffer), buffer, toWrite);
         //bytesWritten = executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), media->byteBuffer, 0, toWrite).asInt32;
         media->waveHeader->Subchunk2Size += bytesWritten;
         bytesReceived -= toWrite;
      }
   }
   return errNone;
}
