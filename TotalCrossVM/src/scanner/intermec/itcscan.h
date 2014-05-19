/*******************************************************************************
 *
 *  FILE NAME:  ITCScan.h
 *  PURPOSE:    Contains library ADC functions
 *
 *  AUTHOR:     Ronald van der Putten
 *
 *  COPYRIGHT (c) 1998-2003 INTERMEC TECHNOLOGIES CORPORATION, ALL RIGHTS RESERVED
 *
 ******************************************************************************/



#ifndef INCLUDED_ITCSCAN_H                /* Has this file been included before?  */
#define INCLUDED_ITCSCAN_H                /* No, remember it has been now         */



#ifdef __cplusplus
extern "C"{
#endif 

// Global constants

// Max string lengths


#define ITCSCAN_MAX_GRID_FILTER_LENGTH 240
#define ITCSCAN_MAX_ERROR_STRING_LENGTH 512

// Error strings

#define ITCSCAN_UNKNOWN_READ_ERROR L"Unknown Scanner Error."
#define ITCSCAN_ITC50_NOT_AVAIL_ERROR L"Error string retrieval failed for itc50.dll is not loaded."

// ITCSCAN Error codes

#define ITCSCAN_SUCCESS              0x00000000L
#define ITCSCAN_ERROR               -0x00000001L
#define ITCSCAN_KEY_NOT_FOUND       -0x00000002L
#define ITCSCAN_VALUE_NOT_FOUND        -0x00000003L
#define ITCSCAN_CANCEL_VALUE        -0x00000004L
#define ITCSCAN_XML_SETTINGS_FILE_ERROR   -0x00000005L
#define ITCSCAN_BUFFER_OVERRUN_ERROR    -0x00000006L
#define ITCSCAN_NO_INTERFACE_ERROR      -0x00000007L
#define ITCSCAN_NO_SCANNER_ERROR        -0x00000008L
#define ITCSCAN_WRONG_DATASTRUCT_SIZE   -0x00000009L
// Registry keys

#define ITCSCAN_DEVICES_REG_KEY L"SOFTWARE\\Intermec\\ADCDevices"



typedef struct
{
   BYTE  *rgbDataBuffer;
   DWORD dwDataBufferSize;
   DWORD dwBytesReturned;
   DWORD   dwTimeout;
   UINT32   iSymbology;
   UINT32   iDataType;
   
}READ_DATA_STRUCT;

typedef struct
{
   size_t   StructSize;
   BYTE  *rgbDataBuffer;
   DWORD dwDataBufferSize;
   DWORD dwBytesReturned;
   DWORD   dwTimeout;
   UINT32   iSymbology;
   UINT32   iDataType;
   DWORD hDevice;
   
}READ_DATA_STRUCT_EX;



typedef struct
{
    DWORD nStructBytes;
    DWORD nCharsInName;
    WCHAR rgcFriendlyName[ 64 ];
    DWORD dwPortID;
    DWORD nCharsInDeviceType;
    WCHAR rgcDeviceType[ 64 ];
    DWORD nCharsInPortName;
    WCHAR rgcPortName[ 64 ];

}ITCSCAN_DEVICE_DETAILS;

typedef struct 
{ 
    DWORD wStructSize;
    WCHAR rgcSymbologyID[ 10 ];
    DWORD nActualSymbologyIDBytes;
}  ITCSCAN_SID;



// Function declarations

HRESULT ITCSCAN_Open(INT32 *pHandle,LPCTSTR pszDeviceName);
HRESULT ITCSCAN_Open2(INT32 *pHandle,LPCTSTR pszDeviceName, HWND hwnd); 
HRESULT ITCSCAN_Close(INT32 pHandle);
HRESULT ITCSCAN_SyncRead(INT32 pHandle, READ_DATA_STRUCT * pReadDataBlock);
HRESULT ITCSCAN_SyncReadEx(INT32 pHandle, READ_DATA_STRUCT_EX * pReadDataBlock);
HRESULT ITCSCAN_SyncReadWSid(INT32 pHandle, READ_DATA_STRUCT * pReadDataBlock, ITCSCAN_SID *pSid);                         
HRESULT ITCSCAN_CancelRead (INT32 pHandle, BOOL FlushBufferedData,DWORD *pdwTotalDiscardedMessages,DWORD *pdwTotalDiscardedBytes);
HRESULT ITCSCAN_GetAttribute (INT32 pHandle, int eAttribID, BYTE rgbBuffer[], DWORD dwBufferSize, DWORD *pnBufferData);
HRESULT ITCSCAN_SetAttribute(INT32 pHandle,int eAttribID,BYTE rgbData[],DWORD dwBufferSize );
HRESULT ITCSCAN_SetTriggerScanner(INT32 pHandle,bool TriggerOn);
HRESULT ITCSCAN_GetTriggerScanner(INT32 pHandle,int * pTriggerOn);
HRESULT ITCSCAN_SetDataLED(INT32 pHandle,bool On);
HRESULT ITCSCAN_SetReadLED(INT32 pHandle,bool On);
HRESULT ITCSCAN_SetScannerFilterGrid(INT32 pHandle,LPCTSTR pszGrid);
HRESULT ITCSCAN_GetScannerFilterGrid(INT32 pHandle,LPTSTR pszGrid, INT32 *Length);
HRESULT ITCSCAN_SetScannerEnable(INT32 pHandle,INT32 OnOff);
HRESULT ITCSCAN_GetScannerEnable(INT32 pHandle,INT32 *OnOff);
LONG ITCSCAN_GetDeviceTotal (DWORD * pdwTotal);
LONG ITCSCAN_GetDevice (DWORD dwIndex, LPTSTR deviceNameBuffer, DWORD * pBufferLength);
LONG ITCSCAN_GetDefaultDevice (LPTSTR lpDeviceNameBuffer, DWORD * pBufferLength);
void ITCSCAN_GetErrorString (HRESULT errorCode, TCHAR *pErrorString);
HRESULT ITCSCAN_GetBarcodeConfiguration (TCHAR *apScannerConnection, TCHAR *apCfgStr, TCHAR *apCfgValue, INT32 aMaxLen);
HRESULT ITCSCAN_SetBarcodeConfiguration (TCHAR *apScannerConnection, TCHAR *apCfgStr);
HRESULT ITCSCAN_SetBarcodeConfigurationFromFile (TCHAR *apCfgStr);
HRESULT ITCSCAN_GetBarcodeMessage (INT32 pHandle, int *pMsgType, INT32 *pMsgData);


bool ITCSCAN_GetContinuousReadFlag(INT32 pHandle);
void ITCSCAN_SetContinuousReadFlag(INT32 pHandle, bool ContinReadFlag);
HRESULT ITCSCAN_SetActionOnNextDevice(INT32 pHandle,int hDevice);

HRESULT ITCSCAN_GetDeviceDetails(INT32 pHandle,DWORD hDevice,ITCSCAN_DEVICE_DETAILS *stDeviceDetails);


#ifdef __cplusplus
}
#endif

#endif
