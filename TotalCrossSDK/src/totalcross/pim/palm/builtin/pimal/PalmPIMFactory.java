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



package totalcross.pim.palm.builtin.pimal;

import totalcross.pim.NotSupportedByDeviceException;
import totalcross.pim.PIMFactory;
import totalcross.pim.addressbook.AddressBook;
import totalcross.pim.datebook.DateBook;
import totalcross.pim.memobook.MemoBook;
import totalcross.pim.todobook.ToDoBook;

/**
 * Implementation of a PIMFactory for PalmOS. Provides access to the palm
 * specific books.
 *
 * @author Gilbert Fridgen
 */
public class PalmPIMFactory extends PIMFactory
{
   /**
    * standard constructor
    */
   public PalmPIMFactory()
   {

   }

   /**
    * @throws totalcross.io.IOException
    * @see totalcross.pim.PIMFactory#createAddressBook()
    */
   public AddressBook createAddressBook() throws NotSupportedByDeviceException, totalcross.io.IOException
   {
      return new PalmAddressBook();
   }

   /**
    * @throws totalcross.io.IOException
    * @see totalcross.pim.PIMFactory#createDateBook()
    */
   public DateBook createDateBook() throws NotSupportedByDeviceException, totalcross.io.IOException
   {
      return new PalmDateBook();
   }

   /**
    * @throws totalcross.io.IOException
    * @see totalcross.pim.PIMFactory#createToDoBook()
    */
   public ToDoBook createToDoBook() throws NotSupportedByDeviceException, totalcross.io.IOException
   {
      return new PalmToDoBook();
   }

   /**
    * @throws totalcross.io.IOException
    * @see totalcross.pim.PIMFactory#createMemoBook()
    */
   public MemoBook createMemoBook() throws NotSupportedByDeviceException, totalcross.io.IOException
   {
      return new PalmMemoBook();
   }
}
