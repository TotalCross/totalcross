// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef __TESTSUITE_H
#define __TESTSUITE_H

/*////////////////////////////////////////////////////////////////////////////////
//                             DOCUMENTATION                                    //
//////////////////////////////////////////////////////////////////////////////////

This is the header file for the test suite of SuperWaba.
Here is an example of a test case:

TESTCASE(VmSleep)    // 20051209 #DEPENDS(VmGetTimeStamp)
{
   ALLOC_STACK(1);
   int32 start,end;
   // Get the time stamp
   CALL_NM(VmGetTimeStamp);
   start = RETURNED_VALUE_AS(intValue);
   // sleep for 60 ms
   CALL_NM_WITH(VmSleep, intValue, 60);
   // Get the new time stamp
   CALL_NM(VmGetTimeStamp);
   end = RETURNED_VALUE_AS(intValue);
   ASSERT_BETWEEN(I32,40,end-start,80);
finish: ;
}

The 'finish' label must be always defined, because if a test fails, it will jump to it so that
a cleanup can be made (for sake, freeing memory allocated for the test). The ASSERT macros will
jump to the label in case of a failure.

There are three assertion macros: ASSERT<n>_EQUALS, where n is the number of parameters it receives,
excluding the extra parameter for each macro, which is the comparision type, described below:

Type   # params   Description
I8         2      signed int with 8 bits
I16        2      signed int with 16 bits
I32        2      signed int with 32 bits
I64        2      signed int with 64 bits
U8         2      unsigned int with 8 bits
U16        2      unsigned int with 16 bits
U32        2      unsigned int with 32 bits
Dbl        2      double
Sz         2      char* zero-terminated
Ptr        2      pointers to void
Block      3      a block of memory
Filled     3      a block of memory filled with a single value
Null       1      a void* that will be tested if null
NotNull    1      a void* that will be tested if not null
True       1      an integer with 32 bits where 0 is false and != is true
False      1      an integer with 32 bits where 0 is false and != is true

There's also the ASSERT_BETWEEN macro, used with numeric types, which can be used to assert that a value is
between two numbers (low <= x <= high).

Type   # params   Description
I8         3      signed int with 8 bits
I16        3      signed int with 16 bits
I32        3      signed int with 32 bits
I64        3      signed int with 64 bits
U8         3      unsigned int with 8 bits
U16        3      unsigned int with 16 bits
U32        3      unsigned int with 32 bits
Dbl        3      double


The ASSERT_ABOVE macro, also used only with numeric types, tests if v1 > v2.

Type   # params   Description
I8         2      signed int with 8 bits
I16        2      signed int with 16 bits
I32        2      signed int with 32 bits
I64        2      signed int with 64 bits
U8         2      unsigned int with 8 bits
U16        2      unsigned int with 16 bits
U32        2      unsigned int with 32 bits
Dbl        2      double

The TESTCASE macro declares a test case. The first parameter is the function name, and the
second parameter is a group id, which will be used to group several tests. This can be useful to enable
a specific group to be run. The function will be searched later by the engine that will create the program that
will call all test cases.

There are a few useful macros:

TEST_FAIL(x,...)        will fail with the given message (can be formatted)
TEST_OUTPUT(x,...)      will output the given message (idem)
TEST_SKIP               will skip the test. Useful to state that the test was not yet implemented
TEST_OUTPUT_SOURCELINE  outputs the source line.
TEST_ABORT              aborts the current and also all the other test cases
TEST_CANNOT_RUN         states that the test cannot run, maybe because it must be run exclusively

The test case must not assume any call order: it must be prepared to run totally standalone. It may, tho, declare
an unique dependency with another testcase. This is done by adding, inside a comment in the same line of the TESTCASE
declaration, a #DEPENDS(function name). The dependencies cannot be recursive (E.G.: func1 depends on func2 depends
on func3 depends on func1).

The engine that creates the tc_tests.c file is located at P:\TotalCrossVM\src\tests\java\GenTestCalls.
Here's an example of how to run it, assuming you're at the base folder from where the tests will be recursively searched:
java -classpath p:\TotalCrossSDK\output\eclipse;P:\TotalCrossVM\src\tests\java GenTestCalls .

Here's a sample output:

#define TEST_COUNT 5

// Function prototypes
void testTimeCreate(struct TestSuite *tc);                // P:\SuperWabaSDKPro\src\native\waba/SW_Time.c
void testVmGetTimeStamp(struct TestSuite *tc);            // P:\SuperWabaSDKPro\src\native\waba/SW_Vm.c
void testVmSetDeviceAutoOff(struct TestSuite *tc);        // P:\SuperWabaSDKPro\src\native\waba/SW_Vm.c
void testVmSetTimeStamp(struct TestSuite *tc);            // P:\SuperWabaSDKPro\src\native\waba/SW_Vm.c - depends on testTimeCreate
void testVmSleep(struct TestSuite *tc);                   // P:\SuperWabaSDKPro\src\native\waba/SW_Vm.c - depends on testVmGetTimeStamp

#ifdef ENABLE_TEST_SUITE

void fillTestCaseArray(testFunc *tests)
{
   tests[0] = testTimeCreate;
   tests[1] = testVmGetTimeStamp;
   tests[2] = testVmSetDeviceAutoOff;
   tests[3] = testVmSetTimeStamp;
   tests[4] = testVmSleep;
}

void startTestSuite(Context currentContext)
{
   struct TestSuite *tc = createTestSuite();
   int i;
   int from = 0;
   int to = TEST_COUNT-1;
   int lastNumberOfFail = 0; //How many tests have failed until now?
   int testsResults[TEST_COUNT] = { 0 }; // This flag vector indicates whether a given test had failed
   testFunc tests[TEST_COUNT];

   xmemzero(tests, sizeof(tests));
   fillTestCaseArray(tests);

   for (i = from; !tc->abort && i <= to; i++)
   {
      tc->total++;
      currentContext->thrownException = null;
      if (tests[i]) tests[i](tc, currentContext);
      if (tc->failed != lastNumberOfFail)
      {
         testsResults[i] = 1;
         lastNumberOfFail = tc->failed;
      }
   }
   showTestResults(testsResults);
}

#endif

The tests are ordered by the name, so it is easy to run all tests inside a single file; to do this, just change
the 'from' and 'to' parameters that controls the loop inside the startTestSuite function.

To enable the tests, you have to define the ENABLE_TEST_SUITE macro. This will make the SWVM run the test suite instead
of the SW program. The suite will run and the VM will then quit. In the release version of the program, just
undefine the ENABLE_TEST_SUITE macro; this way nobody will call the test cases and they will be stripped by the linker.

==================  MEMORY TESTS ===================

The memory test works like this: a command is issued to the TotalCross memory manager instructing it to return null
at the first xmalloc/heapAlloc. Once the function correctly handles this situation, the counter is incremented and
another command is issued instructing the memory manager return null at the second xmalloc/heapAlloc. And so on, until
all allocations failed and succeeded.

The routines are have the MEMORY_TEST_START and MEMORY_TEST_END requires that:
1. These routines cannot have *return* statements; just replace that to a "goto finish", and create a label "finish: ;"
right before the MEMORY_TEST_END macro call.
2. All local variable initializations must be placed AFTER the MEMORY_TEST_START. This includes any assignment to null and
other initializations, otherwise after the first loop iteration the variables may be initialized to a different value.

To enable the memory test, define ENABLE_MEMORY_TEST at your project settings.

*/

