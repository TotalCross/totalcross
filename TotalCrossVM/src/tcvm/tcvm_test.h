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

#define INT64_TEST_VALUE I64_CONST(-0x12345678ABCE)
#define INT32_TEST_VALUE 0x12345678
#define DOUBLE_TEST_VALUE -1234.567
#define S24_TEST_VALUE -8388608
#define S18_TEST_VALUE -131000
#define S12_TEST_VALUE -2000
#define OBJECT_TEST_VALUE objTestValue
#define INT16_TEST_VALUE 0x1234
#define INT8_TEST_VALUE -126
#define CHAR_TEST_VALUE 0xEEEE
#define S6_TEST_VALUE -31
#define STRING_TEST_VALUE testString

#include "tcz.h"

TCClass testTypesClass;

TESTCASE(VM_LoadTestTCZ) // #6
{
   ConstantPool cp;
   char testtcz[9];
   TCZFile testTCZ;

   xstrcpy(testtcz, "Test.tcz"); // copy to a temp buffer, it may be truncated in tczLoad

   testTCZ = tczLoad(currentContext, testtcz);
   if (testTCZ == null)
   {
      alert("File Test.tcz not found!\nAborting tests.");
      TEST_ABORT;
   }
   cp = testTCZ->header->cp;
   ASSERT_BETWEEN(I32, 2, cp->mtdCount, 1000);
   ASSERT2_EQUALS(Sz, cp->cls[cp->mtd[2][0]], "java.lang.String");
   ASSERT2_EQUALS(Sz, cp->mtdfld[cp->mtd[2][1]], "substring");
   finish: ;
}

TESTCASE(VM_Cleanup) // #100000
{
   UNUSED(tc);
   if (testTypesClass)
   {
      testTypesClass->i32StaticFields = null;
      testTypesClass->v64StaticFields = null;
      testTypesClass->objStaticFields = null;
      freeArray(testTypesClass->i32StaticValues);
      freeArray(testTypesClass->objStaticValues);
      freeArray(testTypesClass->v64StaticValues);
      goto finish; // remove warning
   }
   finish: ;
}

TESTCASE(VM_PrimitiveTypeSizes) // #1
{
   ASSERT2_EQUALS(I32, 2, sizeof(int16));
   ASSERT2_EQUALS(I32, 4, sizeof(int32));
   ASSERT2_EQUALS(I32, 8, sizeof(int64));
   ASSERT2_EQUALS(I32, 8, sizeof(double));
   ASSERT2_EQUALS(I32, 4, sizeof(float));
   finish: ;
}

TESTCASE(VM_TestSetJmp) // #2
{
   jmp_buf buf;
   int ret;
   ret = setjmp(buf);
   if (ret != 0)
   {
      ASSERT2_EQUALS(I32, ret, 1);
      goto finish;
   }
   longjmp(buf,1); // go back
   TEST_FAIL(tc,"Long jump didn't work");
   finish: ;
}

TESTCASE(VM_CodeUnion)
{
#if 0
   TCode c;

   ASSERT2_EQUALS(I32, sizeof(c), 4);

   // MOV

   c.u32.u32 = 0;                     ASSERT2_EQUALS(I32, 0, c.u32.u32);
   c.u32.u32 = 0x12345678;            ASSERT2_EQUALS(I32, 0x12345678, c.u32.u32); // TODO must check endians too!

   c.s24.op = 255;                    ASSERT2_EQUALS(I32, 255, c.s24.op);
   c.s24.desloc = 0;                  ASSERT2_EQUALS(I32, 0, c.s24.desloc);
   c.s24.desloc = -1234567;           ASSERT2_EQUALS(I32, -1234567, c.s24.desloc);
   c.s24.desloc = 1234567;            ASSERT2_EQUALS(I32, 1234567, c.s24.desloc);

   c.reg_reg.reg0 = 63;               ASSERT2_EQUALS(I32, 63, c.reg_reg.reg0);
   c.reg_reg.reg0 = 0;                ASSERT2_EQUALS(I32, 0, c.reg_reg.reg0);
   c.reg_reg.reg1 = 63;               ASSERT2_EQUALS(I32, 63, c.reg_reg.reg1);
   c.reg_reg.reg1 = 0;                ASSERT2_EQUALS(I32, 0, c.reg_reg.reg1);

   c.field_reg.this = 63;          ASSERT2_EQUALS(I32, 63, c.field_reg.this);
   c.field_reg.this = 0;           ASSERT2_EQUALS(I32, 0, c.field_reg.this);
   c.field_reg.reg  = 63;          ASSERT2_EQUALS(I32, 63, c.field_reg.reg);
   c.field_reg.reg  = 0;           ASSERT2_EQUALS(I32, 0, c.field_reg.reg);
   c.field_reg.sym = 4095;         ASSERT2_EQUALS(I32, 4095, c.field_reg.sym);
   c.field_reg.sym = 0;            ASSERT2_EQUALS(I32, 0, c.field_reg.sym);

   c.mtd.this = 63;                   ASSERT2_EQUALS(I32, 63, c.mtd.this);
   c.mtd.this = 0;                    ASSERT2_EQUALS(I32, 0, c.mtd.this);
   c.mtd.retOr1stParam  = 63;         ASSERT2_EQUALS(I32, 63, c.mtd.retOr1stParam);
   c.mtd.retOr1stParam  = 0;          ASSERT2_EQUALS(I32, 0, c.mtd.retOr1stParam);
   c.mtd.sym = 4095;                  ASSERT2_EQUALS(I32, 4095, c.mtd.sym);
   c.mtd.sym = 0;                     ASSERT2_EQUALS(I32, 0, c.mtd.sym);

   c.static_reg.reg  = 63;         ASSERT2_EQUALS(I32, 63, c.static_reg.reg);
   c.static_reg.reg  = 0;          ASSERT2_EQUALS(I32, 0, c.static_reg.reg);
   c.static_reg.sym = 65535;       ASSERT2_EQUALS(I32, 65535, c.static_reg.sym);
   c.static_reg.sym = 0;           ASSERT2_EQUALS(I32, 0, c.static_reg.sym);

   c.reg_ar.base = 63;                ASSERT2_EQUALS(I32, 63, c.reg_ar.base);
   c.reg_ar.base = 0;                 ASSERT2_EQUALS(I32, 0, c.reg_ar.base);
   c.reg_ar.reg = 63;                 ASSERT2_EQUALS(I32, 63, c.reg_ar.reg);
   c.reg_ar.reg = 0;                  ASSERT2_EQUALS(I32, 0, c.reg_ar.reg);
   c.reg_ar.idx = 63;                 ASSERT2_EQUALS(I32, 63, c.reg_ar.idx);
   c.reg_ar.idx = 0;                  ASSERT2_EQUALS(I32, 0, c.reg_ar.idx);

   c.reg_sym.sym = 65535;             ASSERT2_EQUALS(I32, 65535, c.reg_sym.sym);
   c.reg_sym.sym = 0;                 ASSERT2_EQUALS(I32, 0, c.reg_sym.sym);
   c.reg_sym.reg = 63;                ASSERT2_EQUALS(I32, 63, c.reg_sym.reg);
   c.reg_sym.reg = 0;                 ASSERT2_EQUALS(I32, 0, c.reg_sym.reg);

   c.s18_reg.s18 = -131072;           ASSERT2_EQUALS(I32, -131072, c.s18_reg.s18);
   c.s18_reg.s18 = 131071;            ASSERT2_EQUALS(I32, 131071, c.s18_reg.s18);
   c.s18_reg.reg = 63;                ASSERT2_EQUALS(I32, 63, c.s18_reg.reg);
   c.s18_reg.reg = 0;                 ASSERT2_EQUALS(I32, 0, c.s18_reg.reg);

   // OPERATIONS

   c.reg_reg_reg.reg0 = 63;           ASSERT2_EQUALS(I32, 63, c.reg_reg_reg.reg0);
   c.reg_reg_reg.reg0 = 0;            ASSERT2_EQUALS(I32, 0,  c.reg_reg_reg.reg0);
   c.reg_reg_reg.reg1 = 63;           ASSERT2_EQUALS(I32, 63, c.reg_reg_reg.reg1);
   c.reg_reg_reg.reg1 = 0;            ASSERT2_EQUALS(I32, 0,  c.reg_reg_reg.reg1);
   c.reg_reg_reg.reg2 = 63;           ASSERT2_EQUALS(I32, 63, c.reg_reg_reg.reg2);
   c.reg_reg_reg.reg2 = 0;            ASSERT2_EQUALS(I32, 0,  c.reg_reg_reg.reg2);

   c.reg_reg_s12.reg0 = 63;           ASSERT2_EQUALS(I32, 63, c.reg_reg_s12.reg0);
   c.reg_reg_s12.reg0 = 0;            ASSERT2_EQUALS(I32, 0,  c.reg_reg_s12.reg0);
   c.reg_reg_s12.reg1 = 63;           ASSERT2_EQUALS(I32, 63, c.reg_reg_s12.reg1);
   c.reg_reg_s12.reg1 = 0;            ASSERT2_EQUALS(I32, 0,  c.reg_reg_s12.reg1);
   c.reg_reg_s12.s12  = -2048;        ASSERT2_EQUALS(I32, -2048,  c.reg_reg_s12.s12);
   c.reg_reg_s12.s12  = 2047;         ASSERT2_EQUALS(I32, 2047, c.reg_reg_s12.s12);

   c.reg_s6_ar.reg  = 63;             ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.reg);
   c.reg_s6_ar.reg  = 0;              ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.reg);
   c.reg_s6_ar.base = 63;             ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.base);
   c.reg_s6_ar.base = 0;              ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.base);
   c.reg_s6_ar.idx  = 63;             ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.idx);
   c.reg_s6_ar.idx  = 0;              ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.idx);
   c.reg_s6_ar.s6   = -32;            ASSERT2_EQUALS(I32, -32, c.reg_s6_ar.s6);
   c.reg_s6_ar.s6   = 31;             ASSERT2_EQUALS(I32, 31, c.reg_s6_ar.s6);

   c.reg_reg_sym.reg0  = 63;          ASSERT2_EQUALS(I32, 63, c.reg_reg_sym.reg0);
   c.reg_reg_sym.reg0  = 0;           ASSERT2_EQUALS(I32, 0,  c.reg_reg_sym.reg0);
   c.reg_reg_sym.sym = 4095;          ASSERT2_EQUALS(I32, 4095, c.reg_reg_sym.sym);
   c.reg_reg_sym.sym = 0;             ASSERT2_EQUALS(I32, 0,  c.reg_reg_sym.sym);
   c.reg_reg_sym.reg1  = 63;          ASSERT2_EQUALS(I32, 63, c.reg_reg_sym.reg1);
   c.reg_reg_sym.reg1  = 0;           ASSERT2_EQUALS(I32, 0,  c.reg_reg_sym.reg1);

   c.this_reg_reg.this = 4095;        ASSERT2_EQUALS(I32, 4095, c.this_reg_reg.this);
   c.this_reg_reg.this = 0;           ASSERT2_EQUALS(I32, 0,  c.this_reg_reg.this);
   c.this_reg_reg.reg0  = 63;         ASSERT2_EQUALS(I32, 63, c.this_reg_reg.reg0);
   c.this_reg_reg.reg0  = 0;          ASSERT2_EQUALS(I32, 0,  c.this_reg_reg.reg0);
   c.this_reg_reg.reg1  = 63;         ASSERT2_EQUALS(I32, 63, c.this_reg_reg.reg1);
   c.this_reg_reg.reg1  = 0;          ASSERT2_EQUALS(I32, 0,  c.this_reg_reg.reg1);

   c.this_reg_s6.this = 4095;         ASSERT2_EQUALS(I32, 4095, c.this_reg_s6.this);
   c.this_reg_s6.this = 0;            ASSERT2_EQUALS(I32, 0,  c.this_reg_s6.this);
   c.this_reg_s6.regI  = 63;          ASSERT2_EQUALS(I32, 63, c.this_reg_s6.regI);
   c.this_reg_s6.regI  = 0;           ASSERT2_EQUALS(I32, 0,  c.this_reg_s6.regI);
   c.this_reg_s6.s6   = -32;          ASSERT2_EQUALS(I32, -32, c.this_reg_s6.s6);
   c.this_reg_s6.s6   = 31;           ASSERT2_EQUALS(I32, 31, c.this_reg_s6.s6);

   c.reg_s6_ar.reg  = 63;             ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.reg);
   c.reg_s6_ar.reg  = 0;              ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.reg);
   c.reg_s6_ar.s6   = -32;            ASSERT2_EQUALS(I32, -32, c.reg_s6_ar.s6);
   c.reg_s6_ar.s6   = 31;             ASSERT2_EQUALS(I32, 31, c.reg_s6_ar.s6);
   c.reg_s6_ar.base = 63;             ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.base);
   c.reg_s6_ar.base = 0;              ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.base);
   c.reg_s6_ar.idx = 63;              ASSERT2_EQUALS(I32, 63, c.reg_s6_ar.idx);
   c.reg_s6_ar.idx = 0;               ASSERT2_EQUALS(I32, 0,  c.reg_s6_ar.idx);

   c.reg_sym_sdesloc.reg  = 63;       ASSERT2_EQUALS(I32, 63, c.reg_sym_sdesloc.reg);
   c.reg_sym_sdesloc.reg  = 0;        ASSERT2_EQUALS(I32, 0,  c.reg_sym_sdesloc.reg);
   c.reg_sym_sdesloc.sym = 4095;      ASSERT2_EQUALS(I32, 4095, c.reg_sym_sdesloc.sym);
   c.reg_sym_sdesloc.sym = 0;         ASSERT2_EQUALS(I32, 0,  c.reg_sym_sdesloc.sym);
   c.reg_sym_sdesloc.desloc = -32;    ASSERT2_EQUALS(I32, -32, c.reg_sym_sdesloc.desloc);
   c.reg_sym_sdesloc.desloc = 31;     ASSERT2_EQUALS(I32, 31,  c.reg_sym_sdesloc.desloc);

   c.reg_this_sdesloc.reg  = 63;      ASSERT2_EQUALS(I32, 63, c.reg_this_sdesloc.reg);
   c.reg_this_sdesloc.reg  = 0;       ASSERT2_EQUALS(I32, 0,  c.reg_this_sdesloc.reg);
   c.reg_this_sdesloc.this = 4095;    ASSERT2_EQUALS(I32, 4095, c.reg_this_sdesloc.this);
   c.reg_this_sdesloc.this = 0;       ASSERT2_EQUALS(I32, 0,  c.reg_this_sdesloc.this);
   c.reg_this_sdesloc.desloc = -32;   ASSERT2_EQUALS(I32, -32, c.reg_this_sdesloc.desloc);
   c.reg_this_sdesloc.desloc = 31;    ASSERT2_EQUALS(I32, 31,  c.reg_this_sdesloc.desloc);

   c.reg_arl_s12.regI  = 63;          ASSERT2_EQUALS(I32, 63, c.reg_arl_s12.regI);
   c.reg_arl_s12.regI  = 0;           ASSERT2_EQUALS(I32, 0,  c.reg_arl_s12.regI);
   c.reg_arl_s12.base  = 63;          ASSERT2_EQUALS(I32, 63, c.reg_arl_s12.base);
   c.reg_arl_s12.base  = 0;           ASSERT2_EQUALS(I32, 0,  c.reg_arl_s12.base);
   c.reg_arl_s12.desloc  = -2048;     ASSERT2_EQUALS(I32, -2048, c.reg_arl_s12.desloc);
   c.reg_arl_s12.desloc  = 2047;      ASSERT2_EQUALS(I32, 2047,  c.reg_arl_s12.desloc);

   c.reg.reg = 63;                    ASSERT2_EQUALS(I32, 63, c.reg.reg);
   c.reg.reg = 0;                     ASSERT2_EQUALS(I32, 0,  c.reg.reg);

   c.sym.sym = 65535;                 ASSERT2_EQUALS(I32, 65535, c.sym.sym);
   c.sym.sym = 0;                     ASSERT2_EQUALS(I32, 0,  c.sym.sym);

   // others
   c.newarray.regO = 0;               ASSERT2_EQUALS(I32, 0, c.newarray.regO);
   c.newarray.regO = 63;              ASSERT2_EQUALS(I32, 63, c.newarray.regO);
   c.newarray.sym = 0;                ASSERT2_EQUALS(I32, 0, c.newarray.sym);
   c.newarray.sym = 4095;             ASSERT2_EQUALS(I32, 4095, c.newarray.sym);
   c.newarray.lenOrRegIOrDims  = 0;   ASSERT2_EQUALS(I32, 0, c.newarray.lenOrRegIOrDims);
   c.newarray.lenOrRegIOrDims  = 63;  ASSERT2_EQUALS(I32, 63, c.newarray.lenOrRegIOrDims);
