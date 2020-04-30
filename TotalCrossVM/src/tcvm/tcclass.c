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

#include "tcvm.h"

#ifdef ENABLE_TEST_SUITE
#include "../tests/tc_testsuite.h"
#endif

DECLARE_MUTEX(classLoaderLock);

/* These are the structures for Class, Method and Field that are persisted to disk.
 * The structures that will be kept into memory are available at tcvm.h.
 * Note that the main difference between xxx and xxxInfo is that xxxInfo stores
 * the constant pool indexes, while xxx stores the String (pointing directly to
 * the constant pool). Note that we decided to keep this way because the pack(2)
 * below decreases performance on 32 bit systems. The standard is 4-byte alignment.
 */

#pragma pack(2)  // make sure structure members are aligned at 2 bytes

typedef struct
{
   FieldFlags flags;
   uint16 cpIdxName;
   uint16 cpIdxType;
} __attribute_packed__ TFieldInfo;

typedef struct
{
   uint16 startPC;
   uint16 endPC;
   uint16 handlerPC;
   uint16 regO;
   uint16 cpIdxClassName;
} __attribute_packed__ TExceptionInfo;

typedef struct
{
   MethodFlags flags; // uint16

   uint16 opcodeCount; // number of 4-byte instructions
   uint16 exceptionHandlersCount;
   uint16 lineNumberInfoCount;

   uint8 iCount;
   uint8 oCount;
   uint8 v64Count;
   uint8 paramCount;

   uint16 cpName;
   uint16 cpReturn;
} __attribute_packed__ TMethodInfoHeader;

typedef struct
{
   ClassFlags flags;
   uint16 cpIdxClassName;
   uint16 cpIdxSuperClass;
   uint16 interfacesCount;
   uint16 i32InstanceFieldsCount,objInstanceFieldsCount,v64InstanceFieldsCount;
   uint16 i32StaticFieldsCount,objStaticFieldsCount,v64StaticFieldsCount;
   uint16 methodsCount;
} __attribute_packed__ TClassInfoHeader;

#pragma pack()  // restore structure member alignment to default

bool paramsEq(ConstantPool cp1, UInt16Array params1, int32 n1, ConstantPool cp2, UInt16Array params2) // guich@tc110_21
{
   int32 n2,s1,s2;
   CharP p1,p2;

   n2 = ARRAYLENV(params2);
   if (n1 != n2)
      return false;
   params1 += 2; // params1 have the class+method+params, while params2 have only the params
   while (--n1 >= 0)
   {
      s1 = *params1++;
      s2 = *params2++;
      if (((cp1 == cp2) || (s1 < DEFAULT_CONSTANTS_LEN && s2 < DEFAULT_CONSTANTS_LEN)) && s1 == s2) // fast comparison: same constant pools or default constants?
         continue;
      p1 = cp1->cls[s1];
      p2 = cp2->cls[s2];
      if (!strEq(p1,p2))
         return false;
   }
   return true;
}

/***********************************************/
/**               CLASS LOADER                **/
/***********************************************/

int32 getIndexInCP(ConstantPool cp, CharP s) // currently used only by the test cases
{
   int32 i;
   if (cp)
   {
      for (i = ARRAYLEN(cp->cls)-1; i > 0; i--)
         if (strEq(s, cp->cls[i]))
            return i;
      for (i = ARRAYLEN(cp->mtdfld)-1; i > 0; i--)
         if (strEq(s, cp->mtdfld[i]))
            return i;
   }
   alert("IDENT NOT FOUND! %s",s);
   return 0;
}

void methodHashCode(CharP name, uint16* cpParams, int32 n, ConstantPool cp, int32* hashName, int32* hashParam)
{
   *hashName = hashCode(name) << 3 | n;
   for (; n-- > 0; cpParams++)
      *hashParam += hashCode(cp->cls[*cpParams]);
}

