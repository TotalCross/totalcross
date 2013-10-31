/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
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



package totalcross.xml;
import totalcross.sys.*;
import totalcross.util.*;

/**
* This class describes the attributes attached to a start-tag. Tags are case-insensitive.
* <P>
* The <code>AttributeListIterator</code> class provides an iterator over
* the components of each attribute
* in this </code>AttributeList</code> instance:
* <UL>
* <LI>the attribute name
* <LI>the unquoted value
* <LI>the quote, if one exist
* </UL>
*/
public class AttributeList extends Hashtable
{
   Filter filter;
   
   /** Set to true if you want the get/set methods to be case insensitive. */
   public boolean caseInsensitive; // guich@tc113_29

   public AttributeList()
   {
      super(5);
   }

   /**
   * Clone
   */
   public AttributeList(AttributeList source)
   {
      super(source.size());
      Vector keys = source.getKeys();
      Vector values = source.getValues();
      int max = keys.size();
      for (int i=0; i < max; ++i)
         put(getKey((String)keys.items[i]), values.items[i]);
   }
   
   private String getKey(String k) // guich@tc113_29
   {
      return caseInsensitive ? k.toLowerCase() : k; // guich@tc113_12: added toLowerCase
   }

   /**
   * Add a new attribute to this AttributeList
   *
   * @param attrName name of the attribute
   * @param attrValue unquoted value of the attribute
   * @param dlm   delimiter that started the attribute value (' or ").
   *               '\0' if none
   */
   public final void addAttribute(String attrName, String attrValue, byte dlm)
   {
      attrName = getKey(attrName); // guich@tc113_12: convert toLowerCase
      if ((filter == null) || filter.acceptAttribute(attrName, attrValue, dlm))
         put(attrName, new AttributeValue(attrValue, dlm));
   }

   /**
   * Get the attribute value for a given name
   *
   * @param name attribute name
   * @return the value, or null if it wasn't specified
   */
   public final String getAttributeValue(String name)
   {
      AttributeValue value = (AttributeValue)get(getKey(name)); // guich@tc113_12: convert toLowerCase
      if (value != null)
         return value.value;
      return null;
   }

   /**
    * Get the attribute value for a given name as an integer
    *
    * @param name attribute name
    * @return the value, or the default value if it wasn't specified or 
    * there's a problem in the number convertion
    */
    public final int getAttributeValueAsInt(String name, int defaultValue)
    {
       AttributeValue value = (AttributeValue)get(getKey(name)); // guich@tc113_12: convert toLowerCase
       try 
       {
          return Convert.toInt(value.value);
       } 
       catch (Exception e) 
       {
          return defaultValue;
       }
    }

   /**
   * Set an AttributeList.Filter to filter the attribute entered in
   * this AttributeList
   *
   * @param filter AttributeList.Filter to set,
   *               or null if the current AttributeList filter must be removed
   * @return previous AttributeList.Filter or 0 if none was set
   */
   Filter setFilter(Filter filter)
   {
      Filter old = this.filter;
      this.filter = filter;
      return old;
   }

   /** interface to filter each attribute before entering it
   *   in this AttributeList
   */
   public interface Filter
   {
      /**
      * Call back to check if an attribute must be entered
      *
      * @param attrName name of the attribute
      * @param attrValue unquoted value of the attribute
      * @param dlm   delimiter that started the attribute value (' or ").
      *               '\0' if none
      * @return true if the attribute can be entered, false otherwise.
      */
      boolean acceptAttribute(String attrName, String attrValue, byte dlm);
   }

   /** class to iterate over each attribute of an AttributeList */
   public class Iterator
   {
      private int current;
      private String currentName;
      private AttributeValue currentValue;
      private Vector keys;
      private static final String singleQuote = "\'";
      private static final String doubleQuote = "\"";
      private static final String noQuote = "";

      /**
      * Construct an Iterator over each attribute in the outer AttributeList
      */
      public Iterator()
      {
         this.keys = getKeys();
      }

      /**
      * Make current the next attribute in this AttributeList
      *
      * @return true if the next attribute was activated,
      *         false if there are no more attribute in this list.
      */
      public final boolean next()
      {
         if (current < size())
         {
            currentName = (String)keys.items[current++];
            currentValue = (AttributeValue)get(currentName);
            return true;
         }
         else
         {
            currentName = null;
            currentValue = null;
            return false;
         }
      }

      /**
      * Get the name of the current attribute
      *
      * @return the name of the current attribute,
      *         or null if no attribute is actually current.
      */
      public final String getAttributeName()
      {
         return currentName;
      }

      /**
      * Get the unquoted value of the current attribute
      *
      * @return the unquoted value of the current attribute,
      *         or null if no attribute is actually current.
      */
      public final String getAttributeValue()
      {
         if (currentValue != null)
            return currentValue.value;
         return null;
      }

      /**
      * Get the quote surrounding the value of the current attribute
      *
      * @return "\'", "\"" or "" (the latter when no quote were surrounding
      *         the attribute value,)
      *         or null if no attribute is actually current.
      */
      public final String getValueDelimiter()
      {
         if (currentValue != null)
         {
            switch (currentValue.dlm)
            {
               case (byte)'\'':
                  return singleQuote;
               case (byte)'\"':
                  return doubleQuote;
               default:
                  return noQuote;
            }
         }
         return null;
      }

      /**
      * Get the attribute as a String, the value being surrounded by single
      * or double quotes if these were specified in the start-tag.
      *
      * @return The string a described above.
      */
      public final String getAttributeAsString()
      {
         if (currentValue != null)
         {
            String dlm = getValueDelimiter();
            return currentName + "=" + dlm + currentValue.value + dlm;
         }
         return null;
      }
   }

   /** Private class to store Attribute values with their delimiter */
   private static class AttributeValue
   {
      private String value;
      private byte dlm;
      /**
      * Constructor
      *
      * @param value unquoted value for this AttributeValue
      * @param delemiter single quote, double quote or 0
      */
      AttributeValue(String value, byte dlm)
      {
         this.value = value;
         this.dlm = dlm;
      }
   }
}
