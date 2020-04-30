// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

// temporary
#if defined(linux) || defined(WIN32) || defined(ANDROID)
#include "ssl.h"
#define HAVE_IMPLEMENTATION
#ifndef WIN32
typedef int SOCKET;
#endif

static void destroyHT(Context currentContext, bool throwEx)
{
   heapDestroy(heapSSLSocket);
   heapSSLSocket = null;
   if (throwEx)
      throwException(currentContext, OutOfMemoryError, null);
}
static bool createHT(Context currentContext)
{
   if (heapSSLSocket == null) // create the heap and the hash table on the first create call.
   {
      heapSSLSocket = heapCreate();
      IF_HEAP_ERROR(heapSSLSocket)
      {
         destroyHT(currentContext, true);
         return false;
      }
      htSSLSocket = htNew(16, heapSSLSocket);
   }
   return true;
}
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_dispose(NMParams p) // totalcross/net/ssl/SSL native public void dispose();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);
   int32 dontFinalize = SSL_sslDontFinalize(sslObj);
   int32 fd;

   if (!dontFinalize)
   {                
      if (ssl)
      {
         fd = ssl->client_fd; // we should access "ssl" only after checking the "dontFinalize" field
         ssl_free(ssl);
   
         LOCKVAR(htSSL);
         htRemove(&htSSLSocket, fd); // remove socket object from the hash table when the ssl is disposed.
         if (htSSLSocket.size == 0) // destroy the hash table if empty
            destroyHT(p->currentContext, false);
         UNLOCKVAR(htSSL);
      }
      SSL_sslDontFinalize(sslObj) = true; //flsobral@tc114_36: don't finalize disposed objects.
   }
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_handshakeStatus(NMParams p) // totalcross/net/ssl/SSL native public int handshakeStatus();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   p->retI = ssl_handshake_status(ssl);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_getCipherId(NMParams p) // totalcross/net/ssl/SSL native public byte getCipherId();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   p->retI = ssl_get_cipher_id(ssl);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_getSessionId(NMParams p) // totalcross/net/ssl/SSL native public byte[] getSessionId();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   const uint8_t *id = ssl_get_session_id(ssl);

   if (id != null)
   {
      if ((p->retO = createByteArray(p->currentContext, SSL_SESSION_ID_SIZE)) != null)
      {
         xmemmove(ARRAYOBJ_START(p->retO), id, SSL_SESSION_ID_SIZE);
         setObjectLock(p->retO, UNLOCKED);
      }
   }
   else
      p->retO = null;
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_getCertificateDN_i(NMParams p) // totalcross/net/ssl/SSL native public String getCertificateDN(int component);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   int32 component = p->i32[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   char* str = (char*) ssl_get_cert_dn(ssl, component);

   if (str != null)
   {
      if ((p->retO = createStringObjectFromCharP(p->currentContext, str, -1)) != null)
         setObjectLock(p->retO, UNLOCKED);
   }
   else
      p->retO = null;
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_read_s(NMParams p) // totalcross/net/ssl/SSL native public int read(totalcross.net.ssl.SSLReadHolder rh);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   TCObject readHolder = p->obj[1];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);
   int size = 0;
   uint8_t *in_data = null;
   TCObject byteArray;

   if (readHolder == null)
   {
      throwException(p->currentContext, NullPointerException, null);
      return;
   }

   size = ssl_read(ssl, &in_data);
   if (size > 0 && in_data != null)
   {
      if ((byteArray = createByteArray(p->currentContext, size)) != null)
      {
         SSLReadHolder_buf(readHolder) = byteArray;
         xmemmove(ARRAYOBJ_START(byteArray), in_data, size);
         setObjectLock(byteArray, UNLOCKED);
      }
   }
   p->retI = size;
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_write_Bi(NMParams p) // totalcross/net/ssl/SSL native public int write(byte []out_data, int len);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   TCObject out_data = p->obj[1];
   int32 len = p->i32[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   p->retI = ssl_write(ssl, out_data ? ARRAYOBJ_START(out_data) : NULL, len);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_verifyCertificate(NMParams p) // totalcross/net/ssl/SSL native public int verifyCertificate();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   p->retI = ssl_verify_cert(ssl);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSL_renegotiate(NMParams p) // totalcross/net/ssl/SSL native public int renegotiate();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslObj = p->obj[0];
   SSL *ssl = (SSL*) SSL_sslRef(sslObj);

   p->retI = ssl_renegotiate(ssl);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_create_ii(NMParams p) // totalcross/net/ssl/SSLCTX native public void create(int options, int num_sessions);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslCtxObj = p->obj[0];
   int32 options = p->i32[0];
   int32 num_sessions = p->i32[1];

   SSLCTX_ctxRef(sslCtxObj) = (int64) ssl_ctx_new(options, num_sessions);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_dispose(NMParams p) // totalcross/net/ssl/SSLCTX native public void dispose();
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslCtxObj = p->obj[0];
   SSLCTX *ssl_ctx = (SSLCTX*)SSLCTX_ctxRef(sslCtxObj);
   int32 dontFinalize = SSLCTX_dontFinalize(sslCtxObj);

   if (!dontFinalize)
   {
      ssl_ctx_free(ssl_ctx);
      SSLCTX_dontFinalize(sslCtxObj) = true; //flsobral@tc114_36: don't finalize disposed objects.
   }

#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_find_s(NMParams p) // totalcross/net/ssl/SSLCTX native public totalcross.net.ssl.SSL find(totalcross.net.Socket s);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslCtxObj = p->obj[0];
   TCObject socketObj = p->obj[1];
   SSLCTX *ssl_ctx = (SSLCTX*)SSLCTX_ctxRef(sslCtxObj);
   SOCKET *socketHandle;

   if (socketObj == null || Socket_socketRef(socketObj) == null)
   {
      throwException(p->currentContext, NullPointerException, null);
      return;
   }

   socketHandle = (SOCKET*) ARRAYOBJ_START(Socket_socketRef(socketObj));
   p->retO = (TCObject)ssl_find(ssl_ctx, (uint32)*socketHandle); // flsobral@tc110_106: ssl_find expects a handle, not an object.

#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_objLoad_iss(NMParams p) // totalcross/net/ssl/SSLCTX native public int objLoad(int obj_type, totalcross.io.Stream material, String password);
{
#ifdef HAVE_IMPLEMENTATION

#define BUFSIZE 0x400
   TCObject sslCtxObj = p->obj[0];
   int32 obj_type = p->i32[0];
   TCObject streamObj = p->obj[1];
   TCObject passwordObj = p->obj[2];

   volatile TCObject byteArray = null;
   Method readMethod;
   volatile CharP password = null;
   uint8* buffer = null;
   
   SSLCTX *ssl_ctx = (SSLCTX*)SSLCTX_ctxRef(sslCtxObj);

   if (streamObj == null)
      throwNullArgumentException(p->currentContext, "material");
   else if ((readMethod = getMethod(OBJ_CLASS(streamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT)) == null)
      throwException(p->currentContext, NoSuchMethodError, null);
   else if (obj_type < 1 || obj_type > 5)
      throwIllegalArgumentException(p->currentContext, "obj_type");
   else if (passwordObj != null && (password = String2CharP(passwordObj)) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else if ((byteArray = createByteArray(p->currentContext, BUFSIZE)) != null)
   {
      int32 bufferSize = BUFSIZE;
      int32 bufferCount = 0;
      int32 bytesRead;
      uint8* byteArrayPtr = ARRAYOBJ_START(byteArray);

      buffer = (uint8*) xmalloc(BUFSIZE);
      if (buffer == null)
      {
         throwException(p->currentContext, OutOfMemoryError, null);
         goto cleanup;
      }

      do
      {
         bytesRead = executeMethod(p->currentContext, readMethod, streamObj, byteArray, 0, BUFSIZE).asInt32;
         if (p->currentContext->thrownException != null)
            goto cleanup;
         if (bytesRead > 0)
         {
            if (bufferCount + bytesRead > bufferSize)
            {
               int32 newBufferSize = ((bufferSize + bytesRead) * 12 / 10);
               uint8* newBuffer = xrealloc(buffer, newBufferSize);
               if (newBuffer == null)
               {
                  throwException(p->currentContext, OutOfMemoryError, null);
                  goto cleanup;
               }
               bufferSize = newBufferSize;
               buffer = newBuffer;
            }
            xmemmove(buffer + bufferCount, byteArrayPtr, bytesRead);
            bufferCount += bytesRead;
         }
      } while (bytesRead > 0);

      p->retI = ssl_obj_memory_load(ssl_ctx, obj_type, buffer, bufferCount, password);
   }

cleanup:
   if (password)
      xfree(password);
   if (buffer)
      xfree(buffer);
   if (byteArray)
      setObjectLock(byteArray, UNLOCKED);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_objLoad_iBis(NMParams p) // totalcross/net/ssl/SSLCTX native public int objLoad(int obj_type, byte []data, int len, String password);
{
#ifdef HAVE_IMPLEMENTATION
   TCObject sslCtxObj = p->obj[0];
   int32 obj_type = p->i32[0];
   TCObject dataObj = p->obj[1];
   int32 len = p->i32[1];
   TCObject passwordObj = p->obj[2];
   SSLCTX *ssl_ctx = (SSLCTX*)SSLCTX_ctxRef(sslCtxObj);
   volatile CharP password = null;

   if (dataObj == null)
      throwNullArgumentException(p->currentContext, "data");
   else if (obj_type < 1 || obj_type > 5)
      throwIllegalArgumentException(p->currentContext, "obj_type");
   else if (passwordObj != null && (password = String2CharP(passwordObj)) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
      p->retI = ssl_obj_memory_load(ssl_ctx, obj_type, ARRAYOBJ_START(dataObj), len, password);

   if (password)
      xfree(password);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_newClient_sB(NMParams p) // totalcross/net/ssl/SSLCTX native public totalcross.net.ssl.SSL newClient(totalcross.net.Socket s, byte []session_id);
{
#ifdef HAVE_IMPLEMENTATION
   SSL_CTX *ssl_ctx = (SSL_CTX*)SSLCTX_ctxRef(p->obj[0]);
   TCObject socketObj = p->obj[1];
   TCObject id = p->obj[2];
   volatile TCObject ssl = null;
   SOCKET *socketHandle;

   if (socketObj == null || Socket_socketRef(socketObj) == null)
   {
      throwException(p->currentContext, NullPointerException, null);
      return;
   }

   if ((ssl = createObject(p->currentContext, "totalcross.net.ssl.SSL")) == null)
      goto error;
   
   socketHandle = (SOCKET*) ARRAYOBJ_START(Socket_socketRef(socketObj));
   LOCKVAR(htSSL);
   if (heapSSLSocket != null || createHT(p->currentContext))
   {
      IF_HEAP_ERROR(heapSSLSocket)
      {
         destroyHT(p->currentContext, true);
      }
      else
         htPutPtr(&htSSLSocket, (int32)*socketHandle, socketObj); // add the new socket to the hash table.
   }
   UNLOCKVAR(htSSL);

   SSL_sslRef(ssl) = (int64)ssl_client_new(ssl_ctx, (int32)*socketHandle, id ? ARRAYOBJ_START(id): NULL, id ? ARRAYOBJ_LEN(id) : 0);
   p->retO = ssl;

   setObjectLock(p->retO, UNLOCKED);      
   return;

error:
   if (ssl)
      setObjectLock(ssl, UNLOCKED);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCTX_newServer_s(NMParams p) // totalcross/net/ssl/SSLCTX native public totalcross.net.ssl.SSL newServer(totalcross.net.Socket s);
{
#ifdef HAVE_IMPLEMENTATION
   SSL_CTX *ssl_ctx = (SSL_CTX*)SSLCTX_ctxRef(p->obj[0]);
   TCObject socketObj = p->obj[1];
   volatile TCObject ssl = null;
   SOCKET *socketHandle;

   if (socketObj == null || Socket_socketRef(socketObj) == null)
   {
      throwException(p->currentContext, NullPointerException, null);
      return;
   }

   if ((ssl = createObject(p->currentContext, "totalcross.net.ssl.SSL")) == null)
      goto error;

   socketHandle = (SOCKET*) ARRAYOBJ_START(Socket_socketRef(socketObj));
   LOCKVAR(htSSL);
   if (heapSSLSocket != null || createHT(p->currentContext))
   {
      IF_HEAP_ERROR(heapSSLSocket)
      {
         destroyHT(p->currentContext, true);
      }
      else
         htPutPtr(&htSSLSocket, (int32)*socketHandle, socketObj); // add the new socket to the hash table.
   }
   UNLOCKVAR(htSSL);

   SSL_sslRef(ssl) = (int64)ssl_server_new(ssl_ctx, (int32)*socketHandle);
   p->retO = ssl;

   setObjectLock(p->retO, UNLOCKED);
   
   return;

error:
   if (ssl)
      setObjectLock(ssl, UNLOCKED);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLU_getConfig_i(NMParams p) // totalcross/net/ssl/SSLUtil native public static int getConfig(int which);
{
#ifdef HAVE_IMPLEMENTATION
   p->retI = ssl_get_config(p->i32[0]);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLU_displayError_i(NMParams p) // totalcross/net/ssl/SSLUtil native public static void displayError(int error_code);
{
#ifdef HAVE_IMPLEMENTATION
   ssl_display_error(p->i32[0]);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLU_version(NMParams p) // totalcross/net/ssl/SSLUtil native public static String version();
{
#ifdef HAVE_IMPLEMENTATION
   char* str = (char*)ssl_version();
   if (str != NULL)
   {
      p->retO = createStringObjectFromCharP(p->currentContext, str, -1);
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
   else
      p->retO = NULL;
#else
   p = 0;
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "ssl_SSL_test.h"
#endif
