// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <MMSystem.h>

typedef struct
{
   WAVEHDR* headers;
   int8* blocks;
   int32 blockCount;
   int32 blockSize;
   int32 currentBlock;
} TWaveBuffer, *WaveBuffer;

typedef struct
{
   HWAVEIN  waveIN;
   HWAVEOUT waveOUT;
   
   HANDLE evtDone;

   HANDLE hThread;

   Method readMethod;
   Method writeMethod;
   Method setPosMethod;

   WaveHeader waveHeader;
   TCObject mediaStream;
   TCObject byteBuffer;

   Context currentContext;
   int32 dataPos;

   bool stop;

   TWaveBuffer waveBuffer;
   TWaveBuffer waveInBuffer;
} TMediaData, *MediaData;

DWORD WINAPI ThreadProc(LPVOID lpParameter);
DWORD WINAPI ThreadInProc(LPVOID lpParameter);

static bool mediaClipPlay(Context currentContext, TCObject mediaClip, MediaData media)
{
   WAVEFORMATEX waveFormat;
   bool ret = false;
   Err err;
   int32 i;

   if (waveOutGetNumDevs() < 1)
      return false;

   /*
    * initialise the module variables
    */
   media->waveBuffer.blockCount = 10;
   media->waveBuffer.blockSize = media->waveHeader->BlockAlign * 1024;
   media->byteBuffer = createByteArray(currentContext, (media->waveBuffer.blockSize + sizeof(WAVEHDR)) * media->waveBuffer.blockCount);
   media->waveBuffer.blocks =  ARRAYOBJ_START(media->byteBuffer);
   media->waveBuffer.headers = (WAVEHDR*) (ARRAYOBJ_START(media->byteBuffer) + media->waveBuffer.blockSize * media->waveBuffer.blockCount);
   for (i = media->waveBuffer.blockCount - 1; i >= 0 ; i--)
   {
      media->waveBuffer.headers[i].dwBufferLength = media->waveBuffer.blockSize;
      media->waveBuffer.headers[i].dwFlags = 0;
      media->waveBuffer.headers[i].lpData = media->waveBuffer.blocks + media->waveBuffer.blockSize * i;
   }
   media->currentContext = currentContext;
   media->mediaStream = mediaClip;

   waveFormat.wFormatTag        = media->waveHeader->AudioFormat;
   waveFormat.wBitsPerSample    = media->waveHeader->BitsPerSample;
   waveFormat.nChannels         = media->waveHeader->NumChannels;
   waveFormat.nSamplesPerSec    = media->waveHeader->SampleRate;
   waveFormat.nBlockAlign       = media->waveHeader->BlockAlign;
   waveFormat.nAvgBytesPerSec   = media->waveHeader->ByteRate;
   waveFormat.cbSize            = 0;

   media->evtDone = CreateEvent(null, false, false, null);

   if ((err = waveOutOpen(&media->waveOUT, WAVE_MAPPER, (LPWAVEFORMATEX) &waveFormat, (DWORD) media->evtDone, (DWORD) media, CALLBACK_EVENT)) != MMSYSERR_NOERROR)
      goto end;

   media->hThread = CreateThread(null, 0, ThreadProc, media, 0, null);

   return NO_ERROR;
end:
   return ret;
}

static Err mediaClipPause(MediaData media)
{
   return waveOutPause(media->waveOUT);
}


static Err mediaClipResume(MediaData media)
{
   return waveOutRestart(media->waveOUT);
}

static Err mediaClipStop(MediaData media, bool pause)
{
   Err err;

   if (pause)
      err = waveOutPause(media->waveOUT);
   else
      err = waveOutRestart(media->waveOUT);

   return err;
}

static Err mediaClipReset(TCObject mediaClip, MediaData media)
{
   waveOutReset(media->waveOUT);
   executeMethod(media->currentContext, media->setPosMethod, MediaClip_mediaClipStream(mediaClip), media->dataPos);
   ResumeThread(media->hThread);
   return NO_ERROR;
}

