// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define SKIP_TESTS_PDB

TESTCASE(tiPDBF_create_sssi) // totalcross/io/PDBFile native private void create(String name, String creator, String type, int mode) throws totalcross.io.FileNotFoundException, totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;
   
   // Empty parameters. Test must pass because the constructor already checks them.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile"); //, &p.obj[0]);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "", -1);
   p.i32[0] = CREATE_EMPTY; 
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Mode READ_WRITE with a PDB file that not exist means exception.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 3;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   currentContext->thrownException = null;

   // Mode CREATE_EMPTY with a PDB file that not exist does not throw an exception.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 5;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TET", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TET", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TET", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi.", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssixxx", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Doesn't fail passing invalid name because it's checked in the constructor.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Delete if exists and create tiPDBF_create_sssi.TEST.TEST
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_create_sssi", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Open tiPDBF_create_sssii.TEST.TEST
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   finish: ;
}
TESTCASE(tiPDBF_rename_s) // totalcross/io/PDBFile native public void rename(String newName) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete tiPDBF_rename_sii_ren.TEST.TEST, if exists
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_ren", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   if (PDBFile_openRef(p.obj[0]) != null) // tiPDBF_rename_s_ren.TEST.TEST already exists
   {
      tiPDBF_delete(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
      tiPDBF_nativeClose(&p);
      ASSERT1_EQUALS(NotNull, currentContext->thrownException); // It's not possible to close a deleted PDBFile.
      currentContext->thrownException = null;
   }

   // Create tiPDBF_rename_sii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Rename tiPDBF_rename_s.TEST.TEST to tiPDBF_rename_s_ren.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_ren.TEST.TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   tiPDBF_rename_s(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert tiPDBF_rename_s_ren.TEST.TEST exists
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s_ren", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Assert tiPDBF_rename_s.TEST.TEST does not exist
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_rename_s", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[FileNotFoundException]);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   currentContext->thrownException = null;
   finish: ;
}
TESTCASE(tiPDBF_addRecord_i) // totalcross/io/PDBFile native public int addRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_addRecord_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_addRecord_i", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_addRecord_ii) // totalcross/io/PDBFile native public int addRecord(int size, int pos) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   int32 i32Array[2];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_addRecord_ii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_addRecord_ii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record at the first position
   p.i32[0] = 512;
   p.i32[1] = 0;
   tiPDBF_addRecord_ii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Add another record at the first position
   p.i32[0] = 256;
   tiPDBF_addRecord_ii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_resizeRecord_i) // totalcross/io/PDBFile native public void resizeRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   int32 i32Array[1];
   int32 hvRecordLength;
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_resizeRecord_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_resizeRecord_i", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

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

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_nativeClose) // totalcross/io/PDBFile native private void nativeClose() throws totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // Create tiPDBF_create_sssi.TEST.TEST
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_nativeClose_", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_nativeClose_", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   
   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   finish: ;
}
TESTCASE(tiPDBF_delete) // totalcross/io/PDBFile native public boolean delete() throws totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // Create tiPDBF_create_sssi.TEST.TEST
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_delete_i_", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   // Try to open tiPDBF_create_sssi.TEST.TEST (must fail)
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[FileNotFoundException]);
   currentContext->thrownException = null;
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   finish: ;
}
TESTCASE(tiPDBF_listPDBs_ii) // totalcross/io/PDBFile native public static String []listPDBs(int creatorId, int type);
{
   int32 i, count;
   TCObject listObj;
   TCObject* list;
   CharP s;
   bool found = false;
   int32 i32Array[2];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Create tiPDBF_ListPDBFiles_ii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_listPDBs_ii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
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

   list = (TCObject*) ARRAYOBJ_START(listObj);
   for (i = 0; i < count && !found; i ++) // search for tiPDBF_ListPDBFiles_ii
   {
      s = String2CharP(list[i]);
      if (xstrcmp(s, "tiPDBF_listPDBs_ii.TEST.TEST") == 0)
         found = true;
      xfree(s);
   }

   ASSERT1_EQUALS(True, found);

   // Deletes the file.
   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_listPDBs_ii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_deleteRecord) // totalcross/io/PDBFile native public void deleteRecord() throws totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_deleteRecord.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_deleteRecord.TEST.TEST", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record and delete it
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was deleted
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);
   ASSERT2_EQUALS(I32, -1, PDBFile_hvRecordPos(p.obj[0]));

   // Move back to the first record and delete it
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(True, p.retI);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was deleted
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);
   ASSERT2_EQUALS(I32, -1, PDBFile_hvRecordPos(p.obj[0]));

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_getRecordCount) // totalcross/io/PDBFile native public int getRecordCount() throws totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_getRecordCount_.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_getRecordCount_", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Delete records
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_deleteRecord(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_setRecordPos_i) // totalcross/io/PDBFile native public boolean setRecordPos(int pos) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   int32 i32Array[1];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setRecordPos_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setRecordPos_i", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 512;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Add the second record
   p.i32[0] = 256;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (512 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 512, PDBFile_hvRecordLength(p.obj[0]));

   // Move forward to the second record (256 bytes)
   p.i32[0] = 1;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 256, PDBFile_hvRecordLength(p.obj[0]));

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_readBytes_Bii) // totalcross/io/PDBFile native int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException;
{
   CharP buf;
   int32 i, recSize = 128;
   int32 i32Array[3];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_readBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_readBytes_Bii.TEST.TEST", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   p.obj[1] = createByteArray(currentContext, recSize);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = recSize;
   p.i32[2] = false;
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (128 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 128, PDBFile_hvRecordLength(p.obj[0]));

   // Read bytes
   p.i32[1] = ARRAYOBJ_LEN(p.obj[1]);
   p.i32[2] = true;
   xmemzero(buf, p.i32[1]); // clear the buffer

   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, 128, p.retI); 

   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[0] = 0;
   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, 128, p.retI); 

   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[1] = recSize;
   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   for (i = 0; i < p.i32[1]; i ++) // check bytes read
      ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_writeBytes_Bii) // totalcross/io/PDBFile native int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException;
{
   CharP buf;
   int32 i, recSize = 128;
   int32 i32Array[3];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_writeBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_writeBytes_Bii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   p.obj[1] = createByteArray(currentContext, recSize - 1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = ARRAYOBJ_LEN(p.obj[1]);
   p.i32[2] = false;
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, 127, p.retI); 

   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[1] = recSize - 1;
   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   p.i32[1] = 1;
   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, 1, p.retI); 

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Move back to the first record (128 bytes)
   p.i32[0] = 0;
   tiPDBF_setRecordPos_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 128, PDBFile_hvRecordLength(p.obj[0]));

   // Read bytes
   PDBFile_hvRecordOffset(p.obj[0]) = 0;
   p.i32[0] = 0;
   p.i32[1] = recSize;
   p.i32[2] = true;
   for (i = 0; i < p.i32[1]; i ++) // clear the buffer
      buf[i] = 0;

   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   for (i = 0; i < 127; i ++) // check bytes read
      ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);
   
   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_inspectRecord_Bii) // totalcross/io/PDBFile native public int inspectRecord(byte buf[], int recordPos, int offsetInRec) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   CharP buf;
   int32 i, recSize = 128;
   int32 i32Array[3];
   TNMParams p;
   TCObject objs[4];
   TCObject byteArray;

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_inspectRecord_Bi.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_inspectRecord_Bii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert record was created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Write some bytes
   byteArray = createByteArray(currentContext, recSize * 2);
   p.obj[1] = byteArray;
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0;
   p.i32[1] = recSize;
   p.i32[2] = false;
   buf = (CharP) ARRAYOBJ_START(p.obj[1]);

   for (i = 0; i < recSize; i ++) // fill the buffer
      buf[i] = i;

   tiPDBF_readWriteBytes_Biib(&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add a new record
   p.i32[0] = recSize;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiPDBF_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_inspectRecord_Bii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = READ_WRITE;
   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Inspect first record
   p.obj[0] = p.obj[0];
   p.obj[1] = byteArray;
   p.i32[0] = p.i32[1] = 0;
   xmemzero(buf, recSize); 
   
   tiPDBF_inspectRecord_Bii(&p);
   ASSERT2_EQUALS(I32, recSize, p.retI); // cannot read bytes; end of record

   for (i = 0; i < recSize; i++) // check bytes read
      ASSERT2_EQUALS(I8, (int8)i, (int8)buf[i]);
   
   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish:;
}
TESTCASE(tiPDBF_setRecordAttributes_ib) // totalcross/io/PDBFile native public void setRecordAttributes(int recordPos, byte attr) throws totalcross.io.IOException;
{
   int32 i32Array[2];
   TNMParams p;
   TCObject objs[4];
   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setRecordAttributes_ib.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setRecAttributes_ib", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add a record
   p.i32[0] = 128;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

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

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish: ;
}
TESTCASE(tiPDBF_setAttributes_i) // totalcross/io/PDBFile native public void setAttributes(int i);
{
   int32 i32Array[3];
   TNMParams p;
   TCObject objs[4];

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_setAttributes_i.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_setAttributes_i", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
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

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish:;
}
TESTCASE(tiPDBF_searchBytes_Bii) // totalcross/io/PDBFile native public int searchBytes(byte []toSearch, int length, int offsetInRec) throws totalcross.io.IOException;
{
   TNMParams p;
   TCObject objs[4];
   int32 i32Array[3];
   CharP buf = null;
   int32 i;

   p.obj = objs;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.io.PDBFile");
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // Delete if exists and create tiPDBF_searchBytes_Bii.TEST.TEST
   p.obj[1] = createStringObjectFromCharP(currentContext, "tiPDBF_searchBytes_Bii", -1);
   p.obj[2] = createStringObjectFromCharP(currentContext, "TEST", -1);
   p.obj[3] = createStringObjectFromCharP(currentContext, "TEST", -1);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = CREATE_EMPTY;

   tiPDBF_create_sssi(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, PDBFile_openRef(p.obj[0]));

   // Add the first record
   p.i32[0] = 5;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Add the second record
   p.i32[0] = 15;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Assert records were created
   tiPDBF_getRecordCount(&p);
   ASSERT2_EQUALS(I32, 2, p.retI);

   // Write some bytes
   PDBFile_hvRecordOffset(p.obj[0]) += 2;
   p.i32[0] = 0;
   p.i32[1] = 8;
   p.i32[2] = false;
   p.obj[1] = createByteArray(currentContext, 8, null);
   ASSERT1_EQUALS(NotNull, p.obj[1]);

   buf = (CharP) ARRAYOBJ_START(p.obj[1]);
   for (i = 0; i < p.i32[1]; i ++) // fill the buffer
      buf[i] = 'a' + i;

   tiPDBF_readWriteBytes_Biib(&p); (&p);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   // Add the third record
   p.i32[0] = 10;
   tiPDBF_addRecord_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // Release record
   p.i32[0] = -1;
   tiPDBF_setRecordPos_i(&p);

   // Search for written bytes
   p.i32[0] = 6;
   p.i32[1] = 2;
   tiPDBF_searchBytes_Bii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 1, p.retI);

   // Deletes the file.
   tiPDBF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, PDBFile_openRef(p.obj[0]));

   finish:;
}
