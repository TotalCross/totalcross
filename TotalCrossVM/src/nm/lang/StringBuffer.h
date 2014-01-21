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
      
#ifndef STRINGBUFFER_H
#define STRINGBUFFER_H
                                                                                                                                                                            
TC_API TCObject appendJCharP(Context currentContext, TCObject obj, JCharP srcPtr, int32 len);
typedef TCObject (*appendJCharPFunc)(Context currentContext, TCObject obj, JCharP srcPtr, int32 len);

TC_API TCObject appendCharP(Context currentContext, TCObject obj, CharP srcPtr);
typedef TCObject (*appendCharPFunc)(Context currentContext, TCObject obj, CharP srcPtr);           
            
#endif
