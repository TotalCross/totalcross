// Imagingsample.cpp : Defines the entry point for the application.
//

#include "..\barcode.h"
#include "resource.h"
#include <commctrl.h>

typedef enum 
{
   imgOk=0,
   imgErrorMemoryAllocation=1,
   imgErrorMemoryFree=2,
   imgErrorFileCreation=3,
   imgErrorFileWrite=4,
   imgErrorInvalidDIB=5,
   imgErrorUnknown=6,
	imgErrorInvalidParm=7,
	imgErrorAlreadyInitialized=8,
	imgErrorNotInitialized=9,
   imgErrorDuplicateProfile=10,
   imgErrorReadOnlyProfile=11,
	imgErrorProfileNotFound=12,
	imgErrorInvalidProfile=13,
	imgErrorAlreadyCapturing=14,
	imgErrorNotCapturing=15,               
   imgErrorFileRead=16,
   imgErrorConfigFileRead=17,
} imgResult;

typedef enum 
{
    imgRawFull=0,
    imgRawPreview=1,
    imgFull=2,
    imgPreview=3
} imgCaptureType;

typedef void * HDIB;
#define imgPreviewCenter 4

typedef imgResult (__stdcall *procimgGetAimer)(DWORD *pdwValue);
typedef imgResult (__stdcall *procimgSetAimer)(DWORD dwValue);
typedef imgResult (__stdcall *procimgGetIllumination)(DWORD *pdwValue);
typedef imgResult (__stdcall *procimgSetIllumination)(DWORD dwValue);
typedef imgResult (__stdcall *procimgInitCamera)(LPWSTR lpFileName, LPWSTR lpProfileName, DWORD *pdwFlags);
typedef imgResult (__stdcall *procimgUninitCamera)(void);
typedef imgResult (__stdcall *procimgStartCapture)();
typedef imgResult (__stdcall *procimgStopCapture)();
typedef imgResult (__stdcall *procimgCaptureImage)(HDIB hdib, imgCaptureType CaptureType);
typedef imgResult (__stdcall *procimgDisplayImage)(HDIB hdib, HDC hdcDest, RECT *pDestRect, bool stretch, int zoom, bool center);
typedef HDIB (__stdcall *procimgCreateHDIB)(void);
typedef unsigned (__stdcall *procimgWidth )(HDIB hdib);
typedef unsigned (__stdcall *procimgHeight )(HDIB hdib);
typedef unsigned (__stdcall *procimgNumColors )(HDIB hdib);
typedef unsigned (__stdcall *procimgBitCount )(HDIB hdib);
typedef imgResult (__stdcall *procimgHistogramStretchEx)(HDIB hdib, int low, int high, int mode);
typedef imgResult (__stdcall *procimgGammaCorrection)(HDIB hdib,double gamma);
typedef imgResult (__stdcall *procimgSaveJPG)(HDIB hdib, LPTSTR szFile,int quality);

HINSTANCE imgapiDll;
procimgGetIllumination imgGetIllumination;
procimgGetAimer imgGetAimer;
procimgCreateHDIB imgCreateHDIB;
procimgInitCamera imgInitCamera;
procimgUninitCamera imgUninitCamera;
procimgDisplayImage imgDisplayImage;
procimgStopCapture imgStopCapture;
procimgCaptureImage imgCaptureImage;
procimgStartCapture imgStartCapture;
procimgSaveJPG imgSaveJPG;
procimgWidth imgWidth;
procimgHeight imgHeight;
procimgBitCount imgBitCount;
procimgNumColors imgNumColors;
procimgSetAimer imgSetAimer;
procimgSetIllumination imgSetIllumination;
procimgHistogramStretchEx imgHistogramStretchEx;
procimgGammaCorrection imgGammaCorrection;

#define MAX_LOADSTRING 100
#define ID_EDIT 1

// Global Variables:
HINSTANCE   g_hInst;       // current instance
HWND        g_hWndCommandBar; // command bar handle
HDIB        g_hDib=0;
HWND ghWnd;

// Forward declarations of functions included in this code module:
ATOM        MyRegisterClass(HINSTANCE, LPTSTR);
BOOL        InitInstance(HINSTANCE, int);
LRESULT CALLBACK  WndProc(HWND, UINT, WPARAM, LPARAM);
BOOL CALLBACK MainDlg(HWND, UINT, WPARAM, LPARAM);

/* Global Flags */
int gStretch = 1; 
int gCenter  = imgPreviewCenter;
int gZoom    = 1; 
int gHighRes = 0;
bool saved;
TCHAR filename[256];
TCHAR szWindowClass[MAX_LOADSTRING];  // main window class name

