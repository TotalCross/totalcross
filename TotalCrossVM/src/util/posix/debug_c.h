/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

// Debug
#include<unistd.h>

static FILE* fdebug;

static bool privateInitDebug()
{
   return true;
}

void closeDebug()
{
   if (fdebug != NULL)
      fclose(fdebug);
   fdebug = NULL;
}

static void privateDestroyDebug()
{
   if (fdebug)
   {
      fputs("===============\n",fdebug);
      closeDebug();
   }
}

static bool privateDebug(char* str)
{
    bool err = true;
   static char debugPath[MAX_PATHNAME];
   if (!fdebug)
   {
      xstrprintf(debugPath, "%s/DebugConsole.txt", appPath);
      fdebug = fopen(debugPath, "ab+"); //flsobral@tc110: replaced mode "wb" with "ab+".
   }
   if (fdebug)
   {
      if (strEq(str,ERASE_DEBUG_STR))
      {
         closeDebug();
         remove(debugPath);
      }
      else
      {
         fputs(str,fdebug);
         err = (fputs("\n",fdebug) >= 0);
         fflush(fdebug);
         fsync(fileno(fdebug));
      }
   }
    return err;
}

// Alert

#if defined(darwin)
void privateAlert(CharP str);
#else
static void privateAlert(CharP str)
{
   privateDebug(str);
}
#endif
