/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: MD5Digest.c,v 1.10 2011-01-04 13:31:05 guich Exp $

#include "tcvm.h"
#include "../../axtls/crypto.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tcdMD5D_nativeCreate(NMParams p) // totalcross/crypto/digest/MD5Digest native void nativeCreate();
{
   Object md5dObj = p->obj[0];
   Object digestObj;

   if ((digestObj = createByteArray(p->currentContext, sizeof(MD5_CTX))) != null)
   {
      *Digest_digestRef(md5dObj) = digestObj;
      setObjectLock(digestObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcdMD5D_process_B(NMParams p) // totalcross/crypto/digest/MD5Digest native protected final byte[] process(byte []data);
{
   Object md5dObj = p->obj[0];
   Object dataObj = p->obj[1];
   Object digestObj = *Digest_digestRef(md5dObj);
   MD5_CTX *ctx = (MD5_CTX*) ARRAYOBJ_START(digestObj);

   Object byteArrayResult;

   if (!dataObj)
   {
      throwNullArgumentException(p->currentContext, "data");
      return;
   }

   MD5Init(ctx);
   MD5Update(ctx, ARRAYOBJ_START(dataObj), ARRAYOBJ_LEN(dataObj));

   if ((byteArrayResult = createByteArray(p->currentContext, MD5_SIZE)) != null)
   {
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
      MD5Final(ctx, ARRAYOBJ_START(byteArrayResult));
   }
}
