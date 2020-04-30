// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef _BCRSERVICE_H
#define _BCRSERVICE_H

// Virtual key code for the dedicated trigger keys
#define OEM_VK_BCR_TRIGGER 							0xF4

//the Max. length of password for BCR firmware update
#define MAX_PWD								12

// The Max length of option string from configuration setting API
#define MAX_STRING                          256


// operate BCR IOControl return value
#define BCR_SUCCESS                          1

// Firmware Update Post Messages to the windows
#define	BCR_FWUPD_CMP_PWD			(WM_USER + 100)	 //	Be comparing the password
#define	BCR_FWUPD_CHK_FILE			(WM_USER + 101)	 //	Be checking	the	file of	firmware
#define	BCR_FWUPD_DWNLD_2ND_LDR		(WM_USER + 102)	 //	Be downloading the 2nd loader
#define	BCR_FWUPD_ERASE_MEMORY		(WM_USER + 103)	 //	Be erasing memory
#define	BCR_FWUPD_DWNLD_MAIN_PROG	(WM_USER + 104)	 //	Be downloading the main	program
#define	BCR_FWUPD_PROGRESS			(WM_USER + 105)	 //	Represents the downloading progress	in percentage
#define	BCR_FWUPD_RESULT			(WM_USER + 106)	 //	Indicates the downloading process is stop in success or	fail.


//Additional message-specific information for Firmware Update define message
#define	FWUPD_SUCCESS					0	// The operation completed successfully.
#define	FWUPD_INVALID_PARAMETER			1	// The parameter is	incorrect.
#define	FWUPD_WRONG_PASSWORD			2	// The password	is incorrect.
#define	FWUPD_FILE_OPEN_FAILED			3	// Failed to open the file of firmware.
#define	FWUPD_INVALID_FIRMWARE			4	// The content of firmware is invalid.
#define	FWUPD_DWNLD_2ND_LDR_FAILED		5	// Failed to download the 2nd loader.
#define	FWUPD_ERASE_MEMORY_FAILED		6	// Memory erasion is failed.
#define	FWUPD_DWNLD_MAIN_PROG_FAILED	7	// Failed to download the main program.
#define	FWUPD_CANCELLED					8	// The operation was canceled by the user.
#define FWUPD_UNKNOWN_MODULE_TYPE		9	// The barcode reader module type is unknown.


// Define the possible constant values for sBCR_Status
//
//
// Possible combination values for sBCR_Status.dwMask
#define MASK_MODULE_VER				0x0001
#define MASK_SOUND_FILE				0x0002
#define MASK_BCTYPE						0x0004
#define MASK_TRIGGER_KEY			0x0008
#define MASK_READ_MODE				0x0010
#define MASK_MODULE_ENABLE		0x0020
#define MASK_SOUND_ENABLE			0x0040
#define MASK_KEYHOOK_EMABLE		0x0080
#define MASK_BCR_TYPE					0x0100

//Possible value of sBCR_Status.dwModuleEnable
#define BCRSTATUS_MOD_DISABLE 					1
#define BCRSTATUS_MOD_ENABLE  					2

//Possible value of sBCR_Status.dwKeyboardHook
#define BCRSTATUS_HOOK_OFF    					1
#define BCRSTATUS_HOOK_ON     					2

//Possible value of sBCR_Status.dwGoodSound
#define BCRSTATUS_GS_ENABLE   					2
#define BCRSTATUS_GS_DISABLE  					1

//Possible value of sBCR_Status.cBcrModuleType
#define BCRSTATUS_BCR_MODULE_UNKNOWN		0
#define BCRSTATUS_BCR_MODULE_1D					1
#define BCRSTATUS_BCR_MODULE_2D					2

//Possible value of sBCR_Status.dwReadMode
#define BCRSTATUS_RD_MODE_SINGLE				1
#define BCRSTATUS_RD_MODE_MULTIPLE			2


//Possible state of Trigger key when BCR_IOCTL_TRIGGER received
#define BCR_TRIG_RELEASE			1
#define BCR_TRIG_PRESS				2

//Possible value of sBCR_Status.dwTriggerKey
#define BCRSTATUS_TK_DEFAULT						1				/*No additional trigger key assigned*/
#define BCRSTATUS_TK_NONE								1				/*No additional trigger key assigned*/
#define BCRSTATUS_TK_RECORD							2
#define BCRSTATUS_TK_VOLUMEUP						3
#define BCRSTATUS_TK_VOLUMEDOWN					4
#define BCRSTATUS_TK_START							5
#define BCRSTATUS_TK_OK									6
#define BCRSTATUS_TK_SOFT1							7
#define BCRSTATUS_TK_SOFT2							8
#define BCRSTATUS_TK_SEND								9
#define BCRSTATUS_TK_END								10
#define BCRSTATUS_TK_UP									11
#define BCRSTATUS_TK_DOWN								12
#define BCRSTATUS_TK_LEFT								13
#define BCRSTATUS_TK_RIGHT							14
#define BCRSTATUS_TK_ACTION							15
#define BCRSTATUS_TK_AP1								BCRSTATUS_TK_START
#define BCRSTATUS_TK_AP2								BCRSTATUS_TK_OK