bool ImagingLibOpen()
{
   if (imgapiDll == null && (imgapiDll = LoadLibrary(TEXT("imgapi.dll"))) != null)
   {
      imgGetIllumination = (procimgGetIllumination) GetProcAddress(imgapiDll, TEXT("imgGetIllumination"));
      imgGetAimer = (procimgGetAimer) GetProcAddress(imgapiDll, TEXT("imgGetAimer"));
      imgCreateHDIB = (procimgCreateHDIB) GetProcAddress(imgapiDll, TEXT("imgCreateHDIB"));
      imgInitCamera = (procimgInitCamera) GetProcAddress(imgapiDll, TEXT("imgInitCamera"));
      imgUninitCamera = (procimgUninitCamera) GetProcAddress(imgapiDll, TEXT("imgUninitCamera"));
      imgDisplayImage = (procimgDisplayImage) GetProcAddress(imgapiDll, TEXT("imgDisplayImage"));
      imgStopCapture = (procimgStopCapture) GetProcAddress(imgapiDll, TEXT("imgStopCapture"));
      imgCaptureImage = (procimgCaptureImage) GetProcAddress(imgapiDll, TEXT("imgCaptureImage"));
      imgStartCapture = (procimgStartCapture) GetProcAddress(imgapiDll, TEXT("imgStartCapture"));
      imgSaveJPG = (procimgSaveJPG) GetProcAddress(imgapiDll, TEXT("imgSaveJPG"));
      imgWidth = (procimgWidth) GetProcAddress(imgapiDll, TEXT("imgWidth"));
      imgHeight = (procimgHeight) GetProcAddress(imgapiDll, TEXT("imgHeight"));
      imgBitCount = (procimgBitCount) GetProcAddress(imgapiDll, TEXT("imgBitCount"));
      imgNumColors = (procimgNumColors) GetProcAddress(imgapiDll, TEXT("imgNumColors"));
      imgSetAimer = (procimgSetAimer) GetProcAddress(imgapiDll, TEXT("imgSetAimer"));
      imgSetIllumination = (procimgSetIllumination) GetProcAddress(imgapiDll, TEXT("imgSetIllumination"));
      imgHistogramStretchEx = (procimgHistogramStretchEx) GetProcAddress(imgapiDll, TEXT("imgHistogramStretchEx"));
      imgGammaCorrection = (procimgGammaCorrection) GetProcAddress(imgapiDll, TEXT("imgGammaCorrection"));
   }
   return imgGammaCorrection && imgGetAimer && imgCreateHDIB && imgInitCamera && imgUninitCamera && imgDisplayImage && imgStopCapture && imgCaptureImage && imgStartCapture && imgSaveJPG && imgWidth && imgHeight && imgBitCount && imgNumColors && imgSetAimer && imgSetIllumination && imgHistogramStretchEx && imgGammaCorrection;
}

BOOL initInstance();

SCAN_API TCHAR* showDolphinCamera(TCHAR* path, TCHAR* file, bool lowRes) 
{
   MSG msg;
   saved = false;

   wsprintf(filename,TEXT("%s\\%s"), path ? path : TEXT(""), file ? file : TEXT("image.jpg"));
   gHighRes = !lowRes;
   // Perform application initialization:
   if (!ImagingLibOpen() || !initInstance()) 
      return null;

   // Main message loop:
   while (GetMessage(&msg, NULL, 0, 0)) 
   {
      TranslateMessage(&msg);
      DispatchMessage(&msg);
   }
   UnregisterClass(szWindowClass, g_hInst);

   return saved ? filename : null;
}

//
//  FUNCTION: MyRegisterClass()
//
//  PURPOSE: Registers the window class.
//
//  COMMENTS:
//
ATOM MyRegisterClass(HINSTANCE hInstance, LPTSTR szWindowClass)
{
   WNDCLASS wc;

   wc.style         = CS_HREDRAW | CS_VREDRAW;
   wc.lpfnWndProc   = WndProc;
   wc.cbClsExtra    = 0;
   wc.cbWndExtra    = 0;
   wc.hInstance     = hInstance;
   wc.hIcon         = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_IMAGINGSAMPLE));
   wc.hCursor       = 0;
   wc.hbrBackground = (HBRUSH) GetStockObject(WHITE_BRUSH);
   wc.lpszMenuName  = 0;
   wc.lpszClassName = szWindowClass;

   return RegisterClass(&wc);
}

