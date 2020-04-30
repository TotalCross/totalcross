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

/**
 * This has the function definitions for a database in a plain binary file. The data and the metadata (header) is written in one file (.db). The 
 * strings and the blobs are written in the .dbo file. The current number of records inside the database is discovered only when the database is open 
 * by getting its size and discounting the header size. This has a double advantage: it is not necessary to waste space storing the current record 
 * count, and it is not needed to save the record count at each insertion. 
 * 
 * This also has function definitions for a temporary database for <code>ResultSet</code> tables.
 */

#include "PlainDB.h"

// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Creates a new <code>PlainDB</code>, loading or creating the table with the given name or creating a temporary table.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB Receives the new <code>PlainDB</code> or <code>null</code> if an error occurs.
 * @param name The name of the table.
 * @param create Defines if the file will be created if it doesn't exist.
 * @param useCrypto Indicates if the table uses cryptography.
 * @param sourcePath The path where the table is to be open or created.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool createPlainDB(Context context, PlainDB* plainDB, CharP name, bool create, bool useCrypto, TCHARP sourcePath)
{
   TRACE("createPlainDB")
   char buffer[DBNAME_SIZE];

   plainDB->rowInc = name? DEFAULT_ROW_INC : 100; // Sets row incrementor.
   plainDB->headerSize = (uint16)DEFAULT_HEADER; // Sets the initial header size.
   
   if (name) 
   {
      // Sets the .db name if the file is a normal file.
      xstrcpy(plainDB->name, name);
      xstrcpy(buffer, name);
      xstrcat(buffer, DB_EXT);

      // Sets the normal file function pointers.
      plainDB->setPos = nfSetPos;
      plainDB->growTo = nfGrowTo;
      plainDB->readBytes = nfReadBytes;
      plainDB->writeBytes = nfWriteBytes;
      plainDB->close = nfClose;
      // Opens or creates the .db and .dbo files.
	   if (nfCreateFile(context, buffer, create, useCrypto, sourcePath, &plainDB->db, -1)
       && xstrcat(buffer, "o")
       && nfCreateFile(context, buffer, create, useCrypto, sourcePath, &plainDB->dbo, -1))
         return true;
   }
   else
   {
      // Sets the memory file function pointers.
      plainDB->setPos = mfSetPos;
      plainDB->growTo = mfGrowTo;
		plainDB->readBytes = mfReadBytes;
		plainDB->writeBytes = mfWriteBytes;
		plainDB->close = mfClose;
      return true;
   }
	
   plainClose(context, plainDB, false); // Closes the table files if an error occurs.
   return false;
}

/**
 * Sets the size of a row.
 * 
 * @param plainDB The <code>PlainDB</code>.
 * @param newRowSize The new row size.
 * @param buffer A buffer for the <code>PlainDB</code>.
 */
void plainSetRowSize(PlainDB* plainDB, int32 newRowSize, uint8* buffer)
{
	TRACE("plainSetRowSize")
   plainDB->rowSize = newRowSize;
   plainDB->basbuf = buffer;

   if (plainDB->db.size >= plainDB->headerSize) // Finds how many records are there.
		plainDB->rowCount = (plainDB->db.size - plainDB->headerSize)/ plainDB->rowSize;
}

