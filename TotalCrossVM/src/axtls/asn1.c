/*
 *  Copyright(C) 2006 Cameron Rich
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * @file asn1.c
 *
 * Some primitive asn methods for extraction rsa modulus information. It also
 * is used for retrieving information from X.509 certificates.
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#if !defined(_WIN32_WCE)
#include <time.h>
#endif
#include "ssl.h"

#define SIG_OID_PREFIX_SIZE 8
#define SIG_IIS6_OID_SIZE   5

/* Must be an RSA algorithm with either SHA1 or MD5 for verifying to work */
static const uint8_t sig_oid_prefix[SIG_OID_PREFIX_SIZE] =
{
    0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01
};

static const uint8_t sig_iis6_oid[SIG_IIS6_OID_SIZE] =
{
    0x2b, 0x0e, 0x03, 0x02, 0x1d
};

/* CN, O, OU */
static const uint8_t g_dn_types[] = { 3, 10, 11 };

static int get_asn1_length(const uint8_t *buf, int *offset)
{
    int len, i;

    if (!(buf[*offset] & 0x80)) /* short form */
    {
        len = buf[(*offset)++];
    }
    else  /* long form */
    {
        int length_bytes = buf[(*offset)++]&0x7f;
        len = 0;
        for (i = 0; i < length_bytes; i++)
        {
            len <<= 8;
            len += buf[(*offset)++];
        }
    }

    return len;
}

/**
 * Skip the ASN1.1 object type and its length. Get ready to read the object's
 * data.
 */
int asn1_next_obj(const uint8_t *buf, int *offset, int obj_type)
{
    if (buf[*offset] != obj_type)
        return X509_NOT_OK;
    (*offset)++;
    return get_asn1_length(buf, offset);
}

/**
 * Skip over an ASN.1 object type completely. Get ready to read the next
 * object.
 */
int asn1_skip_obj(const uint8_t *buf, int *offset, int obj_type)
{
    int len;

    if (buf[*offset] != obj_type)
        return X509_NOT_OK;
    (*offset)++;
    len = get_asn1_length(buf, offset);
    *offset += len;
    return 0;
}

/**
 * Read an integer value for ASN.1 data
 * Note: This function allocates memory which must be freed by the user.
 */
int asn1_get_int(const uint8_t *buf, int *offset, uint8_t **object)
{
    int len;

    if ((len = asn1_next_obj(buf, offset, ASN1_INTEGER)) < 0)
        goto end_int_array;

    if (len > 1 && buf[*offset] == 0x00)    /* ignore the negative byte */
    {
        len--;
        (*offset)++;
    }

    *object = (uint8_t *)malloc(len+64);
    memcpy(*object, &buf[*offset], len);
    *offset += len;

end_int_array:
    return len;
}

/**
 * Get all the RSA private key specifics from an ASN.1 encoded file
 */
int asn1_get_private_key(const uint8_t *buf, int len, RSA_CTX **rsa_ctx)
{
    int offset = 7;
    uint8_t *modulus = NULL, *priv_exp = NULL, *pub_exp = NULL;
    int mod_len, priv_len, pub_len;
#ifdef CONFIG_BIGINT_CRT
    uint8_t *p = NULL, *q = NULL, *dP = NULL, *dQ = NULL, *qInv = NULL;
    int p_len, q_len, dP_len, dQ_len, qInv_len;
#endif

    /* not in der format */
    if (buf[0] != ASN1_SEQUENCE) /* basic sanity check */
    {
#ifdef CONFIG_SSL_FULL_MODE
        printf("Error: This is not a valid ASN.1 file\n");
#endif
        return X509_INVALID_PRIV_KEY;
    }

    /* initialise the RNG */
    RNG_initialize(buf, len);

    mod_len = asn1_get_int(buf, &offset, &modulus);
    pub_len = asn1_get_int(buf, &offset, &pub_exp);
    priv_len = asn1_get_int(buf, &offset, &priv_exp);

    if (mod_len <= 0 || pub_len <= 0 || priv_len <= 0)
        return X509_INVALID_PRIV_KEY;

#ifdef CONFIG_BIGINT_CRT
    p_len = asn1_get_int(buf, &offset, &p);
    q_len = asn1_get_int(buf, &offset, &q);
    dP_len = asn1_get_int(buf, &offset, &dP);
    dQ_len = asn1_get_int(buf, &offset, &dQ);
    qInv_len = asn1_get_int(buf, &offset, &qInv);

    if (p_len <= 0 || q_len <= 0 || dP_len <= 0 || dQ_len <= 0 || qInv_len <= 0)
        return X509_INVALID_PRIV_KEY;

    RSA_priv_key_new(rsa_ctx,
            modulus, mod_len, pub_exp, pub_len, priv_exp, priv_len,
            p, p_len, q, p_len, dP, dP_len, dQ, dQ_len, qInv, qInv_len);

    free(p);
    free(q);
    free(dP);
    free(dQ);
    free(qInv);
#else
    RSA_priv_key_new(rsa_ctx,
            modulus, mod_len, pub_exp, pub_len, priv_exp, priv_len);
#endif

    free(modulus);
    free(priv_exp);
    free(pub_exp);
    return X509_OK;
}

