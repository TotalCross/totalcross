/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include <PalmOS.h>											// Standard Palm stuff
#include <PalmCompatibility.h>								// use new headers
#include <VfsMgr.h>
#include <DLServer.h>

static char* int2CRID(UInt32 i, char* crid)
{
   crid[0] = (char)((i >> 24) & 0xFF);
   crid[1] = (char)((i >> 16) & 0xFF);
   crid[2] = (char)((i >> 8 ) & 0xFF);
   crid[3] = (char)((i      ) & 0xFF);
   crid[4] = 0;
   return crid;
}

static void tone(int freq, int dur)
{
	SndCommandType sndCmd;
	sndCmd.cmd = sndCmdFreqDurationAmp;
	sndCmd.param1 = freq; // freq
	sndCmd.param2 = dur; // duration
	sndCmd.param3 = sndMaxAmp;
	SndDoCmd(NULL, &sndCmd, 0);
}

static void eraseDebug()
{
   VFSFileDelete(1,"/DebugConsole");
}
#define NOINT -999999
static void debug(char *s, Int32 i)
{
   FileRef debugFH=0;
   Err lastErr;
   lastErr = VFSFileOpen(1,"/DebugConsole", vfsModeReadWrite|vfsModeCreate, &debugFH);
   if (lastErr == errNone)
   {
      char buf[20];
      VFSFileSeek(debugFH, vfsOriginEnd, 0);     // and seek to end
      VFSFileWrite(debugFH, StrLen(s), s, 0);
      if (i != NOINT)
      {
         VFSFileWrite(debugFH, 2, ": ", 0);
         StrIToA(buf, i);
         VFSFileWrite(debugFH, StrLen(buf), buf, 0);
      }
      VFSFileWrite(debugFH, 1, "\n", 0);
      VFSFileClose(debugFH);
   }
   else
      tone(300, 1000);
}
#define IPC_BUFSIZE 65400 // MUST BE THE SAME OF Conduit.c !

