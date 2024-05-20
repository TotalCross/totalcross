// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Net.h"

#ifndef WINCE
#include "mbedtls/platform.h"
#include "mbedtls/net_sockets.h"
#include "mbedtls/ssl.h"
#include "mbedtls/entropy.h"
#include "mbedtls/ctr_drbg.h"

#define SSLContextMbedtls_net_context(o)             FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define SSLContextMbedtls_entropy_context(o)         FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define SSLContextMbedtls_ctr_drbg_context(o)        FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define SSLContextMbedtls_ssl_context(o)             FIELD_OBJ(o, OBJ_CLASS(o), 3)
#define SSLContextMbedtls_ssl_config(o)              FIELD_OBJ(o, OBJ_CLASS(o), 4)

const char *pers = "mini_client";
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCM_init_s(NMParams p) // totalcross/net/ssl/SSLContextMbedtls void init(Socket socket);
{
#ifdef WINCE
   p->retO = null;
#else
   TCObject context = p->obj[0];
   TCObject socket = p->obj[1];
   TCObject socketRef = Socket_socketRef(socket);
   TCObject socketHost = Socket_host(socket);

   CharP hostname = String2CharP(socketHost);

   SOCKET* socketHandle;
   Err err;

   TCObject ctr_drbg_object;
   mbedtls_ctr_drbg_context* ctr_drbg;
   TCObject net_context_object;
   mbedtls_net_context* net_context;
   TCObject ssl_context_object;
   mbedtls_ssl_context* ssl_context;
   TCObject ssl_config_object;
   mbedtls_ssl_config* ssl_config;
   TCObject entropy_context_object;
   mbedtls_entropy_context* entropy_context;

   if ((ctr_drbg_object = createByteArray(p->currentContext, sizeof(mbedtls_ctr_drbg_context))) == null) {
      goto out_of_memory;
   }
   SSLContextMbedtls_ctr_drbg_context(context) = ctr_drbg_object;
   ctr_drbg = (mbedtls_ctr_drbg_context*) ARRAYOBJ_START(ctr_drbg_object);
   setObjectLock(ctr_drbg_object, UNLOCKED);

   if ((net_context_object = createByteArray(p->currentContext, sizeof(mbedtls_net_context))) == null) {
      goto out_of_memory;
   }
   SSLContextMbedtls_net_context(context) = net_context_object;
   net_context = (mbedtls_net_context*) ARRAYOBJ_START(net_context_object);
   setObjectLock(net_context_object, UNLOCKED);

   if ((ssl_context_object = createByteArray(p->currentContext, sizeof(mbedtls_ssl_context))) == null) {
      goto out_of_memory;
   }
   SSLContextMbedtls_ssl_context(context) = ssl_context_object;
   ssl_context = (mbedtls_ssl_context*) ARRAYOBJ_START(ssl_context_object);
   setObjectLock(ssl_context_object, UNLOCKED);

   if ((ssl_config_object = createByteArray(p->currentContext, sizeof(mbedtls_ssl_config))) == null) {
      goto out_of_memory;
   }
   SSLContextMbedtls_ssl_config(context) = ssl_config_object;
   ssl_config = (mbedtls_ssl_config*) ARRAYOBJ_START(ssl_config_object);
   setObjectLock(ssl_config_object, UNLOCKED);

   if ((entropy_context_object = createByteArray(p->currentContext, sizeof(mbedtls_entropy_context))) == null) {
      goto out_of_memory;
   }
   SSLContextMbedtls_entropy_context(context) = entropy_context_object;
   entropy_context = (mbedtls_entropy_context*) ARRAYOBJ_START(entropy_context_object);
   setObjectLock(entropy_context_object, UNLOCKED);

   // 0. Initialize and setup stuff
   mbedtls_ctr_drbg_init( ctr_drbg );
   mbedtls_net_init( net_context );
   mbedtls_ssl_init( ssl_context );
   mbedtls_ssl_config_init( ssl_config );
   mbedtls_entropy_init( entropy_context );

   if( mbedtls_ctr_drbg_seed( ctr_drbg, mbedtls_entropy_func, entropy_context,
                     (const unsigned char *) pers, strlen( pers ) ) != 0 ) {
      throwExceptionWithCode(p->currentContext, IOException, 1);
      goto finish;
   }

   if( mbedtls_ssl_config_defaults( ssl_config,
               MBEDTLS_SSL_IS_CLIENT,
               MBEDTLS_SSL_TRANSPORT_STREAM,
               MBEDTLS_SSL_PRESET_DEFAULT ) != 0 ) {
      throwExceptionWithCode(p->currentContext, IOException, 2);
      goto finish;
   }

   mbedtls_ssl_conf_rng( ssl_config, mbedtls_ctr_drbg_random, ctr_drbg );

   mbedtls_ssl_conf_authmode( ssl_config, MBEDTLS_SSL_VERIFY_NONE );

   if( mbedtls_ssl_setup( ssl_context, ssl_config ) != 0 ) {
      throwExceptionWithCode(p->currentContext, IOException, 3);
      goto finish;
   }

   if( mbedtls_ssl_set_hostname( ssl_context, hostname ) != 0 ) {
      throwExceptionWithCode(p->currentContext, IOException, 4);
      goto finish;
   }

   net_context->fd = *((SOCKET*) ARRAYOBJ_START(socketRef));

   mbedtls_ssl_set_bio( ssl_context, net_context, mbedtls_net_send, mbedtls_net_recv, NULL );

   return;
out_of_memory:
   throwException(p->currentContext, OutOfMemoryError, null);
finish:
   ;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCM_close(NMParams p) // totalcross/net/ssl/SSLContextMbedtls void close();
{
#ifdef WINCE
   p->retO = null;
#else
   TCObject context = p->obj[0];
   TCObject ctr_drbg_object = SSLContextMbedtls_ctr_drbg_context(context);
   TCObject net_context_object = SSLContextMbedtls_net_context(context);
   TCObject ssl_context_object = SSLContextMbedtls_ssl_context(context);
   TCObject ssl_config_object = SSLContextMbedtls_ssl_config(context);
   TCObject entropy_context_object = SSLContextMbedtls_entropy_context(context);
   mbedtls_ctr_drbg_context* ctr_drbg;
   mbedtls_net_context* net_context;
   mbedtls_ssl_context* ssl_context;
   mbedtls_ssl_config* ssl_config;
   mbedtls_entropy_context* entropy_context;

   if (ssl_context_object == NULL) {
      throwException(p->currentContext, IOException, "context is already closed");
      return;
   }

   ctr_drbg = (mbedtls_ctr_drbg_context*) ARRAYOBJ_START(ctr_drbg_object);
   net_context = (mbedtls_net_context*) ARRAYOBJ_START(net_context_object);
   ssl_context  = (mbedtls_ssl_context*) ARRAYOBJ_START(ssl_context_object);
   ssl_config = (mbedtls_ssl_config*) ARRAYOBJ_START(ssl_config_object);
   entropy_context = (mbedtls_entropy_context*) ARRAYOBJ_START(entropy_context_object);

   mbedtls_net_free( net_context );
   mbedtls_ssl_free( ssl_context );
   mbedtls_ssl_config_free( ssl_config );
   mbedtls_ctr_drbg_free( ctr_drbg );
   mbedtls_entropy_free( entropy_context );

   SSLContextMbedtls_ctr_drbg_context(context) = NULL;
   SSLContextMbedtls_net_context(context) = NULL;
   SSLContextMbedtls_ssl_context(context) = NULL;
   SSLContextMbedtls_ssl_config(context) = NULL;
   SSLContextMbedtls_entropy_context(context) = NULL;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCM_startHandshake(NMParams p) // totalcross/net/ssl/SSLContextMbedtls native void startHandshake() throws IOException;
{
   p->retO = null;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnsSSLCM_readWriteBytes_sBiib(NMParams p) // totalcross/net/ssl/SSLContextMbedtls int readWriteBytes(Socket socket, byte []buf, int start, int count, boolean isRead) throws totalcross.io.IOException;
{
#ifdef WINCE
   p->retO = null;
#else
   TCObject context = p->obj[0];
   TCObject socket = p->obj[1];
   TCObject buffer = p->obj[2];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   bool isRead = p->i32[2];
   TCObject ssl_context_object = SSLContextMbedtls_ssl_context(context);
   mbedtls_ssl_context* ssl_context  = (mbedtls_ssl_context*) ARRAYOBJ_START(ssl_context_object);
   CharP buf;
   int32 timeout;
   int32 retCount;
   Err err;
   int32 ret = 0;
   int32 timestamp;

   if (buffer == NULL) {
      throwNullArgumentException(p->currentContext, "buffer");
      return;
   }
   buf = ARRAYOBJ_START(buffer);

   if (isRead) {
      timeout = Socket_readTimeout(socket);
   } else {
      timeout = Socket_writeTimeout(socket);
   }

   timestamp = getTimeStamp();
   retCount = 0;
   do { // flsobral@tc113_33: loop back only on EWOULDBLOCK, respecting the timeout.
      if (isRead) {
         ret = mbedtls_ssl_read( ssl_context, buf + start + retCount, count - retCount); //Read
         if (ret == 0 && retCount >= 0) { // flsobral@tc110_2: if result is 0, the connection was gracefully closed by the remote host.
            break;
         }
      }
      else {
         ret = mbedtls_ssl_write( ssl_context, buf + start + retCount, count - retCount); //Write
      }

      if( ret == MBEDTLS_ERR_SSL_WANT_READ || ret == MBEDTLS_ERR_SSL_WANT_WRITE ) {
         continue;
      }

      if( ret == MBEDTLS_ERR_SSL_PEER_CLOSE_NOTIFY ) {
         if (retCount == 0) {
            retCount = -1;
         }
         break;
      }

      if (ret < 0) {
         goto error;
      }

      retCount += ret; // update the number of bytes write/read
   } while (retCount < count /*|| (getTimeStamp() - timestamp < timeout)*/);

   p->retI = retCount;
   return;

   error:
   throwExceptionWithCode(p->currentContext, IOException, ret);
#endif
}

#ifdef ENABLE_TEST_SUITE
// #include "SSLContextMbedtls_test.h"
#endif