/**
 * Get the time of a certificate. Ignore minutes/seconds.
 */
static int asn1_get_utc_time_h(const uint8_t *buf, int *offset, time_h *t) // fdie@20090325 support certificate expiration dates beyond 2050
{
    int ret = X509_NOT_OK, len, t_offset;
    int time_type;
    int year, month, day, hour, min, sec;

    time_type = buf[(*offset)++];
    if (time_type != ASN1_UTC_TIME && time_type != ASN1_GENERALIZED_TIME)
       goto end_utc_time;

    len = get_asn1_length(buf, offset);
    t_offset = *offset;

    if (time_type == ASN1_UTC_TIME)
    {
       year = (buf[t_offset] - '0')*10 + (buf[t_offset+1] - '0');
       if (year <= 50)    /* 1951-2050 thing */
          year += 100;
       year += 1900;
       t_offset += 2;
    }
    else if (time_type == ASN1_GENERALIZED_TIME)
    {
       year = ((buf[t_offset  ] - '0')*10 + (buf[t_offset+1] - '0')) * 100 +
               (buf[t_offset+2] - '0')*10 + (buf[t_offset+3] - '0');
       t_offset += 4;
    }

    month = (buf[t_offset  ] - '0')*10 + (buf[t_offset+1] - '0');
    day   = (buf[t_offset+2] - '0')*10 + (buf[t_offset+3] - '0');
    hour  = (buf[t_offset+4] - '0')*10 + (buf[t_offset+5] - '0');
    min   = (buf[t_offset+6] - '0')*10 + (buf[t_offset+7] - '0');
    sec   = (buf[t_offset+8] - '0')*10 + (buf[t_offset+9] - '0');

    mk_time_h(t, year, month, day, hour, min, sec, 0);

    *offset += len;
    ret = X509_OK;

end_utc_time:
    return ret;
}

/**
 * Get the version type of a certificate (which we don't actually care about)
 */
static int asn1_version(const uint8_t *cert, int *offset, X509_CTX *x509_ctx)
{
    int ret = X509_NOT_OK;

    (*offset) += 2;        /* get past explicit tag */
    if (asn1_skip_obj(cert, offset, ASN1_INTEGER))
        goto end_version;

    ret = X509_OK;
end_version:
    return ret;
}

/**
 * Retrieve the notbefore and notafter certificate times.
 */
static int asn1_validity(const uint8_t *cert, int *offset, X509_CTX *x509_ctx)
{
    return (asn1_next_obj(cert, offset, ASN1_SEQUENCE) < 0 ||
              asn1_get_utc_time_h(cert, offset, &x509_ctx->not_before) ||
              asn1_get_utc_time_h(cert, offset, &x509_ctx->not_after));
}

/**
 * Get the components of a distinguished name
 */
static int asn1_get_oid_x520(const uint8_t *buf, int *offset)
{
    int dn_type = 0;
    int len;

    if ((len = asn1_next_obj(buf, offset, ASN1_OID)) < 0)
        goto end_oid;

    /* expect a sequence of 2.5.4.[x] where x is a one of distinguished name
       components we are interested in. */
    if (len == 3 && buf[(*offset)++] == 0x55 && buf[(*offset)++] == 0x04)
        dn_type = buf[(*offset)++];
    else
    {
        *offset += len;     /* skip over it */
    }

end_oid:
    return dn_type;
}

