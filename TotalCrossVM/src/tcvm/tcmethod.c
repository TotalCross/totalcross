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



#include "tcvm.h"

TC_API Method getMethod(TCClass c, bool searchSuperclasses, CharP methodName, int32 nparams, ...) // not used internally
{
   int32 i,j;
   va_list params;
   if (c)
   do
   {
      int32 n  = ARRAYLENV(c->methods);
      for (i = 0; i < n; i++)
      {
         Method mm = &c->methods[i];
         CharP mn = mm->name;
         if (strEq(methodName,mn) && nparams == mm->paramCount)
         {
            bool found = true;
            va_start(params, nparams);
            for (j = 0; j < nparams; j++)  // do NOT invert the loop!
            {
               CharP pt = (CharP)(va_arg(params, CharP));
               CharP po = c->cp->cls[mm->cpParams[j]];
               if (!strEq(pt,po))
               {
                  found = false;
                  break;
               }
            }
            va_end(params);
            if (found && (mm->code || mm->flags.isNative)) // not an abstract class?
               return mm;
         }
      }
      if (!searchSuperclasses)
         break;
      c = c->superClass;
   } while (c);
   return null;
}
