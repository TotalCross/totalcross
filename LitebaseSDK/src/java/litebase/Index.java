// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package litebase;

import totalcross.io.*;
import totalcross.util.*;

// juliana@253_5: removed .idr files from all indices and changed its format. 
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
    * The current number of nodes in the nodes array.
    */
   int nodesArrayCount;

   /**
    * The sizes of the columns of the index.
    */
   int[] colSizes;

   /**
    * The types of the columns of the index.
    */
   byte[] types;

   // juliana@253_6: The maximum number of keys of a index was duplicated.
   /**
    * The cache of the index.
    */
   private Node[] cache = new Node[INDEX_CACHE_SIZE]; // Creates the cache.;
   
   /**
    * The first level of the index B-tree.
    */
   Node[] firstLevel; // juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.

   /**
    * The name of the index table.
    */
   String name;

   /**
    * The nodes file.
    */
   NormalFile fnodes;

   /**
    * A stream to be used to save and load data from the index.
    */
   ByteArrayStream bas;

   /**
    * A stream to be used to save and load data from the index.
    */
   DataStreamLB basds;

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
    * An array for climbing on index nodes.
    */
   private Node[] nodes = new Node[4];

   /**
    * Constructs an index structure.
    *
    * @param aTable The table of the index.
    * @param keyTypes The types of the columns of the index.
    * @param newColSizes The column sizes.
    * @param aName The name of the index table.
    * @param sourcePath The path of the index files.
    * @param exist Indicates that the index files already exist. 
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   Index(Table aTable, byte[] keyTypes, int[] newColSizes, String aName, String sourcePath, boolean exist) 
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
      basbuf = (bas = new ByteArrayStream(nodeRecSize)).getBuffer();
      basds = new DataStreamLB(bas, aTable.db.useCrypto);

      firstLevel = new Node[btreeMaxNodes]; // Creates the first index level. // juliana@230_35

      // Creates the index files.
      String fullFileName = Utils.getFullFileName(name, sourcePath);
      fnodes = new NormalFile(fullFileName + ".idk", !exist, nodeRecSize);
      
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
             nodeCounter = nodeCount,
             firstChild,
             size,
             count = 0;
         byte[] typesAux = types;
         SQLValue[] keys = key.keys;
         int[] children;
         Key[] currKeys;
         Key keyFound;
         PlainDB plainDB = table.db;
         int[] vector = plainDB.driver.nodes;
         
         while (true)
         {
            keyFound = (currKeys = curr.keys)[pos = curr.findIn(key, false)]; // juliana@201_3 // Finds the key position.
            firstChild = (children = curr.children)[0];
            
            if (pos < (size = curr.size) && Utils.arrayValueCompareTo(keys, keyFound.keys, typesAux, plainDB) == 0) 
            {
               while (pos >= 0 && Utils.arrayValueCompareTo(keys, (keyFound = currKeys[pos]).keys, typesAux, plainDB) == 0 
                   && (keyFound.record >= record || keyFound.record == Key.NO_VALUE))
                  pos--;
               while (++pos < size && Utils.arrayValueCompareTo(keys, (keyFound = currKeys[pos]).keys, typesAux, plainDB) == 0 
                   && (keyFound.record <= record || keyFound.record == Key.NO_VALUE))
               {
                  if (record == keyFound.record)
                  {
                     keyFound.record = Key.NO_VALUE; // Tries to remove the key.  
                     curr.saveDirtyKey(pos);
                     return;
                  }
                  
                  if (keyFound.record == Key.NO_VALUE && firstChild != Node.LEAF)
                     vector[count++] = children[pos];
               }
            }

            // If there are children, load them if the key was not found yet.
            if (firstChild != Node.LEAF)
               vector[count++] = children[pos];
               
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop. 
              throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));

            if (count > 0)
               curr = loadNode(vector[--count]);
            else
               break;
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
      
      Node cand;
      
      // juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.
      // Tries to find the node in the nodes of the first level.
      if (idx <= btreeMaxNodes)
      {
         if ((cand = firstLevel[idx - 1]) == null)
         {
            (cand = firstLevel[idx - 1] = new Node(this)).idx = idx;
            cand.load();
         }
         else if (cand.idx == -1)
         {
            cand.idx = idx;
            cand.load();
         }
         return cand;
      }

      // Loads the cache if the node is in a deeper level.
      Node[] cacheAux = cache;
      int j = INDEX_CACHE_SIZE;
      while (--j >= 0)  
         if (cacheAux[j] != null && cacheAux[j].idx == idx)
            return cacheAux[cacheI = j];
      
      if (++cacheI >= INDEX_CACHE_SIZE)
         cacheI = 0;
      if ((cand = cacheAux[cacheI]) == null)
         cand = cacheAux[cacheI] = new Node(this);
         
      if (isWriteDelayed && cand.isDirty) // Saves this one if it is dirty.
         cand.save(false, 0, cand.size);

      // Loads the node.
      cand.idx = idx;
      cand.load();
      return cand;
   }

   /**
    * Finds the given key and marks the records that are going to the result set.
    *
    * @param key The key to be found.
    * @param markBits The rows which will be returned to the result set.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws DriverException If the index is corrupted.
    */
   void getValue(Key key, MarkBits markBits) throws IOException, InvalidDateException, DriverException
   {
      if (!isEmpty)
      {
         Node curr = root; // 0 is always the root.
         Key keyFound;
         byte[] typesAux = types;
         SQLValue[] keys = key.keys;
         Key[] currKeys;
         int[] children;
         PlainDB plainDB = table.db;
         int pos,
             nodeCounter = nodeCount,
             firstChild,
             size,
             count = 0;
         int[] vector = plainDB.driver.nodes;
                
         while (true)
         {
            keyFound = (currKeys = curr.keys)[pos = curr.findIn(key, false)]; // juliana@201_3
            firstChild = (children = curr.children)[0];
            
            // juliana@284_2: solved a possible insertion of a duplicate value in a PK.
            if (pos < (size = curr.size) && Utils.arrayValueCompareTo(keys, keyFound.keys, typesAux, plainDB) == 0)
            {
               if (markBits == null) // Only checks primary key violation.
                  if (keyFound.record != Key.NO_VALUE)
                     throw new PrimaryKeyViolationException(LitebaseMessage.getMessage(LitebaseMessage.ERR_STATEMENT_CREATE_DUPLICATED_PK) 
                                                          + table.name);
               do
                  pos--;
               while (pos >= 0 && Utils.arrayValueCompareTo(keys, currKeys[pos].keys, typesAux, plainDB) == 0);                  
               while (++pos < size && Utils.arrayValueCompareTo(keys, currKeys[pos].keys, typesAux, plainDB) == 0)
               {
                  if (markBits != null)
                     markBits.onKey(currKeys[pos]);
                  if (firstChild != Node.LEAF)
                     vector[count++] = children[pos];
               }               
            }

            if (firstChild != Node.LEAF)
               vector[count++] = children[pos];
            
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
              throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
               
            if (count > 0)
               curr = loadNode(vector[--count]);
            else
               break;
         }
      }
   }

   /**
    * Climbs on the nodes that are greater or equal than the current one.
    *
    * @param node The node to be compared with.
    * @param start The first key of the node to be searched.
    * @param markBits The rows which will be returned to the result set.
    * @param stop Indicates when the climb process can be finished.
    * @return If it has to stop the climbing process or not.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private boolean climbGreaterOrEqual(Node node, int start, MarkBits markBits, boolean stop) throws IOException, InvalidDateException
   {
      int size = node.size;
      Key[] keys = node.keys;
      int[] children = node.children;
      if (start >= 0)
         stop = !markBits.onKey(keys[start]);
      if (children[0] == Node.LEAF)
         while (!stop && ++start < size)
            stop = !markBits.onKey(keys[start]);
      else
      {
         Node curr,
              loaded;

         if (nodesArrayCount > 0)
            curr = nodes[--nodesArrayCount];
         else
            curr = new Node(node.index); 

         while (!stop && ++start <= size)
         {
            if ((loaded = getLoadedNode(children[start])) == null)
            {
               (loaded = curr).idx = children[start];
               curr.load();
            }
            stop = climbGreaterOrEqual(loaded, -1, markBits, stop);
            if (start < size && !stop)
               stop = !markBits.onKey(keys[start]);
         }
         nodes[nodesArrayCount++] = curr;
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
             nodeCounter = nodeCount,
             r,
             count = 0;
         PlainDB plainDB = table.db;             
         int[] ancestors = plainDB.driver.nodes;
         Node curr = root; // Starts from the root.
         Key left = markBits.leftKey;
         SQLValue[] leftKeys = left.keys;
         Key[] currKeys;
         int[] children;
         byte[] typesAux = types;
         
         while (true)
         {
            children = curr.children;
            currKeys = curr.keys;
            
            if ((pos = curr.findIn(left, false)) < curr.size) // juliana@201_3
            {
               // Compares left keys with curr keys.
               // If this value is above or equal to the one being looked for, stores it.               
               while (--pos >= 0 && Utils.arrayValueCompareTo(leftKeys, currKeys[pos].keys, typesAux, plainDB) == 0);                  
               if ((r = Utils.arrayValueCompareTo(leftKeys, currKeys[++pos].keys, typesAux, plainDB)) <= 0) 
               {
                  ancestors[count++] = curr.idx;
                  ancestors[count++] = pos;
               }
               else if (r >= 0) // left >= curr.keys[pos] ?
                  break;
            }
            if (children[0] == Node.LEAF)
               break;
            
            if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
            curr = loadNode(children[pos]);
         }
         if (count > 0)
         {
            boolean stop;
            
            while (count > 0)
            {
               stop = false;
               pos = ancestors[--count];              
               if ((stop = climbGreaterOrEqual(curr = loadNode(ancestors[--count]), pos, markBits, stop)))
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
    * @param count The number of elements in the ancestors array.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private void splitNode(Node curr, int count) throws IOException, InvalidDateException
   {
      int left, 
          right,
          ins;
      Key keyFound,
          keyAux = tempKey;
      Node rootAux = root;
      int[] ancestors = table.db.driver.nodes; // juliana@224_2: improved memory usage on BlackBerry.
      
      // guich@110_3: curr.size * 3/4 - note that medPos never changes, because the node is always split when the same size is reached.
      // juliana@283_1: solved a bug which would buid corrupted indices when creating or recreating them.
      int medPos = curr.index.isOrdered? (curr.size - 2) : (curr.size / 2);
  
      while (true)
      {
         keyFound = curr.keys[medPos];
         keyAux.set(keyFound.keys);
         keyAux.record = keyFound.record;

         // Right sibling - must be the first one to save!
         right = curr.save(true, medPos + 1, curr.size);
         
         if (curr.idx == 0)  // Is it the root?
         {
            left = curr.save(true, 0, medPos); // Left sibling.
            rootAux.set(keyAux, left, right); // Replaces the root record.
            rootAux.save(false, 0, rootAux.size);
            break;
         }
         else // guich@110_4: reuses this node; cut it at medPos.
         {
            left = curr.idx;
            curr.save(false, 0, curr.size = medPos);
            ins = 0;
            if (count >= 0) // Parent insert position.
            {
               curr = loadNode(ancestors[--count]); // Loads the parent.
               ins = ancestors[--count];
            }

            curr.insert(keyAux, left, right, ins);
            if (curr.size < btreeMaxNodes) // Parent has not overflown?
               break;
         }
      }
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
      Node[] cacheAux = cache;
      Node[] firstLevelAux = firstLevel;
      
      fnodesAux.growTo(0);
      fnodesAux.finalPos = fnodesAux.pos = fnodesAux.size = 0;
      fnodesAux.cacheIsDirty = false;
     
      isEmpty = true;
      int i = INDEX_CACHE_SIZE;
      while (--i >= 0) // Erases the cache.
         if (cacheAux[i] != null)
            cacheAux[i].idx = -1;
      
      i = btreeMaxNodes;
      while (--i >= 0) // Erases the first level nodes.
         if (firstLevelAux[i] != null)
            firstLevelAux[i].idx = -1;
      
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
      root.setWriteDelayed(delayed); // Commits pending keys.
      
      // juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.
      // Commits the pending first level nodes.
      int i = btreeMaxNodes;
      Node[] nodes = firstLevel;
      while (--i >= 0)
         if (nodes[i] != null)
            nodes[i].setWriteDelayed(delayed);
      
      // Commits the pending cache nodes.
      i = INDEX_CACHE_SIZE;
      nodes = cache;
      while (--i >= 0)
         if (nodes[i] != null)
            nodes[i].setWriteDelayed(delayed);

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
         keyAux.record = record;
         rootAux.set(keyAux, Node.LEAF, Node.LEAF);
         rootAux.save(true, 0, 1);
         isEmpty = false;
      }
      else
      {
         Node curr = rootAux;
         Key keyFound;
         byte[] typesAux = types;
         int[] children;
         SQLValue[] keys = keyAux.keys;
         Key[] currKeys;         
         PlainDB plainDB = table.db;
         int nodeCountAux = nodeCount,
             nodeCounter = nodeCountAux,
             maxSize = btreeMaxNodes - 1,
             pos,
             size,         
             count = 0;
         int[] ancestors = plainDB.driver.nodes; // juliana@224_2: improved memory usage on BlackBerry.
         
         while (true)
         {
            keyFound = (currKeys = curr.keys)[pos = curr.findIn(keyAux, true)]; // juliana@201_3
            children = curr.children;
            
            if (pos < (size = curr.size) && Utils.arrayValueCompareTo(keys, keyFound.keys, typesAux, plainDB) == 0)
            {
               // juliana@281_1: corrected a possible index corruption.
               while (pos >= 0 && Utils.arrayValueCompareTo(keys, (keyFound = currKeys[pos]).keys, typesAux, plainDB) == 0 
                   && (keyFound.record >= record || keyFound.record == Key.NO_VALUE))
                  pos--;
               while (++pos < size && Utils.arrayValueCompareTo(keys, (keyFound = currKeys[pos]).keys, typesAux, plainDB) == 0 
                   && (keyFound.record < record || keyFound.record == Key.NO_VALUE));
            }

            if (children[0] == Node.LEAF)
            {
               // If the node will becomes full, the insert is done again, this time keeping track of the ancestors. Note: with k = 50 and 200000 
               // values, there are about 1.1 million useless pushes without this redundant insert.
               if (!splitting && curr.size == maxSize)
               {
                  splitting = true;
                  curr = rootAux;
                  count = 0;
                  nodeCounter = nodeCountAux;
               }
               else
               {
                  keyAux.record = record;
                  curr.insert(keyAux, Node.LEAF, Node.LEAF, pos);
                  curr.saveDirtyKey(pos);
                  if (splitting) // Curr has overflown.
                     splitNode(curr, count);
                  break;
               }
            }
            else
            {
               if (splitting)
               {
                  ancestors[count++] = pos;
                  ancestors[count++] = curr.idx;
               }
               
               if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
                  throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
               curr = loadNode(children[pos]);
            }
         }
      }
   }

   // juliana@230_21: MAX() and MIN() now use indices on simple queries.   
   /**
    * Finds the minimum value of an index in a range.
    *
    * @param sqlValue The minimum value inside the given range to be returned.
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it. 
    */
   void findMinValue(SQLValue sqlValue, IntVector bitMap) throws IOException, InvalidDateException
   {
      Node curr;
      Key[] currKeys;
      Key currKey;      
      int size,
          i,
          nodeCounter = nodeCount + 1,
          count = 1;
      int[] vector = table.db.driver.nodes;
      int[] children;
      
      // juliana@224_2: improved memory usage on BlackBerry.
      
      // Recursion using a stack. The array sole element is 0.
      vector[0] = 0;
      while (count > 0)
      {
         if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
         curr = loadNode(vector[--count]);
         
         // Searches for the smallest key of the node marked in the result set or is not deleted. 
         size = curr.size;         
         i = -1;
         currKeys = curr.keys;
         children = curr.children;
         
         while (++i < size)
            if ((currKey = currKeys[i]).record != Key.NO_VALUE && (bitMap == null || bitMap.isBitSet(currKey.record)))
            {                  
               currKey.keys[0].cloneSQLValue(sqlValue);
               count = 0; // juliana@284_3: solved a possible wrong result in MAX() and MIN() if the column searched had an index.
               break;                  
            }
         
         // Now searches the children nodes whose keys are smaller than the one marked or all of them if no one is marked. 
         i++;   
         if (children[0] != Node.LEAF)
            while (--i >= 0)            
               vector[count++] = children[i];
      }
      
      if (sqlValue.isNull) // No record found.
         return;
      
      loadString(sqlValue);
   }
   
   /**
    * Finds the maximum value of an index in a range.
    *
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param sqlValue The maximum value inside the given range to be returned.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.  
    */
   void findMaxValue(SQLValue sqlValue, IntVector bitMap) throws IOException, InvalidDateException
   {
      Node curr;
      Key[] currKeys;
      Key currKey;      
      int size,
          i,
          count = 1,
          nodeCounter = nodeCount + 1;
      int[] vector = table.db.driver.nodes;
      int[] children;
      
      // juliana@224_2: improved memory usage on BlackBerry.
      
      // Recursion using a stack. The array sole element is 0.
      vector[0] = 0;
      while (count > 0)
      {
         if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
         curr = loadNode(vector[--count]);
         
         // Searches for the greatest key of the node marked in the result set or is not deleted. 
         i = size = curr.size;
         currKeys = curr.keys;
         children = curr.children;
         
         while (--i >= 0)
            if ((currKey = currKeys[i]).record != Key.NO_VALUE && (bitMap == null || bitMap.isBitSet(currKey.record)))
            {                  
               currKey.keys[0].cloneSQLValue(sqlValue);
               count = 0; // juliana@284_3: solved a possible wrong result in MAX() and MIN() if the column searched had an index.
               break;                  
            }
         
         // Now searches the children nodes whose keys are greater than the one marked or all of them if no one is marked.    
         if (children[0] != Node.LEAF)
            while (++i <= size)
               vector[count++] = children[i];
      }
      
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
      // If the type is string and the value is not loaded, loads it.
      if (types[0] == SQLElement.CHARS || types[0] == SQLElement.CHARS_NOCASE) 
      {
         sqlValue.asLong = Utils.subStringHashCode(table.name, 5);
         if (sqlValue.asString == null)
            sqlValue.asString = table.db.loadString();
      }            
   }
   
   /**
    * Returns a node already loaded or loads it if there is empty space in the cache node to avoid loading already loaded nodes.
    * 
    * @param idx The node index.
    * @return The loaded node, a new cache node with the requested node loaded, a first level node, or <code>null</code> if it is not already loaded 
    * and its cache is full.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   private Node getLoadedNode(int idx) throws IOException, InvalidDateException
   {
      Node node;
      
      // juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.
      // Tries to find the node in the nodes of the first level.
      if (idx <= btreeMaxNodes)
      {
         if ((node = firstLevel[idx - 1]) == null)
         {
            (node = firstLevel[idx - 1] = new Node(this)).idx = idx;
            node.load();
         }
         else if (node.idx == -1)
         {
            node.idx = idx;
            node.load();
         }
            
         return node;
      }
      
      // Tries to get an already loaded node if it is a node from a deeper level.
      Node[] cacheAux = cache;
      
      int i = -1;
      while (++i < INDEX_CACHE_SIZE && cacheAux[i] != null) 
         if (cacheAux[i].idx == idx)
            return cacheAux[cacheI = i];   
      
      if (i < INDEX_CACHE_SIZE) // Loads the node if there is enough space in the node cache.
      {
         (node = cacheAux[cacheI = i] = new Node(this)).idx = idx;
         node.load();
         return node;
      }
      
      return null;
   }
   
   // juliana@230_29: order by and group by now use indices on simple queries.
   /**
    * Sorts the records of a table into a temporary table using an index in the ascending order.
    * 
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param tempTable The temporary table for the result set.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws DriverException If the index is corrupted.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it. 
    */
   void sortRecordsAsc(IntVector bitMap, Table tempTable, SQLValue[] record, short[] columnIndexes, SQLSelectClause clause) 
                                                                             throws DriverException, InvalidDateException, IOException
   {
      int size,
          i,
          valRec,
          node,
          nodeCounter = nodeCount + 1,
          count = 1;
          Node curr;
      int[] valRecs = new int[nodeCounter];
      int[] nodes = table.db.driver.nodes;
      Key[] keys;
      int[] children;
      
      // Recursion using a stack. The nodes array sole element is 0.
      valRecs[0] = Key.NO_VALUE;
      nodes[0] = 0;
      while (count > 0)
      {
         // Gets the key and child node.
         valRec = valRecs[--count];
         node = nodes[count];
         
         // Loads a node if it is not a leaf node.
         if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
         
         size = (curr = loadNode(node)).size;
         children = curr.children;
         keys = curr.keys;
         
         if (children[0] == Node.LEAF) // If the node do not have children, just process its keys in the ascending order.
         {
            i = -1;
            while (++i < size)
               writeKey(keys[i].record, bitMap, tempTable, record, columnIndexes, clause);
            writeKey(valRec, bitMap, tempTable, record, columnIndexes, clause);
         }
         else // If not, push its key and process its children in the ascending order. 
         {
            if (size > 0)
            {
               valRecs[count] = valRec;
               nodes[count++] = children[size];
            }
            while (--size >= 0)
            {
               valRecs[count] = keys[size].record;
               nodes[count++] = children[size];
            }
         }
      }      
   }
   
   /**
    * Sorts the records of a table into a temporary table using an index in the descending order.
    * 
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param tempTable The temporary table for the result set.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws DriverException If the index is corrupted.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.
    */
   void sortRecordsDesc(IntVector bitMap, Table tempTable, SQLValue[] record, short[] columnIndexes, SQLSelectClause clause) 
                                                                              throws DriverException, InvalidDateException, IOException
   {
      int size,
          i,
          valRec,
          node,
          nodeCounter = nodeCount + 1,
          count = 1;
      Node curr;
      int[] valRecs = new int[nodeCounter];
      int[] nodes = table.db.driver.nodes;
      Key[] keys;
      int[] children;

      // Recursion using a stack.
      // Recursion using a stack. The nodes array sole element is 0.
      valRecs[0] = Key.NO_VALUE;
      nodes[0] = 0;
      while (count > 0)
      {
         // Gets the key and child node.
         valRec = valRecs[--count];
         node = nodes[count];
         
         // Loads a node if it is not a leaf node.
         if (--nodeCounter < 0) // juliana@220_16: does not let the index access enter in an infinite loop.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_LOAD_NODE));
         
         size = (curr = loadNode(node)).size;
         children = curr.children;
         keys = curr.keys;
         
         if (children[0] == Node.LEAF) // If the node do not have children, just process its keys in the descending order.
         {
            writeKey(valRec, bitMap, tempTable, record, columnIndexes, clause);
            i = size;
            while (--i >= 0)
               writeKey(keys[i].record, bitMap, tempTable, record, columnIndexes, clause);            
         }
         else // If not, process its children in the descending order and then push its key. 
         {
            i = -1;
            while (++i < size)
            {
               valRecs[count] = keys[i].record;
               nodes[count++] = children[i];
            }
            if (size > 0)
            {
               valRecs[count] = valRec;
               nodes[count++] = children[size];
            }
         }         
      }
   }
   
   /**
    * Writes all the records with a specific key in the temporary table that satisfy the query where clause. 
    * 
    * @param valRec The record index.
    * @param bitMap The table bitmap which indicates which rows will be in the result set.
    * @param tempTable The temporary table for the result set.
    * @param record A record for writing in the temporary table.
    * @param columnIndexes Has the indices of the tables for each resulting column.
    * @param clause The select clause of the query.
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.
    */
   private void writeKey(int valRec, IntVector bitMap, Table tempTable, SQLValue[] record, short[] columnIndexes, SQLSelectClause clause) 
                                                                                           throws IOException, InvalidDateException
   {
      if (valRec != Key.NO_VALUE && (bitMap == null || bitMap.isBitSet(valRec)))
      {
         Table tableAux = table;
         byte[] tempNulls = tempTable.columnNulls[0];
         byte[] origNulls = tableAux.columnNulls[0];
         short[] offsets = tableAux.columnOffsets;
         byte[] types = tableAux.columnTypes;
         int i = tempTable.columnCount,
             colIndex;
         boolean isNull;
         
         tableAux.db.read(valRec); // Reads the record.
         tableAux.readNullBytesOfRecord(0, false, 0); // Reads the bytes of the nulls.
         
         while (--i >= 0) // Reads the fields for the temporary table.
         {
            colIndex = columnIndexes[i];
            if (!(isNull = (origNulls[colIndex >> 3] & (1 << (colIndex & 7))) != 0))
               tableAux.readValue(record[i], offsets[colIndex], types[colIndex], false, true);

            Utils.setBit(tempNulls, i, isNull); // Sets the null values for tempTable.
         } 
         tempTable.writeRSRecord(record); // Writes the temporary table record.
      }
   }
}