void readConstantPool(Context currentContext, ConstantPool t, TCZFile tcz, Heap heap)
{
   int32 len,i,partSize;
   CharPArray sa; TCObjectArray oa;
   uint16* u;
   char chars[256];
   uint8 mark;

   tcz->tempHeap = heap;
   tczRead(tcz, &t->i32Count, 18);

   // read the symbol arrays
   if (t->i32Count > 0)
   {
      t->i32 = newPtrArrayOf(Int32,t->i32Count, heap);
      tczRead(tcz, t->i32+1, 4*(t->i32Count-1));
   }
   if (t->i64Count > 0)
   {
      t->i64 = newPtrArrayOf(Int64,t->i64Count, heap);
      tczRead(tcz, t->i64+1, 8*(t->i64Count-1));
   }
   if (t->dblCount > 0)
   {
      t->dbl = newPtrArrayOf(Double,t->dblCount, heap);
      tczRead(tcz, t->dbl+1, 8*(t->dblCount-1));
   }
   if (t->sfieldCount > 0)
   {
      t->sfieldClass = newPtrArrayOf(UInt16, t->sfieldCount, heap);
      t->sfieldField = newPtrArrayOf(UInt16, t->sfieldCount, heap);
      tczRead(tcz, t->sfieldField+1, 2 * (t->sfieldCount-1));
      tczRead(tcz, t->sfieldClass+1, 2 * (t->sfieldCount-1));
      for (u = t->sfieldField+1, i=t->sfieldCount; --i > 0; u++) *u += *(u-1);
      t->boundSField = newPtrArrayOf(VoidP, t->sfieldCount, heap);
   }
   if (t->ifieldCount > 0)
   {
      t->ifieldClass = newPtrArrayOf(UInt16, t->ifieldCount, heap);
      t->ifieldField = newPtrArrayOf(UInt16, t->ifieldCount, heap);
      tczRead(tcz, t->ifieldField+1, 2 * (t->ifieldCount-1));
      tczRead(tcz, t->ifieldClass+1, 2 * (t->ifieldCount-1));
      for (u = t->ifieldField+1, i=t->ifieldCount; --i > 0; u++) *u += *(u-1);
      t->boundIField = newPtrArrayOf(UInt16, t->ifieldCount, heap);
      xmemset(t->boundIField,255, t->ifieldCount<<1);
   }
   if (t->mtdCount > 0)
   {
      uint8* lens;
      uint16* bunch;
      UInt16Array *ua = t->mtd = (UInt16Matrix)newArray(sizeof(UInt16Array), t->mtdCount, heap); // caution! this is not an uint16 array, but an uint16 matrix!
      lens = t->mtdLens = newPtrArrayOf(UInt8, t->mtdCount, heap);
      if (t->mtdCount > 1) // guich@tc123_24: zero-length mtd has no written data in the constant pool
      {
         tczRead(tcz, t->mtdLens+1, t->mtdCount-1);
         partSize = tczRead32(tcz);
         bunch = heapAlloc(heap, partSize);
         tczRead(tcz, bunch, partSize);
         for (lens++,ua++, i = t->mtdCount; --i > 0; ua++)
         {
            *ua = bunch;
            bunch += *lens++ + 2;
         }
      }
      t->boundNormal = (MethodPtrArray)newArray(sizeof(Method), t->mtdCount, heap);
      t->boundVirtualMethod = (MethodAndClassPtrArray)newArray(TSIZE, t->mtdCount, heap);
   }

   partSize = tczRead32(tcz);
   if (t->mtdfldCount > 0)
   {
      uint8* bunch = heapAlloc(heap, partSize+1);
      tczRead(tcz, bunch, partSize);
      sa = t->mtdfld = newPtrArrayOf(CharP, t->mtdfldCount, heap);
      for (sa++, i = t->mtdfldCount; --i > 0; sa++)
      {
         len = *bunch;
         *sa = (CharP)(bunch+1);
         *bunch = 0; // cut the string
         bunch += len+1;
      }
   }

   partSize = tczRead32(tcz);
   if (t->clsCount > 0)
   {
      uint8* bunch = heapAlloc(heap, partSize+1);
      tczRead(tcz, bunch, partSize);
      sa = t->cls = newPtrArrayOf(CharP, t->clsCount, heap);
      for (sa++, i = t->clsCount; --i > 0; sa++)
      {
         len = *bunch;
         *sa = (CharP)(bunch+1);
         *bunch = 0; // cut the string
         bunch += len+1;
      }
   }
   if (t->strCount > 0) // String constants must be the last loaded one!
   {
#ifdef DEBUG_OMM_LIST
      debug("Start of constant pool strings");
#endif
      oa = t->str = newPtrArrayOf(TCObject, t->strCount, heap);
      for (oa++, i = t->strCount; --i > 0; oa++)
      {
         tczRead(tcz, &mark, 1); // read the mark
         if (mark < 254) // can && l < 254
         {
            tczRead(tcz, chars, mark);
            chars[mark] = 0;
            if ((*oa = createStringObjectFromCharP(currentContext, chars, mark)) == null)
               HEAP_ERROR(heap, HEAP_MEMORY_ERROR);
         }
         else
         {
            len = tczRead16(tcz) & 0xFFFF;
            if ((*oa = createStringObjectWithLen(currentContext, len)) == null)
               HEAP_ERROR(heap, HEAP_MEMORY_ERROR);
            if (mark == 254) // can && l > 255
            {
               JCharP jc = String_charsStart(*oa);
               CharP c;
               while (len > 0)
               {
                  int32 l  = min32(255, len);
                  tczRead(tcz, chars, l);
                  c = chars;
                  len -= l;
                  while (--l >= 0)
                  {
                     *jc++ = *c & 0xFF;
                     c++;
                  }
               }
            }
            else // standard (mark=255)
            {
               tczRead(tcz, String_charsStart(*oa), len*2);
            }
         }
#ifdef TRACE_OBJCREATION
         {char* temp = String2CharP(*oa); debug("                                     = %s", temp); xfree(temp);}
#endif
      }
#ifdef DEBUG_OMM_LIST
      debug("end of constant pool strings");
#endif
   }
   // now we can generate the constants for mtd, because the constants were already loaded
   if (t->mtdCount > 0)
   {
      UInt16Array *ua = t->mtd+1;
      int32* hashNames  = t->hashNames  = newPtrArrayOf(Int32, t->mtdCount, heap);
      int32* hashParams = t->hashParams = newPtrArrayOf(Int32, t->mtdCount, heap);
      uint8* lens = t->mtdLens+1;
      for (i = t->mtdCount; --i > 0; ua++)
         methodHashCode(t->mtdfld[(*ua)[1]], (*ua)+2, *lens++, t, ++hashNames, ++hashParams); // generate a hash for quicker search
   }
   t->heap = heap; // we can assign the heap only when the read is successfull
   tcz->tempHeap = null;
}

