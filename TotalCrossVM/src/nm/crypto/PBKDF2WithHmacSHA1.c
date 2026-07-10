// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#include <axtls/axtls_pbkdf2.h>

TC_API void tcpPBKDF2WHSHA1F_generateSecretI(NMParams p) // totalcross/crypto/provider/PBKDF2WithHmacSHA1Factory native private byte[] generateSecretImpl(char []password, byte []salt, int iterations, int keyLength);
{
   TCObject passwordObj = p->obj[1];
   TCObject saltObj = p->obj[2];
   int32 iterations = p->i32[0];
   int32 keyLength = p->i32[1];
   TCObject keyObj;
   int32 passLen = ARRAYOBJ_LEN(passwordObj);
   CharP password;

   if ((keyObj = createByteArray(p->currentContext, keyLength / 8)) == null)
      return;
   password = JCharP2CharP((JCharP)ARRAYOBJ_START(passwordObj), passLen);
   if (password == null)
   {
      setObjectLock(keyObj, UNLOCKED);
      throwException(p->currentContext, OutOfMemoryError, null);
      return;
   }
   if (axtls_pbkdf2_sha1((const uint8_t *)password, passLen,
         ARRAYOBJ_START(saltObj), ARRAYOBJ_LEN(saltObj), ARRAYOBJ_START(keyObj),
         ARRAYOBJ_LEN(keyObj), (uint32_t)iterations) != 0)
   {
      xfree(password);
      setObjectLock(keyObj, UNLOCKED);
      throwException(p->currentContext, IllegalArgumentException, "Invalid PBKDF2 parameters");
      return;
   }
   xfree(password);
   p->retO = keyObj;
   setObjectLock(keyObj, UNLOCKED);
}
