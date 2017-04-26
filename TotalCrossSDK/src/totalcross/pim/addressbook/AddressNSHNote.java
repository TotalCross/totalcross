/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Kathrin Braunwarth                                        *
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



package totalcross.pim.addressbook;
import totalcross.pim.*;
import totalcross.sys.*;
import totalcross.util.*;
/**
 * Implement the NSH handler for Address.
 * @author braunwka
 */
public class AddressNSHNote extends NotSupportedHandlerNote implements AddressNotSupportedHandler
{
   /**
    * This method is for writing not supported fields into the note of the AddressRecord that is handed over.
    * @param notSupported With this Vector, you hand over the fields that are not supprted by the application.
    * @param ar This AddressRecord is the one your not supported fields belong to. The not supported fields will be added note field of this AddressRecord.
    */
   public void write(Vector notSupported, AddressRecord ar)
   {
      String b = ar.rawReadNote();
      String[] st = Convert.tokenizeString(b, "####DO NOT EDIT BELOW####\n");
      StringBuffer write = new StringBuffer(512);
      if (!st.equals(b)) // guich@totalcross: changed from StrTokenizer to Convert.tokenizeString
         write.append(st[0]);
      int size = notSupported.size();
      if (size > 0)
      {
         write.append("####DO NOT EDIT BELOW####\n");
         AddressField cur;
         size--;
         for (int i = 0;i <= size;i++)
         {
            cur = (AddressField)notSupported.items[i];
            write.append(cur.toString());
            if (i < size)
               write.append('\n');
         }
      }
      ar.rawWriteNote(write.toString());
   }
   static IntHashtable htKey;
   static
   {
      htKey = new IntHashtable(43);
      htKey.put("FN".hashCode(),VCardField.FN);
      htKey.put("N".hashCode(),VCardField.N);
      htKey.put("NICKNAME".hashCode(),VCardField.NICKNAME);
      htKey.put("PHOTO".hashCode(),VCardField.PHOTO);
      htKey.put("BDAY".hashCode(),VCardField.BDAY);
      htKey.put("ADR".hashCode(),VCardField.ADR);
      htKey.put("LABEL".hashCode(),VCardField.LABEL);
      htKey.put("TEL".hashCode(),VCardField.TEL);
      htKey.put("EMAIL".hashCode(),VCardField.EMAIL);
      htKey.put("MAILER".hashCode(),VCardField.MAILER);
      htKey.put("TZ".hashCode(),VCardField.TZ);
      htKey.put("GEO".hashCode(),VCardField.GEO);
      htKey.put("TITLE".hashCode(),VCardField.TITLE);
      htKey.put("ROLE".hashCode(),VCardField.ROLE);
      htKey.put("LOGO".hashCode(),VCardField.LOGO);
      htKey.put("ORG".hashCode(),VCardField.ORG);
      htKey.put("CATEGORIES".hashCode(),VCardField.CATEGORIES);
      htKey.put("PRODID".hashCode(),VCardField.PRODID);
      htKey.put("REV".hashCode(),VCardField.REV);
      htKey.put("SORT_STRING".hashCode(),VCardField.SORT_STRING);
      htKey.put("SOUND".hashCode(),VCardField.SOUND);
      htKey.put("UID".hashCode(),VCardField.UID);
      htKey.put("URL".hashCode(),VCardField.URL);
      htKey.put("VERSION".hashCode(),VCardField.VERSION);
      htKey.put("CLASS".hashCode(),VCardField.CLASS);
      htKey.put("KEY".hashCode(),VCardField.KEY);
   }
   /**
    * When you already read out the supported fields of a AddressRecord into a Vector, use this method to complete this Vector with the not supported fields from the note field.
    * @param  ar The AddressRecord you want to extract the not supported fields of note from.
    * @param alreadyFound Vector of fields of this AddressRecord that are already found in the supported fields
    * @return a Vector of all fields, supported and not supported of this AddressRecord.
    */
   public Vector complete(AddressRecord ar, Vector alreadyFound)
   {
      Vector addressFields = alreadyFound;
      AddressField af;
      String[] options = null;
      String[] values = null;
      int key = -1;
      String special_option = null; // for special vCardkeys: "X-[special_option]"
      //getting the current note, that consists of the actual note and some not supported address fields
      String b = "";
      for (int i = addressFields.size() - 1;i >= 0;i--)
      {
         af = (AddressField)addressFields.items[i];
         if (af.getKey() == VCardField.NOTE)
         {
            if (af.getValues().length > 0)
               b = af.getValues()[0];
            addressFields.removeElementAt(i);
         }
      }
      Hashtable divided = super.divideNoteIntoNoteAndFields(b);
      //handling the actual note and writing it onto the addressFields Vector
      String actual_note = null;
      actual_note = (String)divided.get("actual_note");
      if (actual_note != null)
      {
         values = new String[1];
         values[0] = actual_note;
         af = new AddressField(VCardField.NOTE, options, values);
         addressFields.addElement(af);
      }
      //handling the not supported fields
      Vector fields = null;
      fields = (Vector)divided.get("fields");
      if (fields != null)
      {
         int s = fields.size();
         for (int i = 0;i < s;i++)
         {
            options = null;
            values = null;
            Hashtable cur_field = super.divideFieldIntoKeyOptionsValues((String)fields.items[i]);
            String cur_key = null;
            cur_key = (String)cur_field.get("key");
            if (cur_key != null)
            {
               try {key = htKey.get(cur_key.hashCode());} catch (ElementNotFoundException e) {key = -99999;}
               if (key == -99999)
               {
                  if (cur_key.startsWith("X-"))
                  {
                     key = VCardField.X;
                     special_option = cur_key.substring(2); //storing the extention as first option
                  }
               }

               //getting options ans values and storing them into the string[]
               Vector res = null;
               res = (Vector)cur_field.get("options");
               if (res != null)
               {
                  if (special_option != null)
                     res.insertElementAt(special_option, 0);
                  options = (String[])res.toObjectArray();
               }
               res = (Vector)cur_field.get("values");
               if (res != null)
                  values = (String[])res.toObjectArray();
               // storing key, options and values as AddressField onto addressFields
               af = new AddressField(key, options, values);
               addressFields.addElement(af);
            }
         }
      }
      return addressFields;
   }
}
