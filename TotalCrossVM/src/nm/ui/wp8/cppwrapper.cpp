#include <chrono>
#include <thread>
#include <queue>
#include <system_error>

#include <wrl/client.h>

#include "esUtil.h"

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "MainView.h"
#include "cppwrapper.h"
#include "tcvm.h"
#include "tcclass.h"

using namespace TotalCross;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;
using namespace Windows::Phone::System::Memory;
using namespace Windows::Phone::Devices::Power;
using namespace Windows::Phone::Devices::Notification;

#pragma region varDeclaration

static char appPathWP8[1024];
static char vmPathWP8[1024];
static char devId[1024];
static DWORD32 privHeight;
static DWORD32 privWidth;
static CoreDispatcher ^dispatcher = nullptr;

static std::queue<eventQueueMember> eventQueue;

#pragma endregion

// Not a concurrent queue
void eventQueuePush(int type, int key, int x, int y, int modifiers)
{
	static int32 *ignoreEventOfType = null;
	struct eventQueueMember newEvent;
	if (ignoreEventOfType == null)
		ignoreEventOfType = getStaticFieldInt(loadClass(mainContext, "totalcross.ui.Window", false), "ignoreEventOfType");
	if (type == *ignoreEventOfType) {
		return;
	}
	newEvent.type = type;
	newEvent.key = key;
	newEvent.x = x;
	newEvent.y = y;
	newEvent.modifiers = modifiers;

	eventQueue.push(newEvent);
}

struct eventQueueMember eventQueuePop(void)
{
	struct eventQueueMember top;

	top = eventQueue.front();
	eventQueue.pop();

	debug("popping event from queue; queue size %d", eventQueue.size());

	return top;
}

int eventQueueEmpty(void)
{
	return (int)eventQueue.empty();
}

char *GetAppPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_appPath = mv->getAppPath();

	WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), appPathWP8, 1024, NULL, NULL);
	return appPathWP8;
}

char *GetVmPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_vmPath = mv->getVmPath();

	WideCharToMultiByte(CP_ACP, 0, _vmPath->Data(), _vmPath->Length(), vmPathWP8, 1024, NULL, NULL);
	return vmPathWP8;
}

void cppthread_detach(void *t)
{
	//std::thread *th = (std::thread*)t;

	//if (th != NULL && th->joinable()) {
	//	th->detach();
	//}
}

char *GetDisplayNameWP8()
{
	Platform::String ^displayName = Windows::Networking::Proximity::PeerFinder::DisplayName;
	WideCharToMultiByte(CP_ACP, 0, displayName->Data(), displayName->Length(), devId, 1024, NULL, NULL);
	return devId;
}

extern "C" DWORD WINAPI privateThreadFunc(VoidP argP);

void* cppthread_create(void *args)
{
	try {
		std::thread t(privateThreadFunc, args);

		t.detach();

		return (void*)*(UINT64*)&t.get_id();
	} catch(std::system_error e) {
		return null;
	}
}

void *cppget_current_thread()
{
	// getting the hash is more reliable then the value of the get_id() return
	return (void*)std::this_thread::get_id().hash();
}

void cppsleep(int ms)
{
	std::this_thread::sleep_for(std::chrono::milliseconds(ms));
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

void windowSetDeviceTitle(Object titleObj)
{

}

void windowSetSIP(enum TCSIP kb)
{
	MainView::GetLastInstance()->setKeyboard(kb);
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
