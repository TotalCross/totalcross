#include  "cppwrapper.h"
#include <stdio.h>
#include "MainView.h"

using namespace TotalCross;

static char apPath[1024];

char *GetAppPathWP8()
{
	MainView ^mv = MainView::GetLastInstance();
	Platform::String ^_appPath = mv->getAppPath();

	WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), apPath, 1024, NULL, NULL);
	return apPath;
}