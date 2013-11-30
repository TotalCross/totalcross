#include <chrono>
#include <thread>
#include <system_error>

//#include "Direct3DBase.h"
#include "CubeRenderer.h"
#include "MainView.h"
#include "cppwrapper.h"
#include "tcvm.h"

using namespace TotalCross;
using namespace Windows::Foundation;

static char apPath[1024];
DWORD32 privHeight;
DWORD32 privWidth;

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

void SetBounds()
{
   MainView::GetLastInstance()->setBounds();
}

void GetWidthAndHeight(DWORD32* width, DWORD32* height)
{
   Rect bounds = MainView::GetLastInstance()->getBounds();
   *width = bounds.Width;
   *height = bounds.Height;
}

/*void InitDX(void)
{
   MainView^ mainView = MainView::GetLastInstance();
   if (!mainView->getDirect3DBase())
      mainView->setDirect3DBase(ref new Direct3DBase(mainView->GetWindow()));
}

void DisplayDX(void)
{
   Direct3DBase^ direct3DBase = MainView::GetLastInstance()->getDirect3DBase();
   if (direct3DBase)
   {
      direct3DBase->Render();
      direct3DBase->Present();
   }
}

void ReleaseDX(void)
{
   MainView::GetLastInstance()->getDirect3DBase()->ReleaseDX();
}*/
