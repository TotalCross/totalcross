/*	$OpenBSD: pkcs5_pbkdf2.c,v 1.10 2017/04/18 04:06:21 deraadt Exp $	*/

/*-
 * Copyright (c) 2008 Damien Bergamini <damien.bergamini@free.fr>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#ifndef WINCE
#include <sys/types.h>
#endif

#include <string.h>
#if !defined WIN32 || defined WINCE
#include <stdint.h>
#endif
#include <stdlib.h>
//#include <util.h>

/**
 * use os_port.h defines instead of sha1.h
 */
//#include <sha1.h>
#include "os_port.h"

/**
 * openbsd compat
 */
#ifndef explicit_bzero
#define explicit_bzero(a, b) memset(a, 0, b)
#endif
#ifndef freezero
#define freezero(a, b) free(a)
#endif

#include "crypto.h"
#define SHA1_DIGEST_LENGTH SHA1_SIZE

#ifndef MINIMUM
#define	MINIMUM(a,b) (((a) < (b)) ? (a) : (b))
#endif

#if 0

#if defined(_WIN32)
typedef uint8_t u_int8_t;
typedef uint16_t u_int16_t;
typedef uint32_t u_int32_t;
#endif

/*
 * HMAC-SHA-1 (from RFC 2202).
 */
static void
hmac_sha1(const u_int8_t *text, size_t text_len, const u_int8_t *key,
    size_t key_len, u_int8_t digest[SHA1_DIGEST_LENGTH])
{
	SHA1_CTX ctx;
	u_int8_t k_pad[SHA1_BLOCK_LENGTH];
	u_int8_t tk[SHA1_DIGEST_LENGTH];
	int i;

	if (key_len > SHA1_BLOCK_LENGTH) {
		SHA1Init(&ctx);
		SHA1Update(&ctx, key, key_len);
		SHA1Final(tk, &ctx);

		key = tk;
		key_len = SHA1_DIGEST_LENGTH;
	}

	bzero(k_pad, sizeof k_pad);
	bcopy(key, k_pad, key_len);
	for (i = 0; i < SHA1_BLOCK_LENGTH; i++)
		k_pad[i] ^= 0x36;

	SHA1Init(&ctx);
	SHA1Update(&ctx, k_pad, SHA1_BLOCK_LENGTH);
	SHA1Update(&ctx, text, text_len);
	SHA1Final(digest, &ctx);

	bzero(k_pad, sizeof k_pad);
	bcopy(key, k_pad, key_len);
	for (i = 0; i < SHA1_BLOCK_LENGTH; i++)
		k_pad[i] ^= 0x5c;

	SHA1Init(&ctx);
	SHA1Update(&ctx, k_pad, SHA1_BLOCK_LENGTH);
	SHA1Update(&ctx, digest, SHA1_DIGEST_LENGTH);
	SHA1Final(digest, &ctx);
}
#endif

/*
 * Password-Based Key Derivation Function 2 (PKCS #5 v2.0).
 * Code based on IEEE Std 802.11-2007, Annex H.4.2.
 */
int
pkcs5_pbkdf2(const char *pass, size_t pass_len, const uint8_t *salt,
    size_t salt_len, uint8_t *key, size_t key_len, unsigned int rounds)
{
	uint8_t *asalt, obuf[SHA1_DIGEST_LENGTH];
	uint8_t d1[SHA1_DIGEST_LENGTH], d2[SHA1_DIGEST_LENGTH];
	unsigned int i, j;
	unsigned int count;
	size_t r;

	if (rounds < 1 || key_len == 0)
		return -1;
	if (salt_len == 0 || salt_len > SIZE_MAX - 4)
		return -1;
	if ((asalt = malloc(salt_len + 4)) == NULL)
		return -1;

	memcpy(asalt, salt, salt_len);

	for (count = 1; key_len > 0; count++) {
		asalt[salt_len + 0] = (count >> 24) & 0xff;
		asalt[salt_len + 1] = (count >> 16) & 0xff;
		asalt[salt_len + 2] = (count >> 8) & 0xff;
		asalt[salt_len + 3] = count & 0xff;
		hmac_sha1(asalt, salt_len + 4, pass, pass_len, d1);
		memcpy(obuf, d1, sizeof(obuf));

		for (i = 1; i < rounds; i++) {
			hmac_sha1(d1, sizeof(d1), pass, pass_len, d2);
			memcpy(d1, d2, sizeof(d1));
			for (j = 0; j < sizeof(obuf); j++)
				obuf[j] ^= d1[j];
		}

		r = MINIMUM(key_len, SHA1_DIGEST_LENGTH);
		memcpy(key, obuf, r);
		key += r;
		key_len -= r;
	};
	freezero(asalt, salt_len + 4);
	explicit_bzero(d1, sizeof(d1));
	explicit_bzero(d2, sizeof(d2));
	explicit_bzero(obuf, sizeof(obuf));

	return 0;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tcpPBKDF2WHSHA1F_generateSecretI(NMParams p) // totalcross/crypto/provider/PBKDF2WithHmacSHA1Factory native private byte[] generateSecretImpl(char []password, byte []salt, int iterations, int keyLength);
{
    TCObject passwordObj = p->obj[1];
    TCObject saltObj = p->obj[2];
    int32 iterations = p->i32[0];
    int32 keyLength = p->i32[1];
    TCObject keyObj;
    int32 passLen = ARRAYOBJ_LEN(passwordObj);
    CharP password;
    int32 ret;
    
    if ((keyObj = createByteArray(p->currentContext, keyLength / 8)) != null)
    {
        password = JCharP2CharP((JCharP)ARRAYOBJ_START(passwordObj), passLen);
        if (password != null) {
            ret = pkcs5_pbkdf2(password, passLen, ARRAYOBJ_START(saltObj), ARRAYOBJ_LEN(saltObj), ARRAYOBJ_START(keyObj), ARRAYOBJ_LEN(keyObj), iterations);
            xfree(password);
        }
		p->retO = keyObj;
        setObjectLock(keyObj, UNLOCKED);
    }
}
