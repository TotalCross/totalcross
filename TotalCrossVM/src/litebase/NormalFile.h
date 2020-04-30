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
 * Declares functions for a normal file, ie, a file that is stored on disk.
 */

#ifndef LITEBASE_NORMAL_FILE_H
#define LITEBASE_NORMAL_FILE_H

#include "Litebase.h"

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
bool nfCreateFile(Context context, CharP name, bool isCreation, bool useCrypto, TCHARP sourcePath, XFile* xFile, int32 cacheSize);

/**
 * Reads file bytes.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param buffer The byte array to read data into.
 * @param count The number of bytes to read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nfReadBytes(Context context, XFile* xFile, uint8* buffer, int32 count);

/**
 * Write bytes in a file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param buffer The byte array to write data from.
 * @param count The number of bytes to write.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nfWriteBytes(Context context, XFile* xFile, uint8* buffer, int32 count);

/**
 * Enlarges the file. This function MUST be called to grow the file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param newSize The new size for the file.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to grow the file.
 */
bool nfGrowTo(Context context, XFile* xFile, uint32 newSize);

/**
 * Sets the current file position.
 *
 * @param xFile A pointer to the normal file structure.
 * @param newPos The new file position. 
 */
void nfSetPos(XFile* xFile, int32 newPos);

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
bool nfRename(Context context, XFile* xFile, CharP newName, TCHARP sourcePath);

/** 
 * Closes a file.
 * 
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to close the file.
 */
bool nfClose(Context context, XFile* xFile);

/** 
 * Removes a file.
 * 
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @param sourcePath The path where the file is stored.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to remove the file.
 */
bool nfRemove(Context context, XFile* xFile, TCHARP sourcePath);

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
bool refreshCache(Context context, XFile* xFile, int32 count);

/**
 * Flushs the cache into the disk.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to write to the file.
 */
bool flushCache(Context context, XFile* xFile);

/**
 * Prepares an error message when an error occurs when dealing with files.
 * 
 * @param context The thread context where the function is being executed.
 * @param errorCode The file error code.
 * @param fileName The file where the error ocurred.
 * @throws DriverException An exception with the error message.
 */
void fileError(Context context, int32 errorCode, CharP fileName);

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
int32 openFile(Context context, XFile* xFile, int32 mode);

/**
 * Reopens a file if needed.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile A pointer to the normal file structure.
 * @return The error code if an error occurred or zero if the function succeeds.
 */
int32 reopenFileIfNeeded(Context context, XFile* xFile);

/**
 * Removes a file from the file list.
 *
 * @param xFile A pointer to the normal file structure.
 */
void removeFileFromList(XFile* xFile);
#endif

#endif
