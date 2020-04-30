// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

#define ISNAMESTART    (1 << 0)
#define ISNAMEFOLLOWER (1 << 1)
#define ISSPACE        (1 << 2)
#define ISQUOTE        (1 << 3)
#define ISCONTENTDLM   (1 << 4)
#define ISENDTAGDLM    (1 << 5)
#define ISENDREFERENCE (1 << 6)

static char *chrRef[6];
static uint8 is[256];

static void initialize()
{
   uint8 isNameStartOrFollower = (uint8) (ISNAMESTART | ISNAMEFOLLOWER),i;
   for (i = 'a'; i <= 'z'; ++i)
      is[i] = isNameStartOrFollower;
   for (i = 'A'; i <= 'Z'; ++i)
      is[i] = isNameStartOrFollower;
   for (i = '0'; i <= '9'; ++i)
      is[i] = ISNAMEFOLLOWER;
   is['_'] = isNameStartOrFollower;
   is[':'] = isNameStartOrFollower;
   is['-'] = ISNAMEFOLLOWER;
   is['.'] = ISNAMEFOLLOWER;
   is[' '] = ISSPACE;
   is['\r'] = ISSPACE;
   is['\n'] = ISSPACE;
   is['\t'] = ISSPACE;
   is['\f'] = ISSPACE;
   is['\''] = ISQUOTE;
   is['\"'] = ISQUOTE;
   is['>'] = ISENDTAGDLM;
   is['<'] = ISCONTENTDLM;
   is['&'] = ISCONTENTDLM;
   is[';'] = ISENDREFERENCE;

   chrRef[0] = "<lt";
   chrRef[1] = ">gt";
   chrRef[2] = "&amp";
   chrRef[3] = "'apos";
   chrRef[4] = "\"quot";

   xmlInitialized = true;
}

typedef struct
{
   Method foundStartTagName;
   Method foundEndTagName;
   Method foundEndEmptyTag;
   Method foundCharacterData;
   Method foundCharacter;
   Method foundAttributeName;
   Method foundAttributeValue;
   Method foundComment;
   Method foundProcessingInstruction;
   Method foundDeclaration;
   Method foundReference;
   Method foundInvalidData;
   Method foundEndOfInput;
} TBoundMethods, *BoundMethods;

//////////////////////////////////////////////////////////////////////////
//                 Java methods that will be called                     //
//////////////////////////////////////////////////////////////////////////

static BoundMethods getBoundMethods(TCObject xml)
{
   return (BoundMethods)ARRAYOBJ_START(XmlTokenizer_bag(xml));
}

static void foundStartTagName(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundStartTagName != null)
      executeMethod(currentContext, b->foundStartTagName, xml, input, offset, count);
}

static void foundEndTagName(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundEndTagName != null)
      executeMethod(currentContext, b->foundEndTagName, xml, input, offset, count);
}

static void foundEndEmptyTag(Context currentContext, TCObject xml)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundEndEmptyTag != null)
      executeMethod(currentContext, b->foundEndEmptyTag, xml);
}

static void foundCharacterData(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundCharacterData != null)
      executeMethod(currentContext, b->foundCharacterData, xml, input, offset, count);
}

static void foundCharacter(Context currentContext, TCObject xml, JChar charFound)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundCharacter != null)
      executeMethod(currentContext, b->foundCharacter, xml, charFound);
}

static void foundAttributeName(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundAttributeName != null)
      executeMethod(currentContext, b->foundAttributeName, xml, input, offset, count);
}

static void foundAttributeValue(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count, uint8 dlm)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundAttributeValue != null)
      executeMethod(currentContext, b->foundAttributeValue, xml, input, offset, count, dlm);
}

static void foundComment(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundComment != null)
      executeMethod(currentContext, b->foundComment, xml, input, offset, count);
}

static void foundProcessingInstruction(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundProcessingInstruction != null)
      executeMethod(currentContext, b->foundProcessingInstruction, xml, input, offset, count);
}

static void foundDeclaration(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundDeclaration != null)
      executeMethod(currentContext, b->foundDeclaration, xml, input, offset, count);
}

static void foundReference(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundReference != null)
      executeMethod(currentContext, b->foundReference, xml, input, offset, count);
}

