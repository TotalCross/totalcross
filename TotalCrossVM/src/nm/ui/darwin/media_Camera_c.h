// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif
    
  int iphone_cameraClick(int w, int h, int t, char* fileName);
    
#ifdef __cplusplus
};
#endif
#endif // darwin


static void cameraClick(NMParams p)
{
#ifdef darwin    
    TCObject cameraObj = p->obj[0];
    TCObject defaultFileName = Camera_defaultFileName(cameraObj);
    char tempPictureName[MAX_PATHNAME];
    char fileName[MAX_PATHNAME];
    IntBuf intBuf;
    
    p->retO = null;
    if (Camera_captureMode(cameraObj) != 0)
        return;

    // destination folder
    xstrcpy(tempPictureName, getAppPath());
    xstrcat(tempPictureName, "/");
    if (defaultFileName == null) // if filename not provided, use a default one
    {
        xstrcat(tempPictureName, "img");
        xstrcat(tempPictureName, getApplicationIdStr());
        xstrcat(tempPictureName, int2str(getTimeStamp(), intBuf));
        xstrcat(tempPictureName, ".jpg");
    }
    else
    {
        String2CharPBuf(defaultFileName, fileName);
        if (xstrchr(fileName, '/')) 
            xstrcpy(tempPictureName, fileName); // if path was passed with the filename, use it.
        else
            xstrcat(tempPictureName, fileName); // else, just the name, append after the path
    }
    
    if (iphone_cameraClick(Camera_resolutionWidth(cameraObj),Camera_resolutionHeight(cameraObj), Camera_cameraType(cameraObj), tempPictureName))
        setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, tempPictureName, -1), UNLOCKED);
#endif
}
