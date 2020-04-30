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
 * Defines functions for a normal file, ie, a file that is stored on disk.
 */

#include "NormalFile.h"

/**
 * Creates a disk file to store tables.
 *
 * @param context The thread context where the function is being executed.
 * @param name The name of the file.
 * @param isCreation Indicates if the file must be created or just open.
 * @param useCrypto Indicates if the table uses cryptography.
 * @param sourcePath The path where the file will be created.
 * @param xFile A pointer to the normal file structure.
 * @param cacheSize The cache size to be used. -1 should be passed if the default value is to be used.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the file cannot be open.
 * @throws OutOfMemoryError If there is not enough memory to create the normal file cache.
 */
bool nfCreateFile(Context context, CharP name, bool isCreation, bool useCrypto, TCHARP sourcePath, XFile* xFile, int32 cacheSize)
{
	TRACE("nfCreateFile")
   TCHAR buffer[MAX_PATHNAME];
   uint32 ret;

   xmemzero(xFile, sizeof(XFile));
   fileInvalidate(xFile->file);
	
	// juliana@252_3: corrected a possible crash if the path had more than 255 characteres.
   if (xstrlen(name) + tcslen(sourcePath) + 1 > MAX_PATHNAME)
	{
      char buffer[1024];
      TC_TCHARP2CharPBuf(sourcePath, buffer);
	   TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), buffer);
	   return false;
	}
	
   if (cacheSize != -1 && !(xFile->cache = xmalloc(xFile->cacheInitialSize = cacheSize))) // Creates the cache.
	{
		TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
		return false;
	}
  
   getFullFileName(name, sourcePath, buffer); // Gets the file path.

   // juliana@227_3: improved table files flush dealing.
   if (xstrchr(name, '$') || xstrchr(name, '&'))
      xFile->dontFlush = true;
   
   xFile->useCrypto = useCrypto; // juliana@253_8: now Litebase supports weak cryptography.
      
   // Creates the file or opens it and gets its size.
// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
#if defined(POSIX) || defined(ANDROID)
   xstrcpy(xFile->fullPath, buffer);
   if ((ret = openFile(context, xFile, isCreation? CREATE_EMPTY : READ_WRITE))
#else
   if ((ret = lbfileCreate(&xFile->file, buffer, isCreation? CREATE_EMPTY : READ_WRITE))
#endif
    || (ret = lbfileGetSize(xFile->file, null, (int32*)&xFile->size)))
   {
      fileError(context, ret, name);

#if defined(POSIX) || defined(ANDROID)
      removeFileFromList(xFile);
#endif
      
      if (fileIsValid(xFile->file))
         lbfileClose(&xFile->file);
      return false;
   }
      
   xstrcpy(xFile->name, name);
   return true;
}

