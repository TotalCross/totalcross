#ifndef __BBAPPAPI_H__BLUEBIRDSOFT
#define __BBAPPAPI_H__BLUEBIRDSOFT

#ifndef BBAPI
#ifdef BBPDA_EXPORTS
#define BBAPI __declspec(dllexport)
#else
#define BBAPI __declspec(dllimport)
#endif
#endif

#include "bberror.h"

#ifdef __cplusplus
extern "C" {
#endif
/* ------------------------------------------------------------------------- */

/*
 * CardReader
 */


#define BB_CARDREADER_READ_TRACK1	1
#define BB_CARDREADER_READ_TRACK2	2
#define BB_CARDREADER_READ_TRACK3	4

#define REG_MSR_VOLUME      					TEXT( "Drivers\\CardReader" )
#define RV_VOLUME								TEXT("Volume")
#define CARDREADER_VOLUME_EVENT					TEXT("CardReaderVolume")

typedef LPVOID HBBCARDREADER;
typedef void (WINAPI*CardReaderCallbackFunc)(DWORD dwArg, LPCTSTR data);

BBAPI HBBCARDREADER WINAPI BBCardReaderOpen(DWORD dwReserved);
BBAPI DWORD WINAPI BBCardReaderReadData(HBBCARDREADER hCardReader, int track, LPBYTE buffer, int nBufferSize);
BBAPI DWORD WINAPI BBCardReaderReadString(HBBCARDREADER hCardReader, int track, LPTSTR buf);
BBAPI DWORD WINAPI BBCardReaderStartAsyncReadData(HBBCARDREADER hCardReader, CardReaderCallbackFunc pfCallback, DWORD dwCallbackArg);
BBAPI DWORD WINAPI BBCardReaderStopAsyncReadData(HBBCARDREADER hCardReader);
BBAPI DWORD WINAPI BBCardReaderReadCardData(HBBCARDREADER hCardReader, int track, LPTSTR buffer, DWORD * pBufferSize);
BBAPI DWORD WINAPI BBCardReaderGetReadTrack(HBBCARDREADER hCardReader, DWORD *dwReadTrack);
BBAPI DWORD WINAPI BBCardReaderClose(HBBCARDREADER hCardReader);
BBAPI DWORD WINAPI BBCardReaderSetVolume( DWORD Volume);
BBAPI DWORD WINAPI BBCardReaderGetVolume( DWORD *Volume);
BBAPI DWORD WINAPI BBCardReaderEnableTrackSound( HBBCARDREADER hCardReader, int track, BOOL bOn );
/*
 * Printer
 */
#define BB_PRINTER_SEORO            0
#define BB_PRINTER_SEIKO            1
#define BB_PRINTER_NONE             2

#define BB_PRINT_DEFAULT			0
#define BB_PRINT_EMPHASIZE			1
#define BB_PRINT_CENTERALIGN		2
#define BB_PRINT_RIGHTALIGN			4
#define BB_PRINT_DOUBLEWIDTH		8
#define BB_PRINT_DOUBLEHEIGHT		16
#define BB_PRINT_DOUBLESIZE			24
#define BB_PRINT_UNDERLINE			32

#define BB_PRINT_DENSITY_60PERCENT	0
#define BB_PRINT_DENSITY_70PERCENT	1
#define BB_PRINT_DENSITY_80PERCENT	2
#define BB_PRINT_DENSITY_90PERCENT	3
#define BB_PRINT_DENSITY_100PERCENT	4
#define BB_PRINT_DENSITY_110PERCENT	5
#define BB_PRINT_DENSITY_120PERCENT	6
#define BB_PRINT_DENSITY_130PERCENT	7
#define BB_PRINT_DENSITY_140PERCENT	8
#define BB_PRINT_DENSITY_DEFAULT	BB_PRINT_DENSITY_90PERCENT
#define BB_PRINT_DENSITY_MIN		BB_PRINT_DENSITY_60PERCENT
#define BB_PRINT_DENSITY_MAX		BB_PRINT_DENSITY_140PERCENT

#define BB_PRINT_LINESPACING_ONESIXTH	24
#define BB_PRINT_LINESPACING_DEFAULT	24
#define BB_PRINT_LINESPACING_MAX		255

#define BB_PRINT_BARCODE_UPCA		65
#define BB_PRINT_BARCODE_JAN13		67
#define BB_PRINT_BARCODE_JAN8		68
#define BB_PRINT_BARCODE_CODE39		69
#define BB_PRINT_BARCODE_ITF		70
#define BB_PRINT_BARCODE_CODABAR	71
#define BB_PRINT_BARCODE_CODE93		72
#define BB_PRINT_BARCODE_CODE128	73
#define BB_PRINT_BARCODE_CODE128A   73
#define BB_PRINT_BARCODE_CODE128B   74
#define BB_PRINT_BARCODE_CODE128C   75
#define BB_PRINT_BARCODE_STF		76

#define BB_PRINT_BARCODE_WIDTH_THIN			2
#define BB_PRINT_BARCODE_WIDTH_NORMAL	    3
#define BB_PRINT_BARCODE_WIDTH_THICK	    4
#define BB_PRINT_BARCODE_WIDTH_MORETHICK    5
#define BB_PRINT_BARCODE_WIDTH_MOSTTHICK    6

#define BB_PRINT_BARCODE_ALIGN_LEFT			0
#define BB_PRINT_BARCODE_ALIGN_CENTER	    2
#define BB_PRINT_BARCODE_ALIGN_RIGHT	    4

#define BB_PRINT_BITMAP_NORMAL			    0
#define BB_PRINT_BITMAP_DOUBLE_HEIGHT	    1
#define BB_PRINT_BITMAP_DOUBLE_WIDTH	    2
#define BB_PRINT_BITMAP_DOUBLE			    3
#define BB_PRINT_BITMAP_ALIGN_CENTER		4
#define BB_PRINT_BITMAP_ALIGN_RIGHT 		8

#define BB_PRINT_FONTTYPE_42                0
#define BB_PRINT_FONTTYPE_40                1
#define BB_PRINT_FONTTYPE_32                2

#define BB_PRINT_FONTSET_ASCII              0
#define BB_PRINT_FONTSET_HANGUL             1
#define BB_PRINT_FONTSET_SYMBOL             2
#define BB_PRINT_FONTSET_THAI               3

enum
{
	BB_PRINT_ERROR_NONE			= 0,
	BB_PRINT_ERROR_NOPAPER		= 1,
	BB_PRINT_ERROR_NOBLACKMARK	= 2,
	BB_PRINT_ERROR_OVERHEAT		= 3,
	BB_PRINT_ERROR_UNKNOWN		= 4,
	// SEIKO PRINTER에서 추가된 Error 상황 . SEIKO Printer만 구동하는
	// 장치에서 쓸 수 있다.
	BB_PRINT_ERROR_INVALIDHANDLE = 5,  // 잘못된 HANDLE을 건드리는 경우
	BB_PRINT_ERROR_NOFONT        = 6,  // 글자에 해당하는 FONT가 없는 경우
	BB_PRINT_ERROR_READFILE      = 7,  // File을 읽을 경우에 생기는 Error
	BB_PRINT_ERROR_NOBITMAPFILE  = 8,  // Bitmap File이 없음
	BB_PRINT_ERROR_INVALIDBITMAPFILE = 9,   // 적절한 Bitmap File이 아님
	BB_PRINT_ERROR_SETDENSITY        = 10,  // SetDensity Error
	BB_PRINT_ERROR_SETDENSITY_OUTOFRANGE = 11, // 농도 범위를 벗어난 경우
	BB_PRINT_ERROR_GETDENSITY            = 12, // GetDensity Error
	BB_PRINT_ERROR_LOWTEMPERATURE        = 13, // 극 저온일 경우 Error
	BB_PRINT_ERROR_LOWVOLTAGE            = 14, // 극 저전압일 경우 Error
	BB_PRINT_ERROR_HIGHVOLTAGE           = 15, // 극 고전압일 경우 Error
	BB_PRINT_ERROR_SETLINESPACING        = 16,
	BB_PRINT_ERROR_GETLINESPACING        = 17
};

typedef struct _FONTINFO
{
	LONG      lfHeight;
    LONG      lfWidth;
    LONG      lfEscapement;
    LONG      lfOrientation;
    LONG      lfWeight;
    BYTE      lfItalic;
    BYTE      lfUnderline;
    BYTE      lfStrikeOut;
    BYTE      lfCharSet;
    BYTE      lfOutPrecision;
    BYTE      lfClipPrecision;
    BYTE      lfQuality;
    BYTE      lfPitchAndFamily;
    WCHAR     lfFaceName[LF_FACESIZE];
}FONTINFO, *LPFONTINFO;

typedef LPVOID HBBPRINTER;

BBAPI HBBPRINTER WINAPI BBPrinterOpen(DWORD dwReserved);
BBAPI DWORD WINAPI BBPrinterInit(HBBPRINTER hPrinter);

BBAPI DWORD WINAPI BBPrinterSetDensity(HBBPRINTER hPrinter, UCHAR ucDensity);
BBAPI DWORD WINAPI BBPrinterGetDensity(HBBPRINTER hPrinter, UCHAR *pucDensity);
BBAPI DWORD WINAPI BBPrinterSetLineSpacing(HBBPRINTER hPrinter, UCHAR ucLineSpacing);
BBAPI DWORD WINAPI BBPrinterGetLineSpacing(HBBPRINTER hPrinter, UCHAR *pucLineSpacing);

BBAPI DWORD WINAPI BBPrinterPrint(HBBPRINTER hPrinter, LPCTSTR lpcszData, DWORD dwOptions);
BBAPI DWORD WINAPI BBPrinterPrintBarcode(HBBPRINTER hPrinter, LPCTSTR lpcszData, UCHAR ucHeight,
			UCHAR ucWidth, UCHAR ucBarcodeSystem, UCHAR ucAlign);

BBAPI DWORD WINAPI BBPrinterPrintBitmap(HBBPRINTER hPrinter, LPCTSTR lpcszFilename, DWORD dwMode);
BBAPI DWORD WINAPI BBPrinterFeedUntilMark(HBBPRINTER hPrinter);
BBAPI DWORD WINAPI BBPrinterWaitUntilPrintEnd(HBBPRINTER hPrinter);
BBAPI DWORD WINAPI BBPrinterClose(HBBPRINTER hPrinter);

/*
    1200WL/1250 에서 추가된 함수
*/
BBAPI DWORD WINAPI BBPrinterRegisterFontType( HBBPRINTER hPrinter, UCHAR ucFontSubCode );
BBAPI DWORD WINAPI BBPrinterRegisterFontFromSet( HBBPRINTER hPrinter, UCHAR ucCodeOfSet, UCHAR ucFontSubCode );

/*
	DC를 이용한 프린터 출력	
*/

BBAPI DWORD WINAPI BBPrinterCreateDC ( HBBPRINTER hPrinter , UINT nWidth, UINT nHeight );
BBAPI DWORD WINAPI BBPrinterDeleteDC ( HBBPRINTER hPrinter );
BBAPI DWORD WINAPI BBPrinterPrintDC  ( HBBPRINTER hPrinter );
BBAPI DWORD WINAPI BBPrinterSetFont  ( HBBPRINTER hPrinter, LPFONTINFO lpFontInfo );	
BBAPI DWORD WINAPI BBPrinterDrawText ( HBBPRINTER hPrinter, LPCTSTR lpText, int nCount, int nLeft, int nTop, int nRight, int nBottom, UINT uFormat );
BBAPI DWORD WINAPI BBPrinterBitBlt   ( HBBPRINTER hPrinter, LPCTSTR lpFilePath, int nXDest, int nYDest, int nWidth, int nHeight, int xSrc, int ySrc, DWORD dwRop );


/*
*  Barcode
*/
//duration
#define WM_SCANDECODEDATA   ( WM_USER + 701 )
#define WM_SCANTRIGGER      ( WM_USER + 702 )
#define WM_REPORT_TO_APP	( WM_USER + 703 )

enum
{
	BB_BARCODE_RPT_USER_MSG,
};

enum
{
	BB_BARCODE_UM_BATTERY_LOW,
};

#define BB_BARCODE_DUR_SHORT 0
#define BB_BARCODE_DUR_LONG  1
#define BB_BARCODE_DUR_FASTWARBLE 2
#define BB_BARCODE_DUR_SLOWWARBLE 3
#define BB_BARCODE_DUR_MIX1 4
#define BB_BARCODE_DUR_MIX2 5
#define BB_BARCODE_DUR_MIX3 6
#define BB_BARCODE_DUR_MIX4 7

//return code
#define BB_BARCODE_SC_SUCCESS  0   // 일반적인 성공시의 code
#define BB_BARCODE_SC_ERR_NORMAL 1         // 일반적인 Error시의 code
#define BB_BARCODE_SC_ERR_SERIALPORT 2     // Serial Port와 관련된 Error
#define BB_BARCODE_SC_ERR_POWERMODE  3     // Power mode Setting Error
#define BB_BARCODE_SC_ERR_SCANENABLE 4     // SCAN_ENABLE/SCAN_DISABLE Setting Error
#define BB_BARCODE_SC_ERR_SCANDISABLE 5    // SCAN이 가능하지 않은 경우 내는 Error
#define BB_BARCODE_SC_ERR_STARTDECODE 6    // START_DECODE Error
#define	BB_BARCODE_SC_ERR_STOPDECODE 7     // STOP_DECODE Error
#define BB_BARCODE_SC_ERR_PARAMREQUEST 8   // PARAM_REQUEST Error
#define	BB_BARCODE_SC_ERR_PARAMSEND 9      // PARAM_SEND Error
#define BB_BARCODE_SC_ERR_PARAMDEFAULTS 10 // PARAM_DEFAULTS Error
#define BB_BARCODE_SC_ERR_LED  11          // LED_ON/LED_OFF Error
#define BB_BARCODE_SC_ERR_BEEP 12          // BEEP Error
#define BB_BARCODE_SC_ERR_SENDPACKET 13    // Error occurred during Sending Packet.
#define BB_BARCODE_SC_ERR_AIM 14           // AIM_ON/OFF Error
#define BB_BARCODE_SC_ERR_NOBUFFER 15      // Buffer 크기가 작은 경우 error
#define BB_BARCODE_SC_ERR_SCANCLOSE 16     // Scan Close 도중에 생긴 error
#define BB_BARCODE_SC_ERR_TOOBIGSIZE 17    // 보낼 data의 size가 너무 클 경우의 error
#define BB_BARCODE_SC_ERR_NOPARAMVALUE 18  //  찾고자하는 PARAMETER의 VALUE가 없을때 생기는 error
#define BB_BARCODE_SC_ERR_NOHANDLEGRABBER 19 // 현재 Barcode를 점유하는 handle이 없을 경우 error

// barcode symbology
#define BB_BARCODE_NOTAPPLICABLE        0x00
#define BB_BARCODE_UPC_A                0x11
#define BB_BARCODE_UPC_E                0x12
#define BB_BARCODE_UPC_E1               0x13
#define BB_BARCODE_EAN8                 0x14
#define BB_BARCODE_EAN13                0x15
#define BB_BARCODE_SUPPLEMENTCODE       0x16
#define BB_BARCODE_UCC_COUPON_EXTENDED  0x17
#define BB_BARCODE_CODE39               0x18
#define BB_BARCODE_CODE93               0x19
#define BB_BARCODE_CODE128              0x1A
#define BB_BARCODE_INTERLEAVED2OF5      0x1B
#define BB_BARCODE_INDUSTRIAL2OF5       0x1C
#define BB_BARCODE_CODABAR              0x1D
#define BB_BARCODE_KOREAN_POST          0x1E
#define BB_BARCODE_CODE11               0x1F
#define BB_BARCODE_MSI                  0x20
#define BB_BARCODE_CHINESE_POST         0x21
#define BB_BARCODE_RSS                  0x22
#define BB_BARCODE_PDF417               0x23
#define BB_BARCODE_ISBT128              0x24
#define BB_BARCODE_IATA25               0x25
#define BB_BARCODE_TELEPEN              0x26
#define BB_BARCODE_MATRIX2OF5           0x27
#define BB_BARCODE_COMPOSITE            0x28
#define BB_BARCODE_DATAMATRIX           0x29
#define BB_BARCODE_MAXICODE             0x2A
#define BB_BARCODE_AZTECCODE            0x2B
#define BB_BARCODE_MICROPDF             0x2C
#define BB_BARCODE_QRCODE               0x2D
#define BB_BARCODE_TRIOPTICCODE         0x2E
#define BB_BARCODE_PLESSEY              0x2F
#define BB_BARCODE_CODE32               0x30
#define BB_BARCODE_POSICODE             0x31
#define BB_BARCODE_JAPANESE_POST        0x32
#define BB_BARCODE_AUSTRALIAN_POST      0x33
#define BB_BARCODE_BRITISH_POST         0x34
#define BB_BARCODE_CANADIAN_POST        0x35
#define BB_BARCODE_NETHERLANDS_POST     0x36
#define BB_BARCODE_POSTNET              0x37
#define BB_BARCODE_OCR                  0x38
#define BB_BARCODE_AZTEC_MESA           0x39
#define BB_BARCODE_CODE49               0x3A
#define BB_BARCODE_CODABLOCK            0x3B
#define BB_BARCODE_PLANET               0x3C
#define BB_BARCODE_TLC39                0x3D
#define BB_BARCODE_STRAIGHT2OF5         0x3E
#define BB_BARCODE_CODE16K              0x3F
#define BB_BARCODE_DISCRETE2OF5         0x40
#define BB_BARCODE_UK_PLESSEY           0x41
#define BB_BARCODE_AZTEC_RUNES          0x42
#define BB_BARCODE_USPS4CB              0x43
#define BB_BARCODE_IDTAG                0x44




//OPTION
#define BB_BARCODE_OPT_SYMBOLOGY                            0x00
#define BB_BARCODE_OPT_ABC_CODE_ONLY                        0x01
#define BB_BARCODE_OPT_BOOKLAND                             0x02
#define BB_BARCODE_OPT_CHECK_DIGIT                          0x03
#define BB_BARCODE_OPT_CHECK_DIGIT_ALGORITHM                0x04
#define BB_BARCODE_OPT_CLSI_EDITING                         0x05
#define BB_BARCODE_OPT_COMPOSITE_ON_UCC_EAN                 0x06
#define BB_BARCODE_OPT_CONVERT_A_TO_X                       0x07
#define BB_BARCODE_OPT_CONVERT_CODE39_TO_CODE32             0x08
#define BB_BARCODE_OPT_CONVERT_EAN_8_TO_EAN_13              0x09
#define BB_BARCODE_OPT_CONVERT_INTERLEAVED2OF5_TO_EAN_13    0x0A
#define BB_BARCODE_OPT_CONVERT_RSS_TO_UPC_EAN               0x0B
#define BB_BARCODE_OPT_CONVERT_UPC_E_TO_UPC_A               0x0C
#define BB_BARCODE_OPT_CONVERT_UPC_E1_TO_UPC_A              0x0D
#define BB_BARCODE_OPT_CX_CODE_ONLY                         0x0E
#define BB_BARCODE_OPT_EXPAND_VERSION_E                     0x0F
#define BB_BARCODE_OPT_FONT                                 0x10
#define BB_BARCODE_OPT_FULL_ASCII                           0x11
#define BB_BARCODE_OPT_GROUP_G                              0x12
#define BB_BARCODE_OPT_GROUP_H                              0x13
#define BB_BARCODE_OPT_IGNORE_LINK_FLAG                     0x14
#define BB_BARCODE_OPT_INSERT_SPACE                         0x15
#define BB_BARCODE_OPT_INTER_CHARACTER_GAP_CHECK            0x16
#define BB_BARCODE_OPT_LEADING_ZERO                         0x17
#define BB_BARCODE_OPT_LENGTH_MIN                           0x18
#define BB_BARCODE_OPT_LENGTH_MAX                           0x19
#define BB_BARCODE_OPT_LIMITED                              0x1A
#define BB_BARCODE_OPT_MESA_CODE39                          0x1B
#define BB_BARCODE_OPT_MESA_CODE93                          0x1C
#define BB_BARCODE_OPT_MESA_CODE128                         0x1D
#define BB_BARCODE_OPT_MESA_EAN13                           0x1E
#define BB_BARCODE_OPT_MESA_INTERLEAVED2OF5                 0x1F
#define BB_BARCODE_OPT_MESA_UPC_A                           0x20
#define BB_BARCODE_OPT_NOTIS_EDITING                        0x21
#define BB_BARCODE_OPT_OLDSTYLE                             0x22
#define BB_BARCODE_OPT_OUTPUT_MODE                          0x23
#define BB_BARCODE_OPT_SUPPLEMENTCODE2                      0x24
#define BB_BARCODE_OPT_SUPPLEMENTCODE5                      0x25
#define BB_BARCODE_OPT_SUPPLEMENTCODE_SEPARATOR             0x26
#define BB_BARCODE_OPT_TEMPLATE                             0x27
#define BB_BARCODE_OPT_TRANSMIT_APPLICATION_IDENTIFIER      0x28
#define BB_BARCODE_OPT_TRANSMIT_CHECK_DIGIT                 0x29
#define BB_BARCODE_OPT_TRANSMIT_DASH                        0x2A
#define BB_BARCODE_OPT_TRANSMIT_ISBN                        0x2B
#define BB_BARCODE_OPT_TRANSMIT_ISSN                        0x2C
#define BB_BARCODE_OPT_TRANSMIT_ISMN                        0x2D
#define BB_BARCODE_OPT_TRANSMIT_LEADING_A                   0x2E
#define BB_BARCODE_OPT_TRANSMIT_NUMERIC_SYSTEM_DIGIT        0x2F
#define BB_BARCODE_OPT_TRANSMIT_S_CODE_AS_INTERLEAVED2OF5   0x30
#define BB_BARCODE_OPT_TRANSMIT_START_STOP                  0x31
#define BB_BARCODE_OPT_TRIOPTIC_CODE39                      0x32
#define BB_BARCODE_OPT_UCCEAN_128                           0x33
#define BB_BARCODE_OPT_UPCEAN_SECURITY_LEVEL                0x34
#define BB_BARCODE_OPT_APPEND                               0X35
#define BB_BARCODE_OPT_FNC                                  0x36
#define BB_BARCODE_OPT_TRANSMIT_FNC                         0x37

#define BB_BARCODE_MODE_TRIGGER         0x00
#define BB_BARCODE_MODE_CONTINUOUS      0x01
#define BB_BARCODE_MODE_MULTIPLE        0x02


typedef LPVOID HBBBARCODE;

BBAPI HBBBARCODE WINAPI BBBarcodeOpen(BOOL bEnableTrigger);
BBAPI DWORD WINAPI BBBarcodeClose(HBBBARCODE hBarcode);

BBAPI DWORD WINAPI BBBarcodeStartDecode(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeStopDecode(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeGetDecodeData(HBBBARCODE hBarcode, LPTSTR lpcszBuf, int Bufsize, int* ReadSize );
BBAPI DWORD WINAPI BBBarcodeGetDecodeDataNType(HBBBARCODE hBarcode, LPTSTR lpcszBuf, BYTE* pType, int Bufsize, int* ReadSize);
BBAPI DWORD WINAPI BBBarcodeGetDecodeDataRaw(HBBBARCODE hBarcode, BYTE *pBuf, int Bufsize, int* ReadSize );
BBAPI DWORD WINAPI BBBarcodeGetDecodeDataNTypeRaw(HBBBARCODE hBarcode, BYTE *pBuf, BYTE* pType, int Bufsize, int* ReadSize);
BBAPI DWORD WINAPI BBBarcodeSetClientHandle(HBBBARCODE hBarcode, HWND hwnd );
BBAPI DWORD WINAPI BBBarcodeReleaseClientHandle(HBBBARCODE hBarcode);

BBAPI DWORD WINAPI BBBarcodeSymbologyAllEnable(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeSymbologyAllDisable(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeGetParameterEx(HBBBARCODE hBarcode, BYTE paramCode, BYTE extraParamCode, BYTE& paramValue);
BBAPI DWORD WINAPI BBBarcodeSetParameterEx(HBBBARCODE hBarcode, BYTE paramCode, BYTE extraParamCode, BYTE paramValue, BOOL bPermanent);
BBAPI DWORD WINAPI BBBarcodeSetDefault(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeScanEnable(HBBBARCODE hBarcode, BOOL bEnable);
BBAPI DWORD WINAPI BBBarcodeSetLED(HBBBARCODE hBarcode, BOOL bOn);
BBAPI DWORD WINAPI BBBarcodeSetBeep(HBBBARCODE hBarcode, int Duration, BOOL bHigh, int nNumOfBeep);
BBAPI DWORD WINAPI BBBarcodeSetPowerMode(HBBBARCODE hBarcode, BOOL bContinuousPower);
BBAPI DWORD WINAPI BBBarcodeSetAim(HBBBARCODE hBarcode, BOOL bOn);
BBAPI DWORD WINAPI BBBarcodeGetMultipleParameter(HBBBARCODE hBarcode, BYTE* paramCodes, int RequestSize, BYTE* paramCodeNValue, int BufSize, int* ReadSize);
BBAPI DWORD WINAPI BBBarcodeSetMultipleParameter(HBBBARCODE hBarcode, BYTE* paramCodeNValue, int Size, BOOL bPermanent);
BBAPI DWORD WINAPI BBBarcodeGetParameterFromBytes(HBBBARCODE hBarcode, BYTE paramCodes, BYTE& paramValue, BYTE* paramCodeNValue, int BufSize);
BBAPI DWORD WINAPI BBBarcodeSetParameterToBytes(HBBBARCODE hBarcode, BYTE paramCodes, BYTE paramValue, BYTE* paramCodeNValue, int BufSize);
BBAPI DWORD WINAPI BBBarcodeSetTrigger(HBBBARCODE hBarcode, BOOL bOn);

// code by coffriend - 2003.11.17
// : Amble/Fix 등 일일이 설정해 줄 경우 귀찮은 값들을 API를 써서
//   쉽게 설정할 수 있도록 한다.
BBAPI DWORD WINAPI BBBarcodeSetPreamble(HBBBARCODE hBarcode, LPTSTR szPreamble);
BBAPI DWORD WINAPI BBBarcodeSetPostamble(HBBBARCODE hBarcode, LPTSTR szPostamble);
BBAPI DWORD WINAPI BBBarcodeSetPrefix(HBBBARCODE hBarcode, LPTSTR szPrefix);
BBAPI DWORD WINAPI BBBarcodeSetSuffix(HBBBARCODE hBarcode, LPTSTR szSuffix);
BBAPI DWORD WINAPI BBBarcodeSetVirtualWedge(HBBBARCODE hBarcode, BOOL bOn);
BBAPI DWORD WINAPI BBBarcodeSetOutputMode(HBBBARCODE hBarcode, BOOL bClipboard);
BBAPI DWORD WINAPI BBBarcodeSetDecodeMode(HBBBARCODE hBarcode, BYTE bMode);
BBAPI DWORD WINAPI BBBarcodeGetDecodeMode(HBBBARCODE hBarcode, BYTE &bMode);
BBAPI DWORD WINAPI BBBarcodeSetTimeoutBetweenSameSymbol(HBBBARCODE hBarcode, DWORD dwMillisecond);
BBAPI DWORD WINAPI BBBarcodeSetDecodeOnTime(HBBBARCODE hBarcode,DWORD dwDeciSecond);
BBAPI DWORD WINAPI BBBarcodeSetPushingTriggerMode(HBBBARCODE hBarcode, BYTE bPushingTriggerMode);
BBAPI DWORD WINAPI BBBarcodeSetVolume(int nVolume);

//ImagerBarcode only
typedef enum {
    SCAN_ILLUM_AIMER_OFF = 0,   // Neither aimers or illumination
    SCAN_ILLUM_ONLY_ON,         // Illumination only
    SCAN_AIMER_ONLY_ON,         // Aimers only
    SCAN_ILLUM_AIMER_ON         // Both aimers and illumination.
} ScanIlluminat_t;

typedef enum {
    DECODE_MODE_STANDARD=1,
    DECODE_MODE_ADVANCED_LINEAR,
    DECODE_MODE_QUICK_OMNI=4
} ImagerDecodeMode_t;

typedef struct {
    int Exposure;
    int MaxExposure;
    int Gain;
    int MaxGain;
    int TargetWhite;            // Acceptable target white value falls within the
    int TargetWhiteWindow;      // range TargetWhite +/- TargetWhiteWindow
    int ImageMustConform;       // Image must conform to defined exposure parameters
    int NumUpdates;             // Max number of attempts to achieve target white value
    int FrameRate;
    int SpecExclusion;
    int SpecSaturation;
    int SpecLimit;
    int FixedExposure;          // Exposure setting for fixed exposure mode
    int FixedGain;              // Gain setting for fixed exposure mode
    int FixedFrameRate;         // Frame rate for fixed exposure mode
} ExposureSettings_t;

BBAPI DWORD WINAPI BBBarcodeImagerGetImagerInfo(HBBBARCODE hBarcode, PWORD pnCols, PWORD pnRows, PWORD pnBits);
BBAPI DWORD WINAPI BBBarcodeImagerStreamInit(HBBBARCODE hBarcode, WORD nSkip, RECT * imgRect, BOOL bFlip);
BBAPI DWORD WINAPI BBBarcodeImagerStreamStart(HBBBARCODE hBarcode, RECT *previewRect);
BBAPI DWORD WINAPI BBBarcodeImagerStreamStop(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeImagerStreamCapture(HBBBARCODE hBarcode, TCHAR *filename, unsigned int nFileWidth, unsigned int nFileHeight, BOOL bAutoSave=FALSE);
BBAPI DWORD WINAPI BBBarcodeImagerSetExposureSettings(HBBBARCODE hBarcode, ExposureSettings_t *pExpsoureSettings);
BBAPI DWORD WINAPI BBBarcodeImagerGetExposureSettings(HBBBARCODE hBarcode, ExposureSettings_t *pExpsoureSettings);
BBAPI DWORD WINAPI BBBarcodeImagerSetScanningLightMode(HBBBARCODE hBarcode, ScanIlluminat_t LightMode);
BBAPI DWORD WINAPI BBBarcodeImagerLeaveLightsOn(HBBBARCODE hBarcode, BOOL bLeaveLightOn);
BBAPI DWORD WINAPI BBBarcodeImagerSetDecodeMode(HBBBARCODE hBarcode, ImagerDecodeMode_t DecodeMode);

// C#의 Wrapper Dll에서 Reading이 다 되었다는 것을 받기 위해 기다리는 함수
DWORD WINAPI BBBarcodeWaitReading(HBBBARCODE hBarcode);

// 이 API를 호출하면 Setting을 Update한다.
BBAPI DWORD WINAPI BBBarcodeUpdateSetting(HBBBARCODE hBarcode);
// coffriend end - 2003.11.17

// xml 파일 설정 정보 start

typedef const char* PCCHAR;
#define MAX_TEXT_BUFFER 256


// xml 파일 설정을 위한 구조체
typedef struct
{
	DWORD ItemIndex;
	const char* item_name;
	const char* group_name;
	const char* parameter_name;
	const char* value_name;
	DWORD type;
	BOOL IsValueDword;
	BOOL IsValueWChar;
	union
	{
		DWORD value_dword; //- PARAM_NUMERIC, PARAM_SLIDER, PARAM_CHAR
		TCHAR value_wcharbuffer[MAX_TEXT_BUFFER]; //PARAM_TEXT, PARAM_PATH, PARAM_READONLY
	};

}SettingInfo;

enum ItemIndex
{
	VWEDGETABINDEX = 0,
	SYMBOLOGYTABINDEX,
	SCANNERTABINDEX,
	DATAOPTIONTABINDEX,
	IMGCAPTABINDEX,
    REVISIONTABINDEX
};

enum ParamType {
    PARAM_NUMERIC = 0,   //numeric value(숫자값) 선택
	PARAM_OPTION,        //option(여러개중 하나를 선택)
	PARAM_ENABLE,        //enable/disable 중 하나 선택
	PARAM_CHAR,          //character값
	PARAM_TEXT,          //text값(scanner에 setting할 값은 아니다.)
	PARAM_PATH,			 // file path - add csyou
	PARAM_SLIDER,		 // Slider - add csyou
	PARAM_NUMERIC_TIME,	 // continues time(between same symbol)
	PARAM_NULL,           //not a parameter type
    PARAM_READONLY
};
enum RevisonType {
	BB_BARCODE_REVISION_API = 0,
	BB_BARCODE_REVISION_DECODER,
	BB_BARCODE_REVISION_DRIVER
};

enum SoundMode{
    BB_BARCODE_SOUND_WAVE = 0,
    BB_BARCODE_SOUND_VIBRATE,
    BB_BARCODE_SOUND_WAVE_VIBRATE,
    BB_BARCODE_SOUND_EXTERNAL,
    BB_BARCODE_SOUND_ERROR
};

// xml 파일 설정 정보 end

// xml 파일에서 정보를 얻어오는 API
BBAPI PCCHAR WINAPI BBBarcodeGetNextItemName(HBBBARCODE hBarcode, const char* item_name,BOOL* is_ignore_item);
BBAPI BOOL WINAPI BBBarcodeGetItemString(HBBBARCODE hBarcode, const char* item_name, TCHAR* pbuffer, unsigned int buffer_length);
BBAPI BOOL WINAPI BBBarcodeGetItemNumber(HBBBARCODE hBarcode, const char* item_name, unsigned int* item_number);

BBAPI PCCHAR WINAPI BBBarcodeGetNextGroupName(HBBBARCODE hBarcode, const char* item_name, const char* group_name, BOOL* is_ignore_group);
BBAPI BOOL WINAPI BBBarcodeGetGroupString(HBBBARCODE hBarcode, const char* item_name, const char* group_name, TCHAR* pbuffer, unsigned int buffer_length);

BBAPI PCCHAR WINAPI BBBarcodeGetNextParameterName(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, BOOL* is_ignore_parameter);
BBAPI BOOL WINAPI BBBarcodeGetParameterString(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, TCHAR* pbuffer, unsigned int buffer_length);
BBAPI BOOL WINAPI BBBarcodeGetParameterState(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, TCHAR* pbuffer, unsigned int buffer_length);
BBAPI BOOL WINAPI BBBarcodeGetParameterType(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, unsigned int* type );

BBAPI PCCHAR WINAPI BBBarcodeGetParameterNextOptionName(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, const char* option_name);
BBAPI BOOL WINAPI BBBarcodeGetParameterOptionString(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, const char* option_name, TCHAR* pbuffer, unsigned int buffer_length);
BBAPI BOOL WINAPI BBBarcodeSetParameterValue(HBBBARCODE hBarcode, SettingInfo* settinginfo);
BBAPI BOOL WINAPI BBBarcodeXmlFileSave(HBBBARCODE hBarcode);
BBAPI BOOL WINAPI BBBarcodeInitSetting(HBBBARCODE hBarcode);

BBAPI BOOL WINAPI BBBarcodeGetCanWaveUse(HBBBARCODE hBarcode);
BBAPI BOOL WINAPI BBBarcodeGetVolume(HBBBARCODE hBarcode, DWORD* volume);
BBAPI BOOL WINAPI BBBarcodeGetWaveFilePath(HBBBARCODE hBarcode, TCHAR* file_path, unsigned int file_path_length);
BBAPI BOOL WINAPI BBBarcodeGetVirtualWedge(HBBBARCODE hBarcode);
BBAPI BOOL WINAPI BBBarcodeGetTriggerState(HBBBARCODE hBarcode);
BBAPI PCCHAR WINAPI BBBarcodeGetParameterStateChar(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name);
BBAPI DWORD WINAPI BBBarcodeGetParameterStateDword(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name, DWORD* dwvalue);
BBAPI PCCHAR WINAPI BBBarcodeGetParameterStateName(HBBBARCODE hBarcode, const char* item_name, const char* group_name, const char* parameter_name);
BBAPI BOOL WINAPI BBBarcodeReadDefaultFile(HBBBARCODE hBarcode);
BBAPI BOOL WINAPI BBBarcodeCloseDefaultFile(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeGetRevision(HBBBARCODE hBarcode, BYTE bRevisionType, TCHAR *wcRevision);
BBAPI BOOL WINAPI BBBarcodeSetSoundMode(HBBBARCODE hBarcode, SoundMode nSoundMode);
BBAPI BOOL WINAPI BBBarcodeGetSoundMode(HBBBARCODE hBarcode, SoundMode &nSoundMode);
BBAPI BOOL WINAPI BBBarcodeSetVibrateTimeout(HBBBARCODE hBarcode, DWORD dwVibrateTimeout);
BBAPI BOOL WINAPI BBBarcodeGetVibrateTimeout(HBBBARCODE hBarcode, DWORD &dwVibrateTimeout);

//*******************************************************************************
//예전 BARCODE API
BBAPI DWORD WINAPI BBBarcodeSetMode(HBBBARCODE hBarcode, BOOL bTrigger);
BBAPI BOOL WINAPI BBBarcodeGetContinuousMode(HBBBARCODE hBarcode);
BBAPI BOOL WINAPI BBBarcodeSetTriggerMode(HBBBARCODE hBarcode, BOOL IsContinuousMode);
BBAPI DWORD WINAPI BBBarcodeImageGetImagerInfo(HBBBARCODE hBarcode, PWORD pnCols, PWORD pnRows, PWORD pnBits);
BBAPI DWORD WINAPI BBBarcodeImageStreamInit(HBBBARCODE hBarcode, WORD nSkip, RECT * imgRect, BOOL bFlip);
BBAPI DWORD WINAPI BBBarcodeImageStreamStart(HBBBARCODE hBarcode, RECT *previewRect);
BBAPI DWORD WINAPI BBBarcodeImageStreamStop(HBBBARCODE hBarcode);
BBAPI DWORD WINAPI BBBarcodeImageStreamCapture(HBBBARCODE hBarcode, TCHAR *filename, int nFileWidth, int nFileHeight);
BBAPI DWORD WINAPI BBBarcodeImageSetExposureSettings(HBBBARCODE hBarcode, ExposureSettings_t *pExpsoureSettings);
BBAPI DWORD WINAPI BBBarcodeImageGetExposureSettings(HBBBARCODE hBarcode, ExposureSettings_t *pExpsoureSettings);
BBAPI DWORD WINAPI BBBarcodeGetParameter(HBBBARCODE hBarcode, BYTE paramCode, BYTE& paramValue);
BBAPI DWORD WINAPI BBBarcodeSetParameter(HBBBARCODE hBarcode, BYTE paramCode, BYTE paramValue, BOOL bPermanent);
BBAPI BOOL WINAPI BBBarcodeGetWaveUse(HBBBARCODE hBarcode);
//********************************************************************************

/*
 *  NEO PRINTER - 서로프린터 말고 우리가 직접 제어하는 Printer
 */
typedef LPVOID HBBNEOPRINTER;

// return code
#define BB_NEOPRINTER_CODE_SUCCESS                   0
#define BB_NEOPRINTER_CODE_ERR_UNKNOWN               1
#define BB_NEOPRINTER_CODE_ERR_NORMAL                2
#define BB_NEOPRINTER_CODE_ERR_INVALIDHANDLE         3
#define BB_NEOPRINTER_CODE_ERR_NOFONT                4
#define BB_NEOPRINTER_CODE_ERR_READFILE              5
#define BB_NEOPRINTER_CODE_ERR_NOBITMAPFILE          6
#define BB_NEOPRINTER_CODE_ERR_INVALID_BITMAPFILE    7
#define BB_NEOPRINTER_CODE_ERR_NOPAPER               8
#define BB_NEOPRINTER_CODE_ERR_HIGHTEMPERATURE       9
#define BB_NEOPRINTER_CODE_ERR_SETDENSITY            10
#define BB_NEOPRINTER_CODE_ERR_SETDENSITY_OUTOFRANGE 11
#define BB_NEOPRINTER_CODE_ERR_GETDENSITY            12
#define BB_NEOPRINTER_CODE_ERR_FEEDMARK              13

// printer option
#define BB_NEOPRINTER_OPTION_DEFAULT                 0   // LEFT ALIGN, NO BOLD/ITALIC
#define BB_NEOPRINTER_OPTION_LEFTALIGN               0
#define BB_NEOPRINTER_OPTION_CENTERALIGN             1
#define BB_NEOPRINTER_OPTION_RIGHTALIGN              2
#define BB_NEOPRINTER_OPTION_UNDERLINE               4
#define BB_NEOPRINTER_OPTION_EMPHESIZE               8
#define BB_NEOPRINTER_OPTION_DOUBLEHEIGHT            16
#define BB_NEOPRINTER_OPTION_DOUBLEWIDTH             32
#define BB_NEOPRINTER_OPTION_DOUBLESIZE              48
#define BB_NEOPRINTER_OPTION_ITALIC                  64
#define BB_NEOPRINTER_OPTION_FONTBYFILE             128

// barcode option
#define BB_NEOPRINTER_BARCODE_UPCA                   65
#define BB_NEOPRINTER_BARCODE_UPCE                   66
#define BB_NEOPRINTER_BARCODE_JAN13                  67
#define BB_NEOPRINTER_BARCODE_JAN8                   68
#define BB_NEOPRINTER_BARCODE_CODE39                 69
#define BB_NEOPRINTER_BARCODE_ITF                    70
#define BB_NEOPRINTER_BARCODE_CODABAR                71
#define BB_NEOPRINTER_BARCODE_CODE93                 72
#define BB_NEOPRINTER_BARCODE_CODE128                73

/*
 *	camera
 */
#define WM_BBCAMERA_UPDATE_FPS					WM_USER+1000

#define BB_CAMERA_CODE_SUCCESS                  0
#define BB_CAMERA_CODE_ERR_UNKNOWN				1
#define BB_CAMERA_CODE_ERR_INVALID_HANDLE      	2
#define BB_CAMERA_CODE_ERR_INVALID_PARAMETER   	3

typedef enum
{
	wb_auto = 0,
	wb_cloudy,
	wb_daylight,
	wb_fluorescent_1,
	wb_fluorescent_2,
	wb_light_bulb

} white_balance_mode;

typedef enum
{
	effect_none = 0,
	effect_negative,
	effect_embossing,
	effect_black_n_white,
	effect_sketch,
	effect_solarization,
	effect_sephia,
	effect_aqua,
	effect_posterize,
	effect_warm,
	effect_cool,
	effect_antique,
	effect_moonlight,
	effect_fog,
	effect_gray,
	effect_violet,
} effect_mode;

typedef enum
{
	flip_x = 1,
	flip_y,
	flip_x_y,
	flip_origin


} flip_mode;

typedef enum
{
	rate_auto = 1,
	rate_day_manual,
	rate_night_manual
} frame_rate_mode;

typedef enum
{
	auto_save = 0,
	manual_save
} save_mode;

typedef enum
{
	file_type_jpg = 0,
	file_type_bmp
} save_image_type;

typedef enum
{
	rotate_default=0,
	rotate_0,
	rotate_90,
	rotate_180,
	rotate_270

} rotation_degree;

typedef enum
{
	pixel_format_ycbcr = 0,
	pixel_format_rgb_565,		// output data 가 bmp 이다.
	pixel_format_rgb_565_raw	// output data 가 16bit rgb 데이터 이다.

} pixel_format_select;


typedef struct _tagBBCameraParameter
{
	unsigned int preview_width;
	unsigned int preview_height;
	unsigned int preview_x;
	unsigned int preview_y;
	unsigned int preview_format;
	unsigned int preview_zoom;

	const TCHAR* capture_file_name;
	unsigned int capture_width;
	unsigned int capture_height;
	pixel_format_select capture_format;
	unsigned int capture_strobe_on;

	unsigned int contrast;
	unsigned int saturation;
	unsigned int brightness;
	effect_mode ef_mode;
	white_balance_mode wb_mode;
	flip_mode	fp_mode;
	frame_rate_mode fr_mode;

	unsigned int reserved[20];
	BYTE* 		p_app_buffer;
	BYTE* 		p_app_capture_buffer;	//still image capture에서 데이터를 받기위한 포인터

	save_mode			stillimage_savemode;
	save_image_type		save_fileType;
	unsigned int		ImageQuality;

	BOOL				isSaveFile;
	rotation_degree		rotation;

} BBCameraParameter;

typedef struct _tagBBCameraInfo
{
	unsigned int preview_max_width;
	unsigned int preview_max_height;
	unsigned int preview_min_width;
	unsigned int preview_min_height;
	unsigned int image_max_width;
	unsigned int image_max_height;

	unsigned int reserved[20];

} BBCameraInfo;


typedef LPVOID HBBCAMERA;

BBAPI HBBCAMERA WINAPI BBCameraOpen(HWND);
BBAPI DWORD WINAPI BBCameraClose(HBBCAMERA);

BBAPI DWORD WINAPI BBCameraPreviewStart(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraPreviewStop(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraPreviewPause(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraPreviewResume(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraPreviewZoom(HBBCAMERA, BBCameraParameter*);

BBAPI DWORD WINAPI BBCameraGetRawDataStart(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraGetRawDataStop(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraGetRawDataPause(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraGetRawDataResume(HBBCAMERA);
BBAPI DWORD WINAPI BBCameraGetRawDataZoom(HBBCAMERA, BBCameraParameter*);

BBAPI DWORD WINAPI BBCameraCapture(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraStoreCaptureImage(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraRestartPreviewFromCapture(HBBCAMERA, BBCameraParameter*);

BBAPI DWORD WINAPI BBCameraSetWhiteBalance(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraSetContrast(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraSetSaturation(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraSetBrightness(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraSetEffect(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraSetFlip(HBBCAMERA, BBCameraParameter*);
BBAPI DWORD WINAPI BBCameraNightMode(HBBCAMERA, BBCameraParameter*);

BBAPI DWORD WINAPI BBCameraGetInfo(HBBCAMERA, BBCameraInfo*);
BBAPI DWORD WINAPI BBCameraDumpRegister(HBBCAMERA);

BBAPI DWORD WINAPI BBCameraSetStrobe(HBBCAMERA, BOOL);
BBAPI DWORD WINAPI BBCameraSetAEMode(HBBCAMERA, BOOL);
BBAPI DWORD WINAPI BBCameraSetExposureTime( HBBCAMERA, DWORD );


/*
 *	IC Card
 */
#define BB_ICCARD_SUCCESS                  		0
#define BB_ICCARD_ERR_POWERED_DOWN				1
#define BB_ICCARD_ERR_CARD_PROTOCOL_ERROR		2
#define BB_ICCARD_ERR_ALREADY_POWERED_UP		3
#define BB_ICCARD_ERR_CARD_REMOVED				4
#define BB_ICCARD_ERR_CARD_MISSING				5
#define BB_ICCARD_ERR_RETURNED_BY_CARD			6
#define BB_ICCARD_ERR_UNKNOWN_ERROR				7
#define BB_ICCARD_ERR_SERIAL_DEVICE				8
#define BB_ICCARD_ERR_OPEN_FAILED				9
#define BB_ICCARD_ERR_EXCHANGE_INTERRUPTED		10
#define BB_ICCARD_ERR_INCORRECT_NUMBER			11
#define BB_ICCARD_ERR_DEVICE_ERROR				12


typedef unsigned char BBICCardStatus;
typedef unsigned char *APDU;



typedef LPVOID HBBICCARD;
typedef LPVOID HBBRFREADER;

BBAPI HBBICCARD WINAPI BBICCardOpen();
BBAPI DWORD WINAPI BBICCardClose(HBBICCARD);

BBAPI DWORD WINAPI BBICCardPowerUp(HBBICCARD);
BBAPI DWORD WINAPI BBICCardGetCardStatus(HBBICCARD, BBICCardStatus*);
BBAPI DWORD WINAPI BBICCardExchangAPDU(HBBICCARD hICCard, APDU apduSend, DWORD dwSizeOfAPDU, APDU apduReceived, DWORD *dwReturnSize );

/* ------------------------------------------------------------------------- */

typedef unsigned char BBRFReader_SerialNumber;
typedef unsigned char BBRFReader_ATS;
typedef unsigned char BBRFReader_CID;
typedef unsigned char BBRFReader_ATQA;

typedef unsigned char BBRFReader_AFI;
typedef unsigned char BBRFReader_PUPI;
typedef unsigned char BBRFReader_APPDATA;
typedef unsigned char BBRFReader_Protocol_Info;
typedef unsigned char BBRFReader_SLOTNB;
typedef unsigned char BBRFReader_AttribResponse;
typedef unsigned char BBRFReader_MifareData;
typedef unsigned char BBRFReader_MifareMemVal;
typedef unsigned char BBRFReader_ABL;
typedef unsigned char BBRFReader_LC;
typedef unsigned char BBRFReader_LE;
typedef unsigned char BBRFReader_NAD;
typedef unsigned char BBRFReader_CardCmd;
typedef unsigned char BBRFReader_CardResp;

//typedef unsigned char BBRFReader_TIM;
//typedef unsigned char BBRFReader_DSDR;

/*
#define BB_RFREADER_EXCUTE_SUCCESS                                          ( 0x90 | 0x00 )
#define STATUS_INCORRECT_CLA_ERROR                                          ( 0x6E | 0x00 )
#define STATUS_INCORRECT_INS_APDU_ARGUMENT_LC_ERROR                         ( 0x6D | 0x00 )
#define STATUS_P1_P2_INCORRECT_ERROR                                        ( 0x6B | 0x00 )
#define STATUS_INCORRECT_KEY_IN_ASC_ERROR                                   ( 0x6A | 0x80 )
#define STATUS_INCORRECT_ASC_ERROR                                          ( 0x94 | 0x0B )
#define STATUS_INCORRECT_LE_ERROR                                           ( 0x6C ) // 다시 검토 해봐야 함
#define STATUS_A1_A2_OF_TARGET_BLOCK_INCORRECT_ERROR                        ( 0x94 | 0x0A )
#define STATUS_AUTHENTICATION_FAIL_ERROR                                    ( 0x98 | 0x20 )
#define STATUS_ACCESS_CONDITION_EXCUTE_ERROR                                ( 0x98 | 0x04 )
#define STATUS_MEMORY_FAIL_ERROR                                            ( 0x94 | 0x02 )
#define STATUS_VALUE_BLOCK_OPERATION_ERROR                                  ( 0x94 | 0x04 )
#define STATUS_VALUE_BLOCK_OPERATION_OVERFLOW_ERROR                         ( 0x94 | 0x05 )
#define STATUS_COMMAND_FAIL_ERROR                                           ( 0x94 | 0x0F )
#define STATUS_COMMAND_EXCUTE_TIMEOUT_ERROR                                 ( 0x94 | 0xFF )
*/


#define BB_RFREADER_SUCCESS                                                             0x00
#define BB_RFREADER_ERR_COMMAND                                                         0x01
#define BB_RFREADER_ERR_ARGUMENT                                                        0x03       //IFM2.0 추가 (ISO15693 : Not Supported Option )
#define BB_RFREADER_ERR_BUFFEROVERFLOW                                                  0x05
#define BB_RFREADER_ERR_READ_WRITE_EEPROM_FAIL                                          0x1F
#define BB_RFREADER_ERR_READERCASE_OPEN                                                 0xBA
#define BB_RFREADER_ERR_RFTIMEOUT_OR_NOT_RESPONSE                                       0xA2        //IFM2.0 응답없음 추가
#define BB_RFREADER_ERR_RF_CRC                                                          0xA3        //IFM2.0 CRC Error 추가
#define BB_RFREADER_ERR_RFTIMEOUT                                                       0xA4        //IFM2.0 추가
#define BB_RFREADER_ERR_RFFRAMING                                                       0xA5        //IFM2.0 추가
#define BB_RFREADER_ERR_RFETC                                                           0xA6        //IFM2.0 추가
#define BB_RFREADER_ERR_RFTIMEOUT_NOCARD                                                0xFB
#define BB_RFREADER_ERR_CARDDETECT_NOTISO14443_4_COMPLIANT                              0xFA
#define BB_RFREADER_ERR_UID_CASCASE_TAG_INCORRECT                                       0xEC        //IFM2.0 추가
#define BB_RFREADER_ERR_CARD_NOT_SELECT                                                 0x15
#define BB_RFREADER_ERRSW1_SW2_EXCUTION_ERROR_OR_ISO15693_INVENTORY_COLLISION_DETECT_ERROR      0xE7
#define BB_RFREADER_ERR_CARDTYPE_NOT_SUPPORT                                            0xFC
#define BB_RFREADER_ERR_AUTHENTICATE_OR_READ                                            0xFD
#define BB_RFREADER_ERR_CRC_ERROR_IN_MAD_DIRECTORY                                      0xFE
#define BB_RFREADER_ERR_NO_MORE_AID_INDEX_READ                                          0xFF
#define BB_RFREADER_ERR_CID_INCORRECT                                                   0xE0
#define BB_RFREADER_ERR_ATS_OR_ATQB_OR_HALB_RESPONSE                                    0xE1
#define BB_RFREADER_ERR_BITRATE_NOT_SUPPORT_BY_PICC_OR_PCD                              0xE2
#define BB_RFREADER_ERR_PPS_RESPONSE                                                    0xE3
#define BB_RFREADER_ERR_CARD_ACTIVATION_FORBIDDEN                                       0xE6
#define BB_RFREADER_ERR_ATTRIB                                                          0xE8
#define BB_RFREADER_ERR_ATQA                                                            0xE9
#define BB_RFREADER_ERR_SAK                                                             0xEB
#define BB_RFREADER_ERR_ATQB                                                            0xEE        //IFM2.0 추가
#define BB_RFREADER_ERR_HALTB                                                           0xEF        //IFM2.0 추가
#define BB_RFREADER_ERR_PROTOCOL_RULE_INFRINGEMENT                                      0xE4
#define BB_RFREADER_ERR_CARD_RESPONSE_TOO_LONG                                          0xE5
#define BB_RFREADER_ERR_COLLISION_DETECT_TRANSMISSION_ERROR_INCORRECT_CARD_RESPONSE     0xEA
#define BB_RFREADER_ERR_CARD_DESELECTED                                                 0xED
#define BB_RFREADER_ERR_CARD_LEFT_FIELD                                                 0xF7
//#define BB_RFREADER_ERR_ISO15693_INVENTORY_COLLISION_DETECT_ERROR                       0xE7

#define BB_RFREADER_ERR_ISO15693_NOT_SUPPORT_COMMAND                                    0x01
#define BB_RFREADER_ERR_ISO15693_NOT_RECOGNIZE_COMMAND                                  0x02
#define BB_RFREADER_ERR_ISO15693_NOT_SUPPORT_OPTION                                    0x03
#define BB_RFREADER_ERR_ISO15693_UNKNOWN_ERROR                                          0x0F        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_NOT_AVAILABLE_ERROR                                    0x10        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_BLOCK_LOCK_OR_AGAIN_LOCK_ERROR                         0x11        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_BLOCK_LOCK_OR_CHANGE_CONTENT_ERROR                     0x12        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_BLOCK_PROGRAMMED_NOT_SUCCESS                           0x13        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_BLOCK_PROGRAMMED_NOT_LOCKED                            0x14        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_FACTORY_PROGRAMMED_DISABLE                             0xA0        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_START_BLOCK_EVEN                                       0xA1        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_ONE_OHTER_BLOCK_LOCKED                                 0xA2        //IFM2.0 추가
#define BB_RFREADER_ERR_ISO15693_NOT_SUPPORTED_TEST_OPTION                              0xA3        //IFM2.0 추가

/*
//ISO15693 VICC Response Flags
#define ISO15693_VICC_SET_ERR_FLAG              0x01
#define ISO15693_EXTENSION_SET_ERR_FLAG         0x08
#define ISO15693_VICC_SET_ERR_EXTENTION_FLAG    0x09

//ISO15693 VICC Inforamtion Flag
#define STATUS_ISO15693_INFO_NOT_SUPPORT_DSFID              0x01
#define STATUS_ISO15693_INFO_NOT_SUPPORT_AFI                0x02
#define STATUS_ISO15693_INFO_NOT_SUPPORT_MEMORY_SIZE        0x04
#define STATUS_ISO15693_INFO_NOT_SUPPORT_IC_REFERENCE       0x08
*/

//ISO15693 VICC Response Flags
#define BB_RFREADER_ERR_ISO15693_VICC_SET_ERR_FLAG              0x01
#define BB_RFREADER_ERR_ISO15693_EXTENSION_SET_ERR_FLAG         0x08
#define BB_RFREADER_ERR_ISO15693_VICC_SET_ERR_EXTENTION_FLAG    0x09

//ISO15693 VICC Inforamtion Flag
#define BB_RFREADER_ERR_ISO15693_INFO_NOT_SUPPORT_DSFID              0x01
#define BB_RFREADER_ERR_ISO15693_INFO_NOT_SUPPORT_AFI                0x02
#define BB_RFREADER_ERR_ISO15693_INFO_NOT_SUPPORT_MEMORY_SIZE        0x04
#define BB_RFREADER_ERR_ISO15693_INFO_NOT_SUPPORT_IC_REFERENCE       0x08

#define BB_RFREADER_ERR_DEVICE_ERROR 		0x9900
#define BB_RFREADER_ERR_SERIAL_DEVICE 		0x9901
#define BB_RFREADER_ERR_OPEN_FAILED 		0x9902
#define BB_RFREADER_ERR_SAM_OPEN_FAILED 	0x9903
#define BB_RFREADER_ERR_UNKNOWN_ERROR		0x9904

//#define BB_RFREADER_FULL_SIZE 			0x9010


//SAM

typedef unsigned char BBSAMATR;
typedef unsigned char BBCNF;
typedef unsigned char BBPRT;
typedef unsigned char BBPPS_Response;
typedef unsigned char BBCFG;
typedef unsigned char BBPPS;
typedef unsigned char BBPCK;
typedef unsigned char BBDummyByte;
typedef unsigned char BBIFSD;
typedef unsigned char BBISOHeader;
typedef unsigned char BBSAMResponse;
typedef unsigned char BBSAMData;

//Set Selection Command
BBAPI DWORD WINAPI BBSAMSetCurrent(HBBICCARD hICCard, DWORD dwSlotNum );

//SAM Initialization Command
BBAPI DWORD WINAPI BBSAMPowerUpSAM( HBBICCARD hICCard, DWORD dwCFG, BBSAMATR *SAM_ATR );
BBAPI DWORD WINAPI BBSAMPowerDownSAM( HBBICCARD hICCard );
BBAPI DWORD WINAPI BBSAMStatus( HBBICCARD hICCard, BBCNF *Status);
BBAPI DWORD WINAPI BBSAMCommParam( HBBICCARD hICCard, BBPRT *PRT, BBCNF *Status );

//BBAPI DWORD WINAPI BBSAMExchangePPSSAM( HBBICCARD hICCard, BBCFG *CFG, BBPPS *PPS, BBPCK *PCK, BBPPS_Response *Response);
BBAPI DWORD WINAPI BBSAMExchangePPSSAM( HBBICCARD hICCard, BBCFG *CFG, BBPPS *PPS, BBPPS_Response *Response);
BBAPI DWORD WINAPI BBSAMExchangeIFSDSAM( HBBICCARD hICCard, BBDummyByte *Dummy, BBIFSD *IFSD);
//BBAPI DWORD WINAPI BBSAMExchangeIFSDSAM( HBBICCARD hICCard, BBIFSD *IFSD);

//SAM Communication Command
BBAPI DWORD WINAPI BBSAMISOOutputSAM( HBBICCARD hICCard, BBISOHeader *Header, BBSAMResponse *Response);
BBAPI DWORD WINAPI BBSAMISOInputSAM( HBBICCARD hICCard, BBISOHeader *Header, BBSAMResponse *Response, BBSAMData *Data, DWORD dwDataLen);


//ISO14443 A
/*
typedef enum
{
	BB_RFREADER_NOT_VALUE,
	BB_RFREADER_TG_GEMCOMBI_MPCOS_PRO_EASY32000=0x02,
	BB_RFREADER_TG_GEMEASY8000=0x04
}BBRFReader_ATQA;
*/

#define BB_RFREADER_TG_GEMCOMBI_MPCOS_PRO_EASY32000     0x02
#define BB_RFREADER_TG_GEMEASY8000                      0x04

typedef enum
{
	BB_RFREADER_COLLISION = 1<<8,
	BB_RFREADER_COLLISION_CL_1 = (1<<8) | 0x01,
	BB_RFREADER_COLLISION_CL_2 = (1<<8) | 0x02,
	BB_RFREADER_COLLISION_CL_3 = (1<<8) | 0x03,
	BB_RFREADER_CL_1=0x01,
	BB_RFREADER_CL_2,
	BB_RFREADER_CL_3
}BBRFReader_CascadeLevel;


typedef enum
{
	BB_RFREADER_NOT_COLLISION_DETECT=0, // Success
	BB_RFREADER_COLLISION_DETECT=0xEA   // Collision Detect
}BBRFReader_Collision;


typedef enum
{
	BB_RFREADER_SN_NOT_COMPLETE=0x88,   // Serial number is not complete
}BBRFReader_Anticollison;


typedef enum
{
	BB_RFREADER_SAK_GEMEASY8000_0x08=0x08,
	BB_RFREADER_SAK_GEMEASY8000_0x88=0x88,
	BB_RFREADER_SAK_GEM_MPCOS=0x98,
	BB_RFREADER_SAK_GEM_EASY320000=0x18,
	BB_RFREADER_NOT_SN=0x04,						//Serial number is not detect
	BB_RFREADER_ISO14443_4_COMPLIANT = 0x10,		//ISO 14443-4 Compliant
	BB_RFREADER_ISO14443_4_NOT_COMPLIANT = 0x00	//Not ISO 14443-4 compliant
}BBRFReader_SAK;


//RF
enum BBRFReader_Type
{
	BB_RFREADER_TYPE_A=0x00,
	BB_RFREADER_TYPE_B=0x01,
	BB_RFREADER_TYPE_15693=0x02
};


enum BBRFReader_RFBaudRateTypeA
{
	BB_RFREADER_RF_BDRA_106=0x00,
	BB_RFREADER_RF_BDRA_212=0x90 | 0x01, //(Bit2=1 & Bit5=1)
	BB_RFREADER_RF_BDRA_424=0xA0 | 0x02, //(Bit3=1 & Bit6=1)
	BB_RFREADER_RF_BDRA_848=0xC0 | 0x04, //(Bit4=1 & Bit7=1)
};


enum BBRFReader_RFBaudRateTypeB
{
	BB_RFREADER_RF_BDRB_106=0x00,
	BB_RFREADER_RF_BDRB_212=0x90 | 0x01, //(Bit2=1 & Bit5=1)
	BB_RFREADER_RF_BDRB_424=0xA0 | 0x02, //(Bit3=1 & Bit6=1)
	BB_RFREADER_RF_BDRB_848=0xC0 | 0x04  //(Bit4=1 & Bit7=1)

};


//MIFARE
typedef enum
{
	BB_RFREADER_SCT_GEMEASY8000=0x00,
	BB_RFREADER_SCT_GEMCOMBI_MPCOS_PRO=0x02,
	BB_RFREADER_SCT_GEMEASY320000=0x03,
}BBRFReader_SCT;


enum
{
	BB_RFREADER_ABL_GEMEASY8000=0x3F,
	BB_RFREADER_ABL_GEMCOMBI_MPCOS_PRO=0xFF,
	BB_RFREADER_ABL_GEMEASY320000=0xFF
};

typedef unsigned char BBRFReader_SW1;
typedef unsigned char BBRFReader_SW2;
typedef unsigned char BBRFReader_ASC;
typedef unsigned char BBRFReader_PurseVal;
typedef unsigned char BBRFReader_RegAddr;
typedef unsigned char BBRFReader_RegData;
typedef unsigned char BBRFReader_CardCommand;
typedef unsigned char BBRFReader_CardData;
typedef unsigned char BBRFReader_TimeOut;



typedef struct _tagSW
{
	BBRFReader_SW1 		SW1;
	BBRFReader_SW2		SW2;
}BBRFReader_SW;



typedef struct _tagMifare
{
	BBRFReader_SCT 				SmartCard_Type;
	BBRFReader_ASC 				Authenticate_StateBit;
	BBRFReader_ABL				AddressBlock;				//사용자 입력
	BBRFReader_LE				LE;							//Combine Command 추가
	BBRFReader_LC				LC;							//Combine Command 추가
}BBRFReader_Mifare;


//ISO14443_B
typedef enum
{
	BB_RFREADER_SLOT_1=0x01,
	BB_RFREADER_SLOT_2=0x02,
	BB_RFREADER_SLOT_4=0x04,
	BB_RFREADER_SLOT_8=0x08,
	BB_RFREADER_SLOT_16=0x10,
	BB_RFREADER_OTHER_SLOT
}BBRFReader_NBSLOT;



typedef enum
{
	BB_RFREADER_CONSTANT=0x50
}BBRFReader_ATQBConstant;


typedef struct _tagATQB
{
	BBRFReader_ATQBConstant 	Constant;
	BBRFReader_PUPI				PICC[4];
	BBRFReader_APPDATA			AppData[4];
	BBRFReader_Protocol_Info	Pro_Info[3];
} BBRFReader_ATQBResponse;



//Slot Maker Bitrate
typedef enum
{
	READER_TO_CARD_106=0x00,
	READER_TO_CARD_212=0x01,
	READER_TO_CARD_424=0x02,
	READER_TO_CARD_848=0x03,

	CARD_TO_READER_106=0x00,
	CARD_TO_READER_212=0x04,
	CARD_TO_READER_424=0x08,
	CARD_TO_READER_848=0x0C
}BitRate;


typedef struct _tagBitRate
{
	BitRate 	ReaderToCard;
	BitRate 	CardToReader;
}BBRFReader_BitRate;


typedef enum    //리턴된 값과 AND를 취하여 MBLI와 CID를 얻을수 있다..
{
	BB_RFREADER_NOT_SUPPORT=0x00,
	BB_RFREADER_MBLI=0xF0,
	BB_RFREADER_CID=0x0F
}BBRFReader_Attrib;


//ISO15693=================================================================================
typedef enum
{
    BB_RFREADER_ISO15693_STANDARDMODE=0x00,
    BB_RFREADER_ISO15693_FASTMODE=0x01
}BBRFReader_ISO15693_DataCordingMode;



typedef enum
{
    BB_RFREADER_ISO15693_INVENTORY_16_SLOT = 0x00,
    BB_RFREADER_ISO15693_INVENTORY_1_SLOT = 0x01
}BBRFReader_ISO15693_SLOT;



typedef enum
{
    BB_RFREADER_ISO15693_INVENTORY_AFIF_NOTVAL = 0x00,
    BB_RFREADER_ISO15693_INVENTORY_AFIF_VAL = 0x01,
}BBRFReader_ISO15693_AFIF;

typedef unsigned char BBRFReader_ISO15693_AFIV;
typedef unsigned char BBRFReader_ISO15693_MSKL;
typedef unsigned char BBRFReader_ISO15693_MSKV;
typedef unsigned char BBRFReader_ISO15693_COLLPOS;
typedef unsigned char BBRFReader_ISO15693_FLAG;
typedef unsigned char BBRFReader_ISO15693_DSFID;
typedef unsigned char BBRFReader_ISO15693_UID;
typedef unsigned char BBRFReadeR_ISO15693_InventoryStatus;

typedef struct _tagISO15693_InventoryRequest
{
    BBRFReader_ISO15693_SLOT         SlotSel;
    BBRFReader_ISO15693_AFIF         AFIDValChk;
    BBRFReader_ISO15693_AFIV         AFIVVal;
    BBRFReader_ISO15693_MSKL         MaskLen;
    BBRFReader_ISO15693_MSKV         *MaskVal;
    BBRFReader_ISO15693_COLLPOS      CollPos;
    BBRFReader_ISO15693_FLAG         Flag;
    BBRFReader_ISO15693_DSFID        DSFID;
    BBRFReader_ISO15693_UID          UID[8];
    BBRFReadeR_ISO15693_InventoryStatus InventoryStatus;
    DWORD                            ValidField;
}BBRFReader_ISO15693_InventoryRequest;



//Optional Command

typedef enum
{
    BB_RFREADER_ISO15693_ADDRESS    = 0x00,
    BB_RFREADER_ISO15693_NONADDRESS = 0x01,
    BB_RFREADER_ISO15693_SELECT     = 0x02

}BBRFReader_ISO15693_MODE;

typedef enum
{
    BB_RFREADER_ISO15693_NOTSECURE  = 0x00,
    BB_RFREADER_ISO15693_SECURE     = 0x01
}BBRFReader_ISO15693_SECURE;


typedef enum
{
    BB_RFREADER_ISO15693_ASYNCRONOUS    = 0x00,
    BB_RFREADER_ISO15693_POLLED         = 0x01
}BBRFReader_ISO15693_Reply;



typedef unsigned char BBRFReader_ISO15693_BlockNum;
typedef unsigned char BBRFReader_ISO15693_BlockCount;
typedef unsigned char BBRFReader_ISO15693_RespSEC;
typedef unsigned char BBRFReader_ISO15693_Data;
typedef unsigned char BBRFReader_ISO15693_ERROR;
typedef unsigned char BBRFReader_ISO15693_ValidField;
typedef unsigned char BBRFReader_ISO15693_INFOF;
typedef unsigned char BBRFReader_ISO15693_OTHER;

typedef struct _tagISO15693_OptionCommand
{
    BBRFReader_ISO15693_MODE        Mode;
    BBRFReader_ISO15693_SECURE      Secure;
    BBRFReader_ISO15693_BlockNum    BlockNumber;
    BBRFReader_ISO15693_BlockCount  BlockCount;
    BBRFReader_ISO15693_UID         *UID;
    BBRFReader_ISO15693_Reply       Reply;
    BBRFReader_ISO15693_FLAG        Flag;
    BBRFReader_ISO15693_RespSEC     *RespSecure;
    BBRFReader_ISO15693_RespSEC     RespSecureLen;
    BBRFReader_ISO15693_Data        *Data;
    BBRFReader_ISO15693_ERROR       Error;
    BBRFReader_ISO15693_DSFID       DSFID;
    BBRFReader_ISO15693_AFIV        AFIVVal;
    BBRFReader_ISO15693_INFOF       InfoFlag;
    BBRFReader_ISO15693_OTHER       *Other;
    DWORD                           ValidField;
}BBRFReader_ISO15693_OptionCommand;

#define     FLAG_NOT_ALL       0
#define     FLAG_AFIV_SET      1        // 0 : Not Setting  1 : Setting
#define     FLAG_MASK_SET      2        // 0 : Not Setting  1 : Setting
#define     FLAG_UID_SET       4        // 0 : Not Setting  1 : Setting
#define     FLAG_SECS_SET      8        // 0 : Not Setting  1 : Setting



BBAPI HBBICCARD WINAPI BBRFReaderOpen();
BBAPI DWORD WINAPI BBRFReaderClose(HBBICCARD);

//ISO14443_A
BBAPI DWORD WINAPI BBRFReaderISO14443_A_RequestALL_A( HBBRFREADER , BBRFReader_ATQA * );
BBAPI DWORD WINAPI BBRFReaderISO14443_A_RequestA( HBBRFREADER , BBRFReader_ATQA *);
BBAPI DWORD WINAPI BBRFReaderISO14443_A_Anticollision( HBBRFREADER , BBRFReader_CascadeLevel, BBRFReader_SerialNumber *, BBRFReader_Collision * );
BBAPI DWORD WINAPI BBRFReaderISO14443_A_Select( HBBRFREADER hRFReader, BBRFReader_CascadeLevel, BBRFReader_SerialNumber *, BBRFReader_SAK *);
BBAPI DWORD WINAPI BBRFReaderISO14443_A_Halt( HBBRFREADER );
BBAPI DWORD WINAPI BBRFReaderISO14443_A_RequestForAnswerToSelect( HBBRFREADER, BBRFReader_CID, BBRFReader_ATS *, DWORD *);
BBAPI DWORD WINAPI BBRFReaderISO14443_A_ProtocolParameterSelection( HBBRFREADER, BBRFReader_CID, BBRFReader_BitRate);



//RF
BBAPI DWORD WINAPI BBRFReaderRF_On(HBBRFREADER);
BBAPI DWORD WINAPI BBRFReaderRF_Off(HBBRFREADER);
BBAPI DWORD WINAPI BBRFReaderRF_Reset(HBBRFREADER);
//BBAPI DWORD WINAPI BBRFReaderChangeModeType(HBBRFREADER, BBRFReader_Type);
BBAPI DWORD WINAPI BBRFReaderReadModeType(HBBRFREADER, BBRFReader_Type *);
BBAPI DWORD WINAPI BBRFReaderGetReaderParam(HBBRFREADER, BBRFReader_RFBaudRateTypeA *, BBRFReader_RFBaudRateTypeB *);


//MIFARE

BBAPI DWORD WINAPI BBRFReaderMifare_Authenticate ( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Read( HBBRFREADER hRFReader, BBRFReader_Mifare *, BBRFReader_MifareData *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Write( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_MifareData *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Subtract_Value( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_MifareMemVal *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Add_Value( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_MifareMemVal *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Restore( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_SW *);
BBAPI DWORD WINAPI BBRFReaderMifare_Transfer( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_SW *);

//MIFARE Combine
BBAPI DWORD WINAPI BBRFReaderMifareCombine_Read( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_Write( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_CreateValBlock( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_ReadValue( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_SubtrackValue( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_AddValue( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_CopyValue( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_DefAccessCondition( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );
BBAPI DWORD WINAPI BBRFReaderMifareCombine_LoadKey( HBBRFREADER hRFReader, BBRFReader_Mifare *Mifare_Authenticate, BBRFReader_MifareData *pData, BBRFReader_SW *Status_SW );


//MIFARE Purse

BBAPI DWORD WINAPI BBRFReaderMifarePurse_Create( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_PurseVal *,BBRFReader_SW * );
BBAPI DWORD WINAPI BBRFReaderMifarePurse_Read( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_PurseVal *, BBRFReader_SW * );
BBAPI DWORD WINAPI BBRFReaderMifarePurse_Debit( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_PurseVal *, BBRFReader_SW * );
BBAPI DWORD WINAPI BBRFReaderMifarePurse_Credit( HBBRFREADER, BBRFReader_Mifare *, BBRFReader_PurseVal *, BBRFReader_SW *);


//ISO14443_B
BBAPI DWORD WINAPI BBRFReaderISO14443_B_Request_B( HBBRFREADER, BBRFReader_AFI, BBRFReader_NBSLOT, BBRFReader_ATQBResponse *);
BBAPI DWORD WINAPI BBRFReaderISO14443_B_RequestALL_B( HBBRFREADER, BBRFReader_AFI, BBRFReader_NBSLOT, BBRFReader_ATQBResponse *);
BBAPI DWORD WINAPI BBRFReaderISO14443_B_SlotMarker( HBBRFREADER , BBRFReader_SLOTNB, BBRFReader_ATQBResponse *);
BBAPI DWORD WINAPI BBRFReaderISO14443_B_Attrib( HBBRFREADER, BBRFReader_PUPI *, BBRFReader_Protocol_Info *, BBRFReader_CID , BBRFReader_BitRate, BBRFReader_AttribResponse *);
BBAPI DWORD WINAPI BBRFReaderISO14443_B_Halt_B( HBBRFREADER, BBRFReader_PUPI *);


//ISO1443 A&B
BBAPI DWORD WINAPI BBRFReaderISO14443_AB_DeSelect( HBBRFREADER hRFReader, BBRFReader_CID );
BBAPI DWORD WINAPI BBRFReaderISO14443_AB_Exchange( HBBRFREADER hRFReader, BBRFReader_CID, BBRFReader_NAD, BBRFReader_CardCmd *, DWORD dwCardCmdLen , BBRFReader_CardResp *, DWORD *);
BBAPI DWORD WINAPI BBRFReaderISO14443_AB_Poll( HBBRFREADER hRFReader, BBRFReader_CID );
BBAPI DWORD WINAPI BBRFReaderISO14443_AB_GetMode_15_Status( HBBRFREADER hRFReader, BBRFReader_CardResp * );


//Transparent Communication Command
BBAPI DWORD WINAPI BBReadControllerReg( HBBRFREADER, BBRFReader_RegAddr *, BBRFReader_RegData * );
BBAPI DWORD WINAPI BBWriteControllerReg( HBBRFREADER, BBRFReader_RegAddr *, BBRFReader_RegData * );
BBAPI DWORD WINAPI BBTransExchange( HBBRFREADER, BBRFReader_CardCommand *, DWORD, BBRFReader_CardData *, DWORD * );
BBAPI DWORD WINAPI BBControllerTimeOut( HBBRFREADER, BBRFReader_TimeOut * );


//ISO15693
BBAPI DWORD WINAPI BBRFReaderISO15693_ChangeDataCordingMode( HBBRFREADER hRFReader, BBRFReader_ISO15693_DataCordingMode dwMode );
BBAPI DWORD WINAPI BBRFReaderISO15693_InventoryRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_InventoryRequest *InventoryRequest );
BBAPI DWORD WINAPI BBRFReaderISO15693_StayQuietRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_InventoryRequest *InventoryRequest );
BBAPI DWORD WINAPI BBRFReaderISO15693_ReadSingleBlockRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *ReadCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_WriteSingleBlockRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *WriteCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_LockBlockRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *LockCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_ReadMultipleBlockRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *ReadCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_WriteMultipleBlockRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *WriteCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_SelectRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *SelectCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_ResetToReadyRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *ResetReadyCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_WriteAFIRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *WriteAFICommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_LockAFIRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *LockAFICommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_WriteDSFIDRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *WriteDSFIDCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_LockDSFIDRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *LockDSFIDCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_GetSysInfoRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *GetSysInfoCommand );
BBAPI DWORD WINAPI BBRFReaderISO15693_MultiBlockSecurityRequest( HBBRFREADER hRFReader, BBRFReader_ISO15693_OptionCommand *MultiBlockSecurity );

//UserInput
BBAPI DWORD WINAPI BBUserInput( HBBICCARD hRFReader, unsigned char *strCmd, DWORD CmdLen, unsigned char *strResp, DWORD *RespLen );


//NEW API
typedef struct _CommSettings
{
	DWORD			baudrate;
	char 			protocol;
}CommSettings;


typedef struct _ReaderSettings
{
//	CommSettings	commSettings;
	DWORD			baudrate;
	char 			protocol;
	unsigned char 			stationID;
}ReaderSettings;


enum
{
	rcGetVersion			= 101,
	rcResetReader			= 102,
	rcResetField			= 103,
	rcSetTagType			= 104,
	rcWriteUserport			= 105,
	rcAntennaOn				= 106,
	rcAntennaOff			= 107,

	rcIsoReqa				= 201,
	rcSelect				= 202,
	rcMultiList				= 203,
	rcRATS					= 204,
	rcPPSR					= 205,
	rcHaltA					= 206,
	rcSel1					= 207,
	rcSel2					= 208,
	rcSel3					= 209,
	rcHighSpeed				= 210,

	rcIsoReqb				= 211,
	rcSlotMarker			= 212,
	rcAttrib				= 213,
	rcHaltB					= 214,
	rcDeSelect				= 215,

	rcAPDU					= 216,
	rcGetPaypassModeStatus	= 217,

	rcReadBlock				= 301,
	rcWriteBlock			= 302,
	rcReadValue				= 303,
	rcWriteValue			= 305,
	rcCopyValue				= 306,
	rcLogin					= 307,
	rcDecreaseValue			= 308,
	rcIncreaseValue			= 309,
	rcTransfer				= 310,

	rcChangeDataCodeMode	= 401,
	rcInventoryRequest		= 402,
	rcQuietRequest			= 403,
	rcReadyRequest			= 404,
	rcSelectRequest			= 405,
	rcReadSingleBlock		= 406,
	rcWriteSingleBlock		= 407,
	rcLockBlock				= 408,
	rcGetSystemInfo			= 409,
	rcWriteMultiBlock		= 410,
	rcReadMultiBlock		= 411,
	rcWriteAFI				= 412,
	rcLockAFI				= 413,
	rcWriteDSFID			= 414,
	rcLockDSFID				= 415,
	rcGetMultiSecStatus		= 416,
	rcCtxReqt				= 501, 
	rcCtxIdentify			= 502, 
	rcCtxSelect				= 503, 
	rcCtxSelectAll			= 504, 
	rcCtxRead				= 505, 
	rcCtxWrite				= 506, 
	rcCtxUpdate				= 507, 
	rcCtxDesActive			= 508, 
	rcCtxMultiRead			= 509, 

	rcGetSAMTimeout			= 601,
	rcSetSAMTimeout			= 602,
	rcSetSAMIoMode			= 603,
	rcDefSAMCardType		= 604,
	rcSAMPowerOn			= 605,
	rcSAMPPSSet				= 606,
	rcICSAMConfigureSerial	= 607,
	rcSAMExchangeAPDU		= 608,
	rcSAMPowerDown			= 609,
	rcICPowerOn				= 610,
	rcGetICStatus			= 611,
	rcSetICStatus			= 612,
	rcSetICIOMode			= 613,
	rcICDefCardType			= 614,
	rcICPPSSet				= 615,
	rcICExchangeAPDU		= 616,
	rcICPowerDown			= 617,
	rcGetSAMStatus			= 618,
	rcGetICFirmwareVersion	= 619,

	rcWriteRegister,
	rcReadRegister
};


enum DESFireCmd
{
	cSAMOnOff				= 0x00,
	cAuthenticate   		= 0x01,
	cChangeKeySettings   	= 0x02,
	cGetKeySettings   		= 0x03,
	cChangeKey   			= 0x04,
	cGetKeyVersion   		= 0x05,
	cCreateApplication   	= 0x06,
	cDeleteApplication   	= 0x07,
	cGetApplicationIDs   	= 0x08,
	cSelectApplication   	= 0x09,
	cFormatPICC   			= 0x0A,
	cGetVersion   			= 0x0B,
	cGetFileIDs   			= 0x0C,
	cGetFileSettings   		= 0x0D,
	cChangeFileSettings   	= 0x0E,
	cCreateStandardDataFile = 0x0F,
	cCreateBackupDataFile   = 0x10,
	cCreateValueFile   		= 0x11,
	cCreateLinearRecordFile = 0x12,
	cCreateCyclicRecordFile = 0x13,
	cDeleteFile   			= 0x14,
	cReadData   			= 0x15,
	cReadRecords   			= 0x16,
	cWriteData   			= 0x17,
	cWriteRecords   		= 0x18,
	cGetValue   			= 0x19,
	cCredit   				= 0x1A,
	cDebit   				= 0x1B,
	cLimitedCredit   		= 0x1C,
	cClearRecordFile   		= 0x1D,
	cCommitTransaction   	= 0x1E,
	cAbortTransaction   	= 0x1F,
	cSetFileSettings   		= 0x20,

	cDisableCrypto   		= 0x30,
	cChangeKeyEntry   		= 0x31,
	cGetKeyEntry   			= 0x32,
	cChangeKUCEntry   		= 0x33,
	cGetKUCEntry   			= 0x34,
	cChangeKeyPICC   		= 0x35,
	cGetVersionSAM   		= 0x36,
	cAuthenticateHost   	= 0x37,
	cSelectApplicationSAM   = 0x38,
	cDumpSessionKey   		= 0x39,
	cLoadInitVector   		= 0x3A,
	cAuthenticatePICC1   	= 0x3B,
	cAuthenticatePICC2   	= 0x3C,
	cVerifyMAC   			= 0x3D,
	cGenerateMAC   			= 0x3E,
	cDecipherData   		= 0x3F,
	cEncipherData   		= 0x40,
	cSetLogicalChannel  	= 0x42
};

enum CalypsoCmd
{
	// SAM
	cChangeSpeed					= 0x01,
	cCheckSignedData				= 0x02,
	cCipherCardData					= 0x03,
	cCipherData						= 0x04,
	cCipherSAMData					= 0x05,
	cCipherSecret					= 0x06,
	cComputeDigitalCertificate		= 0x07,
	cComputeDigitalSignature		= 0x08,
	cCTMGetChallenge				= 0x09,
	cCTMGetKey						= 0x0A,
	cCTMVerifyAntiCloseSignature	= 0x0B,
	cDataDecipher					= 0x0C,
	cDataEncipher					= 0x0D,
	cDigestClose					= 0x0E,
	cDigestInit						= 0x0F,
	cDigestUpdate					= 0x10,
	cEPCancelPurchase				= 0x11,
	cEPCheck						= 0x12,
	cEPLoad							= 0x13,
	cEPPurchase						= 0x14,
	cExternalAuthenticate			= 0x15,
	cSAMGetChallenge				= 0x16, // Get Challenge
	cSAMGetResponse					= 0x17, // Get Response
	cGiveRandom						= 0x18,
	cInternalAuthenticate			= 0x19,
	cSAMReadData					= 0x1A, // Read Data
	cReadKeyParameters				= 0x1B,
	cReadWorkKeyParameters			= 0x1C,
	cSelectDiversifier				= 0x1D,
	cVerifyDigitalCertificate		= 0x1E,
	cVerifyDigitalSignature			= 0x1F,
	cVerifySecret					= 0x20,
	cSAMWriteData					= 0x21, // Write Data
	cWriteKey						= 0x22,
	// RFID
	cGetATR							= 0x40,
	cGetResponse					= 0x41,
	cSelectApp						= 0x42, // cSelectApplication
	cSelectFile						= 0x43,
	cInvalidate						= 0x44,
	cRehabilitate					= 0x45,
	cAppendRecord					= 0x46,
	cDecrease						= 0x47,
	cDecreaseMultiple				= 0x48,
	cIncrease						= 0x49,
	cIncreaseMultiple				= 0x4A,
	cReadBinary						= 0x4B,
	cReadRecord						= 0x4C,
	cReadRecordMultiple				= 0x4D,
	cSearchRecordMultiple			= 0x4E,
	cUpdateBinary					= 0x4F,
	cUpdateRecord					= 0x50,
	cWriteBinary					= 0x51,
	cWriteRecord					= 0x52,
	cOpenSecureSession				= 0x53,
	cCloseSecureSession				= 0x54,
	cGetChallenge					= 0x55,
	cVerifyPin						= 0x56,
	cChangeKeys						= 0x57, // Change Key
	cGetEP							= 0x58,
	cDebitEP						= 0x59,
	cUnDebitEP						= 0x5A,
	cReloadEP						= 0x5B,
	cStatus							= 0x5c,
	cReadRecordStamped				= 0x5D,
	cAbortSecureSession				= 0x5E
};
BBAPI char 	WINAPI BBRFReader_OpenComm( const char *commDevice, char autoDetect, const CommSettings* commSettings );
BBAPI void  WINAPI BBRFReader_CloseComm();
BBAPI DWORD	WINAPI BBRFReader_OpenReader( unsigned char id, short knownReader );
BBAPI DWORD	WINAPI BBRFReader_CloseReader();
BBAPI DWORD	WINAPI BBRFReader_ResetReader();
BBAPI DWORD	WINAPI BBRFReader_ReadRegister( BYTE *byRegAddr );
BBAPI DWORD	WINAPI BBRFReader_WriteRegister( BYTE *byRegAddr, BYTE *byRegData );
BBAPI void WINAPI BBRFReader_EmptyCommRcvBuffer();
BBAPI char WINAPI BBRFReader_GetResumeState();
BBAPI void WINAPI BBRFReader_SetCommBaudrate( DWORD dwBaudrate );
BBAPI DWORD WINAPI BBRFReader_GetCommBaudrate();
BBAPI char WINAPI BBRFReader_GetCommProtocol();
BBAPI void WINAPI BBRFReader_SetCommProtocol( char protocol );
BBAPI void WINAPI BBRFReader_SetCommTimeout( DWORD timeout );
BBAPI DWORD WINAPI BBRFReader_GetCommTimeout();

BBAPI ReaderSettings WINAPI BBRFReader_GetReaderConfig();
BBAPI void WINAPI BBRFReader_GetReaderConfigA(ReaderSettings *pSettings);
BBAPI void WINAPI BBRFReader_SetReaderConfig( ReaderSettings *readerset);
BBAPI unsigned char* WINAPI BBRFReader_GetReaderType( unsigned char *deviceVersion );
BBAPI char WINAPI BBRFReader_GetBinProtocol();
BBAPI char WINAPI BBRFReader_GetDebugOutputState();
BBAPI void WINAPI BBRFReader_SetDebugOutputState( char active );
BBAPI char*	WINAPI BBRFReader_GetDebugOutput( char *buffer );
BBAPI DWORD	WINAPI BBRFReader_SendCommand( char *readerCmd, unsigned char *data );
BBAPI DWORD WINAPI BBRFReader_SendCommandGetData( char *readerCmd, unsigned char *data, unsigned char *resultBuf );
BBAPI DWORD WINAPI BBRFReader_SendCommandGetDataTimeout( char *readerCmd, unsigned char* data, unsigned char* resultBuf, long timeout);

BBAPI DWORD	WINAPI BBRFReader_GetData( unsigned char* resultBuf );
BBAPI DWORD	WINAPI BBRFReader_GetDataTimeout( unsigned char *buffer, DWORD timeout );

//BBAPI DWORD	WINAPI BBRFReader_DESFire( DESFireCmd command, const unsigned char *data, unsigned char *resultBuf );
BBAPI DWORD	WINAPI BBRFReader_DESFire( char command, const unsigned char *data, unsigned char *resultBuf );
BBAPI DWORD	WINAPI BBRFReader_Calypso( byte command, const unsigned char *data, unsigned char *resultBuf );

BBAPI char	WINAPI BBRFReader_GetDESFireSAMTimeout();
BBAPI void	WINAPI BBRFReader_SetDESFireSAMTimeout( char cTimeout );
BBAPI char* WINAPI BBRFReader_DESEncrypt( char cOptions, char* pbyKey, char* pbyData, long lLength, char* pbyBuffer );
BBAPI char* WINAPI BBRFReader_DESDecrypt( char cOptions, char* pbyKey, char* pbyData, long lLength, char* pbyBuffer );
BBAPI BOOL WINAPI BBRFReader_OpenICComm();
BBAPI BOOL WINAPI BBRFReader_CloseICComm();
BBAPI BOOL WINAPI BBRFReader_ICPowerDown( BOOL bIsSAM, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_ICPowerOn( BOOL bIsSAM, BYTE *pbyInputParam, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_ICChangePPS( BOOL bIsSAM, BYTE *pbyInputParam, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_ICChangebaudrate( DWORD dwBaudRate, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_SAMSlotIOMode( BOOL bIsSAM, BYTE byMode, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_SAMDefType( BYTE bySAMSelectType, BYTE bySlotSAM, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_ICDefType( BYTE bySAMSelectType, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_SendSAMCommand( BOOL bIsSAM, BYTE *pbyInputData );
BBAPI BOOL WINAPI BBRFReader_GetSAMData( BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_GetSAMDataTimeout( unsigned char *pbyResultBuf, DWORD timeout );
BBAPI BOOL WINAPI BBRFReader_GetICStatus( BOOL bIsSAM, BYTE *pbyResultBuf );
BBAPI BOOL WINAPI BBRFReader_GetICVersion( char *pGetVersion, BYTE *pLengthOfData );
BBAPI void WINAPI BBRFReader_GetEMVVersion( char *pcGetVersion );


//KeyMapping을 위한 API.
BBAPI BOOL WINAPI BBKeyMapping_Init();
BBAPI WORD WINAPI BBKeyMapping_GetKeyCount();
BBAPI BOOL WINAPI BBKeyMapping_GetKeyData(const int nIndex,WORD *wKey,WORD *wType,TCHAR *strName,unsigned int buffer_length);
BBAPI BOOL WINAPI BBKeyMapping_GetAllKeyData(WORD *wKey,WORD *wType,TCHAR **strName,unsigned int buffer_length);
BBAPI BOOL WINAPI BBKeyMapping_SetKeyData(const int nIndex,const WORD wKey,const WORD wType);
BBAPI BOOL WINAPI BBKeyMapping_SetAllKeyData(const WORD *wKey,const WORD *wType);
BBAPI BOOL WINAPI BBKeyMapping_XMLFileSave();
BBAPI BOOL WINAPI BBKeyMapping_Close();
BBAPI BOOL WINAPI BBKeyMapping_XMLCreateHead(const int nCount);
BBAPI BOOL WINAPI BBKeyMapping_XMLCreateData(const WORD wCode,const UINT nType,const int nKeyname);
BBAPI BOOL WINAPI BBKeyMapping_XMLCreateTail();
BBAPI BOOL WINAPI BBKeyMapping_SetDefaultKey();

/*
 *	gps
 */

BBAPI BOOL WINAPI BBGPS_FlashEnable(BOOL bEnable);

#ifdef __cplusplus
}
#endif


#endif//__BBAPPAPI_H__BLUEBIRDSOFT
