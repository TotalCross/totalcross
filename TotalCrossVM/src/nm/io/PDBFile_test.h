/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: PDBFile_test.h,v 1.38 2011-01-04 13:31:16 guich Exp $

#if 1 // MUST CONVERT IT TO THE NEW OBJECT ALLOCATION FORMAT!!!!
#define SKIP_TESTS_PDB
#endif

TESTCASE(tiPDBF_create_si) // totalcross/io/PDBFile native private void create(String name, int mode);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // Fails passing a null path
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile"); //, &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = null;
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   currentContext->thrownException = null;

   // Fails passing an invalid value for mode (< 3)
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 2;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing an invalid value for mode (> 5)
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 6;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TEST.", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST..TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TET", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TESTA", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TESTE.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sixxxxxxxxxxxxxxxx.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // Fails passing invalid name
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IllegalArgumentException]);
   currentContext->thrownException = null;

   // This path format is accepted! (but shouldn't)
   /*
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   */

   // Delete if exists and create tiPDBF_create_si.TEST.TEST
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_si.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Open tiPDBF_create_si.TEST.TEST
   p.i32[0] = READ_WRITE;
   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

#endif
   finish:
      ;
}
TESTCASE(tiPDBF_rename_s) // totalcross/io/PDBFile native public void rename(String newName) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete tiPDBF_rename_sii_renamed.TEST.TEST, if exists
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_renamed.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;

   tiPDBF_create_si(&p);
   currentContext->thrownException = null;
   //ASSERT1_EQUALS(Null, currentContext->thrownException);
   if (PDBFile_openRef(p.obj[0]) != null) // tiPDBF_rename_s_renamed.TEST.TEST already exists
   {
      tiPDBF_delete(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
   }

   // Create tiPDBF_rename_sii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Rename tiPDBF_rename_s.TEST.TEST to tiPDBF_rename_s_renamed.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_renamed.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;
   tiPDBF_rename_s(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert tiPDBF_rename_s_renamed.TEST.TEST exists
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_renamed.TEST.TEST", -1,null);
   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Assert tiPDBF_rename_s.TEST.TEST does not exist
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

#endif
   finish:
      ;
}
TESTCASE(tiPDBF_addRecord_i) // totalcross/io/PDBFile native public int addRecord(int size) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_addRecord_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_addRecord_i.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif
   finish:
      ;
}
TESTCASE(tiPDBF_addRecord_ii) // totalcross/io/PDBFile native public int addRecord(int size, int pos) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_addRecord_ii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_addRecord_ii.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record at the first position
   p.i32[0] = 512;
   p.i32[1] = 0;
   tiPDBF_addRecord_ii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add another record at the first position
   p.i32[0] = 256;
   tiPDBF_addRecord_ii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

