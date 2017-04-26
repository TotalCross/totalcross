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



package totalcross.pim;

import totalcross.pim.addressbook.AddressBook;
import totalcross.pim.ce.builtin.pimal.PocketPCPIMFactory;
import totalcross.pim.datebook.DateBook;
import totalcross.pim.memobook.MemoBook;
import totalcross.pim.todobook.ToDoBook;

/**
 * PIMFactory is the class to connect to first, when writing an application
 * using the pimAL framework. Use getInstance() to receive an instance you can
 * work with. Then call create*(), according to the application you want to
 * access.
 *
 * @author fridgegi
 */
public abstract class PIMFactory
{
   private static PIMFactory instance;

   /**
    * Controls the creation of instances by making sure, only one instance is
    * created. (Singleton) <br>
    * Checks on which device the framework is currently running on and returns
    * the corresponding PIMFactory. (Factory)
    *
    * @return the one and only instance of PIMFactory
    * @throws NotSupportedByDeviceException
    *            when no PIM applications are supported on this device
    */
   public static PIMFactory getInstance() throws NotSupportedByDeviceException
   {
      if (instance == null) // chek if there's already an instance
      {
         /*
          * if (totalcross.sys.Settings.platform.equals("PalmOS") ||
          * totalcross.sys.Settings.platform.equals("Java")) instance = new
          * superwaba.ext.palm.io.builtin.pimal.PalmPIMFactory(); else if
          * (totalcross.sys.Settings.platform.equals("PocketPC"))
          */
         instance = new PocketPCPIMFactory();
         /*
          * else throw new NotSupportedByDeviceException("This device does not
          * provide a connector to PIM applications");
          */}
      return instance;
   }

   /**
    * @return An interface to the device's address book
    * @throws NotSupportedByDeviceException
    *            when device doesn't provide an address book
    * @throws totalcross.io.IOException
    */
   public abstract AddressBook createAddressBook() throws NotSupportedByDeviceException, totalcross.io.IOException;

   /**
    * @return An interface to the device's date book
    * @throws NotSupportedByDeviceException
    *            when device doesn't provide a date book
    * @throws totalcross.io.IOException
    */
   public abstract DateBook createDateBook() throws NotSupportedByDeviceException, totalcross.io.IOException;

   /**
    * @return An interface to the device's todo book
    * @throws NotSupportedByDeviceException
    *            when device doesn't provide a todo book
    * @throws totalcross.io.IOException
    */
   public abstract ToDoBook createToDoBook() throws NotSupportedByDeviceException, totalcross.io.IOException;

   /**
    * @return An interface to the device's memo book
    * @throws NotSupportedByDeviceException
    *            when device doesn't provide a memo book
    * @throws totalcross.io.IOException
    */
   public abstract MemoBook createMemoBook() throws NotSupportedByDeviceException, totalcross.io.IOException;
}
