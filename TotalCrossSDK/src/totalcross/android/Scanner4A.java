/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.android;

import totalcross.*;

/**
 * Scanner class for Android.
 *
 */
public class Scanner4A
{
   static boolean scannerActivate()
   {
      AndroidUtils.debug("scannerActivate");
      return true;
   }
   
   static boolean setBarcodeParam(int barcodeType, boolean enable)
   {
      AndroidUtils.debug("setBarcodeParam");
      return true;
   }
   
   static boolean setParam(int type, int barcodeType, int value)
   {
      AndroidUtils.debug("setParam");
      return true;
   }
   
   static boolean commitBarcodeParams()
   {
      AndroidUtils.debug("commitBarcodeParams");
      return true;
   }
   
   static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max)
   {
      AndroidUtils.debug("setBarcodeLength");
      return true;
   }
   
   static String getData()
   {
      AndroidUtils.debug("getData");
      return "";
   }
   
   static String getScanManagerVersion()
   {
      AndroidUtils.debug("getScanManagerVersion");
      return "";
   }
   
   static String getScanPortDriverVersion()
   {
      AndroidUtils.debug("getScanPortDriverVersion");
      return "";
   }
   
   static boolean deactivate()
   {
      AndroidUtils.debug("deactivate");
      return true;
   }
}