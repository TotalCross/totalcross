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
/**
 * represents the eVC++ IContact interface of the Pocket Outlook Object Model
 * @author Fabian Kroeher
 *
 */
public class IContact extends IObject
{
   /**
    * calls the constructor of the superclass IObject
    * @param nativeString
    */
   protected IContact(StringExt nativeString)
   {
      super(Constant.iContactFields(), nativeString);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#field(int)
   */
   public String field(int position)
   {
      return Constant.iContactFields(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#fields()
   */
   public int fields()
   {
      return Constant.iContactFields();
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#delete()
   */
   public void delete()
   {
      IPOutlookItemCollection.removeIContact(getValue(field(0)));
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#refresh()
   */
   public void refresh()
   {
      StringExt nativeString = new StringExt(IPOutlookItemCollection.getIContactString(getValue(field(0))));
      this.reset();
      parseNativeString(nativeString);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#save()
   */
   public void save()
   {
      String []fields = Constant.iContactFieldRange(0,46);
      int count = IPOutlookItemCollection.editIContact(getValue(fields[0]),
                                            getValue(fields[1]),
                                            getValue(fields[2]),
                                            getValue(fields[3]),
                                            getValue(fields[4]),
                                            getValue(fields[5]),
                                            getValue(fields[6]),
                                            getValue(fields[7]),
                                            getValue(fields[8]),
                                            getValue(fields[9]),
                                            getValue(fields[10]),
                                            getValue(fields[11]),
                                            getValue(fields[12]),
                                            getValue(fields[13]),
                                            getValue(fields[14]),
                                            getValue(fields[15]),
                                            getValue(fields[16]),
                                            getValue(fields[17]),
                                            getValue(fields[18]),
                                            getValue(fields[19]),
                                            getValue(fields[20]),
                                            getValue(fields[21]),
                                            getValue(fields[22]),
                                            getValue(fields[23]),
                                            getValue(fields[24]),
                                            getValue(fields[25]),
                                            getValue(fields[26]),
                                            getValue(fields[27]),
                                            getValue(fields[28]),
                                            getValue(fields[29]),
                                            getValue(fields[30]),
                                            getValue(fields[31]),
                                            getValue(fields[32]),
                                            getValue(fields[33]),
                                            getValue(fields[34]),
                                            getValue(fields[35]),
                                            getValue(fields[36]),
                                            getValue(fields[37]),
                                            getValue(fields[38]),
                                            getValue(fields[39]),
                                            getValue(fields[40]),
                                            getValue(fields[41]),
                                            getValue(fields[42]),
                                            getValue(fields[43]),
                                            getValue(fields[44]),
                                            getValue(fields[45]),
                                            getValue(fields[46]));
      if (count == 0)
         throw new RuntimeException("Could not save book "+getValue(fields[1]));
   }
}