static void foundInvalidData(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundInvalidData != null)
      executeMethod(currentContext, b->foundInvalidData, xml, input, offset, count);
}

static void foundEndOfInput(Context currentContext, TCObject xml, int32 count)
{
   BoundMethods b = getBoundMethods(xml);
   if (b->foundEndOfInput != null)
      executeMethod(currentContext, b->foundEndOfInput, xml, count);
}

//////////////////////////////////////////////////////////////////////////

static JChar hex2char(uint8* input, int32 count)
{
   JChar res = 0;
   if (1 <= count && count <= 4)
      while (true)
      {
         JChar c = (JChar) *input++;
         if (c <= '9')
         {
            if (c < '0')
               break;
            res += (c & 0xF);
         }
         else
         {
            if ((c = (JChar) ((c & ~('a' - 'A')) - 'A')) >= (JChar) (16 - 10))
               break;
            res += (c + 10);
         }
         if (--count == 0)
            return res;
         res <<= 4;
      }
   return 0xFFFF;
}

static JChar dec2char(uint8* input, int32 count)
{
   JChar res = 0;
   if (count > 0)
      while (true)
      {
         JChar c = (JChar) *input++;
         if (c <= '9')
         {
            if (c < '0')
               break;
            res += (c & 0xF);
         }
         if (--count == 0)
            return res;
         if (res >= 6553 && (res > 6553 || *(input+1) > '5'))
            break;
         res = (JChar) ((res << 1) + (res << 3));
      }
   return 0xFFFF;
}

static JChar ref2char(uint8* input, int32 count)
{
   uint8** refs = (uint8**)chrRef;
   for (; *refs != null; refs++)
      if (strEqn((CharP)input, (CharP)(*refs+1), count))
         return *refs[0];
   return 0xFFFF;
}

static JChar resolveCharacterReference(Context currentContext, TCObject inputObj, int32 offset, int32 count)
{
   if (checkArrayRange(currentContext, inputObj, offset, count))
   {
      uint8 *input = ((uint8*)ARRAYOBJ_START(inputObj)) + offset;
      if (count > 1 && *input == '#')
      {
         input++;
         if (*input == 'x' || *input == 'X')
            return hex2char(input + 1, count - 2);
         else
            return dec2char(input, count - 1);
      }
      else
         return ref2char(input, count);
   }
   return 0xFFFF;
}

static void throwSyntaxException(Context currentContext, int32 code, int32 offset)
{
   TCObject exception = currentContext->thrownException = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.xml.SyntaxException");
   if (exception)
   {
      Method m = getMethod(OBJ_CLASS(exception), false, CONSTRUCTOR_NAME, 2, J_INT, J_INT);
      if (m)
      {
         executeMethod(currentContext, m, exception, code, offset);
         fillStackTrace(currentContext, exception, -1, currentContext->callStack);
      }
      setObjectLock(exception, UNLOCKED);
   }
}

static void tellReference(Context currentContext, TCObject xml, TCObject input, int32 offset, int32 count)
{
   if (XmlTokenizer_resolveCharRef(xml))
   {
      JChar res = resolveCharacterReference(currentContext, input, offset, count);
      if (XmlTokenizer_strictlyXml(xml) && res == 0xFFFF)
      {
         throwSyntaxException(currentContext, XmlTokenizer_state(xml), XmlTokenizer_ofsCur(xml) + XmlTokenizer_readPos(xml));
         return;
      }
      foundCharacter(currentContext, xml, res);
   }
   else
      foundReference(currentContext, xml, input, offset, count);
}

static bool endTokenize(Context currentContext, TCObject xml, TCObject input)
{
   int32 dif = XmlTokenizer_ofsCur(xml) - XmlTokenizer_ofsStart(xml);
   switch (XmlTokenizer_state(xml))
   {
      case 0:
         break;
      case 10:
         if (dif > 0)
            foundCharacterData(currentContext, xml, input, XmlTokenizer_ofsStart(xml), dif);
         break;
      case 11:
         if (!XmlTokenizer_strictlyXml(xml))
         {
            tellReference(currentContext, xml, input, XmlTokenizer_ofsStart(xml), dif);
            break;
         }
         /* fall thru */
      default:
         throwSyntaxException(currentContext, XmlTokenizer_state(xml), XmlTokenizer_ofsCur(xml) + XmlTokenizer_readPos(xml));
         return false;
   }
   foundEndOfInput(currentContext, xml, XmlTokenizer_ofsCur(xml) + XmlTokenizer_readPos(xml));
   return true;
}