//
//   FUNCTION: InitInstance(HINSTANCE, int)
//
//   PURPOSE: Saves instance handle and creates main window
//
//   COMMENTS:
//
//        In this function, we save the instance handle in a global variable and
//        create and display the main program window.
//
BOOL initInstance()
{
    HWND hWnd;
    TCHAR szTitle[MAX_LOADSTRING];     // title bar text

    g_hInst = GetModuleHandle(TEXT("Dolphin.dll")); // Store instance handle in our global variable

    LoadString(g_hInst, IDS_APP_TITLE, szTitle, MAX_LOADSTRING); 
    LoadString(g_hInst, IDC_IMAGINGSAMPLE, szWindowClass, MAX_LOADSTRING);

    if (!MyRegisterClass(g_hInst, szWindowClass))
       return FALSE;

    ghWnd = hWnd = CreateWindow(szWindowClass, szTitle, WS_VISIBLE,
        CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, NULL, NULL, g_hInst, NULL);

    if (!hWnd)
        return FALSE;

    ShowWindow(hWnd, SW_SHOW);
    UpdateWindow(hWnd);

    if (g_hWndCommandBar)
       CommandBar_Show(g_hWndCommandBar, TRUE);

    return TRUE;
}

static void play(imgResult s)
{
   PlaySound (s == imgOk ? TEXT("BEEP") : TEXT("BUZZ"), g_hInst, SND_ASYNC);
}

static void updateStatus()
{
   TCHAR szBuffer[256];
   wsprintf (szBuffer, TEXT ("Image Information: \nPixel width:%i\tPixel height:%i\nBits per pixel:%i\tNumber of colors:%i"),imgWidth (g_hDib), imgHeight (g_hDib),imgBitCount (g_hDib), imgNumColors (g_hDib));
   SetDlgItemText(ghWnd,ID_EDIT, szBuffer);
}

