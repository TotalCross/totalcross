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

// $Id: debug_c.h,v 1.17 2011-01-04 13:31:15 guich Exp $

// Debug

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
   static char debugPath[MAX_PATHNAME];
   bool err = true;
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