static RegType getRegType(ConstantPool cp, uint16 idx)
{
   if (idx >= 10)
      return RegO;
   switch (cp->cls[idx][0])
   {
      case 'L':
         return RegL;
      case 'F':
      case 'D':
         return RegD;
      default :
         return RegI;
   }
}

static UInt8Array genParamRegs(ConstantPool cp, UInt16Array params, Heap heap)
{
   int32 n = ARRAYLEN(params);
   UInt8Array ret = newPtrArrayOf(UInt8, n, heap), r=ret;
   for (; n-- > 0; r++)
      *r = getRegType(cp, *params++);
   return ret;
}

static CharP appendFirstLetter(CharP fn, CharP pack)
{
   CharP next;
   while (true)
   {
      next = xstrchr(pack, '.');
      if (next != null) // is this the part of a package name?
         *fn++ = *pack;
      else
      {
         for (; *pack; pack++) // this is the class name, so add all uppercase letters
            if (('A' <= *pack && *pack <= 'Z') || ('0' <= *pack && *pack <= '9'))
               *fn++ = *pack;
         break;
      }
      pack = next+1;
   }
   return fn;
}

// totalcross.io.PDBFile.readBytes[&BII -> tiPDBF_readBytes_Bii
static CharP createMethodSignature(Method m, Heap h)
{
   char fn[100];
   CharP c = fn,mn;
   int32 n;
   uint16 *p;
   CharP* consts = m->class_->cp->cls;

   mn = m->name;
   c = appendFirstLetter(c, m->class_->name);
   *c++ = '_';
   while (*mn) *c++ = *mn++; // copy and go to end
   n = ARRAYLENV(m->cpParams);
   if (n > 0 && (c-fn) < 31) // 32-1 (used by the '_')
   {
      *c++ = '_';
      for (p = m->cpParams; (c-fn) < 32 && n-- > 0; p++,c++)
      {
         CharP pname = consts[*p];
         if (*p < 10) // primitive type?
            *c = toLower(*pname);
         else // object, need to scan for the first class name in uppercase
         {
            bool isUp = false;
            for (; *pname == '['; pname++) // use uppercase for arrays - stop the for only when the [ sequence ends
               isUp = true;
            if (isUp && *pname == '&') // array of primitive?
               *c = *++pname;
            else
               for (*c = *pname++; *pname; pname++) // guich@20131224: fix support for totalcross.db.sqlite.DB$ProgressObserver
                  if (*pname == '.' || *pname == '$')
                     *c = *(pname+1); // don't break! we want the last piece
            *c = isUp ? toUpper(*c) : toLower(*c);
         }
      }
   }
   if ((c-fn) > 32)
      c = &fn[32];
   *c = 0;
   return hstrdup(fn, h);
}

