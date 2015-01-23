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



package totalcross.pim;
import totalcross.sys.*;
import totalcross.util.*;
/**
 * Handy methods to divide a note into note and fields and to devide a field into key options values.
 * @author braunwka
 */
public class NotSupportedHandlerNote
{
   /**
    * This method allows to divide the actual note from the not supported fields.
    * @param note complete note of this Record. Can consist of actual_note, the delimiter "####DO NOT EDIT BELOW####\n" and the not supported fields
    * @return Hashtable with keys "actual_note" (String) and "fields"(Vector of Strings, one for each field)
    */
   public Hashtable divideNoteIntoNoteAndFields(String note)
   {
      Hashtable divided = new Hashtable(31);
      Vector fields = new Vector();
      String actual_note = "";
      //dividing the current note into the actual note and the not supported address fields
      String[] note_st = Convert.tokenizeString(note, "####DO NOT EDIT BELOW####\n");
      if (!note_st.equals(note))
      {
         actual_note = note_st[0];
         if (note_st.length > 1)
         {
            //dividing the not supported fields from each other
            String[] notsupported_st = Convert.tokenizeString(note_st[1], '\n');
            String next;
            for (int i =0; i < notsupported_st.length; i++)
            {
               next = notsupported_st[i];
               if (next.length() > 0)
                  fields.addElement(next);
            }
         }
      }
      divided.put("actual_note", actual_note);
      divided.put("fields", fields);
      return divided;
   }
   /**
    * This method parses a Field for key, options and values.
    * @param field String representative of a field
    * @return Hashtable with keys "key" (String of the key of the field), "options" (Vector of Strings of the options of this field) and "values"(Vector of Strings of the values of this field)
    */
   public Hashtable divideFieldIntoKeyOptionsValues(String field)
   {
      Hashtable divided = new Hashtable(31);
      String key = "";
      Vector options = new Vector();
      Vector values = new Vector();
      char key_delimiter;
      if (field.indexOf(';') == -1)
      { 
         //no options
         key_delimiter = ':';
      }
      else
      {
         if (field.indexOf(';') < field.indexOf(':'))
            key_delimiter = ';'; // options found
         else
            key_delimiter = ':'; // no options
      }
      String[] key_options_st = Convert.tokenizeString(field, key_delimiter);
      //dividing key from options and storing key and options into hashtable divided
      if (!key_options_st.equals(field))
      {
         key = key_options_st[0];
         if (key_delimiter == ';')
         {
            // options found
            for (int i=1; i < key_options_st.length;)
               options.addElement(key_options_st[i]);
         }
      }
      //going to the values
      String[] dvalues_st = Convert.tokenizeString(field, ':');
      if (!dvalues_st.equals(field))
      {
         //dividing the values from each other and storing them on the vector values. Then adding values on the Hashtable divided
         if (dvalues_st.length > 1)
         {
            String[] values_st = Convert.tokenizeString(dvalues_st[1], ';');
            for (int i =0; i < values_st.length; i++)
               values.addElement(values_st[i]);
         }
      }
      divided.put("key", key);
      divided.put("options", options);
      divided.put("values", values);
      return divided;
   }
}
