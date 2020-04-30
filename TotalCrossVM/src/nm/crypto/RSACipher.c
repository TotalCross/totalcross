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
#include "../../axtls/crypto.h"

#define OPERATION_ENCRYPT 0
#define OPERATION_DECRYPT 1

//////////////////////////////////////////////////////////////////////////
TC_API void tccRSAC_init(NMParams p) // totalcross/crypto/digest/RSACipher native private void init();
{
   TCObject rsacObj = p->obj[0];
   TCObject cipherObj;

   if ((cipherObj = createByteArray(p->currentContext, sizeof(RSA_CTX))) != null)
      *Cipher_cipherRef(rsacObj) = cipherObj;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tccRSAC_finalize(NMParams p) // totalcross/crypto/digest/RSACipher native protected final void finalize();
{
   TCObject rsacObj = p->obj[0];
   TCObject cipherObj = *Cipher_cipherRef(rsacObj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(cipherObj);

   if (ctx->m) // initialized?
      RSA_free_external(ctx);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tccRSAC_doReset(NMParams p) // totalcross/crypto/cipher/RSACipher native protected final void doReset() throws totalcross.crypto.CryptoException;
{
   TCObject rsacObj = p->obj[0];
   TCObject cipherObj = *Cipher_cipherRef(rsacObj);
   int32 operation = *Cipher_operation(rsacObj);
   TCObject key = *Cipher_key(rsacObj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(cipherObj);
   TCObject e, d, n;
   
   if (ctx->m) // already initialized?
   {
      RSA_free_external(ctx);
      ctx->d = null; // necessary to avoid freeing ctx->d when it is not allocated (in public keys)
   }

   if (operation == OPERATION_ENCRYPT)
   {
      e = RSAPublicKey_e(key);
      n = RSAPublicKey_n(key);

      RSA_pub_key_new_external(ctx, ARRAYOBJ_START(n), ARRAYOBJ_LEN(n), ARRAYOBJ_START(e), ARRAYOBJ_LEN(e));
   }
   else
   {
      e = RSAPrivateKey_e(key);
      d = RSAPrivateKey_d(key);
      n = RSAPrivateKey_n(key);

      RSA_priv_key_new_external(ctx, ARRAYOBJ_START(n), ARRAYOBJ_LEN(n), ARRAYOBJ_START(e), ARRAYOBJ_LEN(e), ARRAYOBJ_START(d), ARRAYOBJ_LEN(d));
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tccRSAC_process_B(NMParams p) // totalcross/crypto/cipher/RSACipher native protected byte[] process(byte []data) throws totalcross.crypto.CryptoException;
{
   TCObject rsacObj = p->obj[0];
   TCObject dataObj = p->obj[1];
   int32 operation = *Cipher_operation(rsacObj);
   TCObject key = *Cipher_key(rsacObj);
   TCObject cipherObj = *Cipher_cipherRef(rsacObj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(cipherObj);

   uint8 *out;
   TCObject byteArrayResult;
   int32 count = -1;

   if (operation == OPERATION_ENCRYPT)
   {
      if ((out = xmalloc(ARRAYOBJ_LEN(RSAPublicKey_n(key)))) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         if ((count = RSA_encrypt(ctx, ARRAYOBJ_START(dataObj), (uint16) ARRAYOBJ_LEN(dataObj), out, false)) == -1)
            throwException(p->currentContext, CryptoException, "Encryption error");
      }
   }
   else
   {
      if ((out = xmalloc(ARRAYOBJ_LEN(RSAPrivateKey_n(key)))) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         if ((count = RSA_decrypt(ctx, ARRAYOBJ_START(dataObj), out, ARRAYOBJ_LEN(RSAPrivateKey_n(key)), true)) == -1)
            throwException(p->currentContext, CryptoException, "Decryption error");
      }
   }
      
   if (p->currentContext->thrownException == null && (byteArrayResult = createByteArray(p->currentContext, count)) != null)
   {
      xmemmove(ARRAYOBJ_START(byteArrayResult), out, count);
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
   }
   xfree(out);
}