#define ALL_1 255

static void readMethod(ConstantPool cp, TCZFile tcz, Method m, TCClass c)
{
   TMethodInfoHeader ti;
   int32 i;
   ExceptionArray ex; TExceptionInfo exi;

   tczRead(tcz, &ti, 16);

   m->flags = ti.flags;
   m->iCount  = ti.iCount;
   m->oCount  = ti.oCount;
   m->v64Count = ti.v64Count;
   m->paramCount = ti.paramCount;
   m->name = cp->mtdfld[ti.cpName];
   m->cpReturn = ti.cpReturn;

   m->class_ = c;

   // read the method signature
   if (m->paramCount > 0)
   {
      m->cpParams = newPtrArrayOf(UInt16, m->paramCount, tcz->tempHeap);
      tczRead(tcz, m->cpParams, 2*m->paramCount);
      m->paramRegs = genParamRegs(cp, m->cpParams, tcz->tempHeap);
   }
   methodHashCode(m->name, m->cpParams, m->paramCount, c->cp, &m->hashName, &m->hashParams);
   // read the opcodes
   if (ti.opcodeCount > 0)
   {
      m->code = newArrayOf(Code, ti.opcodeCount, tcz->tempHeap);
      tczRead(tcz, m->code, 4*ti.opcodeCount);
   }
   // read the Exception handlers
   if (ti.exceptionHandlersCount > 0)
   {
      m->exceptionHandlers = newArrayOf(Exception, ti.exceptionHandlersCount, tcz->tempHeap);
      for (i = ti.exceptionHandlersCount, ex = m->exceptionHandlers; i-- > 0; ex++)
      {
         tczRead(tcz, &exi, 10);
         ex->startPC = m->code + exi.startPC;
         ex->endPC = m->code + exi.endPC;
         ex->handlerPC = m->code + exi.handlerPC;
         ex->className = cp->cls[exi.cpIdxClassName];
         ex->regO = exi.regO;
      }
   }
   // read the line number debug info
   if (ti.lineNumberInfoCount > 0)
   {
      uint8 first;
      uint16 *p, *pend;
      uint8 *t;
      m->lineNumberStartPC = newPtrArrayOf(UInt16, ti.lineNumberInfoCount, tcz->tempHeap); // guich@tc110_69: now the line number is placed in two uint16 arrays.
      m->lineNumberLine    = newPtrArrayOf(UInt16, ti.lineNumberInfoCount, tcz->tempHeap);
      tczRead(tcz, &first, 1);
      if (ti.lineNumberInfoCount == 1)
      {
         uint8 second;
         tczRead(tcz, &second, 1);
         m->lineNumberLine[0] = (second << 8) | first; // pc is already 0
      }
      else
      {
         p = m->lineNumberStartPC;
         pend = p + ti.lineNumberInfoCount;
         // read pc
         if (first == ALL_1) // all1 means pcs = 0,1,2,3,4,... p[0] is already 0
            for (i=1; i < ti.lineNumberInfoCount; i++)
               *++p = i;
         else
         {
            *++p = first; // p[0] is always 0, so we skip it; put in p[1] the first that was already read
            if (ti.lineNumberInfoCount > 2)
            {
               t = (uint8*)p;
               tczRead(tcz, t+1, ti.lineNumberInfoCount-2); // read the byte array into the short array
               for (i = ti.lineNumberInfoCount-2; i >= 1; i--) // transform the byte array into a short array. p[0] and p[1] are already filled - guich@tc114_83: correct is -2, not -1
               {
                  p[i] = t[i];
                  t[i] = 0;
               }
            }
            while (++p < pend)
               *p += *(p-1);
         }
         // line numbers - read the first, then all differences
         p = m->lineNumberLine;
         pend = p + ti.lineNumberInfoCount;
         t = (uint8*)p;
         tczRead(tcz, t, 3); // read the starting line number and the first
         if (t[2] == ALL_1)
            for (i=1,p++; i < ti.lineNumberInfoCount; i++,p++)
               *p = m->lineNumberLine[0] + i;
         else
         {
            // now we have: p[0] = line[0], and first at the nibble 1 of p[1]
            if (ti.lineNumberInfoCount > 2)
            {
               tczRead(tcz, t+3, ti.lineNumberInfoCount-2); // read the byte array into the short array
               for (i = ti.lineNumberInfoCount-1; i >= 2; i--) // transform the byte array into a short array. p[0] and p[1] are already filled
               {
                  p[i] = t[i+1];
                  t[i+1] = 0;
               }
            }
            while (++p < pend)
               *p += *(p-1);
         }
      }
   }
   if (m->paramCount > 0)
      m->paramSkip = (uint8)((3 + m->paramCount - (m->cpReturn == 0)) >> 2);
   if (m->flags.isNative)
      m->nativeSignature = createMethodSignature(m, tcz->tempHeap);
   if (m->cpReturn > 0)
      m->returnReg = getRegType(cp, m->cpReturn);
}

