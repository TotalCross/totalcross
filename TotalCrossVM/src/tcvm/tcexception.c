// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
                      
void printStackTraceFromObj(TCObject traceObj);
                      
TC_API void throwException(Context currentContext, Throwable t, CharP message, ...) // throw an exception based on the Throwable enumeration
{
   TCObject exception;
   // note: code is duplicated here because the ... cannot be passed along different routines
   CharP exceptionClassName = throwableAsCharP[(int32)t];

   if (currentContext->thrownException != null) // do not overwrite a first exception, maybe the second one is caused by the first.
      return;

   exception = t == OutOfMemoryError ? null : createObject(currentContext, exceptionClassName); // guich@tc110_40: use the already-created OOME object
   if (exception == null)
   {
      currentContext->thrownException = exception = currentContext->OutOfMemoryErrorObj;
//      debug("invalidating exception due to OutOfMemory (1). Stack trace:");
//      printStackTraceFromObj(*Throwable_trace(exception));
      *Throwable_trace(exception) = null; // let a new trace be generated
   }
   else
   {
      currentContext->thrownException = exception;
      setObjectLock(currentContext->thrownException, UNLOCKED);
   }

   if (message)
   {
      va_list args;
      va_start(args, message);
      vsprintf(currentContext->exmsg, message, args);
      va_end(args);
      *Throwable_msg(exception) = createStringObjectFromCharP(currentContext, currentContext->exmsg,-1);
      setObjectLock(*Throwable_msg(exception), UNLOCKED);
   }
   fillStackTrace(currentContext, exception, -1, currentContext->callStack);
}

TC_API TCObject createException(Context currentContext, Throwable t, bool fillStack, CharP message, ...)
{
   TCObject exception;

   if (currentContext->thrownException != null)
      return currentContext->thrownException;

#ifdef ENABLE_TRACE
   if (traceOn) debug("creating exception %s\n",throwableAsCharP[(int32)t]);
#endif

   exception = createObject(currentContext, throwableAsCharP[(int32)t]);
   if (exception == null)
   {
      currentContext->thrownException = exception = currentContext->OutOfMemoryErrorObj;
//      debug("invalidating exception due to OutOfMemory (2). Stack trace:");
//      printStackTraceFromObj(*Throwable_trace(exception));
      *Throwable_trace(exception) = null; // let a new trace be generated
   }
   else
   {
      currentContext->thrownException = exception;
      setObjectLock(currentContext->thrownException, UNLOCKED);
   }
   
   if (message)
   {
      va_list args;
      va_start(args, message);
      vsprintf(currentContext->exmsg, message, args);
      va_end(args);
      *Throwable_msg(exception) = createStringObjectFromCharP(currentContext, currentContext->exmsg,-1);
      setObjectLock(*Throwable_msg(exception), UNLOCKED);
   }
   if (fillStack) fillStackTrace(currentContext, exception, -1, currentContext->callStack);
   return exception;
}

TC_API void throwExceptionNamed(Context currentContext, CharP exceptionClassName, CharP message, ...) // throw an exception
{
   TCObject exception;

   if (currentContext->thrownException != null) // do not overwrite a first exception, maybe the second one is caused by the first.
      return;

   exception = createObject(currentContext, exceptionClassName);

   if (exception == null)
   {
      currentContext->thrownException = exception = currentContext->OutOfMemoryErrorObj;
      debug("invalidating exception due to OutOfMemory (3). Stack trace:");
      printStackTraceFromObj(*Throwable_trace(exception));
      *Throwable_trace(exception) = null; // let a new trace be generated
   }
   else
   {
      currentContext->thrownException = exception;
      setObjectLock(currentContext->thrownException, UNLOCKED);
   }
   
   if (message)
   {
      va_list args;
      va_start(args, message);
      vsprintf(currentContext->exmsg, message, args);
      va_end(args);
      *Throwable_msg(exception) = createStringObjectFromCharP(currentContext, currentContext->exmsg,-1);
      setObjectLock(*Throwable_msg(exception), UNLOCKED);
   }
   fillStackTrace(currentContext, exception, -1, currentContext->callStack);
}

TC_API void throwExceptionWithCode(Context currentContext, Throwable t, int32 errorCode)
{
   CharP text;
   char errMsg[256];

   if (currentContext->thrownException != null) // do not overwrite a first exception, maybe the second one is caused by the first.
      return;

   text = getErrorMessage(errorCode, errMsg, sizeof(errMsg)-1);
   if (text != null)
      createException(currentContext, t, true, "Error Code: %d - %s", errorCode, text);
   else
      createException(currentContext, t, true, errorCode < -10000 ? "Error Code: %X" : "Error Code: %d", errorCode);
}