//////////////////////////////////////////////////////////////////////////////////
//                       PUBLIC ROUTINES AND MACROS                             //
//////////////////////////////////////////////////////////////////////////////////

// This macro defines a test case function
#define TESTCASE(name) void test_##name(struct TestSuite *tc, Context currentContext)

// Macros used within the testcases, to make the assertions
#define ASSERT1_EQUALS(type, v1)         if (!tc->assertEquals##type(tc,v1,        __FILE__,__LINE__)) goto finish
#define ASSERT2_EQUALS(type, v1, v2)     if (!tc->assertEquals##type(tc,v1,v2,     __FILE__,__LINE__)) goto finish
#define ASSERT3_EQUALS(type, v1, v2, v3) if (!tc->assertEquals##type(tc,v1,v2,v3,  __FILE__,__LINE__)) goto finish
#define ASSERT_BETWEEN(type, v1, v2, v3) if (!tc->assertBetween##type(tc,v1,v2,v3, __FILE__,__LINE__)) goto finish
#define ASSERT_ABOVE(type, v1, v2)       if (!tc->assertAbove##type(tc,v1,v2,      __FILE__,__LINE__)) goto finish

#define TEST_FAIL tc->fail
#define TEST_OUTPUT tc->output
#define TEST_SKIP do {tc->output(tc, "%s (%d): test skipped!",(char*)__FILE__,(int)__LINE__); tc->skipped++; goto finish;} while (0) // remove warning of finish not defined
#define TEST_OUTPUT_SOURCELINE tc->output(tc, "from %s (%d)\n",(char*)__FILE__,(int)__LINE__)
#define TEST_ABORT do {tc->abort = true; goto finish;} while (0) // abort all tests
#define TEST_CANNOT_RUN do {tc->output(tc, "%s (%d): test cannot run!",(char*)__FILE__,(int)__LINE__); tc->cannotRun++; goto finish;} while (0)