/**
 * Obtain an ASN.1 printable string type.
 */
static int asn1_get_printable_str(const uint8_t *buf, int *offset, char **str)
{
    int len = X509_NOT_OK;

    /* some certs have this awful crud in them for some reason */
    if (buf[*offset] != ASN1_PRINTABLE_STR &&
            buf[*offset] != ASN1_TELETEX_STR &&
            buf[*offset] != ASN1_IA5_STR &&
            buf[*offset] != ASN1_UTF8_STR &&
            buf[*offset] != ASN1_UNICODE_STR)
    {
        //printf("bad printable type: %d\n", (int)buf[*offset]);
        goto end_pnt_str;
    }

    (*offset)++;
    len = get_asn1_length(buf, offset);

    if (buf[*offset - 1] == ASN1_UNICODE_STR)
    {
        int i;
        *str = (char *)malloc(len/2+1+64);     /* allow for null */

        for (i = 0; i < len; i += 2)
            (*str)[i/2] = buf[*offset + i + 1];

        (*str)[len/2] = 0;                  /* null terminate */
    }
    else
    {
        *str = (char *)malloc(len+1+64);       /* allow for null */
        memcpy(*str, &buf[*offset], len);
        (*str)[len] = 0;                    /* null terminate */
    }

    *offset += len;

end_pnt_str:
    return len;
}

/**
 * Get the subject name (or the issuer) of a certificate.
 */
static int asn1_name(const uint8_t *cert, int *offset, char *dn[])
{
    int ret = X509_NOT_OK;
    int dn_type;
    char *tmp = NULL;

    if (asn1_next_obj(cert, offset, ASN1_SEQUENCE) < 0)
        goto end_name;

    while (asn1_next_obj(cert, offset, ASN1_SET) >= 0)
    {
        int i, found = 0;

        if (asn1_next_obj(cert, offset, ASN1_SEQUENCE) < 0 ||
               (dn_type = asn1_get_oid_x520(cert, offset)) < 0)
            goto end_name;

        if (asn1_get_printable_str(cert, offset, &tmp) < 0)
        {
            free(tmp);
            goto end_name;
        }

        /* find the distinguished named type */
        for (i = 0; i < X509_NUM_DN_TYPES; i++)
        {
            //printf("compare type: %d val: (%s,%d)\n", g_dn_types[i], tmp, dn_type);
            if (dn_type == g_dn_types[i])
            {
                if (dn[i] == NULL)
                {
                    dn[i] = tmp;
                    found = 1;
                    break;
                }
            }
        }

        if (found == 0) /* not found so get rid of it */
        {
            free(tmp);
            //tmp = NULL; prevents fatal error on double free
        }
    }

    ret = X509_OK;
end_name:
    return ret;
}

/**
 * Read the modulus and public exponent of a certificate.
 */
int asn1_public_key(const uint8_t *cert, int *offset, X509_CTX *x509_ctx)
{
    int ret = X509_NOT_OK, mod_len, pub_len;
    uint8_t *modulus = NULL, *pub_exp = NULL;

    if (asn1_next_obj(cert, offset, ASN1_SEQUENCE) < 0 ||
            asn1_skip_obj(cert, offset, ASN1_SEQUENCE) ||
            asn1_next_obj(cert, offset, ASN1_BIT_STRING) < 0)
        goto end_pub_key;

    (*offset)++;        /* ignore the padding bit field */

    if (asn1_next_obj(cert, offset, ASN1_SEQUENCE) < 0)
        goto end_pub_key;

    mod_len = asn1_get_int(cert, offset, &modulus);
    pub_len = asn1_get_int(cert, offset, &pub_exp);

    RSA_pub_key_new(&x509_ctx->rsa_ctx, modulus, mod_len, pub_exp, pub_len);

    free(modulus);
    free(pub_exp);
    ret = X509_OK;

end_pub_key:
    return ret;
}

#ifdef CONFIG_SSL_CERT_VERIFICATION
/**
 * Read the signature of the certificate.
 */
