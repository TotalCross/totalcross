// WindowsPhoneRuntimeComponent1.cpp
#include "pch.h"

#define HAS_TCHAR

#include "WindowsPhoneRuntimeComponent1.h"
#include "tcvm.h"

#include "MainViewFactory.h"

using namespace WindowsPhoneRuntimeComponent1;
using namespace Platform;
using namespace TotalCross;

using namespace Windows::ApplicationModel::Core;

int WindowsPhoneRuntimeComponent::executeProgramWrapper(Platform::String^ cmdLine)
{
	//char cmdlineStr[512];

	//memset(cmdlineStr, 0, sizeof(cmdlineStr));
	//WideCharToMultiByte(CP_ACP, 0, cmdLine->Data(), cmdLine->Length(), cmdlineStr, 512, NULL, NULL);
	//cmdlineStr[cmdLine->Length()] = 0;

	CoreApplication::Run(ref new MainViewFactory(cmdLine));

	return 0;// executeProgram(cmdlineStr);
}

WindowsPhoneRuntimeComponent^ WindowsPhoneRuntimeComponent::getNew()
{
	return ref new WindowsPhoneRuntimeComponent();
}