static UInt32 handleConduitCommands(MemPtr cmdPBP, UInt16 launchFlags)
{
 	SysAppLaunchCmdHandleSyncCallAppType *command = (SysAppLaunchCmdHandleSyncCallAppType*)cmdPBP;
   DlkCallAppReplyParamType reply;
 	char* buf; // must be the same of IPC_BUFSIZE of Conduit.c !
 	char* bufPtr;
 	char buf2[256];
 	void* param = command->paramP;
   Err err = errNone;

 	bufPtr = buf = (char*)MemPtrNew(IPC_BUFSIZE);
   MemSet(&reply, sizeof(DlkCallAppReplyParamType), 0);
   reply.pbSize = sizeof(DlkCallAppReplyParamType);
   reply.dlRefP = command->dlRefP;
   reply.dwResultCode = 0;
   reply.resultP = buf;
   command->handled = true;

   if (!buf)
   {
      err = sysErrNoFreeRAM;
      reply.dwResultCode = -1; // not enough memory
   }
   else
   {
   	switch (command->action)
   	{
   		case 1: // list volumes
   		{
   		   UInt16 volRefNum = 0;
   		   UInt32 volIterator = vfsIteratorStart | 0x80000000;
   		   int volCount = 0;
            eraseDebug();

            // seach for all (possibly) installed volumes
            while (volIterator != vfsIteratorStop)
            {
               if ((err = VFSVolumeEnumerate(&volRefNum, &volIterator)) != errNone)
               {
                  reply.dwResultCode = -100;
                  break;
               }
               else
               {
                  if ((err = VFSVolumeGetLabel(volRefNum, buf2, sizeof(buf2))) == vfsErrNameShortened)
                     err = errNone;
                  if (err != errNone)
                  {
                     reply.dwResultCode = -101;
                     break;
                  }
                  else
                  {
                     if (StrNCompare(buf2, "BUILTIN", 7) == 0) // use a standard name
                        StrCopy(buf2, "builtin");
                     StrPrintF(bufPtr, "%d:%s", volRefNum, buf2);
                     bufPtr += StrLen(bufPtr) + 1;
                     volCount++;
                  }
               }
            }

            if (err == errNone)
               reply.dwResultCode = volCount;
            break;
         }
   	   case 2: // list files in a directory
   	   {
   	      int fileCount = 0;
   	      char *dir = (char*) param;
            // expected format: 0:/dir
            UInt16 vol = dir[0]-'0';
            char fileNameBuf[255];
            FileRef dirRef;
            FileInfoType fileInfo;
            fileInfo.nameP = fileNameBuf;

            if ((err = VFSFileOpen(vol, dir+2, vfsModeRead, &dirRef)) != errNone)
               reply.dwResultCode = -110;
            else
            {
               UInt32 dirEntryIterator = expIteratorStart;
               while (dirEntryIterator != expIteratorStop)
               {
                  fileInfo.nameBufLen = 255; // each pass sets the returned name size, so we must set the len back to the real
                  if ((err = VFSDirEntryEnumerate(dirRef, &dirEntryIterator, &fileInfo)) != errNone)
                  {
                     reply.dwResultCode = -111;
                     break;
                  }
                  else
                  if (fileInfo.attributes & vfsFileAttrDirectory) // if its a directory, end it with /
                     StrPrintF(bufPtr, "%s/", fileInfo.nameP);
                  else
                     StrPrintF(bufPtr, "%s", fileInfo.nameP);
                  bufPtr += StrLen(bufPtr) + 1;
                  fileCount++;
               }
               VFSFileClose(dirRef);
            }
            if (err == errNone)
               reply.dwResultCode = fileCount;
            break;
   	   }
   	   case 3: // delete a file
   	   {
            char *dirOrFile = (char*) param;
            UInt16 vol = dirOrFile[0] - '0';

            if ((err = VFSFileDelete(vol, dirOrFile + 2)) == errNone)
               reply.dwResultCode = -120;
            break;
   	   }
   	   case 4: // open file F at position I and read
   	   {
            FileRef srcRef;
   	      UInt32 numberBytesRead = 0;
   	      UInt32 size;

            // read the filename and the offset
            char* srcFile = (char*) param;
            UInt32 offset = StrAToI(srcFile + StrLen(srcFile) + 1);
            // get the volume
            UInt16 vol = srcFile[0] - '0';

            // skip volume info
            srcFile += 2;
       	   if ((err = VFSFileOpen(vol, srcFile, vfsModeRead, &srcRef)) != errNone)
       	      reply.dwResultCode = -130;
       	   else
       	   {
               if ((err = VFSFileSize(srcRef, &size)) != errNone)
                  reply.dwResultCode = -131;
               else
               {
                  if (size > 0 && offset < size)  // if file is 0, just close
                  {
             	      err = VFSFileSeek(srcRef, vfsOriginBeginning, offset);
             	      if (err == errNone)
             	      {
                        if ((err = VFSFileRead(srcRef, (size - offset) > IPC_BUFSIZE ? IPC_BUFSIZE : (size - offset), bufPtr, &numberBytesRead)) == vfsErrFileEOF)
                           reply.dwResultCode = -133;
             	      }
             	      else
             	      if (err == vfsErrFileEOF)
             	         reply.dwResultCode = -132;
                  }
               }
               // no error?
               if (err == errNone)
               {
                  reply.dwResultCode = size;
                  bufPtr = buf + numberBytesRead; // update the ptr so that the size can be set accordingly, below
               }
               VFSFileClose(srcRef);
       	   }
   	      break;
   	   }
   	   case 5: // open file F at position I and write
   	   {
            UInt16 mode = vfsModeWrite;
            FileRef dstRef;
            UInt32 numberBytesWritten;
            char* p;

            // read filename, offset and data
            char* dstFile = (char*) param;
            char* offsetP = dstFile + StrLen(dstFile) + 1;
            char* toWrite = offsetP + StrLen(offsetP) + 1;
            UInt32 offset = StrAToI(offsetP);
            UInt32 toWriteLen = command->dwParamSize - (toWrite - dstFile);

            // get the volume
            UInt16 vol = dstFile[0] - '0';
            // skip volume info
            dstFile += 2;

            if (offset == 0)
            {
               VFSFileDelete(vol, dstFile); // guich@tc101_46: delete the file by hand, because vfsModeTruncate fails at Tungsten E2
               mode |= vfsModeCreate;
            }
            if ((err = VFSFileOpen(vol, dstFile, mode, &dstRef)) != errNone && offset == 0)
            {
               // recursively create the folders.
               p = dstFile;
               while ((p = StrChr(p+1, '/')) != 0)
               {
                  *p = 0;
                  VFSDirCreate(vol, dstFile);
                  *p = '/';
               }
               // now try to create it again
               err = VFSFileOpen(vol, dstFile, mode, &dstRef);
            }
            if (err != errNone)
               reply.dwResultCode = -err;
            else
            {
               int temp;
               VFSFileResize(dstRef, offset + toWriteLen);
       	      VFSFileSeek(dstRef, vfsOriginBeginning, offset);
               if ((err = VFSFileWrite(dstRef, toWriteLen, toWrite, &numberBytesWritten)) != errNone)
                  reply.dwResultCode = -141;
               VFSFileClose(dstRef);
            }
            break;
   	   }
   	}
      reply.dwResultSize = bufPtr-buf;
   }
   command->replyErr = DlkControl(dlkCtlSendCallAppReply,&reply, NULL);
   if (buf)
      MemPtrFree(buf);
   return err;
}

DWord PilotMain(Word cmd, Ptr cmdline, Word launchFlags)
{
   if (cmd == sysAppLaunchCmdHandleSyncCallApp)
   {
       return handleConduitCommands(cmdline, launchFlags);
   }
   return 0;
}
