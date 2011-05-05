/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package litebase;

import totalcross.io.*;
import totalcross.util.*;

/**
 * Represents a B-Tree header.
 */
class Index
{
   /**
    * The size of the index cache.
    */
   private static final int INDEX_CACHE_SIZE = 20;
   
   /** 
    * The size of the disk sector. Used for calculating the number of keys of a node.
    */
   private static final int SECTOR_SIZE = 512;
 
   /**
    * The sizes for each type.
    */
   private static final int[] keyRecSizes = {4, 2, 4, 8, 4, 8, 4, 0, 4, 8, 0};
   
   /**
    * The maximun number of keys per node.
    */
   int btreeMaxNodes;
   
   /**
    * The root of the tree.
    */
   Node root;

   /**
    * The middle key.
    */
   private Key med;

   /**
    * A temporary key.
    */
   Key tempKey;

   /**
    * Indicates if the index is still empty.
    */
   private boolean isEmpty;

   /**
    * The size of the nodes.
    */
   int nodeRecSize;

   /**
    * A cache of node.
    */
   private int cacheI;

   /**
    * The size of the keys.
    */
   int keyRecSize;

   /**
    * The number of nodes.
    */
   int nodeCount;

   /**
    * The sizes of the columns of the index.
    */
   int[] colSizes;

   /**
    * The types of the columns of the index.
    */
   int[] types;

   /**
    * The cache of the index.
    */
   private Node[] cache;

   /**
    * The name of the index table.
    */
   String name;

   /**
    * The nodes file.
    */
   NormalFile fnodes;

   /**
    * The repeated values file.
    */
   NormalFile fvalues;

   /**
    * A stream to be used to save and load data from the index.
    */
   ByteArrayStream bas;

   /**
    * A stream to be used to save and load data from the index.
    */
   DataStreamLE basds;

   /**
    * A buffer to be used to save and load data from the index.
    */
   byte[] basbuf;

   /**
    * If the keys are mostly ordered (like the rowid), makes the nodes more full.
    */
   boolean isOrdered; // guich@110_5

   /**
    * The table of the index.
    */
   Table table;

