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



package ras;

import totalcross.sys.Convert;
import totalcross.xml.soap.SOAP;

public abstract class ActivationClient
{
  public static final String defaultServerURI = "http://www.superwaba.net/ActivationServer/services/ActivationService";
  public static final int version = 4;

  private static ActivationClient instance;
  static boolean litebaseAllowed; // read by the vm
  static boolean activateOnJDK_DEBUG = false;

  public abstract String getProductId();
  public abstract String getActivationCode();
  public abstract String getPlatform();
  public abstract String getDeviceId();
  public abstract String getDeviceHash();
  public abstract boolean isLitebaseAllowed();
  public abstract boolean isActivated() throws ActivationException;
  public abstract boolean isActivatedSilent();
  public abstract boolean isValidKey(String key);
  public abstract void activate() throws ActivationException;

  public static ActivationClient getInstance()
  {
    /* ENABLE THE LINES BELOW TO TEST THE ACTIVATION ON JDK */
    //      activateOnJDK_DEBUG = true;
    //      if (instance == null)
    //         instance = new ActivationClientImpl();
    return instance;
  }
  public static ActivationClient getInstance4D() {
    if (instance == null) {
      try {
        instance = (ActivationClient) Class.forName("ras.ActivationClientImpl").newInstance();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }

  public static byte[] activate(byte[] request) throws ActivationException
  {
    SOAP soap = new SOAP("toolActivation", defaultServerURI);
    soap.openTimeout = 30000;
    soap.readTimeout = soap.writeTimeout = 20000;

    try
    {
      soap.setParam(version, "version");
      soap.setParam(request, "request");
      soap.execute();

      String response = (String) soap.getAnswer();
      return Convert.hexStringToBytes(response);         
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw new ActivationException("Cannot send packet", ex);
    }
  }   
}
