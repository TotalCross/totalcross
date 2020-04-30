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

#ifndef EXCEPTION_H
#define EXCEPTION_H

#ifdef __cplusplus
extern "C" {
#endif

/// Exception enumeration that can be used in some throwException functions.
typedef enum
{
   NoException,
   ArithmeticException,
   ArrayIndexOutOfBoundsException,
   ArrayStoreException,
   ClassCastException,
   ClassNotFoundException,
   ErrorClass,
   ExceptionClass,
   IllegalAccessException,
   IllegalArgumentException,
   ImageException,
   IndexOutOfBoundsException,
   InstantiationException,
   IOException,
   FileNotFoundException,
   IllegalArgumentIOException,
   UnknownHostException,
   SocketTimeoutException,
   NoSuchFieldError,
   NoSuchMethodError,
   NullPointerException,
   OutOfMemoryError,
   RuntimeException,
   ZipException,
   AppExitException,
   InvalidNumberException,
   ElementNotFoundException,
   CryptoException,
   SQLException,
   SQLWarning,
   NegativeArraySizeException,
   InvocationTargetException,
   NoSuchMethodException,
   NoSuchFieldException,
   NotInstalledException,
   GPSDisabledException,
   ThrowableCount,
} Throwable;

void initException();

/// Throw an exception based on the Throwable enumeration. The message passed must have total size < 1024, or use null if there are no messages!
TC_API void throwException(Context currentContext, Throwable t, CharP message, ...);
typedef void (*throwExceptionFunc)(Context currentContext, Throwable t, CharP message, ...);
/// Throw an exception of the given class name with the OPTIONAL message. The message passed must have total size < 1024, or use null if there are no messages!
TC_API void throwExceptionNamed(Context currentContext, CharP exceptionClassName, CharP message, ...);
typedef void (*throwExceptionNamedFunc)(Context currentContext, CharP exceptionClassName, CharP message, ...);
/// Throw an exception based on the Throwable enumeration, showing the given error code in a message.
TC_API void throwExceptionWithCode(Context currentContext, Throwable t, int32 errorCode);
typedef void (*throwExceptionWithCodeFunc)(Context currentContext, Throwable t, int32 errorCode);
/// Throw an IllegalArgumentException, showing the given error code in a message.
TC_API void throwIllegalArgumentException(Context currentContext, CharP argName);
typedef void (*throwIllegalArgumentExceptionFunc)(Context currentContext, CharP argName);
/// Throw an IllegalArgumentException with the given Java's int value, showing the given error code in a message.
TC_API void throwIllegalArgumentExceptionI(Context currentContext, CharP argName, int32 illegalValue);
typedef void (*throwIllegalArgumentExceptionIFunc)(Context currentContext, CharP argName, int32 illegalValue);
/// Throw an IllegalArgumentIOException, showing the given argument name and value.
TC_API void throwIllegalArgumentIOException(Context currentContext, CharP argName, CharP argValue);
typedef void (*throwIllegalArgumentIOExceptionFunc)(Context currentContext, CharP argName, CharP argValue);
/// Throw a FileNotFoundException, showing the given path name.
TC_API void throwFileNotFoundException(Context currentContext, TCHARP path);
typedef void (*throwFileNotFoundExceptionFunc)(Context currentContext, TCHARP path);
/// Throw an NullArgumentException, showing the given error code in a message.
TC_API void throwNullArgumentException(Context currentContext, CharP argName);
typedef void (*throwNullArgumentExceptionFunc)(Context currentContext, CharP argName);
/// Create an exception Object of the given throwable. The message passed must have total size < 1024!
TC_API TCObject createException(Context currentContext, Throwable t, bool fillStack, CharP message, ...);
typedef TCObject (*createExceptionFunc)(Context currentContext, Throwable t, bool fillStack, CharP message, ...);
/// fills the stack trace into the currently thrown exception
void fillStackTrace(Context currentContext, TCObject exception, int32 pc, VoidPArray callStack);
/// Returns the line number based on the given PC
int32 locateLine(Method m, int32 pc);
/// prints the current stack trace to the console
void printStackTrace(Context currentContext);

/// Returns the error message for the given error code.
CharP errorMessage(int32 code);

/// Shows the exception in context->thrownException in an alert
void showUnhandledException(Context context, bool useAlert);

#ifdef __cplusplus
}
#endif

#endif
