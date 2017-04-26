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



package totalcross.xml.rpc;

// Copyright (C) 2004 Nimkathana (www.nimkathana.com), USA
//
// License: LGPL
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, execute to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

import totalcross.util.*;
import totalcross.net.*;
import totalcross.sys.*;

/**
 * Used to represent an XML-RPC value while a request is being parsed.
 * @version March 2006
 * @author Added to SuperWaba by Guich
 *@author
 * Maintained by Nimkathana
 * (<a href="http://www.nimkathana.com">www.nimkathana.com</a>)
 *@author
 * Original by IOP GmbH
 * (<a href="http://www.iop.de">www.iop.de</a>)
 */
public class XmlRpcValue
{
   private static final String types[] = { "String", "Properties.Int", "Properties.Boolean", "Properties.Double",
      "Date", "Base64", "Struct", "Array", "Long",
      "Value","Member","Fault","MethodName","Name","I4"};

   // XML RPC parameter types
   public static final int STRING = 0;
   public static final int INTEGER = 1;
   public static final int BOOLEAN = 2;
   public static final int DOUBLE = 3;
   public static final int DATE = 4;
   public static final int BASE64 = 5;
   public static final int STRUCT = 6;
   public static final int ARRAY = 7;
   public static final int LONG = 8;
   public static final int VALUE = 9;
   public static final int MEMBER = 10;
   public static final int FAULT = 11;
   public static final int METHODNAME = 12;
   public static final int NAME = 13;
   public static final int I4 = 14;

   public static IntHashtable tag2code;

   // flsobral@tc111_13: TagDereferencer expects tags in upper case.
   static
   {
      tag2code = new IntHashtable(31);
      tag2code.put("ARRAY", ARRAY);
      tag2code.put("VALUE", VALUE);
      tag2code.put("FAULT", FAULT);
      tag2code.put("NAME", NAME);
      tag2code.put("STRUCT", STRUCT);
      tag2code.put("STRING", STRING);
      tag2code.put("MEMBER", MEMBER);
      tag2code.put("METHODNAME", METHODNAME);
      tag2code.put("I4", I4);
      tag2code.put("INT", INTEGER);
      tag2code.put("INTEGER", INTEGER);
      tag2code.put("DATE", DATE);
      tag2code.put("DOUBLE", DOUBLE);
      tag2code.put("BOOLEAN", BOOLEAN);
      tag2code.put("BASE64", BASE64);
      tag2code.put("LONG", LONG);
   }

   private int type;
   private Vector array;
   private Object value;
   private Hashtable struct;
   private String nextMemberName;

   public XmlRpcValue()
   {
      this.type = STRING;
   }

   public String toString()
   {
      return (types[type] + " element " + value);
   }

   // This is a performance hack to get the type of a value without casting the Object.
   // It breaks the contract of method hashCode, but it doesn't matter since
   // Value objects are never used as keys in Hashtables.
   public int hashCode()
   {
      return type;
   }

   /**
    * Gets the corresponding object of this XmlRpcValue
    *@return
    * This XmlRpcValue's object
    */
   public Object getValue()
   {
      return value;
   }

   /**
    * Notification that a new child element has been parsed.
    *@param child
    * The child that was parsed
    */
   public void endElement(XmlRpcValue child)
   {
      if (type == ARRAY)
         array.addElement(child.value);
      else if (type == STRUCT) struct.put(nextMemberName, child.value);
   }

   /**
    * Set the type of this value.
    * If it's a container, this creates the corresponding Java container.
    *@param type
    * One of this class' public fields
    */
   public void setType(int type)
   {
      this.type = type;
      if (type == ARRAY) value = array = new Vector(); else
      if (type == STRUCT) value = struct = new Hashtable(13);
   }

   /**
    * Set the character data for the element and
    * interprets it according to the element type
    *@param cdata
    * The character data to set and interpret
    */
   public void characterData(String cdata)
   {
      try
      {
         switch (type)
         {
            case INTEGER:
               value = new Properties.Int(Convert.toInt(cdata.trim()));
               break;
            case BOOLEAN:
               value = new Properties.Boolean("1".equals(cdata.trim()));
               break;
            case DOUBLE:
               value = new Properties.Double(Convert.toDouble(cdata.trim()));
               break;
            case DATE:
               value = new Time(cdata.trim());
               break;
            case BASE64:
               value = Base64.decode(cdata);
               break;
            case STRING:
               value = cdata;
               break;
            case STRUCT:
               // this is the name to use for the next member of this struct
               nextMemberName = cdata;
               break;
            case LONG:
               value = new Properties.Long(Convert.toLong(cdata.trim()));
               break;
         }
      } catch (InvalidNumberException ine) {value = ine.getMessage();}
   }
}
