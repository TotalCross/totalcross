#include <chrono>
#include <thread>
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

using namespace TotalCross;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

#pragma region varDeclaration

static char apPath[1024];
static DWORD32 privHeight;
static DWORD32 privWidth;
static CoreDispatcher ^dispatcher = nullptr;

#pragma endregion

char *GetAppPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_appPath = mv->getAppPath();

	WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), apPath, 1024, NULL, NULL);
	return apPath;
}

void cppthread_detach(void *t)
{
	//std::thread *th = (std::thread*)t;

	//if (th != NULL && th->joinable()) {
	//	th->detach();
	//}
}

void* cppthread_create(void (*func)(void *a), void *args)
{
	try {
		std::thread t(func, args);

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

void setKeyboard(int state)
{
	MainView::GetLastInstance()->setKeyboard(state);
}
