// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined(__GNUC__) && !defined(TRACK_USED_OPCODES)
#define DIRECT_JUMP
#endif

#define TRACE if (traceOn) debug
#define DUMP_BYTECODE(s) //TRACE("%s",s); //TRACE("T %08d %X %X %05d - %4d: %s", getTimeStamp(), thread, context, ++context->ccon, (int32)(code-method->code), s);

#ifdef DIRECT_JUMP // use a direct jump if supported
 #define OPCODE(x) _##x: DUMP_BYTECODE(#x)
 #define NEXT_OP goto *address[(++code)->s24.op];
 #define NEXT_OP0 goto *address[code->s24.op];
 #define FIRST_OP NEXT_OP0
 #define OPADDR(x) _address[x] = &&_##x;

 #define XOPTION(pref,x) pref##x
 #define XSELECT(addr,idx) goto *addr[idx];
#else
 #define OPCODE(x) case x: DUMP_BYTECODE(#x)
 #define NEXT_OP  code++; goto mainLoop;
 #define NEXT_OP0 goto mainLoop;
 #define FIRST_OP switch (code->s24.op)

 #define XOPTION(pref,x) case x
 #define XSELECT(addr, idx) switch (idx)
#endif

TC_API void jlC_forName_s(NMParams p);
//////////////////////////////////////////////////////////////////////////////
#ifdef ENABLE_TRACE
static CharP getSpaces(Context currentContext, int32 n)
{
   if (n > 0)
   {
      n = min32(n, sizeof(currentContext->spaces)-1);
      xmemset(currentContext->spaces, ' ', n);
      currentContext->spaces[n] = 0;
      return currentContext->spaces;
   }
   else return "";
}
#endif

static void tcvmCreateException(Context currentContext, Throwable t, int32 pc, int32 decTrace, CharP message, ...)
{
   if (currentContext->thrownException == null) // don't overwrite a thrown exception
   {
      char str[1024];
      if (message)
      {
         va_list args;
         va_start(args, message);
         vsprintf(str, message, args);
         va_end(args);
      }
      if (t == OutOfMemoryError)           
      {
         currentContext->thrownException = currentContext->OutOfMemoryErrorObj;
         if (message)
         {
            *Throwable_msg(currentContext->thrownException) = createStringObjectFromCharP(currentContext, str,-1);
            setObjectLock(*Throwable_msg(currentContext->thrownException), UNLOCKED);
            if (*Throwable_msg(currentContext->thrownException) == null)
               debug("out of memory error reason: %s",str);
         }
      }
      else
         createException(currentContext, t, false, message != null ? str : null);
      fillStackTrace(currentContext, currentContext->thrownException, pc, currentContext->callStack + decTrace);
   }
}

bool checkArrayRange(Context currentContext, TCObject obj, int32 start, int32 count) // check if the given array can access from start to start+count-1
{
   if (obj == null)
      throwException(currentContext, NullPointerException,"In checkArrayRange");
   else
   {
      int32 len = ARRAYOBJ_LEN(obj);
      int32 end = start+count-1;
      if (start < 0 || count < 0 || !(end < len))
         throwException(currentContext, ArrayIndexOutOfBoundsException, "In checkArrayRange(%d, %d)", (int)start, (int)count);
      else
         return true;
   }
   return false;
}

#if defined(WINCE) || defined(WIN32)
#pragma optimize( "ys", off ) // with this pragma, evc3 makes less one branch per instruction
#endif