#endif
   finish:
      ;
}
TESTCASE(tiPDBF_resizeRecord_i) // totalcross/io/PDBFile native public void resizeRecord(int size) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   int32 hvRecordLength;
   TNMParams p;

   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_resizeRecord_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_resizeRecord_i.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert record size
   hvRecordLength = PDBFile_hvRecordLength(p.obj[0]);
   ASSERT2_EQUALS(I32, p.i32[0], hvRecordLength);

   // Resize record
   p.i32[0] = 256;
   tiPDBF_resizeRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   hvRecordLength = PDBFile_hvRecordLength(p.obj[0]);
   ASSERT2_EQUALS(I32, p.i32[0], hvRecordLength);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_nativeClose) // totalcross/io/PDBFile native private void nativeClose();
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // Create tiPDBF_create_si.TEST.TEST
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_nativeClose_.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));;

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_delete) // totalcross/io/PDBFile native public boolean delete() throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // Create tiPDBF_create_si.TEST.TEST
   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_delete_i_.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Try to open tiPDBF_create_si.TEST.TEST (must fail)
   p.i32[0] = READ_WRITE;
   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_listPDBs_ii) // totalcross/io/PDBFile native public static String []listPDBs(int creatorId, int type);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i, count;
   Object listObj;
   Object* list;
   CharP s;
   bool found = false;

   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Create tiPDBF_ListPDBFiles_ii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_listPDBs_ii.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Now, list every TEST.TEST database
   p.i32[0] = chars2int(TEXT("TEST"));
   p.i32[1] = p.i32[0];
   tiPDBF_listPDBs_ii(&p);

   listObj = p.retO;
   ASSERT1_EQUALS(NotNull, listObj);

   count = ARRAYOBJ_LEN(listObj);
   ASSERT1_EQUALS(True, count > 0);

   list = (Object*) ARRAYOBJ_START(listObj);
   for (i = 0; i < count && !found; i ++) // search for tiPDBF_ListPDBFiles_ii
   {
      s = String2CharP(list[i]);
      if (xstrcmp(s, "tiPDBF_listPDBs_ii") == 0)
         found = true;
      xfree(s);
   }

   ASSERT1_EQUALS(True, found);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_deleteRecord) // totalcross/io/PDBFile native public void deleteRecord() throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_deleteRecord.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_deleteRecord.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record and delete it
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(True, p.retI);

   // Assert record was deleted
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);
   //tiPDBF_getRecordPos(&p);
   //ASSERT2_EQUALS(I32, -1, p.retI);
   ASSERT2_EQUALS(I32, -1, PDBFile_hvRecordPos(p.obj[0]));

   // Move back to the first record and delete it
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(True, p.retI);

   // Assert record was deleted
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);
   //tiPDBF_getRecordPos(&p);
   //ASSERT2_EQUALS(I32, -1, p.retI);
   ASSERT2_EQUALS(I32, -1, PDBFile_hvRecordPos(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_getRecordCount) // totalcross/io/PDBFile native public int getRecordCount();
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_getRecordCount_.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_getRecordCount_.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Delete records
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_setRecordPos_i) // totalcross/io/PDBFile native public boolean setRecordPos(int pos) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[1];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setRecordPos_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setRecordPos_i.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (512 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   ASSERT2_EQUALS(I32, 512, PDBFile_hvRecordLength(p.obj[0]));

   // Move forward to the second record (256 bytes)
   p.i32[0] = 1;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   ASSERT2_EQUALS(I32, 256, PDBFile_hvRecordLength(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_readBytes_Bii) // totalcross/io/PDBFile native public int readBytes(byte []buf, int start, int count);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   CharP buf;
   int32 i, recSize = 128;

   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_readBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_readBytes_Bii.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   p.obj[1] = createByteArray(currentContext, recSize * 2, null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = recSize;
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (128 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   ASSERT2_EQUALS(I32, 128, PDBFile_hvRecordLength(p.obj[0]));

   // Read bytes
   p.i32[1] = ARRAYOBJ_LEN(p.obj[1]);
   xmemzero(buf, p.i32[1]); // clear the buffer

   tiPDBF_readBytes_Bii(&p);
   ASSERT2_EQUALS(I32, -1, p.retI); // cannot read bytes; end of record

   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[0] = 0;
   tiPDBF_readBytes_Bii(&p);
   ASSERT2_EQUALS(I32, -1, p.retI); // cannot read bytes; len is bigger than available bytes

   p.i32[1] = recSize;
   tiPDBF_readBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   for (i = 0; i < p.i32[1]; i ++) // check bytes read
      ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_writeBytes_Bii) // totalcross/io/PDBFile native public int writeBytes(byte []buf, int start, int count);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   CharP buf;
   int32 i, recSize = 128;

   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_writeBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_writeBytes_Bii.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   p.obj[1] = createByteArray(currentContext, recSize * 2, null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = ARRAYOBJ_LEN(p.obj[1]);
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, -1, p.retI); // cannot write bytes; len is bigger than available bytes

   p.i32[1] = recSize;
   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   p.i32[1] = 1;
   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, -1, p.retI); // cannot write bytes; end of record

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (128 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   ASSERT2_EQUALS(I32, 128, PDBFile_hvRecordLength(p.obj[0]));

   // Read bytes
   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[0] = 0;
   p.i32[1] = recSize;
   for (i = 0; i < p.i32[1]; i ++) // clear the buffer
      buf[i] = 0;

   tiPDBF_readBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   for (i = 0; i < p.i32[1]; i ++) // check bytes read
   {
     ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);
     i=i;
   }

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_inspectRecord_Bi) // totalcross/io/PDBFile native public int inspectRecord(byte []buf, int recPosition) throws totalcross.io.IOException;
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   CharP buf;
   int32 i, recSize = 128;

   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   Object byteArray;
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_inspectRecord_Bi.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_inspectRecord_Bi.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   byteArray = createByteArray(currentContext, recSize * 2, null);
   p.obj[1] = byteArray;
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = recSize;
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add a new record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_inspectRecord_Bi.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;
   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Inspect first record
   p.obj[0] = p.obj[0];
   p.obj[1] = byteArray;
   p.i32[0] = 0;
   xmemzero(buf, p.i32[1]);
   /*
   for (i = 0; i < p.i32[1]; i ++) // clear the buffer
      buf[i] = 0;
   */

   tiPDBF_inspectRecord_Bi(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI); // cannot read bytes; end of record

   for (i = 0; i < p.i32[1]; i ++) // check bytes read
   {
      ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);
   }

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_setRecordAttributes_ib) // totalcross/io/PDBFile native public void setRecordAttributes(int recordPos, byte attr);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setRecordAttributes_ib.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setRecAttributes_ib.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = 128;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Change attributes
   p.i32[0] = 0;
   p.i32[1] = dmRecAttrDelete;
   tiPDBF_setRecordAttributes_ib(&p);
   tiPDBF_getRecordAttributes_i(&p);
   ASSERT2_EQUALS(I32, dmRecAttrDelete, p.retI & dmRecAttrDelete);

   // Release record
   p.i32[0] = -1;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Change attributes
   p.i32[0] = 0;
   p.i32[1] = dmRecAttrSecret;
   tiPDBF_setRecordAttributes_ib(&p);
   tiPDBF_getRecordAttributes_i(&p);
   ASSERT2_EQUALS(I32, dmRecAttrSecret, p.retI & dmRecAttrSecret);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_setAttributes_i) // totalcross/io/PDBFile native public void setAttributes(int i);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   int32 i32Array[2];
   TNMParams p;
   Object objs[2];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setAttributes_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setAttributes_i.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Change attributes
   p.i32[0] = 0x0004 | 0x0008; // missing DB_ATTR_APPINFODIRTY
   tiPDBF_setAttributes_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_getAttributes(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0x0004, p.retI & 0x0004);
   ASSERT2_EQUALS(I32, 0x0008, p.retI & 0x0008);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
