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
    * Indicates if the write of the node is delayed.
    */
   boolean isWriteDelayed;

   /**
    * The table of the index.
    */
   Table table;
   
   /**
    * A vector for climbing on index nodes.
    */
   Vector nodes = new Vector(10);

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
      }
      // Creates the root node.
      root = new Node(this);
      root.idx = 0;

      if (!(isEmpty = fnodes.size == 0))
         root.load();

      // juliana@213_8: the index node count was not being loaded when loading the indices, which could cause an infinite loop when using them.
      nodeCount = fnodes.size / nodeRecSize;  
      
      tempKey = new Key(this); // Creates the temp key.
   }

   /**
    * Removes a value from the index.
    *
    * @param key The key to be removed.
    * @param record The repeated value record index.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void removeValue(Key key, int record) throws IOException, InvalidDateException, DriverException
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
            keyFound = curr.keys[pos = curr.findIn(key, false)]; // juliana@201_3 // Finds the key position.

            if (pos < curr.size && Utils.arrayValueCompareTo(keys, keyFound.keys, types) == 0) 
            {
               switch (keyFound.remove(record)) // Tries to remove the key.  
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
      
      Node[] cacheAux = cache;
      
      // Loads the cache.
      int j = INDEX_CACHE_SIZE;
      while (--j >= 0)  
         if (cacheAux[j] != null && cacheAux[j].idx == idx)
            return cacheAux[cacheI = j];
      
      if (++cacheI >= INDEX_CACHE_SIZE)
         cacheI = 0;
      Node cand = cacheAux[cacheI];
      if (cand == null)
         cand = cacheAux[cacheI] = new Node(this);
         
      if (isWriteDelayed && cand.isDirty) // Saves this one if it is dirty.
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
            keyFound = curr.keys[pos = curr.findIn(key, false)]; // juliana@201_3
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
         while (!stop && ++start < size)
            stop = !monkey.onKey(keys[start]);
      else
      {
         Node curr,
              loaded;
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
            if ((loaded = getLoadedNode(children[start])) == null)
            {
               (loaded = curr).idx = children[start];
               curr.load();
            }
            stop = climbGreaterOrEqual(loaded, nodes, -1, monkey, stop);
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
    * @param markBits The bitmap that represents all the table rows.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void getGreaterOrEqual(MarkBits markBits) throws IOException, InvalidDateException, DriverException
   {
      if (!isEmpty)
      {
         int pos,
             nodeCounter = nodeCount;
         IntVector iv = new IntVector(10);
         Node curr = root; // Starts from the root.
         Key left = markBits.leftKey;
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
            boolean stop;
            Vector vector = nodes;
            
            while (iv.size() > 0)
            {
               stop = false;
               try
               {
                  pos = iv.pop();
                  curr = loadNode(iv.pop());
               }
               catch (ElementNotFoundException exception) {}
               if ((stop = climbGreaterOrEqual(curr, vector, pos, markBits, stop)))
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
      Key keyFound,
          keyAux = tempKey;
      Node rootAux = root;
      IntVector ancestors = curr.index.table.ancestors; // juliana@224_2: improved memory usage on BlackBerry.
      
      // guich@110_3: curr.size * 3/4 - note that medPos never changes, because the node is always split when the same size is reached.
      int medPos = curr.index.isOrdered? (curr.size - 1) : (curr.size / 2);
  
      while (true)
      {
         keyFound = curr.keys[medPos];
         keyAux.set(keyFound.keys);
         keyAux.valRec = keyFound.valRec;

         // Right sibling - must be the first one to save!
         right = curr.save(true, medPos + 1, curr.size);

         if (curr.idx == 0)  // Is it the root?
         {
            left = curr.save(true, 0, medPos); // Left sibling.
            rootAux.save(false, 0, rootAux.size); // juliana@114_3: fixed the index saving. When the root node was splitted, it was not being saved.
            rootAux.set(keyAux, left, right); // Replaces the root record.
            rootAux.save(false, 0, rootAux.size);
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

            curr.insert(keyAux, left, right, ins);
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
      NormalFile fnodesAux = fnodes;
      NormalFile fvaluesAux = fvalues;
      Node[] cacheAux = cache;
      
      fnodesAux.growTo(0);
      fnodesAux.finalPos = fnodesAux.pos = fnodesAux.size = 0;
      fnodesAux.cacheIsDirty = false;
      if (fvaluesAux != null)
      {
         fvaluesAux.growTo(0);
         fvaluesAux.finalPos = fvaluesAux.pos = fvaluesAux.size = 0;
         fvaluesAux.cacheIsDirty = false;
      }
     
      isEmpty = true;
      int i = INDEX_CACHE_SIZE;
      while (--i >= 0)
         if (cacheAux[i] != null)
            cacheAux[i].idx = -1;
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
      Node[] cacheAux = cache;

      root.setWriteDelayed(delayed); // Commits pending keys.
      while (--i >= 0)
         if (cacheAux[i] != null)
            cacheAux[i].setWriteDelayed(delayed);

      if (!delayed) // Shrinks the values.
         fnodes.growTo(nodeCount * nodeRecSize);
      isWriteDelayed = delayed;
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
      Key keyAux = tempKey;
      Node rootAux = root;
      
      keyAux.set(values); // Sets the key.
      
      // Inserts the key.
      boolean splitting = false;
      if (isEmpty)
      {
         keyAux.addValue(record, isWriteDelayed);
         rootAux.set(keyAux, Node.LEAF, Node.LEAF);
         rootAux.save(true, 0, rootAux.size);
         isEmpty = false;
      }
      else
      {
         Node curr = rootAux;
         Key keyFound;
         int[] types = keyAux.index.types;
         SQLValue[] keys = keyAux.keys;
         int nodeCountAux = nodeCount,
             nodeCounter = nodeCountAux,
             maxSize = btreeMaxNodes - 1,
             pos;
         boolean isDelayed = isWriteDelayed;
         IntVector ancestors = rootAux.index.table.ancestors; // juliana@224_2: improved memory usage on BlackBerry.
         
         while (true)
         {
            keyFound = curr.keys[pos = curr.findIn(keyAux, true)]; // juliana@201_3
            if (pos < curr.size && Utils.arrayValueCompareTo(keys, keyFound.keys, types) == 0)
            {
               keyFound.addValue(record, isDelayed);  // Adds the repeated key to the currently stored one.
               curr.saveDirtyKey(pos); // Key was dirty - save just it.
               break;
            }
            else
            if (curr.children[0] == Node.LEAF)
            {
               // If the node will becomes full, the insert is done again, this time keeping track of the ancestors. Note: with k = 50 and 200000 
               // values, there are about 1.1 million useless pushes without this redundant insert.
               if (!splitting && curr.size == maxSize)
               {
                  splitting = true;
                  curr = rootAux;
                  ancestors.removeAllElements();
                  nodeCounter = nodeCountAux;
               }
               else
               {
                  keyAux.addValue(record, isDelayed);
                  curr.insert(keyAux, Node.LEAF, Node.LEAF, pos);
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

   // juliana@230_21: MAX() and MIN() now use indices on simple queries.   
   /**
    * Finds the minimum value of an index in a range.
    *
    * @param sqlValue The minimum value inside the given range to be returned.
    * @param bitMap The table bitmap wich indicates which rows will be in the result set.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it. 
    */
   void findMinValue(SQLValue sqlValue, IntVector bitMap) throws IOException, InvalidDateException
   {
      try
      {
         Node curr;
         IntVector vector = new IntVector(nodeCount);
         int size,
             i = -1,
             valRec,
             nodeCounter = nodeCount << 1;
         Value tempVal = table.tempVal; // juliana@224_2: improved memory usage on BlackBerry.
         NormalFile fvaluesAux = fvalues;
         byte[] valueBuf = table.valueBuf;
         
         // Recursion using a stack.
         vector.push(root.idx);
         while (true)
         {
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(vector.pop());
            
            // Searches for the smallest key of the node marked in the result set or is not deleted. 
            size = curr.size;
            
            if (bitMap == null)
            {
               while (++i < size)
                  if (curr.keys[i].valRec != Key.NO_VALUE)
                  {
                     curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                     break;
                  }
            }
            else  
               while (++i < size)
                  if ((valRec = curr.keys[i].valRec) < 0)
                  {
                     if (bitMap.isBitSet(-1 - valRec))
                     {
                        curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                        break;
                     }
                  }
                  else if (valRec != Key.NO_VALUE)
                  {
                     while (valRec != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
                     {
                        fvaluesAux.setPos(Value.VALUERECSIZE * valRec);
                        tempVal.load(fvaluesAux, valueBuf);
                        if (bitMap.isBitSet(tempVal.record))
                        {
                           curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                           break;
                        }
                        valRec = tempVal.next;
                     }
                     if (valRec != Value.NO_MORE)
                        break;
                  }
            
            // Now searches the children nodes whose keys are smaller than the one marked or all of them if no one is marked. 
            i++;   
            while (--i >= 0)
               if (curr.children[i] != Node.LEAF)
                  vector.push(curr.children[i]);
         }
      }
      catch (ElementNotFoundException exception) {}
      
      if (sqlValue.isNull) // No record found.
         return;
      
      loadString(sqlValue);
   }
   
   /**
    * Finds the maximum value of an index in a range.
    *
    * @param bitMap The table bitmap wich indicates which rows will be in the result set.
    * @param sqlValue The maximum value inside the given range to be returned.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.  
    */
   void findMaxValue(SQLValue sqlValue, IntVector bitMap) throws IOException, InvalidDateException
   {
      try
      {
         Node curr;
         IntVector vector = new IntVector(nodeCount);
         int size,
             i = -1,
             valRec,
             nodeCounter = nodeCount << 1;
         Value tempVal = table.tempVal; // juliana@224_2: improved memory usage on BlackBerry.
         NormalFile fvaluesAux = fvalues;
         byte[] valueBuf = table.valueBuf;
         
         // Recursion using a stack.
         vector.push(root.idx);
         while (true)
         {
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(vector.pop());
            
            // Searches for the greatest key of the node marked in the result set or is not deleted. 
            i = size = curr.size;
            
            if (bitMap == null)
            {
               while (--i >= 0)
                  if (curr.keys[i].valRec != Key.NO_VALUE)
                  {
                     curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                     break;
                  }
            }
            else  
               while (--i >= 0)
                  if ((valRec = curr.keys[i].valRec) < 0)
                  {
                     if (bitMap.isBitSet(-1 - valRec))
                     {
                        curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                        break;
                     }
                  }
                  else if (valRec != Key.NO_VALUE)
                  {
                     while (valRec != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
                     {
                        fvaluesAux.setPos(Value.VALUERECSIZE * valRec);
                        tempVal.load(fvaluesAux, valueBuf);
                        if (bitMap.isBitSet(tempVal.record))
                        {
                           curr.keys[i].keys[0].cloneSQLValue(sqlValue);
                           break;
                        }
                        valRec = tempVal.next;
                     }
                     if (valRec != Value.NO_MORE)
                        break;
                  }
            
            // Now searches the children nodes whose keys are greater than the one marked or all of them if no one is marked.    
            while (++i <= size && curr.children[i] != Node.LEAF)
               vector.push(curr.children[i]);
         }
      }
      catch (ElementNotFoundException exception) {}
      
      if (sqlValue.isNull) // No record found.
         return;
      loadString(sqlValue);
   }
   
   /**
    * Loads a string from the table if needed.
    * 
    * @param sqlValue The record structure which will hold (holds) the string.
    * @throws IOException If an internal method throws it.
    */
   private void loadString(SQLValue sqlValue) throws IOException
   {
      sqlValue.asLong = Utils.subStringHashCode(table.name, 5);
      
      // If the type is string and the value is not loaded, loads it.
      if ((types[0] == SQLElement.CHARS || types[0] == SQLElement.CHARS_NOCASE) && sqlValue.asString == null)
         sqlValue.asString = table.db.loadString();
   }
   
   /**
    * Returns a node already loaded or loads it if there is empty space in the cache node to avoid loading already loaded nodes.
    * 
    * @param idx The node index.
    * @return The loaded node, a new cache node with the requested node loaded, or <code>null</code> if it is not already loaded or its cache is
    * full.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private Node getLoadedNode(int idx) throws IOException, InvalidDateException
   {
      int i = -1;
      Node[] cacheAux = cache;
      Node node;
   
      while (++i < INDEX_CACHE_SIZE && cacheAux[i] != null) // Tries to get an already loaded node.
         if (cacheAux[i].idx == idx)
            return cacheAux[cacheI = i];   
      
      if (i < INDEX_CACHE_SIZE) // Loads the node if there is enough space in the node cache.
      {
         node = cacheAux[cacheI = i] = new Node(this);
         node.idx = idx;
         node.load();
         return node;
      }
      
      return null;
   }
   
   /**
    * Sorts the records of a table into a temporary table using an index in the ascending order.
    * 
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param tableOrig The original table used in the query.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it. 
    */
   void sortRecordsAsc(IntVector bitMap, Table tempTable, SQLValue[] record, int[] columnIndexes, SQLSelectClause clause) 
                                                                                                  throws InvalidDateException, IOException
   {
      try
      {
         Node curr;
         IntVector vector = new IntVector(nodeCount);
         Table tableAux = table;
         Value tempVal = tableAux.tempVal; // juliana@224_2: improved memory usage on BlackBerry.
         NormalFile fvaluesAux = fvalues;
         Key[] keys;
         byte[] valueBuf = tableAux.valueBuf;
         short[] children;
         int size,
             valRec,
             child,
             nodeCounter = nodeCount << 1;
         
         // Recursion using a stack.
         vector.push(Key.NO_VALUE);
         vector.push(root.idx);
         while (true)
         {
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            
            // Gets the key and child.
            child = vector.pop();
            valRec = vector.pop();
            
            // The child is smaller, so its keys are evaluated first, from the first to the last.
            if (child != Node.LEAF)
            {
               size = (curr = loadNode(child)).size;
               children = curr.children;
               keys = curr.keys;
               vector.push(Key.NO_VALUE);
               vector.push(children[size]);
               while (--size >= 0)
               {
                  vector.push(keys[size].valRec);
                  vector.push(children[size]);
               }
            }
            
            // Then evaluates the key to see if it is in the result set and puts its record in the correct order.
            
            // No repeated value, just cheks the record.
            if (valRec < 0 && (bitMap == null || bitMap.isBitSet(-1 - valRec)))
               writeRecord(-1 -valRec, tempTable, record, columnIndexes, clause);
            else if (valRec != Key.NO_VALUE) // Checks all the repeated values if the value was not deleted.
               while (valRec != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
               {
                  fvaluesAux.setPos(Value.VALUERECSIZE * valRec);
                  tempVal.load(fvaluesAux, valueBuf);
                  if (bitMap == null || bitMap.isBitSet(tempVal.record))
                     writeRecord(tempVal.record, tempTable, record, columnIndexes, clause);
                  valRec = tempVal.next;
               }
         } 
      }
      catch (ElementNotFoundException exception) {}
   }
   
   /**
    * Sorts the records of a table into a temporary table using an index in the descending order.
    * 
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param tempTable The temporary table for the result set.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.
    */
   void sortRecordsDesc(IntVector bitMap, Table tempTable, SQLValue[] record, int[] columnIndexes, SQLSelectClause clause) 
                                                                                                   throws InvalidDateException, IOException
   {
      try
      {
         Node curr;
         IntVector vector = new IntVector(nodeCount);
         Table tableAux = table;
         Value tempVal = tableAux.tempVal; // juliana@224_2: improved memory usage on BlackBerry.
         NormalFile fvaluesAux = fvalues;
         Key[] keys;
         byte[] valueBuf = tableAux.valueBuf;
         short[] children;
         int size,
             i,
             valRec,
             child,
             nodeCounter = nodeCount << 1;
         
         // Recursion using a stack.
         vector.push(Key.NO_VALUE);
         vector.push(root.idx);
         while (true)
         {
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            
            // Gets the key and child.
            child = vector.pop();
            valRec = vector.pop();
            
            // The record is greater than the child, so evaluates the key to see if it is in the result set and puts its record in the correct order.
            
            // No repeated value, just cheks the record.
            if (valRec < 0 && (bitMap == null || bitMap.isBitSet(-1 - valRec)))
               writeRecord(-1 -valRec, tempTable, record, columnIndexes, clause);
            else if (valRec != Key.NO_VALUE) // Checks all the repeated values if the value was not deleted.
               while (valRec != Value.NO_MORE) // juliana@224_2: improved memory usage on BlackBerry.
               {
                  fvaluesAux.setPos(Value.VALUERECSIZE * valRec);
                  tempVal.load(fvaluesAux, valueBuf);
                  if (bitMap == null || bitMap.isBitSet(tempVal.record))
                     writeRecord(tempVal.record, tempTable, record, columnIndexes, clause);
                  valRec = tempVal.next;
               }
            
            // Then the child keys are evaluated, from the last to the first.
            if (child != Node.LEAF)
            {
               size = (curr = loadNode(child)).size;
               children = curr.children;
               keys = curr.keys;
               i = -1;
               
               while (++i < size)
               {
                  vector.push(keys[i].valRec);
                  vector.push(children[i]);
               }
               vector.push(Key.NO_VALUE);
               vector.push(children[size + 1]);
            }
            
         } 
      }
      catch (ElementNotFoundException exception) {}
   }
   
   /**
    * Reads from the selected record from the table and writes the necessary fields in the temporary table.
    * 
    * @param pos The position of the selected record.
    * @param tempTable The temporary table for the result set.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void writeRecord(int pos, Table tempTable, SQLValue[] record, int[] columnIndexes, SQLSelectClause clause) 
                                                                                      throws IOException, InvalidDateException
   {
      Table tableAux = table;
      byte[] tempNulls = tempTable.columnNulls[0];
      byte[] origNulls = tableAux.columnNulls[0];
      int[] offsets = tableAux.columnOffsets;
      int[] types = tableAux.columnTypes;
      int i = tempTable.columnCount,
          colIndex;
      boolean isNull;
      
      tableAux.db.read(pos); // Reads the record.
      tableAux.readNullBytesOfRecord(0, false, 0); // Reads the bytes of the nulls.
      
      while (--i >= 0) // Reads the fields for the temporary table.
      {
         colIndex = columnIndexes[i];
         if (!(isNull = (origNulls[colIndex >> 3] & (1 << (colIndex & 7))) != 0))
            tableAux.readValue(record[i], offsets[colIndex], types[colIndex], isNull, true);

         Utils.setBit(tempNulls, i, isNull); // Sets the null values for tempTable.
      } 
      tempTable.writeRSRecord(record); // Writes the temporary table record.
   }
}
