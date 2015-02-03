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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, execute to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

import totalcross.util.*;
import totalcross.xml.AttributeList;
import totalcross.xml.ContentHandler;

/**
 * Methods here are called by the XML parser used in XmlRpcClient
 *
 * @version March 2006
 * @author Added to SuperWaba by Guich
 * @author Nimkathana (<a href="http://www.nimkathana.com">www.nimkathana.com</a>)
 */
public class XmlRpcContentHandler extends ContentHandler
{
   private Vector values;
   private StringBuffer cdata;
   private XmlRpcValue currentValue;
   private boolean readCdata;

   /** Gets the object unmarshalled from the last XML-RPC response parsing */
   public Object result;
   /** Tells if a fault occurred during the parsing */
   public boolean faultOccured;

   public XmlRpcContentHandler()
   {
      values = new Vector(50);
      cdata = new StringBuffer(128);
   }

   public void characters(String chars)
   {
      if (readCdata)
         cdata.append(chars);
   }

   public void endElement(int tag)
   {
      // finalize character data, if appropriate
      if (currentValue != null && readCdata)
      {
         currentValue.characterData(cdata.toString());
         cdata.setLength(0);
         readCdata = false;
      }

      if (tag == XmlRpcValue.VALUE)
      {
         int depth = values.size();
         // Only handle top level objects or objects contained in arrays here.
         // For objects contained in structs, wait for </member> (see code below).
         if (depth < 2 || values.items[depth - 2].hashCode() != XmlRpcValue.STRUCT)
         {
            XmlRpcValue v = currentValue;
            try {values.pop();} catch (ElementNotFoundException e) {}
            if (depth < 2)
            {
               // This is a top-level object
               result = v.getValue();
               currentValue = null;
            }
            else
            {
               // add object to sub-array; if current container is a struct, add later (at </member>)
               try {currentValue = (XmlRpcValue) values.peek();} catch (ElementNotFoundException e) {}
               currentValue.endElement(v);
            }
         }
      }

      if (tag == XmlRpcValue.MEMBER)
      {
         // Handle objects contained in structs.
         XmlRpcValue v = currentValue;
         try {values.pop();} catch (ElementNotFoundException e) {}
         try {currentValue = (XmlRpcValue) values.peek();} catch (ElementNotFoundException e) {}
         currentValue.endElement(v);
      }
      else if (tag == XmlRpcValue.METHODNAME)
      {
         // String methodName = cdata.toString();
         cdata.setLength(0);
         readCdata = false;
      }
   }

   public void startElement(int tag, AttributeList atts)
   {
      switch (tag)
      {
         case XmlRpcValue.ARRAY:
         case XmlRpcValue.STRUCT:
            currentValue.setType(tag);
            break;
         case XmlRpcValue.FAULT:
            faultOccured = true;
            break;
         case XmlRpcValue.NAME:
            //isStructName = true; // fall thru
         case XmlRpcValue.METHODNAME:
         case XmlRpcValue.STRING:
            cdata.setLength(0);
            readCdata = true;
            break;
         case XmlRpcValue.I4:
            tag = XmlRpcValue.INTEGER;
         case XmlRpcValue.DATE:
         case XmlRpcValue.BASE64:
         case XmlRpcValue.DOUBLE:
         case XmlRpcValue.BOOLEAN:
         case XmlRpcValue.INTEGER:
         case XmlRpcValue.LONG:
            currentValue.setType(tag);
            cdata.setLength(0);
            readCdata = true;
            break;
         case XmlRpcValue.VALUE:
            XmlRpcValue v = new XmlRpcValue();
            values.push(v);
            currentValue = v;
            // cdata object is reused
            cdata.setLength(0);
            readCdata = true;
            break;
         default:
            break;
      }
   }
}