TESTCASE(tiPDBF_searchBytes_Bii) // totalcross/io/PDBFile native public int searchBytes(byte []toSearch, int length, int offsetInRec);
{
#ifdef SKIP_TESTS_PDB
   TEST_CANNOT_RUN;
#else
   TNMParams p;
   Object objs[2];
   int32 i32Array[2];
   CharP buf = null;
   int32 i;

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   createObject(currentContext, "totalcross.io.PDBFile", &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_searchBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_searchBytes_Bii.TEST.TEST", -1,null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_si(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 5;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the second record
   p.i32[0] = 15;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Write some bytes
   PDBFile_hvRecordOffset(p.obj[0]) += 2;
   p.i32[0] = 0;
   p.i32[1] = 8;
   p.obj[1] = createByteArray(currentContext, 8, null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);

   buf = (CharP) ARRAYOBJ_START(p.obj[1]);
   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = 'a' + i;

   tiPDBF_writeBytes_Bii(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add the third record
   p.i32[0] = 10;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Release record
   p.i32[0] = -1;
   tiPDBF_setRecordPos_i(&p);
   ASSERT2_EQUALS(I32, -1, p.i32[0]);

   // Search for written bytes
   p.i32[0] = 6;
   p.i32[1] = 2;
   tiPDBF_searchBytes_Bii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#endif

   finish:
      ;
}