   /**
    * Constructs an index structure.
    *
    * @param aTable The table of the index.
    * @param keyTypes The types of the columns of the index.
    * @param newColSizes The column sizes.
    * @param aName The name of the index table.
    * @param sourcePath The path of the index files.
    * @param hasIndr Indicates if the index has the .idr file.
    * @param exist Indicates that the index files already exist. 
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   Index(Table aTable, int[] keyTypes, int[] newColSizes, String aName, String sourcePath, boolean hasIdr, boolean exist) 
                                                                                           throws IOException, InvalidDateException
   {
      int numberColumns = keyTypes.length;

      table = aTable;
      types = keyTypes;
      name = aName;
      colSizes = newColSizes;
      keyRecSize = Key.VALREC_SIZE;
      
      while (--numberColumns >= 0) // Gets the key sizes for each column of the index.
         keyRecSize += keyRecSizes[keyTypes[numberColumns]];
      
      btreeMaxNodes = (SECTOR_SIZE - 5) / (keyRecSize + 2);
      nodeRecSize = 2 + btreeMaxNodes * keyRecSize + ((btreeMaxNodes + 1) << 1); // int size + key[k] + (Node = int)[k+1]

      // Creates the streams.
      bas = new ByteArrayStream(nodeRecSize);
      basbuf = bas.getBuffer();
      basds = new DataStreamLE(bas);

      // Creates and populates the cache.
      cache = new Node[INDEX_CACHE_SIZE];

      // Creates the index files.
      String fullFileName = Utils.getFullFileName(name, sourcePath);
      fnodes = new NormalFile(fullFileName + ".idk", !exist, nodeRecSize);
      if (hasIdr)
      {
         fvalues = new NormalFile(fullFileName + ".idr", !exist, NormalFile.CACHE_INITIAL_SIZE);
         fvalues.finalPos = fvalues.size; // juliana@211_2: corrected a possible .idr corruption if it was used after a closeAll().
         fvalues.valAux = table.tempVal2; // juliana@224_2: improved memory usage on BlackBerry.
      }
      // Creates the root node.
      root = new Node(this);
      root.idx = 0;

      if (!(isEmpty = fnodes.size == 0))
         root.load();

      // juliana@213_8: the index node count was not being loaded when loading the indices, which could cause an infinite loop when using them.
      nodeCount = fnodes.size / nodeRecSize;  
      
      // Creates the temp keys.
      med = new Key(this);
      tempKey = new Key(this);
   }

   /**
    * Removes a value from the index.
    *
    * @param key The key to be removed.
    * @param value The repeated value index.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void removeValue(Key key, Value value) throws IOException, InvalidDateException, DriverException
   {
      if (!isEmpty)
      {
         Node curr = root; // 0 is always the root.
         int pos,
             nodeCounter = nodeCount;
         int[] types = key.index.types;
         SQLValue[] keys = key.keys;
         Key keyFound;
         
         while (true)
         {
            pos = curr.findIn(key, false); // juliana@201_3 // Finds the key position.
            keyFound = curr.keys[pos];

            if (pos < curr.size && Utils.arrayValueCompareTo(keys, keyFound.keys, types) == 0) 
            {
               switch (keyFound.remove(value)) // Tries to remove the key.  
               {
                  // It successfully removed the key.
                  case Key.REMOVE_SAVE_KEY:
                     curr.saveDirtyKey(pos); // no break!
                  case Key.REMOVE_VALUE_ALREADY_SAVED:
                     return;
               }
            }

            if (curr.children[0] == Node.LEAF)  // If there are children, load them if the key was not found yet.
               break;
            
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop. 
              throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(curr.children[pos]);
         }
         
      }
      throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_IDX_RECORD_DEL));
   }

   /**
    * Loads a node.
    *
    * @param idx The index of the value to be loaded.
    * @return The node.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   private Node loadNode(int idx) throws IOException, InvalidDateException, DriverException
   {
      if (idx == 0) // If the index is 0, return the root.
         return root;
      if (idx == Node.LEAF) // If the node is a leaf, the index is corrupted.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
      
      // Loads the cache.
      int j = INDEX_CACHE_SIZE;
      while (--j >= 0)  
         if (cache[j] != null && cache[j].idx == idx)
            return cache[cacheI = j];
      
      if (++cacheI >= INDEX_CACHE_SIZE)
         cacheI = 0;
      Node cand = cache[cacheI];
      if (cand == null)
         (cand = cache[cacheI] = new Node(this)).isWriteDelayed = root.isWriteDelayed;
         
      if (cand.isWriteDelayed && cand.isDirty) // Saves this one if it is dirty.
         cand.save(false, 0, cand.size);

      // Loads the node.
      cand.idx = idx;
      cand.load();
      return cand;
   }

   /**
    * Finds the given key and make the monkey climb on the values.
    *
    * @param key The key to be found.
    * @param monkey The monkey object.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void getValue(Key key, Monkey monkey) throws IOException, InvalidDateException, DriverException
   {
      if (!isEmpty)
      {
         Node curr = root; // 0 is always the root.
         Key keyFound;
         int[] types = key.index.types;
         SQLValue[] keys = key.keys;
         int pos,
             nodeCounter = nodeCount;
         
         while (true)
         {
            pos = curr.findIn(key, false); // juliana@201_3
            keyFound = curr.keys[pos];
            if (pos < curr.size && Utils.arrayValueCompareTo(keys, keyFound.keys, types) == 0)
            {
               monkey.onKey(keyFound);
               break;
            }
            if (curr.children[0] == Node.LEAF)
               break;
            
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
              throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(curr.children[pos]);
         }
      }
   }

   /**
    * Climbs on the nodes that are greater or equal than the current one.
    *
    * @param node The node to be compared with.
    * @param nodes A vector of nodes.
    * @param start The first key of the node to be searched.
    * @param monkey The monkey object.
    * @param stop Indicates when the climb process can be finished.
    * @return If it has to stop the climbing process or not.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private boolean climbGreaterOrEqual(Node node, Vector nodes, int start, Monkey monkey, boolean stop) throws IOException, InvalidDateException
   {
      int size = node.size;
      Key[] keys = node.keys;
      short[] children = node.children;
      if (start >= 0)
         stop = !monkey.onKey(keys[start]);
      if (children[0] == Node.LEAF)
         for (int i = start + 1; !stop && i < size; i++)
            stop = !monkey.onKey(keys[i]);
      else
      {
         Node curr = null;
         try
         {
            curr = (Node)nodes.pop();
         }
         catch (ElementNotFoundException exception)
         {
            curr = new Node(node.index); 
         }

         while (!stop && ++start <= size)
         {
            curr.idx = children[start];
            curr.load();
            stop = climbGreaterOrEqual(curr, nodes, -1, monkey, stop);
            if (start < size && !stop)
               stop = !monkey.onKey(keys[start]);
         }
         nodes.push(curr);
      }
      return stop;
   }

   /**
    * Starts from the root to find the left key, then climbs from it until the end.
    *
    * @param left The left key.
    * @param monkey The Monkey object.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void getGreaterOrEqual(Key left, Monkey monkey) throws IOException, InvalidDateException, DriverException
   {
      if (!isEmpty)
      {
         int pos,
             nodeCounter = nodeCount;
         IntVector iv = new IntVector(10);
         Node curr = root; // Starts from the root.
         SQLValue[] currKeys;
         SQLValue[] leftKeys = left.keys;
         int[] types = left.index.types;
         
         while (true)
         {
            pos = curr.findIn(left, false); // juliana@201_3
            if (pos < curr.size)
            {
               currKeys = curr.keys[pos].keys;
               int r = Utils.arrayValueCompareTo(leftKeys, currKeys, types); // Compares left keys with curr keys.
               if (r <= 0) // If this value is above or equal to the one being looked for, stores it.
               {
                  iv.push(curr.idx);
                  iv.push(pos);
               }
               if (r >= 0) // left >= curr.keys[pos] ?
                  break;
            }
            if (curr.children[0] == Node.LEAF)
               break;
            
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(curr.children[pos]);
         }
         if (iv.size() > 0)
         {
            Vector v = new Vector(10);
            boolean stop;
            while (iv.size() > 0)
            {
               stop = false;
               try
               {
                  pos = iv.pop();
                  curr = loadNode(iv.pop());
               }
               catch (ElementNotFoundException exception) {}
               if ((stop = climbGreaterOrEqual(curr, v, pos, monkey, stop)))
                  break;
            }
         }
      }
   }

   /**
    * Splits the overflown node of this B-Tree. The stack ancestors contains all ancestors of the node, together with the known insertion position in 
    * each of these ancestors.
    *
    * @param curr The current node.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private void splitNode(Node curr) throws IOException, InvalidDateException
   {
      int left, 
          right,
          ins;
      Key keyFound;
      IntVector ancestors = curr.index.table.ancestors; // juliana@224_2: improved memory usage on BlackBerry.
      
      // guich@110_3: curr.size * 3/4 - note that medPos never changes, because the node is always split when the same size is reached.
      int medPos = curr.index.isOrdered ? (curr.size - 1) : (curr.size / 2);
  
      while (true)
      {
         keyFound = curr.keys[medPos];
         med.set(keyFound.keys);
         med.valRec = keyFound.valRec;

         // Right sibling - must be the first one to save!
         right = curr.save(true, medPos + 1, curr.size);

         if (curr.idx == 0)  // Is it the root?
         {
            left = curr.save(true, 0, medPos); // Left sibling.
            root.save(false, 0, root.size); // juliana@114_3: fixed the index saving. When the root node was splitted, it was not being saved.
            root.set(med, left, right); // Replaces the root record.
            root.save(false, 0, root.size);
            break;
         }
         else // guich@110_4: reuses this node; cut it at medPos.
         {
            left = curr.idx;
            curr.size = medPos;
            curr.save(false, 0, curr.size);
            ins = 0;
            try
            {
               curr = loadNode(ancestors.pop()); // Loads the parent.
               ins = ancestors.pop();
            }
            catch (ElementNotFoundException exception) {} // Parent insert position.

            curr.insert(med, left, right, ins);
            if (curr.size < btreeMaxNodes) // Parent has not overflown?
               break;
         }
      }
   }

   /**
    * Removes the index files.
    * 
    * @throws IOException If an internal method throws it.
    */
   void remove() throws IOException 
   {
      fnodes.f.delete();
      if (fvalues != null)
         fvalues.f.delete();
   }

