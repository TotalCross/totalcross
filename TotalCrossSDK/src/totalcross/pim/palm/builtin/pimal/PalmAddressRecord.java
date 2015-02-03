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



package totalcross.pim.palm.builtin.pimal;
import totalcross.pim.addressbook.*;
import totalcross.pim.palm.builtin.*;
import totalcross.util.*;
/**
 * An implementation of interface AddressRecord for PalmOS.
 * Currently supports the following fields:
 * N, TITLE, ORG, TEL, EMAIL, ADR, X-CUSTOM, NOTE
 * @author Gilbert Fridgen
 */
public class PalmAddressRecord implements AddressRecord
{
   private int index;
   private AddressNotSupportedHandler nsh;
   /**
    * Creates a PalmAddressRecord from the given index
    * @param index the index, for which this Record should be created
    */
   protected PalmAddressRecord(int index)
   {
      this.index = index;
   }
   public Vector getFields()
   {
      Address physicalRecord = Address.getAddress(index);
      Vector addressFields = new Vector();
      AddressField af;
      String[] n_value =
      {
         physicalRecord.name, physicalRecord.firstName, "",
         "", ""
      };
      String[] n_options = {};
      af = new AddressField(AddressField.N, n_options, n_value);
      addressFields.addElement(af);
      String[] title_value =
      {
         physicalRecord.title
      };
      String[] title_options = {};
      af = new AddressField(AddressField.TITLE, title_options, title_value);
      addressFields.addElement(af);
      String[] org_value =
      {
         physicalRecord.company
      };
      String[] org_options = {};
      af = new AddressField(AddressField.ORG, org_options, org_value);
      addressFields.addElement(af);
      for (int i = 0;i < 5;i++)
      {
         String phone = physicalRecord.phone[i];
         if (!(phone == null) && phone.length() > 0)
         {
            int phoneLabelID = physicalRecord.phoneLabelID[i];
            if (phoneLabelID == 4)
            { 
               //email address
               String[] email_value =
               {
                  phone
               };
               String[] email_options =
               {
                  "type=pref"
               };
               af = new AddressField(AddressField.EMAIL, email_options, email_value);
            }
            else
            {
               String[] phone_value =
               {
                  phone
               };
               // todo: replace this by an array
               String type = "";
               switch(phoneLabelID)
               {
                  case 0:
                     type = "work";
                     break;

                  case 1:
                     type = "home";
                     break;

                  case 2:
                     type = "fax";
                     break;

                  case 5:
                     type = "work";
                     break;

                  case 6:
                     type = "pager";
                     break;

                  case 7:
                     type = "cell";
                     break;
               }
               String[] phone_options =
               {
                  "type=pref", "type=" + type
               };
               af = new AddressField(AddressField.TEL, phone_options, phone_value);
            }
            addressFields.addElement(af);
         }
      }
      String[] adr_value =
      {
         "", "", physicalRecord.address, physicalRecord.city,
         physicalRecord.state, physicalRecord.zipCode, physicalRecord.country
      };
      String[] adr_options =
      {
         "type=PREF"
      };
      af = new AddressField(AddressField.ADR, adr_options, adr_value);
      addressFields.addElement(af);
      for (int i = 0;i < 4;i++)
      {
         String[] x_value =
         {
            physicalRecord.custom[i]
         };
         String[] x_options =
         {
            "description=X-CUSTOM"
         };
         af = new AddressField(AddressField.X, x_options, x_value);
         addressFields.addElement(af);
      }
      String[] note_value =
      {
         physicalRecord.note
      };
      String[] note_options = {};
      af = new AddressField(AddressField.NOTE, note_options, note_value);
      addressFields.addElement(af);
      if (nsh != null)
         return nsh.complete(this, addressFields); // completed by notsupportedhanlder
      else
         return addressFields;
   }
   public void setFields(Vector fields)
   {
      Address physicalRecord = Address.getAddress(index);
      Vector notSupported = new Vector();
      int nCount = 0, titleCount = 0, orgCount = 0, telemailCount = 0, adrCount = 0, xCount = 0;
      int n = fields.size();
      for (int i = 0;i < n;i++)
      {
         AddressField af = (AddressField)fields.items[i];
         String[] values = af.getValues();
         switch(af.getKey())
         {
            case AddressField.N:
               if (nCount < 1)
               {
                  if (values.length >= 2)
                  { 
                     // check if array is long enough
                     physicalRecord.name = values[0];
                     physicalRecord.firstName = values[1];
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(af);
               nCount++;
               break;

            case AddressField.TITLE:
               if (titleCount < 1)
               {
                  if (values.length >= 1)
                     physicalRecord.title = values[0];
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               titleCount++;
               break;

            case AddressField.ORG:
               if (orgCount < 1)
               {
                  if (values.length >= 1)
                     physicalRecord.company = values[0];
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               orgCount++;
               break;

            case AddressField.TEL:
               if (telemailCount < 5)
               {
                  if (values.length >= 1)
                  {
                     int phoneID;
                     Vector types = af.getOption("TYPE");
                     if (types == null)
                        phoneID = 5;
                     else
                     if (types.indexOf("CELL") != -1)
                        phoneID = 7;
                     else
                     if (types.indexOf("FAX") != -1)
                        phoneID = 2;
                     else
                     if (types.indexOf("PAGER") != -1)
                        phoneID = 6;
                     else
                     if (types.indexOf("WORK") != -1)
                        phoneID = 0;
                     else
                     if (types.indexOf("HOME") != -1)
                        phoneID = 1;
                     else
                        phoneID = 5;
                     physicalRecord.phoneLabelID[telemailCount] = phoneID;
                     physicalRecord.phone[telemailCount] = values[0];
                  }
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               telemailCount++;
               break;

            case AddressField.EMAIL:
               if (telemailCount < 5)
               {
                  if (values.length >= 1)
                  {
                     physicalRecord.phoneLabelID[telemailCount] = 4;
                     physicalRecord.phone[telemailCount] = values[0];
                  }
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               telemailCount++;
               break;

            case AddressField.ADR:
               if (adrCount < 1)
               {
                  if (values.length >= 7)
                  {
                     physicalRecord.address = values[2];
                     physicalRecord.city = values[3];
                     physicalRecord.state = values[4];
                     physicalRecord.zipCode = values[5];
                     physicalRecord.country = values[6];
                  }
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               adrCount++;
               break;

            case AddressField.X:
               if (xCount < 4)
               {
                  if (values.length >= 1)
                     physicalRecord.custom[xCount] = values[0];
                  else
                     break;
               }
               else
                  notSupported.addElement(af);
               xCount++;
               break;

            default:
               notSupported.addElement(af);
               break;
         }
      }
      Address.changeAddress(index, physicalRecord);
      if (nsh != null)
         nsh.write(notSupported, this); // writes to notsupportedhandler
   }
   /**
    * Getter for index
    * @return index
    */
   protected int getIndex()
   {
      return index;
   }
   public String rawReadNote()
   {
      Address physicalRecord = Address.getAddress(index);
      return physicalRecord.note;
   }
   public void rawWriteNote(String note)
   {
      Address physicalRecord = Address.getAddress(index);
      physicalRecord.note = note;
      Address.changeAddress(index, physicalRecord);
   }
   public void registerNotSupportedhandler(AddressNotSupportedHandler nsh)
   {
      this.nsh = nsh;
   }
}