/**
 * Reads file bytes.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param buffer The byte array to read data into.
 * @param count The number of bytes to read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nfReadBytes(Context context, XFile* xFile, uint8* buffer, int32 count)
{
	TRACE("nfReadBytes")

   // juliana@202_4: Removed a possible reset or GPF if there is not enough memory to create the file cache on Windows 32, Windows CE, Palm OS, 
   // and iPhone.
	if ((xFile->cacheInitialSize < count || xFile->cachePos < xFile->cacheIni || (xFile->cachePos + count) > xFile->cacheEnd) 
     && !refreshCache(context, xFile, count))
      return false;
   
   xmemmove(buffer, &xFile->cache[xFile->cachePos - xFile->cacheIni], count);
   
   // juliana@253_8: now Litebase supports weak cryptography.
   if (xFile->useCrypto) // Decrypts data if asked.
   {
      int32 i = count;
      while (--i >= 0)
         *buffer++ ^= 0xAA; 
   }
   
   xFile->cachePos += count; // do NOT update xf->pos here!
   return true;
}

// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Write bytes in a file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param buffer The byte array to write data from.
 * @param count The number of bytes to write.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nfWriteBytes(Context context, XFile* xFile, uint8* buffer, int32 count)
{
	TRACE("nfWriteBytes")
   int32 cachePos;
   uint8* bufferAux = buffer;

   // juliana@253_8: now Litebase supports weak cryptography.
   if (xFile->useCrypto) // Encrypts data if asked.
   {
      int32 i = count;
      while (--i >= 0)
         *bufferAux++ ^= 0xAA; 
   }

	// juliana@202_4: Removed a possible reset or GPF if there is not enough memory to create the file cache on Windows 32, Windows CE, Palm OS, 
   // and iPhone.
	if ((xFile->cacheInitialSize < count || xFile->cachePos < xFile->cacheIni || (xFile->cachePos + count) > xFile->cacheEnd) 
     && !refreshCache(context, xFile, count))
      return false;

   xmemmove(&xFile->cache[(cachePos = xFile->cachePos) - xFile->cacheIni], buffer, count);
   
   // juliana@253_8: now Litebase supports weak cryptography.
   if (xFile->useCrypto) // Decrypts data if asked.
   {
      int32 i = count;
      while (--i >= 0)
         *buffer++ ^= 0xAA; 
   }
   
   xFile->cacheIsDirty = true;
   xFile->cacheDirtyIni = MIN(cachePos, xFile->cacheDirtyIni);
   xFile->cacheDirtyEnd = MAX(cachePos + count, xFile->cacheDirtyEnd);
   xFile->position = xFile->cachePos = cachePos + count;
   return true;
}

// juliana@227_3: improved table files flush dealing.
/**
 * Enlarges the file. This function MUST be called to grow the file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param newSize The new size for the file.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to grow the file.
 */
bool nfGrowTo(Context context, XFile* xFile, uint32 newSize)
{
	TRACE("nfGrowTo")
   int32 ret;

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      goto error;
#endif

   // The index files grow a bunch per time, so it is necessary to check here if the growth is really needed.
   // If so, enlarges the file.
   if ((ret = lbfileSetSize(&xFile->file, newSize)))
      goto error;

// juliana@227_23: solved possible crashes when using a table recovered which was being used with setRowInc().
#if !defined(POSIX) && !defined(ANDROID)
   if (newSize - xFile->size > 0) // juliana@230_18: removed possible garbage in table files.
   {
      uint8 zeroBuf[1024];
      int32 remains = newSize - xFile->size,
            written;
      xmemzero(zeroBuf, 1024);

      if ((ret = lbfileSetPos(xFile->file, xFile->size)))
         goto error;
      while (remains > 0)
      {
         if ((ret = lbfileWriteBytes(xFile->file, zeroBuf, 0, remains > 1024? 1024 : remains, &written)))
            goto error;
         remains -= written;
      }
      
   } 
#endif

   xFile->position = xFile->size = newSize;
   return true;
   
error:
   fileError(context, ret, xFile->name);
   return false;
}

/**
 * Sets the current file position.
 *
 * @param xFile A pointer to the normal file structure.
 * @param newPos The new file position. 
 */
void nfSetPos(XFile* xFile, int32 newPos)
{
	TRACE("nfSetPos")
   xFile->cachePos = xFile->position = newPos;
}

// juliana@227_3: improved table files flush dealing.
/**
 * Renames a file
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param newName The new name of the file.
 * @param sourcePath The path where the file is stored.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to rename the file.
 */
bool nfRename(Context context, XFile* xFile, CharP newName, TCHARP sourcePath)
{  
   TRACE("nfRename")
   TCHAR oldPath[MAX_PATHNAME];
   TCHAR newPath[MAX_PATHNAME];
   int32 ret;

   getFullFileName(xFile->name, sourcePath, oldPath);
   getFullFileName(newName, sourcePath, newPath);

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      goto error;
#endif

   // Renames and reopens the file.
   if ((ret = lbfileRename(xFile->file, oldPath, newPath, true))
    || (ret = lbfileCreate(&xFile->file, newPath, READ_WRITE)))
   {

#if defined(POSIX) || defined(ANDROID)
error:
#endif

      fileError(context, ret, xFile->name);
      return false;
   }

   xstrcpy(xFile->name, newName);

#if defined(POSIX) || defined(ANDROID)
   xstrcpy(xFile->fullPath, newPath);
#endif

   return true;
}

