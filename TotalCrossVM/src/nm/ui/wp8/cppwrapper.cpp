#include <chrono>
#include <thread>
#include <queue>
#include <system_error>

#if (_MSC_VER >= 1800)
#include "C:\Program Files (x86)\Windows Phone Kits\8.1\Include\D3D11_2.h" //<d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "cppwrapper.h"
#include "tcvm.h"
#include <wrl/client.h>
#include "tcclass.h"
#include "Direct3DBase.h"

using namespace Windows::Foundation;
using namespace Windows::UI::Core;
using namespace Windows::Phone::System::Memory;
using namespace Windows::Phone::Devices::Power;
using namespace Windows::Phone::Devices::Notification;

#pragma region varDeclaration

static char appPathWP8[1024] = "";
static char vmPathWP8[1024] = "";
static char devId[1024];
static DWORD32 privHeight;
static DWORD32 privWidth;

static std::queue<eventQueueMember> eventQueue;

#pragma endregion

static int counter;
// Not a concurrent queue
void eventQueuePush(int type, int key, int x, int y, int modifiers)
{
	struct eventQueueMember newEvent;
	newEvent.type = type;
	newEvent.key = key;
	newEvent.x = x;
	newEvent.y = y;
	newEvent.modifiers = modifiers;
   newEvent.count = ++counter;
	eventQueue.push(newEvent);
}

extern "C"
{
   void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, bool nothingChanged);
   extern int appW, appH;
}

struct eventQueueMember eventQueuePop(void)
{
	struct eventQueueMember top;
	top.type = 0;
	if (!eventQueue.empty())
	{
		top = eventQueue.front();
		eventQueue.pop();
      if (top.type == KEYEVENT_SPECIALKEY_PRESS && top.key == -1030)
      {
         screenChange(mainContext, appW, appH, 0, 0, true);
         top.type = 0;
      }
	}
	return top;
}

int eventQueueEmpty(void)
{
	int ret = (int)eventQueue.empty();
   Sleep(1);
	return ret;
}

char *GetAppPathWP8()
{
	if (appPathWP8[0] == '\0') 
   {
		Platform::String ^_appPath = Windows::Storage::ApplicationData::Current->LocalFolder->Path;
		WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), appPathWP8, 1024, NULL, NULL);
	}
	return appPathWP8;
}

char *GetVmPathWP8()
{
	if (vmPathWP8[0] == '\0') {
		Platform::String ^_vmPath = Windows::ApplicationModel::Package::Current->InstalledLocation->Path;

		WideCharToMultiByte(CP_ACP, 0, _vmPath->Data(), _vmPath->Length(), vmPathWP8, 1024, NULL, NULL);
	}
	return vmPathWP8;
}

char *GetDisplayNameWP8() // no longer used
{
	if (devId[0] == '\0') {
	   Platform::String ^displayName = Windows::Networking::Proximity::PeerFinder::DisplayName;
	   WideCharToMultiByte(CP_ACP, 0, displayName->Data(), displayName->Length(), devId, 1024, NULL, NULL);
	}
   return devId;
}

void windowSetDeviceTitle(TCObject titleObj)
{
}

bool windowGetSIP()
{                 
   return false;
}
void windowSetSIP(enum TCSIP kb)
{
	//XXX
}

DWORD32 getRemainingBatery()
{
   return Battery::GetDefault()->RemainingChargePercent;
}

int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   Platform::String^ comm = ref new Platform::String((wchar_t*)szCommand);
   Platform::String^ args = ref new Platform::String((wchar_t*)szArgs);
   if (comm->Equals("url") || comm->Equals("viewer"))
      Windows::System::Launcher::LaunchUriAsync(ref new Windows::Foundation::Uri(args));
   return 1;
}

void vibrate(DWORD32 milliseconds)
{
   VibrationDevice^ vib = VibrationDevice::GetDefault();
   if (vib != nullptr)
   {
      TimeSpan time;
      time.Duration = min(milliseconds * 10000, 50000000); // The time unit is 100ns and the limit is 5 s. More than that, boom!
      vib->Vibrate(time);
   }
}

DWORD32 getFreeMemoryWP8()
{
   long long ret2 = Direct3DBase::getLastInstance()->csharp->getFreeMemory();
   return (DWORD32)ret2;
}

