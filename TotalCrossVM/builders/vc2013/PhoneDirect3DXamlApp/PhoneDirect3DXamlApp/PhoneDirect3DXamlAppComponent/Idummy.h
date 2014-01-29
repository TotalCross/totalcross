#ifndef __IDUMMY_H__
#define __IDUMMY_H__

#pragma once

namespace PhoneDirect3DXamlAppComponent
{

	public interface class Idummy {
	public:
		void callDraw();
      void privateAlertCS(Platform::String^ alertMsg);
      void vmSetAutoOffCS(bool enable);
      void dialNumberCS(Platform::String^ number);
      void smsSendCS(Platform::String^ message, Platform::String^ destination);
	};
}

#endif