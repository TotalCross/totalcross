#ifndef __IDUMMY_H__
#define __IDUMMY_H__

#pragma once

namespace PhoneDirect3DXamlAppComponent
{

	public interface class Idummy {
	public:
		void callDraw();
		void alert(Platform::String ^alertMsg);
	};
}

#endif