//////////////////////////////////////////////////////////////////////////
#define REMOVE_IF_ONLY_RETURNS_VOID(m)  if (m->code[0].op.op == RETURN_void) m = null;
TC_API void txXT_nativeCreate(NMParams p) // totalcross/xml/XmlTokenizer native private void nativeCreate();
{
   TCObject xml = p->obj[0];
   if (!xmlInitialized)
      initialize();
   if ((XmlTokenizer_bag(xml) = createByteArray(p->currentContext, sizeof(TBoundMethods))) != null)
   {
      TCClass c = OBJ_CLASS(xml);
      BoundMethods b = getBoundMethods(xml);
      b->foundStartTagName = getMethod(c, true, "foundStartTagName", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundEndTagName = getMethod(c, true, "foundEndTagName", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundCharacterData = getMethod(c, true, "foundCharacterData", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundAttributeName = getMethod(c, true, "foundAttributeName", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundAttributeValue = getMethod(c, true, "foundAttributeValue", 4, BYTE_ARRAY, J_INT, J_INT, J_BYTE);
      b->foundComment = getMethod(c, true, "foundComment", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundProcessingInstruction = getMethod(c, true, "foundProcessingInstruction", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundDeclaration = getMethod(c, true, "foundDeclaration", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundReference = getMethod(c, true, "foundReference", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundInvalidData = getMethod(c, true, "foundInvalidData", 3, BYTE_ARRAY, J_INT, J_INT);
      b->foundEndEmptyTag = getMethod(c, true, "foundEndEmptyTag", 0);
      b->foundCharacter = getMethod(c, true, "foundCharacter", 1, J_CHAR);
      b->foundEndOfInput = getMethod(c, true, "foundEndOfInput", 1, J_INT);
      // now an optimization. We check if the method contains only a "return;". If so, we null out the reference and then the method won't be called
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundStartTagName);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundEndTagName);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundCharacterData);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundAttributeName);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundAttributeValue);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundComment);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundProcessingInstruction);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundDeclaration);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundReference);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundInvalidData);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundEndEmptyTag);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundCharacter);
      REMOVE_IF_ONLY_RETURNS_VOID(b->foundEndOfInput);
      setObjectLock(XmlTokenizer_bag(xml), UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void txXT_resolveCharacterReference_B(NMParams p) // totalcross/xml/XmlTokenizer native public static JChar resolveCharacterReference(uint8 []input, int32 offset, int32 count);
{
   p->retI = resolveCharacterReference(p->currentContext, p->obj[0], p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void txXT_setCdataContents_Bii(NMParams p) // totalcross/xml/XmlTokenizer native protected void setCdataContents(uint8 []input, int32 offset, int32 count);
{
   TCObject xml = p->obj[0];
   TCObject inputObj = p->obj[1];
   int32 offset = p->i32[0];
   int32 count = p->i32[1];
   uint8 *input, *endTagToSkip;

   if ((XmlTokenizer_endTagToSkipTo(xml) = createByteArray(p->currentContext, count)) != null)
   {
      input = (uint8*) ARRAYOBJ_START(inputObj);
      endTagToSkip = (uint8*) ARRAYOBJ_START(XmlTokenizer_endTagToSkipTo(xml));
      for (input += offset; count-- > 0; input++)
         *endTagToSkip++ = (*input >= 'a') ? (*input - 32) : *input;
      setObjectLock(XmlTokenizer_endTagToSkipTo(xml), UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void txXT_endTokenize_B(NMParams p) // totalcross/xml/XmlTokenizer native private void endTokenize(uint8 []input) throws SyntaxException;
{
   endTokenize(p->currentContext, p->obj[0], p->obj[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void txXT_tokenizeBytes_B(NMParams p) // totalcross/xml/XmlTokenizer native private void tokenizeBytes(uint8 []input) throws SyntaxException;
{
   TCObject xml      = p->obj[0];
   TCObject inputObj = p->obj[1];
   int32 ofsStart         = XmlTokenizer_ofsStart(xml);
   int32 state            = XmlTokenizer_state(xml);
   int32 substate         = XmlTokenizer_substate(xml);
   int32 ixEndTagToSkipTo = XmlTokenizer_ixEndTagToSkipTo(xml);
   int32 strictlyXml      = XmlTokenizer_strictlyXml(xml);
   uint8 *input0 = null, *input = null, *inputEnd = null;
   Context currentContext = p->currentContext;
   uint8* _is = is;

   if (inputObj == null)
   {
      throwNullArgumentException(p->currentContext, "input");
      return;
   }
   else
   {
      int32 ofsCur = XmlTokenizer_ofsCur(xml);
      int32 ofsEnd = XmlTokenizer_ofsEnd(xml);
      input0 = (uint8*)ARRAYOBJ_START(inputObj);
      input    = input0 + ofsCur;
      inputEnd = input0 + ofsEnd;
#define _ofsCur ((int32)(input-input0))
   }

   while (input < inputEnd)
   {
      int32 ch = (int32) *input;
      switch (state)
      {
         case 0:
            ofsStart = _ofsCur;
            if (XmlTokenizer_endTagToSkipTo(xml) != null)
            {
               state = 22;
               continue; // same ofsCur!!! it can start </script>
            }
            else
            if (ch == '<')
               state = 1;
            else
            if (ch == '&')
               state = 11;
            else
               state = 10;
            break;
         case 1:
            if ((_is[ch] & ISNAMESTART) != 0)
               state = 2;
            else
            if (ch == '/')
               state = 12;
            else
            if (ch == '!')
               state = 16;
            else
            if (ch == '?')
            {
               state = 20;
               substate = 0; // so we wait for "?>"
            }
            else
            if (!strictlyXml)
               state = 10; // recovery: process "<$xxx" as data
            else
            if (!endTokenize(currentContext, xml,inputObj)) // strictly XML: give up
               goto end;
            break;
         case 2:
            while ((_is[ch] & ISNAMEFOLLOWER) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (ch == '>')
               state = 0;
            else
            if (ch == '/')
               state = 9;
            else
            if ((_is[ch] & ISSPACE) != 0)
               state = 3;
            else
            if (!strictlyXml)
            {
               // <ABC$xxx
               state = 10; // recovery: process "<ABC$xxx" as data
               break;
            }
            else
            if (!endTokenize(currentContext, xml, inputObj)) // strictly XML: give up
               goto end;
            foundStartTagName(currentContext, xml, inputObj, ofsStart + 1, _ofsCur - ofsStart - 1);
            break;
         case 3:
            while ((_is[ch] & ISSPACE) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (ch == '>')
               state = 0;
            else
            if (ch == '/')
               state = 9;
            else
            if ((_is[ch] & ISNAMESTART) != 0)
            {
               ofsStart = _ofsCur;
               state = 4;
            }
            else
               state = 21; // possible recovery: skip to TAGC
            break;
         case 4:
            while ((_is[ch] & ISNAMEFOLLOWER) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if ((_is[ch] & ISSPACE) != 0)
               state = 5;
            else
            if (ch == '=')
               state = 6;
            else
            if (!strictlyXml && (ch == '>'))
               state = 0; // <list compact> allowed in HTML
            else
            {
               state = 21; // possible recovery: skip to TAGC
               break;
            }
            foundAttributeName(currentContext, xml, inputObj, ofsStart, _ofsCur - ofsStart);
            break;
         case 5:
            while ((_is[ch] & ISSPACE) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (ch == '=')
               state = 6;
            else
            if (!strictlyXml)
            {
               if (ch == '>')
                  state = 0; // <list compact > allowed in HTML
               else
               if ((_is[ch] & ISNAMESTART) != 0)
               {
                  ofsStart = _ofsCur;
                  state = 4; // <list compact simple> allowed in HTML
               }
               else
                  state = 21; // possible recovery: skip to TAGC
            }
            else
               state = 21; // possible recovery: skip to TAGC
            break;
         case 6:
            while ((_is[ch] & ISSPACE) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if ((_is[ch] & ISQUOTE) != 0)
            {
               XmlTokenizer_quote(xml) = (uint8) ch;
               ofsStart = _ofsCur;
               state = 7;
            }
            else
            if (!strictlyXml)
            {
               if (ch == '>')
                  state = 0;
               else
               {
                  ofsStart = _ofsCur;
                  state = 15;
               }
            }
            else
            if (!endTokenize(currentContext, xml,inputObj)) // strictly XML: give up
               goto end;
            break;
         case 7:
            while (ch != XmlTokenizer_quote(xml))
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            ++ofsStart;
            foundAttributeValue(currentContext, xml,inputObj, ofsStart, _ofsCur - ofsStart, (uint8)XmlTokenizer_quote(xml));
            state = 8;
            break;
         case 8:
            if (ch == '>')
               state = 0;
            else
            if (ch == '/')
               state = 9;
            else
            if ((_is[ch] & ISSPACE) != 0)
               state = 3;
            else
            if ((_is[ch] & ISNAMESTART) != 0)
            {
               ofsStart = _ofsCur;
               state = 4;
            }
            else
               state = 21; // possible recovery: skip to TAGC
            break;
         case 9:
            if (ch != '>')
               state = 21; // possible recovery: skip to TAGC
            else
            {
               foundEndEmptyTag(currentContext, xml);
               state = 0;
            }
            break;
         case 10:
            while ((_is[ch] & ISCONTENTDLM) == 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (_ofsCur > ofsStart)
               foundCharacterData(currentContext, xml, inputObj, ofsStart, _ofsCur - ofsStart);
            ofsStart = _ofsCur;
            if (ch == '<')
               state = 1;
            else
               state = 11;
            break;
         case 11:
            while ((_is[ch] & (ISCONTENTDLM | ISSPACE | ISENDREFERENCE)) == 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            tellReference(currentContext, xml, inputObj, ofsStart + 1, _ofsCur - ofsStart - 1);
            if (ch == ';')
            {
               ofsStart = _ofsCur + 1; // data starts at next uint8
               state = 10;
            }
            else
            if (!strictlyXml)
            {
               ofsStart = _ofsCur;
               if (ch == '<')
                  state = 1;
               else
               if (ch != '&') // spaces (else '&' again, stay here)
                  state = 10;
            }
            else
            if (!endTokenize(currentContext, xml, inputObj)) // strictly XML: give up
               goto end;
            break;
         case 12:
            if ((_is[ch] & ISNAMESTART) != 0)
               state = 13;
            else
            if (!strictlyXml)
               state = 10; // recovery: process "</$xxx" as data
            else
            if (!endTokenize(currentContext, xml, inputObj)) // strictly XML: give up
               goto end;
            break;
         case 13:
            while ((_is[ch] & ISNAMEFOLLOWER) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (ch == '>')
               state = 0;
            else
            if ((_is[ch] & ISSPACE) != 0)
               state = 14;
            else
            if (!strictlyXml)
            {
               state = 10; // recovery: process "</xxx$" as data
               break;
            }
            else
            if (!endTokenize(currentContext, xml, inputObj)) // strictly XML: give up
               goto end;
            foundEndTagName(currentContext, xml, inputObj, ofsStart + 2, _ofsCur - ofsStart - 2);
            break;
         case 14:
            while ((_is[ch] & ISSPACE) != 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            if (ch == '>')
               state = 0;
            else
               state = 21; // possible recovery: skip to TAGC
            break;
         case 15: // !strictlyXml
            while ((_is[ch] & (ISSPACE | ISENDTAGDLM)) == 0)
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            foundAttributeValue(currentContext, xml, inputObj, ofsStart, _ofsCur - ofsStart, (uint8) 0);
            if (ch == '>')
               state = 0;
            else
               state = 3;
            break;
         case 16:
            ofsStart = _ofsCur;
            if (ch == '-')
               state = 18;
            else
               state = 17;
            break;
         case 17:
            while (ch != '>')
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            foundDeclaration(currentContext, xml, inputObj, ofsStart, _ofsCur - ofsStart);
            state = 0;
            break;
         case 18:
            if (ch == '-')
            {
               ofsStart = _ofsCur;
               state = 19;
               substate = 0; // so we wait for "-->"
            }
            else // keep ofsStart unchanged!
               state = 17;
            break;
         case 19:
            switch (substate)
            {
               case 0:
                  while (ch != '-')
                  {
                     if (++input >= inputEnd)
                        goto end;
                     ch = (int32) *input;
                  }
                  substate = 1;
                  break;
               case 1: // '-' found
                  if (ch != '-')
                     substate = 0;
                  else
                     substate = 2;
                  break;
               case 2: // '-'('-')+ found
                  while (ch == '-')
                  {
                     if (++input >= inputEnd)
                        goto end;
                     ch = (int32) *input;
                  }
                  if (ch == '>')
                  {
                     foundComment(currentContext, xml, inputObj, ofsStart + 1, _ofsCur - ofsStart - 3);
                     ofsStart = _ofsCur;
                     state = 0;
                  }
                  else
                  if (ch == '!')
                     substate = 3;
                  else
                     substate = 0;
                  break;
               case 3:
                  if (ch == '>')
                  {
                     foundComment(currentContext, xml, inputObj, ofsStart + 1, _ofsCur - ofsStart - 4);
                     ofsStart = _ofsCur;
                     state = 0;
                  }
                  else
                     substate = 0;
                  break;
            }
            break;
         case 20:
            switch (substate)
            {
               case 0:
                  while (ch != '?')
                  {
                     if (++input >= inputEnd)
                        goto end;
                     ch = (int32) *input;
                  }
                  substate = 1;
                  break;
               case 1:
                  if (ch == '>')
                  {
                     foundProcessingInstruction(currentContext, xml, inputObj, ofsStart + 2, _ofsCur - ofsStart - 3);
                     ofsStart = _ofsCur;
                     state = 0;
                  }
                  else
                     substate = 0;
                  break;
            }
            break;
         case 21: // Skip to TAGC
            if (strictlyXml)
            {
               if (!endTokenize(currentContext, xml, inputObj)) // strictly XML: give up
                  goto end;
            }
            else
            {
               ofsStart = _ofsCur;
               while (ch != '>')
               {
                  if (++input >= inputEnd)
                     goto end;
                  ch = (int32) *input;
               }
               foundInvalidData(currentContext, xml, inputObj, ofsStart, _ofsCur - ofsStart);
               state = 0;
            }
            break;
         case 22: // skip to end tag (SCRIPT contents)
            while (ch != '<')
            {
               if (++input >= inputEnd)
                  goto end;
               ch = (int32) *input;
            }
            state = 23;
            break;
         case 23:
            if (ch != '/')
               state = 22;
            else
            {
               state = 24;
               ixEndTagToSkipTo = 0;
            }
            break;
         case 24:
            if (ixEndTagToSkipTo == (int32)ARRAYOBJ_LEN(XmlTokenizer_endTagToSkipTo(xml)))
            {
               int32 ofsTemp = _ofsCur - ixEndTagToSkipTo - 2;
               if (ch == '>')
                  state = 0;
               else
               if ((_is[ch] & ISSPACE) != 0)
                  state = 14;
               else
               {
                  state = 22;
                  break; // abandon here
               }
               foundCharacterData(currentContext, xml, inputObj, ofsStart, ofsTemp - ofsStart);
               ofsStart = ofsTemp;
               foundEndTagName(currentContext, xml, inputObj, ofsTemp + 2, ixEndTagToSkipTo);
               XmlTokenizer_endTagToSkipTo(xml) = null;
            }
            else
            {
               uint8* endTagToSkipTo = (uint8*)ARRAYOBJ_START(XmlTokenizer_endTagToSkipTo(xml));
               if ('a' <= ch)
                  ch -= ('a' - 'A'); // fast toUpper
               if (endTagToSkipTo[ixEndTagToSkipTo++] != (uint8) ch)
                  state = 22;
            }
            break;
      }
      input++;
   }
end:
   XmlTokenizer_ofsCur(xml) = _ofsCur;
   XmlTokenizer_state(xml) = state;
   XmlTokenizer_ofsStart(xml) = ofsStart;
   XmlTokenizer_substate(xml) = substate;
   XmlTokenizer_ixEndTagToSkipTo(xml) = ixEndTagToSkipTo;
}

#ifdef ENABLE_TEST_SUITE
#include "xml_XmlTokenizer_test.h"
#endif
