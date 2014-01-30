#pragma once

namespace WindowsPhoneRuntimeComponent1
{
	public ref class WindowsPhoneRuntimeComponent sealed
	{
	public:
		static int executeProgramWrapper(Platform::String^ cmdLine);
		WindowsPhoneRuntimeComponent^ getNew();
	};
}