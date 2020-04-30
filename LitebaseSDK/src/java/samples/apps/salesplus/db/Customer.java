// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.db;

/** 
 * This is where a customer is specified: a customer must have a name, an identification number, a complete address (address, city, state, country, 
 * zip), and contact phone numbers.
 *
 * Sometimes when dealing with large companies there is often a person inside the company that is the contact, which has a name, role in the 
 * company, and telephone numbers. Therefore, they are also fields of the customer abstract data type.
 */
public class Customer
{
   /**
    * Customer name.
    */
   public String name;
   
   /**
    * Customer fed id.
    */
   public String fedId;
   
   /**
    * Customer address.
    */
   public String address;
   
   /**
    * Customer city.
    */
   public String city;
   
   /**
    * Customer state.
    */
   public String state;
   
   /**
    * Customer country.
    */
   public String country;
   
   /**
    * Customer zip address.
    */
   public String zip;
   
   /**
    * Customer first telephone number.
    */
   public String tel1;
   
   /**
    * Customer second telephone number.
    */
   public String tel2;
   
   /**
    * Customer fax.
    */
   public String fax;

   /**
    * Customer contact name.
    */
   public String cName;
   
   /**
    * Customer contact role.
    */
   public String role;
   
   /**
    * Customer contact e-mail.
    */
   public String email;
   
   /**
    * Customer contact telephone number.
    */
   public String cTel; 
   
   /**
    * Customer contact mobile telephone number.
    */
   public String cel;
}
