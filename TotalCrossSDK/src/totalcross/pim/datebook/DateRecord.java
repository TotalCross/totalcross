/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Gilbert Fridgen                                           *
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
import totalcross.pim.*;
import totalcross.util.*;
/**
 * <code>DateRecord</code> describes the signature, a device specific implementation of a <code>DateRecord</code> has to fulfill.
 * A <code>DateRecord</code> is one appointment. It's attributes are saved in it's <code>DateFields</code>
 * When get*Fields() is called, the data must be read out directly from the device, new *Fields must be created from it and stored in a Vector. This is the place where you need some kind of "upward"-mapping of the datatypes (mapping the native data structure to the generic pimAL *Fields). Now we can understand why get*Records() does not have to read out the complete data from the device but only the ids: As soon as get*Fields() of a single *Record is called, the data will be read out anyway (it must only be found - thats where you need to device id for).
 *
 * @author fridgegi
 *
 */
public interface DateRecord extends VCalRecord
{
   /**
    * Reads <code>DateFields</code> from the device, maps them and stores them in <code>DateField<code>s and returns them.
    * @return a Vector containing the contact information of this record in <code>DateField</code>s
    */
   public Vector getFields();
   /**
    * Reads information from the passed fields, maps them and writes them to the device. Passes not supported fields to a <code>NotSupportedHandler</code>
    * @param fields the fields to write to the device
    */
   public void setFields(Vector fields);
   /**
    * Reads this <code>DateRecord</code>'s note directly.
    * @return the note's text
    */
   public String rawReadNote();
   /**
    * Writes this <code>DateRecord</code>'s note directly
    * @param note the text to write to the note
    */
   public void rawWriteNote(String note);
   /**
    * Registeres a NotSupportedHandler that handle's <code>DateField</code>s this device cannot store
    * @param nsh the NotSupportedHandler to register
    */
   public void registerNotSupportedhandler(DateNotSupportedHandler nsh);
}
