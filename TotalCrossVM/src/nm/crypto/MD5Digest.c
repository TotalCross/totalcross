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

//////////////////////////////////////////////////////////////////////////
TC_API void tcdMD5D_init(NMParams p) // totalcross/crypto/digest/MD5Digest native private void init();
{
   TCObject md5dObj = p->obj[0];
   TCObject digestObj;

   if ((digestObj = createByteArray(p->currentContext, sizeof(MD5_CTX))) != null)
   {
      *Digest_digestRef(md5dObj) = digestObj;
      setObjectLock(digestObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcdMD5D_process_B(NMParams p) // totalcross/crypto/digest/MD5Digest native protected final byte[] process(byte []data);
{
   TCObject md5dObj = p->obj[0];
   TCObject dataObj = p->obj[1];
   TCObject digestObj = *Digest_digestRef(md5dObj);
   MD5_CTX *ctx = (MD5_CTX*) ARRAYOBJ_START(digestObj);

   TCObject byteArrayResult;

   if (!dataObj)
   {
      throwNullArgumentException(p->currentContext, "data");
      return;
   }

   MD5_Init(ctx);
   MD5_Update(ctx, ARRAYOBJ_START(dataObj), ARRAYOBJ_LEN(dataObj));

   if ((byteArrayResult = createByteArray(p->currentContext, MD5_SIZE)) != null)
   {
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
      MD5_Final(ARRAYOBJ_START(byteArrayResult), ctx);
   }
}