#else
   TEST_SKIP;
#endif
   finish: ;
}

//////////////////////////// AUXILIARY FUNCTIONS ////////////////////////////////
static TCObject objTestValue;
static TCObject newTestTypesInstance(Context currentContext)
{
   CharP tt = "TestTypes"; // for class definitions, see classinfo.test, function createTestTypesClass
   TCClass c;
   TCObject o;

   o = createObjectWithoutCallingDefaultConstructor(currentContext, tt); // do NOT call the default constructor, it is buggy
   setObjectLock(o, UNLOCKED);
   if (!o)
   {
      alert("Cannot find (or corrupted) TestTypes.tcz!\nAborting tests");
      return null;
   }
   testTypesClass = c = OBJ_CLASS(o);
   if (!objTestValue)
   {
      objTestValue = createStringObjectFromCharP(currentContext, "TotalCross",10);
      //setObjectLock(objTestValue, UNLOCKED);
   }
   // initialize the array instance fields
   FIELD_OBJ(o, c, 1) = createByteArray(currentContext, 2);
   setObjectLock(FIELD_OBJ(o, c, 1), UNLOCKED);
   FIELD_OBJ(o, c, 2) = createArrayObject(currentContext, SHORT_ARRAY, 2);
   setObjectLock(FIELD_OBJ(o, c, 2), UNLOCKED);
   FIELD_OBJ(o, c, 3) = createArrayObject(currentContext, INT_ARRAY, 2);
   setObjectLock(FIELD_OBJ(o, c, 3), UNLOCKED);
   FIELD_OBJ(o, c, 4) = createCharArray(currentContext, 2);
   setObjectLock(FIELD_OBJ(o, c, 4), UNLOCKED);
   FIELD_OBJ(o, c, 5) = createArrayObject(currentContext, DOUBLE_ARRAY, 2);
   setObjectLock(FIELD_OBJ(o, c, 5), UNLOCKED);
   FIELD_OBJ(o, c, 6) = createArrayObject(currentContext, LONG_ARRAY, 2);
   setObjectLock(FIELD_OBJ(o, c, 6), UNLOCKED);
   FIELD_OBJ(o, c, 7) = createStringArray(currentContext, 2);
   setObjectLock(FIELD_OBJ(o, c, 7), UNLOCKED);

   // now we'll use a trick: to simplify the creation of the static fields,
   // we'll just copy the instance fields over the class fields.
   // This is safe, since the values were already used to create the
   // defaultInstanceValues and they are now useless (only used by the compiler).
   // TODO make sure these fields are not destroyed (set them to null later).
   c->i32StaticFields = c->i32InstanceFields;
   c->v64StaticFields = c->v64InstanceFields;
   c->objStaticFields = c->objInstanceFields;
   c->i32StaticValues = newPtrArrayOf(Int32, ARRAYLENV(c->i32StaticFields), null);
   c->objStaticValues = newPtrArrayOf(TCObject, ARRAYLENV(c->objStaticFields), null);
   c->v64StaticValues = newPtrArrayOf(Double, ARRAYLENV(c->v64StaticFields), null);
   return o;
}