/**
 * Adds a new record. The file pointer is positioned in the record's beginning so that the data can be written. Usually the record is first added, 
 * then the contents are written.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainAdd(Context context, PlainDB* plainDB)
{
	TRACE("plainAdd") 

   if (--plainDB->rowAvail <= 0) // Checks if there are no more space pre-allocated.
   {
      if (!plainDB->growTo(context, &plainDB->db, (plainDB->rowCount + plainDB->rowInc) * plainDB->rowSize + plainDB->headerSize))
         return false;
      plainDB->rowAvail = plainDB->rowInc;
   }
   return plainSetPos(context, plainDB, plainDB->rowCount); // Sets the position to the start of the record.
}

/**
 * Writes the data of the .db file buffer into the current file position.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainWrite(Context context, PlainDB* plainDB)
{
	TRACE("plainWrite") 
   if (plainDB->writeBytes(context, &plainDB->db, plainDB->basbuf, plainDB->rowSize))
   {
      plainDB->rowCount++;
      return true;
   }
   return false;
}

/**
 * Reads a row at the given position into the .db file buffer.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The record to be read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRead(Context context, PlainDB* plainDB, int32 record)
{
	TRACE("plainRead")
   return plainSetPos(context, plainDB, record) && plainDB->readBytes(context, &plainDB->db, plainDB->basbuf, plainDB->rowSize);
}

/**
 * Rewrites a row at the given position.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The .db file record to be read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRewrite(Context context, PlainDB* plainDB, int32 record)
{
	TRACE("plainRewrite")
   return plainSetPos(context, plainDB, record) && plainDB->writeBytes(context, &plainDB->db, plainDB->basbuf, plainDB->rowSize);
}

/**
 * Renames the files to the new given name.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param newName The new table name.
 * @param sourcePath The files path.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRename(Context context, PlainDB* plainDB, CharP newName, TCHARP sourcePath)
{
	TRACE("plainRename")
   char buffer[DBNAME_SIZE];
   int32 finalPos = plainDB->dbo.finalPos;

   xstrcpy(buffer, newName);
   xstrcat(buffer, DB_EXT);

	// juliana@202_1: .db should be renamed back if .dbo can't be renamed.
   if (nfRename(context, &plainDB->db, buffer, sourcePath)) // Renames the .db file.
	{	
      xstrcat(buffer, "o");
		if (!nfRename(context, &plainDB->dbo, buffer, sourcePath)) // Renames the .dbo file.
		{
         // If the file could not be renamed, which is unlikely to occur, the .db file should be renamed back.
         xstrcpy(buffer, plainDB->name);
         xstrcat(buffer, DB_EXT);
         nfRename(context, &plainDB->db, buffer, sourcePath);
			return false;
		}
	} 
	else
		return false;

   plainDB->dbo.finalPos = finalPos;
   return true;
}

/**
 * Writes the given metadata to the header of the .db file.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param buffer The data to be written.
 * @param length The data length.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainWriteMetaData(Context context, PlainDB* plainDB, uint8* buffer, int32 length)
{
	TRACE("plainWriteMetaData") 
   int32 headerSize = plainDB->headerSize;
   XFile* db = &plainDB->db;

   if (!plainDB->db.size) // The metadata size must have a free space for future composed indices or composed primary key.
   {
      // juliana@230_7: corrected a possible exception or crash when the table has too many columns and composed indices or PKs.
      while (length > headerSize || headerSize - length < COMP_IDX_PK_SIZE)
         headerSize <<= 1;
      if (!nfGrowTo(context, db, plainDB->headerSize = headerSize))
         return false;

      // juliana@223_15: solved a bug that could corrupt tables created with a very large metadata size.
      buffer[4] = (uint8)headerSize;
      buffer[5] = (uint8)(headerSize >> 8);
   }
   nfSetPos(db, 0);
        
   if (db->useCrypto) // juliana@253_8: now Litebase supports weak cryptography.
   {
      int32 i = 4;
      while (--i >= 0)
         *buffer++ ^= 0xAA;
      buffer -= 4; 
   }
   
   return nfWriteBytes(context, db, buffer, length);
}

/**
 * Reads the user metadata from the .db file header.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param buffer An static buffer for reading the metadata.
 * @return The metadata.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory. 
 * @return <code>null</code> if an error occurs; a metadata buffer, otherwise.
 */
uint8* plainReadMetaData(Context context, PlainDB* plainDB, uint8* buffer)
{
	TRACE("plainReadMetaData")

   if (!buffer) // Allocates the buffer for the metadata.
      if (!(buffer = (uint8*)xmalloc(plainDB->headerSize)))
      {
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         return null;
      }

   // Fetches and reads the metadata.
   nfSetPos(&plainDB->db, 0);
   
   // juliana@223_14: solved possible memory problems.
   if (!nfReadBytes(context, &plainDB->db, buffer, plainDB->headerSize)) 
   {   
      if (plainDB->headerSize != DEFAULT_HEADER)
         xfree(buffer);
      return null;
   }
   
   if (plainDB->db.useCrypto) // juliana@253_8: now Litebase supports weak cryptography.
   {
      int32 i = 4;
      while (--i >= 0)
         *buffer++ ^= 0xAA;
      buffer -= 4; 
   }
   
   return buffer;
}

