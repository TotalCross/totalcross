// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "axtls.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tcdSHA256D_init(NMParams p) // totalcross/crypto/digest/SHA256Digest native private void init();
{
   TCObject sha256dObj = p->obj[0];
   TCObject digestObj;

   if ((digestObj = createByteArray(p->currentContext, sizeof(SHA256_CTX))) != null)
   {
      *Digest_digestRef(sha256dObj) = digestObj;
      setObjectLock(digestObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcdSHA256D_process_B(NMParams p) // totalcross/crypto/digest/SHA256Digest native protected final byte[] process(byte []data);
{
   TCObject sha256dObj = p->obj[0];
   TCObject dataObj = p->obj[1];
   TCObject digestObj = *Digest_digestRef(sha256dObj);
   SHA256_CTX *ctx = (SHA256_CTX*) ARRAYOBJ_START(digestObj);

   TCObject byteArrayResult;

   if (!dataObj)
   {
      throwNullArgumentException(p->currentContext, "data");
      return;
   }

   SHA256_Init(ctx);
   SHA256_Update(ctx, ARRAYOBJ_START(dataObj), ARRAYOBJ_LEN(dataObj));

   if ((byteArrayResult = createByteArray(p->currentContext, SHA256_SIZE)) != null)
   {
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
      SHA256_Final(ARRAYOBJ_START(byteArrayResult), ctx);
   }
}
