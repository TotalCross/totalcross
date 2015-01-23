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



package totalcross.pim.datebook;
import totalcross.sys.*;
import totalcross.util.*;
import totalcross.pim.*;
/**
 * Implement the NSH handler for Date.
 * @author braunwka
 */
public class DateNSHNote extends NotSupportedHandlerNote implements DateNotSupportedHandler
{
   /**
    * This method is for writing not supported fields into the description of the DateRecord that is handed over.
    * @param notSupported With this Vector, you hand over the fields that are not supprted by the application.
    * @param dr This DateRecord is the one your not supported fields belong to. The not supported fields will be added to the description field of this DateRecord.
    */
   public void write(Vector notSupported, DateRecord dr)
   {
      String b = dr.rawReadNote();
      String[] st = Convert.tokenizeString(b, "####DO NOT EDIT BELOW####\n");
      StringBuffer write = new StringBuffer(512);
      if (!st.equals(b))
         write.append(st[0]);
      int size = notSupported.size();
      if (size > 0)
      {
         write.append("####DO NOT EDIT BELOW####\n");
         DateField cur;
         size--;
         for (int i = 0;i <= size;i++)
         {
            cur = (DateField)notSupported.items[i];
            write.append(cur.toString());
            if (i < size)
               write.append('\n');
         }
      }
      dr.rawWriteNote(write.toString());
   }
   static IntHashtable htKey;
   static
   {
      htKey = new IntHashtable(67);
      htKey.put("ATTACH".hashCode(),VCalField.ATTACH);
      htKey.put("CATEGORIES".hashCode(),VCalField.CATEGORIES);
      htKey.put("CLASSIFICATION".hashCode(),VCalField.CLASSIFICATION);
      htKey.put("COMMENT".hashCode(),VCalField.COMMENT);
      htKey.put("DESCRIPTION".hashCode(),VCalField.DESCRIPTION);
      htKey.put("GEO".hashCode(),VCalField.GEO);
      htKey.put("LOCATION".hashCode(),VCalField.LOCATION);
      htKey.put("PERCENT_COMPLETE".hashCode(),VCalField.PERCENT_COMPLETE);
      htKey.put("PRIORITY".hashCode(),VCalField.PRIORITY);
      htKey.put("RESSOURCES".hashCode(),VCalField.RESOURCES);
      htKey.put("STATUS".hashCode(),VCalField.STATUS);
      htKey.put("SUMMARY".hashCode(),VCalField.SUMMARY);
      htKey.put("COMPLETED".hashCode(),VCalField.COMPLETED);
      htKey.put("DTEND".hashCode(),VCalField.DTEND);
      htKey.put("DUE".hashCode(),VCalField.DUE);
      htKey.put("DTSTART".hashCode(),VCalField.DTSTART);
      htKey.put("DURATION".hashCode(),VCalField.DURATION);
      htKey.put("FREEBUSY".hashCode(),VCalField.FREEBUSY);
      htKey.put("TRANSP".hashCode(),VCalField.TRANSP);
      htKey.put("TZID".hashCode(),VCalField.TZID);
      htKey.put("TZNAME".hashCode(),VCalField.TZNAME);
      htKey.put("TZOFFSETFROM".hashCode(),VCalField.TZOFFSETFROM);
      htKey.put("TZOFFSETTO".hashCode(),VCalField.TZOFFSETTO);
      htKey.put("TZURL".hashCode(),VCalField.TZURL);
      htKey.put("ATTENDEE".hashCode(),VCalField.ATTENDEE);
      htKey.put("CONTACT".hashCode(),VCalField.CONTACT);
      htKey.put("ORGANIZER".hashCode(),VCalField.ORGANIZER);
      htKey.put("RECURRENCE_ID".hashCode(),VCalField.RECURRENCE_ID);
      htKey.put("RELATED_TO".hashCode(),VCalField.RELATED_TO);
      htKey.put("URL".hashCode(),VCalField.URL);
      htKey.put("UID".hashCode(),VCalField.UID);
      htKey.put("EXDATE".hashCode(),VCalField.EXDATE);
      htKey.put("EXRULE".hashCode(),VCalField.EXRULE);
      htKey.put("RDATE".hashCode(),VCalField.RDATE);
      htKey.put("RRULE".hashCode(),VCalField.RRULE);
      htKey.put("ACTION".hashCode(),VCalField.ACTION);
      htKey.put("REPEAT".hashCode(),VCalField.REPEAT);
      htKey.put("TRIGGER".hashCode(),VCalField.TRIGGER);
      htKey.put("CREATED".hashCode(),VCalField.CREATED);
      htKey.put("DTSTAMP".hashCode(),VCalField.DTSTAMP);
      htKey.put("LAST_MODIFIED".hashCode(),VCalField.LAST_MODIFIED);
      htKey.put("SEQUENCE".hashCode(),VCalField.SEQUENCE);
      htKey.put("REQUEST_STATUS".hashCode(),VCalField.REQUEST_STATUS);
   }
   /**
    * When you already read out the supported fields of a DateRecord into a Vector, use this method to complete this Vector with the not supported fields from the description field.
    * @param dr The DateRecord you want to extract the not supported fields of description from.
    * @param alreadyFound Vector of fields of this DateRecord that are already found in the supported fields
    * @return a Vector of all fields, supported and not supported of this DateRecord.
    */
   public Vector complete(DateRecord dr, Vector alreadyFound)
   {
      Vector dateFields = alreadyFound;
      DateField df;
      String[] options = null;
      String[] values = null;
      int key = -1;
      String special_option = null; // for special vCalkeys: "X-[special_option]"
      //getting the current note, that consists of the actual note and some not supported address fields
      String b = "";
      for (int i = dateFields.size() - 1;i >= 0;i--)
      {
         df = (DateField)dateFields.items[i];
         if (df.getKey() == VCalField.DESCRIPTION)
         {
            if (df.getValues().length > 0)
               b = df.getValues()[0];
            dateFields.removeElementAt(i);
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
         df = new DateField(VCalField.DESCRIPTION, options, values);
         dateFields.addElement(df);
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
                     key = VCalField.X;
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
               df = new DateField(key, options, values);
               dateFields.addElement(df);
            }
         }
      }
      return dateFields;
   }
}