/**
 * Closes the table files.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param updatePos Indicates if <code>finalPos</code> must be re-calculated to shrink the file. 
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainClose(Context context, PlainDB* plainDB, bool updatePos)
{
	TRACE("plainClose")
	bool ret = true;

   if (plainDB)
   {
      if (plainDB->db.fbuf || fileIsValid(plainDB->db.file) || plainDB->db.cache)
      {
			if (*plainDB->name)
         {
            uint8 buffer[7];
            uint8* pointer = buffer;

            // Stores the changeable information.
            // juliana@253_8: now Litebase supports weak cryptography.
            xmemzero(buffer, 4);
            *pointer = (plainDB->db.useCrypto? (plainDB->useOldCrypto? 1 : USE_CRYPTO) : 0); 
            xmove2(pointer + 4, &plainDB->headerSize);
            pointer += 6;

            // The table format must also be saved.
				*pointer++ = plainDB->isAscii? IS_ASCII | !plainDB->wasNotSavedCorrectly : !plainDB->wasNotSavedCorrectly;

            ret = plainWriteMetaData(context, plainDB, buffer, 7);

            if (updatePos) // Calculates .db used space: .db won't have zeros at the end.
			      plainDB->db.finalPos = plainDB->rowCount * plainDB->rowSize + plainDB->headerSize;
         }
         ret &= plainDB->close(context, &plainDB->db); // Closes .db.
      }
		if (plainDB->dbo.fbuf || fileIsValid(plainDB->dbo.file) || plainDB->dbo.cache) // Closes .dbo if it's open.
         ret &= plainDB->close(context, &plainDB->dbo);
   }
   return ret;
}

/**
 * Removes the table files.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param sourcePath The files path.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRemove(Context context, PlainDB* plainDB, TCHARP sourcePath)
{
	TRACE("plainRemove")
   bool ret = true;

	if (fileIsValid(plainDB->db.file) || plainDB->db.cache)
      ret = nfRemove(context, &plainDB->db, sourcePath);
   if (fileIsValid(plainDB->dbo.file) || plainDB->dbo.cache)
      ret &= nfRemove(context, &plainDB->dbo, sourcePath);

   return ret;
}

/**
 * Sets the .db file pointer to point to a record position.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The record position.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainSetPos(Context context, PlainDB* plainDB, int32 record)
{
	TRACE("plainSetPos")
   if (record >= 0 && record < (plainDB->rowCount + plainDB->rowAvail))
   {
      // If the table grows too much, the real position can be negative because the integer size has a limit.
      int32 value = record * plainDB->rowSize + (*plainDB->name? plainDB->headerSize : 0);
      if (value < 0)
		{
			TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_POS), record);
			return false;
		}
      plainDB->setPos(&plainDB->db, value);
      return true;
   }
   if (record >= 0)
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_POS), record);
	return false;
}

// guich@201_9: always shrink the .db and .dbo memory files.
/**
 * Compresses the memory file buffers at the current position. This is necessary so that the memory tables do not have unused space after computing 
 * the select.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainShrinkToSize(Context context, PlainDB* plainDB)
{
	TRACE("plainShrinkToSize")
   if (plainDB->rowCount > 0 && plainDB->rowAvail > 0)
   {
      uint32 ret = plainDB->rowCount * plainDB->rowSize;
   	if (plainDB->db.size != ret) // Shrinks the .db.
   	{
         if (mfGrowTo(context, &plainDB->db, ret))
         {
            plainDB->db.size = ret;
            plainDB->rowAvail = 0;
         } 
         else
            return false;
      }
		if ((int32)plainDB->dbo.size != plainDB->dbo.finalPos)
      {
         if (mfGrowTo(context, &plainDB->dbo, plainDB->dbo.finalPos)) // Shrinks the .dbo.
			   plainDB->dbo.size = plainDB->dbo.finalPos;
         else
            return false;
      }
   }
   return true;
}

/**
 * Reads a value from a PlainDB.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param value The value to be read.
 * @param offset The offset of the value in its row.
 * @param colType The type of the value.
 * @param buffer The buffer where the row data is stored.
 * @param isTemporary Indicates if this is a result set table or the value or the integer of a rowid index is to be loaded.
 * @param isNull Indicates if the value is null.
 * @param isTempBlob Indicates if the blob is being read for a temporary table.
 * @param size The column size of the string being read.
 * @param heap A heap to allocate temporary strings.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool readValue(Context context, PlainDB* plainDB, SQLValue* value, int32 offset, int32 colType, uint8* buffer, bool isTemporary, bool isNull, 
                                                                                                bool isTempBlob, int32 size, Heap heap)
{
	TRACE("readValue")
   buffer += offset;
   if (!isNull)
      switch (colType)
      {
         // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
         case CHARS_NOCASE_TYPE:
         case CHARS_TYPE:
         {
            int32 length = 0,
                  position;
            XFile* dbo;

            xmove4(&position, buffer);

            if (isTempBlob && !*plainDB->name)
            {
               uint8* ptrStr = plainDB->dbo.fbuf + position;
               xmove4(&position, ptrStr);
               ptrStr += 4; 
               xmoveptr(&plainDB, ptrStr);
            }

            if (position < 0) // juliana@270_22: solved a possible crash when the table is corrupted on Android and possibly on other platforms.
            {
               TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_POS), position);
               return false;
            } 

            // juliana@253_8: now Litebase supports weak cryptography.
            else if (position > (dbo = &plainDB->dbo)->finalPos)
            {
               value->length = 0;
               return true; 
            }
            
            // Reads the string position in the .dbo and sets its position.
            plainDB->setPos(dbo, value->asInt = position); 
            
            value->asBlob = (uint8*)plainDB; // Holds the plainDB pointer so the string don't need to be loaded in the temporary table.

            // Reads the string size. If it is zero nothing is read.
            if (!plainDB->readBytes(context, dbo, (uint8*)&length, 2))
               return false;
            
            // juliana@230_12: improved recover table to take .dbo data into consideration.
            if (size != -1 && length > size)
               length = size;
            
            // juliana@253_5: removed .idr files from all indices and changed its format. 
            if (!(value->length = length) && !value->asChars)
            {
               value->asChars = (JCharP)"";
               return true;
            }   

            if (!value->asChars) // Allocates the string if it was not previoulsy allocated.
               value->asChars = (JCharP)TC_heapAlloc(heap, length << 1);

		      return loadString(context, plainDB, value->asChars, length);
         }
         case SHORT_TYPE:
            xmove2(&value->asShort, buffer); // Reads the short.
            break;

         case INT_TYPE:
            xmove4(&value->asInt, buffer); // Reads the int.
            if (!offset && !isTemporary) // Is it the row id?
				   value->asInt &= ROW_ID_MASK; // Masks out the attributes.
            break;

         case LONG_TYPE:
            xmove8(&value->asLong, buffer); // Reads the long.
            break;

         case FLOAT_TYPE:
            xmove4(&value->asFloat, buffer); // Reads the float.
            break;

         case DOUBLE_TYPE:
            READ_DOUBLE((uint8*)&value->asDouble, buffer); // Reads the double.
            break;

         case DATE_TYPE: 
            xmove4(&value->asInt, buffer); // Reads the date.
            break;

         case DATETIME_TYPE: // rnovais@567_2
            xmove4(&value->asDate, buffer); // Reads the date.
            xmove4(&value->asTime, buffer + 4); // Reads the time.
            break;

         case BLOB_TYPE: // juliana@220_3: blobs are not loaded anymore in the temporary table and sortings.
			   if (isTempBlob)  // A blob is being read to a temporary table.
			   {
				   xmove4(&value->asInt, buffer); 
				   value->asBlob = (uint8*)plainDB;
			   }
			   else
			   {
               int32 position,
                     length;
               XFile* dbo = &plainDB->dbo;

               // Reads and sets the blob position in the .dbo.
				   xmove4(&position, buffer);

               if (position > dbo->finalPos)
               {
                  value->length = 0;
                  return true; 
               }
               else if (position < 0) // juliana@270_22: solved a possible crash when the table is corrupted on Android and possibly on other platforms.
               {
                  TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_POS), position);
                  return false;
               }

				   plainDB->setPos(dbo, position);

				   if (!plainDB->readBytes(context, dbo, (uint8*)&length, 4)) // Reads the blob size;
					   return false;

               if (length < 0) // juliana@253_8: now Litebase supports weak cryptography.
                  length = 0;
               if (size != -1 && length > size)
                  length = size;
               
               // If the size is zero nothing is read.
				   if ((value->length = length) > 0 && value->asBlob && !plainDB->readBytes(context, dbo, value->asBlob, length))
					   return false;
			   }
      }
   return true;
}

/**
 * Writes a value to a table column.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param value The value to be written.
 * @param buffer The buffer where the value is stored before going to the table.
 * @param colType The type of the column.
 * @param colSize The column size of the value.
 * @param isValueOk Indicates if the value is to be written.
 * @param addingNewRecord Indicates if it is an update or an insert.
 * @param isNull Indicates that the value being inserted is a null.
 * @paran isTempBlob Indicates if a temporary table is being used.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool writeValue(Context context, PlainDB* plainDB, SQLValue* value, uint8* buffer, int32 colType, int32 colSize, bool isValueOk, bool addingNewRecord, bool isNull, bool isTempBlob)
{
	TRACE("writeValue")

   if (isValueOk && !isNull)
      switch (colType)
      {
         // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets.
         case CHARS_NOCASE_TYPE:
         case CHARS_TYPE:
            if (isTempBlob)
            {
               int32 size;
               XFile* dbo = &plainDB->dbo;

               if ((dbo->finalPos + (size = TSIZE + 4)) > (int32)dbo->size
                && !plainDB->growTo(context, dbo, dbo->size + size * MAX(16, plainDB->rowInc))) 
					    return false;

               plainDB->setPos(dbo, dbo->finalPos);
               xmove4(buffer, &dbo->position);

               // Saves the .dbo position and the physical plainDB pointer. 
				   if (!plainDB->writeBytes(context, dbo, (uint8*)&value->asInt, 4) 
                || !plainDB->writeBytes(context, dbo, (uint8*)&value->asBlob, TSIZE))
			         return false;
               dbo->finalPos = dbo->position;
               return true;
            }
            else
            {
               int32 length = value->length, // juliana@225_7: the string that is bigger than its field definiton was already trimmed.
                     size = plainDB->isAscii? (2 + length) : (2 + (length << 1)), // Computes the string size.
                     ret = true;
               XFile* dbo = &plainDB->dbo;

			      // juliana@201_20: only grows .dbo if it is going to be increased.
			      // juliana@202_21: Always writes the string at the end of the .dbo. This removes possible bugs when doing updates.
               // guich@201_8: grows using rowInc instead of 16 if rowInc > 16.
               if ((dbo->finalPos + size) > (int32)dbo->size && !plainDB->growTo(context, dbo, dbo->size + size * MAX(16, plainDB->rowInc))) 
                  return false;
               
               plainDB->setPos(dbo, dbo->finalPos);       
               xmove4(buffer, &dbo->position);
               value->asInt = dbo->position; // The string position for an index.

               // Writes the string.
               if (plainDB->isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
			      {
			         int32 i = length;
				      CharP from = (CharP)value->asChars,
				            to = (CharP)value->asChars;

				      while (--i >= 0) // Transforms the unicode string into an ascii string.
                  {
                     *to++ = *from;
                     from += 2;
                  }
                  
				      if (!plainDB->writeBytes(context, dbo, (uint8*)&length, 2) 
                   || (length && !plainDB->writeBytes(context, dbo, (uint8*)value->asChars, length)))
				         ret = false;

                  // The string MUST be transformed back. Otherwise, it may mess a Java string up.
				      from = (CharP)value->asChars + (i = length - 1);
				      to = from + i;
				      while (--i >= 0)
				      {
				         *to = *from;
				         *from-- = 0;
					      to -= 2;
			         }
			      } 
			      else if (!plainDB->writeBytes(context, dbo, (uint8*)&length, 2) 
                     || (length && !plainDB->writeBytes(context, dbo, (uint8*)value->asChars, length << 1)))
                   return false;

               dbo->finalPos = dbo->position; // juliana@202_21: the final positon now is always the new positon.
               return ret;
            }

         case SHORT_TYPE:
            xmove2(buffer, &value->asShort); 
            break;

         case DATE_TYPE: // rnovais@567_2
         case INT_TYPE:
            xmove4(buffer, &value->asInt); 
            break;

         case LONG_TYPE:
            xmove8(buffer, &value->asLong);
            break;

         case FLOAT_TYPE:
            xmove4(buffer, &value->asFloat); 
            break;

         case DOUBLE_TYPE:
            READ_DOUBLE(buffer, (uint8*)&value->asDouble); 
            break;

         case DATETIME_TYPE: // rnovais@567_2
            xmove4(buffer, &value->asDate); // Writes the date.
            xmove4(buffer + 4, &value->asTime); // Writes the time.
            break;

         case BLOB_TYPE:
         {
			   int32 size;
            XFile* dbo = &plainDB->dbo;

			   if (isTempBlob) // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
			   {
               // guich@201_8: grows using rowInc instead of 16 if rowInc > 16.
               // If the .dbo is full, grows it. 
				   if ((dbo->finalPos + (size = TSIZE + 4)) > (int32)dbo->size
                && !plainDB->growTo(context, dbo, dbo->size + size * MAX(16, plainDB->rowInc))) 
					    return false;

               plainDB->setPos(dbo, dbo->finalPos);
               xmove4(buffer, &dbo->position);

               // Saves the .dbo position and the physical plainDB pointer. 
				   if (!plainDB->writeBytes(context, dbo, (uint8*)&value->asInt, 4) 
                || !plainDB->writeBytes(context, dbo, (uint8*)&value->asBlob, TSIZE))
			         return false;
               dbo->finalPos = dbo->position;
			   }
			   else
			   {
				   int32 oldPos = 0,
                     length = MIN((int32)value->length, colSize);

				   size = length + 4; 
					
				   // juliana@201_20: only grows .dbo if it is going to be increased.
               if (addingNewRecord && (dbo->finalPos + size) > (int32)dbo->size 
                && !plainDB->growTo(context, dbo, dbo->size + size * MAX(16, plainDB->rowInc)))  // guich@201_8: grow using rowInc instead of 16 if rowInc > 16
					    return false;
				   
               // It is an insert or the size of the blob is greater then the old, writes the blob at the end of the .dbo. 
               if (addingNewRecord)
					   plainDB->setPos(dbo, dbo->finalPos);
               else
					   plainDB->setPos(&plainDB->dbo, oldPos = dbo->position);

               // Writes its position in the buffer.
				   xmove4(buffer, &dbo->position);

               // Writes the blob size to .dbo and the blob itself to .dbo.
				   if (!plainDB->writeBytes(context, dbo, (uint8*)&length, 4) 
                || (length && !plainDB->writeBytes(context, dbo, value->asBlob, length)))
					    return false;

				   if (addingNewRecord) // It is an insert or the size of the blob is greater then the old one, the final positon is the new positon.
					   dbo->finalPos = dbo->position;
				   else // Otherwise, restores the old position.
					   plainDB->setPos(dbo, oldPos + size + 4);
			   }
         }
      }
   return true;
}

/**
 * Tests if a record of a table is not deleted.
 *
 * @param buffer The buffer where the table record is stored.
 * @return <code>false</code> if the record is deleted; <code>true</code> otherwise.
 */
int32 recordNotDeleted(uint8* buffer)
{
	TRACE("recordNotDeleted")
	int32 attr;

	xmove4(&attr, buffer);
   return (attr & ROW_ATTR_MASK) != ROW_ATTR_DELETED; // When a row is deleted, its rowid is set to 0.
}

/**
 * Loads a string from a table taking the storage format into consideration.
 *
 * @param context The thread context where the function is being executed. 
 * @param plainDB The <code>PlainDB</code>.
 * @param string The buffer where the string will be stored.
 * @param length The length of the string to be loaded.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool loadString(Context context, PlainDB* plainDB, JCharP string, int32 length)
{
   TRACE("loadString")
   
   if (plainDB->isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
   {
      int32 i = length - 1;
	   CharP str = (CharP)string,
	         from = str + i,
			   to = from + i;
	   
	   if (!plainDB->readBytes(context, &plainDB->dbo, (uint8*)str, length)) // Reads the string.
		   return false;
		   
	   while (--i >= 0)
	   {
	      *to = *from;
	      *from-- = 0;
		   to -= 2;
      }
   }
   else if (!plainDB->readBytes(context, &plainDB->dbo, (uint8*)string, length << 1)) // Reads the string.
      return false;
   return true;
}
