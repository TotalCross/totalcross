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
import totalcross.pim.addressbook.*;
import totalcross.pim.datebook.*;
import totalcross.pim.memobook.*;
import totalcross.pim.todobook.*;
import totalcross.pim.*;
/**
 * Implements the <code>pimal.PIMFactory</code> interface for PocketPC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCPIMFactory extends PIMFactory
{
   public PocketPCPIMFactory()
   {
      if (!totalcross.sys.Vm.attachNativeLibrary("CeIoBuiltIn"))
         throw new RuntimeException("Can't find CeIoBuiltIn.dll!");
   }
   /* (non-Javadoc)
   * @see pimal.PIMFactory#createAddressBook()
   */
   public AddressBook createAddressBook() throws NotSupportedByDeviceException
   {
      return new PocketPCAddressBook();
   }
   /* (non-Javadoc)
   * @see pimal.PIMFactory#createDateBook()
   */
   public DateBook createDateBook() throws NotSupportedByDeviceException
   {
      return new PocketPCDateBook();
   }
   /* (non-Javadoc)
   * @see pimal.PIMFactory#createToDoBook()
   */
   public ToDoBook createToDoBook() throws NotSupportedByDeviceException
   {
      return new PocketPCToDoBook();
   }
   /**
    * always throws a NotSupportedByDeviceException because Memos are not supported
    * by PPC interface
    */
   public MemoBook createMemoBook() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("Cannot access the Memos on a PPC - sorry for that");
   }
}
