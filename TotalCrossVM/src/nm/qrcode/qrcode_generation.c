// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "qrcode_compat.h"

TC_API void tqQRC_nativeGetBytes_sii(NMParams p) {
    uint8_t version = p->i32[1];
    uint8_t ecc = p->i32[0]; //Error correction 
    TCObject text = p->obj[1];
    int y;
    int x;
    int32 size;
    TCObject byteMatrix;
    TCObjectArray out; 
    CharP textChars = String2CharP(text);
    TCQRCode qrcode;

    tcQRCodeInitText(&qrcode, version, ecc, textChars);
    xfree(textChars);

    size = tcQRCodeGetSize(&qrcode);
    byteMatrix = createArrayObject(p->currentContext, "[[&B", size);
    out = (TCObjectArray)ARRAYOBJ_START(byteMatrix);
    for(y = 0; y < size; y++, out++) {
        *out = createByteArray(p->currentContext, size);
        {
            uint8_t* byteArrayWithInfoForTheQRCode = ARRAYOBJ_START(*out);
            for(x = 0; x < size; x++) {
                if(tcQRCodeGetModule(&qrcode, x, y)) {
						byteArrayWithInfoForTheQRCode[x] = 0xFF; //11111111
                } else {
                    byteArrayWithInfoForTheQRCode[x] = 0; //00000000
                }
            }
        }
        setObjectLock(*out, UNLOCKED);
    }
    tcQRCodeDispose(&qrcode);
    p->retO = byteMatrix;
    setObjectLock(p->retO, UNLOCKED);
}
//private void getBytes(int module, int errorCorrection, String text, byte[] qrcodeByte);
