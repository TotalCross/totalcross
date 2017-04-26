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



package totalcross.pim.ce.builtin;
/**
 * represents a recipient of a task/appointment with his name and email
 * @author Fabian Kroeher
 *
 */
public class IRecipient extends IExtended
{
   protected String name;
   protected String email; // TODO guich: i'm not sure if this class is used, since these were private! they aren't set anywhere
   /**
    * calls the contructor of superclass IExtended
    * @param nativeString
    */
   public IRecipient(String nativeString)
   {
      super(nativeString);
   }
   /**
    * @return the name of the IRecipient
    */
   public String getName()
   {
      return name;
   }
   /**
    * @return the email address of the IRecipient
    */
   public String getEmail()
   {
      return email;
   }
}