DWORD32 getUsedMemoryWP8()
{
   long long ret2 = Direct3DBase::getLastInstance()->csharp->getUsedMemory();
   return (DWORD32)ret2;
}

void alertCPP(JCharP jCharStr)
{
   Direct3DBase::getLastInstance()->csharp->privateAlertCS(ref new Platform::String((wchar_t*)jCharStr), eventsInitialized);
   while (Direct3DBase::getLastInstance()->csharp->isAlertVisible()) Sleep(10);
}

void vmSetAutoOffCPP(bool enable)
{
   Direct3DBase::getLastInstance()->csharp->vmSetAutoOffCS(enable);
}

void dialNumberCPP(JCharP number)
{
   Direct3DBase::getLastInstance()->csharp->dialNumberCS(ref new Platform::String((wchar_t*)number));
}

void smsSendCPP(JCharP szMessage, JCharP szDestination)
{
   Direct3DBase::getLastInstance()->csharp->smsSendCS(ref new Platform::String((wchar_t*)szMessage), ref new Platform::String((wchar_t*)szDestination));
}

int rdGetStateCPP(int type)
{
   PhoneDirect3DXamlAppComponent::CSwrapper^ cs = Direct3DBase::getLastInstance()->csharp;
   cs->rdGetStateCS(type);
   return cs->getTurnedState();
}

bool isAvailableCPP(int type)
{
   return rdGetStateCPP(type - 2) ? true : false;
}

bool nativeStartGPSCPP()
{
   return Direct3DBase::getLastInstance()->csharp->nativeStartGPSCS();
}

int nativeUpdateLocationCPP(Context context, TCObject gpsObject)
{
   PhoneDirect3DXamlAppComponent::CSwrapper^ cs = Direct3DBase::getLastInstance()->csharp;
   int ret = cs->nativeUpdateLocationCS();
   TCObject lastFix = GPS_lastFix(gpsObject);
   Platform::String^ messageReceived;
   Platform::String^ lowSignalReason;

   GPS_latitude(gpsObject) = cs->getLatitude();
   
   GPS_longitude(gpsObject) = cs->getLongitude();
   
   GPS_direction(gpsObject) = cs->getDirection();
   
   GPS_velocity(gpsObject) = cs->getVelocity();
   
   Time_year(lastFix) = cs->getYear();
   Time_month(lastFix) = cs->getMonth();
   Time_day(lastFix) = cs->getDay();
   Time_hour(lastFix) = cs->getHour();
   Time_minute(lastFix) = cs->getMinute();
   Time_second(lastFix) = cs->getSecond();
   Time_millis(lastFix) = cs->getMilliSecond();

   messageReceived = cs->getMessageReceived();
   setObjectLock(GPS_messageReceived(gpsObject) = createStringObjectFromJCharP(context, (JCharP)messageReceived->Data(), messageReceived->Length()), UNLOCKED);

   lowSignalReason = cs->getLowSignalReason();
   setObjectLock(GPS_lowSignalReason(gpsObject) = createStringObjectFromJCharP(context, (JCharP)lowSignalReason->Data(), lowSignalReason->Length()), UNLOCKED);

   GPS_pdop(gpsObject) = cs->getPdop();

   return ret;
}

void nativeStopGPSCPP()
{
   Direct3DBase::getLastInstance()->csharp->nativeStopGPSCS();
}

bool fileIsCardInsertedCPP()
{
   return Direct3DBase::getLastInstance()->csharp->fileIsCardInsertedCS();
}

void dxUpdateScreen()
{
   Direct3DBase::getLastInstance()->updateScreen();
}

void dxGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch)
{
   Direct3DBase::getLastInstance()->getPixels(dstPixels,srcX,srcY,width,height,pitch);
}

int32 dxGetSipHeight()
{
   return Direct3DBase::getLastInstance()->csharp->getSipHeight();
}

int32 dxGetScreenSize()
{
   return Direct3DBase::getLastInstance()->csharp->getScreenSize();
}

bool dxLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool onlyAlpha)
{
   return Direct3DBase::getLastInstance()->loadTexture(currentContext, img, textureId, pixels, width, height, onlyAlpha);
}

void dxDeleteTexture(TCObject img, int32* textureId)
{
   Direct3DBase::getLastInstance()->deleteTexture(img, textureId);
}

void dxDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask)
{
   Direct3DBase::getLastInstance()->drawTexture(textureId, x, y, w, h, dstX, dstY, dstW, dstH, imgW, imgH, color, alphaMask);
}

void dxDrawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, int color, bool fill)
{
   Direct3DBase::getLastInstance()->drawLines(currentContext, g, x, y, n, tx, ty, color, fill);
}

void dxDrawLine(int x1, int y1, int x2, int y2, int color)
{
	Direct3DBase::getLastInstance()->drawLine(x1, y1, x2, y2, color);
}

void dxFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
	Direct3DBase::getLastInstance()->fillShadedRect(g,x,y,w,h,c1,c2,horiz);
}

void dxFillRect(int32 x, int32 y, int32 w, int32 h, int color)
{
   Direct3DBase::getLastInstance()->fillRect(x, y, w, h, color);
}

void dxDrawPixelColors(int32* x, int32* y, PixelConv* colors, int32 n)
{
   Direct3DBase::getLastInstance()->drawPixelColors(x, y, colors, n);
}

void dxDrawPixels(float *glXYA, int count, int color)
{
	Direct3DBase::getLastInstance()->drawPixels(glXYA, count, color);
}

double getFontHeightCPP()
{
	return Direct3DBase::getLastInstance()->csharp->getFontHeightCS();
}

static bool isShown;

void privateWindowSetSIP(bool visible)
{                   
   isShown = visible;
   Direct3DBase::getLastInstance()->csharp->privateWindowSetSIP(visible);
}

bool privateWindowGetSIP()
{
   return dxGetSipHeight() != 0;
}

void dxprivateScreenChange()
{
   Direct3DBase::getLastInstance()->updateScreenMatrix();
}

void cameraClick(NMParams p)
{
   Direct3DBase::getLastInstance()->csharp->cameraClick();
   int status;
   while ((status = Direct3DBase::getLastInstance()->csharp->cameraStatus()) == -1)
      Sleep(100);
   if (status == 0) // cancelled?
      p->retO = null;
   else
   {
      Platform::String ^name = Direct3DBase::getLastInstance()->csharp->cameraFilename();
      CharP ret = (CharP)xmalloc(name->Length() + 1);
      WideCharToMultiByte(CP_ACP, 0, name->Data(), name->Length(), ret, name->Length() + 1, NULL, NULL);
      setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, ret, name->Length()), UNLOCKED);
      xfree(ret);
   }
}

void appExit()
{
   Direct3DBase::getLastInstance()->csharp->appExit();
}

void appSetFullScreen()
{
   Direct3DBase::getLastInstance()->csharp->appSetFullScreen();
}

void nativeSoundPlayCPP(char* filename)
{
   std::string s_str = std::string(filename);
   std::wstring wid_str = std::wstring(s_str.begin(), s_str.end());
   const wchar_t* w_char = wid_str.c_str();
   Platform::String^ p_string = ref new Platform::String(w_char);
   Direct3DBase::getLastInstance()->csharp->nativeSoundPlayCS(p_string);
}

bool isVirtualKeyboard()
{
   return Direct3DBase::getLastInstance()->csharp->isVirtualKeyboard();
}
double getDpiX()
{
   return Direct3DBase::getLastInstance()->csharp->getDpiX();
}
double getDpiY()
{
   return Direct3DBase::getLastInstance()->csharp->getDpiY();
}
int getOSVersion()
{
   return Direct3DBase::getLastInstance()->csharp->getOSVersion();
}
void getDeviceIdCPP(CharP ret)
{
   Platform::String ^name = Direct3DBase::getLastInstance()->csharp->getDeviceId();
   WideCharToMultiByte(CP_ACP, 0, name->Data(), name->Length(), ret, name->Length() + 1, NULL, NULL);
}
void getRomSerialNumberCPP(CharP ret)
{
   Platform::String ^name = Direct3DBase::getLastInstance()->csharp->getSerialNumber();
   WideCharToMultiByte(CP_ACP, 0, name->Data(), name->Length(), ret, name->Length() + 1, NULL, NULL);
}

bool showMap(JCharP originStr, int32 originLen, JCharP destinationStr, int32 destinationLen)
{
   return Direct3DBase::getLastInstance()->csharp->showMap(ref new Platform::String((wchar_t*)originStr, originLen), ref new Platform::String((wchar_t*)destinationStr, destinationLen));
}

