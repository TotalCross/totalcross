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

// $Id: media_Camera_c.h,v 1.6 2011-01-04 13:31:18 guich Exp $

#include "palmOneCameraLibARM.h"
#include "../../io/File.h"

TC_API void tiF_create_sii(NMParams p);
TC_API void tiF_nativeClose(NMParams p);
TC_API void tiF_delete(NMParams p);
extern bool rgb565_2jpeg(Context currentContext, Object srcStreamObj, Object dstStreamObj, int32 width, int32 height);

Err loadCameraLib()
{
   Err err;

   if (gpalmOneCameraLink != null)
      return errNone;

   if ((err = LinkModule(kCamLibType, kCamLibCreator, &gpalmOneCameraLink, NULL)) != errNone ||
       (err = CamLibOpen()) != errNone) // could not open the library
      gpalmOneCameraLink = null;
   return err;
}

static void Camera_initCamera()
{
   if (loadCameraLib() == errNone)
      CamLibControl(kCamLibCtrlPreviewStart, null);
}

static void Camera_stopCamera()
{
   if (gpalmOneCameraLink == null)
      return;

   CamLibClose();
   gpalmOneCameraLink = null;
}

Err CaptureCallback(void *bufP, UInt32 size, void *userDataP)
{
   FileRef* fref = (FileRef*) userDataP;
   VFSFileWrite(*fref, size, bufP, null);

   return errNone;
}

static Err cameraClick(Context currentContext, CharP tempPictureName, CharP picturePath)
{
   CamLibSettingType camSetting;
   CamLibCaptureType captureType;
   FileRef fref;
   Err err;

   Object srcStreamObj, dstStreamObj;
   Object objParams[2];
   Object srcFilePathObj, dstFilePathObj;
   int32 i32Params[2];
   TNMParams fileParams;
   Object thrownException = null;

   VFSFileDelete(1, tempPictureName);
   VFSFileCreate(1, tempPictureName);
   VFSFileOpen(1, tempPictureName, vfsModeReadWrite, &fref);

   camSetting.type = kCamLibCaptureDataFormatRGB565;
   CamLibControl(kCamLibCtrlCaptureFormatSet, (void *) &camSetting);

   // resolution hard coded to 640x480
   camSetting.type = kCamLibImageSizeVGA;
   camSetting.value = kCamLibImageSizeVGA;
   CamLibControl(kCamLibCtrlCaptureSizeSet, &camSetting);

   captureType.userDataP = &fref;
   captureType.callbackP = CaptureCallback;

   if ((err = CamLibControl(kCamLibCtrlPreviewStop, null)) != errNone)
      return err;

   if ((err = CamLibControl(kCamLibCtrlCapture, &captureType)) != errNone)
      return err;

   VFSFileClose(fref);

   Camera_stopCamera();

   // begin convertion of captured image from rgb565 to jpeg.
   srcStreamObj = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.io.File");
   fileParams.i32 = i32Params;
   fileParams.obj = objParams;
   fileParams.obj[0] = srcStreamObj;
   srcFilePathObj = createStringObjectFromCharP(currentContext, tempPictureName, -1);
   File_path(srcStreamObj) = srcFilePathObj;
   File_mode(srcStreamObj) = READ_WRITE;
   File_slot(srcStreamObj) = 1;
   fileParams.obj[1] = srcFilePathObj;
   fileParams.i32[0] = READ_WRITE;
   fileParams.i32[1] = 1;
   fileParams.currentContext = currentContext;
   tiF_create_sii(&fileParams);
   if (fileParams.currentContext->thrownException != null)
      goto finish;

   dstStreamObj = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.io.File");
   fileParams.obj[0] = dstStreamObj;
   dstFilePathObj = createStringObjectFromCharP(currentContext, picturePath, -1);
   File_path(dstStreamObj) = dstFilePathObj;
   File_mode(dstStreamObj) = CREATE_EMPTY;
   File_slot(dstStreamObj) = 1;
   fileParams.obj[1] = dstFilePathObj;
   fileParams.i32[0] = CREATE_EMPTY;
   fileParams.i32[1] = 1;
   fileParams.currentContext = currentContext;
   tiF_create_sii(&fileParams);
   if (fileParams.currentContext->thrownException != null)
   {
      thrownException = currentContext->thrownException;
      // delete the temp file before exiting
      fileParams.obj[0] = srcStreamObj;
      tiF_delete(&fileParams);
      // restore the original exception
      currentContext->thrownException = thrownException;
      goto finish;
   }

   if (!rgb565_2jpeg(currentContext, srcStreamObj, dstStreamObj, 640, 480))
      thrownException = currentContext->thrownException;

   // delete the temp file and close the image before exiting
   fileParams.obj[0] = srcStreamObj;
   tiF_delete(&fileParams);

   fileParams.obj[0] = dstStreamObj;
   tiF_nativeClose(&fileParams);

   // restore the exception thrown by rgb565_2jpeg, if any.
   if (thrownException != null)
      currentContext->thrownException = thrownException;

finish:
   if (srcFilePathObj)
      setObjectLock(srcFilePathObj, UNLOCKED);
   if (dstFilePathObj)
      setObjectLock(dstFilePathObj, UNLOCKED);
   if (srcStreamObj)
      setObjectLock(srcStreamObj, UNLOCKED);
   if (dstStreamObj)
      setObjectLock(dstStreamObj, UNLOCKED);

   return NO_ERROR;
}

static Err Camera_finalize()
{
   if (gpalmOneCameraLink != null)
   {
      CamLibControl(kCamLibCtrlPreviewStop, null);
      Camera_stopCamera();
   }
}
