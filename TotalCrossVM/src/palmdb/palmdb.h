// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef PALMDB_H
#define PALMDB_H

#define TC_privateXfree                privateXfree
#define TC_privateXmalloc              privateXmalloc
#define TC_privateXrealloc             privateXrealloc
#define TC_createArrayObject           createArrayObject
#define TC_createByteArrayObject       createByteArrayObject
#include "tcvm.h"

/*

This is a Palm Database (PDB) support for SuperWaba WinCE native libraries.
You will call any of the functions as if they were local, but the macros
will redirect the call to the vmGlobals structure. This will maintain compatibility
with existing PalmOS code.

The declarations must be customized based on some defines:

Documentation:

	HandlePDBSearchProcType: callback function for DmListDatabases. Return: 0 to continue the search, 1 to stop it
	DmCloseDatabase:         closes the given database
	DmCreateDatabase:        create a database with the given parameters. Name must have 32 characters (counting \0)
	DmOpenDatabase:          opens the given database. It must have the full pathname specified
	DmOpenDatabaseReadOnly:  opens the given database for READ ONLY. It must have the full pathname specified
	DmReleaseRecord:         release the record. If the database is read-only, it can have many
	                         locked records. Otherwise, if it is read-write, it can have only one record
	DmNewRecord:             creates a new record at the given position. if *atP is < 0, the record is appended
	DmRenameDatabase:        Rename the given opened database to the new parameters
	DmDeleteDatabase:        delete the given opened database
	DmListDatabases:         lists the databases, calling the defined proc. If proc is null,
                            only the total files is returned. userVars can be any pointer
                            to a user defined variable. if recursive is 1, pdb files are
                            searched from SUPERWABA_CORE_PATH recursively
	DmListDatabasesIn:       lists the databases, calling the defined proc. If proc is null,
                            only the total files is returned. userVars can be any pointer
                            to a user defined variable. if recursive is 1, pdb files are
                            searched from the given path recursively
	DmFindAndOpenDB:         Finds and opens the database with the given parameters
	DmNumRecords:            returns the total number of records in the database
	DmWrite:                 writes to the given opened database
	DmResizeRecord:          resizes the given record. The record cannot be locked
	DmRemoveRecord:          removes the given record
	DmGetRecord:             gets a handle to the given record. The record is marked as locked
                            and is copied into a buffer. You must call DmReleaseRecord to
                            unlock this record and also you must free the record by calling xfree
   DmQueryRecord:           gets a handle to the given record. The record may be locked. size ptr
                            may be NULL, or a variable set to 0 (to load the entire record) or the number of
                            bytes to load. Note that you must free the allocated MemHandle by yourself, using xfree
	DmGetRecordAttributes:   get the record attributes, even if the record is locked
	DmSetRecordAttributes:   set the record attributes, even if the record is locked
	DmSetAttributes:         set the database attributes
	DmGetAttributes:         get the database attributes
	DmSetBackupAttribute:    sets the backup attribute of the database
	DmGetRecordSize:         returns the record size of the given record
	DmGetDatabaseInfo:       returns the database type and creator for the given filename. Optionally, returns
                            also the records count and the version information
	DmGetName:               returns the internal name of the file. The buffer must be 32 chars long
	splitPath:               splits the path from the filename. path or name may be null

*/

/* These routines implement the PDB file format on a standard file system.
The PDB is handled, in SuperWaba, by the waba.io.Catalog class.

This file contains the platform independent routines.
The files _ce/_posix contains platform dependent code.

Catalog Premisses:

1. Read only catalogs can have more than one locked records (used when
   loading classes, loading fonts, etc - only by the VM)

2. Read-write catalogs can only have one record opened at a time (as the
   Catalog.java class works)

Notes:

. Locked records have its contents copied into a temp location and are
  directly manipulated

. When a record is closed, if modified, it is written directly to "disk",
  expanding of shrinking the records as necessary.
*/

#define DB_NAME_LENGTH 32 // 31 chars + 1 null terminator
#define DB_FULLNAME_LENGTH DB_NAME_LENGTH + 10

#if defined WINCE || defined WIN32
 typedef HANDLE _HANDLE;
