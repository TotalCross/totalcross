/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Kathrin Braunwarth                                        *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.datebook;
import totalcross.util.*;
/**
 * Interface used to define classes that can handle not supported fields.
 * @author braunwka
 */
public interface DateNotSupportedHandler
{
   /**
    * Handles not supported fields.
    * @param notSupported the not supported fields
    * @param dr the respective <code>DateRecord</code>
    */
   public void write(Vector notSupported, DateRecord dr);
   /**
    * Collects the not supported fields that have been handled and adds them into the Vector of supported fields (already found)
    * @param dr the respective <code>DateRecord</code>
    * @param alreadyFound Vector of supported fields that already have been collected
    * @return Vector of all fields (supported and not supported) of this <code>DateRecord</code>
    */
   public Vector complete(DateRecord dr, Vector alreadyFound);
}