// This function must be called to start the tests. It is called by the GenTestCalls on the automatically generated SWTests.c
void startTestSuite(Context currentContext);
// This function presents the test results at the proper place; if the array testResults isn't NULL, then print which tests have failed by their number

void showTestResults(int *testResults);

// Memory tests
#ifdef ENABLE_MEMORY_TEST
#define MEMORY_TEST_START                                                           \
          do                                                                        \
          {                                                                         \
             int32 xtonull;                                                         \
             TCObject lastException = null;                                         \
             for (xtonull = 1; TCAPI_FUNC(getCountToReturnNull)() == 0; xtonull++)  \
             {                                                                      \
                TCAPI_FUNC(setCountToReturnNull)(xtonull);
#define MEMORY_TEST_END                                                             \
                lastException = p->currentContext->thrownException;                 \
                p->currentContext->thrownException = null;                          \
             }                                                                      \
             TCAPI_FUNC(setCountToReturnNull)(0);                                   \
             p->currentContext->thrownException = lastException;                    \
          } while (0);
#else
#define MEMORY_TEST_START
#define MEMORY_TEST_END
#endif

//////////////////////////////////////////////////////////////////////////////////
//                       PRIVATE ROUTINES AND MACROS                            //
//////////////////////////////////////////////////////////////////////////////////

// function declarations

struct TestSuite;