static FieldArray readFields(ConstantPool cp, int32 len, TCZFile tcz, FieldArray super, CharP sourceClass) // sourceClass: numeric fields must pass the source class, object fields must pass null
{
   TFieldInfo ci;
   int32 lens = ARRAYLENV(super);
   Field f,f0;
   if (len == 0 && lens == 0)
      return null;
   f0 = f = newArrayOf(Field, len+lens, tcz->tempHeap);

   // first, get the inherited fields, to keep the same order
   for (; lens-- > 0; f++,super++)
   {
      *f = *super;
      f->flags.isInherited = true;
   }
   // then, get our fields
   for (; len-- > 0; f++)
   {
      tczRead(tcz, &ci, 6);

      f->name = cp->mtdfld[ci.cpIdxName];
      f->flags  = ci.flags;
      f->sourceClassName = sourceClass;
      if (ci.cpIdxType) f->targetClassName = cp->cls[ci.cpIdxType];
   }
   return f0;
}

static TCClass readClass(Context currentContext, ConstantPool cp, TCZFile tcz)
{
   int32 i, j, superI32, superObj, superV64, totalI32, totalObj, totalV64;
   uint16 u16;
   TClassInfoHeader ci;
   TCClass c;
   volatile Heap heap;

   heap = tcz->tempHeap = heapCreate();
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      return !heap || heap->ex.errorCode == HEAP_MEMORY_ERROR ? CLASS_OUT_OF_MEMORY : null;
   }
   heap->greedyAlloc = true;

   IF_HEAP_ERROR(tcz->header->hheap)
   {
      heapDestroy(heap);
      return !heap || heap->ex.errorCode == HEAP_MEMORY_ERROR ? CLASS_OUT_OF_MEMORY : null;
   }

   tczRead(tcz, &ci, 22);

   c = newXH(TCClass, heap);
   c->cp = cp;
   c->heap = heap;
   c->name = cp->cls[ci.cpIdxClassName];
   c->flags = ci.flags;

   // read the Interface indexes
   if (ci.interfacesCount > 0)
   {
      c->interfaces = newPtrArrayOf(TCClass, ci.interfacesCount, heap);
      for (i = 0; i < ci.interfacesCount; i++)
      {
         u16 = (uint16)tczRead16(tcz);
         if ((c->interfaces[i] = loadClass(currentContext, cp->cls[u16], true)) == null)
            HEAP_ERROR(heap, 11);
      }
   }

   if (ci.cpIdxSuperClass) // java.lang.Object does not have a superclass
   {
      c->superClass = loadClass(currentContext, cp->cls[ci.cpIdxSuperClass], true);
      if (c->superClass == null)
      {
         alert("readClass - Superclass not found: %s",cp->cls[ci.cpIdxSuperClass]); // TODO throw an exception
         HEAP_ERROR(heap, 10);
      }

      // read the fields
      if (ci.i32StaticFieldsCount > 0)
      {
         c->i32StaticFields = readFields(cp, ci.i32StaticFieldsCount, tcz, null, c->name);
         c->i32StaticValues = newPtrArrayOf(Int32, ci.i32StaticFieldsCount, heap);
      }
      if (ci.objStaticFieldsCount > 0)
      {
         c->objStaticFields   = readFields(cp, ci.objStaticFieldsCount, tcz, null, c->name);
         c->objStaticValues = newPtrArrayOf(TCObject, ci.objStaticFieldsCount, heap);
      }
      if (ci.v64StaticFieldsCount > 0)
      {
         c->v64StaticFields = readFields(cp, ci.v64StaticFieldsCount, tcz, null, c->name);
         c->v64StaticValues = newPtrArrayOf(Double, ci.v64StaticFieldsCount, heap);
      }
      superI32 = ARRAYLENV(c->superClass->i32InstanceFields);
      superObj = ARRAYLENV(c->superClass->objInstanceFields);
      superV64 = ARRAYLENV(c->superClass->v64InstanceFields);

      if (ci.i32InstanceFieldsCount > 0 || superI32 > 0)
         c->i32InstanceFields = readFields(cp, ci.i32InstanceFieldsCount, tcz, c->superClass->i32InstanceFields, c->name);
      if (ci.objInstanceFieldsCount > 0 || superObj > 0)
         c->objInstanceFields = readFields(cp, ci.objInstanceFieldsCount, tcz, c->superClass->objInstanceFields, c->name);
      if (ci.v64InstanceFieldsCount > 0 || superV64 > 0)
         c->v64InstanceFields = readFields(cp, ci.v64InstanceFieldsCount, tcz, c->superClass->v64InstanceFields, c->name);

      // load the instance fields of this class and the inherited ones
      totalI32 = ci.i32InstanceFieldsCount + superI32;
      totalObj = ci.objInstanceFieldsCount + superObj;
      totalV64 = ci.v64InstanceFieldsCount + superV64;
      c->objSize = totalI32 * 4 + totalObj * TSIZE + totalV64 * 8;
      // The fields are placed sequentialy: int1, int2, ..., obj1, obj2, ..., double1/long1, double2/long2, ...
      // In each type, this class instance fields go first and non-private Inherited fields go LAST.
      c->objOfs = totalI32 * 4;
      c->v64Ofs = totalI32 * 4 + totalObj * TSIZE;

      // create a matrix of Fields so it can be easily accessed given the register type
      c->instanceFields[RegI] = c->i32InstanceFields;
      c->instanceFields[RegO] = c->objInstanceFields;
      c->instanceFields[RegD] = c->instanceFields[RegL] = c->v64InstanceFields;

      c->staticFields[RegI] = c->i32StaticFields;
      c->staticFields[RegO] = c->objStaticFields;
      c->staticFields[RegD] = c->staticFields[RegL] = c->v64StaticFields;
   }

   // read the methods
   if (ci.methodsCount > 0)
   {
      Method m = c->methods = newArrayOf(Method, ci.methodsCount, heap);
      for (i = 0; i < ci.methodsCount; i++, m++)
      {
         readMethod(cp, tcz, m, c);
         if (m->paramCount == 0 && *m->name == 'f' && strEq(m->name, "finalize"))
         {
            c->finalizeMethod = m;
            // now search for a dontFinalize field
            for (j = ARRAYLENV(c->i32InstanceFields)-1; j >= 0; j--)
               if (strEq(c->i32InstanceFields[j].name,"dontFinalize"))
               {
                  c->dontFinalizeFieldIndex = j+1; // 0 will mean no field
                  break;
               }
         }
      }
   }
   if (c->finalizeMethod == null && c->superClass != null) // if no finalize methods are defined in this class, inherit it from the super class
      c->finalizeMethod = c->superClass->finalizeMethod;
   //if (c->dontFinalize == null && c->superClass != null) c->dontFinalize = c->superClass->dontFinalize;
   return c;
}

