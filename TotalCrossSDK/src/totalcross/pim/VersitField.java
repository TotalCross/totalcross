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
import totalcross.util.*;

/**
 * Abstract superclass for Field-classes that use the vCard or vCalendar standard. Contains methods for handling key-option-value triples
 * Please refer to vCard and vCalendar specification for more information
 * @author Gilbert Fridgen
 */
public abstract class VersitField
{
   final public static int X = 9999;
   protected int key = -1;
   protected String[] values;
   protected Hashtable options;
   protected String[] optionsAsArray; // to clone the Object properly
   /**
    * Saves the key and the values in arrays and the options in a hashtable
    * @param key a field's key
    * @param options options to store
    * @param values values to store
    */
   public VersitField(int key, String[] options, String[] values)
   {
/*      this.key = key;
      this.optionsAsArray = options;
      this.values = (values == null) ? (new String[0]) : values;
      if (options == null)
         this.options = new Hashtable(13);
      else
      {
         this.options = new Hashtable(options.length);
         for (int i = 0;i < options.length;i++)
         {
            int equals = options[i].indexOf('='); // test if there's an equals-symbol in the option
            if (equals != -1)
            {
               // there is an equals
               int len = options[i].length();
               if (len > 1)
               {
                  // make sure that the String contains more than one "="
                  if (equals == len)
                  {
                     // is "=" the last character?
                     addOption(options[i], ""); // must be an empty option
                  }
                  else
                  if (equals == 0)
                  {
                     // is "=" the first character?
                     addOption("", options[i].substring(1)); // must be the value of an empty option - strange...
                  }
                  else
                  {
                     // regular option=value
                     addOption(options[i].substring(0, equals), options[i].substring(equals + 1));
                  }
               }
            }
            else
            {
               // when there's no "=" this must be a type-option
               if (options[i].length() > 0)
                  addOption("TYPE", options[i]);
            }
         }
      }*/
		this.key = key;
		this.optionsAsArray = options;
		this.values = (values==null) ? (new String[0]) : values;
		if(options == null){
			this.options = new Hashtable(13);
		} else{
			this.options = new Hashtable(options.length);
			for (int i = 0; i < options.length; i++) {
				int equals = options[i].indexOf('='); // test if there's an equals-symbol in the option
				if (equals != -1)  // there is an equals
				{
					if(options[i].length() > 1){ // make sure that the String contains more than one "="
						if (equals == options[i].length())  // is "=" the last character?
						{
							addOption(options[i].substring(0, equals), ""); // must be an empty option
						} 
						else 
						if (equals == 0){ 
						   // is "=" the first character?
							addOption("",options[i].substring(1)); // must be the value of an empty option - strange...
						}
						else //  regular option=value
						{
							addOption(options[i].substring(0, equals), options[i].substring(equals + 1));
						}
					}
				} else 
				   // when there's no "=" this must be a type-option
				if(options[i].length() > 0) addOption("TYPE", options[i]);
			}
		}
   }
   /**
    * Getter for the key
    * @return int this field's key
    */
   public int getKey()
   {
      return key;
   }
   /**
    * Returns this field's options. You can manipulate this Hashtable, but it is recommended to use the methods addOption() and removeOption() instead. *
    * The data structure of the options is as follows:
    * There's a Hashtable of options. The hashtable's keys consist of the option's keys. The hashtables value is a Vector of Strings of the option's value(s). If you have a one-value-option the option is still saved in a one-element-Vector.
    * @return Hashtable options
    */
   public Hashtable getOptions()
   {
      return options;
   }
   /**
    * Returns option-values for a specific option-key
    * @param key the option-key for which options should be returned
    * @return a Vector of option-values found for this option-key
    */
   public Vector getOption(String key)
   {
      key = key.toUpperCase();
      return (Vector)options.get(key);
   }
   /**
    * Getter for the values
    * @return A String[] of values
    */
   public String[] getValues()
   {
      return values;
   }
   /**
    * Adds an option to this field.
    * Options have the format "OPTION-KEY=OPTION-VALUE".
    * @param key the option's key
    * @param value the option's value
    */
   public void addOption(String key, String value)
   {
      key = key.toUpperCase();
      Vector v = null;
      v = (Vector)options.get(key); //retrieve corresponding vector
      if (v == null)
         v = new Vector(); // create Vector if necessary
      // TODO parse comma separated list
      if (value.length() > 0)
         v.addElement(value.toUpperCase()); // add value
      options.put(key, v);
   }
   /**
    * Removes a specific option.
    * @param key the key, whose given value should be removed
    * @param value the value that should be removed
    */
   public void removeOption(String key, String value)
   {
      key = key.toUpperCase();
      Vector v = null;
      v = (Vector)options.get(key); //retrieve corresponding vector
      // added by kroehefa
      if (v != null)
      {
         v.removeElement(value); // remove option
         if (v.size() == 0)
            options.remove(key); // remove Vector from Hashtable if it's empty
      }
   }
   /**
    * Setter for the values
    * @param values values to set
    */
   public void setValues(String[] values)
   {
      this.values = values;
   }
   /**
    * This method returns the String representative of this Object.
    * If you use this method of VersitField the key is missing in the String representative.
    * So please use the toString()-method of VCalField respective VCardField.
    * @author Kathrin Braunwarth
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer asString = new StringBuffer(256);
      Hashtable ht = getOptions();
      if (ht.size() > 0)
         asString.append(';');
      Vector v = ht.getKeys();
      int z = v.size();
      for (int i = 0;i < z;i++)
      {
         String o = (String)v.items[i];
         Vector a = (Vector)ht.get(o);
         int nj = a.size();
         for (int j = 0;j < nj;j++)
            asString.append(o).append('=').append((String)a.items[j]).append(';');
      }
      if (asString.length() > 0) // remove the last ;
         asString.setLength(asString.length()-1);

      asString.append(':');
      String[] h = getValues();
      z = h.length-1;
      for (int i = 0; i <= z; i++)
      {
         asString.append(h[i]);
         if (i < z)
            asString.append(";");
      }
      return asString.toString();
   }
   /**
    * Clones the options
    * @return the options' clone
    * @author Fabian Kroeher
    */
   protected String[] cloneOptions()
   {
      String[] options = new String[optionsAsArray.length];
		for (int j=0; j<this.optionsAsArray.length; j++) options[j]=this.optionsAsArray[j];
//      Vm.copyArray(this.optionsAsArray,0,options,0, options.length);
      return options;
   }
   /**
    * Clones the values
    * @return the values' clone
    * @author Fabian Kroeher
    */
   protected String[] cloneValues()
   {
      String[] values = new String[this.values.length];
//      Vm.copyArray(this.values,0,values,0,this.values.length);
		for (int i=0; i<this.values.length; i++) values[i]= this.values[i];
      return values;
   }
   /**
    * Checks of a spcific option is set
    * @param key the key of the option to check
    * @param value the value, that has to be set
    * @return <code>true</code>, if the value is found for the given option, otherwise <code>false</code>
    */
   public boolean hasOption(String key, String value)
   {
      key = key.toUpperCase();
      value = value.toUpperCase();
      Vector v = (Vector)options.get(key);
      if (v != null)
      {
         int n = v.size();
         for (int i = 0; i < n; i++)
         {
            // if Option is found, return true immediately
            if (((String)v.items[i]).toUpperCase().equals(value))
               return true;
         }
      }
      // options has not been found, return false
      return false;
   }
   /**
    * Calculates a score, how similar to VersitFields are
    * @param fieldToMatch VersitField to compare this field to
    * @return the score
    */
   public int match(VersitField fieldToMatch)
   {
      // the fields do not have the same key -> that means matching of 0 (no match)
      if (this.getKey() != fieldToMatch.getKey())
         return 0;
      // the fields have the same key - so lets compare them
      int score = 1;
      // determine what kind of options the fieldToMatch has
      Vector keys = fieldToMatch.getOptions().getKeys();
      // compare options for every key of the fieldToMatch compare
      int n = keys.size();
      for (int i = 0;i < n; i++)
      {
         String tmpKey = (String)keys.items[i];
         Vector tmpMyOptions = (Vector)this.getOption(tmpKey);
         Vector tmpFieldToMatchOptions = (Vector)fieldToMatch.getOption(tmpKey);
         if (tmpMyOptions == null || tmpMyOptions.size() == 0)
         {

         // tmpMyOptions does not contain options under this key -> no score
         }
         else
         {
            // both Objects have options corresponding to the tmpKey -> compare them
            int nj = tmpMyOptions.size();
            for (int j = 0;j < nj;j++)
            {
               String tmpMyOption = (String)tmpMyOptions.items[j];
               // go through the Options of tmpFieldToMatchOptions and compare
               int nk = tmpFieldToMatchOptions.size();
               forLoop3:for (int k = 0;k < nk;k++)
               {
                  String tmpFieldToMatchOption = (String)tmpFieldToMatchOptions.items[k];
                  if (tmpFieldToMatchOption.equals(tmpMyOption))
                  {
                     // found Options that are equal -> increment score and break this loop
                     if (tmpMyOption.equals("WORK") || tmpMyOption.equals("HOME"))
                        score += 5;
                     else
                        if (tmpMyOption.startsWith("X-"))
                           score += 9999;
                        else
                           score += 1;
                     break forLoop3;
                  }
               }
            }
         }
      }
      if (this.getKey() == VersitField.X)
      {
         if (score < 9999)
            return 0;
         else
            return score - 9999;
      }
      else
         return score;
   }
}