#define DECLARE_ASSERT_FUNC1(name, type1) \
   typedef bool (*assert##name##Func) (struct TestSuite *tc, type1 v1,                     const char *file, int line); \
   bool           assert##name        (struct TestSuite *tc, type1 v1,                     const char *file, int line);

#define DECLARE_ASSERT_FUNC2(name, type1, type2) \
   typedef bool (*assert##name##Func) (struct TestSuite *tc, type1 v1, type2 v2,           const char *file, int line); \
   bool           assert##name        (struct TestSuite *tc, type1 v1, type2 v2,           const char *file, int line);

#define DECLARE_ASSERT_FUNC3(name, type1, type2, type3)  \
   typedef bool (*assert##name##Func) (struct TestSuite *tc, type1 v1, type2 v2, type3 v3, const char *file, int line); \
   bool           assert##name        (struct TestSuite *tc, type1 v1, type2 v2, type3 v3, const char *file, int line);

DECLARE_ASSERT_FUNC1(EqualsNull    , VoidP )
DECLARE_ASSERT_FUNC1(EqualsNotNull , VoidP )
DECLARE_ASSERT_FUNC1(EqualsTrue    , int32 )
DECLARE_ASSERT_FUNC1(EqualsFalse   , int32 )
DECLARE_ASSERT_FUNC2(EqualsI8      , int8    , int8  )
DECLARE_ASSERT_FUNC2(EqualsI16     , int16   , int16 )
DECLARE_ASSERT_FUNC2(EqualsI32     , int32   , int32 )
DECLARE_ASSERT_FUNC2(EqualsI64     , int64   , int64 )
DECLARE_ASSERT_FUNC2(EqualsU8      , uint8   , uint8 )
DECLARE_ASSERT_FUNC2(EqualsU16     , uint16  , uint16)
DECLARE_ASSERT_FUNC2(EqualsU32     , uint32  , uint32)
DECLARE_ASSERT_FUNC2(EqualsDbl     , double  , double)
DECLARE_ASSERT_FUNC2(EqualsSz      , CharP   , CharP )
DECLARE_ASSERT_FUNC2(EqualsObj     , TCObject, TCObject)
DECLARE_ASSERT_FUNC2(EqualsPtr     , VoidP   , VoidP )
DECLARE_ASSERT_FUNC3(EqualsBlock   , VoidP   , VoidP  , int32 )
DECLARE_ASSERT_FUNC3(EqualsFilled  , VoidP   , uint32 , uint8 )
DECLARE_ASSERT_FUNC3(BetweenI8     , int8    , int8   , int8  )
DECLARE_ASSERT_FUNC3(BetweenI16    , int16   , int16  , int16 )
DECLARE_ASSERT_FUNC3(BetweenI32    , int32   , int32  , int32 )
DECLARE_ASSERT_FUNC3(BetweenI64    , int64   , int64  , int64 )
DECLARE_ASSERT_FUNC3(BetweenU8     , uint8   , uint8  , uint8 )
DECLARE_ASSERT_FUNC3(BetweenU16    , uint16  , uint16 , uint16)
DECLARE_ASSERT_FUNC3(BetweenU32    , uint32  , uint32 , uint32)
DECLARE_ASSERT_FUNC3(BetweenDbl    , double  , double , double)

DECLARE_ASSERT_FUNC2(AboveI8       , int8    , int8   )
DECLARE_ASSERT_FUNC2(AboveI16      , int16   , int16  )
DECLARE_ASSERT_FUNC2(AboveI32      , int32   , int32  )
DECLARE_ASSERT_FUNC2(AboveI64      , int64   , int64  )
DECLARE_ASSERT_FUNC2(AboveU8       , uint8   , uint8  )
DECLARE_ASSERT_FUNC2(AboveU16      , uint16  , uint16 )
DECLARE_ASSERT_FUNC2(AboveU32      , uint32  , uint32 )
DECLARE_ASSERT_FUNC2(AboveDbl      , double  , double )

typedef bool (*failFunc)   (struct TestSuite *tc, char *,    ...);
bool           fail        (struct TestSuite *tc, char *msg, ...);
typedef void (*outputFunc) (struct TestSuite *tc, char *,    ...);
void           output      (struct TestSuite *tc, char *msg, ...);

struct TestSuite
{
   double doubleError; // 1e-8;
   int32 failed;
   int32 total;
   int32 skipped;
   int32 cannotRun;
   bool abort;

   assertEqualsI8Func      assertEqualsI8;
   assertEqualsI16Func     assertEqualsI16;
   assertEqualsI32Func     assertEqualsI32;
   assertEqualsI64Func     assertEqualsI64;
   assertEqualsU8Func      assertEqualsU8;
   assertEqualsU16Func     assertEqualsU16;
   assertEqualsU32Func     assertEqualsU32;
   assertEqualsDblFunc     assertEqualsDbl;
   assertEqualsSzFunc      assertEqualsSz;
   assertEqualsObjFunc     assertEqualsObj;
   assertEqualsPtrFunc     assertEqualsPtr;
   assertEqualsBlockFunc   assertEqualsBlock;
   assertEqualsFilledFunc  assertEqualsFilled;
   assertEqualsNullFunc    assertEqualsNull;
   assertEqualsNotNullFunc assertEqualsNotNull;
   assertEqualsTrueFunc    assertEqualsTrue;
   assertEqualsFalseFunc   assertEqualsFalse;

   assertBetweenI8Func     assertBetweenI8;
   assertBetweenI16Func    assertBetweenI16;
   assertBetweenI32Func    assertBetweenI32;
   assertBetweenI64Func    assertBetweenI64;
   assertBetweenU8Func     assertBetweenU8;
   assertBetweenU16Func    assertBetweenU16;
   assertBetweenU32Func    assertBetweenU32;
   assertBetweenDblFunc    assertBetweenDbl;

   assertAboveI8Func       assertAboveI8;
   assertAboveI16Func      assertAboveI16;
   assertAboveI32Func      assertAboveI32;
   assertAboveI64Func      assertAboveI64;
   assertAboveU8Func       assertAboveU8;
   assertAboveU16Func      assertAboveU16;
   assertAboveU32Func      assertAboveU32;
   assertAboveDblFunc      assertAboveDbl;

   failFunc                fail;
   outputFunc              output;
};

// the function signature of all Test cases
typedef void (*testFunc)(struct TestSuite *tc, Context currentContext);

// this function is called by the startTestSuite in order to assign the functions and setup the variables of the SWTestSuite structure.
struct TestSuite *createTestSuite();

#endif
