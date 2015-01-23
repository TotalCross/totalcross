/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Rob Nielsen                                               *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.palm.builtin;

import totalcross.io.DataStream;
import totalcross.io.ObjectPDBFile;
import totalcross.io.PDBFile;
import totalcross.io.Storable;

/**
 * Provides a link to the standard Palm Memo database. Note: if you call getMemo
 * and null is returned, its because that memo was deleted. The deleted memos
 * stays on the PDBFile, but you can't access them. So to do a search for a
 * title, use the method findMemo.
 * <p>
 * Here is an example for storing a memo:
 *
 * <pre>
 * Memo m2 = new Memo();
 * m2.text = &quot;a new memo&quot;;
 * Memo.addMemo(m2);
 * </pre>
 *
 * @author <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version 1.0.0 16 October 1999
 */
public class Memo implements Storable
{
   /** the memo PDBFile */
   private static ObjectPDBFile memoCat;

   public static void initMemo() throws totalcross.io.IOException
   {
      if (memoCat == null)
         memoCat = new ObjectPDBFile("MemoDB.memo.DATA");
   }

   /**
    * Gets the number of Memos in the database
    *
    * @return the number of memos
    */
   public static int memoCount()
   {
      return memoCat.getRecordCount();
   }

   /**
    * Gets a Memo from the database
    *
    * @param i
    *           the index to get
    * @return the retrieved memo or null if the memo with that index was
    *         deleted.
    */
   public static Memo getMemo(int i)
   {
      Memo memo = new Memo();
      if (memoCat.loadObjectAt(memo, i))
         return memo;
      return null;
   }

   /**
    * Gets a Memo from the database and places it into the given Memo. Any
    * previous data in the memo is erased.
    *
    * @param i
    *           the index to get
    * @param memo
    *           the memo object to place the memo into.
    */
   public static boolean getMemo(int i, Memo memo)
   {
      return memoCat.loadObjectAt(memo, i);
   }

   /**
    * Adds a new Memo to the database
    *
    * @param memo
    *           the memo to add
    * @return true if successful, false otherwise guich changed this method to
    *         be public.
    */
   public static boolean addMemo(Memo memo)
   {
      return memoCat.addObject(memo);
   }

   /**
    * Deletes a Memo entry in the database
    *
    * @param i
    *           the index to delete
    * @return true if successful, false otherwise
    */
   public static boolean delMemo(int i)
   {
      return memoCat.setObjectAttribute(i, PDBFile.REC_ATTR_DELETE);
   }

   /**
    * Changes the Memo at the given index
    *
    * @param i
    *           the index to change
    * @param memo
    *           a Memoobject with the values you want the Memo at i to have
    * @return true if successful, false otherwise
    */
   public static boolean changeMemo(int i, Memo memo)
   {
      if (memo == null)
         return false;
      if (memoCat.deleteObjectAt(i))
         return memoCat.insertObjectAt((Storable) memo, i);
      else
         return false;
   }

   // *************************** //
   // individual memo stuff below //
   // *************************** //
   /**
    * the text of the memo. Note: to read and write this text, you must use
    * DataStream.read/writeCString.
    */
   public String text;

   /**
    * Constructs a new empty memo
    */
   public Memo()
   {

   }

   /**
    * Send the state information of this object to the given object PDBFile
    * using the given DataStream. If any Storable objects need to be saved as
    * part of the state, their saveState() method can be called too.
    * @throws totalcross.io.IOException
    */
   public void saveState(DataStream ds) throws totalcross.io.IOException
   {
      ds.writeCString(text);
   }

   /**
    * Load state information from the given DataStream into this object If any
    * Storable objects need to be loaded as part of the state, their loadState()
    * method can be called too.
    *
    * @throws totalcross.io.IOException
    */
   public void loadState(DataStream ds) throws totalcross.io.IOException
   {
      // memos can be big so free up this one before loading in the
      // new one
      text = null;
      text = ds.readCString();
   }

   /**
    * Gets a unique ID for this class. It is up to the user to ensure that the
    * ID of each class of Storable contained in a single ObjectPDBFile is unique
    * and the ID of each instance in a class is the same.
    */
   public byte getID()
   {
      return 0; // not used
   }

   /**
    * Returns an object of the same class as this object.
    *
    * @return a class. Any data is irrelevent.
    */
   public Storable getInstance()
   {
      return new Memo();
   }

   /**
    * Search through the Memo to find records that starts with the given text.
    * ignoreCase searches are slower, since the string is converted to lowercase
    * before comparision.
    *
    * @see totalcross.sys.Convert#toLowerCase(char)
    * @param text
    *           this <code>String</code> value contains the text to search for ;
    *           multi-lines are OK with \n separator
    * @param ignoreCase
    *           if true, the search is case-insensitive ; uses
    *           <code>String.toLowerCase</code>
    * @return a <code>Memo</code> value ; <code>null</code> if no memo found
    *
    * @author Guich
    * @author <a href="mailto:sholtzer at users.sourceforge.net">Sylvain Holtzer</a>
    */
   public Memo findMemo(String text, boolean ignoreCase) // sholtzer@450_3: completely changed the routine
   {
      int i = findMemoIndex(text, ignoreCase);
      return (i >= 0) ? Memo.getMemo(i) : null;
   }

   /**
    * Search through the Memo to find records that starts with the given text.
    * ignoreCase searches are slower, since the string is converted to lowercase
    * before comparision.
    *
    * @param text
    *           this <code>String</code> value contains the text to search for ;
    *           multi-lines are OK with \n separator
    * @param ignoreCase
    *           if true, the search is case-insensitive ; uses
    *           <code>String.toLowerCase</code>
    * @return -1 if the Memo was deleted or not found; the index of the Memo
    *         otherwise. Added by Vik Olliver
    */
   public static int findMemoIndex(String text, boolean ignoreCase)
   {
      if (ignoreCase)
         text = text.toLowerCase();
      int n = Memo.memoCount();
      int len = text.length();
      for (int i = 0; i < n; i++)
      {
         Memo m = Memo.getMemo(i);
         if (m != null) // isnt the memo deleted?
         {
            String memoBody = m.text;
            if (memoBody.length() >= len) // the message can't be shorter than the title we're searching.
            {
               if (ignoreCase)
                  memoBody = memoBody.toLowerCase();
               // If the title is the first thing in the memo,
               // return the index of this memo.
               if (memoBody.startsWith(text))
                  return i;
            }
         }
      }
      return -1;
   }
}