#else
 typedef FILE* _HANDLE;
#endif

//Record Header
//
typedef struct
{
   uint32 offset;
   int8 attr;
   int8 uniqueId[3];
} RecordHeader;

//builds a linked list of locked records
typedef struct tagRecordList
{
   uint8* recPtr;
   struct tagRecordList *next;
   uint16 index;
   uint16 size;
   bool changed;
} RecordList;

//DataBase Header
//
typedef struct t_DatabaseHeader
{
   char name[DB_NAME_LENGTH];
   /** The attribute flags for the database. */
   uint16 attributes;
   /** The application-specific version of the database layout. */
   uint16 version;
   /** The creation date of the database, specified as the number of seconds since
   * 12:00 A.M. on January 1, 1904.
   */
   uint32 creationDate;
   /** The date of the most recent modification of the database, specified as the
   * number of seconds since 12:00 A.M. on January 1, 1904.
   */
   uint32 modificationDate;
   /** The date of the most recent backup of the database, specified as the number
   * of seconds since 12:00 A.M. on January 1, 1904.
   */
   uint32 lastBackupDate;
   /** The modification number of the database. */
   uint32 modificationNumber;
   /** The local offset from the beginning of the database header data to the start
   * of the optional, application-specific appInfo block. This value is set to null
   * for databases that do not include an appInfo block.
   */
   uint32 appInfoOffset;
   /** The local offset from the beginning of the PDB header data to the start of the
   * optional, application-specific sortInfo block. This value is set to null for
   * databases that do not include an sortInfo block.
   */
   uint32 sortInfoOffset;
   uint32 type; // guich@120
   uint32 creator; // guich@120
   /** Used internally by the Palm OS to generate unique identifiers for records on the
   * Palm device when the database is loaded into the device. For PRC databases, this
   * value is normally not used and is set to 0. For PQA databases, this value is not
   * used, and is set to 0.
   */
   uint32 uniqueIDSeed;
   /** The local chunk ID of the next record list in this database. This is 0 if there
   * is no next record list, which is almost always the case. <b>Important!</b> In
   * SuperWaba, this type of database is not supported!
   */
   uint32 nextRecordListID;
   /** Number of records in this db */
   uint16 numRecords;
} DatabaseHeader;

//Catalog
//
typedef struct t_PDBFile
{
   DatabaseHeader dbh;
   _HANDLE fh;
   uint32 fileSize;
   TCHAR fullPath[MAX_PATHNAME];
   int32 mode;
   // queryRecordBuf
   CharP queryRecordBuf;
   // current active record
   int8 *recordBuf; // buffer used to store the current record
   uint32 sizeofRecordBuf;
   uint32 recordSize;
   uint32 originalRecordSize;
   int32 recordIndex;
   bool recordChanged;
   int32 lockedRecords; // count
   uint32 originalModificationNumber;
   bool isNewRecord;

   // a mini hashtable to store the gotRecords
   struct tagRecordList *recPtrList[16];

   // getRecord cache
   int32 recordCachedPos;
   uint32 recordCachedSize;
   RecordHeader recordCachedHeader;

   // to optimize read/writes
   RecordHeader rhs[16];
} TPDBFile, *PDBFile;