static TCObject testString;
static TCObject testTypesInstance;
static TMethod testMethod;

static Method initMethod(Context currentContext, int op)
{
   static TCode code[30];
   if (!testTypesInstance)
   {
      testTypesInstance = newTestTypesInstance(currentContext);
      if (testTypesInstance == null)
         return null;
   }

   xmemzero(code, sizeof(code)); // fill code with BREAKs (0)
   xmemzero(currentContext->regIStart, STARTING_REGI_SIZE*sizeof(currentContext->regI[0]));
   xmemzero(currentContext->reg64Start,STARTING_REG64_SIZE*sizeof(currentContext->reg64[0]));
   xmemzero(currentContext->regOStart, STARTING_REGO_SIZE*sizeof(currentContext->regO[0]));

   currentContext->thrownException = null;
   currentContext->regO[0] = testTypesInstance; // instance fields assume that the class instance is in regO[0]
   code[0].s24.op = op;
   testMethod.flags.isStatic = true;
   testMethod.class_ = testTypesClass;
   testMethod.code = code;
   return &testMethod;
}

static void* newArrayTest(Context currentContext, Type t) // returns the start of the array
{
   TCObject array = null;
   switch (t)
   {
      case Type_Byte  : array = FIELD_OBJ(testTypesInstance, testTypesClass, 1); break;
      case Type_Short : array = FIELD_OBJ(testTypesInstance, testTypesClass, 2); break;
      case Type_Int   : array = FIELD_OBJ(testTypesInstance, testTypesClass, 3); break;
      case Type_Char  : array = FIELD_OBJ(testTypesInstance, testTypesClass, 4); break;
      case Type_Double: array = FIELD_OBJ(testTypesInstance, testTypesClass, 5); break;
      case Type_Long  : array = FIELD_OBJ(testTypesInstance, testTypesClass, 6); break;
      case Type_Object: array = FIELD_OBJ(testTypesInstance, testTypesClass, 7); break;
      default:
         break;
   }
   testMethod.code[0].reg_ar.reg = 2;
   currentContext->regI[testMethod.code[0].reg_ar.idx  = 0] = 1;
   currentContext->regO[testMethod.code[0].reg_ar.base = 1] = array;
   return ARRAYOBJ_START(array);
}
static bool arCheck(Context currentContext, struct TestSuite *tc, int op)
{
   Method m = &testMethod;
   m->code[0].reg_ar.op = op;
   // array checks
   currentContext->regI[0] = 3; // array[3]
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;
   currentContext->regI[0] = -1; // array[-1]
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;
   currentContext->regI[0] = 0; // array[0]
   currentContext->regO[1] = 0; // null[0]
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   return true;
finish:
   return false;
}

static TCObject initExtTest(Context currentContext, struct TestSuite *tc, Method m, RegType t, bool isStatic)
{
   static TCObject ext;
   static uint16 instanceField[4]  = {1,2,3,4};  // positions for cp.identExt
   static uint16 staticField[4] = {1,2,3,4};

   if (!ext)
   {
      ext = createObject(currentContext, "TestExt");
      setObjectLock(ext, UNLOCKED);
   }
   // force NPE and test it
   currentContext->regO[0] = 0;
   if (!isStatic)
   {
      m->code[0].field_reg.this_ = 0;
      executeMethod(currentContext, m);
      ASSERT1_EQUALS(NotNull, currentContext->thrownException);
      ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[isStatic ? ClassNotFoundException : NullPointerException]);
      currentContext->thrownException = null;
   }
   // ok, now test with the value
   if (isStatic)
   {
      m->code[0].static_reg.sym = staticField[t];
      m->code[0].static_reg.reg = 0;
   }
   else
   {
      currentContext->regO[m->code[0].field_reg.this_ = 2] = ext; // instance fields assume that the class instance is in regO
      m->code[0].field_reg.sym = instanceField[t];
      m->code[0].field_reg.reg = 0;
   }
   return ext;
finish:
   return NULL;
}

/////////////////////////////// MOV ///////////////////////////////

TESTCASE(VM_BREAK) // must make this test run before the other instruction tests. #7
{
   Method m = initMethod(currentContext,BREAK);
   if (m == null)
      TEST_ABORT;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   // nothing useful to test. if something goes wrong, a GPF will probably occur.
finish: ;
}
TESTCASE(VM_MOV_regI_regI)
{
   Method m = initMethod(currentContext,MOV_regI_regI);
   currentContext->regI[1] = INT32_TEST_VALUE;
   m->code[0].reg_reg.reg1 = 1; // src
   m->code[0].reg_reg.reg0 = 0; // dst
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[1], INT32_TEST_VALUE);
   ASSERT2_EQUALS(I32, currentContext->regI[0], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regO_regO)
{
   Method m = initMethod(currentContext,MOV_regO_regO);
   currentContext->regO[1] = OBJECT_TEST_VALUE;
   m->code[0].reg_reg.reg1 = 1; // src
   m->code[0].reg_reg.reg0 = 0; // dst
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, currentContext->regO[1], OBJECT_TEST_VALUE);
   ASSERT2_EQUALS(Obj, currentContext->regO[0], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regO_null)
{
   Method m = initMethod(currentContext,MOV_regO_null);
   currentContext->regO[1] = OBJECT_TEST_VALUE;
   m->code[0].reg.reg = 1;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(Null, currentContext->regO[1]);
finish: ;
}
TESTCASE(VM_MOV_reg64_reg64)
{
   // test with double
   Method m = initMethod(currentContext,MOV_reg64_reg64);
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   m->code[0].reg_reg.reg1 = 1; // src
   m->code[0].reg_reg.reg0 = 0; // dst
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[1], DOUBLE_TEST_VALUE);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], DOUBLE_TEST_VALUE);

   // test with long
   m = initMethod(currentContext,MOV_reg64_reg64);
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   m->code[0].reg_reg.reg1 = 1; // src
   m->code[0].reg_reg.reg0 = 0; // dst
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[1], INT64_TEST_VALUE);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], INT64_TEST_VALUE);finish: ;
}
TESTCASE(VM_MOV_regI_s18)
{
   Method m = initMethod(currentContext,MOV_regI_s18);
   currentContext->regI[1] = 0;
   m->code[0].s18_reg.reg = 1; // dst
   m->code[0].s18_reg.s18 = S18_TEST_VALUE; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[1], S18_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regD_s18)
{
   Method m = initMethod(currentContext,MOV_regD_s18);
   REGD(currentContext->reg64)[1] = 0;
   m->code[0].s18_reg.reg = 1; // dst
   m->code[0].s18_reg.s18 = S18_TEST_VALUE; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[1], S18_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regL_s18)
{
   Method m = initMethod(currentContext,MOV_regL_s18);
   REGL(currentContext->reg64)[1] = 0;
   m->code[0].s18_reg.reg = 1; // dst
   m->code[0].s18_reg.s18 = S18_TEST_VALUE; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[1], S18_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regI_sym)
{
   Method m = initMethod(currentContext,MOV_regI_sym);
   int32 orig = m->class_->cp->i32[1];
   m->code[0].reg_sym.reg = 1; // dst
   m->code[0].reg_sym.sym = 1; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[1], orig);
finish: ;
}
TESTCASE(VM_MOV_regO_sym)
{
   Method m = initMethod(currentContext,MOV_regO_sym);
   TCObject orig = m->class_->cp->str[1];
   m->code[0].reg_sym.reg = 1; // dst
   m->code[0].reg_sym.sym = 1; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Ptr, currentContext->regO[1], orig);
finish: ;
}
TESTCASE(VM_MOV_regD_sym)
{
   Method m = initMethod(currentContext,MOV_regD_sym);
   double orig = m->class_->cp->dbl[1];
   m->code[0].reg_sym.reg = 1; // dst
   m->code[0].reg_sym.sym = 1; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[1], orig);
finish: ;
}
TESTCASE(VM_MOV_regL_sym)
{
   Method m = initMethod(currentContext,MOV_regL_sym);
   int64 orig = m->class_->cp->i64[1];
   m->code[0].reg_sym.reg = 1; // dst
   m->code[0].reg_sym.sym = 1; // src
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[1], orig);
finish: ;
}
TESTCASE(VM_MOV_regI_arlen)
{
   Method m = initMethod(currentContext,MOV_regI_arlen);
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 7);
   currentContext->regO[1/*sym*/] = array;
   m->code[0].reg_ar.reg = 2; // dst
   m->code[0].reg_ar.base = 1;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[2], ARRAYOBJ_LEN(array));
