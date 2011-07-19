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
      
#ifndef STRINGBUFFER_H
#define STRINGBUFFER_H
                                                                                                                                                                            
TC_API Object appendJCharP(Context currentContext, Object obj, JCharP srcPtr, int32 len);
typedef Object (*appendJCharPFunc)(Context currentContext, Object obj, JCharP srcPtr, int32 len);

TC_API Object appendCharP(Context currentContext, Object obj, CharP srcPtr);
typedef Object (*appendCharPFunc)(Context currentContext, Object obj, CharP srcPtr);           
            
#endif
