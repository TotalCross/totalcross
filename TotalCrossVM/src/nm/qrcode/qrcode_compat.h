// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_QRCODE_COMPAT_H
#define TC_QRCODE_COMPAT_H

/*
 * CMake sets TC_QRCODE_USE_QRCODEGEN to 0 or 1 and compiles the matching
 * implementation source.
 */
#ifndef TC_QRCODE_USE_QRCODEGEN
  #error "TC_QRCODE_USE_QRCODEGEN must be defined by the build configuration"
#endif

#if TC_QRCODE_USE_QRCODEGEN != 0 && TC_QRCODE_USE_QRCODEGEN != 1
  #error "TC_QRCODE_USE_QRCODEGEN must be 0 or 1"
#endif

#if TC_QRCODE_USE_QRCODEGEN

/* tcvm.h already provides bool, true, and false. */
#ifndef __bool_true_false_are_defined
  #define __bool_true_false_are_defined 1
#endif
#include "win/qrcodegen.h"

typedef struct TCQRCode {
    uint8_t modules[qrcodegen_BUFFER_LEN_MAX];
    uint8_t temporary[qrcodegen_BUFFER_LEN_MAX];
} TCQRCode;

static void tcQRCodeInitText(TCQRCode* qrcode, uint8_t version, uint8_t ecc, const char* text) {
    qrcodegen_encodeText(text, qrcode->temporary, qrcode->modules,
        (enum qrcodegen_Ecc)ecc, version, version, qrcodegen_Mask_AUTO, true);
}

static int tcQRCodeGetSize(const TCQRCode* qrcode) {
    return qrcodegen_getSize(qrcode->modules);
}

static bool tcQRCodeGetModule(const TCQRCode* qrcode, int x, int y) {
    return qrcodegen_getModule(qrcode->modules, x, y);
}

static void tcQRCodeDispose(TCQRCode* qrcode) {
    (void)qrcode;
}

#else

#include "qrcode.h"

typedef struct TCQRCode {
    QRCode code;
    uint8_t* modules;
} TCQRCode;

static void tcQRCodeInitText(TCQRCode* qrcode, uint8_t version, uint8_t ecc, const char* text) {
    qrcode->modules = xmalloc(sizeof(uint8_t) * qrcode_getBufferSize(version));
    qrcode_initText(&qrcode->code, qrcode->modules, version, ecc, text);
}

static int tcQRCodeGetSize(const TCQRCode* qrcode) {
    return qrcode->code.size;
}

static bool tcQRCodeGetModule(const TCQRCode* qrcode, int x, int y) {
    return qrcode_getModule((QRCode*)&qrcode->code, (uint8_t)x, (uint8_t)y);
}

static void tcQRCodeDispose(TCQRCode* qrcode) {
    xfree(qrcode->modules);
}

#endif

#endif
