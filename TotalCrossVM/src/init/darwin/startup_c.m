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

void privateGetWorkingDir(char* vmPath, char* appPath)
{
   const char* path = [[[NSBundle mainBundle] bundlePath] cStringUsingEncoding:NSASCIIStringEncoding];
   strcpy(appPath, path);
#if defined THEOS
   strcpy(vmPath, "/Applications/TotalCross.app/");
#else
   strcpy(vmPath, path);
#endif
}

extern void privateFullscreen(bool on);

void setFullScreen()
{
   privateFullscreen(true);
}
