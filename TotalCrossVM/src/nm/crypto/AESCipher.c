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
#include "../../axtls/crypto.h"

#define OPERATION_ENCRYPT 0
#define OPERATION_DECRYPT 1

//////////////////////////////////////////////////////////////////////////
TC_API void tccAESC_init(NMParams p) // totalcross/crypto/digest/AESCipher native void init();
{
   TCObject aesObj = p->obj[0];
   TCObject cipherObj;

   if ((cipherObj = createByteArray(p->currentContext, sizeof(AES_CTX))) != null)
   {
      *Cipher_cipherRef(aesObj) = cipherObj; // guich@tc110_98: store in a byte array
      setObjectLock(cipherObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tccAESC_doReset(NMParams p) // totalcross/crypto/cipher/AESCipher native protected final void doReset() throws totalcross.crypto.CryptoException;
{
   TCObject aesObj = p->obj[0];
   TCObject cipherObj = *Cipher_cipherRef(aesObj);
   int32 operation = *Cipher_operation(aesObj);
   TCObject key = *Cipher_key(aesObj);
   TCObject iv = *Cipher_iv(aesObj);
   AES_CTX *ctx = (AES_CTX*) ARRAYOBJ_START(cipherObj);
   int32 keyLen;
   TCObject dataObj;

   if (iv == NULL)
   {
      // initialize a random IV
      if ((iv = createByteArray(p->currentContext, AES_BLOCKSIZE)) == null)
         return;
      RNG_custom_init((uint8*) &iv, 4);
      get_random_NZ(AES_BLOCKSIZE, ARRAYOBJ_START(iv));
      RNG_terminate();
      *Cipher_iv(aesObj) = iv;
      setObjectLock(iv, UNLOCKED);
   }

   dataObj = AESKey_data(key);
   keyLen = ARRAYOBJ_LEN(dataObj);

   AES_set_key(ctx, ARRAYOBJ_START(dataObj), ARRAYOBJ_START(iv), (keyLen*8) == 128 ? AES_MODE_128 : AES_MODE_256); // guich@tc110_10: select the aes size

   if (operation == OPERATION_DECRYPT)
      AES_convert_key(ctx);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tccAESC_process_B(NMParams p) // totalcross/crypto/cipher/AESCipher native protected byte[] process(byte []data) throws totalcross.crypto.CryptoException;
{
   TCObject aesObj = p->obj[0];
   TCObject dataObj = p->obj[1];
   int32 operation = *Cipher_operation(aesObj);
   TCObject cipherObj = *Cipher_cipherRef(aesObj);
   AES_CTX *ctx = (AES_CTX*) ARRAYOBJ_START(cipherObj);

   TCObject byteArrayResult;

   uint8* data = ARRAYOBJ_START(dataObj);
   int32 n = ARRAYOBJ_LEN(dataObj);
   uint8_t *out;
   int32 out_size = 0;

   if ((out = xmalloc(n + AES_BLOCKSIZE)) == null)
   {
      throwException(p->currentContext, OutOfMemoryError, "Can't create out buffer");
      return;
   }

   if (operation == OPERATION_ENCRYPT)
   {
      // the cypher encrypts in 16 bytes step. So, if we have to pad, we
      // encrypt into the last 16 boundary, then pad, and encrypt the rest.
      uint8_t padding[AES_BLOCKSIZE];
      uint8_t *pp;
      int32 i;
      int32 padding_size = AES_BLOCKSIZE - (n % AES_BLOCKSIZE);
      out_size = n - (n % AES_BLOCKSIZE);

      // first part multiple of block_size
      if (out_size > 0)
         AES_cbc_encrypt(ctx, data, out, out_size);

      pp = data + out_size;
      for (i = 0; i < AES_BLOCKSIZE; i++)
         padding[i] = (i + out_size < n) ? *pp++ : padding_size;

      // remaining + padding
      AES_cbc_encrypt(ctx, padding, out + out_size, AES_BLOCKSIZE);

      // processed a total of s1 + block_size
      out_size += AES_BLOCKSIZE;
   }
   else
   {
      AES_cbc_decrypt(ctx, data, out, n);
      // remove padding
      out_size = n - out[n-1];
   }

   if (out_size < 0) // guich@tc111_23
      throwException(p->currentContext, CryptoException, "Invalid key size");
   else if ((byteArrayResult = createByteArray(p->currentContext, out_size)) != null)
   {
      xmemmove(ARRAYOBJ_START(byteArrayResult), out, out_size);
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
   }

   xfree(out);
}
