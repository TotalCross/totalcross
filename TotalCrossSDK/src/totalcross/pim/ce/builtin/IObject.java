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
import totalcross.util.*;
/**
 * superclass of all the classes that represent data structures on the PocketPC device
 * @author Fabian Kroeher
 *
 */
public abstract class IObject
{
   private Hashtable fields;
   private int capacity;
   /**
    * creates a new IObject
    * @param capacity of the Hashtable where the fields are stored
    * @param nativeString the String which has been obtained from the native lib call
    */
   protected IObject(int capacity, StringExt nativeString)
   {
      fields = new Hashtable(capacity);
      this.capacity = capacity;
      parseNativeString(nativeString);
   }
   /**
    * this method reads out the nativeString until it reaches the next dataset Separator (defined in Constant);
    * please note that the part that has been read out is deleted from the nativeString (thus, by calling this
    * method the nativeString gets shorter and shorter every time)
    * @param nativeString the StringExt to parse
    */
   protected void parseNativeString(StringExt nativeString)
   {
      int fieldCounter = 0;
      // go through the native String an extract the values
      StringBuffer tmp = new StringBuffer("");
whileLoop:
      while (nativeString.length() > 0)
      {
         // check if there is a separator next
         if (nativeString.startsWith(Constant.d1Sep))
         {
            //System.out.println("found d1Sep, tmp is " + tmp);
            // found a separator - create new Field, flush tmp and delete the separator from the native String
            setValue(field(fieldCounter++), tmp.toString());
            tmp.setLength(0);
            nativeString.delete(0, Constant.d1Sep.length());
         }
         else
            if (nativeString.startsWith(Constant.datasetSep))
            {
               //System.out.println("found dataset sep, tmp is " + tmp);
               // found a dataset Separator, delete it from the String and break the loop
               setValue(field(fieldCounter++), tmp.toString());
               nativeString.delete(0, Constant.datasetSep.length());
               break whileLoop;
            }
            else
            {
               // nothing special, take the first char from the native String and append it to tmp
               tmp.append(nativeString.charAt(0));
               nativeString.delete(0, 1);
            }
      }
      if (tmp.length() > 0) // guich: if still has data, add it (case of the id returned by newTask, which has no separators)
         setValue(field(fieldCounter++), tmp.toString());
   }
   /**
    * resets all the fields of the IObject except for the id
    */
   public void reset()
   {
      // get the id and the key under which id is saved
      Hashtable newFields = new Hashtable(capacity);
      String idKey = field(0);
      String idVal = getValue(idKey);
      newFields.put(idKey, idVal);
      // overwrite fields with the new, empty (except for the id) Hashtable
      fields = newFields;
   }
   /**
    * gets the value which is stored under field name from the internal Hashtable fields
    * @param fieldName the field name
    * @return the value of the fieldname, null if field name is not found
    */
   private Object get(String fieldName)
   {
      return fields.get(fieldName);
   }
   /**
    * returns the value which is stored under field name from the internal Hashtable fields as a String,
    * no matter which datatype the value has.
    * @param fieldName the field name
    * @return the value of the field name, "" if the field is not found
    */
   public String getValue(String fieldName)
   {
      String retVal = null;
      if (fieldName.startsWith("(String)"))
         retVal = this.getString(fieldName);
      else
      {
         Object o=null;
         if (fieldName.startsWith("(IDate)"))
            o = this.getIDate(fieldName);
         else
         if (fieldName.startsWith("(IRecipients)"))
            o = this.getIRecipients(fieldName);
         else
         if (fieldName.startsWith("(IRecurrencePattern)"))
            o = this.getIRecurrencePattern(fieldName);
         if (o != null)
            retVal = o.toString();
      }

		if (retVal == null) retVal = "";

      return retVal;
   }
   /**
    * returns the value of a given field name as a String, ClassCast Exceptions are catched
    * @param fieldName the field name
    * @return the value as a String, null if field is not found or ClassCast Exception occurs
    */
   protected String getString(String fieldName)
   {
      String retVal = null;
      try
      {
         retVal = (String)get(fieldName);
      }
      catch(ClassCastException cce)
      {

      }
      return retVal;
   }
   /**
    * returns the value of a given field name as a String, ClassCast Exceptions are catched
    * @param fieldName the field name
    * @return the value as a IDate, null if field is not found or ClassCast Exception occurs
    */
   protected IDate getIDate(String fieldName)
   {
      IDate retVal = null;
      try
      {
         retVal = (IDate)get(fieldName);
      }
      catch(ClassCastException cce)
      {

      }
      return retVal;
   }
   /**
    * returns the value of a given field name as a String, ClassCast Exceptions are catched
    * @param fieldName the field name
    * @return the value as a IRecipients, null if field is not found or ClassCast Exception occurs
    */
   public IRecipients getIRecipients(String fieldName)
   {
      IRecipients retVal = null;
      try
      {
         retVal = (IRecipients)get(fieldName);
      }
      catch(ClassCastException cce)
      {

      }
      return retVal;
   }
   /**
    * returns the value of a given field name as a String, ClassCast Exceptions are catched
    * @param fieldName the field name
    * @return the value as a IRecurrencePattern, null if field is not found or ClassCast Exception occurs
    */
   public IRecurrencePattern getIRecurrencePattern(String fieldName)
   {
      IRecurrencePattern retVal = null;
      try
      {
         retVal = (IRecurrencePattern)get(fieldName);
      }
      catch(ClassCastException cce)
      {

      }
      return retVal;
   }
   /**
    * sets a field with name key to the value value; Objects according to their given data type #
    * (within the key) will be created when necessary
    * @param key the field name
    * @param value the value as String representation
    */
   public void setValue(String key, String value)
   {
      if (key.startsWith("(String)"))
         fields.put(key, value);
      else
      if (key.startsWith("(IDate)"))
         fields.put(key, new IDate(value));
      else
      if (key.startsWith("(IRecipients)"))
         fields.put(key, new IRecipients(value));
      else
      if (key.startsWith("(IRecurrencePattern)"))
         fields.put(key, new IRecurrencePattern(value));
   }
   /**
    * refreshes the data fields of the IObject from the PPC device
    * this method has to be implemented according to the
    * native library call that matches his datatype
    */
   abstract public void refresh();
   /**
    * saves the data fields of the IObject to the PPC device
    * this method has to be implemented according to the
    * native library call that matches his datatype
    */
   abstract public void save();
   /**
    * deletes this IObject on the PPC device
    * this method has to be implemented according to the
    * native library call that matches his datatype
    */
   abstract public void delete();
   /**
    * this method must return the
    * field name (from the corresponding i*Field Vectors of class Constant) of the given position
    * @param position of the fieldname
    * @return the name of the field
    */
   abstract public String field(int position);
   /**
    * this method must return the
    * size of the corresponding i*Field Vector of the class Constant
    * @return the size of the fields
    */
   abstract public int fields();
   /**
    * provides a simple String representation of this IObject mainly for debugging purposes
    * @return a String representation of this object
    */
   public String toString()
   {
      int n = fields();
      StringBuffer retVal = new StringBuffer(n*10);
      for (int i = 0; i < n; i++)
      {
         String f = field(i);
         retVal.append(f).append(':').append(getString(f)).append('\n');
      }
      return retVal.toString();
   }
}