bool initClassInfo()
{
   SETUP_MUTEX;
   INIT_MUTEX(classLoaderLock);
   htLoadedClasses = htNew(0xFF,null);
   htMutexes = htNew(10, null);
   return true;
}

static void freeClass(int32 i32, VoidP ptr)
{
   TCClass c = (TCClass)ptr;
   UNUSED(i32);
#ifdef TRACE_OBJCREATION
   debug("Destroying class %s, alloc count: %d, alloc total: %d, avail: %d, block count: %d",c->name, c->heap->numAlloc, c->heap->totalAlloc, c->heap->totalAvail, c->heap->blocksAlloc);
#endif
   heapDestroy(c->heap);
}

void destroyClassInfo()
{
   DESTROY_MUTEX(classLoaderLock);
   htFree(&htLoadedClasses, freeClass);
   htFree(&htMutexes, freeMutex);
}

static int32 getArrayElementSize(CharP type)
{
   if (*type == '[')
      type++;
   if (*type == '&') // marker for array of primitives
      switch (*++type)
      {
         case 'I': return 2;
         case 'B': return 0;
         case 'S': return 1;
         case 'C': return 1;
         case 'b': return 0;
         case 'L': return 3;
         case 'F':
         case 'D': return 3;
      }
   return 2;
}