   /**
    * Closes the index files.
    * 
    * @throws IOException If an internal method throws it.
    */
   void close() throws IOException
   {
      fnodes.finalPos = nodeCount * nodeRecSize; // Calculated the used space; the file will have no zeros at the end.
      fnodes.close();
      if (fvalues != null)
         fvalues.close();
   }

   /**
    * Empties the index files, since the rows were deleted.
    *
    * @throws IOException If an internal method throws it.
    */
   void deleteAllRows() throws IOException
   {
      // It is faster truncating a file than re-creating it again. 
      fnodes.growTo(0);
      fnodes.finalPos = fnodes.pos = fnodes.size = 0;
      fnodes.cacheIsDirty = false;
      if (fvalues != null)
      {
         fvalues.growTo(0);
         fvalues.finalPos = fvalues.pos = fvalues.size = 0;
         fvalues.cacheIsDirty = false;
      }
     
      isEmpty = true;
      int i = cache.length;
      while (--i >= 0)
         if (cache[i] != null)
            cache[i].idx = -1;
      cacheI = nodeCount = 0; // juliana@220_6: The node count should be reseted when recreating the indices.
   }

   /** 
    * Delays the write to disk, caching them at memory. 
    * 
    * @param delayed Indicates if the writing process is to be done later or not.
    * @throws IOException If an internal method throws it.
    */
   void setWriteDelayed(boolean delayed) throws IOException
   {
      int i = INDEX_CACHE_SIZE;

      root.setWriteDelayed(delayed); // Commits pending keys.
      while (--i >= 0)
         if (cache[i] != null)
            cache[i].setWriteDelayed(delayed);

      if (!delayed) // Shrinks the values.
         fnodes.growTo(nodeCount * nodeRecSize);
   }

