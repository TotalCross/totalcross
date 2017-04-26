#pragma once

namespace PhoneXamlDirect3DApp1Comp
{
	public ref class WindowsPhoneRuntimeComponent sealed
	{
	public:
		static int executeProgramWrapper(Platform::String^ cmdLine);
		WindowsPhoneRuntimeComponent^ getNew();
	};
}