//Attributes of a Database
//
 #define dmHdrAttrResDB             0x0001   // Resource database
 #define dmHdrAttrReadOnly          0x0002   // Read Only database
 #define dmHdrAttrAppInfoDirty      0x0004   // Set if Application Info block is dirty
                                                // Optionally supported by an App's conduit
 #define dmHdrAttrBackup            0x0008   // Set if database should be backed up to PC if no app-specific
                                                // synchronization conduit has been supplied.
 #define dmHdrAttrOKToInstallNewer  0x0010   // This tells the backup conduit that it's OK for it to install
                                                // a newer version of this database with a different name if
                                                // the current database is open. This mechanism is used to update the
                                                // Graffiti Shortcuts database, for example.
 #define dmHdrAttrResetAfterInstall 0x0020   // Device requires a reset after this database is installed.
 #define dmHdrAttrCopyPrevention    0x0040   // This database should not be copied to.
 #define dmHdrAttrStream            0x0080   // This database is used for file stream implementation.
 #define dmHdrAttrHidden            0x0100   // This database should generally be hidden from view used to hide
                                                // some apps from the main view of the launcher for example.
                                                // For data (non-resource) databases, this hides the record
                                                // count within the launcher info screen.
 #define dmHdrAttrLaunchableData    0x0200   // This data database (not applicable for executables) can be "launched"
                                                // by passing it's name to it's owner app ('appl' database with same
                                                // creator) using the sysAppLaunchCmdOpenNamedDB action code.
 #define dmHdrAttrRecyclable        0x0400   // This database (resource or record) is recyclable:
                                                // it will be deleted Real Soon Now, generally the next
                                                // time the database is closed.
 #define dmHdrAttrBundle            0x0800   // This database (resource or record) is associated with the application with
                                                // the same creator. It will be beamed and copied along with the application.
 #define dmHdrAttrOpen              0x8000   // Database not closed properly.

//Record Attributes
//
 #define dmRecAttrDelete            0x80  // delete this record next sync
 #define dmRecAttrDirty             0x40  // archive this record next sync
 #define dmRecAttrBusy              0x20  // record currently in use
 #define dmRecAttrSecret            0x10  // "secret" record - password protected

//Other constants
//
 #define dmModeReadWrite            0
 #define dmMaxRecordIndex           0xFFFF

//Return value
 #define dmErrInvalidParam          (0x0200 | 3)

//In Windows, != 0 means success, in PalmOS 0 means success
//This defines a standard approach
 #define errNone 0
#define PALM_SUCCESS errNone
#define PALM_ERROR !errNone

#define DB_IS_OPEN(db) (db != null ? (((VoidP)db->fh) != null) : false )
#define GET_OFFSET(idx) (78 + ((int32)idx << 3))

 typedef CharP MemHandle;
 typedef PDBFile DmOpenRef;

 #define SysNotifyBroadcast(x)

 typedef int32 DmSearchStateType;
 typedef int32* DmSearchStatePtr;

#if defined(WIN32) || defined(WINCE)
 #define PATH_SEPARATOR           '\\'
 #define NO_PATH_SEPARATOR        '/'
 #define PATH_SEPARATOR_STR       TEXT("\\")
 #define NO_PATH_SEPARATOR_STR    TEXT("/")
#else
 #define PATH_SEPARATOR           '/'
 #define NO_PATH_SEPARATOR        '\\'
 #define PATH_SEPARATOR_STR       "/"
 #define NO_PATH_SEPARATOR_STR    "\\"
#endif

 #define BYTE_BUF_LEN 8192

typedef struct
{
   TCHAR    fullPath[MAX_PATHNAME];
   char     name[DB_NAME_LENGTH];
   uint32   creator;
   uint32   type;
   TCHARPs* resultList;
   int32    resultListLen;
   Heap     h;
   _HANDLE  fileRef;
} FileMatches_Vars;

