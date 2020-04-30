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

#ifndef CONTEXT_H
#define CONTEXT_H

#define STARTING_REGI_SIZE  2000  // * 4 = 8k
#define STARTING_REG64_SIZE 1000  // * 8 = 8k
#define STARTING_REGO_SIZE  2000  // * 4 = 8k for a 32-bit architecture
#define STARTING_STACK_SIZE 2000  // * 4 = 8k - each method call uses 2 positions

struct TContext
{
   TCObject thrownException;
   VoidPArray callStack;
   Int32Array  regI;  // start <= x < end
   TCObjectArray regO;
   Value64Array reg64;
   Int32Array  regIStart, regIEnd;  // start <= x < end
   TCObjectArray regOStart, regOEnd;
   Value64Array reg64Start, reg64End;
   // method stack
   VoidPArray callStackStart, callStackEnd;
   Code code;
   Heap heap;
   ThreadHandle thread; // the thread handle for this thread or null if its the main thread
   TCObject threadObj;
   TNMParams nmp;

   // global variables that can be changed by the thread
   // tcexception.c
   TCObject OutOfMemoryErrorObj;

   // tcexception.c
   char exmsg[1024];

   // graphicsprimitives.c
   // in a menu, we use often two colors only, so we cache them
   PixelConv aafroms1[16],aafroms2[16];
   PixelConv aatos1[16],aatos2[16];
   int32 aaFromColor1,aaToColor1,aaTextColor1;
   int32 aaFromColor2,aaToColor2,aaTextColor2;
   bool lastWas1; // used to switch from 1 to 2

   // PalmFont_c.h
   UserFont lastUF;
   TCObject lastFontObj;

   VoidP litebasePtr; // used by litebase
   VoidP sslPtr; // used by SSL
   VoidP rsaPtr; // used by RSACipher
   int32 sslPtrCount; // number of references to sslPtr.
   int32 rsaPtrCount; // number of references to rsaPtr.

   // Control access to this context in executeMethod
   DECLARE_MUTEX(usageLock);
   ThreadHandle usageOwner;
   int32 usageCount;

   // graphics
   bool fullDirty;
   int32 dirtyX1, dirtyY1, dirtyX2, dirtyY2;

   // reflection
   bool parametersInArray;

   // IMPORTANT: ALL IFDEFS MUST BE PLACED AT THE END, otherwise, other native libraries that 
   // use this header that do not define the same #defines, will have problems.
   #ifdef ENABLE_TEST_SUITE
   VoidP *callStackForced;
   #endif
   #ifdef ENABLE_TRACE
   int32 ccon,depth;
   char spaces[200];
   #endif
   #ifdef TRACK_USED_OPCODES
   int8 usedOpcodes[256];
   #endif
};

Context newContext(ThreadHandle thread, TCObject threadObj, bool bigContextSizes); // if bigContextSize is false, use STARTING_xxx_SIZE/10
void deleteContext(Context c, bool destroyThread);

bool contextIncreaseRegI(Context c, int32** r);
bool contextIncreaseRegO(Context c, TCObject** r);
bool contextIncreaseReg64(Context c, Value64* r);
bool contextIncreaseCallStack(Context c);

Context initContexts();
void destroyContexts();

TC_API Context getMainContext();
typedef Context (*getMainContextFunc)();

#endif