// macros used in this function
#define ARRAYCHECK(target)                                                                                \
   if (regO[target##_ar.base] == null)                                                                    \
      {exceptionMsg = "When checking array object"; goto throwNullPointerException;}                      \
   if (regI[target##_ar.idx] < 0 || regI[target##_ar.idx] >= (int32)ARRAYOBJ_LEN(regO[target##_ar.base])) \
   {                                                                                                      \
      tcvmCreateException(context, ArrayIndexOutOfBoundsException, (int32)(code-method->code), 0, "%d must be >= 0 and < %d", \
           (int)regI[target##_ar.idx], (int)ARRAYOBJ_LEN(regO[target##_ar.base]));                        \
      goto handleException;                                                                               \
   }

#define GET_STATIC_FIELD(type)                                                     \
   sf = cp->boundSField[code->static_reg.sym];                                     \
   if (sf == null)                                                                 \
   {                                                                               \
      if ((sf = getSField_Ref(context, class_, code->static_reg.sym, type)) >= SF_ERROR_ANY) \
      {                                                                            \
         getSField_Names(cp, code->static_reg.sym, &fieldName, &className);        \
         if (sf == SF_CLASS_ERROR)                                                 \
            goto throwClassNotFoundException;                                      \
         else                                                                      \
            goto throwNoSuchFieldError;                                            \
      }                                                                            \
   }

#define GET_INSTANCE_FIELD(type)                                              \
   o = regO[code->field_reg.this_];                                            \
   if (o)                                                                     \
   {                                                                          \
      retv = cp->boundIField[code->field_reg.sym];                            \
      if (retv == UNBOUND)                                                    \
      {                                                                       \
         retv = getIField_Index(cp, o, code->field_reg.sym, type);            \
         if (retv >= UNBOUND_ERROR)                                           \
         {                                                                    \
            getIField_Names(cp, code->field_reg.sym, &fieldName, &className); \
            if (retv == UNBOUND_CLASS_ERROR)                                  \
               goto throwClassNotFoundException;                              \
            else                                                              \
               goto throwNoSuchFieldError;                                    \
         }                                                                    \
      }                                                                       \
   }                                                                          \
   else {exceptionMsg = "Getting instance field"; goto throwNullPointerException;}

#ifdef DIRECT_JUMP // use a direct jump if supported
uint32 *_address[OPCODE_LENGTH];
uint32 *_addrMtdParam[4];
uint32 *_addrMtdcParam[4];
uint32 *_addrMtdbParam[4];
uint32 *_addrInParam[4];
uint32 *_addrInParamA[4];
uint32 *_addrNMRet[4];
#endif

/* Note: this code is highly optimized. So, even if some things seems not "natural", keep it that way! */
TC_API TValue executeMethod(Context context, Method method, ...)
{
   // cache the current context into local variables
   Int32Array   regI  = context->regI;
   Value64Array reg64 = context->reg64;
   TCObjectArray  regO  = context->regO,regO2;
   int32 callStackMethodEnd = 0;
   // get method's variables
   register Code code = method->code;
   TCClass class_ = method->class_, c=null,thisClass;
   ConstantPool cp = class_->cp;
   uint32 nparam=0;
   NMParams nmp = &context->nmp;
   CharP className, methodName, fieldName;
   int32 i,len;
   UInt16Array sym;
   bool originalClassIsInterface,directNativeCall=false;
   CharP exceptionMsg = null;
   TCObject o=null;
   Method newMethod=null;
   uint16 retv;
   VoidP sf;
   TValue returnedValue;
   MethodAndClass mac,last;
   MethodAndClass* macArray;
   MethodAndClass* boundHead=null;
   int32 hashName,hashParams;
   ThreadHandle thread;

   printf("m: %s, %s\n", method->class_->name, method->name);

#ifdef DIRECT_JUMP // use a direct jump if supported
   uint32 **address;
   uint32 **addrMtdParam;
   uint32 **addrMtdcParam;
   uint32 **addrMtdbParam;
   uint32 **addrInParam;
   uint32 **addrInParamA;
   uint32 **addrNMRet;
   if (_address[0] == null)
   {
      _addrMtdParam[RegI] = &&aRegI;  _addrMtdParam[RegO] = &&aRegO;  _addrMtdParam[RegD] = &&aRegD;  _addrMtdParam[RegL] = &&aRegL;
      _addrMtdcParam[RegI] = &&cRegI; _addrMtdcParam[RegO] = &&cRegO; _addrMtdcParam[RegD] = &&cRegD; _addrMtdcParam[RegL] = &&cRegL;
      _addrMtdbParam[RegI] = &&bRegI; _addrMtdbParam[RegO] = &&bRegO; _addrMtdbParam[RegD] = &&bRegD; _addrMtdbParam[RegL] = &&bRegL;
      _addrInParam[RegI] = &&dRegI;   _addrInParam[RegO] = &&dRegO;   _addrInParam[RegD] = &&dRegD;   _addrInParam[RegL] = &&dRegL;
      _addrInParamA[RegI] = &&eRegI;  _addrInParamA[RegO] = &&eRegO;  _addrInParamA[RegD] = &&eRegD;  _addrInParamA[RegL] = &&eRegL;
      _addrNMRet[RegI] = &&nmRegI;    _addrNMRet[RegO] = &&nmRegO;    _addrNMRet[RegD] = &&nmRegD;    _addrNMRet[RegL] = &&nmRegL;
      OPADDR(BREAK)               OPADDR(MOV_regI_regI)       OPADDR(MOV_regI_field)      OPADDR(MOV_regI_static)     OPADDR(MOV_regI_aru)       OPADDR(MOV_regI_arc)        OPADDR(MOV_regI_sym)        OPADDR(MOV_regI_s18)        OPADDR(MOV_regI_arlen)     OPADDR(MOV_regO_regO)       OPADDR(MOV_regO_field)      OPADDR(MOV_regO_static)     OPADDR(MOV_regO_aru)       OPADDR(MOV_regO_arc)        OPADDR(MOV_regO_sym)        OPADDR(MOV_reg64_reg64)     OPADDR(MOV_reg64_field)      OPADDR(MOV_reg64_static)    OPADDR(MOV_reg64_aru)       OPADDR(MOV_reg64_arc)       OPADDR(MOV_regD_sym)       OPADDR(MOV_regL_sym)        OPADDR(MOV_regD_s18)        OPADDR(MOV_regL_s18)        OPADDR(MOV_field_regI)     OPADDR(MOV_field_regO)      OPADDR(MOV_field_reg64)     OPADDR(MOV_static_regI)     OPADDR(MOV_static_regO)    OPADDR(MOV_static_reg64)    OPADDR(MOV_arc_regI)        OPADDR(MOV_arc_regO)
      OPADDR(MOV_arc_reg64)       OPADDR(MOV_aru_regI)        OPADDR(MOV_aru_regO)        OPADDR(MOV_aru_reg64)       OPADDR(MOV_arc_regIb)      OPADDR(MOV_arc_reg16)       OPADDR(MOV_aru_regIb)       OPADDR(MOV_aru_reg16)       OPADDR(MOV_regIb_arc)      OPADDR(MOV_reg16_arc)       OPADDR(MOV_regIb_aru)       OPADDR(MOV_reg16_aru)       OPADDR(MOV_regO_null)      OPADDR(INC_regI)            OPADDR(ADD_regI_regI_regI)  OPADDR(ADD_regI_s12_regI)   OPADDR(ADD_regI_arc_s6)      OPADDR(ADD_regI_aru_s6)     OPADDR(ADD_regI_regI_sym)   OPADDR(ADD_regD_regD_regD)  OPADDR(ADD_regL_regL_regL) OPADDR(ADD_aru_regI_s6)     OPADDR(SUB_regI_s12_regI)   OPADDR(SUB_regI_regI_regI)  OPADDR(SUB_regD_regD_regD) OPADDR(SUB_regL_regL_regL)  OPADDR(MUL_regI_regI_s12)   OPADDR(MUL_regI_regI_regI)  OPADDR(MUL_regD_regD_regD) OPADDR(MUL_regL_regL_regL)  OPADDR(DIV_regI_regI_s12)   OPADDR(DIV_regI_regI_regI)
      OPADDR(DIV_regD_regD_regD)  OPADDR(DIV_regL_regL_regL)  OPADDR(MOD_regI_regI_s12)   OPADDR(MOD_regI_regI_regI)  OPADDR(MOD_regD_regD_regD) OPADDR(MOD_regL_regL_regL)  OPADDR(SHR_regI_regI_s12)   OPADDR(SHR_regI_regI_regI)  OPADDR(SHR_regL_regL_regL) OPADDR(SHL_regI_regI_s12)   OPADDR(SHL_regI_regI_regI)  OPADDR(SHL_regL_regL_regL)  OPADDR(USHR_regI_regI_s12) OPADDR(USHR_regI_regI_regI) OPADDR(USHR_regL_regL_regL) OPADDR(AND_regI_regI_s12)   OPADDR(AND_regI_aru_s6)      OPADDR(AND_regI_regI_regI)  OPADDR(AND_regL_regL_regL)  OPADDR(OR_regI_regI_s12)    OPADDR(OR_regI_regI_regI)  OPADDR(OR_regL_regL_regL)   OPADDR(XOR_regI_regI_s12)   OPADDR(XOR_regI_regI_regI)  OPADDR(XOR_regL_regL_regL) OPADDR(JEQ_regO_regO)       OPADDR(JEQ_regO_null)       OPADDR(JEQ_regI_regI)       OPADDR(JEQ_regL_regL)      OPADDR(JEQ_regD_regD)       OPADDR(JEQ_regI_s6)         OPADDR(JEQ_regI_sym)
      OPADDR(JNE_regO_regO)       OPADDR(JNE_regO_null)       OPADDR(JNE_regI_regI)       OPADDR(JNE_regL_regL)       OPADDR(JNE_regD_regD)      OPADDR(JNE_regI_s6)         OPADDR(JNE_regI_sym)        OPADDR(JLT_regI_regI)       OPADDR(JLT_regL_regL)      OPADDR(JLT_regD_regD)       OPADDR(JLT_regI_s6)         OPADDR(JLE_regI_regI)       OPADDR(JLE_regL_regL)      OPADDR(JLE_regD_regD)       OPADDR(JLE_regI_s6)         OPADDR(JGT_regI_regI)       OPADDR(JGT_regL_regL)        OPADDR(JGT_regD_regD)       OPADDR(JGT_regI_s6)         OPADDR(JGE_regI_regI)       OPADDR(JGE_regL_regL)      OPADDR(JGE_regD_regD)       OPADDR(JGE_regI_s6)         OPADDR(JGE_regI_arlen)      OPADDR(DECJGTZ_regI)       OPADDR(DECJGEZ_regI)        OPADDR(TEST_regO)           OPADDR(JUMP_s24)            OPADDR(CONV_regI_regL)     OPADDR(CONV_regI_regD)      OPADDR(CONV_regIb_regI)     OPADDR(CONV_regIc_regI)
      OPADDR(CONV_regIs_regI)     OPADDR(CONV_regL_regI)      OPADDR(CONV_regL_regD)      OPADDR(CONV_regD_regI)      OPADDR(CONV_regD_regL)     OPADDR(RETURN_regI)         OPADDR(RETURN_regO)         OPADDR(RETURN_reg64)        OPADDR(RETURN_void)        OPADDR(RETURN_s24I)         OPADDR(RETURN_null)         OPADDR(RETURN_s24D)         OPADDR(RETURN_s24L)        OPADDR(RETURN_symI)         OPADDR(RETURN_symO)         OPADDR(RETURN_symD)         OPADDR(RETURN_symL)          OPADDR(SWITCH)              OPADDR(NEWARRAY_len)        OPADDR(NEWARRAY_regI)       OPADDR(NEWARRAY_multi)     OPADDR(NEWOBJ)              OPADDR(THROW)               OPADDR(INSTANCEOF)          OPADDR(CHECKCAST)          OPADDR(CALL_normal)         OPADDR(CALL_virtual)        OPADDR(JUMP_regI)           OPADDR(MONITOR_Enter)      OPADDR(MONITOR_Enter2)      OPADDR(MONITOR_Exit)        OPADDR(MONITOR_Exit2)
   }
   address = _address;
   addrMtdParam = _addrMtdParam;
   addrMtdcParam = _addrMtdcParam;
   addrMtdbParam = _addrMtdbParam;
   addrInParam = _addrInParam;
   addrInParamA = _addrInParamA;
   addrNMRet = _addrNMRet;
#endif

   // First, check if it's safe to execute over this context
   thread = threadGetCurrent();
   LOCKVAR(context->usageLock);
   if (thread != null && context->usageOwner != thread)
   {
      if (context->usageOwner != null)
      {
#ifdef ENABLE_TRACE
         TRACE("T %08d %X %X %05d - Cannot acquire context; owner=%X; usageCount=%d", getTimeStamp(), thread, context, ++context->ccon, context->usageOwner, context->usageCount);
#else
         debug("Cannot acquire context! Waiting to release...");
#endif
         do
         {
            UNLOCKVAR(context->usageLock);
            Sleep(1); // force a context switch (so the other thread can release the context at some point)
            LOCKVAR(context->usageLock);
         }
         while (context->usageOwner != null); // while this context is not released
#ifndef ENABLE_TRACE
         debug("Context released!");
#endif
      }
      context->usageOwner = thread;
   }
   context->usageCount++;
#ifdef ENABLE_TRACE
   TRACE("T %08d %X %X %05d - Context acquired; usageCount=%d", getTimeStamp(), thread, context, ++context->ccon, context->usageCount);
#endif
   UNLOCKVAR(context->usageLock);

   // push the method being called so a stack trace can take that method into consideration

#ifdef ENABLE_TEST_SUITE
   if (context->callStackForced)
      callStackMethodEnd = context->callStackForced - context->callStackStart; // needed by the RETURN_XXX test cases
#endif

   returnedValue.asInt32 = (int32)0xFFFFFFFF;

   // check if the register arrays have enough space to store the new method
   if (((context->regI  + method->iCount)   >= context->regIEnd      && !contextIncreaseRegI(context, &regI))   ||
       ((context->regO  + method->oCount)   >= context->regOEnd      && !contextIncreaseRegO(context, &regO))   ||
       ((context->reg64 + method->v64Count) >= context->reg64End     && !contextIncreaseReg64(context, &reg64)) ||
       ((context->callStack+2)              >= context->callStackEnd && !contextIncreaseCallStack(context)))
   {
      exceptionMsg = "On context's stack expansion 1";
      goto throwOutOfMemoryError;
   }

#ifndef ENABLE_TEST_SUITE
   *regO = null;
   if (method->oCount > 1) for (regO2 = regO+method->oCount; regO2 > regO;) *--regO2 = 0; // erase the region that will be used for objects so the gc don't confuse an object previously used with the objects in our method's frame. All methods have at least one regO
#endif
   // protect the registers used by this method
   context->regI  += method->iCount;
   context->regO  += method->oCount;
   context->reg64 += method->v64Count;
   context->callStack[0] = method;
   context->callStack += 2;
#ifdef ENABLE_TEST_SUITE
   if (context->callStackForced == null)
#endif
      callStackMethodEnd = (int32)(context->callStack - context->callStackStart); // method will end when callStackMethodEnd is reached. guich@tc310: changed the pointer to a difference because when the call stack reaches the limit, it will not change all pointers since this method is called recursively

   // push the parameters
#ifdef ENABLE_TRACE
   TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s initial - calling %s.%s", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), regO, context->regO, context->callStack-2, getSpaces(context, context->depth), method->class_->name, method->name);
   context->depth++;
#endif
   if (method->paramCount > 0 || !method->flags.isStatic)
   {
      TCObject *rO = regO;
      va_list vaargs;
      va_start(vaargs, method);

      if (!method->flags.isStatic && (*rO++ = va_arg(vaargs, TCObject)) == null) // push "this", which must be the first parameter passed to executeMethod
      {
         tcvmCreateException(context, NullPointerException, -1, 0, "Instance is null when calling method %s", method->name); // cannot use "goto throwNullPointerException" here, because we can't replace "method" by newMethod
         goto handleException;
      }

      /*if (method->flags.isSynchronized)
         lockMutex(method->flags.isStatic? (size_t)method->class_ : (size_t)regO[0]);
      */
      if (method->paramCount > 0)
      {
         int32  *rI = regI;
         TValue64 *r64 = reg64;
         int32 n = method->paramCount;
         UInt8Array regTypes = method->paramRegs;
         if (context->parametersInArray)
         {
            TValue* aargs = va_arg(vaargs, TValue*);
            context->parametersInArray = false;
            for (; n-- > 0; regTypes++, aargs++)
            {
               XSELECT(addrInParamA, *regTypes)
               {
                  XOPTION(e,RegI): *rI++ = aargs->asInt32;         continue;
                  XOPTION(e,RegO): *rO++ = aargs->asObj;           continue;
                  XOPTION(e,RegD):
                  XOPTION(e,RegL): *REGD(r64++) = aargs->asDouble; continue;
               }
            }
         }
         else
         {
            for (; n-- > 0; regTypes++)
            {
               XSELECT(addrInParam, *regTypes)
               {
                  XOPTION(d,RegI): *rI++ = va_arg(vaargs, int32);         continue;
                  XOPTION(d,RegO): *rO++ = va_arg(vaargs, TCObject);        continue;
                  XOPTION(d,RegD):
                  XOPTION(d,RegL): *REGD(r64++) = va_arg(vaargs, double); continue;
               }
            }
         }
      }
      va_end(vaargs);
   }

   if (context->thrownException != null)
      goto handleException;
   if (method->flags.isNative)
   {
      newMethod = method;
      directNativeCall = true;
      goto nativeMethodCall;
   }

#ifndef DIRECT_JUMP // use a direct jump if supported
mainLoop:
#endif
#ifdef TRACK_USED_OPCODES
   usedOpcodes[(code+1)->op.op] = 1;
#endif
   FIRST_OP
   {
      OPCODE(BREAK)
         #ifdef ENABLE_TEST_SUITE
         goto finishMethod;
         #else
         NEXT_OP  // NOP instruction
         #endif
      OPCODE(MOV_regI_regI)       regI[code->reg_reg.reg0] = regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(MOV_regI_arc)        ARRAYCHECK(code->reg) // all array checks must fall throught!
      OPCODE(MOV_regI_aru)        regI[code->reg_ar.reg]  = ((int32*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]]; NEXT_OP
      OPCODE(MOV_regI_sym)        regI[code->reg_sym.reg] = cp->i32[code->reg_sym.sym]; NEXT_OP
      OPCODE(MOV_regI_s18)        regI[code->s18_reg.reg] = (int32)code->s18_reg.s18; NEXT_OP
      OPCODE(MOV_regI_arlen)      if (regO[code->reg_ar.base] == null) {exceptionMsg = "On array's length object"; goto throwNullPointerException;} regI[code->reg_ar.reg]  = ARRAYOBJ_LEN(regO[code->reg_ar.base]); NEXT_OP
      OPCODE(MOV_regO_regO)       regO[code->reg_reg.reg0] = regO[code->reg_reg.reg1]; NEXT_OP
      OPCODE(MOV_regO_null)       regO[code->reg.reg] = 0; NEXT_OP
      OPCODE(MOV_regO_arc)        ARRAYCHECK(code->reg)
      OPCODE(MOV_regO_aru)        regO[code->reg_ar.reg]  = ((TCObject*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]]; NEXT_OP
      OPCODE(MOV_regO_sym)        regO[code->reg_sym.reg] = cp->str[code->reg_sym.sym]; NEXT_OP
      OPCODE(MOV_reg64_reg64)     reg64[code->reg_reg.reg0] = reg64[code->reg_reg.reg1]; NEXT_OP
      OPCODE(MOV_reg64_arc)       ARRAYCHECK(code->reg)
      OPCODE(MOV_reg64_aru)       REGL(reg64)[code->reg_ar.reg]  = ((int64*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]]; NEXT_OP
      OPCODE(MOV_regD_sym)        REGD(reg64)[code->reg_sym.reg] = cp->dbl[code->reg_sym.sym]; NEXT_OP
      OPCODE(MOV_regL_sym)        REGL(reg64)[code->reg_sym.reg] = cp->i64[code->reg_sym.sym]; NEXT_OP
      OPCODE(MOV_regD_s18)        REGD(reg64)[code->s18_reg.reg] = (int32)code->s18_reg.s18; NEXT_OP
      OPCODE(MOV_regL_s18)        REGL(reg64)[code->s18_reg.reg] = (int64)code->s18_reg.s18; NEXT_OP
      OPCODE(MOV_arc_regI)        ARRAYCHECK(code->reg)
      OPCODE(MOV_aru_regI)        ((int32 *)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]] = regI[code->reg_ar.reg]; NEXT_OP
      OPCODE(MOV_arc_regO)        ARRAYCHECK(code->reg)
      OPCODE(MOV_aru_regO)        ((TCObject*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]] = regO[code->reg_ar.reg]; NEXT_OP
      OPCODE(MOV_arc_reg64)       ARRAYCHECK(code->reg)
      OPCODE(MOV_aru_reg64)       ((Value64)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]] = reg64[code->reg_ar.reg]; NEXT_OP
      OPCODE(MOV_arc_regIb)       ARRAYCHECK(code->reg)
      OPCODE(MOV_aru_regIb)       ((int8  *)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]] = (int8)regI[code->reg_ar.reg]; NEXT_OP
      OPCODE(MOV_arc_reg16)       ARRAYCHECK(code->reg)
      OPCODE(MOV_aru_reg16)       ((uint16*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]] = (uint16)regI[code->reg_ar.reg]; NEXT_OP
      OPCODE(MOV_regIb_arc)       ARRAYCHECK(code->reg)
      OPCODE(MOV_regIb_aru)       regI[code->reg_ar.reg] = ((int8  *)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]]; NEXT_OP
      OPCODE(MOV_reg16_arc)       ARRAYCHECK(code->reg)
      OPCODE(MOV_reg16_aru)       regI[code->reg_ar.reg] = ((uint16*)ARRAYOBJ_START(regO[code->reg_ar.base]))[regI[code->reg_ar.idx]]; NEXT_OP
      OPCODE(MOV_field_regI)      GET_INSTANCE_FIELD(RegI) FIELD_I32(o,               retv) = regI[code->field_reg.reg]; NEXT_OP
      OPCODE(MOV_field_regO)      GET_INSTANCE_FIELD(RegO) FIELD_OBJ(o, OBJ_CLASS(o), retv) = regO[code->field_reg.reg]; NEXT_OP
      OPCODE(MOV_field_reg64)     GET_INSTANCE_FIELD(RegD) FIELD_DBL(o, OBJ_CLASS(o), retv) = REGD(reg64)[code->field_reg.reg];NEXT_OP
      OPCODE(MOV_regI_field)      GET_INSTANCE_FIELD(RegI) regI[code->field_reg.reg] = FIELD_I32(o,               retv); NEXT_OP
      OPCODE(MOV_regO_field)      GET_INSTANCE_FIELD(RegO) regO[code->field_reg.reg] = FIELD_OBJ(o, OBJ_CLASS(o), retv); NEXT_OP
      OPCODE(MOV_reg64_field)     GET_INSTANCE_FIELD(RegD) REGD(reg64)[code->field_reg.reg] = FIELD_DBL(o, OBJ_CLASS(o), retv); NEXT_OP
      OPCODE(MOV_static_regI)     GET_STATIC_FIELD(RegI) ((int32*) sf)[0] = regI[code->static_reg.reg]; NEXT_OP
      OPCODE(MOV_static_regO)     GET_STATIC_FIELD(RegO) ((TCObject*)sf)[0] = regO[code->static_reg.reg]; NEXT_OP
      OPCODE(MOV_static_reg64)    GET_STATIC_FIELD(RegD) ((double*)sf)[0] = REGD(reg64)[code->static_reg.reg]; NEXT_OP
      OPCODE(MOV_regI_static)     GET_STATIC_FIELD(RegI) regI[code->static_reg.reg] = ((int32*) sf)[0]; NEXT_OP
      OPCODE(MOV_regO_static)     GET_STATIC_FIELD(RegO) regO[code->static_reg.reg] = ((TCObject*)sf)[0]; NEXT_OP
      OPCODE(MOV_reg64_static)    GET_STATIC_FIELD(RegD) REGD(reg64)[code->static_reg.reg] = ((double*)sf)[0]; NEXT_OP
      OPCODE(ADD_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] + regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(ADD_regI_s12_regI)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] + (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(ADD_regD_regD_regD)  REGD(reg64)[code->reg_reg_reg.reg0] = REGD(reg64)[code->reg_reg_reg.reg1] + REGD(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(ADD_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] + REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(ADD_regI_arc_s6)     ARRAYCHECK(code->reg_s6)
      OPCODE(ADD_regI_aru_s6)     regI[code->reg_s6_ar.reg] = ((int32*)ARRAYOBJ_START(regO[code->reg_s6_ar.base]))[regI[code->reg_s6_ar.idx]] + (int32)code->reg_s6_ar.s6; NEXT_OP
      OPCODE(ADD_regI_regI_sym)   regI[code->reg_reg_sym.reg0] = regI[code->reg_reg_sym.reg1] + cp->i32[code->reg_reg_sym.sym]; NEXT_OP
      OPCODE(ADD_aru_regI_s6)     ((int32*)ARRAYOBJ_START(regO[code->reg_s6_ar.base]))[regI[code->reg_s6_ar.idx]] = regI[code->reg_s6_ar.reg] + (int32)code->reg_s6_ar.s6; NEXT_OP
      OPCODE(SUB_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] - regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(SUB_regD_regD_regD)  REGD(reg64)[code->reg_reg_reg.reg0] = REGD(reg64)[code->reg_reg_reg.reg1] - REGD(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(SUB_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] - REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(SUB_regI_s12_regI)   regI[code->reg_reg_s12.reg0] = (int32)code->reg_reg_s12.s12 - regI[code->reg_reg_s12.reg1]; NEXT_OP
      OPCODE(MUL_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] * regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(MUL_regD_regD_regD)  REGD(reg64)[code->reg_reg_reg.reg0] = REGD(reg64)[code->reg_reg_reg.reg1] * REGD(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(MUL_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] * REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(MUL_regI_regI_s12)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] * (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(DIV_regI_regI_regI)  if (regI[code->reg_reg_reg.reg2] != 0) regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] / regI[code->reg_reg_reg.reg2]; else goto throwArithmeticException; NEXT_OP
      OPCODE(DIV_regD_regD_regD)  if (REGD(reg64)[code->reg_reg_reg.reg2] != 0) REGD(reg64)[code->reg_reg_reg.reg0] = REGD(reg64)[code->reg_reg_reg.reg1] / REGD(reg64)[code->reg_reg_reg.reg2]; else goto throwArithmeticException; NEXT_OP
      OPCODE(DIV_regL_regL_regL)  if (REGL(reg64)[code->reg_reg_reg.reg2] != 0) REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] / REGL(reg64)[code->reg_reg_reg.reg2]; else goto throwArithmeticException; NEXT_OP
      OPCODE(DIV_regI_regI_s12)   if (code->reg_reg_s12.s12        != 0) regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] / (int32)code->reg_reg_s12.s12; else goto throwArithmeticException; NEXT_OP
      OPCODE(MOD_regI_regI_regI)  if (regI[code->reg_reg_reg.reg2] != 0) regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] % regI[code->reg_reg_reg.reg2]; else goto throwArithmeticException; NEXT_OP
      OPCODE(MOD_regD_regD_regD)  if (REGD(reg64)[code->reg_reg_reg.reg2] != 0) REGD(reg64)[code->reg_reg_reg.reg0] = dmod(REGD(reg64)[code->reg_reg_reg.reg1], REGD(reg64)[code->reg_reg_reg.reg2]); else goto throwArithmeticException; NEXT_OP
      OPCODE(MOD_regL_regL_regL)  if (REGL(reg64)[code->reg_reg_reg.reg2] != 0) REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] % REGL(reg64)[code->reg_reg_reg.reg2]; else goto throwArithmeticException; NEXT_OP
      OPCODE(MOD_regI_regI_s12)   if (code->reg_reg_s12.s12        != 0) regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] % (int32)code->reg_reg_s12.s12; else goto throwArithmeticException; NEXT_OP
      OPCODE(SHL_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] << (regI[code->reg_reg_reg.reg2] & 0x1F); NEXT_OP
      OPCODE(SHL_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] << (REGL(reg64)[code->reg_reg_reg.reg2] & 0x3F); NEXT_OP
      OPCODE(SHL_regI_regI_s12)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] << (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(SHR_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] >> (regI[code->reg_reg_reg.reg2] & 0x1F); NEXT_OP
      OPCODE(SHR_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] >> (REGL(reg64)[code->reg_reg_reg.reg2] & 0x3F); NEXT_OP
      OPCODE(SHR_regI_regI_s12)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] >> (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(USHR_regI_regI_regI) regI[code->reg_reg_reg.reg0] = (uint32)regI[code->reg_reg_reg.reg1] >> (regI[code->reg_reg_reg.reg2] & 0x1F); NEXT_OP
      OPCODE(USHR_regL_regL_regL) REGL(reg64)[code->reg_reg_reg.reg0] = (uint64)REGL(reg64)[code->reg_reg_reg.reg1] >> (REGL(reg64)[code->reg_reg_reg.reg2] & 0x3F); NEXT_OP
      OPCODE(USHR_regI_regI_s12)  regI[code->reg_reg_s12.reg0] = (uint32)regI[code->reg_reg_s12.reg1] >> (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(AND_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] & regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(AND_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] & REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(AND_regI_regI_s12)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] & (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(AND_regI_aru_s6)     regI[code->reg_s6_ar.reg] = ((int32*)ARRAYOBJ_START(regO[code->reg_s6_ar.base]))[regI[code->reg_s6_ar.idx]] & (int32)code->reg_s6_ar.s6; NEXT_OP
      OPCODE(OR_regI_regI_regI)   regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] | regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(OR_regL_regL_regL)   REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] | REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(OR_regI_regI_s12)    regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] | (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(XOR_regI_regI_regI)  regI[code->reg_reg_reg.reg0] = regI[code->reg_reg_reg.reg1] ^ regI[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(XOR_regL_regL_regL)  REGL(reg64)[code->reg_reg_reg.reg0] = REGL(reg64)[code->reg_reg_reg.reg1] ^ REGL(reg64)[code->reg_reg_reg.reg2]; NEXT_OP
      OPCODE(XOR_regI_regI_s12)   regI[code->reg_reg_s12.reg0] = regI[code->reg_reg_s12.reg1] ^ (int32)code->reg_reg_s12.s12; NEXT_OP
      OPCODE(JEQ_regO_regO)       if (regO[code->reg_reg_s12.reg0]        == regO[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regO_null)       if (regO[code->reg_reg_s12.reg0]        == 0)                                                 {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regI_regI)       if (regI[code->reg_reg_s12.reg0]        == regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] == REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] == REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regI_s6)         if (regI[code->reg_s6_desloc.reg]       == (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JEQ_regI_sym)        if (regI[code->reg_sym_sdesloc.reg]     == cp->i32[code->reg_sym_sdesloc.sym])                {code += (int32)code->reg_sym_sdesloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regO_regO)       if (regO[code->reg_reg_s12.reg0]        != regO[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regO_null)       if (regO[code->reg_reg_s12.reg0]        != 0)                                                 {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regI_regI)       if (regI[code->reg_reg_s12.reg0]        != regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] != REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] != REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regI_s6)         if (regI[code->reg_s6_desloc.reg]       != (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JNE_regI_sym)        if (regI[code->reg_sym_sdesloc.reg]     != cp->i32[code->reg_sym_sdesloc.sym])                {code += (int32)code->reg_sym_sdesloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JLT_regI_regI)       if (regI[code->reg_reg_s12.reg0]        <  regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLT_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] <  REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLT_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] <  REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLT_regI_s6)         if (regI[code->reg_s6_desloc.reg]       <  (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JLE_regI_regI)       if (regI[code->reg_reg_s12.reg0]        <= regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLE_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] <= REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLE_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] <= REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JLE_regI_s6)         if (regI[code->reg_s6_desloc.reg]       <= (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JGT_regI_regI)       if (regI[code->reg_reg_s12.reg0]        >  regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGT_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] >  REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGT_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] >  REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGT_regI_s6)         if (regI[code->reg_s6_desloc.reg]       >  (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JGE_regI_regI)       if (regI[code->reg_reg_s12.reg0]        >= regI[code->reg_reg_s12.reg1])                      {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGE_regL_regL)       if (REGL(reg64)[code->reg_reg_s12.reg0] >= REGL(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGE_regD_regD)       if (REGD(reg64)[code->reg_reg_s12.reg0] >= REGD(reg64)[code->reg_reg_s12.reg1])               {code += (int32)code->reg_reg_s12.s12; NEXT_OP0} NEXT_OP
      OPCODE(JGE_regI_s6)         if (regI[code->reg_s6_desloc.reg]       >= (int32)code->reg_s6_desloc.s6)                     {code += (int32)code->reg_s6_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(JGE_regI_arlen)      if (regI[code->reg_arl_s12.regI]        >= (int32)ARRAYOBJ_LEN(regO[code->reg_arl_s12.base])) {code += (int32)code->reg_arl_s12.desloc; NEXT_OP0} NEXT_OP
      OPCODE(DECJGTZ_regI)        if (--regI[code->reg_desloc.reg]        >  0)                                                 {code += (int32)code->reg_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(DECJGEZ_regI)        if (--regI[code->reg_desloc.reg]        >= 0)                                                 {code += (int32)code->reg_desloc.desloc; NEXT_OP0} NEXT_OP
      OPCODE(CALL_normal) // 33% of the calls
         if ((newMethod = cp->boundNormal[code->mtd.sym]) != null) // note: there's no need to check here if this class is an interface because interface methods are never bound
            goto contCall;
         else
            goto notYetLinked;
      OPCODE(CALL_virtual) // 66% of the calls
         if (regO[code->mtd.this_] == null)
         {     
            exceptionMsg = "On 'this' when calling a method";
            goto throwNullPointerException;
         }
         thisClass = OBJ_CLASS(regO[code->mtd.this_]);
		 if (thisClass == null)
		 {
			 exceptionMsg = "Obj class is null";
          debug("NULL CLASS OBJECT: %X", regO[code->mtd.this_]);
			 goto throwNullPointerException;
		 }
         if ((macArray = cp->boundVirtualMethod[code->mtd.sym]) != null && // was the method ever linked?
            (mac = macArray[thisClass->hash&15]) != null &&                // map to the hash position
            mac->c == thisClass) // 90% of the cases
         {
            newMethod = mac->m;
contCall:
            if (xstrcmp(newMethod->name, "fibR") == 0) {
               int a = 0;
               a++;
            }
            regI  = context->regI; // same of regI += method->iCount
            regO  = context->regO;
            reg64 = context->reg64;
            // "protect" the registers that the current method is using - do not move from here!
            context->regI  += newMethod->iCount;
            context->regO  += newMethod->oCount;
            context->reg64 += newMethod->v64Count;
            context->callStack += 2;
            // check if the register arrays have enough space for the new method ones
            if ((context->regI      >= context->regIEnd      && !contextIncreaseRegI(context, &regI))   ||
                (context->regO      >= context->regOEnd      && !contextIncreaseRegO(context, &regO))   ||
                (context->reg64     >= context->reg64End     && !contextIncreaseReg64(context, &reg64)) ||
                (context->callStack >= context->callStackEnd && !contextIncreaseCallStack(context)))
            {
               context->regI  -= newMethod->iCount;
               context->regO  -= newMethod->oCount;
               context->reg64 -= newMethod->v64Count;
               context->callStack -= 2;
               exceptionMsg = "On context's stack expansion 2";
               goto throwOutOfMemoryError;
            }

            *regO = null;
            if (newMethod->oCount > 1) for (regO2 = regO+newMethod->oCount; regO2 > regO;) *--regO2 = 0; // erase the region that will be used for objects so the gc don't confuse an object previously used with the objects in our method's frame. All methods have at least one regO
            // context registers point to right after the current method ones. so we set the current registers to that position, and increment the context's ones to rely after the new method's
            context->callStack[-3] = code; // push returning address of previous method's - note: do NOT increment with the paramSkip value because we use the current code in the RETURN_xxx instructions.
            context->callStack[-2] = newMethod;

#ifdef ENABLE_TRACE
            TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s calling %s.%s - %s (%X)", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), (int)regO, context->regO, context->callStack-2, getSpaces(context,context->depth), newMethod->class_->name, newMethod->name, regO[(int32)code->mtd.this_ - (int32)method->oCount] == null ? "" : OBJ_CLASS(regO[(int32)code->mtd.this_ - (int32)method->oCount])->name, (int)(regO[(int32)code->mtd.this_ - (int32)method->oCount]));
            context->depth++;
#else
            if (traceOn)
               debug("T %08d %X %s - %s",getTimeStamp(), thread, newMethod->class_->name, newMethod->name);
#endif
            if (!newMethod->flags.isStatic)
            {
               regO[0] = regO[(int32)code->mtd.this_ - (int32)method->oCount]; // put "this" in regO[0]
               if (regO[0] == null) // note: this is an exception, not the rule. so it doesn't matter if it is an inneficient code
               {
#ifdef ENABLE_TRACE
                  TRACE("T %08d %X %X %05d - M throwing NPE.", getTimeStamp(), thread, context, ++context->ccon);
                  context->depth--;
#endif
                  tcvmCreateException(context, NullPointerException, (int32)(code-method->code), -2, null, "Instance is null when calling method %s", newMethod->name); // cannot use "goto throwNullPointerException" here, because we can't replace "method" by newMethod
                  goto popStackFrame; // stack frame must be pulled before handling the exception
               }
            }

            /*if (newMethod->flags.isSynchronized)
               lockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[0]);*/           
            // retrieve the instance reference, the return register and the parameters
            nparam = newMethod->paramCount;
            if (nparam == 0) // is anything being passed along?
               goto noMoreParams;
            // no else here!
            {
               int32  *ri = regI;
               TValue64 *r64 = reg64;
               uint8 *regs = newMethod->paramRegs, *params=0;
               TCObject *ro = regO + (newMethod->flags.isStatic == 0);
               // if nothing is returned, then the first parameter is in the method's instruction
               if (newMethod->cpReturn == 0)
               {
#ifdef DIRECT_JUMP // note: these two are splitted because the default below improves performance on compilers that has no DIRECT_JUMP.
                  XSELECT(addrMtdbParam, *regs++)
                  {
                     XOPTION(b,RegI): *ri++ = regI[(int32)code->mtd.retOr1stParam - (int32)method->iCount]; goto cont;
                     XOPTION(b,RegO): *ro++ = regO[(int32)code->mtd.retOr1stParam - (int32)method->oCount]; goto cont;
                     XOPTION(b,RegD):
                     XOPTION(b,RegL): *r64++= reg64[(int32)code->mtd.retOr1stParam - (int32)method->v64Count]; goto cont;
                  }
#else
                  switch (*regs++)
                  {
                     case RegI: *ri++ = regI [(int32)code->mtd.retOr1stParam - (int32)method->iCount];  goto cont;
                     case RegO: *ro++ = regO [(int32)code->mtd.retOr1stParam - (int32)method->oCount];  goto cont;
                     default:   *r64++= reg64[(int32)code->mtd.retOr1stParam - (int32)method->v64Count];
                  }
#endif
cont:
                  if (--nparam == 0) // if no more parameters, continue. putting this here avoids another "if (nparam > 0)" after this block ends.
                     goto noMoreParams;
               }
            if (xstrcmp(newMethod->name, "fibR") == 0) {
               int a = 0;
               a++;
            }
               // The method's parameters are passed in the registers, in sequence.
               params = (uint8*)(code+1);
               for (; nparam-- > 0; params++, regs++)
               {
                  if (*params <= 63)
                  {
#ifdef DIRECT_JUMP // idem, read above
                     XSELECT(addrMtdParam, *regs)
                     {
                        XOPTION(a,RegI): *ri++ = regI[(int32)*params - (int32)method->iCount]; continue; // the register is set from 0-63. from 64 to 255, it is a number. So, -1 is stored as 64, 0 as 65, 1 as 66, and so on, up to 190 as 255
                        XOPTION(a,RegO): *ro++ = regO[(int32)*params - (int32)method->oCount]; continue;
                        XOPTION(a,RegD):
                        XOPTION(a,RegL): *r64++ = reg64[(int32)*params - (int32)method->v64Count]; continue;
                     }
#else
                     switch (*regs)
                     {
                        case RegI: *ri++  = regI [(int32)*params - (int32)method->iCount];  continue; // the register is set from 0-63. from 64 to 255, it is a number. So, -1 is stored as 64, 0 as 65, 1 as 66, and so on, up to 190 as 255
                        case RegO: *ro++  = regO [(int32)*params - (int32)method->oCount];  continue;
                        default  : *r64++ = reg64[(int32)*params - (int32)method->v64Count]; continue;
                     }
#endif
                  }
                  else
                  {
                     XSELECT(addrMtdcParam, *regs)
                     {
                        XOPTION(c,RegI): *ri++        = (int32)*params-65; continue; // the register is set from 0-63. from 64 to 255, it is a number. So, -1 is stored as 64, 0 as 65, 1 as 66, and so on, up to 190 as 255
                        XOPTION(c,RegO): *ro++        = null;              continue;
                        XOPTION(c,RegD): *REGD(r64++) = (int32)*params-65; continue; // regD and regO can't do a direct assignment because there's a convertion from int to double/long here.
                        XOPTION(c,RegL): *REGL(r64)   = (int32)*params-65; r64++; continue; // so each one must be assigned in its original type - the split of r64++ removes a gcc warning
                     }
                  }
               }
            }
noMoreParams:

                  // OPCODE(RETURN_reg64) context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {reg64      [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = reg64[code->reg.reg];      goto resumePreviousMethod;} else {returnedValue.asInt64  = reg64[code->reg.reg];     goto finishMethod;}

            if (!newMethod->flags.isNative)
            {
               // replace current variables by the new ones
               class_ = newMethod->class_; // replace current's class
               cp = class_->cp;
               method = newMethod;
               code = method->code;

               if (xstrcmp(newMethod->name, "fibR") == 0) {
                  int a = 0;
                  a++;

                  // if argument in hash, set reg64 & XSELECT

                  switch (reg64[-(int32)method->v64Count]) {
                     case 0:reg64[code->reg.reg] = 1; XSELECT(address,RETURN_reg64); 
                     case 1:reg64[code->reg.reg] = 1; XSELECT(address,RETURN_reg64); 
                     case 2:reg64[code->reg.reg] = 2; XSELECT(address,RETURN_reg64); 
                     case 3:reg64[code->reg.reg] = 3; XSELECT(address,RETURN_reg64); 
                     case 4:reg64[code->reg.reg] = 5; XSELECT(address,RETURN_reg64); 
                     case 5:reg64[code->reg.reg] = 8; XSELECT(address,RETURN_reg64); 
                     case 6:reg64[code->reg.reg] = 13; XSELECT(address,RETURN_reg64); 
                     case 7:reg64[code->reg.reg] = 21; XSELECT(address,RETURN_reg64); 
                     case 8:reg64[code->reg.reg] = 34; XSELECT(address,RETURN_reg64); 
                  }
               }
               NEXT_OP0
            }
            // no else here!
            {
nativeMethodCall:
               if (newMethod->boundNM == null &&  // not yet bound?
                  (newMethod->boundNM = (NativeMethod)getProcAddress(null, newMethod->nativeSignature)) == null && // Check first in the tcvm dll
                  (newMethod->boundNM = (NativeMethod)findProcAddress(newMethod->nativeSignature, &newMethod->ref)) == null)
               {
                  tcvmCreateException(context, NoSuchMethodError, (int32)(code-method->code), 0, "Native method %s %s (%s) with %d parameters", newMethod->class_->name, newMethod->name, newMethod->nativeSignature, ARRAYLENV(newMethod->cpParams));
                  goto handleException;
               }
               nmp->i32 = regI;
               nmp->obj = regO;
               nmp->i64 = reg64;
               nmp->retO = null;
               newMethod->boundNM(nmp); // call the method
popStackFrame:
               // There's no "return" instruction for native methods, so we must pop the frame here
               context->regI  -= newMethod->iCount;
               context->regO  -= newMethod->oCount;
               context->reg64 -= newMethod->v64Count;
               if (!directNativeCall)
               {
                  regI  = context->regI  - method->iCount;
                  regO  = context->regO  - method->oCount;
                  reg64 = context->reg64 - method->v64Count;
               }
               else
               {
                  directNativeCall = false;
                  regI  = context->regI;
                  regO  = context->regO;
                  reg64 = context->reg64;
               }
               context->callStack -= 2;
               newMethod = (Method)context->callStack[0];
               /*if (newMethod->flags.isSynchronized)
                  unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]);
               */
               if (context->thrownException != null) // if an exception was thrown by the native method, handle it
               {
                  nmp->retO = null;
                  goto handleException;
               }

#ifdef ENABLE_TRACE
               context->depth--;
               TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s native method called and returned to %s.%s - %s (%X)", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), (int)regO, context->regO, context->callStack-2, getSpaces(context,context->depth), method->class_->name, method->name, regO[0] == null ? "" : OBJ_CLASS(regO[0])->name, regO[0]);
#endif
               // check the returned value
               if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd)
               {
                  if (newMethod->cpReturn) // not returning void?
                  {
                     XSELECT(addrNMRet, newMethod->returnReg)
                     {
                        XOPTION(nm,RegI): regI [code->mtd.retOr1stParam] = nmp->retI; code += newMethod->paramSkip; NEXT_OP;
                        XOPTION(nm,RegO): regO [code->mtd.retOr1stParam] = nmp->retO;
                        if (((long)nmp->retO & 1) == 1)
                           nmp->retO = 0;
                        code += newMethod->paramSkip; nmp->retO = null; NEXT_OP;
                        XOPTION(nm,RegD):
                        XOPTION(nm,RegL): reg64[code->mtd.retOr1stParam] = nmp->retL; code += newMethod->paramSkip; NEXT_OP;
                     }
                  }
                  code += newMethod->paramSkip; NEXT_OP;
               }
               else
               {
                  if (newMethod->cpReturn != 0)  // must be a switch: asObj and asDouble are an union, while nmp->retO and retD are not
                  {
                     if (newMethod->returnReg == RegO)
                        returnedValue.asObj = nmp->retO;
                     else
                        returnedValue.asDouble = nmp->retD;
                     nmp->retO = null;
                  }
                  goto finishMethod;
               }
            }
         }
         else
         {
            if (macArray == null) // have to create the array?
            {
               IF_HEAP_ERROR(cp->heap)
               {                            
                  exceptionMsg = "When expanding MAC array";
                  goto throwOutOfMemoryError;
               }
               macArray = cp->boundVirtualMethod[code->mtd.sym] = (MethodAndClass*)heapAlloc(cp->heap, TSIZE*16);
               boundHead = &macArray[thisClass->hash&15];
               goto notYetLinked;
            }
            LOCKVAR(metAndCls);
            boundHead = &macArray[thisClass->hash&15];
            for (last=mac, mac = mac ? mac->next : null; mac != null; last = mac, mac = mac->next)
               if (mac->c == thisClass)
               {
                  // move to front. increases first hit by 50%
                  if (last)
                     last->next = mac->next;
                  mac->next = *boundHead;
                  *boundHead = mac;
                  // get the method's address and execute it.
                  newMethod = mac->m;
                  UNLOCKVAR(metAndCls);
                  goto contCall;
               }
            UNLOCKVAR(metAndCls);
notYetLinked:
            originalClassIsInterface = false;
            sym = cp->mtd[ code->mtd.sym ]; // virtual methods are directly referenced: mtd.sym is the index to an array that will point to the mtd table
            len = cp->mtdLens[code->mtd.sym];
            className = cp->cls[sym[0]];
            hashName = cp->hashNames[code->mtd.sym];
            hashParams = cp->hashParams[code->mtd.sym];
            methodName = cp->mtdfld[sym[1]];
            printf("m2: %s, %s\n", className, methodName);
            if (xstrcmp(methodName, "fibR") == 0) {
               int a = 10;
               a++;
            }
            if (code->op.op == CALL_virtual)
            {
               c = OBJ_CLASS(regO[code->mtd.this_]); // search for the method starting on the class pointed by "this"
               if (c->flags.isString && strEq(className, "java.lang.Class"))
               {
                  TNMParams params;

                  tzero(params);
                  params.currentContext = context;
                  params.obj = &regO[code->mtd.this_];
                  jlC_forName_s(&params);
                  regO[code->mtd.this_] = params.retO;
                  c = params.retO ? OBJ_CLASS(params.retO) : null;
                  if (c == null)
                     goto throwClassNotFoundException;
               }
            }
            else
            if (className == class_->name || strEq(className, class_->name)) // calling a method inside this class? (first comparison is always true, but keep 2nd for safety)
               c = class_;
            else
            {
               c = loadClass(context, className, false);
               if (context->thrownException != null)
                  goto handleException;
               if (c == null)
                  goto throwClassNotFoundException;
               if (c->flags.isInterface) // if we're calling an interface method, use the current object's class instead
               {
                  originalClassIsInterface = true;
                  if (regO[code->mtd.this_] == null)
                  {
                     exceptionMsg = "Calling an interface's method";
                     goto throwNullPointerException;
                  }
                  c = OBJ_CLASS(regO[code->mtd.this_]);
               }
            }
            do
            {
               for (newMethod = c->methods, i = ARRAYLENV(c->methods); i-- > 0; newMethod++)
                  if (newMethod->hashName == hashName && newMethod->hashParams == hashParams && strEq(newMethod->name, methodName) && paramsEq(cp, sym, len, c->cp, newMethod->cpParams)) // guich@tc110_21: after the hashcode match, we must ensure that the names also match.
                  {
                     if (!originalClassIsInterface && !newMethod->flags.isAbstract) // if the class is not an interface nor the method abstract, bind it
                     {
                        if (code->op.op == CALL_virtual)
                        {
                           MethodAndClass newMac = newXH(MethodAndClass, cp->heap);
                           newMac->m = newMethod;
                           newMac->c = OBJ_CLASS(regO[code->mtd.this_]);
                           newMac->next = *boundHead;
                           *boundHead = newMac;
                        }
                        else
                           cp->boundNormal[code->mtd.sym] = newMethod;
                     }
                     goto contCall;
                  }
               // not found in current class, search in inherited classes, if calling a virtual method
               if (code->op.op != CALL_virtual && code->op.op != CALL_normal && !originalClassIsInterface)
                  break;
               c = c->superClass;
            } while (c);
         }
         goto throwNoSuchMethodError;
      OPCODE(CONV_regI_regL)   regI[code->reg_reg.reg0] =  (int32)REGL(reg64)[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regI_regD)   regI[code->reg_reg.reg0] =  (int32)REGD(reg64)[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regIb_regI)  regI[code->reg_reg.reg0] =  (int8 )regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regIc_regI)  regI[code->reg_reg.reg0] = (uint16)regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regIs_regI)  regI[code->reg_reg.reg0] =  (int16)regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regL_regI)   REGL(reg64)[code->reg_reg.reg0] =  (int64)regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regL_regD)   REGL(reg64)[code->reg_reg.reg0] =  (int64)REGD(reg64)[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regD_regI)   REGD(reg64)[code->reg_reg.reg0] = (double)regI[code->reg_reg.reg1]; NEXT_OP
      OPCODE(CONV_regD_regL)   REGD(reg64)[code->reg_reg.reg0] = (double)REGL(reg64)[code->reg_reg.reg1]; NEXT_OP
      // retReg: taken from the first parameter in the method call,
      //         means: where the caller wants the answer to be placed
      // reg.reg: points where the current routine placed the answer
      // so, the movement is: reg.reg -> retReg
      OPCODE(RETURN_s24I)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regI       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->iCount  ] = (int32 )code->s24.desloc;  goto resumePreviousMethod;} else {returnedValue.asInt32  = (int32 )code->s24.desloc; goto finishMethod;}
      OPCODE(RETURN_null)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regO       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->oCount  ] = null;                      goto resumePreviousMethod;} else {returnedValue.asObj    = null;                     goto finishMethod;}
      OPCODE(RETURN_s24D)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {REGD(reg64)[((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = (double)code->s24.desloc;  goto resumePreviousMethod;} else {returnedValue.asDouble = (double)code->s24.desloc; goto finishMethod;}
      OPCODE(RETURN_s24L)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {REGL(reg64)[((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = (int64 )code->s24.desloc;  goto resumePreviousMethod;} else {returnedValue.asInt64  = (int64 )code->s24.desloc; goto finishMethod;}
      OPCODE(RETURN_symI)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regI       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->iCount  ] = cp->i32[code->sym.sym];    goto resumePreviousMethod;} else {returnedValue.asInt32  = cp->i32[code->sym.sym];   goto finishMethod;}
      OPCODE(RETURN_symO)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regO       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->oCount  ] = cp->str[code->sym.sym];    goto resumePreviousMethod;} else {returnedValue.asObj    = cp->str[code->sym.sym];   goto finishMethod;}
      OPCODE(RETURN_symD)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {REGD(reg64)[((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = cp->dbl[code->sym.sym];    goto resumePreviousMethod;} else {returnedValue.asDouble = cp->dbl[code->sym.sym];   goto finishMethod;}
      OPCODE(RETURN_symL)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {REGL(reg64)[((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = cp->i64[code->sym.sym];    goto resumePreviousMethod;} else {returnedValue.asInt64  = cp->i64[code->sym.sym];   goto finishMethod;}
      OPCODE(RETURN_regI)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regI       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->iCount  ] = regI[code->reg.reg];       goto resumePreviousMethod;} else {returnedValue.asInt32  = regI[code->reg.reg];      goto finishMethod;}
      OPCODE(RETURN_regO)  context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {regO       [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->oCount  ] = regO[code->reg.reg];       goto resumePreviousMethod;} else {returnedValue.asObj    = regO[code->reg.reg];      goto finishMethod;}
      OPCODE(RETURN_reg64) context->callStack -= 2; /*newMethod = (Method)context->callStack[0]; if (newMethod->flags.isSynchronized) unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)regO[-method->oCount]); */ if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd) {reg64      [((Code)context->callStack[-1])->mtd.retOr1stParam - ((Method)context->callStack[-2])->v64Count] = reg64[code->reg.reg];      goto resumePreviousMethod;} else {returnedValue.asInt64  = reg64[code->reg.reg];     goto finishMethod;}
returnVoid:
      OPCODE(RETURN_void)  
         context->callStack -= 2;
         newMethod = (Method)context->callStack[0];
         /*if (newMethod->flags.isSynchronized)
            unlockMutex(newMethod->flags.isStatic? (size_t)newMethod->class_ : (size_t)context->regO[-newMethod->oCount]);
         */
         if (((int32)(context->callStack-context->callStackStart)) >= callStackMethodEnd)
         {
resumePreviousMethod:
            // if (fibR) {
            //    // save argument -> result in hashmap
            // }

            code = ((Code)context->callStack[-1]) + method->paramSkip; // now that retReg was used, its safe to skip over the parameters
            // pop the current method's stack frame
            context->regI  -= method->iCount;
            context->regO  -= method->oCount;
            context->reg64 -= method->v64Count;
            method = (Method)context->callStack[-2];
            regI  = context->regI  - method->iCount;
            regO  = context->regO  - method->oCount;
            reg64 = context->reg64 - method->v64Count;

#ifdef ENABLE_TRACE
            context->depth--;
            TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s returned to %s.%s - %s (%X)", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), (int)regO, context->regO, context->callStack-2, getSpaces(context,context->depth), method->class_->name, method->name, regO[0] == null ? "" : OBJ_CLASS(regO[0])->name, regO[0]);
#endif
            context->callStack[0] = null; // null the context - can be removed after everything is working
            context->callStack[1] = null;
            class_ = method->class_;
            cp = class_->cp;
            if (context->thrownException != null)
               goto handleException;
            NEXT_OP
         }
         goto finishMethod;
      OPCODE(JUMP_s24)  code += (int32)code->s24.desloc;  NEXT_OP0
      OPCODE(JUMP_regI) code = method->code + regI[code->reg.reg]; NEXT_OP0
      OPCODE(SWITCH) // switch (regI)
      {
         // list of key/value pairs:  key (4 bytes) / value (2 bytes - destination address):
         // default address and exit address: 2 bytes each
         // format: default_addr (2 bytes) / pad (2 bytes) / key-array (4*n bytes) / value-array (2*n bytes)
         // total: 2 + n + ceil(n/2) instructions
         // All addresses are relative to the start of the instruction (CODE)
         int32 n = code->switch_reg.n; // IMPORTANT: THE COMPILER MUST REMOVE ANY EMPTY SWITCHes!
         int32 low,high,mid,i;
         int32 key = regI[code->switch_reg.key];
         int32 defaultAddr = code[1].two16.v1;
         int32* keyTable   = (int32*)(code+2);
         uint16* addrTable = (uint16*)(keyTable+n);

         low = 0;
         high = n;
         while (true)
         {
            mid = (high + low) / 2;
            i = keyTable[mid];
            if (key == i)
            {
               int32 inc = addrTable[mid];//(mid&1) ? addrTable[mid].two16.v2 : addrTable[mid].two16.v1;
               code += inc; // found
               break;
            }
            if (mid == low) // not found?
            {
               code += defaultAddr;
               break;
            }
            if (key < i)
               high = mid;
            else
               low = mid;
         }
         NEXT_OP0
      }
      OPCODE(NEWARRAY_len)   if ((regO[code->newarray.regO] = createArrayObject(context, cp->cls[code->newarray.sym], code->newarray.lenOrRegIOrDims)) == null) {exceptionMsg = "When creating array with length"; goto throwOutOfMemoryError;} setObjectLock(regO[code->newarray.regO], UNLOCKED); NEXT_OP
      OPCODE(NEWARRAY_regI)  if ((regO[code->newarray.regO] = createArrayObject(context, cp->cls[code->newarray.sym], regI[code->newarray.lenOrRegIOrDims])) == null) {exceptionMsg = "When creating array with register"; goto throwOutOfMemoryError;} setObjectLock(regO[code->newarray.regO], UNLOCKED); NEXT_OP
      OPCODE(NEWARRAY_multi) if ((regO[code->newarray.regO] = createArrayObjectMulti(context, cp->cls[code->newarray.sym], code->newarray.lenOrRegIOrDims, (uint8*)(code+1), regI)) == null) {exceptionMsg = "When creating multiple arrays"; goto throwOutOfMemoryError;} setObjectLock(regO[code->newarray.regO], UNLOCKED); code += (code->newarray.lenOrRegIOrDims+3)>>2; NEXT_OP
      OPCODE(NEWOBJ)         if ((regO[code->reg_sym.reg]   = createObjectWithoutCallingDefaultConstructor(context, cp->cls[code->reg_sym.sym])) == null) {exceptionMsg = "When creating object"; goto throwOutOfMemoryError;} setObjectLock(regO[code->reg_sym.reg], UNLOCKED); NEXT_OP // do not call default constructor
      OPCODE(THROW)
         context->thrownException = regO[code->reg_reg.reg0];
#ifdef ENABLE_TRACE
         TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s throwing exception %s", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), regO, context->regO, context->callStack-2, getSpaces(context, context->depth), (OBJ_CLASS(context->thrownException))->name);
#endif
         if (*Throwable_trace(context->thrownException) == null) // guich@tc120_21: don't overwrite the trace
            fillStackTrace(context, context->thrownException, (int32)(code-method->code), context->callStack); // guich@tc100b4_4
handleException:
      {
         ExceptionArray ex = method->exceptionHandlers;
         int32 exlen = ARRAYLENV(ex);
         TCClass exc = OBJ_CLASS(context->thrownException);
         for (; exlen-- > 0; ex++)
            if (ex->startPC <= code && code <= ex->endPC)
            {
               if (areClassesCompatible(context, exc, ex->className) == COMPATIBLE && (!appExitThrown || !strEq(exc->name, "totalcross.sys.AppExitException"))) // -1 may be returned;   never catch AppExitException - it must be used to terminate the application
               {
                  code = ex->handlerPC;
                  regO[ex->regO] = context->thrownException;
                  class_ = method->class_;
                  cp = class_->cp;
#ifdef ENABLE_TRACE
                  TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s handling exception %s in %s.%s", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), regO, context->regO, context->callStack-2, getSpaces(context, context->depth), (OBJ_CLASS(context->thrownException))->name, method->class_->name, method->name);
#endif
                  context->thrownException = null;
                  NEXT_OP0;
               }
            }
         // exception unhandled - crawl back in the stack to see if someone else can handle.
#ifdef ENABLE_TRACE
         TRACE("T %08d %X %X %05d - %04d #%4d %X-%X %X %s exception %s was not handled in %s.%s", getTimeStamp(), thread, context, ++context->ccon, (int)(code-method->code), locateLine(method, (int32)(code-method->code)), regO, context->regO, context->callStack-2, getSpaces(context, context->depth), (OBJ_CLASS(context->thrownException))->name, method->class_->name, method->name);
#endif
         goto returnVoid;
      }
      OPCODE(INSTANCEOF)
      OPCODE(CHECKCAST)
      {
         CompatibilityResult result = NOT_COMPATIBLE;
         o = regO[code->instanceof.regO];
         if (o != null) // if the object being compared is not null...
         {
            result = OBJ_CLASS(o)->name == cp->cls[code->instanceof.sym] ? COMPATIBLE : areClassesCompatible(context, OBJ_CLASS(o),cp->cls[code->instanceof.sym]); // quick check - will work almost all the times
            if (result == TARGET_CLASS_NOT_FOUND)
            {
               className = cp->cls[code->instanceof.sym];
               goto throwClassNotFoundException;
            }
         }
         if (code->instanceof.op == INSTANCEOF) // instanceof must set the value in the return register
            regI[code->instanceof.regI] = result;
         else // CHECKCAST
         if (o != null && result == NOT_COMPATIBLE) // checkcast allows the object to be null
         {
            tcvmCreateException(context, ClassCastException, (int32)(code-method->code), 0, "%s is not compatible with %s", OBJ_CLASS(o)->name,cp->cls[code->instanceof.sym]);
            goto handleException;
         }
         NEXT_OP
      }
      OPCODE(TEST_regO) if (regO[code->reg.reg] == null) {exceptionMsg = "Testing object."; goto throwNullPointerException;} NEXT_OP
      OPCODE(INC_regI)  regI[code->inc.reg] += (int32)code->inc.s16; NEXT_OP
      OPCODE(MONITOR_Enter)
      OPCODE(MONITOR_Enter2)
      {
         // get variables and do some checks
         if (code->s24.op == MONITOR_Enter)        
            o = regO[code->reg_reg.reg0];
         else
            o = cp->str[code->reg_reg.reg0];
         if (o == null) {exceptionMsg = "On synchronized object's enter"; goto throwNullPointerException;}
         if (OBJ_CLASS(o) != lockClass) // check for totalcross.util.concurrent.Lock
         {            
            if (!lockMutex((size_t)o))
            {
               exceptionMsg = "When locking mutex";
               goto throwOutOfMemoryError;         
            }     
         }
         else
         {
            TCObject mutex = Lock_mutex(o);
            if (mutex != null) // guich@tc126_62
               RESERVE_MUTEX_VAR(*((MUTEX_TYPE *)ARRAYOBJ_START(mutex))); // now, get access to the mutex
         }
         NEXT_OP
      }
      OPCODE(MONITOR_Exit)
      OPCODE(MONITOR_Exit2) 
      {
         // get variables and do some checks
         if (code->s24.op == MONITOR_Exit)        
            o = regO[code->reg_reg.reg0];
         else
            o = cp->str[code->reg_reg.reg0];
         if (o == null) {exceptionMsg = "On synchronized object's exit"; goto throwNullPointerException;}
         if (OBJ_CLASS(o) != lockClass) // check for totalcross.util.concurrent.Lock
            unlockMutex((size_t)o);
         else
         {   
            TCObject mutex = Lock_mutex(o);
            if (mutex != null) // guich@tc126_62
            {
               MUTEX_TYPE* pmutex = ((MUTEX_TYPE *)ARRAYOBJ_START(mutex));
               if (pmutex)
                  RELEASE_MUTEX_VAR(*pmutex); // now, get access to the mutex
            }
         }
         NEXT_OP
      }
      // end of opcodes
#ifndef DIRECT_JUMP
      default:
//		  alert("Invalid opcode: %d",code->op.op); - this causes an infinite loop when quitting some applications with unhandled exceptions
		  goto finishMethod; // do not remove!
#endif
   }

throwArithmeticException:
   tcvmCreateException(context, ArithmeticException, (int32)(code-method->code), 0, null);
   goto handleException;
throwNullPointerException:
   tcvmCreateException(context, NullPointerException, (int32)(code-method->code), 0, exceptionMsg);
   exceptionMsg = null;
   goto handleException;
throwClassNotFoundException:
   tcvmCreateException(context, ClassNotFoundException, (int32)(code-method->code), 0, className);
   goto handleException;
throwOutOfMemoryError:
   if (context->thrownException == null)
   {
      tcvmCreateException(context, OutOfMemoryError, (int32)(code-method->code), 0, exceptionMsg);
      exceptionMsg = null;
   }
   goto handleException;
throwNoSuchFieldError:
   tcvmCreateException(context, NoSuchFieldError, (int32)(code-method->code), 0, "%s %s. The current VM may not be compatible with this program.", className, fieldName);
   goto handleException;
throwNoSuchMethodError:
{
   // guich@tc123_28: print the full parameter list
   int32 slen = len; // place for the commas
   CharP paramsStr = null;
   for (i = 0; i < len; i++)
      slen += xstrlen(cp->cls[sym[i+2]]);
   slen++;
   if (slen > 0 && (paramsStr = (CharP)xmalloc(slen)) != null)
   {                 
      for (i = 0; i < len; i++)
      {
         xstrcat(paramsStr,cp->cls[sym[i+2]]);
         xstrcat(paramsStr,",");
      }              
      paramsStr[slen-1] = 0;
   }
   tcvmCreateException(context, NoSuchMethodError, (int32)(code-method->code), 0, "%s %s(%s). The current VM may not be compatible with this program OR there may be a bug in the Java compiler; try to upgrade or downgrade your JDK.", className, methodName, slen == 0 ? "" : paramsStr == null ? "..." : paramsStr);
   xfree(paramsStr);
   goto handleException;
}

finishMethod:
   context->regO  = regO; // alredy points to the end of the previous called method
   context->regI  = regI;
   context->reg64 = reg64;
   context->callStack = context->callStackStart + callStackMethodEnd-2; // needed for testcases
   context->code = code;

#ifdef ENABLE_TRACE
   if (traceOn) debug("T 5 %08d %05d            %X-%X %X %s final - %s (%X)", getTimeStamp(), ++context->ccon, (int)regO, context->regO, context->callStack, getSpaces(context,context->depth), regO[0] == null ? "" : OBJ_CLASS(regO[0])->name,regO[0]);
   context->depth--;
   TRACE("T %08d %X %X %05d -             %X-%X %X %s final - %s (%X)", getTimeStamp(), thread, context, ++context->ccon, (int)regO, context->regO, context->callStack, getSpaces(context,context->depth), regO[0] == null ? "" : OBJ_CLASS(regO[0])->name, regO[0]);
#endif
#ifndef ENABLE_TEST_SUITE // some tests assume that an unhandled exception will be thrown
   // save current context
   if (context->thrownException != null && context->callStack == context->callStackStart)
   {
      class_ = OBJ_CLASS(context->thrownException);
      if (context == gcContext)
      {
#ifdef ENABLE_TRACE
         TRACE("T %08d %X %X %05d - Finalize method threw exception %s, which is being silently ignored", getTimeStamp(), thread, context, ++context->ccon, OBJ_CLASS(context->thrownException)->name);
#endif
         context->thrownException = null;
      }
      else
      if (strEq(class_->name, "totalcross.sys.AppExitException"))
         context->thrownException = null;
      else
      if (keepRunning)
      {
         if (context->thread == 0) // main execution? abort the program
            keepRunning = false;
         showUnhandledException(context, context->thread == 0); // show the message using alert if this is the main execution line or debug if its a thread
      }
   }
#endif

   // We are finished with this context, so decrement usage count (and release it, if we are not using it anymore)
   LOCKVAR(context->usageLock);
   if (--context->usageCount == 0)
      context->usageOwner = null;
#ifdef ENABLE_TRACE
   TRACE("T %08d %X %X %05d - Context released; usageCount=%d", getTimeStamp(), thread, context, ++context->ccon, context->usageCount);
#endif
   UNLOCKVAR(context->usageLock);

   return returnedValue;
}

#if defined(WINCE) || defined(WIN32)
#pragma optimize( "ys", on )
#endif

#ifdef ENABLE_TEST_SUITE
#include "tcvm_test.h"
#endif