static int asn1_signature(const uint8_t *cert, int *offset, X509_CTX *x509_ctx)
{
    int ret = X509_NOT_OK;

    if (cert[(*offset)++] != ASN1_BIT_STRING)
        goto end_sig;

    x509_ctx->sig_len = get_asn1_length(cert, offset)-1;
    (*offset)++;            /* ignore bit string padding bits */
    x509_ctx->signature = (uint8_t *)malloc(x509_ctx->sig_len+64);
    memcpy(x509_ctx->signature, &cert[*offset], x509_ctx->sig_len);
    *offset += x509_ctx->sig_len;
    ret = X509_OK;

end_sig:
    return ret;
}

/*
 * Compare 2 distinguished name components for equality
 * @return 0 if a match
 */
static int asn1_compare_dn_comp(const char *dn1, const char *dn2)
{
    int ret = 1;

    if ((dn1 && dn2 == NULL) || (dn1 == NULL && dn2)) goto err_no_match;

    ret = (dn1 && dn2) ? strcmp(dn1, dn2) : 0;

err_no_match:
    return ret;
}

/**
 * Clean up all of the CA certificates.
 */
void remove_ca_certs(CA_CERT_CTX *ca_cert_ctx)
{
    int i = 0;

    if (ca_cert_ctx == NULL)
        return;

    while (i < CONFIG_X509_MAX_CA_CERTS && ca_cert_ctx->cert[i])
    {
        x509_free(ca_cert_ctx->cert[i]);
        ca_cert_ctx->cert[i++] = NULL;
    }

    free(ca_cert_ctx);
}

/*
 * Compare 2 distinguished names for equality
 * @return 0 if a match
 */
static int asn1_compare_dn(char * const dn1[], char * const dn2[])
{
    int i;

    for (i = 0; i < X509_NUM_DN_TYPES; i++)
    {
        if (asn1_compare_dn_comp(dn1[i], dn2[i]))
            return 1;
    }

    return 0;       /* all good */
}

/**
 * Retrieve the signature from a certificate.
 */
const uint8_t *x509_get_signature(const uint8_t *asn1_sig, int *len)
{
    int offset = 0;
    const uint8_t *ptr = NULL;

    if (asn1_next_obj(asn1_sig, &offset, ASN1_SEQUENCE) < 0 ||
            asn1_skip_obj(asn1_sig, &offset, ASN1_SEQUENCE))
        goto end_get_sig;

    if (asn1_sig[offset++] != ASN1_OCTET_STRING)
        goto end_get_sig;
    *len = get_asn1_length(asn1_sig, &offset);
    ptr = &asn1_sig[offset];          /* all ok */

end_get_sig:
    return ptr;
}

#endif

/**
 * Read the signature type of the certificate. We only support RSA-MD5 and
 * RSA-SHA1 signature types.
 */
static int asn1_signature_type(const uint8_t *cert,
                                int *offset, X509_CTX *x509_ctx)
{
    int ret = X509_NOT_OK, len;

    if (cert[(*offset)++] != ASN1_OID)
        goto end_check_sig;

    len = get_asn1_length(cert, offset);

    if (len == 5 && memcmp(sig_iis6_oid, &cert[*offset],
                                    SIG_IIS6_OID_SIZE) == 0)
    {
        x509_ctx->sig_type = SIG_TYPE_SHA1;
    }
    else
    {
        if (memcmp(sig_oid_prefix, &cert[*offset], SIG_OID_PREFIX_SIZE))
            goto end_check_sig;     /* unrecognised cert type */

        x509_ctx->sig_type = cert[*offset + SIG_OID_PREFIX_SIZE];
    }

    *offset += len;
    asn1_skip_obj(cert, offset, ASN1_NULL); /* if it's there */
    ret = X509_OK;

end_check_sig:
    return ret;
}

/**
 * Construct a new x509 object.
 * @return 0 if ok. < 0 if there was a problem.
 */
