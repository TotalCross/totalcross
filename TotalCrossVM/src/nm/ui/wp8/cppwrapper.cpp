#include <chrono>
#include <thread>
#include <queue>
#include <system_error>

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
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
	static int32 *ignoreEventOfType = null;
	struct eventQueueMember newEvent;
	if (ignoreEventOfType == null)
		ignoreEventOfType = getStaticFieldInt(loadClass(mainContext, "totalcross.ui.Window", false), "ignoreEventOfType");
	if (type == *ignoreEventOfType) 
		return;
	newEvent.type = type;
	newEvent.key = key;
	newEvent.x = x;
	newEvent.y = y;
	newEvent.modifiers = modifiers;
   newEvent.count = ++counter;
	eventQueue.push(newEvent);
   //debug("%X - %d. push %d", GetCurrentThreadId(), counter, type);
}

struct eventQueueMember eventQueuePop(void)
{
	struct eventQueueMember top;
	top.type = 0;
	if (!eventQueue.empty())
	{
		top = eventQueue.front();
		eventQueue.pop();
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

char *GetDisplayNameWP8()
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

void windowSetSIP(enum TCSIP kb)
{
	//XXX
}

DWORD32 getRemainingBatery()
{
   return Battery::GetDefault()->RemainingChargePercent;
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
   return (DWORD32)MemoryManager::ProcessCommittedLimit;
}

void alertCPP(JCharP jCharStr)
{
   Direct3DBase::getLastInstance()->alertMsg = ref new Platform::String((wchar_t*)jCharStr);
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

bool dxSetup()
{
	return true;//XXX
}

void dxUpdateScreen()
{
   Direct3DBase::getLastInstance()->updateScreen();
}

void dxLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList)
{
   Direct3DBase::getLastInstance()->loadTexture(currentContext, img, textureId, pixels, width, height, updateList);
}

void dxDeleteTexture(TCObject img, int32* textureId, bool updateList)
{
   Direct3DBase::getLastInstance()->deleteTexture(img, textureId, updateList);
}

void dxDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv* color, int32* clip)
{
   Direct3DBase::getLastInstance()->drawTexture(textureId, x, y, w, h, dstX, dstY, imgW, imgH, color, clip);
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

void dxDrawPixels(int *x, int *y, int count, int color)
{
	Direct3DBase::getLastInstance()->drawPixels(x, y, count, color);
}

double getFontHeightCPP()
{
	return Direct3DBase::getLastInstance()->csharp->getFontHeightCS();
}
