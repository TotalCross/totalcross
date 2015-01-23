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



package totalcross.pim.memobook;
import totalcross.util.*;
import totalcross.pim.*;
/**
 * An interface describing the functionality and implementation <code>DateBook</code> must provide.
 * @author Gilbert Fridgen
 */
public interface MemoBook
{
   /**
    * Reads stored <code>MemoRecord</code>s from the device. The returned Vector will be adapted, when records are deleted or created later on.
    * @return RecordList of <code>MemoRecords</code> stored on this device
    */
   public RecordList getMemoRecords();
   /**
    * Creates a new <code>MemoRecord</code>
    * @return The created MemoRecord
    * @throws NotSupportedByDeviceException when no/no more records can be created on the given device
    */
   public MemoRecord createMemoRecord() throws NotSupportedByDeviceException;
   /**
    * Deletes a <code>MemoRecord</code> from the device
    * @param memoRecord <code>MemoRecord</code> to delete
    */
   public void deleteMemoRecord(MemoRecord memoRecord);
   /**
    * @return A Vector if <code>String</code>s containing the category name
    * @throws NotSupportedByDeviceException when the given device doesn't support categories
    */
   public Vector getCategories() throws NotSupportedByDeviceException;
   /**
    * Adds a category
    * @param category Category to add
    * @throws NotSupportedByDeviceException when the given device doesn't support categories or doesn't support more categories
    */
   public void addCategory(String category) throws NotSupportedByDeviceException;
   /**
    * Removes a category
    * @param category the name of the category to remove
    * @throws NotSupportedByDeviceException when the given device doesn't support categories
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException;
   /**
    * Renames a category
    * @param oldName old name of the category
    * @param newName new name of the category
    * @throws NotSupportedByDeviceException when the given device doesn't support categories
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException;
}