   /**
    * Adds a key to an index.
    *
    * @param values The key to be inserted.
    * @param record The record of the key in the table.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void indexAddKey(SQLValue[] values, int record) throws IOException, InvalidDateException
   {
      Value tempVal = table.tempVal1; // juliana@224_2: improved memory usage on BlackBerry.
      
      tempKey.set(values); // Sets the key.
      tempVal.record = record; // Sets the record.
      tempVal.next = Value.NO_MORE; // There's no repeated key.
      
      // Inserts the key.
      boolean splitting = false;
      if (isEmpty)
      {
         tempKey.addValue(tempVal, root.isWriteDelayed);
         root.set(tempKey, Node.LEAF, Node.LEAF);
         root.save(true, 0, root.size);
         isEmpty = false;
      }
      else
      {
         Node curr = root;
         Key keyFound;
         int[] types = tempKey.index.types;
         SQLValue[] keys = tempKey.keys;
         int nodeCounter = nodeCount,
             pos;
         IntVector ancestors = root.index.table.ancestors; // juliana@224_2: improved memory usage on BlackBerry.
         
         while (true)
         {
            keyFound = curr.keys[pos = curr.findIn(tempKey, true)]; // juliana@201_3
            if (pos < curr.size && Utils.arrayValueCompareTo(keys, keyFound.keys, types) == 0)
            {
               keyFound.addValue(tempVal, curr.isWriteDelayed);  // Adds the repeated key to the currently stored one.
               curr.saveDirtyKey(pos); // Key was dirty - save just it.
               break;
            }
            else
            if (curr.children[0] == Node.LEAF)
            {
               // If the node will becomes full, the insert is done again, this time keeping track of the ancestors. Note: with k = 50 and 200000 
               // values, there are about 1.1 million useless pushes without this redundant insert.
               if (!splitting && curr.size == btreeMaxNodes - 1)
               {
                  splitting = true;
                  curr = root;
                  ancestors.removeAllElements();
                  nodeCounter = nodeCount;
               }
               else
               {
                  tempKey.addValue(tempVal, curr.isWriteDelayed);
                  curr.insert(tempKey, Node.LEAF, Node.LEAF, pos);
                  curr.saveDirtyKey(pos);
                  if (splitting) // Curr has overflown.
                     splitNode(curr);
                  break;
               }
            }
            else
            {
               if (splitting)
               {
                  ancestors.push(pos);
                  ancestors.push(curr.idx);
               }
               
               if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
                  throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
               curr = loadNode(curr.children[pos]);
            }
         }
      }
   }

}