TCClass loadClass(Context currentContext, CharP className, bool throwClassNotFound) // do NOT throw error messages from here
{
   volatile TCClass ret;
   int32 hc;
   Method staticInitializer = null;
   // check if we already have loaded it
   LOCKVAR(classLoaderLock);
   hc = hashCodeSlash2Dot(className);
   ret = (TCClass)htGetPtr(&htLoadedClasses, hc);
   if (ret == null)
   {
      bool isArray;
      TCZFile tcz;
      CharP realClassName = className;
      // check if we're loading an array and load the correct class instead.
      isArray = *className == '[';  // if we're "loading" an array, then load the java.lang.Array instead
      if (isArray)
         realClassName = "java.lang.Array";
      tcz = tczGetFile(realClassName, true);
      if (tcz != null)
      {
#ifdef TRACE_OBJCREATION
         int32 fr = getFreeMemory(false);
         debug("**** READING CLASS %s", realClassName);
#endif
         ret = readClass(currentContext, tcz->header->cp, tcz);
         tczClose(tcz);
#ifdef TRACE_OBJCREATION
         debug("**** putting class %s in hashtable. Consumed: %d ****", realClassName, getFreeMemory(false)-fr);
#endif
         // call static initializer
         if (ret != null && ret != CLASS_OUT_OF_MEMORY)
         {
            TCClass ret2 = (TCClass)htGetPtr(&htLoadedClasses, hc); // guich@tc110_92: the class may have a reference to itself, which was loaded in the readClass above. So we double-check for it and use the previous version
            if (ret2 != null || !htPutPtr(&htLoadedClasses, hc, ret)) // class must be placed in the loaded classes before any method runs because the GC can be triggered and this class' objects may be collected
            {
               freeClass(0, ret);
               ret = ret2;
            }
            else
               staticInitializer = getMethod(ret, false, STATIC_INIT_NAME, 0);
         }
      }
      // if was an array, replace the real java file by the given one
      if (ret != null && ret != CLASS_OUT_OF_MEMORY)
      {
         if (isArray)
         {
            ret->name = className;
            ret->flags.bits2shift = (uint16)getArrayElementSize(className); // store the bit shift
            ret->flags.isObjectArray = className[1] != '&'; // [&I: int array
         }
         ret->hash = hc;
      }
   }
   UNLOCKVAR(classLoaderLock);

   if (ret == CLASS_OUT_OF_MEMORY)
	   throwException(currentContext, OutOfMemoryError, className);
   else
   if (ret == null && throwClassNotFound)
      throwException(currentContext, ClassNotFoundException, className);
   else if (staticInitializer)
      executeMethod(currentContext, staticInitializer);
   return ret == CLASS_OUT_OF_MEMORY ? null : ret;
}

Type type2javaType(CharP type)
{
   if (*type == '[')
      type++;
   if (*type == '&') // array of primitives?
      type++;
   if (!*type)
      return Type_Void; // Type_None or Type_Null would be better
   switch (*type)
   {
      case 'I': return Type_Int;
      case 'B': return Type_Byte;
      case 'S': return Type_Short;
      case 'C': return Type_Char;
      case 'b': return Type_Boolean;
      case 'L': return Type_Long;
      case 'D': return Type_Double;
      case 'F': return Type_Double;
      default : return Type_Object;
   }
}

bool isSuperClass(TCClass s, TCClass t) // s instanceof t
{
   int32 i;
   for (; s != null; s = s->superClass)
      if (s == t)
         return true;
      else
      if (s->interfaces != null)
         for (i = ARRAYLEN(s->interfaces)-1; i >= 0; i--)
            if (isSuperClass(s->interfaces[i], t))
               return true;
   return false;
}

