#include <stdio.h>

#include "Direct3DBase.h"
#include "MainView.h"
#include "cppwrapper.h"


using namespace TotalCross;

static char apPath[1024];

char *GetAppPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_appPath = mv->getAppPath();

	WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), apPath, 1024, NULL, NULL);
	return apPath;
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
