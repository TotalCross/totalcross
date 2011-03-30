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

// $Id: logs.h,v 1.7 2011-01-04 13:31:15 guich Exp $

#ifndef __LOGS_H
#define __LOGS_H

#if !defined(ENABLE_LOGGING)

#define _LOG_REGISTER(base, categories)
#define _LOG_SETLEVEL(level, cat)
#define _LOG_DEBUG(instr)
#define _LOG_INFO(instr)
#define _LOG_ERROR(instr)

#else //ENABLE_LOGGING

/**
 * logging levels.
 */
typedef enum
{
  LOG_LEVEL_NONE,
  LOG_LEVEL_DEBUG,
  LOG_LEVEL_INFO,
  LOG_LEVEL_ERROR
} LogLevel;

typedef int LogCategory;

enum
{
   LOG_CAT_PALMPOSIX = 0x100
};

#define _LOG_REGISTER(base, categories) log_register(base, categories)
#define _LOG_SETLEVEL(level, cat)       logSetLevel(level, cat)
#define _LOG_ERROR(x)                   log_error x
#define _LOG_INFO(x)                    log_info  x
#define _LOG_DEBUG(x)                   log_debug x

extern int      log_register    (int base, const char **categories)
extern LogLevel logSetLevel     (LogLevel level, LogCategory cat);
extern void     log_error 		(LogCategory cat, const char *format, ...);
extern void     log_info  		(LogCategory cat, const char *format, ...);
extern void     log_debug 		(LogCategory cat, const char *format, ...);

#endif //ENABLE_LOGGING

#endif //__LOGS_H
