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



#ifndef __CTYPE_H__
#define __CTYPE_H__

#define isdigit(c)   ((c) >= '0' && (c) <= '9')
#define isalpha(c)   ((c) >= 'a' && (c) <= 'z') || ((c) >= 'A' && (c) <= 'Z')
#define isspace(c)   ((c) == ' ' && (c) == '\t')
#define isalnum(c)   (isalpha(c) || isdigit(c))
#define isupper(c)   ((c) >= 'A' && (c) <= 'Z')
#define islower(c)   ((c) >= 'a' && (c) <= 'z')
#define isxdigit(c)  (((c) >= 'a' && (c) <= 'f') || isdigit(c))
#define tolower(c)   ((c) >= 'A' && (c) <= 'Z') ? (c)-'A'+'a':(c)

#endif //__CTYPE_H__