/** 
 * Closes a file.
 * 
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to close the file.
 */
bool nfClose(Context context, XFile* xFile)
{
	TRACE("nfClose")
   int32 ret = 0;

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      fileError(context, ret, xFile->name);
#endif

   if (fileIsValid(xFile->file))
   {
      // Flushes the cache if necessary and frees it.
      if (xFile->cacheIsDirty) 
         flushCache(context, xFile);
      
      xfree(xFile->cache);

      // juliana@201_5: the .dbo file must be cropped so that it wont't be too large with zeros at the end of the file.
		if (xFile->finalPos && (ret |= lbfileSetSize(&xFile->file, xFile->finalPos)))
         fileError(context, ret, xFile->name);

      if ((ret |= lbfileClose(&xFile->file)))
         fileError(context, ret, xFile->name);
   
      fileInvalidate(xFile->file);
   }

#if defined(POSIX) || defined(ANDROID)
   removeFileFromList(xFile);
#endif

   return !ret;
}

/** 
 * Removes a file.
 * 
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param sourcePath The path where the file is stored.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to remove the file.
 */
bool nfRemove(Context context, XFile* xFile, TCHARP sourcePath)
{
	TRACE("nfRemove")
   TCHAR buffer[MAX_PATHNAME]; 
   int32 ret = 0;

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      fileError(context, ret, xFile->name);
#endif

   getFullFileName(xFile->name, sourcePath, buffer);
   if ((ret |= lbfileDelete(&xFile->file, buffer, true)))
      fileError(context, ret, xFile->name);
   fileInvalidate(xFile->file);
   xfree(xFile->cache);

#if defined(POSIX) || defined(ANDROID)
   removeFileFromList(xFile);
#endif

   return !ret;
}

/**
 * The cache must be refreshed if what is desired is not inside it.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param count The number of bytes that must be read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to read from the file.
 * @throws OutOfMemoryError If there is not enough memory to enlarge the normal file cache.
 */
bool refreshCache(Context context, XFile* xFile, int32 count)
{
	TRACE("refreshCache")
   int32 bytes,
         ret;
   
   if (xFile->cacheIsDirty && !flushCache(context, xFile)) // Flushes the cache if necessary.
      return false;
      
   // juliana@223_14: solved possible memory problems.
   if (!xFile->cache || xFile->cacheInitialSize < count) // Increases the cache size if necessary.
      if (!(xFile->cache = xrealloc(xFile->cache, xFile->cacheInitialSize = MAX(CACHE_INITIAL_SIZE, count << 2))))
		{
			TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         return false;
		}

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      goto error;
#endif

   // Reads data from the file.
   if ((ret = lbfileSetPos(xFile->file, xFile->cachePos)) || (ret = lbfileReadBytes(xFile->file, (CharP)xFile->cache, 0, xFile->cacheInitialSize, &bytes)))
   {

#if defined(POSIX) || defined(ANDROID)
error:
#endif

      fileError(context, ret, xFile->name);
      return false;
   }

   // Updates the cache parameters.
   xFile->cacheDirtyEnd = xFile->cacheIni = xFile->cachePos;
   xFile->cacheDirtyIni = xFile->cacheEnd = xFile->cacheIni + bytes;

   return true;
}

/**
 * Flushs the cache into the disk.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to write to the file.
 */
