/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Gilbert Fridgen                                           *
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
/**
 * Abstract superclass for Field-classes that use the vCard standard. Contains constants according to the keys, the vCard standard requests.
 * Please refer to vCard specification for more information
 * @author Gilbert Fridgen
 */
public abstract class VCardField extends VersitField
{
   // Identification Types
   final public static int FN = 1;
   final public static int N = 2;
   final public static int NICKNAME = 3;
   final public static int PHOTO = 4;
   final public static int BDAY = 5;
   // Delivery Addressing Types
   final public static int ADR = 11;
   final public static int LABEL = 12;
   // Telecommunications Addressing Types
   final public static int TEL = 21;
   final public static int EMAIL = 22;
   final public static int MAILER = 23;
   // Geographical Types
   final public static int TZ = 31;
   final public static int GEO = 32;
   // Organizational Types
   final public static int TITLE = 41;
   final public static int ROLE = 42;
   final public static int LOGO = 43;
   // final public int AGENT = 44; // not implemented (difficult to parse)
   final public static int ORG = 45;
   // Explanatory Types
   final public static int CATEGORIES = 51;
   final public static int NOTE = 52;
   final public static int PRODID = 53;
   final public static int REV = 54;
   final public static int SORT_STRING = 55;
   final public static int SOUND = 56;
   final public static int UID = 57;
   final public static int URL = 58;
   final public static int VERSION = 59;
   //Security Types
   final public static int CLASS = 61;
   final public static int KEY = 62;
   /**
    * @param key this addresses key (one of the static keys of contained in this class)
    * @param options an array of Strings of the form "option=value"
    * @param values an array of values, corresponding to the vCard3.0 specification of the chosen key
    */
   public VCardField(int key, String[] options, String[] values)
   {
      super(key, options, values);
   }
   /*
   *  (non-Javadoc)
   * @author Kathrin Barunwarth
   * @see java.lang.Object#toString()
   */
   public String toString()
   {
      String asString = "";
      switch(key)
      {
         case VCardField.FN:
            asString = "FN";
            break;

         case VCardField.N:
            asString = "N";
            break;

         case VCardField.NICKNAME:
            asString = "NICKNAME";
            break;

         case VCardField.PHOTO:
            asString = "PHOTO";
            break;

         case VCardField.BDAY:
            asString = "BDAY";
            break;

         case VCardField.ADR:
            asString = "ADR";
            break;

         case VCardField.LABEL:
            asString = "LABEL";
            break;

         case VCardField.TEL:
            asString = "TEL";
            break;

         case VCardField.EMAIL:
            asString = "EMAIL";
            break;

         case VCardField.MAILER:
            asString = "MAILER";
            break;

         case VCardField.TZ:
            asString = "TZ";
            break;

         case VCardField.GEO:
            asString = "GEO";
            break;

         case VCardField.TITLE:
            asString = "TITLE";
            break;

         case VCardField.ROLE:
            asString = "ROLE";
            break;

         case VCardField.LOGO:
            asString = "LOGO";
            break;

         //case VCardField.AGENT:
         //break;
         case VCardField.ORG:
            asString = "ORG";
            break;

         case VCardField.CATEGORIES:
            asString = "CATEGORIES";
            break;

         case VCardField.NOTE:
            asString = "NOTE";
            break;

         case VCardField.PRODID:
            asString = "PRODID";
            break;

         case VCardField.REV:
            asString = "REV";
            break;

         case VCardField.SORT_STRING:
            asString = "KEY";
            break;

         case VCardField.SOUND:
            asString = "SOUND";
            break;

         case VCardField.UID:
            asString = "UID";
            break;

         case VCardField.URL:
            asString = "URL";
            break;

         case VCardField.VERSION:
            asString = "VERSION";
            break;

         case VCardField.CLASS:
            asString = "CLASS";
            break;

         case VCardField.KEY:
            asString = "KEY";
            break;

         case VCardField.X:
            asString = "X-";
            break;
      }
      return asString + super.toString();
   }
}