CompatibilityResult areClassesCompatible(Context currentContext, TCClass s, CharP ident)  // S instanceof idenT ?
{
   TCClass t=null;
   CompatibilityResult result = NOT_COMPATIBLE;
   bool sIsArray,tIsArray;
   CharP className;

   if (s == null)
   {
      debug("areClassesCompatible: class s is null");
      return NOT_COMPATIBLE;
   }
   className = s->name;
tryAgain:
   if (ident == null || className == null)
   {
      debug("areClassesCompatible: %X (%s) / %X (%s)", ident, ident ? ident : "null", className, className ? className : "null");
      return NOT_COMPATIBLE;
   }
   tIsArray = *ident == '[';
   sIsArray = *className == '[';
   if (!s && !(s = loadClass(currentContext, className, true)))
      result = TARGET_CLASS_NOT_FOUND;
   if (strEq(ident,className)) // quick test
      result = COMPATIBLE;
   else
   if (*ident == '&' || *className == '&') // if comparing a [String with a [&S, at the second iteration will remain String &S, which results to false.
      result = NOT_COMPATIBLE;
   else
   if (!tIsArray && (t = loadClass(currentContext, ident, true)) == null) // not an array but class was not found? - note: we should throw a ClassNotFoundException here
      result = TARGET_CLASS_NOT_FOUND;
   else
   if (s == t) // another quick test
      result = COMPATIBLE;
   else
   if (strEq(className, "java.lang.String") && strEq(ident, "java.lang.Class"))
      result = COMPATIBLE;
   else
   // If S is an ordinary (nonarray) class, then:
   if (!sIsArray)
   {
      if (!t)
         result = tIsArray ? 0 : TARGET_CLASS_NOT_FOUND; // s not array but t is array ? not compatible; otherwise, goto throwClassNotFoundException
      else
      // If T is an interface type, then S must implement (2.13) interface T.
      if (t->flags.isInterface)
      {
         int32 n = ARRAYLENV(s->interfaces),i;
         for (i = 0; i < n; i++)
            if (t == s->interfaces[i])
            {
               result = COMPATIBLE;
               break;
            }
      }
      //  no else here - if T is an interface, check also if S has any subclasses that implements T
      // If T is a class type, then S must be the same class (2.8.1) as T, or a subclass of T.
      if (!tIsArray)
         result = ((s == t) || isSuperClass(s, t)) ? COMPATIBLE : NOT_COMPATIBLE;
   }
   else
   // If S is an interface type, then:
   if (s->flags.isInterface)
   {
      if (!t)
         result = TARGET_CLASS_NOT_FOUND;
      else
      // If T is an interface type, then T must be the same interface as S or a superinterface of S (2.13.2).
      if (t->flags.isInterface)
         result = ((s == t) || isSuperClass(s, t)) ? COMPATIBLE : NOT_COMPATIBLE;
      else
      // If T is a class type, then T must be Object (2.4.7).
      if (!tIsArray)
         result = strEq("java.lang.Object", t->name) ? COMPATIBLE : NOT_COMPATIBLE;
   }
   else
   // If S is a class representing the array type SC[], that is, an array of components of type SC, then:
   if (sIsArray)
   {
      // If T is an array type TC[], that is, an array of components of type TC, then one of the following must be true:
      if (tIsArray)
      {
         // TC and SC are the same primitive type (2.4.1).
         Type sc = type2javaType(className);
         Type tc = type2javaType(ident); // already strips the [
         if (sc < Type_Object && tc < Type_Object)
            result = (sc == tc) ? COMPATIBLE : NOT_COMPATIBLE;
         else
         {
            // TC and SC are reference types (2.4.6), and type SC can be cast to TC by recursive application of these rules.
            ident++; // strip the first array dimension and try again
            className++;
            s = null;
            goto tryAgain;
         }
      }
      else
      if (!t) // a piece of an array?
         result = NOT_COMPATIBLE;
      else
      //   If T is an interface type, T must be one of the interfaces implemented by arrays (2.15).
      if (t->flags.isInterface)
         result = (strEq("java.lang.Cloneable",t->name) || strEq("java.io.Serializable",t->name)) ? COMPATIBLE : NOT_COMPATIBLE;
      //   If T is a class type, then T must be Object (2.4.7).
      else
         result = strEq("java.lang.Object", t->name) ? COMPATIBLE : NOT_COMPATIBLE;
   }
   return result;
}
