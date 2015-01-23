/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Rob Nielsen                                               *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.palm.builtin;

import totalcross.io.DataStream;
import totalcross.io.ObjectPDBFile;
import totalcross.io.PDBFile;
import totalcross.io.Storable;

/**
 * Provides a link to the standard Palm Address database.
 *
 * @author <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version 1.0.0 16 October 1999
 */
public class Address implements Storable
{
   /** the address PDBFile */
   private static ObjectPDBFile addressCat;

   public static void initAddress() throws totalcross.io.IOException
   {
      if (addressCat == null)
         addressCat = new ObjectPDBFile("AddressDB.addr.DATA");
   }

   /**
    * Gets the number of Addresss in the database
    *
    * @return the number of addresss
    */
   public static int addressCount()
   {
      return addressCat.getRecordCount();
   }

   /**
    * Gets a Address from the database
    *
    * @param i
    *           the index to get
    * @return the retrieved address
    */
   public static Address getAddress(int i)
   {
      Address address = new Address();
      if (addressCat.loadObjectAt(address, i))
         return address;
      return null;
   }

   /**
    * Gets a Address from the database and places it into the given Address. Any
    * previous data in the address is erased.
    *
    * @param i
    *           the index to get
    * @param address
    *           the address object to place the address into.
    */
   public static boolean getAddress(int i, Address address)
   {
      return addressCat.loadObjectAt(address, i);
   }

   /**
    * Adds a new Address to the database
    *
    * @param address
    *           the address to add
    * @return true if successful, false otherwise
    */
   public static boolean addAddress(Address address)
   {
      return addressCat.addObject(address);
   }

   /**
    * Deletes an Address entry in the database
    *
    * @param i
    *           the index to delete
    * @return true if successful, false otherwise
    */
   public static boolean delAddress(int i)
   {
      return addressCat.setObjectAttribute(i, PDBFile.REC_ATTR_DELETE);
   }

   /**
    * Changes the Address at the given index
    *
    * @param i
    *           the index to change
    * @param address
    *           an Addressobject with the values you want the Address at i to
    *           have
    * @return true if successful, false otherwise
    */
   public static boolean changeAddress(int i, Address address)
   {
      if (address == null)
         return false;
      if (addressCat.deleteObjectAt(i))
         return addressCat.insertObjectAt((Storable) address, i);
      else
         return false;
   }

   // *************************** //
   // individual address stuff below //
   // *************************** //
   private static final int PHONE8       = 1 << 21;
   private static final int PHONE7       = 1 << 20;
   private static final int PHONE6       = 1 << 19;
   private static final int NOTE         = 1 << 18;
   private static final int CUSTOM4      = 1 << 17;
   private static final int CUSTOM3      = 1 << 16;
   private static final int CUSTOM2      = 1 << 15;
   private static final int CUSTOM1      = 1 << 14;
   private static final int TITLE        = 1 << 13;
   private static final int COUNTRY      = 1 << 12;
   private static final int ZIPCODE      = 1 << 11;
   private static final int STATE        = 1 << 10;
   private static final int CITY         = 1 << 9;
   private static final int ADDRESS      = 1 << 8;
   private static final int PHONE5       = 1 << 7;
   private static final int PHONE4       = 1 << 6;
   private static final int PHONE3       = 1 << 5;
   private static final int PHONE2       = 1 << 4;
   private static final int PHONE1       = 1 << 3;
   private static final int COMPANY      = 1 << 2;
   private static final int FIRSTNAME    = 1 << 1;
   private static final int NAME         = 1 << 0;
   /** The index of phone (1-7) to display in the main list */
   public int               displayPhone;
   /** the type of each phone number to be displayed (0-Work,1-Home,etc) */
   public int[]             phoneLabelID = new int[5];
   /** the phone numbers */
   public String[]          phone        = new String[8];
   /** the custom fields */
   public String[]          custom       = new String[4];
   /** a note giving extra information */
   public String            note;
   /** the title of the person, eg "Mr" */
   public String            title;
   /** the country of the address, eg "Australia" */
   public String            country;
   /** the zip code or postcode of the address, eg "6009" */
   public String            zipCode;
   /** the state of the the address, eg "WA" */
   public String            state;
   /** the city of the address, eg "Perth" */
   public String            city;
   /** the street name, eg "12 Long St" */
   public String            address;
   /** the company the person works for, eg "TechCo" */
   public String            company;
   /** the first name of the person, eg "Rob" */
   public String            firstName;
   /** the surname of the person, eg "Nielsen" */
   public String            name;

   /**
    * Constructs a new empty address
    */
   public Address()
   {

   }