int x509_new(const uint8_t *cert, int *len, X509_CTX **ctx)
{
    int begin_tbs, end_tbs;
    int ret = X509_NOT_OK, offset = 0, cert_size = 0;
    X509_CTX *x509_ctx;
    BI_CTX *bi_ctx;

    *ctx = (X509_CTX *)calloc(1, sizeof(X509_CTX));
    x509_ctx = *ctx;

    /* get the certificate size */
    asn1_skip_obj(cert, &cert_size, ASN1_SEQUENCE);

    if (asn1_next_obj(cert, &offset, ASN1_SEQUENCE) < 0)
        goto end_cert;

    begin_tbs = offset;         /* start of the tbs */
    end_tbs = begin_tbs;        /* work out the end of the tbs */
    asn1_skip_obj(cert, &end_tbs, ASN1_SEQUENCE);

    if (asn1_next_obj(cert, &offset, ASN1_SEQUENCE) < 0)
        goto end_cert;

    if (cert[offset] == ASN1_EXPLICIT_TAG)   /* optional version */
    {
        if (asn1_version(cert, &offset, x509_ctx))
            goto end_cert;
        }

    if (asn1_skip_obj(cert, &offset, ASN1_INTEGER) || /* serial number */
            asn1_next_obj(cert, &offset, ASN1_SEQUENCE) < 0)
        goto end_cert;

    /* make sure the signature is ok */
    if (asn1_signature_type(cert, &offset, x509_ctx))
    {
        //printf("Error: X509 unsupported digest\n");
        ret = X509_VFY_ERROR_UNSUPPORTED_DIGEST;
        goto end_cert;
    }

#if 1
    if (asn1_name(cert, &offset, x509_ctx->ca_cert_dn) ||
            asn1_validity(cert, &offset, x509_ctx) ||
            asn1_name(cert, &offset, x509_ctx->cert_dn) ||
            asn1_public_key(cert, &offset, x509_ctx))
    {
        //printf("Error: X509 bad something\n");
        goto end_cert;
    }
#else
    if (asn1_name(cert, &offset, x509_ctx->ca_cert_dn))
    {
        printf("Error: X509 invalid cacert dn\n");
        goto end_cert;
    }

    if (asn1_validity(cert, &offset, x509_ctx))
    {
        printf("Error: X509 invalid validity\n");
        goto end_cert;
    }

    if (asn1_name(cert, &offset, x509_ctx->cert_dn))
    {
        printf("Error: X509 invalid cert dn\n");
        goto end_cert;
    }
    if (asn1_public_key(cert, &offset, x509_ctx))
    {
        printf("Error: X509 invalid key\n");
        goto end_cert;
    }
#endif

    bi_ctx = x509_ctx->rsa_ctx->bi_ctx;

#ifdef CONFIG_SSL_CERT_VERIFICATION /* only care if doing verification */
    /* use the appropriate signature algorithm (either SHA1 or MD5) */
    if (x509_ctx->sig_type == SIG_TYPE_MD5)
    {
        MD5_CTX md5_ctx;
        uint8_t md5_dgst[MD5_SIZE];
        MD5Init(&md5_ctx);
        MD5Update(&md5_ctx, &cert[begin_tbs], end_tbs-begin_tbs);
        MD5Final(&md5_ctx, md5_dgst);
        x509_ctx->digest = bi_import(bi_ctx, md5_dgst, MD5_SIZE);
    }
    else if (x509_ctx->sig_type == SIG_TYPE_MD2)
    {
        MD2_CTX md2_ctx;
        uint8_t md2_dgst[MD2_SIZE];
        MD2Init(&md2_ctx);
        MD2Update(&md2_ctx, &cert[begin_tbs], end_tbs-begin_tbs);
        MD2Final(&md2_ctx, md2_dgst);
        x509_ctx->digest = bi_import(bi_ctx, md2_dgst, MD2_SIZE);
    }
    else if (x509_ctx->sig_type == SIG_TYPE_SHA1)
    {
        SHA1_CTX sha_ctx;
        uint8_t sha_dgst[SHA1_SIZE];
        SHA1Init(&sha_ctx);
        SHA1Update(&sha_ctx, &cert[begin_tbs], end_tbs-begin_tbs);
        SHA1Final(&sha_ctx, sha_dgst);
        x509_ctx->digest = bi_import(bi_ctx, sha_dgst, SHA1_SIZE);
    }

    offset = end_tbs;   /* skip the v3 data */
    if (asn1_skip_obj(cert, &offset, ASN1_SEQUENCE) ||
            asn1_signature(cert, &offset, x509_ctx))
    {
        //printf("Error: X509 bad signature\n");
        goto end_cert;
    }
#endif

    if (len)
    {
        *len = cert_size;
    }

    ret = X509_OK;
end_cert:

#ifdef CONFIG_SSL_FULL_MODE
    if (ret)
    {
        printf("Error: Invalid X509 ASN.1 file\n");
        //x509_print(NULL, x509_ctx);
    }
#endif

    return ret;
}

