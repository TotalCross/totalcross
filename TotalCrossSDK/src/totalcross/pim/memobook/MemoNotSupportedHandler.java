/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Kathrin Braunwarth                                        *
 *  Copyright (C) 2003-2011 SuperWaba Ltda.                                      *
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

// $Id: MemoNotSupportedHandler.java,v 1.7 2011-01-04 13:19:26 guich Exp $

package totalcross.pim.memobook;
import totalcross.util.*;
/**
 * Interface used to define classes that can handle not supported fields.
 * @author braunwka
 */
public interface MemoNotSupportedHandler
{
   /**
    * Handles not supported fields.
    * @param notSupported the not supported fields
    * @param mr the respective <code>MemoRecord</code>
    */
   public void write(Vector notSupported, MemoRecord mr);
   /**
    * Collects the not supported fields that have been handled and adds them into the Vector of supported fields (already found)
    * @param mr the respective <code>MemoRecord</code>
    * @param alreadyFound Vector of supported fields that already have been collected
    * @return Vector of all fields (supported and not supported) of this <code>MemoRecord</code>
    */
   public Vector complete(MemoRecord mr, Vector alreadyFound);
}