   /**
    * Send the state information of this object to the given object PDBFile
    * using the given DataStream. If any Storable objects need to be saved as
    * part of the state, their saveState() method can be called too.
    * @throws totalcross.io.IOException
    */
   public void saveState(DataStream ds) throws totalcross.io.IOException
   {
      int options = 0;
      int tmp;
      int i = 0;
      for (; i < 5; i++) // annahov@401_24: changed options calculation
      {
         tmp = phoneLabelID[i] & 0xF;
         tmp <<= (i << 2);
         options |= tmp;
      }
      tmp = (displayPhone - 1) & 0xF;
      tmp <<= (i << 2);
      options |= tmp;
      ds.writeInt(options);
      int flags = (name != null ? NAME : 0) | (firstName != null ? FIRSTNAME : 0) | (company != null ? COMPANY : 0)
            | (phone[0] != null ? PHONE1 : 0) | (phone[1] != null ? PHONE2 : 0) | (phone[2] != null ? PHONE3 : 0)
            | (phone[3] != null ? PHONE4 : 0) | (phone[4] != null ? PHONE5 : 0) | (address != null ? ADDRESS : 0)
            | (city != null ? CITY : 0) | (state != null ? STATE : 0) | (zipCode != null ? ZIPCODE : 0)
            | (country != null ? COUNTRY : 0) | (title != null ? TITLE : 0) | (custom[0] != null ? CUSTOM1 : 0)
            | (custom[1] != null ? CUSTOM2 : 0) | (custom[2] != null ? CUSTOM3 : 0) | (custom[3] != null ? CUSTOM4 : 0)
            | (note != null ? NOTE : 0) | (phone[5] != null ? PHONE6 : 0) | (phone[6] != null ? PHONE7 : 0)
            | (phone[7] != null ? PHONE8 : 0);
      ds.writeInt(flags);
      ds.writeByte(0);
      if (name != null)
         ds.writeCString(name);
      if (firstName != null)
         ds.writeCString(firstName);
      if (company != null)
         ds.writeCString(company);
      for (i = 0; i < 5; i++)
         if (phone[i] != null)
            ds.writeCString(phone[i]);
      if (address != null)
         ds.writeCString(address);
      if (city != null)
         ds.writeCString(city);
      if (state != null)
         ds.writeCString(state);
      if (zipCode != null)
         ds.writeCString(zipCode);
      if (country != null)
         ds.writeCString(country);
      if (title != null)
         ds.writeCString(title);
      for (i = 0; i < 4; i++)
         if (custom[i] != null)
            ds.writeCString(custom[i]);
      if (note != null)
         ds.writeCString(note);
      for (i = 5; i < 8; i++)
         if (phone[i] != null)
            ds.writeCString(phone[i]);
   }

   /**
    * Load state information from the given DataStream into this object If any
    * Storable objects need to be loaded as part of the state, their loadState()
    * method can be called too.
    *
    * @throws totalcross.io.IOException
    */
   public void loadState(DataStream ds) throws totalcross.io.IOException
   {
      // parse record
      int options = ds.readInt();
      for (int i = 0; i < 5; i++)
      {
         phoneLabelID[i] = (options & 0xF);
         options >>>= 4;
      }
      displayPhone = (options & 0xF) + 1;
      // flags determines which data exists in the record
      int flags = ds.readInt();
      ds.skipBytes(1);
      name = (flags & NAME) > 0 ? ds.readCString() : null;
      firstName = (flags & FIRSTNAME) > 0 ? ds.readCString() : null;
      company = (flags & COMPANY) > 0 ? ds.readCString() : null;
      for (int i = 0; i < 5; i++)
         phone[i] = (flags & PHONE1 << i) > 0 ? ds.readCString() : null;
      address = (flags & ADDRESS) > 0 ? ds.readCString() : null;
      city = (flags & CITY) > 0 ? ds.readCString() : null;
      state = (flags & STATE) > 0 ? ds.readCString() : null;
      zipCode = (flags & ZIPCODE) > 0 ? ds.readCString() : null;
      country = (flags & COUNTRY) > 0 ? ds.readCString() : null;
      title = (flags & TITLE) > 0 ? ds.readCString() : null;
      for (int i = 0; i < 4; i++)
         custom[i] = (flags & CUSTOM1 << i) > 0 ? ds.readCString() : null;
      // notes can be big so free up this one before loading in the
      // new one
      note = null;
      note = (flags & NOTE) > 0 ? ds.readCString() : null;
      for (int i = 5; i < 8; i++)
         phone[i] = (flags & PHONE1 << i) > 0 ? ds.readCString() : null;
   }

   /**
    * Gets a unique ID for this class. It is up to the user to ensure that the
    * ID of each class of Storable contained in a single ObjectPDBFile is unique
    * and the ID of each instance in a class is the same.
    */
   public byte getID()
   {
      return 0; // not used
   }

   /**
    * Returns an object of the same class as this object.
    *
    * @return a class. Any data is irrelevent.
    */
   public Storable getInstance()
   {
      return new Address();
   }
}