bool flushCache(Context context, XFile* xFile)
{
	TRACE("flushCache")
   int32 written,
         ret;

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Some files might have been closed if the maximum number of opened files was reached.
#if defined(POSIX) || defined(ANDROID)
   if ((ret = reopenFileIfNeeded(context, xFile)))
      goto error;
#endif

   if ((ret = lbfileSetPos(xFile->file, xFile->cacheDirtyIni)) || (ret = lbfileWriteBytes(xFile->file, 
                         (CharP)&xFile->cache[xFile->cacheDirtyIni - xFile->cacheIni], 0, xFile->cacheDirtyEnd - xFile->cacheDirtyIni, &written)))
      goto error;
   xFile->cacheIsDirty = false;

// juliana@227_3: improved table files flush dealing.
// juliana@226a_22: solved a problem on Windows CE of file data being lost after a forced reset.
#if defined(POSIX) || defined(ANDROID)
   if (!xFile->dontFlush && (ret = lbfileFlush(xFile->file)))
      goto error;
#endif

   return true;

error:
   fileError(context, ret, xFile->name);
   return false;
}

/**
 * Prepares an error message when an error occurs when dealing with files.
 * 
 * @param context The thread context where the function is being executed.
 * @param errorCode The file error code.
 * @param fileName The file where the error ocurred.
 * @throws DriverException An exception with the error message.
 */
void fileError(Context context, int32 errorCode, CharP fileName)
{
   TRACE("fileError")
   char errorMsg[1024];
   
   TC_getErrorMessage(errorCode, errorMsg, 1024);
   errorMsg[errorCode = xstrlen(errorMsg)] = ' ';
   xstrcpy(&errorMsg[errorCode + 1], fileName);
   TC_throwExceptionNamed(context, "litebase.DriverException", errorMsg);
}

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
#if defined(POSIX) || defined(ANDROID)
/**
 * Opens a disk file to store tables and put it in the files list.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param mode Indicates if the file must be created or just opened. 
 * @return The error code if an error occurred or zero if the function succeeds.
 */
int32 openFile(Context context, XFile* xFile, int32 mode)
{  
   LOCKVAR(files);
   if (filesList.count < MAX_OPEN_FILES)  // There is space in the list.
   {
      filesList.list[filesList.count++] = xFile;
      xFile->timeStamp = TC_getTimeStamp();
   }
   else // No space: the last used file must be removed from the list and closed.
   {
      int32 ret = MAX_OPEN_FILES,
            minStamp,
            oldest = 0;
      XFile** list = filesList.list;
      XFile* file;

      xFile->timeStamp = minStamp = TC_getTimeStamp();
      while (--ret > 0)
         if (list[ret]->timeStamp < minStamp)
            minStamp = list[oldest = ret]->timeStamp;

      if ((file = list[oldest])->cacheIsDirty && !(ret = flushCache(context, file)))
      {
         UNLOCKVAR(files);
         return 1;
      }

      if ((ret = lbfileClose(&file->file)))
      {
         UNLOCKVAR(files);
         return ret;
      }
      fileInvalidate(list[oldest]->file);
      list[oldest] = xFile;      
   }
   UNLOCKVAR(files);
   return lbfileCreate(&xFile->file, xFile->fullPath, mode);
}

/**
 * Reopens a file if needed.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return The error code if an error occurred or zero if the function succeeds.
 */
int32 reopenFileIfNeeded(Context context, XFile* xFile)
{
   if (fileIsValid(xFile->file)) // If the file is opened, just updates its time stamp.
   {
      xFile->timeStamp = TC_getTimeStamp();
      return 0;
   }
   else
      return openFile(context, xFile, READ_WRITE); // If the file was closed, reopens it.
}

/**
 * Removes a file from the file list, which is open.
 *
 * @param xFile A pointer to the normal file structure.
 */
void removeFileFromList(XFile* xFile)
{
   int32 i;
   XFile** list;

   LOCKVAR(files);
   i = filesList.count;
   list = filesList.list;
   while (--i >= 0 && xFile != list[i]);
   if (i >= 0)
   {
      list[i] = list[--filesList.count];
      list[filesList.count] = null;
   }
   UNLOCKVAR(files);
}
#endif

