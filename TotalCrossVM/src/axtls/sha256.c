/**
 * SHA256 implementation - as defined in FIPS PUB 180-2 published April 17, 1995.
 * This code was originally taken from RFC4634
 */

#include <string.h>
#include "crypto.h"

/* Define the SHA shift, rotate left and rotate right macro */
#define SHA256_SHR(bits,word) ((word) >> (bits))
#define SHA256_ROTL(bits,word) (((word) << (bits)) | ((word) >> (32 - (bits))))
#define SHA256_ROTR(bits,word) (((word) >> (bits)) | ((word) << (32 - (bits))))

/* Define the SHA SIGMA and sigma macros */
#define SHA256_SIGMA0(word) (SHA256_ROTR(2, word) ^ SHA256_ROTR(13, word) ^ SHA256_ROTR(22, word))
#define SHA256_SIGMA1(word) (SHA256_ROTR(6, word) ^ SHA256_ROTR(11, word) ^ SHA256_ROTR(25, word))
#define SHA256_sigma0(word) (SHA256_ROTR(7, word) ^ SHA256_ROTR(18, word) ^ SHA256_SHR(3, word))
#define SHA256_sigma1(word) (SHA256_ROTR(17, word) ^ SHA256_ROTR(19, word) ^ SHA256_SHR(10, word))

/*
 * These definitions are defined in FIPS-180-2, section 4.1.
 * Ch() and Maj() are defined identically in sections 4.1.1,
 * 4.1.2 and 4.1.3.
 */
#define SHA_Ch(x,y,z) (((x) & (y)) ^ ((~(x)) & (z)))
#define SHA_Maj(x,y,z) (((x) & (y)) ^ ((x) & (z)) ^ ((y) & (z)))

/* ----- static functions ----- */
static void SHA256PadMessage(SHA256_CTX *ctx);
static void SHA256ProcessMessageBlock(SHA256_CTX *ctx);

/*
 * SHA256Init
 *
 * Description:
 *   This function will initialize the SHA256Context in preparation
 *   for computing a new SHA256 message digest.
 *
 * Parameters:
 *   ctx: [in/out]
 *     The context to reset.
 */
void SHA256Init(SHA256_CTX *ctx)
{
	ctx->Length_Low           = 0;
	ctx->Length_High          = 0;
	ctx->Message_Block_Index  = 0;

	ctx->Intermediate_Hash[0] = 0x6A09E667;
	ctx->Intermediate_Hash[1] = 0xBB67AE85;
	ctx->Intermediate_Hash[2] = 0x3C6EF372;
	ctx->Intermediate_Hash[3] = 0xA54FF53A;
	ctx->Intermediate_Hash[4] = 0x510E527F;
	ctx->Intermediate_Hash[5] = 0x9B05688C;
	ctx->Intermediate_Hash[6] = 0x1F83D9AB;
	ctx->Intermediate_Hash[7] = 0x5BE0CD19;
}

/*
 * SHA256Update
 *
 * Description:
 *   This function accepts an array of octets as the next portion
 *   of the message.
 *
 * Parameters:
 *   ctx: [in/out]
 *     The SHA context to update
 *   msg: [in]
 *     An array of characters representing the next portion of
 *     the message.
 *   len: [in]
 *     The length of the message in message_array
 */

void SHA256Update(SHA256_CTX *ctx, const uint8_t *msg, int len)
{
	while (len--)
	{
		ctx->Message_Block[ctx->Message_Block_Index++] = (*msg & 0xFF);
		ctx->Length_Low += 8;

		if (ctx->Length_Low == 0)
			ctx->Length_High++;

		if (ctx->Message_Block_Index == 64)
			SHA256ProcessMessageBlock(ctx);

		msg++;
	}
}

/*
 * SHA256Final
 *
 * Description:
 *   This function will return the 256-bit message
 *   digest into the Message_Digest array provided by the caller.
 *   NOTE: The first octet of hash is stored in the 0th element,
 *      the last octet of hash in the 32nd element.
 *
 * Parameters:
 *   ctx: [in/out]
 *     The context to use to calculate the SHA hash.
 *   digest: [out]
 *     Where the digest is returned.
 */
void SHA256Final(SHA256_CTX *ctx, uint8_t* digest)
{
	int i;

	SHA256PadMessage(ctx);
	memset(ctx->Message_Block, 0, 64);
    ctx->Length_Low = 0;    /* and clear length */
    ctx->Length_High = 0;

	for (i = 0; i < SHA256_SIZE; ++i)
		digest[i] = ctx->Intermediate_Hash[i >> 2] >> 8 * ( 3 - ( i & 0x03 ) );
}