TC_API void throwIllegalArgumentExceptionI(Context currentContext, CharP argName, int32 illegalValue)
{
   char msg[128];
   sprintf(msg, "Invalid value '%d' for argument '%s'", illegalValue, argName);
   throwException(currentContext, IllegalArgumentException, msg);
}

TC_API void throwIllegalArgumentException(Context currentContext, CharP argName)
{
   char msg[128];
   sprintf(msg, "Invalid value for argument '%s'", argName);
   throwException(currentContext, IllegalArgumentException, msg);
}

TC_API void throwIllegalArgumentIOException(Context currentContext, CharP argName, CharP argValue)
{
   char msg[128];
   if (argValue != null)
      sprintf(msg, "Invalid value for argument '%s': %s", argName, argValue);
   else
      sprintf(msg, "Invalid value for argument '%s'", argName);
   throwException(currentContext, IllegalArgumentIOException, msg);
}

TC_API void throwFileNotFoundException(Context currentContext, TCHARP path)
{
   char msg[MAX_PATHNAME*3/2];
   xstrcpy(msg, "File not found: ");
   TCHARP2CharPBuf(path, msg + xstrlen(msg));
   //sprintf(msg, "File not found: %s", path);
   throwException(currentContext, FileNotFoundException, msg);
}

TC_API void throwNullArgumentException(Context currentContext, CharP argName)
{
   char msg[128];
   sprintf(msg, "Argument '%s' cannot have a null value", argName);
   throwException(currentContext, NullPointerException, msg);
}

static CharP dumpMethodInfo(CharP c, Method m, int32 line, CharP end) // waba.applet.Applet.registerMainWindow(Applet.java:441)
{
   CharP s,b;
   // totalcross.Launcher
   s = m->class_->name;
   while (c < end && *s) *c++ = *s++;
   // .
   if (c < end) *c++ = '.';
   // registerMainWindow
   s = m->name;
   if (s)
      while (c < end && *s) *c++ = *s++;
   if ((c+6) < end && line >= 0)
   {
      IntBuf ib;
      b = int2str(line, ib);
      *c++ = ' ';
      while (*b) *c++ = *b++;
   }
   // \n
   if (c < end) *c++ = '\n';
   return c;
}

int32 locateLine(Method m, int32 pc)
{
   if (pc >= 0)
   {
      int32 i = ARRAYLENV(m->lineNumberLine);
      UInt16Array lines = m->lineNumberStartPC + i - 1;
      for (; --i >= 0; lines--)
         if (*lines <= pc) // guich@tc100b5_32: changed < to <=
            return m->lineNumberLine[i];
   }
   return -1;
}
void fillStackTrace(Context currentContext, TCObject exception, int32 pc0, VoidPArray callStack)
{
   Method m=null;
   int32 line;
   size_t im;
   char *c0 =currentContext->exmsg; 
   char *c=c0;
   bool first = true;
   Code oldpc;
   
   while (callStack > currentContext->callStackStart)
   {
      callStack -= 2;
      //int2hex((int32)callStack, 6, c); c += 6; *c++ = ' '; - used when debugging
      m = (Method)callStack[0];  
      im = (size_t)m;
      if (im < 1000 || (im & 3) != 0) 
      {
         debug("breaking fillStackTrace due to invalid memory addresses");
         break; // trying to handle crash on addresses 0x33 and 0x36 and odd addresses
      }
      oldpc = (Code)callStack[1];
      line = (m->lineNumberLine != null) ? locateLine(m, first ? pc0 : ((int32)(oldpc - m->code))) : -1;
      c = dumpMethodInfo(c, m, line, c0 + sizeof(currentContext->exmsg) - 2);
      first = false;
   }
   *c = 0;
   if (exception != null)
   {                    
      TCObject *trace = Throwable_trace(exception);
      if (c != c0) // was something filled in?
      {
         if (currentContext != gcContext && exception == currentContext->OutOfMemoryErrorObj)
            debug("OutOfMemory:\n%s",c0);
         *trace = createStringObjectFromCharP(currentContext, c0, (int32)(c-c0));
         if (*trace)
            setObjectLock(*trace, UNLOCKED);
         else
         if (currentContext != gcContext)
            debug("Not enough memory to create the stack trace string. Dumping to here: %s\n%s", OBJ_CLASS(exception)->name,c0);
         else
         if (exception != currentContext->OutOfMemoryErrorObj)
            debug("Exception thrown in finalize: %s\n%s", OBJ_CLASS(exception)->name,c0); // guich@tc126_63
      }
      else
         *trace = null; // the trace may not be null if we're reusing OutOfMemoryErrorObj
   }
#ifdef _DEBUG
//   if (c != c0) debug(c0);
#endif
}

