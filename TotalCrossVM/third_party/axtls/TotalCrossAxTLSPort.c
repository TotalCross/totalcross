// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "TotalCrossAxTLSPort.h"

static bool tcAxTLSEnsureBuffer(TC_AxTLSContextP context, int length)
{
   if (context->ioBuffer == null || ARRAYOBJ_LEN(context->ioBuffer) < length)
   {
      if (context->ioBuffer != null)
         setObjectLock(context->ioBuffer, UNLOCKED);
      context->ioBuffer = createByteArray(context->currentContext, length);
   }
   return context->ioBuffer != null;
}

static void* tcAxTLSMalloc(void* userData, size_t size, const char* file, int line)
{
   UNUSED(userData); UNUSED(file); UNUSED(line)
   return xmalloc((int32)size);
}

static void* tcAxTLSCalloc(void* userData, size_t count, size_t size, const char* file, int line)
{
   UNUSED(userData); UNUSED(file); UNUSED(line)
   return xcalloc((int32)count, (int32)size);
}

static void* tcAxTLSRealloc(void* userData, void* ptr, size_t size, const char* file, int line)
{
   UNUSED(userData); UNUSED(file); UNUSED(line)
   return xrealloc(ptr, (int32)size);
}

static void tcAxTLSFree(void* userData, void* ptr, const char* file, int line)
{
   UNUSED(userData); UNUSED(file); UNUSED(line)
   xfree(ptr);
}

static int tcAxTLSRead(void* userData, int fd, void* buffer, int length)
{
   TC_AxTLSContextP context = (TC_AxTLSContextP)userData;
   int32 result;
   UNUSED(fd)
   if (context->currentContext == null || context->socket == null || !tcAxTLSEnsureBuffer(context, length))
      return -1;
   result = executeMethod(context->currentContext, context->socketReadWrite, context->socket,
      context->ioBuffer, 0, length, true).asInt32;
   if (context->currentContext->thrownException != null)
      return -1;
   if (result <= 0)
      return result == -1 ? 0 : result;
   xmemmove(buffer, ARRAYOBJ_START(context->ioBuffer), result);
   return result;
}

static int tcAxTLSWrite(void* userData, int fd, const void* buffer, int length)
{
   TC_AxTLSContextP context = (TC_AxTLSContextP)userData;
   int32 result;
   UNUSED(fd)
   if (context->currentContext == null || context->socket == null || !tcAxTLSEnsureBuffer(context, length))
      return -1;
   xmemmove(ARRAYOBJ_START(context->ioBuffer), buffer, length);
   result = executeMethod(context->currentContext, context->socketReadWrite, context->socket,
      context->ioBuffer, 0, length, false).asInt32;
   return context->currentContext->thrownException == null ? result : -1;
}

static int tcAxTLSClose(void* userData, int fd)
{
   UNUSED(userData); UNUSED(fd)
   return 0;
}

static int tcAxTLSVLog(void* userData, const char* format, va_list args)
{
   TC_AxTLSContextP context = (TC_AxTLSContextP)userData;
   UNUSED(context)
   return vprintf(format, args);
}

static void tcAxTLSAbort(void* userData, const char* message, const char* file, int line)
{
   UNUSED(userData); UNUSED(message)
   tcabort("axTLS", (char*)file, line);
}

TC_AxTLSContextP tcAxTLSCreate(Context currentContext, uint32 options, int32 sessions)
{
   TC_AxTLSContextP context = (TC_AxTLSContextP)xcalloc(1, sizeof(TC_AxTLSContext));
   if (context == null)
      return null;
   context->currentContext = currentContext;
   axtls_port_hooks_init(&context->hooks);
   context->hooks.user_data = context;
   context->hooks.malloc_fn = tcAxTLSMalloc;
   context->hooks.calloc_fn = tcAxTLSCalloc;
   context->hooks.realloc_fn = tcAxTLSRealloc;
   context->hooks.free_fn = tcAxTLSFree;
   context->hooks.read_fn = tcAxTLSRead;
   context->hooks.write_fn = tcAxTLSWrite;
   context->hooks.close_fn = tcAxTLSClose;
   context->hooks.vlog_fn = tcAxTLSVLog;
   context->hooks.abort_fn = tcAxTLSAbort;
   context->sslContext = ssl_ctx_new_with_port(options, sessions, &context->hooks);
   if (context->sslContext == null)
   {
      xfree(context);
      return null;
   }
   return context;
}

bool tcAxTLSSetSocket(TC_AxTLSContextP context, Context currentContext, TCObject socket)
{
   Method method;
   if (context == null || socket == null)
      return false;
   if (context->socket != null && context->socket != socket)
      return false;
   method = getMethod(OBJ_CLASS(socket), true, "readWriteBytes", 4, BYTE_ARRAY, J_INT, J_INT, J_BOOLEAN);
   if (method == null)
      return false;
   context->currentContext = currentContext;
   setObjectLock(socket, LOCKED);
   context->socket = socket;
   context->socketReadWrite = method;
   return true;
}

void tcAxTLSSetCurrentContext(TC_AxTLSContextP context, Context currentContext)
{
   if (context != null)
      context->currentContext = currentContext;
}

void tcAxTLSDestroy(TC_AxTLSContextP context)
{
   if (context == null)
      return;
   if (context->ioBuffer != null)
      setObjectLock(context->ioBuffer, UNLOCKED);
   if (context->socket != null)
      setObjectLock(context->socket, UNLOCKED);
   xfree(context);
}
