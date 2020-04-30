// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <wrl/client.h>

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "MainView.h"
#include <combaseapi.h>

#include "MainViewFactory.h"

using namespace Platform;
using namespace Windows::ApplicationModel;
using namespace Windows::ApplicationModel::Core;
using namespace Windows::ApplicationModel::Activation;
using namespace Windows::UI::Core;
using namespace Windows::Foundation;
using namespace Windows::Graphics::Display;
using namespace TotalCross;

IFrameworkView^ MainViewFactory::CreateView()
{
	String ^vmPath = Windows::ApplicationModel::Package::Current->InstalledLocation->Path;
	String ^appPath = Windows::Storage::ApplicationData::Current->LocalFolder->Path;
	// Essa linha daqui de baixo dever� ser apagada; ela deve ser chamada por Settings
	//OutputDebugString(Windows::Storage::ApplicationData::Current->LocalFolder->Path->Data());

	//char lpCmdLine[512];
	//memset(cmdline, 0, sizeof(cmdline));
	//getTCZName(cmdline);

	//char cmdline[512] = " / cmd ";
	//WideCharToMultiByte(CP_ACP, 0, cmdLine->Data(), cmdLine->Length(), cmdline + 7, 512 - 7, NULL, NULL);

	//executeProgram(cmdline);
	return ref new MainView(cmdLine, vmPath, appPath);
}

MainViewFactory::MainViewFactory(String^ cmdLine)
{
	this->cmdLine = cmdLine;
}