#ifndef __CSWRAPPER_H__
#define __CSWRAPPER_H__

#pragma once

namespace PhoneDirect3DXamlAppComponent
{
	public interface class CSwrapper {
	public:
		Platform::String^ getAppName();

      // Vm
      void privateAlertCS(Platform::String^ alertMsg, bool eventsInitialized);
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
     INT privateWindowGetSIP();
     int getSipHeight();
     int getScreenSize();
     void setSip(bool visible);

     // Camera
     void cameraClick();
     int cameraStatus();
     Platform::String^ cameraFilename();

     // Media
     void nativeSoundPlayCS(Platform::String^ filename);

     // Others
     void appExit();
     void appSetFullScreen();

     // Settings
     long long getFreeMemory();
     long long getUsedMemory();
     Platform::String^ getDeviceId();
     bool isVirtualKeyboard();
     double getDpiX();
     double getDpiY();
     Platform::String^ getSerialNumber();
     int getOSVersion();

     // Map
     bool showMap(Platform::String^ origin, Platform::String^ destination);
   };
}

#endif