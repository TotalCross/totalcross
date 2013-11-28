#include "Direct3DBase.h"
#include "MainView.h"
#include "cppwrapper.h"
#include "tcvm.h"


#include <chrono>
#include <thread>
#include <system_error>

using namespace TotalCross;

static char apPath[1024];

char *GetAppPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_appPath = mv->getAppPath();

	WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), apPath, 1024, NULL, NULL);
	return apPath;
}

void cppthread_detach(void *t)
{
	std::thread *th = (std::thread*)t;

	if (th->joinable()) {
		th->detach();
	}
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
	// get the id (64 bit var), then get its address, transform to a uint64 pointer, derreference it and then cast to voidP
	return (void*)*(UINT64*)&std::this_thread::get_id();
}

void cppsleep(int ms)
{
	std::this_thread::sleep_for(std::chrono::milliseconds(ms));
}

void GetWidthAndHeight(DWORD32* width, DWORD32* height)
{
   Windows::UI::Core::CoreWindow^ window = MainView::GetLastInstance()->GetWindow();
   *width = window->Bounds.Width;
   *height = window->Bounds.Height;
}

void SetupDX(void)
{
   MainView^ mainView = MainView::GetLastInstance();
   if (!mainView->getDirect3DBase())
      mainView->setDirect3DBase(ref new Direct3DBase(mainView->GetWindow()));
}

void ReleaseDX(void)
{
   MainView::GetLastInstance()->getDirect3DBase()->ReleaseDX();
}