/**
 * Free an X.509 object's resources.
 */
void x509_free(X509_CTX *x509_ctx)
{
    X509_CTX *next;
    int i;

    if (x509_ctx == NULL)       /* if already null, then don't bother */
        return;

    for (i = 0; i < X509_NUM_DN_TYPES; i++)
    {
        free(x509_ctx->ca_cert_dn[i]);
        free(x509_ctx->cert_dn[i]);
    }

    free(x509_ctx->signature);

#ifdef CONFIG_SSL_CERT_VERIFICATION
    if (x509_ctx->digest)
    {
        bi_free(x509_ctx->rsa_ctx->bi_ctx, x509_ctx->digest);
    }
#endif

    RSA_free(x509_ctx->rsa_ctx);

    next = x509_ctx->next;
    free(x509_ctx);
    x509_free(next);        /* clear the chain */
}

#ifdef CONFIG_SSL_CERT_VERIFICATION
/**
 * Do some basic checks on the certificate chain.
 *
 * Certificate verification consists of a number of checks:
 * - A root certificate exists in the certificate store.
 * - The date of the certificate is after the start date.
 * - The date of the certificate is before the finish date.
 * - The certificate chain is valid.
 * - That the certificate(s) are not self-signed.
 * - The signature of the certificate is valid.
 */
int x509_verify(const CA_CERT_CTX *ca_cert_ctx, const X509_CTX *cert)
{
    int ret = X509_OK, i = 0;
    bigint *cert_sig;
    X509_CTX *next_cert = NULL;
    BI_CTX *ctx;
    bigint *mod, *expn;
    time_h now;
    int match_ca_cert = 0;

    if (cert == NULL || ca_cert_ctx == NULL)
    {
        ret = X509_VFY_ERROR_NO_TRUSTED_CERT;
        goto end_verify;
    }

    /* last cert in the chain - look for a trusted cert */
    if (cert->next == NULL)
    {
        while (i < CONFIG_X509_MAX_CA_CERTS && ca_cert_ctx->cert[i])
        {
            if (asn1_compare_dn(cert->ca_cert_dn,
                                        ca_cert_ctx->cert[i]->cert_dn) == 0)
            {
                match_ca_cert = 1;
                break;
            }

            i++;
        }

        if (i < CONFIG_X509_MAX_CA_CERTS && ca_cert_ctx->cert[i])
        {
            next_cert = ca_cert_ctx->cert[i];
        }
        else    /* trusted cert not found */
        {
            ret = X509_VFY_ERROR_NO_TRUSTED_CERT;
            goto end_verify;
        }
    }
    else
    {
        next_cert = cert->next;
    }

    getNowUTC(&now);

    /* check the not before date */
    if (isBefore(&now, &cert->not_before))
    {
        ret = X509_VFY_ERROR_NOT_YET_VALID;
        goto end_verify;
    }

    /* check the not after date */
    if (isBefore(&cert->not_after, &now))
    {
        ret = X509_VFY_ERROR_EXPIRED;
        goto end_verify;
    }

    /* check the chain integrity */
    if (asn1_compare_dn(cert->ca_cert_dn, next_cert->cert_dn))
    {
        ret = X509_VFY_ERROR_INVALID_CHAIN;
        goto end_verify;
    }

    /* check for self-signing */
    if (!match_ca_cert && asn1_compare_dn(cert->ca_cert_dn, cert->cert_dn) == 0)
    {
        ret = X509_VFY_ERROR_SELF_SIGNED;
        goto end_verify;
    }

    /* check the signature */
    ctx = cert->rsa_ctx->bi_ctx;
    mod = next_cert->rsa_ctx->m;
    expn = next_cert->rsa_ctx->e;
    cert_sig = RSA_sign_verify(ctx, cert->signature, cert->sig_len,
            bi_clone(ctx, mod), bi_clone(ctx, expn));

    if (cert_sig)
    {
        ret = cert->digest ?    /* check the signature */
            bi_compare(cert_sig, cert->digest) :
            X509_VFY_ERROR_UNSUPPORTED_DIGEST;
        bi_free(ctx, cert_sig);

        if (ret)
            goto end_verify;
    }
    else
    {
        ret = X509_VFY_ERROR_BAD_SIGNATURE;
        goto end_verify;
    }

    /* go down the certificate chain using recursion. */
    if (ret == 0 && cert->next)
    {
        ret = x509_verify(ca_cert_ctx, next_cert);
    }

end_verify:
    return ret;
}
#endif