finish: ;
}
TESTCASE(VM_MOV_regI_aru)
{
   Method m = initMethod(currentContext,MOV_regI_aru);
   int32* v = (int32*)newArrayTest(currentContext, Type_Int);
   v[1] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[2], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regI_arc) // #DEPENDS(VM_MOV_regI_aru)
{
   newArrayTest(currentContext, Type_Int);
   if (!arCheck(currentContext, tc, MOV_regI_arc)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_regO_aru)
{
   Method m = initMethod(currentContext,MOV_regO_aru);
   TCObject* v = (TCObject*)newArrayTest(currentContext, Type_Object);
   v[1] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, currentContext->regO[2], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regO_arc) // #DEPENDS(VM_MOV_regO_aru)
{
   newArrayTest(currentContext, Type_Object);
   if (!arCheck(currentContext, tc, MOV_regO_arc)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_reg64_aru)
{
   Method m = initMethod(currentContext,MOV_reg64_aru);
   int64* vv;

   // double
   double* v = (double*)newArrayTest(currentContext, Type_Double);
   v[1] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[2], DOUBLE_TEST_VALUE);

   // long
   vv = (int64*)newArrayTest(currentContext, Type_Long);
   vv[1] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[2], INT64_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_reg64_arc) // #DEPENDS(VM_MOV_reg64_aru)
{
   newArrayTest(currentContext, Type_Double);
   if (!arCheck(currentContext, tc, MOV_reg64_arc)) TEST_OUTPUT_SOURCELINE;
   currentContext->thrownException = null;
   newArrayTest(currentContext, Type_Long);
   if (!arCheck(currentContext, tc, MOV_reg64_arc)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_aru_regI)
{
   Method m = initMethod(currentContext,MOV_aru_regI);
   int32* v = (int32*)newArrayTest(currentContext, Type_Int);
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, v[1], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_arc_regI) // #DEPENDS(VM_MOV_aru_regI)
{
   newArrayTest(currentContext, Type_Int);
   if (!arCheck(currentContext, tc,MOV_arc_regI)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_aru_regO)
{
   Method m = initMethod(currentContext,MOV_aru_regO);
   TCObject* v = (TCObject*)newArrayTest(currentContext, Type_Object);
   currentContext->regO[2] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, v[1], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_arc_regO) // #DEPENDS(VM_MOV_aru_regO)
{
   newArrayTest(currentContext, Type_Object);
   if (!arCheck(currentContext, tc,MOV_arc_regO)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_aru_reg64)
{
   Method m = initMethod(currentContext,MOV_aru_reg64);
   int64* vv;

   // double
   double* v = (double*)newArrayTest(currentContext, Type_Double);
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, v[1], DOUBLE_TEST_VALUE);

   // long
   vv = (int64*)newArrayTest(currentContext, Type_Long);
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, vv[1], INT64_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_arc_reg64) // #DEPENDS(VM_MOV_aru_reg64)
{
   newArrayTest(currentContext, Type_Long);
   if (!arCheck(currentContext, tc,MOV_arc_reg64)) TEST_OUTPUT_SOURCELINE;
   currentContext->thrownException = null;
   newArrayTest(currentContext, Type_Double);
   if (!arCheck(currentContext, tc,MOV_arc_reg64)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_aru_regIb)
{
   Method m = initMethod(currentContext,MOV_aru_regIb);
   uint8* v = (uint8*)newArrayTest(currentContext, Type_Byte);
   currentContext->regI[2] = INT8_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I8, v[1], INT8_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_arc_regIb) // #DEPENDS(VM_MOV_aru_regIb)
{
   newArrayTest(currentContext, Type_Byte);
   if (!arCheck(currentContext, tc,MOV_arc_regIb)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_MOV_aru_reg16)
{
   Method m = initMethod(currentContext,MOV_aru_reg16);
   uint16* v = (uint16*)newArrayTest(currentContext, Type_Char);
   currentContext->regI[2] = CHAR_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(U16, v[1], CHAR_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_arc_reg16)
{
   Method m = initMethod(currentContext,MOV_arc_reg16);

   int16* v = (int16*)newArrayTest(currentContext, Type_Short);
   currentContext->regI[2] = INT16_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I16, v[1], INT16_TEST_VALUE);
   if (!arCheck(currentContext, tc,MOV_arc_reg16)) TEST_OUTPUT_SOURCELINE;
finish: ;
}
TESTCASE(VM_MOV_regIb_aru)
{
   Method m = initMethod(currentContext,MOV_regIb_aru);
   int8* v = (int8*)newArrayTest(currentContext, Type_Byte);
   v[1] = INT8_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I8, (int8)currentContext->regI[2], INT8_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regIb_arc) // #DEPENDS(VM_MOV_reg16_aru)
{
   newArrayTest(currentContext, Type_Byte);
   if (!arCheck(currentContext, tc,MOV_regIb_arc)) TEST_OUTPUT_SOURCELINE;
}

TESTCASE(VM_MOV_reg16_aru)
{
   Method m = initMethod(currentContext,MOV_reg16_aru);
   uint16* v = (uint16*)newArrayTest(currentContext, Type_Char);
   v[1] = CHAR_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(U16, (uint16)currentContext->regI[2], CHAR_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_reg16_arc)
{
   Method m = initMethod(currentContext,MOV_reg16_arc);
   int16* v = (int16*)newArrayTest(currentContext, Type_Short);
   v[1] = INT16_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I16, (int16)currentContext->regI[2], INT16_TEST_VALUE);
   if (!arCheck(currentContext, tc,MOV_reg16_arc)) TEST_OUTPUT_SOURCELINE;
finish: ;
}
#define DEF_I_FIELD 0
#define DEF_L_FIELD 1
#define DEF_D_FIELD 0
#define DEF_O_FIELD 0
TESTCASE(VM_MOV_regI_field) // #DEPENDS(VM_MOV_field_regI)
{
   Method m = initMethod(currentContext,MOV_regI_field);
   TCObject ext = initExtTest(currentContext, tc,m,RegI,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regO_field) // #DEPENDS(VM_MOV_field_regO)
{
   Method m = initMethod(currentContext,MOV_regO_field);
   TCObject ext = initExtTest(currentContext, tc,m,RegO,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, currentContext->regO[0], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_reg64_field) // #DEPENDS(VM_MOV_field_reg64)
{
   Method m = initMethod(currentContext,MOV_reg64_field);

   // double
   TCObject ext = initExtTest(currentContext, tc,m,RegD,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], DOUBLE_TEST_VALUE);

   // long
   ext = initExtTest(currentContext, tc,m,RegL,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], INT64_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_field_regI)
{
   Method m = initMethod(currentContext,MOV_field_regI);
   TCObject ext = initExtTest(currentContext, tc,m,RegI,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   currentContext->regI[0] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, FIELD_I32(ext, 0), INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_field_regO)
{
   Method m = initMethod(currentContext,MOV_field_regO);
   TCObject ext = initExtTest(currentContext, tc,m,RegO,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   currentContext->regO[m->code->field_reg.reg] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, FIELD_OBJ(ext,OBJ_CLASS(ext), DEF_O_FIELD), OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_field_reg64)
{
   Method m = initMethod(currentContext,MOV_field_reg64);

   // double
   TCObject ext = initExtTest(currentContext, tc,m,RegD,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   REGD(currentContext->reg64)[m->code->field_reg.reg] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, FIELD_DBL(ext,OBJ_CLASS(ext), DEF_D_FIELD), DOUBLE_TEST_VALUE);

   // long
   m = initMethod(currentContext,MOV_field_reg64);
   ext = initExtTest(currentContext, tc,m,RegL,false);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   REGL(currentContext->reg64)[m->code->field_reg.reg] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, FIELD_I64(ext,OBJ_CLASS(ext), DEF_L_FIELD), INT64_TEST_VALUE);
finish: ;
}

TESTCASE(VM_MOV_regI_static)
{
   Method m = initMethod(currentContext,MOV_regI_static);
   TCObject ext = initExtTest(currentContext, tc,m,RegI,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_regO_static) // #DEPENDS(VM_MOV_static_regO)
{
   Method m = initMethod(currentContext,MOV_regO_static);
   TCObject ext = initExtTest(currentContext, tc,m,RegO,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, currentContext->regO[0], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_reg64_static)
{
   Method m = initMethod(currentContext,MOV_reg64_static);

   // double
   TCObject ext = initExtTest(currentContext, tc,m,RegD,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], DOUBLE_TEST_VALUE);

   // long
   ext = initExtTest(currentContext, tc,m,RegL,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], INT64_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_static_regI)
{
   Method m = initMethod(currentContext,MOV_static_regI);
   TCObject ext = initExtTest(currentContext, tc,m,RegI,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   currentContext->regI[m->code->static_reg.reg] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, OBJ_CLASS(ext)->i32StaticValues[0], INT32_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_static_regO)
{
   Method m = initMethod(currentContext,MOV_static_regO);
   TCObject ext = initExtTest(currentContext, tc,m,RegO,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   currentContext->regO[m->code->static_reg.reg] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Obj, OBJ_CLASS(ext)->objStaticValues[DEF_O_FIELD], OBJECT_TEST_VALUE);
finish: ;
}
TESTCASE(VM_MOV_static_reg64)
{
   Method m;
   TCObject ext;
   TCClass c;
   Int64Array i64;

   // double
   m = initMethod(currentContext,MOV_static_reg64);
   ext = initExtTest(currentContext, tc,m,RegD,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   c = OBJ_CLASS(ext);
   REGD(currentContext->reg64)[m->code->static_reg.reg] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, OBJ_CLASS(ext)->v64StaticValues[DEF_D_FIELD], DOUBLE_TEST_VALUE);

   // long
   ext = initExtTest(currentContext, tc,m,RegL,true);
   if (!ext) {TEST_OUTPUT_SOURCELINE; goto finish;}
   REGL(currentContext->reg64)[m->code->static_reg.reg] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   i64 = (Int64Array)&(OBJ_CLASS(ext)->v64StaticValues[DEF_L_FIELD]);
   ASSERT2_EQUALS(I64, i64[0], INT64_TEST_VALUE);
finish: ;
}

/////////////////////////////// OPERATIONS ///////////////////////////////

TESTCASE(VM_ADD_regI_regI_regI)
{
   Method m = initMethod(currentContext,ADD_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE/2;
   res = currentContext->regI[1] + currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_ADD_regI_s12_regI)
{
   Method m = initMethod(currentContext,ADD_regI_s12_regI);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = S12_TEST_VALUE + currentContext->regI[1];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_ADD_regI_aru_s6)
{
   Method m = initMethod(currentContext,ADD_regI_aru_s6);
   int32 res;
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 3);
   int32* v = (int32*)ARRAYOBJ_START(array);
   v[1] = INT32_TEST_VALUE;
   m->code[0].reg_s6_ar.reg = 2;
   m->code[0].reg_s6_ar.s6 = S6_TEST_VALUE;
   currentContext->regI[m->code[0].reg_s6_ar.idx  = 0] = 1;
   currentContext->regO[m->code[0].reg_s6_ar.base = 1] = array;
   res = INT32_TEST_VALUE + S6_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[2], res);
finish: ;
}
TESTCASE(VM_ADD_regI_arc_s6) // #DEPENDS(VM_ADD_regI_aru_s6)
{
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 3);
   testMethod.code[0].reg_s6_ar.reg = 2;
   currentContext->regI[testMethod.code[0].reg_s6_ar.idx  = 0] = 1;
   currentContext->regO[testMethod.code[0].reg_s6_ar.base = 1] = array;
   if (!arCheck(currentContext, tc,ADD_regI_arc_s6)) TEST_OUTPUT_SOURCELINE;
}
TESTCASE(VM_ADD_regD_regD_regD)
{
   Method m = initMethod(currentContext,ADD_regD_regD_regD);
   double res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE/2;
   res = REGD(currentContext->reg64)[1] + REGD(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_ADD_regL_regL_regL)
{
   Method m = initMethod(currentContext,ADD_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE/2;
   res = REGL(currentContext->reg64)[1] + REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_ADD_aru_regI_s6) // this is the first lexicographically ordered testcase. so we make CodeUnion appear before us, since it must be the first test case: #DEPENDS(VM_CodeUnion)
{
   Method m = initMethod(currentContext,ADD_aru_regI_s6);
   int32 res;
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 3);
   int32* v = (int32*)ARRAYOBJ_START(array);
   m->code[0].reg_s6_ar.reg = 2;
   m->code[0].reg_s6_ar.s6 = S6_TEST_VALUE;
   currentContext->regI[m->code[0].reg_s6_ar.idx  = 0] = 1;
   currentContext->regO[m->code[0].reg_s6_ar.base = 1] = array;
   currentContext->regI[2] = INT32_TEST_VALUE;
   res = INT32_TEST_VALUE + S6_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, v[1], res);
finish: ;
}
TESTCASE(VM_ADD_regI_regI_sym)
{
   Method m = initMethod(currentContext,ADD_regI_regI_sym);
   int32 orig = m->class_->cp->i32[1];
   int32 res;
   m->code[0].reg_reg_sym.reg0 = 0;
   m->code[0].reg_reg_sym.reg1 = 1;
   m->code[0].reg_reg_sym.sym  = 1;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = INT32_TEST_VALUE + orig;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SUB_regI_s12_regI)
{
   Method m = initMethod(currentContext,SUB_regI_s12_regI);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = S12_TEST_VALUE - currentContext->regI[1];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SUB_regI_regI_regI)
{
   Method m = initMethod(currentContext,SUB_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE/2;
   res = currentContext->regI[1] - currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SUB_regD_regD_regD)
{
   Method m = initMethod(currentContext,SUB_regD_regD_regD);
   double res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE/2;
   res = REGD(currentContext->reg64)[1] - REGD(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_SUB_regL_regL_regL)
{
   Method m = initMethod(currentContext,SUB_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE/2;
   res = REGL(currentContext->reg64)[1] - REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_MUL_regI_regI_s12)
{
   Method m = initMethod(currentContext,MUL_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] * S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_MUL_regI_regI_regI)
{
   Method m = initMethod(currentContext,MUL_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 3;
   res = currentContext->regI[1] * currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_MUL_regD_regD_regD)
{
   Method m = initMethod(currentContext,MUL_regD_regD_regD);
   double res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = 3;
   res = REGD(currentContext->reg64)[1] * REGD(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_MUL_regL_regL_regL)
{
   Method m = initMethod(currentContext,MUL_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 3;
   res = REGL(currentContext->reg64)[1] * REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_DIV_regI_regI_s12) // TESTAR DIVISAO POR ZERO EM TODOS OS DIVs!
{
   Method m = initMethod(currentContext,DIV_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] / S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
   m->code[0].reg_reg_s12.s12 = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_DIV_regI_regI_regI)
{
   Method m = initMethod(currentContext,DIV_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 3;
   res = currentContext->regI[1] / currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
   currentContext->regI[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_DIV_regD_regD_regD)
{
   Method m = initMethod(currentContext,DIV_regD_regD_regD);
   double res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = 3;
   res = REGD(currentContext->reg64)[1] / REGD(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
   REGD(currentContext->reg64)[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_DIV_regL_regL_regL)
{
   Method m = initMethod(currentContext,DIV_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 3;
   res = REGL(currentContext->reg64)[1] / REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
   REGL(currentContext->reg64)[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_MOD_regI_regI_s12)
{
   Method m = initMethod(currentContext,MOD_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] % S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
   m->code[0].reg_reg_s12.s12 = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_MOD_regI_regI_regI)
{
   Method m = initMethod(currentContext,MOD_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 10;
   res = currentContext->regI[1] % currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
   currentContext->regI[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_MOD_regD_regD_regD)
{
   Method m = initMethod(currentContext,MOD_regD_regD_regD);
   double res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = 10;
   res = dmod(REGD(currentContext->reg64)[1], REGD(currentContext->reg64)[2]);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
   REGD(currentContext->reg64)[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_MOD_regL_regL_regL)
{
   Method m = initMethod(currentContext,MOD_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 10;
   res = REGL(currentContext->reg64)[1] % REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
   REGL(currentContext->reg64)[2] = 0; // force ArithmeticException
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArithmeticException]);
finish: ;
}
TESTCASE(VM_SHR_regI_regI_s12)
{
   Method m = initMethod(currentContext,SHR_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = 3;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] >> 3;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SHR_regI_regI_regI)
{
   Method m = initMethod(currentContext,SHR_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 3;
   res = currentContext->regI[1] >> currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SHR_regL_regL_regL)
{
   Method m = initMethod(currentContext,SHR_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 3;
   res = REGL(currentContext->reg64)[1] >> REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_SHL_regI_regI_s12)
{
   Method m = initMethod(currentContext,SHL_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = 3;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] << 3;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SHL_regI_regI_regI)
{
   Method m = initMethod(currentContext,SHL_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 3;
   res = currentContext->regI[1] << currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_SHL_regL_regL_regL)
{
   Method m = initMethod(currentContext,SHL_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 3;
   res = REGL(currentContext->reg64)[1] << REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_USHR_regI_regI_s12)
{
   Method m = initMethod(currentContext,USHR_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = 3;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = ((uint32)currentContext->regI[1]) >> 3;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_USHR_regI_regI_regI)
{
   Method m = initMethod(currentContext,USHR_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 3;
   res = ((uint32)currentContext->regI[1]) >> currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_USHR_regL_regL_regL)
{
   Method m = initMethod(currentContext,USHR_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = 3;
   res = ((uint64)REGL(currentContext->reg64)[1]) >> REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_AND_regI_regI_s12)
{
   Method m = initMethod(currentContext,AND_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] & S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_AND_regI_aru_s6)
{
   Method m = initMethod(currentContext,AND_regI_aru_s6);
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 3);
   int32* v = (int32*)ARRAYOBJ_START(array);
   int32 res;
   m->code[0].reg_s6_ar.reg = 2;
   m->code[0].reg_s6_ar.s6 = S6_TEST_VALUE;
   currentContext->regI[m->code[0].reg_s6_ar.idx  = 0] = 1;
   currentContext->regO[m->code[0].reg_s6_ar.base = 1] = array;
   v[1] = INT32_TEST_VALUE;
   res = INT32_TEST_VALUE & S6_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[2], res);
finish: ;
}
TESTCASE(VM_AND_regI_regI_regI)
{
   Method m = initMethod(currentContext,AND_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 0x10101010;
   res = currentContext->regI[1] & currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_AND_regL_regL_regL)
{
   Method m = initMethod(currentContext,AND_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = I64_CONST(0x1010101010101010);
   res = REGL(currentContext->reg64)[1] & REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_OR_regI_regI_s12)
{
   Method m = initMethod(currentContext,OR_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] | S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_OR_regI_regI_regI)
{
   Method m = initMethod(currentContext,OR_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 0x10101010;
   res = currentContext->regI[1] | currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_OR_regL_regL_regL)
{
   Method m = initMethod(currentContext,OR_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = I64_CONST(0x1010101010101010);
   res = REGL(currentContext->reg64)[1] | REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_XOR_regI_regI_s12)
{
   Method m = initMethod(currentContext,XOR_regI_regI_s12);
   int32 res;
   m->code[0].reg_reg_s12.reg0 = 0;
   m->code[0].reg_reg_s12.reg1 = 1;
   m->code[0].reg_reg_s12.s12  = S12_TEST_VALUE;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] ^ S12_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_XOR_regI_regI_regI)
{
   Method m = initMethod(currentContext,XOR_regI_regI_regI);
   int32 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = 0x10101010;
   res = currentContext->regI[1] ^ currentContext->regI[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_XOR_regL_regL_regL)
{
   Method m = initMethod(currentContext,XOR_regL_regL_regL);
   int64 res;
   m->code[0].reg_reg_reg.reg0 = 0;
   m->code[0].reg_reg_reg.reg1 = 1;
   m->code[0].reg_reg_reg.reg2 = 2;
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = I64_CONST(0x1010101010101010);
   res = REGL(currentContext->reg64)[1] ^ REGL(currentContext->reg64)[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_JEQ_regO_regO)
{
   Method m = initMethod(currentContext,JEQ_regO_regO);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regO[1] = OBJECT_TEST_VALUE;
   currentContext->regO[2] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regO[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regO_null)
{
   Method m = initMethod(currentContext,JEQ_regO_null);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regO[1] = 0;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regO[1]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regI_regI)
{
   Method m = initMethod(currentContext,JEQ_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regL_regL)
{
   Method m = initMethod(currentContext,JEQ_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGL(currentContext->reg64)[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regD_regD)
{
   Method m = initMethod(currentContext,JEQ_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGD(currentContext->reg64)[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regI_s6)
{
   Method m = initMethod(currentContext,JEQ_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JEQ_regI_sym)
{
   int32 orig;
   Method m = initMethod(currentContext,JEQ_regI_sym);
   m->code[0].reg_sym_sdesloc.reg = 1;
   m->code[0].reg_sym_sdesloc.sym = 1;
   m->code[0].reg_sym_sdesloc.desloc = 9;
   orig = m->class_->cp->i32[1];
   currentContext->regI[1] = orig;
   // jump
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   m->class_->cp->i32[1] = orig+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   m->class_->cp->i32[1] = orig; // restore or MOV_regI_static test will fail
finish: ;
}
TESTCASE(VM_JNE_regO_regO)
{
   Method m = initMethod(currentContext,JNE_regO_regO);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // don't jump
   currentContext->regO[1] = OBJECT_TEST_VALUE;
   currentContext->regO[2] = OBJECT_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   currentContext->regO[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regO_null)
{
   Method m = initMethod(currentContext,JNE_regO_null);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regO[1] = (TCObject)1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regO[1] = 0;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regI_regI)
{
   Method m = initMethod(currentContext,JNE_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // don't jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   currentContext->regI[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regL_regL)
{
   Method m = initMethod(currentContext,JNE_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // don't jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   REGL(currentContext->reg64)[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regD_regD)
{
   Method m = initMethod(currentContext,JNE_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // don't jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   REGD(currentContext->reg64)[2]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regI_s6)
{
   Method m = initMethod(currentContext,JNE_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // don't jump
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   currentContext->regI[1]++;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
finish: ;
}
TESTCASE(VM_JNE_regI_sym)
{
   int32 orig;
   Method m = initMethod(currentContext,JNE_regI_sym);
   m->code[0].reg_sym_sdesloc.reg = 1;
   m->code[0].reg_sym_sdesloc.sym = 1;
   m->code[0].reg_sym_sdesloc.desloc = 9;
   orig = m->class_->cp->i32[1];
   currentContext->regI[1] = orig;
   // don't jump
   m->class_->cp->i32[1] = orig;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   // jump
   m->class_->cp->i32[1] = orig+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   m->class_->cp->i32[1] = orig; // restore or MOV_regI_static test will fail
finish: ;
}
TESTCASE(VM_JLT_regI_regI)
{
   Method m = initMethod(currentContext,JLT_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[2] = INT32_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLT_regL_regL)
{
   Method m = initMethod(currentContext,JLT_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLT_regD_regD)
{
   Method m = initMethod(currentContext,JLT_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLT_regI_s6)
{
   Method m = initMethod(currentContext,JLT_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 29;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   currentContext->regI[1] = 31;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLE_regI_regI)
{
   Method m = initMethod(currentContext,JLE_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[2] = INT32_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLE_regL_regL)
{
   Method m = initMethod(currentContext,JLE_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLE_regD_regD)
{
   Method m = initMethod(currentContext,JLE_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JLE_regI_s6)
{
   Method m = initMethod(currentContext,JLE_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 29;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 31;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGT_regI_regI)
{
   Method m = initMethod(currentContext,JGT_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regI[1] = INT32_TEST_VALUE+1;
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   currentContext->regI[1] = INT32_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGT_regL_regL)
{
   Method m = initMethod(currentContext,JGT_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE+1;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGT_regD_regD)
{
   Method m = initMethod(currentContext,JGT_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE+1;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGT_regI_s6)
{
   Method m = initMethod(currentContext,JGT_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 31;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   currentContext->regI[1] = 29;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGE_regI_regI)
{
   Method m = initMethod(currentContext,JGE_regI_regI);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   currentContext->regI[1] = INT32_TEST_VALUE;
   currentContext->regI[2] = INT32_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[1] = INT32_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = INT32_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGE_regL_regL)
{
   Method m = initMethod(currentContext,JGE_regL_regL);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE;
   REGL(currentContext->reg64)[2] = INT64_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGL(currentContext->reg64)[1] = INT64_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGE_regD_regD)
{
   Method m = initMethod(currentContext,JGE_regD_regD);
   m->code[0].reg_reg_s12.reg0 = 1;
   m->code[0].reg_reg_s12.reg1 = 2;
   m->code[0].reg_reg_s12.s12  = 9;
   // jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE;
   REGD(currentContext->reg64)[2] = DOUBLE_TEST_VALUE;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE+1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   REGD(currentContext->reg64)[1] = DOUBLE_TEST_VALUE-1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGE_regI_s6)
{
   Method m = initMethod(currentContext,JGE_regI_s6);
   m->code[0].reg_s6_desloc.reg    = 1;
   m->code[0].reg_s6_desloc.s6     = 30;
   m->code[0].reg_s6_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 31;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[1] = 30;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 29;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_JGE_regI_arlen)
{
   Method m = initMethod(currentContext,JGE_regI_arlen);
   TCObject array = FIELD_OBJ(testTypesInstance, testTypesClass, 7);
   currentContext->regO[1/*sym*/] = array;
   m->code[0].reg_arl_s12.regI = 2;
   m->code[0].reg_arl_s12.base = 1;
   m->code[0].reg_arl_s12.desloc = 9;
   // jump
   currentContext->regI[2] = 2;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[2] = 3;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[2] = 1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_CONV_regI_regL)
{
   Method m = initMethod(currentContext,CONV_regI_regL);
   int32 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   REGL(currentContext->reg64)[0] = INT64_TEST_VALUE;
   res = (int32)REGL(currentContext->reg64)[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_CONV_regI_regD)
{
   Method m = initMethod(currentContext,CONV_regI_regD);
   int32 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   REGD(currentContext->reg64)[0] = DOUBLE_TEST_VALUE;
   res = (int32)REGD(currentContext->reg64)[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_CONV_regIb_regI)
{
   Method m = initMethod(currentContext,CONV_regIb_regI);
   int8 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   currentContext->regI[0] = INT32_TEST_VALUE;
   res = (int8)currentContext->regI[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I8, (int8)currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_CONV_regIc_regI)
{
   Method m = initMethod(currentContext,CONV_regIc_regI);
   JChar res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   currentContext->regI[0] = INT32_TEST_VALUE;
   res = (JChar)currentContext->regI[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(U16, (uint16)currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_CONV_regIs_regI)
{
   Method m = initMethod(currentContext,CONV_regIs_regI);
   int16 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   currentContext->regI[0] = INT32_TEST_VALUE;
   res = (int16)currentContext->regI[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I16, (int16)currentContext->regI[0], res);
finish: ;
}
TESTCASE(VM_CONV_regL_regI)
{
   Method m = initMethod(currentContext,CONV_regL_regI);
   int64 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   currentContext->regI[0] = INT32_TEST_VALUE;
   res = (int64)currentContext->regI[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_CONV_regL_regD)
{
   Method m = initMethod(currentContext,CONV_regL_regD);
   int64 res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   REGD(currentContext->reg64)[0] = DOUBLE_TEST_VALUE;
   res = (int64)REGD(currentContext->reg64)[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I64, REGL(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_CONV_regD_regI)
{
   Method m = initMethod(currentContext,CONV_regD_regI);
   double res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   currentContext->regI[0] = INT32_TEST_VALUE;
   res = (double)currentContext->regI[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
finish: ;
}
TESTCASE(VM_CONV_regD_regL)
{
   Method m = initMethod(currentContext,CONV_regD_regL);
   double res;
   m->code[0].reg_reg.reg0 = 0; // dst
   m->code[0].reg_reg.reg1 = 0; // src
   REGL(currentContext->reg64)[0] = INT64_TEST_VALUE;
   res = (double)REGL(currentContext->reg64)[0];
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[0], res);
finish: ;
}
// the zn_ is used to define the run order of these tests
TESTCASE(VM_z0_JUMP_s24)
{
   Method m = initMethod(currentContext,JUMP_s24);
   m->code[0].s24.desloc = 7;
   m->code[7].s24.op = JUMP_s24;
   m->code[7].s24.desloc = -5;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, currentContext->code, &m->code[2]);
finish: ;
}
TESTCASE(VM_z1_JUMP_regI)
{
   Method m = initMethod(currentContext,JUMP_regI);
   m->code[0].reg.reg = 1;
   currentContext->regI[1] = 6;
   m->code[6].reg.op = BREAK;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, currentContext->code, &m->code[6]);
finish: ;
}

static Method initContext(Context c, int32 op, int32 retReg)
{
   Method m = initMethod(c,op);
   // the retReg now stays on the method call instruction, which is stored in the call stack
   m->code[0].mtd.retOr1stParam = retReg;
   c->callStack[0] = m;
   c->callStack[1] = m->code;
   c->callStackForced = c->callStack = c->callStackStart + 2;
   // now we move ahead the code so that the retReg can be kept safe.
   m->code += 2;
   m->code[0].s24.op = op;
   m->iCount = 5;
   m->oCount = 4;
   m->v64Count = 3;
   c->regI = c->regIStart + m->iCount;
   c->regO = c->regOStart + m->oCount;
   c->reg64 = c->reg64Start + m->v64Count;
   return m;
}
TESTCASE(VM_z2_RETURN_void)
{
   Method m = initContext(currentContext, RETURN_void,0);
   Context c = currentContext;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z3_RETURN_regI)
{
   Method m = initContext(currentContext, RETURN_regI,3);
   Context c = currentContext;
   // int
   c->regI[m->code[0].reg.reg = 4] = INT32_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I32, c->regI[3], INT32_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z3_RETURN_regO)
{
   Method m = initContext(currentContext, RETURN_regO,2);
   Context c = currentContext;
   // object
   c->regO[(m->code[0].reg.reg = 3)] = OBJECT_TEST_VALUE;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(Obj, c->regO[2], OBJECT_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z3_RETURN_reg64)
{
   Method m = initContext(currentContext, RETURN_reg64,1);
   Context c = currentContext;
   // double
   REGD(c->reg64)[m->code[0].reg.reg = 2] = DOUBLE_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(Dbl, REGD(c->reg64)[1], DOUBLE_TEST_VALUE);

   // long
   m = initContext(currentContext, RETURN_reg64,0);
   REGL(c->reg64)[m->code[0].reg.reg = 1] = INT64_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I64, REGL(c->reg64)[0], INT64_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z4_RETURN_s24I)
{
   Method m = initContext(currentContext, RETURN_s24I,3);
   Context c = currentContext;
   // int
   m->code[0].s24.desloc = S24_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I32, c->regI[3], S24_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z4_RETURN_null)
{
   Method m = initContext(currentContext, RETURN_null,2);
   Context c = currentContext;
   // object
   m->code[0].s24.desloc = S24_TEST_VALUE;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT1_EQUALS(Null, c->regO[2]);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z4_RETURN_s24D)
{
   Method m = initContext(currentContext, RETURN_s24D,1);
   Context c = currentContext;
   // double
   m->code[0].s24.desloc = S24_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(Dbl, REGD(c->reg64)[1], S24_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z4_RETURN_s24L)
{
   Method m = initContext(currentContext, RETURN_s24L,0);
   Context c = currentContext;
   // long
   m->code[0].s24.desloc = S24_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I64, REGL(c->reg64)[0], S24_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z5_RETURN_symI)
{
   Method m = initContext(currentContext, RETURN_symI,0);
   Context c = currentContext;
   // int
   m->class_->cp->i32[m->code[0].sym.sym = 0] = INT32_TEST_VALUE; // regI's already in the next method's frame

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I32, c->regI[0], INT32_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z5_RETURN_symO)
{
   Method m = initContext(currentContext, RETURN_symO,1);
   Context c = currentContext;
   TCObject old;
   // object
   m->code[0].sym.sym = 0;
   old = m->class_->cp->str[0];
   m->class_->cp->str[0] = STRING_TEST_VALUE;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(Ptr, c->regO[1], STRING_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   m->class_->cp->str[1] = old;
   c->callStackForced = null;
}
TESTCASE(VM_z5_RETURN_symD)
{
   Method m = initContext(currentContext, RETURN_symD,1);
   Context c = currentContext;
   // double
   m->class_->cp->dbl[m->code[0].sym.sym = 2] = DOUBLE_TEST_VALUE;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(Dbl, REGD(c->reg64)[1], DOUBLE_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}
TESTCASE(VM_z5_RETURN_symL)
{
   Method m = initContext(currentContext, RETURN_symL,0);
   Context c = currentContext;
   // long
   m->class_->cp->i64[m->code[0].sym.sym = 2] = INT64_TEST_VALUE;

   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, c->callStack, c->callStackStart);
   ASSERT2_EQUALS(Ptr, c->regI, c->regIStart);
   ASSERT2_EQUALS(Ptr, c->regO, c->regOStart);
   ASSERT2_EQUALS(Ptr, c->reg64,c->reg64Start);
   ASSERT2_EQUALS(I64, REGL(c->reg64)[0], INT64_TEST_VALUE);
finish:
   // restore original pointers
   c->regI = c->regIStart;
   c->regO = c->regOStart;
   c->reg64 = c->reg64Start;
   c->callStackForced = null;
}

// place these CALLxxxx tests after all the others.
static int32 getExtMethodIndex(ConstantPool cp, uint16 nameidx) // this
{
   int32 n = ARRAYLENV(cp->mtd);
   while (--n > 0)
   {
      UInt16Array a = cp->mtd[n];
      if (a[1] == nameidx)
         return n;
   }
   return -1;
}

#if 0
static int32 getMethodIndex(TCClass c, uint16 idx)
{
   int32 n = ARRAYLENV(c->methods);
   while (--n > 0)
      if (c->methods[n].cpName == idx)
         return n;
   return -1;
}
#endif

static CharP ttprintRes;
TC_API void TT_print_s(NMParams p)
{
   TCObject str = p->obj[0];
   p->retI = false;
   if (str && String_charsLen(str) == 7) // Barbara
   {
      char text[8];
      JCharP2CharPBuf(String_charsStart(str), 7, text);
      p->retI = strEq(text,"Barbara");
   }
   else ttprintRes = String2CharP(str);
}

TESTCASE(VM_z6_CALL_normal)
{
#if 0
   double d;
   int32 i;
   TCObject barbara;
   Method m = initMethod(currentContext,CALL_normal);
   // method call using call
   m->code[0].mtd.this = 0;
   m->code[0].mtd.sym = getMethodIndex(m->class, (uint16)getIndexInCP(m->class_->cp, "getDouble"));
   m->code[0].mtd.retOr1stParam = 1; // return in regD[1]
   m->code[1].u32.u32 = 0; // break;
   executeMethod(currentContext, m); // a test for static method
   ASSERT2_EQUALS(Dbl, REGD(currentContext->reg64)[1], DOUBLE_TEST_VALUE);

   // now call it directly
   m = getMethod(testTypesClass, true, "getDouble", 0);
   ASSERT1_EQUALS(NotNull, m);
   d = executeMethod(currentContext, m).asDouble;
   ASSERT2_EQUALS(Dbl, d, DOUBLE_TEST_VALUE);

   // call a native method
   m = getMethod(testTypesClass, true, "print", 1, "java.lang.String");
   ASSERT1_EQUALS(NotNull, m);
   barbara = createStringObjectFromCharP(currentContext, "Barbara", 7);
   ASSERT1_EQUALS(NotNull, barbara);
   i = executeMethod(currentContext, m, barbara).asInt32;
   ASSERT1_EQUALS(True, i);
#endif
}

TESTCASE(VM_z7_CALL_virtual)
{
#if 0
   JChar res[5];
   Method m = initMethod(currentContext,CALL_virtual);

   // will test:   "Vera Nardelli".substring(0,4);   returns: "Vera"
   // method call
   m->code[0].mtd.sym = getExtMethodIndex(m->class_->cp, (uint16)getIndexInCP(m->class_->cp, "substring"));
   m->code[0].mtd.this = 0; // in this case the string's instance goes on regO[0]
   m->code[0].mtd.retOr1stParam = 1; // return in regO[1]
   // method parameters: return in regO[1], pass ints that are in regI[0] and regI[1] (0,4)
   m->code[1].params.param1 = 65; // constant value that represents 0
   m->code[1].params.param2 = 1; // 2nd parameter in retI[1]
   // the parameters that will be passed
   m->iCount = 2;
   m->oCount = 1;
   currentContext->regI[1] = 4;
   currentContext->regO[0] = m->class_->cp->str[2];
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull,currentContext->regO[1]);
   ASSERT2_EQUALS(I32, String_charsLen(currentContext->regO[1]), 4);
   CharP2JCharPBuf("Vera",4, res,true);
   ASSERT3_EQUALS(Block, String_charsStart(currentContext->regO[1]), res, 4*2);
finish:
   m->iCount = m->oCount = 0;
#endif
}
TESTCASE(VM_NEWARRAY_len)
{
   Method m = initMethod(currentContext,NEWARRAY_len);
   m->code[0].newarray.regO = 1; // dst
   m->code[0].newarray.sym = getIndexInCP(testTypesClass->cp, SHORT_ARRAY);
   m->code[0].newarray.lenOrRegIOrDims = 60;
   currentContext->regO[1] = 0;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->regO[1]);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(currentContext->regO[1]), 60);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->regO[1])->name, SHORT_ARRAY);
finish: ;
}
TESTCASE(VM_NEWARRAY_regI)
{
   Method m = initMethod(currentContext,NEWARRAY_regI);
   m->code[0].newarray.regO = 1;
   m->code[0].newarray.sym = getIndexInCP(testTypesClass->cp, CHAR_ARRAY);
   currentContext->regO[1] = 0;
   currentContext->regI[m->code[0].newarray.lenOrRegIOrDims = 2] = 100;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->regO[1]);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(currentContext->regO[1]), 100);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->regO[1])->name, CHAR_ARRAY);
finish: ;
}
TESTCASE(VM_NEWARRAY_multi)
{
   TCObject a,*oa;
   // int[][][][][] v = new int[2][3][200][1][4];
   Method m = initMethod(currentContext,NEWARRAY_multi);
   currentContext->regI[8] = 200;  // MOV_regI_s18 8,200
   m->code[0].newarray.regO = 1; // NEWARRAY_multi 36,4,5  66,67,8,65,68
   m->code[0].newarray.sym = getIndexInCP(testTypesClass->cp,"[[[[[&I");
   m->code[0].newarray.lenOrRegIOrDims = 5;
   m->code[1].params.param1 = 67;
   m->code[1].params.param2 = 68;
   m->code[1].params.param3 = 8;
   m->code[1].params.param4 = 66;
   m->code[2].params.param1 = 69;
   currentContext->regO[1] = 0;
   executeMethod(currentContext, m);
   a = currentContext->regO[1];
   ASSERT1_EQUALS(NotNull, a);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(a), 2);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(a)->name, "[[[[[&I");

   oa = (TCObject*)ARRAYOBJ_START(a);
   ASSERT1_EQUALS(NotNull, oa);
   a = oa[0];
   ASSERT1_EQUALS(NotNull, a);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(a), 3);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(a)->name, "[[[[&I");

   oa = (TCObject*)ARRAYOBJ_START(a);
   ASSERT1_EQUALS(NotNull, oa);
   a = oa[0];
   ASSERT1_EQUALS(NotNull, a);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(a), 200);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(a)->name, "[[[&I");

   oa = (TCObject*)ARRAYOBJ_START(a);
   ASSERT1_EQUALS(NotNull, oa);
   a = oa[0];
   ASSERT1_EQUALS(NotNull, a);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(a), 1);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(a)->name, "[[&I");

   oa = (TCObject*)ARRAYOBJ_START(a);
   ASSERT1_EQUALS(NotNull, oa);
   a = oa[0];
   ASSERT1_EQUALS(NotNull, a);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(a), 4);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(a)->name, INT_ARRAY); // [&I

   finish: ;
}
TESTCASE(VM_NEWOBJ)
{
   Method m = initMethod(currentContext,NEWOBJ);
   TCClass c;
   m->code[0].reg_sym.reg = 1;
   m->code[0].reg_sym.sym = getIndexInCP(testTypesClass->cp, "java.lang.String");
   currentContext->regO[1] = 0;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->regO[1]);
   c = OBJ_CLASS(currentContext->regO[1]);
   ASSERT2_EQUALS(Sz, c->name, "java.lang.String");
finish: ;
}
extern CharP throwableTrace;
TC_API void TT_printException_s(NMParams p)
{
   throwableTrace = String2CharP(p->obj[0]);
}

TESTCASE(VM_THROW) // throw new Exception() -> newObj regO, java.lang.Exception; throw regO;
{
   Method testException;
   TCObject aioobe;

   // NOTE: THESE TESTS WILL GENERATE A "Warning! NullPointerException" in the output, due to the call of printStackTrace in Java's code

   // Test 1: NullPointerException thrown and handled in the same method
   testException = getMethod(testTypesClass, true, "testException", 0);
   htPutPtr(&htNativeProcAddresses, hashCode("TT_printException_s"), &TT_printException_s);
   ASSERT1_EQUALS(NotNull, testException);
   executeMethod(currentContext, testException);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, "TestTypes.methodC 62\nTestTypes.methodB 56\nTestTypes.methodA 55\nTestTypes.testException 54\n", throwableTrace);
   xfree(throwableTrace);

   // Test 2: NullPointerException thrown in a method and handled in another one
   testException = getMethod(testTypesClass, true, "testException2", 0);
   ASSERT1_EQUALS(NotNull, testException);
   executeMethod(currentContext, testException);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, throwableTrace);
   ASSERT2_EQUALS(Sz, "TestTypes.methodC2 93\nTestTypes.methodB2 88\nTestTypes.methodA2 79\nTestTypes.testException2 73\n", throwableTrace);
   xfree(throwableTrace);

   // Test 3: Exception thrown in a method and handled in another one
   testException = getMethod(testTypesClass, true, "testException3", 0);
   ASSERT1_EQUALS(NotNull, testException);
   executeMethod(currentContext, testException);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, ttprintRes);
   ASSERT2_EQUALS(Sz, "Testing exceptions (3)", ttprintRes);
   xfree(ttprintRes);

   // Test 4: IndexOutOfBoundsException thrown by a native method
   throwExceptionNamed(currentContext, "java.lang.IndexOutOfBoundsException","%s %d", "here", 3);
   aioobe = currentContext->thrownException;
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   executeMethod(currentContext, testException);
   ASSERT2_EQUALS(Ptr, currentContext->thrownException, aioobe); // the exception must not have been changed
finish:
   xfree(ttprintRes);
   xfree(throwableTrace);
}
TESTCASE(VM_INSTANCEOF) // if (other instanceof Rect) -> mov regI, regO instanceof sym; jeq regI,1;
{
   Method m = initMethod(currentContext,INSTANCEOF);
   // byte array
   currentContext->regO[m->code[0].instanceof.regO = 1] = FIELD_OBJ(testTypesInstance,testTypesClass,1);
   m->code[0].instanceof.regI = 0;
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, BYTE_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 1);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, SHORT_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 0);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "[java.lang.String");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 0);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "java.lang.Object");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 1);
   // String array
   currentContext->regO[1] = FIELD_OBJ(testTypesInstance,testTypesClass,7);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "[java.lang.String");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 1);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "java.lang.Object");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 1);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, SHORT_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 0);
finish: ;
}
TESTCASE(VM_CHECKCAST) // PenEvent pe = (PenEvent)event; -> checkcast event, PenEvent; mov pe, event;
{
   Method m = initMethod(currentContext,CHECKCAST);
   // byte array
   currentContext->regO[m->code[0].instanceof.regO = 1] = FIELD_OBJ(testTypesInstance,testTypesClass,1);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, BYTE_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, SHORT_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ClassCastException]);
   currentContext->thrownException = null;
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "[java.lang.String");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ClassCastException]);
   currentContext->thrownException = null;
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "java.lang.Object");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   // String array
   currentContext->regO[1] = FIELD_OBJ(testTypesInstance,testTypesClass,7);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "[java.lang.String");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, "java.lang.Object");
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   m->code[0].instanceof.sym = getIndexInCP(testTypesClass->cp, SHORT_ARRAY);
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ClassCastException]);
finish: ;
}
TESTCASE(VM_SWITCH)
{
   /*
   int res=0;
   switch (x)
   {
      case 1:
         res = 1;
         break;
      case 2:
         res = 2;
         break;
      case 3:
         res = -1;
      case 4:
         res += 4;
         break;
      default:
         res = -1;
   }
   // list of key/value pairs:  key (4 bytes) / value (2 bytes - destination address):
   // default address and exit address: 2 bytes each
   // format: default_addr (2 bytes) / exit_addr (2 bytes) / key-array (4*n bytes) / value-array (2*n bytes)
   // total: 2 + n + ceil(n/2) instructions
   // All addresses are relative to the start of the instruction (code)
   0:  mov regI[0],0
   1:  key=1, n=5  // switch (regI[1])
   2:  default and exit addresses -> 20,22
   3:  value "1"
   4:  value "2"
   5:  value "3"
   6:  value "4"
   7:  value "5"
   8:  address of label "1:" / "2:"  11/13
   9:  address of label "3:" / "4:"  15/16
   10: address of label "5:" / empty 18
   11: case 1: mov regI[0],1;
   12: goto finish;
   13: case 2: mov regI[0],2;
   14: goto finish;
   15: case 3: mov regI[0],-1;
   16: case 4: add regI[0],regI[0],4;
   17: goto finish;
   18: case 5: mov regI[0],5;
   19: goto finish;
   20: default: mov regI[0];
   21: goto finish;
   22: (finish) - next opcode
   */
   int32 base = 1, exit = 22;
   Method m = initMethod(currentContext,SWITCH);
   m->code[0].s18_reg.op = MOV_regI_s18;
   m->code[0].s18_reg.reg = 0;
   m->code[0].s18_reg.s18 = 0;

   m->code[1/*=base*/].switch_reg.op = SWITCH;
   m->code[1].switch_reg.key = 1; // regI[1]
   m->code[1].switch_reg.n = 5;
   m->code[2].two16.v1 = 20-base; // default
   m->code[2].two16.v2 = exit-base; // exit
   m->code[3].i32.i32 = 1;
   m->code[4].i32.i32 = 2;
   m->code[5].i32.i32 = 3;
   m->code[6].i32.i32 = 4;
   m->code[7].i32.i32 = 5;
   m->code[8].two16.v1 = 11-base;
   m->code[8].two16.v2 = 13-base;
   m->code[9].two16.v1 = 15-base;
   m->code[9].two16.v2 = 16-base;
   m->code[10].two16.v1 = 18-base;
   m->code[11].s18_reg.op = MOV_regI_s18; // case 1
   m->code[11].s18_reg.reg = 0;
   m->code[11].s18_reg.s18 = 1;
   m->code[12].s24.op = JUMP_s24;
   m->code[12].s24.desloc = exit-12; // note: jump is always relative to the current instruction pointer, which is the switch opcode.
   m->code[13].s18_reg.op = MOV_regI_s18; // case 2
   m->code[13].s18_reg.reg = 0;
   m->code[13].s18_reg.s18 = 2;
   m->code[14].s24.op = JUMP_s24;
   m->code[14].s24.desloc = exit-14;
   m->code[15].s18_reg.op = MOV_regI_s18; // case 3
   m->code[15].s18_reg.reg = 0;
   m->code[15].s18_reg.s18 = -1;
   m->code[16].reg_reg_s12.op = ADD_regI_s12_regI; // case 4
   m->code[16].reg_reg_s12.reg0 = 0;
   m->code[16].reg_reg_s12.reg1 = 0;
   m->code[16].reg_reg_s12.s12 = 4;
   m->code[17].s24.op = JUMP_s24;
   m->code[17].s24.desloc = exit-17;
   m->code[18].s18_reg.op = MOV_regI_s18; // case 5
   m->code[18].s18_reg.reg = 0;
   m->code[18].s18_reg.s18 = 5;
   m->code[19].s24.op = JUMP_s24;
   m->code[19].s24.desloc = exit-19;
   m->code[20].s18_reg.op = MOV_regI_s18; // default
   m->code[20].s18_reg.reg = 0;
   m->code[20].s18_reg.s18 = -1;
   m->code[21].s24.op = JUMP_s24;
   m->code[21].s24.desloc = exit-21; // could be eliminated
   m->code[22].s24.op = BREAK;

   // switch (1)
   currentContext->regI[1] = 1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 1);
   // switch (2)
   currentContext->regI[1] = 2;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 2);
   // switch (3)
   currentContext->regI[1] = 3;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 3);
   // switch (4)
   currentContext->regI[1] = 4;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 4);
   // switch (5)
   currentContext->regI[1] = 5;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], 5);
   // default:
   // switch (0)
   currentContext->regI[1] = 0;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], -1);
   // switch (6)
   currentContext->regI[1] = 6;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(I32, currentContext->regI[0], -1);

finish: ;
}
TESTCASE(VM_TEST_regO)
{
   Method m = initMethod(currentContext,TEST_regO);
   currentContext->regO[m->code[0].reg.reg = 0] = null;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   currentContext->thrownException = null;

   currentContext->regO[m->code[0].reg.reg = 1] = (TCObject)1;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
finish: ;
}
TESTCASE(VM_INC_regI)
{
   Method m = initMethod(currentContext,INC_regI);
   int32 res;
   m->code[0].inc.reg = 1;
   m->code[0].inc.s16 = -10;
   currentContext->regI[1] = INT32_TEST_VALUE;
   res = currentContext->regI[1] - 10;
   executeMethod(currentContext, m);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, currentContext->regI[1], res);
finish: ;
}
TESTCASE(VM_DECJGTZ_regI) // if (--x > 0) goto ....
{
   Method m = initMethod(currentContext,DECJGTZ_regI);
   m->code[0].reg_desloc.reg    = 1;
   m->code[0].reg_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 2;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
   currentContext->regI[1] = 0;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
TESTCASE(VM_DECJGEZ_regI) // if (--x >= 0) goto ....
{
   Method m = initMethod(currentContext,DECJGEZ_regI);
   m->code[0].reg_desloc.reg    = 1;
   m->code[0].reg_desloc.desloc = 9;
   // jump
   currentContext->regI[1] = 2;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   currentContext->regI[1] = 1;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+9, currentContext->code);
   // don't jump
   currentContext->regI[1] = 0;
   executeMethod(currentContext, m);
   ASSERT2_EQUALS(Ptr, m->code+1, currentContext->code);
finish: ;
}
