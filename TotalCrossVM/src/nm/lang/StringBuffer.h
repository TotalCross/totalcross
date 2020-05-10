// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
      
#ifndef STRINGBUFFER_H
#define STRINGBUFFER_H
                                                                                                                                                                            
TC_API TCObject appendJCharP(Context currentContext, TCObject obj, JCharP srcPtr, int32 len);
typedef TCObject (*appendJCharPFunc)(Context currentContext, TCObject obj, JCharP srcPtr, int32 len);

TC_API TCObject appendCharP(Context currentContext, TCObject obj, CharP srcPtr);
typedef TCObject (*appendCharPFunc)(Context currentContext, TCObject obj, CharP srcPtr);           
            
#endif
