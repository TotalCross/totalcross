#include <chrono>
#include <thread>
#include <queue>
#include <system_error>

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "MainView.h"
#include "cppwrapper.h"
#include "tcvm.h"
#include <wrl/client.h>
#include "tcclass.h"
#include "Direct3DBase.h"

using namespace TotalCross;
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
static CoreDispatcher ^dispatcher = nullptr;

static std::queue<eventQueueMember> eventQueue;

#pragma endregion

enum qStEn {
	QUEUE_PUSH,
	QUEUE_POP,
	QUEUE_IDLE
};

static enum qStEn queue_state = QUEUE_IDLE;

// Not a concurrent queue
void eventQueuePush(int type, int key, int x, int y, int modifiers)
{
	int ini, fim;

	static int32 *ignoreEventOfType = null;
	struct eventQueueMember newEvent;
	if (ignoreEventOfType == null)
		ignoreEventOfType = getStaticFieldInt(loadClass(mainContext, "totalcross.ui.Window", false), "ignoreEventOfType");
	if (type == *ignoreEventOfType) {
		return;
	}
	ini = GetTickCount64() & 0x3FFFFFFF;

	newEvent.type = type;
	newEvent.key = key;
	newEvent.x = x;
	newEvent.y = y;
	newEvent.modifiers = modifiers;

	/*if (eventQueue.size() < 3) {
		while (queue_state == QUEUE_POP) {
			Sleep(1);
		}
		queue_state = QUEUE_PUSH;
		eventQueue.push(newEvent);
		queue_state = QUEUE_IDLE;
	}
	else {*/
		eventQueue.push(newEvent);
	//}

	fim = GetTickCount64() & 0x3FFFFFFF;
	debug("elapsed %d, pushing event from queue; queue size %d", fim - ini, eventQueue.size());
}

struct eventQueueMember eventQueuePop(void)
{
	int ini, fim;
	struct eventQueueMember top;

	ini = GetTickCount64() & 0x3FFFFFFF;

	top.type = 0;
	/*if (eventQueue.size() < 3) {
		while (queue_state == QUEUE_PUSH) {
			Sleep(1);
		}
		queue_state = QUEUE_POP;
		top = eventQueue.front();
		eventQueue.pop();
		queue_state = QUEUE_IDLE;
	}
	else {*/
	if (!eventQueue.empty())
	{
		top = eventQueue.front();
		eventQueue.pop();
	}

	fim = GetTickCount64() & 0x3FFFFFFF;
	debug("elapsed %d, popping event from queue; queue size %d", fim - ini, eventQueue.size());

	return top;
}

int eventQueueEmpty(void)
{
	int ret = (int)eventQueue.empty();
	return ret;
}

char *GetAppPathWP8()
{
	if (appPathWP8[0] == '\0') {
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

void set_dispatcher()
{
	CoreWindow::GetForCurrentThread()->Activate();
	dispatcher = CoreWindow::GetForCurrentThread()->Dispatcher;
}

void dispatcher_dispath()
{
	if (dispatcher != nullptr)
		dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
}

void windowSetDeviceTitle(TCObject titleObj)
{

}

void windowSetSIP(enum TCSIP kb)
{
	MainView::GetLastInstance()->setKeyboard(kb); //XXX
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
   Direct3DBase::GetLastInstance()->getCSwrapper()->privateAlertCS(ref new Platform::String((wchar_t*)jCharStr));
}

void vmSetAutoOffCPP(bool enable)
{
//XXX   Direct3DBase::GetLastInstance()->getCSwrapper()->vmSetAutoOffCS(enable);
}

void dialNumberCPP(JCharP number)
{
   Direct3DBase::GetLastInstance()->getCSwrapper()->dialNumberCS(ref new Platform::String((wchar_t*)number));
}

void smsSendCPP(JCharP szMessage, JCharP szDestination)
{
   Direct3DBase::GetLastInstance()->getCSwrapper()->smsSendCS(ref new Platform::String((wchar_t*)szMessage), ref new Platform::String((wchar_t*)szDestination));
}

int rdGetStateCPP(int type)
{
   PhoneDirect3DXamlAppComponent::CSwrapper^ cs = Direct3DBase::GetLastInstance()->getCSwrapper();
   cs->rdGetStateCS(type);
   return cs->getTurnedState();
}

bool isAvailableCPP(int type)
{
   return rdGetStateCPP(type - 2);
}

bool nativeStartGPSCPP()
{
   return Direct3DBase::GetLastInstance()->getCSwrapper()->nativeStartGPSCS();
}

int nativeUpdateLocationCPP(Context context, TCObject gpsObject)
{
   PhoneDirect3DXamlAppComponent::CSwrapper^ cs = Direct3DBase::GetLastInstance()->getCSwrapper();
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
   Direct3DBase::GetLastInstance()->getCSwrapper()->nativeStopGPSCS();
}

bool dxSetup()
{
	return true;//XXX
}

void dxUpdateScreen()
{
   Direct3DBase::GetLastInstance()->Present();
}

void dxLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList)
{
   Direct3DBase::GetLastInstance()->loadTexture(currentContext, img, textureId, pixels, width, height, updateList);
}
void dxDeleteTexture(TCObject img, int32* textureId, bool updateList)
{
   Direct3DBase::GetLastInstance()->deleteTexture(img, textureId, updateList);
}
void dxDrawTexture(int32 textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH)
{
   Direct3DBase::GetLastInstance()->drawTexture(textureId, x, y, w, h, dstX, dstY, imgW, imgH);
}

void dxDrawLine(int x1, int y1, int x2, int y2, int color)
{
	Direct3DBase::GetLastInstance()->drawLine(x1, y1, x2, y2, color);
}

void dxFillRect(int x1, int y1, int x2, int y2, int color)
{
	Direct3DBase::GetLastInstance()->fillRect(x1, y1, x2, y2, color);
}

void dxDrawPixels(int *x, int *y, int count, int color)
{
	Direct3DBase::GetLastInstance()->drawPixels(x, y, count, color);
}

double getFontHeightCPP()
{
	return Direct3DBase::GetLastInstance()->getCSwrapper()->getFontHeightCS();
}