void printStackTrace(Context currentContext)
{
   fillStackTrace(currentContext, null, -1, currentContext->callStack); 
   debug(currentContext->exmsg);
}

void showUnhandledException(Context context, bool useAlert)
{
   TCObject o;
   TCObject thrownException = context->thrownException;
   CharP msg=null, throwableTrace=null;              

   context->thrownException = null; // guich@tc130: null it out before the alert
   
   o = *Throwable_msg(thrownException);
   if (o) msg = String2CharP(o);
   o = *Throwable_trace(thrownException);
   if (o && String_charsStart(o))
      throwableTrace = String2CharP(o);
#ifndef ANDROID // this is already done in Android
   printf("Unhandled exception:\n%s:\n %s\n\nStack trace:\n%s\nAborting %s.", OBJ_CLASS(thrownException)->name, msg==null?"":msg, throwableTrace==null?"":throwableTrace,useAlert?"program":"thread"); // always dump to the console
#endif      
   if (useAlert)
      alert("Unhandled exception:\n%s:\n %s\n\nStack trace:\n%s\nAborting program.", OBJ_CLASS(thrownException)->name, msg==null?"":msg, throwableTrace==null?"":throwableTrace);
   xfree(msg);
   xfree(throwableTrace);
}
void initException()
{
   throwableAsCharP[ArithmeticException           ] = "java.lang.ArithmeticException";
   throwableAsCharP[ArrayIndexOutOfBoundsException] = "java.lang.ArrayIndexOutOfBoundsException";
   throwableAsCharP[ArrayStoreException           ] = "java.lang.ArrayStoreException";
   throwableAsCharP[ClassCastException            ] = "java.lang.ClassCastException";
   throwableAsCharP[ClassNotFoundException        ] = "java.lang.ClassNotFoundException";
   throwableAsCharP[ErrorClass                    ] = "java.lang.Error";
   throwableAsCharP[ExceptionClass                ] = "java.lang.Exception";
   throwableAsCharP[IllegalAccessException        ] = "java.lang.IllegalAccessException";
   throwableAsCharP[IllegalArgumentException      ] = "java.lang.IllegalArgumentException";
   throwableAsCharP[ImageException                ] = "totalcross.ui.image.ImageException";
   throwableAsCharP[IndexOutOfBoundsException     ] = "java.lang.IndexOutOfBoundsException";
   throwableAsCharP[InstantiationException        ] = "java.lang.InstantiationException";
   throwableAsCharP[NoSuchFieldError              ] = "java.lang.NoSuchFieldError";
   throwableAsCharP[NoSuchMethodError             ] = "java.lang.NoSuchMethodError";
   throwableAsCharP[NullPointerException          ] = "java.lang.NullPointerException";
   throwableAsCharP[OutOfMemoryError              ] = "java.lang.OutOfMemoryError";
   throwableAsCharP[RuntimeException              ] = "java.lang.RuntimeException";
   throwableAsCharP[IOException                   ] = "totalcross.io.IOException";
   throwableAsCharP[FileNotFoundException         ] = "totalcross.io.FileNotFoundException";
   throwableAsCharP[IllegalArgumentIOException    ] = "totalcross.io.IllegalArgumentIOException";
   throwableAsCharP[UnknownHostException          ] = "totalcross.net.UnknownHostException";
   throwableAsCharP[SocketTimeoutException        ] = "totalcross.net.SocketTimeoutException";
   throwableAsCharP[ZipException                  ] = "totalcross.util.zip.ZipException";
   throwableAsCharP[AppExitException              ] = "totalcross.sys.AppExitException";
   throwableAsCharP[InvalidNumberException        ] = "totalcross.sys.InvalidNumberException";
   throwableAsCharP[ElementNotFoundException      ] = "totalcross.util.ElementNotFoundException";
   throwableAsCharP[CryptoException               ] = "totalcross.crypto.CryptoException";
   throwableAsCharP[SQLException                  ] = "totalcross.sql.SQLException";
   throwableAsCharP[SQLWarning                    ] = "totalcross.sql.SQLWarning";
   throwableAsCharP[NegativeArraySizeException    ] = "java.lang.NegativeArraySizeException";
   throwableAsCharP[InvocationTargetException     ] = "java.lang.reflect.InvocationTargetException";
   throwableAsCharP[NoSuchMethodException         ] = "java.lang.NoSuchMethodException";
   throwableAsCharP[NoSuchFieldException          ] = "java.lang.NoSuchFieldException";
   throwableAsCharP[NotInstalledException         ] = "totalcross.util.NotInstalledException";
   throwableAsCharP[GPSDisabledException          ] = "totalcross.io.device.gps.GPSDisabledException";
}