static bool mediaClipClose(TCObject mediaClip, MediaData media)
{
   int32 dataSize;
   Err err;

   if (!MediaClip_isRecording(mediaClip))
   {
      media->stop = true;
   
      if ((err = waveOutReset(media->waveOUT)) != MMSYSERR_NOERROR)
         return false;
      ResumeThread(media->hThread);
      WaitForSingleObject(media->hThread, INFINITE);
      if ((err = waveOutClose(media->waveOUT)) != MMSYSERR_NOERROR)
         return false;
      CloseHandle(media->evtDone);
   }
   else
   {
      media->stop = true;
      waveInStop(media->waveIN);
      WaitForSingleObject(media->hThread, INFINITE);
      err = waveInClose(media->waveIN);
      CloseHandle(media->evtDone);
      dataSize = media->waveHeader->Subchunk2Size;
      media->waveHeader->ChunkSize = dataSize + 38;
      executeMethod(media->currentContext, media->setPosMethod, MediaClip_mediaClipStream(mediaClip), 0);
      executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), MediaClip_mediaHeader(mediaClip), 0, 46);
   }

   return true;
}

////////////////// SOUND RECORDING SUPPORT ////////////////////

static char* getMMErrorMessage(int32 res)
{
	switch (res)
	{
	   case MMSYSERR_ALLOCATED:    return "Specified resource is already allocated";
	   case MMSYSERR_BADDEVICEID:  return "Specified device identifier is out of range";
	   case MMSYSERR_NODRIVER:     return "No device driver is present";
	   case MMSYSERR_NOMEM:        return "Unable to allocate or lock memory";
	   case WAVERR_BADFORMAT:      return "Attempted to open with an unsupported waveform-audio format";
	   case WAVERR_UNPREPARED:     return "The buffer pointed to by the wave header parameter hasn't been prepared";
	   case WAVERR_SYNC:           return "The device is synchronous but waveOutOpen was called without using the WAVE_ALLOWSYNC flag";
	   case WAVERR_STILLPLAYING:   return "The buffer pointed to by the wave header parameter is still in the queue";
	   case MMSYSERR_NOTSUPPORTED: return "Specified device is synchronous and does not support pausing";
      case 999:                   return "Not enough memory to create recording buffer";
	   default: return null;
	}
}

static void throwMMException(Context currentContext, uint32 res)
{
   char* msg = getMMErrorMessage(res);
   if (msg == null)
      throwExceptionWithCode(currentContext, IOException, res);
   else
      throwException(currentContext, IOException, msg);
}

static void mediaClipRecord(Context currentContext, TCObject mediaClip, MediaData media, int32 samplesPerSecond, int32 bitsPerSample, bool stereo)
{
   WAVEFORMATEX waveFormat;
   uint16 nChannels = stereo ? 2 : 1;
   Err err;
   int32 i;

   media->currentContext = currentContext;
   media->mediaStream = mediaClip;

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

   tzero(waveFormat);
   waveFormat.wFormatTag = WAVE_FORMAT_PCM;
   waveFormat.nChannels = media->waveHeader->NumChannels;
   waveFormat.nSamplesPerSec = samplesPerSecond;
   waveFormat.wBitsPerSample = bitsPerSample;
   waveFormat.nBlockAlign = media->waveHeader->BlockAlign;
   waveFormat.nAvgBytesPerSec = bitsPerSample * samplesPerSecond;

   //flsobral@tc112_17: The section below was using two different buffers, waveBuffer and waveInBuffer.
   media->waveInBuffer.blockCount = 5;
   media->waveInBuffer.blockSize = media->waveHeader->BlockAlign * 1024;
   media->byteBuffer = createByteArray(currentContext, (media->waveInBuffer.blockSize + sizeof(WAVEHDR)) * media->waveInBuffer.blockCount);
   media->waveInBuffer.blocks = ARRAYOBJ_START(media->byteBuffer);
   media->waveInBuffer.headers = (WAVEHDR*) (ARRAYOBJ_START(media->byteBuffer) + media->waveInBuffer.blockSize * media->waveInBuffer.blockCount);

   media->evtDone = CreateEvent(null, false, false, null);

   executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(mediaClip), MediaClip_mediaHeader(mediaClip), 0, 46);

   if ((err = waveInOpen(&media->waveIN, WAVE_MAPPER, (LPWAVEFORMATEX) &waveFormat, (DWORD) media->evtDone, (DWORD) media, CALLBACK_EVENT)) != MMSYSERR_NOERROR)
      goto error;

   for (i = media->waveInBuffer.blockCount - 1; i >= 0 ; i--)
   {
      media->waveInBuffer.headers[i].dwBufferLength = media->waveInBuffer.blockSize;
      media->waveInBuffer.headers[i].dwFlags = 0;
      media->waveInBuffer.headers[i].lpData = media->waveInBuffer.blocks + media->waveInBuffer.blockSize * i;
      err = waveInPrepareHeader(media->waveIN, &media->waveInBuffer.headers[i], sizeof(WAVEHDR));
   }
   err = waveInAddBuffer(media->waveIN, &media->waveInBuffer.headers[0], sizeof(WAVEHDR));

   media->hThread = CreateThread(null, 0, ThreadInProc, media, 0, null);
    
   err = waveInStart(media->waveIN);

   return;// NO_ERROR;

