/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Fabian Kroeher                                            *
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



package totalcross.pim.ce.builtin.pimal;
import totalcross.pim.ce.builtin.*;
import totalcross.pim.addressbook.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * Implements the <code>pimal.addressbook.AddressRecord</code> interface for PocketPC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCAddressRecord extends PocketPCRecord implements AddressRecord
{
   private AddressNotSupportedHandler exceptionalFieldHandler;
   /**
    * creates a new PocketPCAddressRecord from the given source; the only field needed from
    * the source is the id!
    * @param source the source from which this instance shall be created
    */
   public PocketPCAddressRecord(IContact source)
   {
      super(source);
   }
   /* this method updates the fields of the iContact from the PPC device and
   * translates it into the appropriate vCard-Fields; everytime it is called,
   * the data is refreshed from the PPC device!
   * @see totalcross.pim.addressbook.AddressRecord#getAddressFields()
   * @return Vector the Vector with the requested AddressFields
   */
   public Vector getAddressFields()
   {
      return super.getFields();
   }
   /* (non-Javadoc)
   * @see totalcross.pim.addressbook.AddressRecord#setAddressFields(totalcross.util.Vector)
   */
   public void setAddressFields(Vector fields)
   {
      super.setFields(fields);
   }
   /* (non-Javadoc)
   * @see totalcross.pim.addressbook.AddressRecord#registerNotSupportedhandler(pimal.notsupportedhandler.AddressNotSupportedHandler)
   */
   public void registerNotSupportedhandler(AddressNotSupportedHandler nsh)
   {
      this.exceptionalFieldHandler = nsh;
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal.PocketPCRecord#handleExceptionalFields(totalcross.util.Vector)
   */
   public void handleExceptionalFields(Vector exceptionalFields)
   {
      exceptionalFieldHandler.write(exceptionalFields, this);
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal.PocketPCRecord#addExceptionalFields(totalcross.util.Vector)
   */
   public void addExceptionalFields(Vector alreadyFoundFields)
   {
      exceptionalFieldHandler.complete(this, alreadyFoundFields);
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal.PocketPCRecord#templates()
   */
   public int templates()
   {
      return Constant.addressFieldTemplates();
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal.PocketPCRecord#template(int)
   */
   public VersitField template(int position)
   {
      return Constant.addressFieldTemplate(position);
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal..PocketPCRecord#getTemplates()
   */
   public Vector getTemplates()
   {
      return Constant.getAddressFieldTemplates();
   }
   /* (non-Javadoc)
   * @see superwaba.ext.ce.io.builtin.pimal.PocketPCRecord#field(int)
   */
   public String field(int position)
   {
      return Constant.iContactFields(position);
   }
}
