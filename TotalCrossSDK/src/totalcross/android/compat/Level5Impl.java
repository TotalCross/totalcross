/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

package totalcross.android.compat;

import totalcross.*;

import java.io.*;
import java.util.*;

import android.bluetooth.*;
import android.content.*;
import android.hardware.Camera.*;
import android.os.*;

public class Level5Impl extends Level5
{
   public Level5Impl()
   {
   }

   ///////////////////  CAMERA METHODS ////////////////////
   public void setPictureParameters(Parameters parameters, int stillQuality, int ww, int hh)
   {
      parameters.setPreviewSize(ww,hh);
      parameters.setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100);
   }
   public List<Size> getSupportedPictureSizes(Parameters parameters)
   {
      return parameters.getSupportedPictureSizes();
   }
}
