// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

void privateGetWorkingDir(char* vmPath, char* appPath)
{
#if defined THEOS
   const char* _appPath = [[[NSBundle mainBundle] bundlePath] cStringUsingEncoding:NSASCIIStringEncoding];
   const char* _vmPath = "/Applications/TotalCross.app/";
#else
   const char* _appPath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0] cStringUsingEncoding:NSASCIIStringEncoding];
   const char* _vmPath = [[[NSBundle mainBundle] bundlePath] cStringUsingEncoding:NSASCIIStringEncoding];
#endif

   strcpy(vmPath, _vmPath);
   strcpy(appPath, _appPath);
}

extern void privateFullscreen(bool on);

void setFullScreen()
{
   privateFullscreen(true);
}
