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

#define OPERATION_SIGN 0
#define OPERATION_VERIFY 1

//   5.2.4.1.2. RSA Signatures
//
//   With RSA signatures, the hash value is encoded as described in
//   PKCS #1 section 9.2.1 encoded using PKCS #1 encoding type
//   EMSA-PKCS1-v1_5 [RFC2437].  This requires inserting the hash value
//   as an octet string into an ASN.1 structure. The object identifier
//   for the type of hash being used is included in the structure.
//
//   The ASN.1 OIDs are:
//
//     - MD5:        1.2.840.113549.2.5
//
//     - RIPEMD-160: 1.3.36.3.2.1
//
//     - SHA-1:      1.3.14.3.2.26
//
//     - SHA256:     2.16.840.1.101.3.4.2.1
//
//     - SHA384:     2.16.840.1.101.3.4.2.2
//
//     - SHA512:     2.16.840.1.101.3.4.2.3
//
//   In practice this amounts to prefixing the hash with one of the
//   following, then padding as described in PKCS #1:
//
//       MD5:        0x30, 0x20, 0x30, 0x0C, 0x06, 0x08, 0x2A, 0x86,
//                   0x48, 0x86, 0xF7, 0x0D, 0x02, 0x05, 0x05, 0x00,
//                   0x04, 0x10
//
//       RIPEMD-160: 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x24,
//                   0x03, 0x02, 0x01, 0x05, 0x00, 0x04, 0x14
//
//       SHA-1:      0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0E,
//                   0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14
//
//       SHA256:     0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86,
//                   0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05,
//                   0x00, 0x04, 0x20
//
//       SHA384:     0x30, 0x41, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86,
//                   0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x05,
//                   0x00, 0x04, 0x30
//
//       SHA512:     0x30, 0x51, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86,
//                   0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x05,
//                   0x00, 0x04, 0x40

static uint8 MD5_HASH_INFO[] = { 0x30, 0x20, 0x30, 0x0C, 0x06, 0x08, 0x2A, 0x86, 0x48,
         0x86, 0xF7, 0x0D, 0x02, 0x05, 0x05, 0x00, 0x04, 0x10 };
static int32 MD5_HASH_INFO_LENGTH = 18;

static uint8 SHA1_HASH_INFO[] = { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0E, 0x03,
         0x02, 0x1A, 0x05, 0x00, 0x04, 0x14 };
static int32 SHA1_HASH_INFO_LENGTH = 15;

static uint8 SHA256_HASH_INFO[] = { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86, 0x48,
         0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };
static int32 SHA256_HASH_INFO_LENGTH = 19;

// This method computes the message digest using the digest algorithm given.
static TCObject getMessageDigest(Context currentContext, TCObject digest, TCObject data)
{
   TCObject res;

   executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "reset", 0), digest); // reset digest
   if (currentContext->thrownException != null)
      return null;
   executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "update", 1, BYTE_ARRAY), digest, data); // update digest data with the message to sign
   if (currentContext->thrownException != null)
      return null;
   res = executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "getDigest", 0), digest).asObj; // get the message digest
   if (currentContext->thrownException != null)
      return null;

   return res;
}

// This method creates the signature packet from the message digest. Basically, PKCS #1 v1.5
// packets are formed by the message digest prepended with the digest algorithm information.
static int32 createInfo(Context currentContext, TCObject digest, uint8 *data, int32 dataLen, uint8 **buf)
{
   TCObject digestName;
   char name[64];
   uint8 *hashInfo;
   int32 hashInfoLen, len;

   digestName = executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "getAlgorithm", 0), digest).asObj;
   if (currentContext->thrownException != null)
      return 0;

   String2CharPBuf(digestName, name);

   if (xstrcmp(name, "MD5") == 0)
   {
      hashInfo = MD5_HASH_INFO;
      hashInfoLen = MD5_HASH_INFO_LENGTH;
   }
   else if (xstrcmp(name, "SHA-1") == 0)
   {
      hashInfo = SHA1_HASH_INFO;
      hashInfoLen = SHA1_HASH_INFO_LENGTH;
   }
   else if (xstrcmp(name, "SHA-256") == 0)
   {
      hashInfo = SHA256_HASH_INFO;
      hashInfoLen = SHA256_HASH_INFO_LENGTH;
   }
   else
   {
      throwException(currentContext, RuntimeException, "Algorithm %s was not found", name);
      return 0;
   }

   len = hashInfoLen + dataLen;
   if ((*buf = xmalloc(len)) == null)
   {
      throwException(currentContext, OutOfMemoryError, null);
      return 0;
   }

   xmemmove(*buf, hashInfo, hashInfoLen);
   xmemmove(*buf + hashInfoLen, data, dataLen);

   return len;
}

