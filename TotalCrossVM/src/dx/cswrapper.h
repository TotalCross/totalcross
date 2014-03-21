#ifndef __CSWRAPPER_H__
#define __CSWRAPPER_H__

#pragma once

namespace PhoneDirect3DXamlAppComponent
{
	public interface class CSwrapper {
	public:
      // Vm
      void privateAlertCS(Platform::String^ alertMsg);
      bool isAlertVisible();
      void vmSetAutoOffCS(bool enable);

      // Dial
      void dialNumberCS(Platform::String^ number);

      // SMS
      void smsSendCS(Platform::String^ message, Platform::String^ destination);

      // RadioDevice
      int getTurnedState();
      void rdGetStateCS(int type);

      // GPS
      bool nativeStartGPSCS();
      int nativeUpdateLocationCS();
      void nativeStopGPSCS();
      double getLatitude();
      double getLongitude();
      double getDirection();
      double getVelocity();
      int getYear();
      int getMonth();
      int getDay();
      int getHour();
      int getMinute();
      int getSecond();
      int getMilliSecond();
      Platform::String^ getMessageReceived();
      double getPdop();
      Platform::String^ getLowSignalReason();

      // File
      bool fileIsCardInsertedCS();

	  // UI
	  double getFontHeightCS();
     void privateWindowSetSIP(bool visible);
     int getSipHeight();
     int getScreenSize();
     void setSip(bool visible);

     // Camera
     void cameraClick();
     int cameraStatus();
     Platform::String^ cameraFilename();

     // Others
     void appExit();
	};
}

#endif