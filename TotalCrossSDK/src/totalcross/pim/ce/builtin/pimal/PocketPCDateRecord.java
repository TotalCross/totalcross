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
import totalcross.pim.datebook.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * Implements the <code>pimal.datebook.DateRecord</code> interface for PocketPC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCDateRecord extends PocketPCRecord implements DateRecord
{
   private DateNotSupportedHandler exceptionalFieldHandler;
   /**
    * creates a new PocketPCDateRecord from the given source; the only field needed from
    * the source is the id!
    * @param source the source from which this instance shall be created
    */
   public PocketPCDateRecord(IAppointment source)
   {
      super(source);
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateRecord#getDateFields()
   */
   public Vector getDateFields()
   {
      return super.getFields();
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateRecord#setDateFields(totalcross.util.Vector)
   */
   public void setDateFields(Vector fields)
   {
      super.setFields(fields);
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateRecord#registerNotSupportedhandler(pimal.notsupportedhandler.DateNotSupportedHandler)
   */
   public void registerNotSupportedhandler(DateNotSupportedHandler nsh)
   {
      this.exceptionalFieldHandler = nsh;
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#handleExceptionalFields(totalcross.util.Vector)
   */
   public void handleExceptionalFields(Vector exceptionalFields)
   {
      exceptionalFieldHandler.write(exceptionalFields, this);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#addExceptionalFields(totalcross.util.Vector)
   */
   public void addExceptionalFields(Vector alreadyFoundFields)
   {
      exceptionalFieldHandler.complete(this, alreadyFoundFields);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#field(int)
   */
   public String field(int position)
   {
      return Constant.iAppointmentFields(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#getTemplates()
   */
   public Vector getTemplates()
   {
      return Constant.getDateFieldTemplates();
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#template(int)
   */
   public VersitField template(int position)
   {
      return Constant.dateFieldTemplate(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#templates()
   */
   public int templates()
   {
      return Constant.dateFieldTemplates();
   }
}
