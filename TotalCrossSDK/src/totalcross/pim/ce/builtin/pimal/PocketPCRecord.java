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



package totalcross.pim.ce.builtin.pimal;
import totalcross.pim.ce.builtin.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * abstract superclass for all PocketPCRecords
 * used to minimize code in the subclasses (see setFields() and getFields())
 * @author Fabian Kroeher
 *
 */
public abstract class PocketPCRecord
{
   protected IObject source;
   protected Vector fields;
   /**
    * creates a new instance of PocketPCRecord with a given IObject source
    * @param source the IObject the PocketPCRecord wraps
    */
   public PocketPCRecord(IObject source)
   {
      this.source = source;
      this.fields = new Vector();
   }
   /**
    * this methods gets the POOM fields from the source (IObject) and maps them to Address-, Date- or ToDoField
    * @return a Vector with Address-, Date- or ToDoFields of this record
    */
   public Vector getFields()
   {
      // clear the fields Vector
      fields.removeAllElements();
      // first refresh the source with data from the device
      source.refresh();
      // go through the AddressField-Templates and replace the fieldnames with values
      int n = templates();
      for (int i = 0; i < n; i++)
      {
         VersitField tmp = template(i);
         String[] values = tmp.getValues();
         String[] newValues = new String[values.length];
         boolean addField = false;
         for (int j = 0; j < values.length; j++)
         {
            String fieldName = values[j];
            newValues[j] = source.getValue(fieldName);
            if (newValues[j].length() > 0)
               addField = true;
         }
         tmp.setValues(newValues);
         // only add the field if not all of the values are empty
         if (addField)
            fields.addElement(tmp);
      }
      // add exceptional fields (which are stored in the not e.g.)
      addExceptionalFields(fields);
      // finally return the retVal Vector
      return fields;
   }
   /**
    * sets the fields of this record to fields
    * @param fields a Vector which contains Address-, Date- or ToDoFields
    */
   public void setFields(Vector fields)
   {
      // delete all the existing fields from the source
      source.reset();
      // set the fields Vector to the new one (this should be the same anyway)
      this.fields = fields;
      // reorder the given fields; put the one with PREF at the beginning
      // (they are saved first) -> this way a PREF email-Address gets into email1-field
      // on the device (and not into email3 i.e.)
      Vector sortedFields = new Vector();
      // new Vetor for those fields which cannot be mapped
      Vector exceptionalFields = new Vector();
      int n = fields.size();
      for (int i = 0; i < n; i++)
      {
         VersitField tmp = (VersitField)fields.items[i];
         // tmp has PREF an is inserted at the beginning
         if (tmp.hasOption("TYPE", "PREF"))
            sortedFields.insertElementAt(tmp, 0);
         // tmp does not have pref and is appended
         else
            sortedFields.addElement(tmp);
      }
      // first, get a copy of the fieldTemplates
      Vector fieldTemplates = getTemplates();
      // go through the given (sorted) fields
      n = sortedFields.size();
      for (int i = 0; i < n; i++)
      {
         VersitField tmpSortedField = (VersitField)sortedFields.items[i];
         // now we need to estimate which one of the fieldTemplates (from
         // which we know how to map it) fits best to tmp (the actual field from the
         // given sorted fields)
         // Therefore we give a score for matching properties and assign tmp to the
         // fieldTemplate it fits best (highest score). How many points for a
         // matching property are given depends on the property and is defined in VersitField
         // the field must at least match with score higher than highscore to
         // be assigned to each other
         int highscore = 0;
         VersitField matchingTemplate = null;
         // go through the AddressFieldTemplates
         int nn = fieldTemplates.size();
         for (int j = 0; j < nn; j++)
         {
            VersitField tmpTemplate = (VersitField)fieldTemplates.items[j];
            int matchingScore = tmpSortedField.match(tmpTemplate);
            // the tmpTemplate matches better than anything before!
            if (matchingScore > highscore)
            {
               // set highscore to new score
               highscore = matchingScore;
               // set the matchingTemplate to tmpTemplate
               matchingTemplate = tmpTemplate;
            }
         }
         // check if we found a matching template at all
         if (matchingTemplate == null)
            // the tmpSortedField cannot be mapped because we found no matching temlate
            exceptionalFields.addElement(tmpSortedField);
         else
         {
            // we found out the best matching template for tmpSortedField
            // it will be used for mapping the tmpSortedField to source,
            // but first we must delete the matchingTemplate
            // from the templates to ensure that it cannot be used again (if the template would be
            // used again, the corresponding fields on the device would be overwritten instantly)
            fieldTemplates.removeElement(matchingTemplate);
            map(tmpSortedField, matchingTemplate);
         }
      }
      // finally, after mapping all the values to the source we store it on the device
      source.save();
      // after that, we handle the exceptional fields (if a handler is registered)
      handleExceptionalFields(exceptionalFields);
   }
   /**
    * the values of the AddressField values will be stored in the IObject source
    * according to the given template
    * please note that if the value has too many fields (that means is not after the vCard spec)
    * the additional values will be DROPPED!
    * please do also note that the source should be resetted before starting to insert new values!
    * @param value the AddressField which contains the values
    * @param template the AddressField which contains the fieldnames on the device for the values
    */
   protected void map(VersitField value, VersitField template)
   {
      try
      {
         String templates[] = template.getValues();
         String values[] = value.getValues();
         for (int i = 0; i < templates.length; i++)
            source.setValue(templates[i], values[i]);
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      {
         // well, this should not occur - it means that either the value VersitField
         // has to many values or not enough values
         // but if it occurs -> do nothing (there is no more value to map)
      }
   }
   /**
    * reads the note directly from the device and returns it
    * @return the note directly read from the device
    */
   public String rawReadNote()
   {
      // refresh the source from the device
      // source.refresh(); - guich: when inserting new todobooks, the record CANNOT be refreshed, or the old information will be losted. if something else fails bc of this, we must put a flag in order to keep the ToDoNSHNote.write working
      // get the note and pass it back
      return source.getValue("(String)note");
   }
   /**
    * writes the note directly to the device
    * @param note the note to be written on the device
    */
   public void rawWriteNote(String note)
   {
      // set the new value for source
      source.setValue("(String)note", note);
      // save the source to the device
      source.save();
   }
   /**
    * returns the first field of the fieldname Vector (this should be the id!)
    * @return the id of this Record
    */
   public String getId()
   {
      return source.getValue(field(0));
   }
   /**
    * deletes this Record from the device
    */
   public void delete()
   {
      source.delete();
   }
   /**
    * returns the size of the corresponding  *FieldTemplates Vector of class Constant
    * @return the number of templates for the object
    */
   abstract public int templates();
   /**
    * returns the template at the given position from the corresponding template Vector
    * from class Constant
    * @param position the index of the template
    * @return the template at the given index
    */
   abstract public VersitField template(int position);
   /**
    * returns a cloned copy of the corresponding *FieldTemplates Vector of class Constant
    * @return all the corresponding templates in a Vector
    */
   abstract public Vector getTemplates();
   /**
    * return the field_name_ of a *FieldTemplate at a given position
    * @param position the index of the fieldname to return
    * @return the fieldname at given index
    */
   abstract public String field(int position);
   /**
    * adds the exceptional fields to the given Vector; exceptional fields can mean that
    * they have been read out of the note e.g.
    * @param alreadyFoundFields the Vector of the "regular" fields where the exceptional fields can be added
    */
   abstract public void addExceptionalFields(Vector alreadyFoundFields);
   /**
    * handles the fields that could not be stored on the device (writes them in the note e.g.)
    * @param exceptionalFields a Vector of the fields that could not be mapped to the device
    */
   abstract public void handleExceptionalFields(Vector exceptionalFields);
}