static void SHA256PadMessage(SHA256_CTX *ctx)
{
	/*
	* Check to see if the current message block is too small to hold
	* the initial padding bits and length. If so, we will pad the
	* block, process it, and then continue padding into a second
	* block.
	*/
	if (ctx->Message_Block_Index > 55)
	{
		ctx->Message_Block[ctx->Message_Block_Index++] = 0x80;

		while (ctx->Message_Block_Index < 64)
			ctx->Message_Block[ctx->Message_Block_Index++] = 0;

		SHA256ProcessMessageBlock(ctx);
	}
	else
		ctx->Message_Block[ctx->Message_Block_Index++] = 0x80;

	while (ctx->Message_Block_Index < 56)
		ctx->Message_Block[ctx->Message_Block_Index++] = 0;

	/*
	* Store the message length as the last 8 octets
	*/
	ctx->Message_Block[56] = (uint8_t)(ctx->Length_High >> 24);
	ctx->Message_Block[57] = (uint8_t)(ctx->Length_High >> 16);
	ctx->Message_Block[58] = (uint8_t)(ctx->Length_High >> 8);
	ctx->Message_Block[59] = (uint8_t)(ctx->Length_High);
	ctx->Message_Block[60] = (uint8_t)(ctx->Length_Low >> 24);
	ctx->Message_Block[61] = (uint8_t)(ctx->Length_Low >> 16);
	ctx->Message_Block[62] = (uint8_t)(ctx->Length_Low >> 8);
	ctx->Message_Block[63] = (uint8_t)(ctx->Length_Low);

	SHA256ProcessMessageBlock(ctx);
}

static void SHA256ProcessMessageBlock(SHA256_CTX *ctx)
{
	/* Constants defined in FIPS-180-2, section 4.2.2 */
	static const uint32_t K[64] =
	{
		0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b,
		0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01,
		0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7,
		0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
		0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152,
		0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147,
		0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc,
		0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
		0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819,
		0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08,
		0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f,
		0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
		0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
	};

	int        t;                   /* Loop counter */
	uint32_t   temp1, temp2;            /* Temporary word value */
	uint32_t   W[64];                   /* Word sequence */
	uint32_t   A, B, C, D, E, F, G, H;  /* Word buffers */

	/*
	* Initialize the first 16 words in the array W
	*/
	for (t = 0; t < 16; t++)
	{
		W[t] = (((uint32_t)ctx->Message_Block[t * 4]) << 24);
		W[t] |= (((uint32_t)ctx->Message_Block[t * 4 + 1]) << 16);
		W[t] |= (((uint32_t)ctx->Message_Block[t * 4 + 2]) << 8);
		W[t] |= (((uint32_t)ctx->Message_Block[t * 4 + 3]));
	}

	for (t = 16; t < 64; t++)
		W[t] = SHA256_sigma1(W[t-2]) + W[t-7] + SHA256_sigma0(W[t-15]) + W[t-16];

	A = ctx->Intermediate_Hash[0];
	B = ctx->Intermediate_Hash[1];
	C = ctx->Intermediate_Hash[2];
	D = ctx->Intermediate_Hash[3];
	E = ctx->Intermediate_Hash[4];
	F = ctx->Intermediate_Hash[5];
	G = ctx->Intermediate_Hash[6];
	H = ctx->Intermediate_Hash[7];

	for (t = 0; t < 64; t++)
	{
		temp1 = H + SHA256_SIGMA1(E) + SHA_Ch(E,F,G) + K[t] + W[t];
		temp2 = SHA256_SIGMA0(A) + SHA_Maj(A,B,C);
		H = G;
		G = F;
		F = E;
		E = D + temp1;
		D = C;
		C = B;
		B = A;
		A = temp1 + temp2;
	}

	ctx->Intermediate_Hash[0] += A;
	ctx->Intermediate_Hash[1] += B;
	ctx->Intermediate_Hash[2] += C;
	ctx->Intermediate_Hash[3] += D;
	ctx->Intermediate_Hash[4] += E;
	ctx->Intermediate_Hash[5] += F;
	ctx->Intermediate_Hash[6] += G;
	ctx->Intermediate_Hash[7] += H;

	ctx->Message_Block_Index = 0;
}