#if defined (CONFIG_SSL_FULL_MODE)
/**
 * Used for diagnostics.
 */
void x509_print(CA_CERT_CTX *ca_cert_ctx, const X509_CTX *cert)
{
    char buffer[TIME_H_STR_MAXLEN+1];

    if (cert == NULL)
        return;

    printf("----------------   CERT DEBUG   ----------------\n");
    printf("* CA Cert Distinguished Name\n");
    if (cert->ca_cert_dn[X509_COMMON_NAME])
    {
        printf("Common Name (CN):\t%s\n", cert->ca_cert_dn[X509_COMMON_NAME]);
    }

    if (cert->ca_cert_dn[X509_ORGANIZATION])
    {
        printf("Organization (O):\t%s\n", cert->ca_cert_dn[X509_ORGANIZATION]);
    }

    if (cert->ca_cert_dn[X509_ORGANIZATIONAL_TYPE])
    {
        printf("Organizational Unit (OU): %s\n",
                cert->ca_cert_dn[X509_ORGANIZATIONAL_TYPE]);
    }

    printf("* Cert Distinguished Name\n");
    if (cert->cert_dn[X509_COMMON_NAME])
    {
        printf("Common Name (CN):\t%s\n", cert->cert_dn[X509_COMMON_NAME]);
    }

    if (cert->cert_dn[X509_ORGANIZATION])
    {
        printf("Organization (O):\t%s\n", cert->cert_dn[X509_ORGANIZATION]);
    }

    if (cert->cert_dn[X509_ORGANIZATIONAL_TYPE])
    {
        printf("Organizational Unit (OU): %s\n",
                cert->cert_dn[X509_ORGANIZATIONAL_TYPE]);
    }

    printf("Not Before:\t\t%s\n", asc_time_h(&cert->not_before, buffer));
    printf("Not After:\t\t%s\n", asc_time_h(&cert->not_after, buffer));

    if (cert->rsa_ctx != NULL)
        printf("RSA bitsize:\t\t%d\n", cert->rsa_ctx->num_octets*8);
    printf("Sig Type:\t\t");
    switch (cert->sig_type)
    {
        case SIG_TYPE_MD5:
            printf("MD5\n");
            break;
        case SIG_TYPE_SHA1:
            printf("SHA1\n");
            break;
        case SIG_TYPE_MD2:
            printf("MD2\n");
            break;
        default:
            printf("Unrecognized: %d\n", cert->sig_type);
            break;
    }

    printf("Verify:\t\t\t");

    if (ca_cert_ctx)
    {
        x509_display_error(x509_verify(ca_cert_ctx, cert));
    }

    printf("\n");
#if 0
    print_blob("Signature", cert->signature, cert->sig_len);
    bi_print("Modulus", cert->rsa_ctx->m);
    bi_print("Pub Exp", cert->rsa_ctx->e);
#endif

    if (ca_cert_ctx)
    {
        x509_print(ca_cert_ctx, cert->next);
    }
}

void x509_display_error(int error)
{
    switch (error)
    {
        case X509_NOT_OK:
            printf("X509 not ok");
            break;

        case X509_VFY_ERROR_NO_TRUSTED_CERT:
            printf("No trusted cert is available");
            break;

        case X509_VFY_ERROR_BAD_SIGNATURE:
            printf("Bad signature");
            break;

        case X509_VFY_ERROR_NOT_YET_VALID:
            printf("Cert is not yet valid");
            break;

        case X509_VFY_ERROR_EXPIRED:
            printf("Cert has expired");
            break;

        case X509_VFY_ERROR_SELF_SIGNED:
            printf("Cert is self-signed");
            break;

        case X509_VFY_ERROR_INVALID_CHAIN:
            printf("Chain is invalid (check order of certs)");
            break;

        case X509_VFY_ERROR_UNSUPPORTED_DIGEST:
            printf("Unsupported digest");
            break;

        case X509_INVALID_PRIV_KEY:
            printf("Invalid private key");
            break;
    }
}
#endif      /* CONFIG_SSL_FULL_MODE */

