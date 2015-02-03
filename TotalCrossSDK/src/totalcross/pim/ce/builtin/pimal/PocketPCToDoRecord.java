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
import totalcross.pim.todobook.*;
import totalcross.pim.*;
import totalcross.util.Vector;
/**
 * Implements the <code>pimal.todobook.ToDoRecord</code> interface for PocketPc devices
 * @author Fabian Kroeher
 */
public class PocketPCToDoRecord extends PocketPCRecord implements ToDoRecord
{
   private ToDoNotSupportedHandler exceptionalFieldHandler;
   /**
    * creates a new PocketPCToDoRecord from the given source; the only field needed from
    * the source is the id!
    * @param source the source from which this instance shall be created
    */
   public PocketPCToDoRecord(ITask source)
   {
      super(source);
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#getToDoFields()
   */
   public Vector getToDoFields()
   {
      return super.getFields();
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#setToDoFields(totalcross.util.Vector)
   */
   public void setToDoFields(Vector fields)
   {
      super.setFields(fields);
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#registerNotSupportedhandler(pimal.notsupportedhandler.ToDoNotSupportedHandler)
   */
   public void registerNotSupportedhandler(ToDoNotSupportedHandler nsh)
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
      return Constant.iTaskFields(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#getTemplates()
   */
   public Vector getTemplates()
   {
      return Constant.getToDoFieldTemplates();
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#template(int)
   */
   public VersitField template(int position)
   {
      return Constant.toDoFieldTemplate(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.PocketPCRecord#templates()
   */
   public int templates()
   {
      return Constant.toDoFieldTemplates();
   }
}
