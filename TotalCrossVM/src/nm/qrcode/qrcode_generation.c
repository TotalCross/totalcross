#include "tcvm.h"
#ifdef _MSC_VER
	#include "win/qrcodegen.h"
#else
	#include "qrcode.h"
#endif

TC_API void tqQRC_nativeGetBytes_sii(NMParams p) {
	uint8_t version = p->i32[1];
	uint8_t ecc = p->i32[0]; //Error correction
	uint8_t* qrcodeBytes;
	TCObject text = p->obj[1];
	int y;
	int x;
	int32 size;
	TCObject byteMatrix;
	TCObjectArray out;
	CharP textChars = String2CharP(text);
	{
#if defined WIN32 || defined WINCE
		uint8_t qrcode[qrcodegen_BUFFER_LEN_MAX];
		uint8_t tempBuffer[qrcodegen_BUFFER_LEN_MAX];

		qrcodegen_encodeText(textChars, tempBuffer, qrcode, ecc, version, version, qrcodegen_Mask_AUTO,
							 true);
		size = qrcodegen_getSize(qrcode);
#else
		QRCode qrcode;
		qrcodeBytes = xmalloc(sizeof(uint8_t) * qrcode_getBufferSize(version));
		qrcode_initText(&qrcode, qrcodeBytes, version, ecc, textChars);
		xfree(textChars);
		size = qrcode.size;
#endif
		byteMatrix = createArrayObject(p->currentContext, "[[&B", size);
		out = (TCObjectArray)ARRAYOBJ_START(byteMatrix);
		for (y = 0; y < size; y++, out++) {
			*out = createByteArray(p->currentContext, size);
			{
				uint8_t* byteArrayWithInfoForTheQRCode = ARRAYOBJ_START(*out);
				for (x = 0; x < size; x++) {
#if defined WIN32 || defined WINCE
					if (qrcodegen_getModule(qrcode, x, y)) {
#else
					if (qrcode_getModule(&qrcode, x, y)) {
#endif

						byteArrayWithInfoForTheQRCode[x] = 0xFF; //11111111
					} else {
						byteArrayWithInfoForTheQRCode[x] = 0; //00000000
					}
				}
			}
			setObjectLock(*out, UNLOCKED);
		}
	}
#ifdef _MSC_VER

#else
	xfree(qrcodeBytes);
#endif
	p->retO = byteMatrix;
	setObjectLock(p->retO, UNLOCKED);
}
//private void getBytes(int module, int errorCorrection, String text, byte[] qrcodeByte);