//
//  FUNCTION: WndProc(HWND, UINT, WPARAM, LPARAM)
//
//  PURPOSE:  Processes messages for the main window.
//
//  WM_COMMAND - process the application menu
//  WM_PAINT   - Paint the main window
//  WM_DESTROY - post a quit message and return
//
//
LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    int wmId, wmEvent;
    PAINTSTRUCT ps;
    HDC hdc;
    static HWND hwndEdit;

    switch (message) 
    {
        case WM_COMMAND:
        {
            wmId    = LOWORD(wParam); 
            wmEvent = HIWORD(wParam); 
            // Parse the menu selections:
            switch (wmId)
            {        
               case ID_FILE_SAVEIMAGE:
               case ID_FILE_SAVEIMAGEEXIT:
               {
                  int error=0;
                  TCHAR szBuffer[300];
               
                  // Save the image as jpeg
                  if(imgSaveJPG(g_hDib, filename,100)!=imgOk) 
                  {
                     play(imgErrorUnknown);
                     wsprintf(szBuffer, TEXT("ERROR Saving Image: %s"), filename);
                     SetDlgItemText(hWnd,ID_EDIT, szBuffer);
                  }
                  else
                  {
                     play(imgOk);
                     saved = true;
                     wsprintf(szBuffer, TEXT("Image saved to: %s"), filename);
                     SetDlgItemText(hWnd,ID_EDIT, szBuffer);
                  }

                  SetFocus(hWnd);   
                  if (wmId == ID_FILE_SAVEIMAGE)
                     break; // else fall thru
               }
               case IDM_FILE_EXIT:
               {
                    DestroyWindow(hWnd);
                    break;
               }
               case ID_OPTIONS_LOWRES:
               case ID_OPTIONS_HIGHRES:
               {
                  gHighRes = wmId == ID_OPTIONS_HIGHRES;
                  CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),gHighRes ? ID_OPTIONS_HIGHRES : ID_OPTIONS_LOWRES,MF_CHECKED | MF_BYCOMMAND);
                  CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),!gHighRes ? ID_OPTIONS_HIGHRES : ID_OPTIONS_LOWRES,MF_UNCHECKED | MF_BYCOMMAND);
                  break;
               }
               case ID_OPTIONS_ENABLEAIMER:
               {
                  if(CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEAIMER, MF_BYCOMMAND) == MF_CHECKED)
                  {
                     // UnCheck the item
                     CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEAIMER,MF_UNCHECKED | MF_BYCOMMAND);
                     // Set Aimer            
                     play(imgSetAimer(0));
                  }
                  else
                  {
                     // check the items
                     CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEAIMER,MF_CHECKED | MF_BYCOMMAND);
                     // Set Aimer         
                     play(imgSetAimer(1));
                  }
                  break;
               }
               case ID_OPTIONS_ENABLEILLUMINATION:
               {
                  if(CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEILLUMINATION, MF_BYCOMMAND) == MF_CHECKED)
                  {
                     // UnCheck the item
                     CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEILLUMINATION,MF_UNCHECKED | MF_BYCOMMAND);
                     // Set Illumination
                     play(imgSetIllumination(0));
                  }
                  else
                  {
                     // check the items
                     CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEILLUMINATION,MF_CHECKED | MF_BYCOMMAND);
                     // Set Illumination  
                     play(imgSetIllumination(1));
                  }
                  break;
               }
               case ID_OPTIONS_ENABLEHISTOGRAM:
               {
                  /* Dynamically adjust the image contrast by performing a linear histogram stretch. */
                  imgHistogramStretchEx(g_hDib,1,1,1);

                  InvalidateRect(hWnd,0,1);  //force background to be erased
                  UpdateWindow(hWnd);
                  break;
               }
               case ID_OPTIONS_INCGAMMA:
               {
                  /* Corrects the brightness levels in an image. On a computer display, a small change 
                  in brightness at a low brightness level is not equal to the same change at a high 
                  level. Gamma correction compensates for the inequality.  */          
                  imgGammaCorrection(g_hDib,.45);

                  InvalidateRect(hWnd,0,1);  //force background to be erased
                  UpdateWindow(hWnd);

                  break;
               }
               case ID_OPTIONS_DECGAMMA:
               {
                  imgGammaCorrection(g_hDib,1.45);

                  InvalidateRect(hWnd,0,1);  //force background to be erased
                  UpdateWindow(hWnd);

                  break;
               }
               default:
                  return DefWindowProc(hWnd, message, wParam, lParam);
            }
            break;
        }
        case WM_CREATE:
        {
            DWORD dwAimer = 0;               
            DWORD dwIllumination;            
            // Create a label to display to display feedback information
            hwndEdit = CreateWindow( TEXT("static"),NULL,WS_CHILD|WS_VISIBLE,0,240,240,52,hWnd,(HMENU)ID_EDIT, g_hInst, NULL);
            
            //Load a sample profile from DemosMenu.exm
            if (imgInitCamera(NULL, L"Normal", FALSE) != imgOk)
            {
               alert("Error: imgInitCamera()"); 
               return -1;
            }

            hdc = GetDC(hWnd);

            //Create the hdib buffer to hold the image data */
            g_hDib=imgCreateHDIB();
            
            if (!g_hDib)
            {
               alert("Error: imgCreateHDIB()"); 
               DestroyWindow(hWnd);
               return FALSE;
            }
            updateStatus();
            g_hWndCommandBar = CommandBar_Create(g_hInst, hWnd, 1);
            CommandBar_InsertMenubar(g_hWndCommandBar, g_hInst, IDR_MENU, 0);
            CommandBar_AddAdornments(g_hWndCommandBar, 0, 0);

            // Read the imager setting so that we can set the menu items
            // set the aimer menu item if needed
            imgGetAimer(&dwAimer);                       
            if (dwAimer)
               CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEAIMER,MF_CHECKED | MF_BYCOMMAND);
            
            // set the illumination menu item if needed
            imgGetIllumination(&dwIllumination);
            if (dwIllumination)
               CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),ID_OPTIONS_ENABLEILLUMINATION,MF_CHECKED | MF_BYCOMMAND);

            // set the resolution mode
            CheckMenuItem(CommandBar_GetMenu(g_hWndCommandBar,0),gHighRes ? ID_OPTIONS_HIGHRES : ID_OPTIONS_LOWRES,MF_CHECKED | MF_BYCOMMAND);
            break;
        }
        case WM_PAINT:
        {
            RECT rect;
            hdc = BeginPaint(hWnd, &ps);

            // Get Client Rect area
            GetClientRect(hWnd,&rect);

            rect.bottom = rect.right;

            // Display the full frame Data
            imgDisplayImage(g_hDib,hdc,&rect, gStretch?true:false,gZoom,gCenter?true:false);

            EndPaint(hWnd, &ps);
            break;
        }
        case WM_KEYDOWN:
        {  switch(wParam)
          {
            case 0x2A:  /* 0x2A=ON/SCAN */
            {
               bool resshown = false;
               ///* Need to be careful, its possible that key presses have stored up and
               //the ON/SCAN key is not down! */
               if(GetAsyncKeyState(0x2A) > -1) 
                  return true;

                  if (imgStartCapture() != imgOk)
                     break;

                  while(GetAsyncKeyState(0x2A) < 0)
                  {
                     if (imgCaptureImage(g_hDib, gHighRes ? imgFull : imgPreview) != imgOk)
                        break;  // unexpected

                     if (!resshown)
                     {
                        updateStatus();
                        resshown = true;
                     }
                     InvalidateRect(hWnd,0,0);
                     UpdateWindow(hWnd);
                  }

                  // stop capturing to turn off aimer, if on
                  imgStopCapture();
                  
               break;
            }
         }
         break;
      }
      case WM_DESTROY:
      {
         // Un initialize imager before closing
         imgUninitCamera();
         CommandBar_Destroy(g_hWndCommandBar);
         PostQuitMessage(0);
         break;
      }

      default: 
         return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

