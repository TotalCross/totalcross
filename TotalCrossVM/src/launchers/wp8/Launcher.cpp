// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <combaseapi.h>
//#include "MainViewFactory.h"

using namespace Windows::ApplicationModel::Core;
//using namespace TotalCross;

[Platform::MTAThread]
int main(Platform::Array<Platform::String^>^ argsArray)
{
	//CoreApplication::Run(ref new MainViewFactory(argsArray[0]));

   //CoInitializeEx(NULL, COINIT_MULTITHREADED);

	auto x = LoadPackagedLibrary(TEXT("PhoneClassLibrary1.dll"), 0);

	PhoneXamlDirect3DApp1Comp::WindowsPhoneRuntimeComponent::executeProgramWrapper(argsArray[0]);
   return 0;
}