typedef bool (*HandlePDBSearchProcType) (TCHARP fileName, VoidP userVars);

 #define DmGetLastErr()                                  myDmGetLastErr()
 #define DmReleaseRecord(dbP, index, dirty)              myDmReleaseRecord(dbP, index, dirty)
 #define DmCloseDatabase(dbP)                            myDmCloseDatabase(dbP)
 #define DmFindDatabase(currentContext, fileName, dbId)  myDmFindDatabase(currentContext, fileName, dbId)
 #define DmDatabaseInfo(dbId, name, attributes, version, creationDate, modificationDate, backupDate, modificationNumber, appInfoIDP, sortInfoIDP, type, creator)  myDmDatabaseInfo(dbId, name, attributes, version, creationDate, modificationDate, backupDate, modificationNumber, appInfoIDP, sortInfoIDP, type, creator)
 #define DmDeleteDatabase(dbId)                          myDmDeleteDatabase(dbId)
 #define DmCreateDatabase(name, creator, type, isResDB)  myDmCreateDatabase(name, creator, type, isResDB)
 #define DmOpenDatabase(dbId, mode)                      myDmOpenDatabase(dbId, mode)
 #define DmSetDatabaseInfo(dbId, name, attributes, version, creationDate, modificationDate, backupDate, modificationNumber, appInfoIDP, sortInfoIDP, type, creator)  myDmSetDatabaseInfo(dbId, name, attributes, version, creationDate, modificationDate, backupDate, modificationNumber, appInfoIDP, sortInfoIDP, type, creator)
 #define DmNewRecord(dbP, atP, size)                     myDmNewRecord(dbP, atP, size)
 #define DmNumRecords(dbP)                               myDmNumRecords(dbP)
 #define DmResizeRecord(dbP, index, newSize)             myDmResizeRecord(dbP, index, newSize)
 #define DmGetRecord(dbP, index)                         myDmGetRecord(dbP, index)
 #define DmRemoveRecord(dbP, index)                      myDmRemoveRecord(dbP, index)
 #define MemHandleSize(h)                                myMemHandleSize(h)
 #define DmRecordInfo(dbP, index, attrP, uniqueIDP, chunkIDP)  myDmRecordInfo(dbP, index, attrP)
 #define DmSetRecordInfo(dbP, index, attrP, uniqueIDP)         myDmSetRecordInfo(dbP, index, *attrP)
 #define DmWrite(recordP, offset, srcP, bytes)           myDmWrite(recordP, offset, srcP, bytes)
 #define DmQueryRecord(dbP, index)                       myDmQueryRecord(dbP, index)
 #define MemHandleLock(handle)                           myMemHandleLock(handle)
 #define MemHandleUnlock(handle)                         errNone

Err myDmGetLastErr();
Err myDmCloseDatabase(DmOpenRef dbref);
Err myDmReleaseRecord(DmOpenRef dbref, int32 index, bool dirty);
VoidP myDmFindDatabase(Context currentContext, TCHARP fileName, PDBFile dbId);
Err myDmDatabaseInfo(VoidP dbId, TCHARP name, uint16* attributes, uint16* version,
      uint32* creationDate, uint32* modificationDate, uint32* backupDate,
      uint32* modificationNumber, VoidP appInfoIDP, VoidP sortInfoIDP,
      uint32* type, uint32* creator);
Err myDmDeleteDatabase(VoidP dbId);
Err myDmCreateDatabase(TCHARP pdbName, uint32 creator, uint32 type, bool isResDB);
DmOpenRef myDmOpenDatabase(VoidP dbId, uint16 mode);
Err myDmSetDatabaseInfo(VoidP dbId, TCHARP name, uint16* attributes, uint16* version,
      uint32* creationDate, uint32* modificationDate, uint32* backupDate,
      uint32* modificationNumber, VoidP appInfoIDP, VoidP sortInfoIDP,
      uint32* type, uint32* creator);
MemHandle myDmNewRecord (DmOpenRef dbP, uint16 *atP, uint32 size);
uint16 myDmNumRecords (DmOpenRef db);
MemHandle DmResizeRecord (DmOpenRef dbP, uint16 index, uint32 newSize);
MemHandle myDmGetRecord (DmOpenRef dbP, uint16 index);
Err myDmRemoveRecord (DmOpenRef dbP, uint16 index);
uint32 myMemHandleSize (MemHandle h);
Err myDmRecordInfo(DmOpenRef dbref, uint16 index, uint16* attrs); //int8* attrs);
Err myDmSetRecordInfo(DmOpenRef dbref, uint16 index, uint16 attrs); //int8 attrs);
Err myDmWrite (VoidP recordP, uint32 offset, VoidP srcP, uint32 bytes);
MemHandle myDmQueryRecord(DmOpenRef dbref, uint16 index);
CharP myMemHandleLock(MemHandle h);

#if defined _RAPI_
int32 listDatabases(TCHARP searchPath, HandlePDBSearchProcType proc, void *userVars, byte recursive);
void swapDatabaseHeader(DatabaseHeader *src, DatabaseHeader *dst);
#else
TCHARPs* listDatabasesByTypeCreator(uint32 type, uint32 creator, int32* count, Heap h);
#endif

bool endsWithPDB(TCHARP fName);

#endif
