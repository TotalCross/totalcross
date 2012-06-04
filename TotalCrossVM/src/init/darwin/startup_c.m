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

void privateGetWorkingDir(char* vmPath)
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