#if 0

   err = waveInOpen(&media->waveIN, (UINT)WAVE_MAPPER, &PCMfmt, (DWORD)SoundWaveInProc, (DWORD)params, CALLBACK_FUNCTION);
   if (err != MMSYSERR_NOERROR)
      goto error;

   err = waveInStart(media->waveIN);
   if (res != MMSYSERR_NOERROR)
   {
      waveInClose(media->waveIN);
      goto error;
   }
#endif
   return;
error:
   //xfree(params);
   throwMMException(currentContext, err);
}

DWORD WINAPI ThreadProc(LPVOID lpParameter)
{
   MediaData media = (MediaData) lpParameter;
   int32 bytesRead;
   WAVEHDR* current;
   Err err;
   int8* buf;

start:
   while (!media->stop)
   {
      current = &media->waveBuffer.headers[media->waveBuffer.currentBlock];
      if (current->dwFlags != 0)
      {
         while (!(current->dwFlags & WHDR_DONE))
            WaitForSingleObject(media->evtDone, INFINITE);
         waveOutUnprepareHeader(media->waveOUT, current, sizeof(WAVEHDR));
      }

      buf = current->lpData;
      bytesRead = executeMethod(media->currentContext, media->readMethod, MediaClip_mediaClipStream(media->mediaStream), media->byteBuffer, media->waveBuffer.blockSize * media->waveBuffer.currentBlock, media->waveBuffer.blockSize).asInt32;
      if (bytesRead <= 0)
      {
         // post end of media and update state
         MediaClip_state(media->mediaStream) = PREFETCHED;
         MediaClip_internalState(media->mediaStream) = mediaFinished;
         postEvent(media->currentContext, MEDIACLIPEVENT_END_OF_MEDIA, 0, 0, 0, 0);
         SuspendThread(media->hThread);
         goto start;
      }
      if (bytesRead < media->waveBuffer.blockSize) 
         memset(buf + bytesRead, 0, media->waveBuffer.blockSize - bytesRead);

      current->dwBufferLength = bytesRead;
      err = waveOutPrepareHeader(media->waveOUT, current, sizeof(WAVEHDR));
      err = waveOutWrite(media->waveOUT, current, sizeof(WAVEHDR));

      /*
      * point to the next block
      */
      media->waveBuffer.currentBlock++;
      media->waveBuffer.currentBlock %= media->waveBuffer.blockCount;
   }

   return 0;
}

DWORD WINAPI ThreadInProc(LPVOID lpParameter)
{
   MediaData media = (MediaData) lpParameter;
   int32 bytesRead, currentBlock;
   WAVEHDR* current;
   WAVEHDR* next;
   Err err;

   while (!media->stop)
   {
      WaitForSingleObject(media->evtDone, INFINITE);
      current = &media->waveInBuffer.headers[media->waveInBuffer.currentBlock];
      currentBlock = media->waveInBuffer.currentBlock;
      /*
       * point to the next block
       */
      media->waveInBuffer.currentBlock++;
      media->waveInBuffer.currentBlock %= media->waveInBuffer.blockCount;
      next = &media->waveInBuffer.headers[media->waveInBuffer.currentBlock];
      err = waveInPrepareHeader(media->waveIN, next, sizeof(WAVEHDR));
      waveInAddBuffer(media->waveIN, next, sizeof(WAVEHDR));

      bytesRead = executeMethod(media->currentContext, media->writeMethod, MediaClip_mediaClipStream(media->mediaStream), media->byteBuffer, media->waveInBuffer.blockSize * currentBlock, current->dwBytesRecorded).asInt32;
      media->waveHeader->Subchunk2Size += bytesRead;
   }

   return 0;
}
