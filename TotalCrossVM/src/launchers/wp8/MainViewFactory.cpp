/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include <combaseapi.h>
#include "MainViewFactory.h"
#include "MainVieW.h"

using namespace Windows::ApplicationModel;
using namespace Windows::ApplicationModel::Core;
using namespace Windows::ApplicationModel::Activation;
using namespace Windows::UI::Core;
using namespace Windows::Foundation;
using namespace Windows::Graphics::Display;
using namespace TotalCross;

IFrameworkView^ MainViewFactory::CreateView()
{
	// Essa linha daqui de baixo deverá ser apagada; ela deve ser chamada por Settings
	OutputDebugString(Windows::Storage::ApplicationData::Current->LocalFolder->Path->Data());

	//char lpCmdLine[512];
	//memset(cmdline, 0, sizeof(cmdline));
	//getTCZName(cmdline);

	//char cmdline[512] = " / cmd ";
	//WideCharToMultiByte(CP_ACP, 0, cmdLine->Data(), cmdLine->Length(), cmdline + 7, 512 - 7, NULL, NULL);

	//executeProgram(cmdline);
	return ref new MainView(cmdLine);
}

MainViewFactory::MainViewFactory(String^ cmdLine)
{
	this->cmdLine = cmdLine;
}