//Possible combination values for sBCR_Status.dwBCType
#define ID_UPC								(1<<0)
#define ID_EAN								(1<<1)
#define ID_CODE_39						    (1<<2)
#define ID_TRIOPTIC						    (1<<3)
#define ID_CODABAR						    (1<<4)
#define ID_INDUSTRIAL					    (1<<5)
#define ID_INTERLEAVED				        (1<<6)
#define ID_SCODE							(1<<7)
#define ID_MATRIX							(1<<8)
#define ID_CHINESE							(1<<9)
#define ID_KOREAN							(1<<10)
#define ID_IATA								(1<<11)
#define ID_MSI								(1<<12)
#define ID_TELEPEN						    (1<<13)
#define ID_UK								(1<<14)
#define ID_CODE_128						    (1<<15)
#define ID_CODE_93						    (1<<16)
#define ID_CODE_11						    (1<<17)
#define ID_RSS						        (1<<18)
#define ID_RSS_LMT					        (1<<19)
#define ID_RSS_EXP					        (1<<20)
#define ID_DMATRIX						    (1<<21)
#define ID_DMATRIX_200					    (1<<22)
#define ID_AZTEC							(1<<23)
#define ID_AZTECRUN							(1<<24)
#define ID_QR   							(1<<25)
#define ID_MAXI			    			    (1<<26)
#define ID_PDF417              	            (1<<27)
#define ID_MICROPDF         	            (1<<28)
#define ID_ALL_ADDONS         	            (1<<31)

// Possible combination values for sBCR_CustomOptionSetting.dwMask
#define MASK_OPTION_FILE			1
#define MASK_OPTION_STRING  		2

#pragma pack(1)

typedef struct
{
	HWND     hWnd;
	TCHAR    szFilePath[MAX_PATH];
  TCHAR    szPassword[MAX_PWD+1];
}sBCR_FW, *psBCR_FW;

typedef struct
{
	TCHAR   szOldPassword[MAX_PWD+1];
	TCHAR   szNewPassword[MAX_PWD+1];
}sBCR_Set_PW, *psBCR_Set_PW;

typedef struct
{
	DWORD		dwSize;										//the size of this struct
	DWORD 	dwMask;										// indicate the valid field
	char    szModuleVersion[20];			// BCR Module Version String
    TCHAR   szGoodSoundFile[MAX_PATH];// Sound file path for Good Read
    DWORD   dwBCType;         				// Bar Code Type;
    DWORD   dwTriggerKey;     				// Trigger Key
    DWORD   dwReadMode;        				//
    DWORD   dwModuleEnable;   				// Module Enable; MOD_DISABLE, MOD_ENABLE
    DWORD   dwGoodSound;      				// Enable Good Read Sound; GS_ENABLE, GS_DISABLE
    DWORD   dwKeyboardHook;
    BYTE    cBcrModuleType;
    DWORD   dwBarCodeTypeSupport;
}sBCR_Status, *psBCR_Status;

typedef struct
{
	DWORD   dwSize;                     /* size of this structure */
	DWORD   dwMask;                     /* Mask field, bit0 : OptionFile, bit1 : Optionstring */
	TCHAR   szOPFile[MAX_PATH];         /* Option file */
	char    szOPString[MAX_STRING];     /* Opttion string */
} sBCR_CustomOptionSetting, *psBCR_CustomOptionSetting;

#pragma pack()

//
// Define the various device type values.  Note that values used by Microsoft
// Corporation are in the range 0-32767, and 32768-65535 are reserved for use
// by customers.
//

#define FILE_DEVICE_BARCODE         32780

//
// Function codes 0-2047 are reserved for Microsoft; codes 2048-4095 are reserved for OEMs and IHVs.
//


#define BCR_IOCTL_TRIGGER \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2048, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_SETOPTIONS \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2049, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_UPDATE_FIRMWARE \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2052, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_GET_STATUS \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2053, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_GET_EVENTNAME \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2054, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_UPDATE_SETTING \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2055, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_SETPWD \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2056, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_CANCEL_UPDATE \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2057, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_CUSTOM_SETOPTIONS \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2061, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_SET_DEFAULT \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2062, METHOD_BUFFERED,  FILE_ANY_ACCESS)
#define BCR_IOCTL_CUSTOM_GETOPTIONS \
                     CTL_CODE(FILE_DEVICE_BARCODE,  2063, METHOD_BUFFERED,  FILE_ANY_ACCESS)


// Create a event to inform the user's application when the BCR Service resume
const TCHAR szevtBCRResume[] =  TEXT("BCR_Module_Resume_EVT");

#endif _BCRSERVICE_H