// This method retrieves the message digest from the signature packet. Basically, PKCS #1 v1.5
// packets are formed by the message digest prepended with the digest algorithm information.
static int32 retrieveInfo(Context currentContext, TCObject digest, uint8 *data, int32 dataLen, uint8 **buf)
{
   TCObject digestName;
   int32 digestLen;
   char name[64];
   uint8 *expectedHashInfo;
   int32 expectedHashInfoLen;

   digestName = executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "getAlgorithm", 0), digest).asObj;
   digestLen = executeMethod(currentContext, getMethod(OBJ_CLASS(digest), true, "getDigestLength", 0), digest).asInt32;
   String2CharPBuf(digestName, name);

   if (xstrcmp(name, "MD5") == 0)
   {
      expectedHashInfo = MD5_HASH_INFO;
      expectedHashInfoLen = MD5_HASH_INFO_LENGTH;
   }
   else if (xstrcmp(name, "SHA-1") == 0)
   {
      expectedHashInfo = SHA1_HASH_INFO;
      expectedHashInfoLen = SHA1_HASH_INFO_LENGTH;
   }
   else if (xstrcmp(name, "SHA-256") == 0)
   {
      expectedHashInfo = SHA256_HASH_INFO;
      expectedHashInfoLen = SHA256_HASH_INFO_LENGTH;
   }
   else
   {
      throwException(currentContext, RuntimeException, "Algorithm %s was not found", name);
      return 0;
   }

   if (dataLen != (digestLen + expectedHashInfoLen) || memcmp(data, expectedHashInfo, expectedHashInfoLen) != 0)
      return -1;

   if ((*buf = xmalloc(digestLen)) == null)
   {
      throwException(currentContext, OutOfMemoryError, null);
      return 0;
   }
   xmemmove(*buf, data + expectedHashInfoLen, digestLen);

   return digestLen;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tcsPKCS1S_init(NMParams p) // totalcross/crypto/signature/PKCS1Signature native private void init();
{
   TCObject pkcs1Obj = p->obj[0];
   TCObject signatureObj;

   if ((signatureObj = createByteArray(p->currentContext, sizeof(RSA_CTX))) == null)
      goto cleanup;
   *Signature_signatureRef(pkcs1Obj) = signatureObj;

cleanup:
   if (signatureObj)
      setObjectLock(signatureObj, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcsPKCS1S_finalize(NMParams p) // totalcross/crypto/signature/PKCS1Signature native protected final void finalize();
{
   TCObject pkcs1Obj = p->obj[0];
   TCObject signatureObj = *Signature_signatureRef(pkcs1Obj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(signatureObj);

   if (ctx->m) // initialized?
      RSA_free_external(ctx);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcsPKCS1S_doReset(NMParams p) // totalcross/crypto/signature/PKCS1Signature native protected final void doReset() throws totalcross.crypto.CryptoException;
{
   TCObject pkcs1Obj = p->obj[0];
   int32 operation = *Signature_operation(pkcs1Obj);
   TCObject key = *Signature_key(pkcs1Obj);
   TCObject signatureObj = *Signature_signatureRef(pkcs1Obj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(signatureObj);
   TCObject e, d, n;
   
   if (ctx->m) // already initialized?
   {
      RSA_free_external(ctx);
      ctx->d = null; // necessary to avoid freeing ctx->d when it is not allocated (in public keys)
   }

   if (operation == OPERATION_SIGN)
   {
      e = RSAPrivateKey_e(key);
      d = RSAPrivateKey_d(key);
      n = RSAPrivateKey_n(key);

      RSA_priv_key_new_external(ctx, ARRAYOBJ_START(n), ARRAYOBJ_LEN(n), ARRAYOBJ_START(e), ARRAYOBJ_LEN(e), ARRAYOBJ_START(d), ARRAYOBJ_LEN(d));
   }
   else
   {
      e = RSAPublicKey_e(key);
      n = RSAPublicKey_n(key);

      RSA_pub_key_new_external(ctx, ARRAYOBJ_START(n), ARRAYOBJ_LEN(n), ARRAYOBJ_START(e), ARRAYOBJ_LEN(e));
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcsPKCS1S_doSign_B(NMParams p) // totalcross/crypto/signature/PKCS1Signature native protected final byte[] doSign(byte []data) throws totalcross.crypto.CryptoException;
{
   TCObject pkcs1Obj = p->obj[0];
   TCObject dataObj = p->obj[1];
   TCObject key = *Signature_key(pkcs1Obj);
   TCObject signatureObj = *Signature_signatureRef(pkcs1Obj);
   TCObject digest = *PKCS1Signature_digest(pkcs1Obj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(signatureObj);

   uint8_t *hashedBuf=null, *encBuf=null;
   TCObject byteArrayResult;
   int32 count;
   TCObject msgDigest;

   msgDigest = getMessageDigest(p->currentContext, digest, dataObj); // compute the message digest
   if (p->currentContext->thrownException != null)
      return;

   count = createInfo(p->currentContext, digest, ARRAYOBJ_START(msgDigest), ARRAYOBJ_LEN(msgDigest), &hashedBuf); // prepend digest with the hash info
   if (p->currentContext->thrownException != null) // something went wrong createInfo has already thrown an exception.
      goto cleanup;

   if ((encBuf = xmalloc(ARRAYOBJ_LEN(RSAPrivateKey_n(key)))) == null)
   {
      throwException(p->currentContext, OutOfMemoryError, null);
      goto cleanup;
   }

   count = RSA_encrypt(ctx, hashedBuf, (uint16) count, encBuf, true); // encrypt the digest, obtaining the message signature
   if (count == -1)
      throwException(p->currentContext, CryptoException, "Signature error");
   else if ((byteArrayResult = createByteArray(p->currentContext, count)) != null)
   {
      xmemmove(ARRAYOBJ_START(byteArrayResult), encBuf, count);
      p->retO = byteArrayResult;
      setObjectLock(byteArrayResult, UNLOCKED);
   }

cleanup:
   if (hashedBuf != null)
      xfree(hashedBuf);
   if (encBuf != null)
      xfree(encBuf);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tcsPKCS1S_doVerify_BB(NMParams p) // totalcross/crypto/signature/PKCS1Signature native protected final boolean doVerify(byte []data, byte []signature) throws totalcross.crypto.CryptoException;
{
   TCObject pkcs1Obj = p->obj[0];
   TCObject dataObj = p->obj[1];
   TCObject signature = p->obj[2];
   TCObject key = *Signature_key(pkcs1Obj);
   TCObject signatureObj = *Signature_signatureRef(pkcs1Obj);
   TCObject digest = *PKCS1Signature_digest(pkcs1Obj);
   RSA_CTX *ctx = (RSA_CTX*) ARRAYOBJ_START(signatureObj);

   uint8 *sigDigestBuf=null, *decBuf=null;
   int32 digestLen, sigDigestLen;

   TCObject msgDigest;

   // 1. Get the original message digest from signature
   if ((decBuf = xmalloc(ARRAYOBJ_LEN(RSAPublicKey_n(key)))) == null)
   {
      throwException(p->currentContext, OutOfMemoryError, null);
      goto cleanup;
   }

   sigDigestLen = RSA_decrypt(ctx, ARRAYOBJ_START(signature), decBuf, ARRAYOBJ_LEN(RSAPublicKey_n(key)), false); // get the original decrypted signature package
   if (sigDigestLen == -1) // error decrypting
   {
      throwException(p->currentContext, CryptoException, "Signature error");
      goto cleanup;
   }

   sigDigestLen = retrieveInfo(p->currentContext, digest, decBuf, sigDigestLen, &sigDigestBuf); // get the original message digest
   if (sigDigestLen == -1) // error retrieving digest
   {
      throwException(p->currentContext, CryptoException, "Digest error");
      goto cleanup;
   }
   else if (p->currentContext->thrownException != null)
      goto cleanup;

   // 2. Get the message digest from current data
   msgDigest = getMessageDigest(p->currentContext, digest, dataObj); // compute the message digest
   if (p->currentContext->thrownException != null)
      goto cleanup;
   digestLen = ARRAYOBJ_LEN(msgDigest);

   // 3. Compare digests
   p->retI = (sigDigestLen == digestLen) && (xmemcmp(sigDigestBuf, ARRAYOBJ_START(msgDigest), digestLen) == 0); // compare digests

cleanup:
   if (decBuf != null)
      xfree(decBuf);
   if (sigDigestBuf != null)
      xfree(sigDigestBuf);
}
