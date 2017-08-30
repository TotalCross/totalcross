#ifndef _ZBCRLIB_H
#define _ZBCRLIB_H


#ifdef LIB_INTERNAL

#define ZBCRLIB
#else
      #ifdef __cplusplus
          #define ZBCRLIB extern "C"	
      #else
          #define ZBCRLIB extern
      #endif 

#endif

#define   WM_BCR_NOTIFY  (WM_APP + 180) 

#define   BCR_NOTIFY_NO_EVENT           0xBC00

#define   BCR_NOTIFY_START_SCAN         0xBC01

#define   BCR_NOTIFY_STOP_SCAN          0xBC02

#define   BCR_NOTIFY_RECEIVE_BARCODE    0xBC03

#define   BCR_NOTIFY_SCAN_FAILED        0xBC04


#define   BCR_CLIPBOARD_OUTPUT  0x01

#define   BCR_KEYEVENT_OUTPUT  (BCR_CLIPBOARD_OUTPUT<<1)

#define   BCR_DISABLE_OUTPUT   (BCR_CLIPBOARD_OUTPUT<<2)


#define   BCR_TERMINAL_CHAR_ENTER  0x0D

#define   BCR_TERMINAL_CHAR_SPACE  0x20

#define   BCR_TERMINAL_CHAR_TAB    0x09

#define   BCR_TERMINAL_CHAR_NONE   '\0'


ZBCRLIB BOOL ZBCRGetPowerState(PBOOL lpPowerState);

ZBCRLIB BOOL ZBCRSetPower(BOOL dwState);

ZBCRLIB BOOL ZBCRStartScan(void);

ZBCRLIB BOOL ZBCRStopScan(void);

ZBCRLIB BOOL ZBCRGetLastNotifyEvent(PDWORD lpNotifyEvent);

ZBCRLIB BOOL ZBCRGetLastBarcode(LPTSTR lpszBarcode);

ZBCRLIB BOOL ZBCRGetOutputMode(PBYTE lpOutputMode);

ZBCRLIB BOOL ZBCRSetOutputMode(BYTE dwMode);

ZBCRLIB BOOL ZBCRGetTerminalChar(PBYTE lpTermChar);

ZBCRLIB BOOL ZBCRSetTerminalChar(BYTE dwTermChar);

ZBCRLIB BOOL ZBCRGetPrefix(LPTSTR lpszPrefix);

ZBCRLIB BOOL ZBCRSetPrefix(LPCTSTR lpszPrefix);

ZBCRLIB BOOL ZBCRGetSuffix(LPTSTR lpszSuffix);

ZBCRLIB BOOL ZBCRSetSuffix(LPCTSTR lpszSuffix);

#endif