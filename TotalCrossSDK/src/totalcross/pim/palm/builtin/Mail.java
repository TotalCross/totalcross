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
 * Provides a link to the standard Palm Mail database..
 *
 * @author <A HREF="mailto:tines@ravnaandtines.com">Mr. Tines</A>,
 * @version 1.1.0 17 June 2001
 */
public class Mail implements Storable
{
   // record attributes for category as per PDBFile
   /** Indicates a message for the In box */
   public static final int      REC_ATTR_INBOX   = 0x0;
   /** Indicates a message for the Out box */
   public static final int      REC_ATTR_OUTBOX  = 0x1;
   /** Indicates a message for the Deleted category */
   public static final int      REC_ATTR_DELETED = 0x2;
   /** Indicates a message that has been filed */
   public static final int      REC_ATTR_FILED   = 0x3;
   /** Indicates a message that has been saved as draft */
   public static final int      REC_ATTR_DRAFT   = 0x4;
   private static final int     READ             = 1 << 7;
   private static final int     SIGNATURE        = 1 << 6;
   private static final int     CONFIRM_READ     = 1 << 5;
   private static final int     CONFIRM_DELIVERY = 1 << 4;
   private static final int     PRIORITY         = 3 << 2;
   /** Indicates a high priority message */
   public static final int      PRIORITY_HIGH    = 0;
   /** Indicates a normal priority message */
   public static final int      PRIORITY_NORMAL  = 1;
   /** Indicates a low priority message */
   public static final int      PRIORITY_LOW     = 2;
   private static final int     PRIORITY_SHIFT   = 2;
   private static final int     ADDRESSING       = 3;
   // no addressing values reverse engineered
   /** the mail PDBFile */
   private static ObjectPDBFile mailCat;

   public static void initMail() throws totalcross.io.IOException
   {
      if (mailCat == null)
         mailCat = new ObjectPDBFile("MailDB.mail.DATA");
   }

   /**
    * Gets the number of mails in the database
    *
    * @return the number of mails
    */
   public static int mailCount()
   {
      return mailCat.getRecordCount();
   }

   /**
    * Gets a Mail from the database
    *
    * @param i
    *           the index to get
    * @return the retrieved mail
    */
   public static Mail getMail(int i)
   {
      Mail mail = new Mail();
      if (mailCat.loadObjectAt(mail, i))
         return mail;
      return null;
   }

   /**
    * Gets a Mail from the database and places it into the given Mail. Any
    * previous data in the mail is erased.
    *
    * @param i
    *           the index to get
    * @param mail
    *           the mail object to place the mail into.
    */
   public static boolean getMail(int i, Mail mail)
   {
      return mailCat.loadObjectAt(mail, i);
   }

   /**
    * Adds a new Mail to the database
    *
    * @param mail
    *           the Mail to add
    * @return true if successful, false otherwise
    */
   public static boolean addMail(Mail mail)
   {
      return addMail(mail, REC_ATTR_OUTBOX);
   }

   /**
    * Adds a new Mail to the database
    *
    * @param mail
    *           the Mail to add
    * @param category
    *           The mail folder REC_ATTR_* category to set. Defaults to OUTBOX
    *           if invalid
    * @return true if successful, false otherwise
    */
   public static boolean addMail(Mail mail, int category)
   {
      if (!mailCat.addObject(mail))
         return false;
      int where = mailCount();
      byte cat = mailCat.getRecordAttributes(where - 1);
      if (category < REC_ATTR_INBOX || category > REC_ATTR_DRAFT)
         category = REC_ATTR_OUTBOX;
      byte cat2 = (byte) ((cat & 0xF0) | (category & 0xF));
      if (cat2 != cat)
         cat2 |= PDBFile.REC_ATTR_DIRTY;
      mailCat.setRecordAttributes(where - 1, cat2);
      return true;
   }

   // *************************** //
   // individual mail stuff below //
   // *************************** //
   /** Has this message been read */
   public boolean             read;
   /** Is there a .sig attached */
   public boolean             signature;
   /** Confirm read? */
   public boolean             confirmRead;
   /** Confirm Delivery? */
   public boolean             confirmDelivery;
   /** priority value 0-3 */
   public byte                priority = PRIORITY_NORMAL;
   /** addressing value 0-3 */
   public byte                addressing;
   /** Is there a date? */
   public boolean             dated;
   /** the date if any */
   public totalcross.sys.Time date;
   /** the subject of the mail */
   public String              subject;
   /** the sender of the mail */
   public String              from;
   /** the recipient of the mail */
   public String              to;
   /** the cc list */
   public String              cc;
   /** the bcc list */
   public String              bcc;
   /** the author of the mail's preferred address */
   public String              replyTo;
   /** the actual recipient of the mail */
   public String              sentTo;
   /** the text */
   public String              body;

   /**
    * Constructs a new empty address
    */
   public Mail()
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
      // The date (if any)
      int d = 0;
      if (dated)
      {
         d = (date.year - 1904) << 9;
         d |= date.month << 5;
         d |= date.day;
      }
      ds.writeShort(d);
      int hold = dated ? date.hour : 0;
      ds.writeByte(hold);
      hold = dated ? date.minute : 0;
      ds.writeByte(hold);
      // flags
      hold = 0;
      if (read)
         hold |= READ;
      if (signature)
         hold |= SIGNATURE;
      if (confirmRead)
         hold |= CONFIRM_READ;
      if (confirmDelivery)
         hold |= CONFIRM_DELIVERY;
      // sanity check
      if (priority < PRIORITY_HIGH || priority > PRIORITY_LOW)
         priority = PRIORITY_NORMAL;
      hold |= ((priority << PRIORITY_SHIFT) & PRIORITY);
      //no known non-zero addressing value
      //hold |= (addressing & ADDRESSING);
      ds.writeByte(hold);
      ds.writeByte(0);
      // data
      ds.writeCString(subject);
      ds.writeCString(from);
      ds.writeCString(to);
      ds.writeCString(cc);
      ds.writeCString(bcc);
      ds.writeCString(replyTo);
      ds.writeCString(sentTo);
      ds.writeCString(body);
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
      // The date (if any)
      int d = ds.readUnsignedShort();
      if (d != 0)
      {
         dated = true;
         date = new totalcross.sys.Time();
         date.year = (d >> 9) + 1904;
         date.month = (d >> 5) & 15;
         date.day = d & 31;
      }
      byte hold = ds.readByte();
      if (dated)
         date.hour = hold;
      hold = ds.readByte();
      if (dated)
      {
         date.minute = hold;
         date.second = 0;
         date.millis = 0;
      }
      // flags
      hold = ds.readByte();
      read = (hold & READ) != 0;
      signature = (hold & SIGNATURE) != 0;
      confirmRead = (hold & CONFIRM_READ) != 0;
      confirmDelivery = (hold & CONFIRM_DELIVERY) != 0;
      priority = (byte) ((hold & PRIORITY) >> PRIORITY_SHIFT);
      addressing = (byte) (hold & ADDRESSING);
      hold = ds.readByte();
      // data
      subject = ds.readCString();
      from = ds.readCString();
      to = ds.readCString();
      cc = ds.readCString();
      bcc = ds.readCString();
      replyTo = ds.readCString();
      sentTo = ds.readCString();
      body = ds.readCString();
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
      return new Mail();
   }
}
