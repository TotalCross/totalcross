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

// guich@421_43: new upper/lower convertions, and made int2hex native - guich@421_74: moved to here and fixed and optimized the routines
static char* LOWER3 = "1313BC10110310510710910B10D10F11111311511711911B11D11F12112312512712912B12D12F06913313513713A13C13E14014214414614814B14D14F15115315515715915B15D15F16116316516716916B16D16F1711731751770FF17A17C17E07325318318525418825625718C1DD25925B19226026326926819926F2722751A11A31A52801A82831AD2881B028A28B1B41B62921B91BD1C61C61C91C91CC1CC1CE1D01D21D41D61D81DA1DC1DF1E11E31E51E71E91EB1ED1EF1F31F31F51951BF1F91FB1FD1FF20120320520720920B20D20F21121321521721921B21D21F19E22322522722922B22D22F2312333B93AC3AD3AE3AF3CC3CD3CE3B13B23B33B43B53B63B73B83B93BA3BB3BC3BD3BE3BF3C03C13C33C43C53C63C73C83C93CA3CB3C33B23B83C63C03D93DB3DD3DF3E13E33E53E73E93EB3ED3EF3BA3C13B83B53F83F23FB45045145245345445545645745845945A45B45C45D45E45F43043143243343443543643743843943A43B43C43D43E43F44044144244344444544644744844944A44B44C44D44E44F46146346546746946B46D46F47147347547747947B47D47F48148B48D48F49149349549749949B49D49F4A14A34A54A74A94AB4AD4AF4B14B34B54B74B94BB4BD4BF4C24C44C64C84CA4CC4CE4D14D34D54D74D94DB4DD4DF4E14E34E54E74E94EB4ED4EF4F14F34F54F950150350550750950B50D50F56156256356456556656756856956A56B56C56D56E56F57057157257357457557657757857957A57B57C57D57E57F580581582583584585586";
static char* LOWER4 = "1E011E031E051E071E091E0B1E0D1E0F1E111E131E151E171E191E1B1E1D1E1F1E211E231E251E271E291E2B1E2D1E2F1E311E331E351E371E391E3B1E3D1E3F1E411E431E451E471E491E4B1E4D1E4F1E511E531E551E571E591E5B1E5D1E5F1E611E631E651E671E691E6B1E6D1E6F1E711E731E751E771E791E7B1E7D1E7F1E811E831E851E871E891E8B1E8D1E8F1E911E931E951E611EA11EA31EA51EA71EA91EAB1EAD1EAF1EB11EB31EB51EB71EB91EBB1EBD1EBF1EC11EC31EC51EC71EC91ECB1ECD1ECF1ED11ED31ED51ED71ED91EDB1EDD1EDF1EE11EE31EE51EE71EE91EEB1EED1EEF1EF11EF31EF51EF71EF91F001F011F021F031F041F051F061F071F101F111F121F131F141F151F201F211F221F231F241F251F261F271F301F311F321F331F341F351F361F371F401F411F421F431F441F451F511F531F551F571F601F611F621F631F641F651F661F671F801F811F821F831F841F851F861F871F901F911F921F931F941F951F961F971FA01FA11FA21FA31FA41FA51FA61FA71FB01FB11F701F711FB303B91F721F731F741F751FC31FD01FD11F761F771FE01FE11F7A1F7B1FE51F781F791F7C1F7D1FF303C9006B00E52170217121722173217421752176217721782179217A217B217C217D217E217F24D024D124D224D324D424D524D624D724D824D924DA24DB24DC24DD24DE24DF24E024E124E224E324E424E524E624E724E824E9FF41FF42FF43FF44FF45FF46FF47FF48FF49FF4AFF4BFF4CFF4DFF4EFF4FFF50FF51FF52FF53FF54FF55FF56FF57FF58FF59FF5A";
static char* UPPER3 = "0490B510010210410610810A10C10E11011211411611811A11C11E12012212412612812A12C12E13013213413613913B13D13F14114314514714A14C14E15015215415615815A15C15E16016216416616816A16C16E17017217417617817917B17D17F18118218418618718918A18B18E18F19019119319419619719819C19D19F1A01A21A41A61A71A91AC1AE1AF1B11B21B31B51B71B81BC1C41C51C71C81CA1CB1CD1CF1D11D31D51D71D91DB1DE1E01E21E41E61E81EA1EC1EE1F11F21F41F61F71F81FA1FC1FE20020220420620820A20C20E21021221421621821A21C21E22022222422622822A22C22E23023234538638838938A38C38E38F39139239339439539639739839939A39B39C39D39E39F3A03A13A33A43A53A63A73A83A93AA3AB3C23D03D13D53D63D83DA3DC3DE3E03E23E43E63E83EA3EC3EE3F03F13F43F53F73F93FA40040140240340440540640740840940A40B40C40D40E40F41041141241341441541641741841941A41B41C41D41E41F42042142242342442542642742842942A42B42C42D42E42F46046246446646846A46C46E47047247447647847A47C47E48048A48C48E49049249449649849A49C49E4A04A24A44A64A84AA4AC4AE4B04B24B44B64B84BA4BC4BE4C14C34C54C74C94CB4CD4D04D24D44D64D84DA4DC4DE4E04E24E44E64E84EA4EC4EE4F04F24F44F850050250450650850A50C50E53153253353453553653753853953A53B53C53D53E53F54054154254354454554654754854954A54B54C54D54E54F550551552553554555556";
static char* UPPER4 = "1E001E021E041E061E081E0A1E0C1E0E1E101E121E141E161E181E1A1E1C1E1E1E201E221E241E261E281E2A1E2C1E2E1E301E321E341E361E381E3A1E3C1E3E1E401E421E441E461E481E4A1E4C1E4E1E501E521E541E561E581E5A1E5C1E5E1E601E621E641E661E681E6A1E6C1E6E1E701E721E741E761E781E7A1E7C1E7E1E801E821E841E861E881E8A1E8C1E8E1E901E921E941E9B1EA01EA21EA41EA61EA81EAA1EAC1EAE1EB01EB21EB41EB61EB81EBA1EBC1EBE1EC01EC21EC41EC61EC81ECA1ECC1ECE1ED01ED21ED41ED61ED81EDA1EDC1EDE1EE01EE21EE41EE61EE81EEA1EEC1EEE1EF01EF21EF41EF61EF81F081F091F0A1F0B1F0C1F0D1F0E1F0F1F181F191F1A1F1B1F1C1F1D1F281F291F2A1F2B1F2C1F2D1F2E1F2F1F381F391F3A1F3B1F3C1F3D1F3E1F3F1F481F491F4A1F4B1F4C1F4D1F591F5B1F5D1F5F1F681F691F6A1F6B1F6C1F6D1F6E1F6F1F881F891F8A1F8B1F8C1F8D1F8E1F8F1F981F991F9A1F9B1F9C1F9D1F9E1F9F1FA81FA91FAA1FAB1FAC1FAD1FAE1FAF1FB81FB91FBA1FBB1FBC1FBE1FC81FC91FCA1FCB1FCC1FD81FD91FDA1FDB1FE81FE91FEA1FEB1FEC1FF81FF91FFA1FFB1FFC2126212A212B2160216121622163216421652166216721682169216A216B216C216D216E216F24B624B724B824B924BA24BB24BC24BD24BE24BF24C024C124C224C324C424C524C624C724C824C924CA24CB24CC24CD24CE24CFFF21FF22FF23FF24FF25FF26FF27FF28FF29FF2AFF2BFF2CFF2DFF2EFF2FFF30FF31FF32FF33FF34FF35FF36FF37FF38FF39FF3A";

TC_API JCharP CharP2JCharP(CharP s, int32 len)
{
   if (len < 0 ) len = xstrlen(s);
   return CharP2JCharPBuf(s, len, (JCharP)xmalloc((len+1)*2), true);
}

TC_API CharP JCharP2CharP(JCharP js, int32 len)
{
   if (len < 0 ) len = JCharPLen(js);
   return JCharP2CharPBuf(js, len, (CharP)xmalloc(len+1));
}

TC_API JCharP CharP2JCharPBuf(CharP s, int32 len, JCharP buffer, bool endWithZero)
{
   JCharP js = buffer;
   if (len < 0) len = xstrlen(s);
   if (js)
   {
      while (len-- > 0)
         *js++ = *s++ & 0xFF;
      if (endWithZero) *js = 0;
   }
   return buffer;
}

TC_API CharP JCharP2CharPBuf(JCharP js, int32 len, CharP buffer)
{
   CharP s = buffer;
   if (len < 0) len = JCharPLen(js);
   if (s)
   {
      while (len-- > 0)
         *s++ = (uint8)*js++;
      *s = 0;
   }
   return buffer;
}

TC_API int32 JCharPLen(JCharP s)
{
   int32 len = 0;
   while (*s++)
      len++;
   return len;
}

// given the hex char, return its decimal value
static int32 digitOf(char c) // don't inline!
{
   return ('0' <= c && c <= '9') ? (c - '0') : (c - 'A' + 10);
}

// given the 3-char hex value, return its decimal value
static int32 hex2unsigned3(CharP p)
{
   return (digitOf(*(p)) << 8) | (digitOf(*(p+1)) << 4) | digitOf(*(p+2));
}

// given the 4-char hex value, return its decimal value
static int32 hex2unsigned4(CharP p)
{
   return (digitOf(*p) << 12) | (digitOf(*(p+1)) << 8) | (digitOf(*(p+2)) << 4) | digitOf(*(p+3));
}

// search for a given fixed-size-3 string
static char* searchChar3(CharP from, CharP what)
{
   for (; *from; from+=3)
      if (*from == *what && *(from+1) == *(what+1) && *(from+2) == *(what+2))
         return from;
   return NULL;
}

// search for a given fixed-size-4 string
static char* searchChar4(CharP from, CharP what)
{
   for (; *from; from+=4)
      if (*from == *what && *(from+1) == *(what+1) && *(from+2) == *(what+2) && *(from+3) == *(what+3))
         return from;
   return NULL;
}

// converts a char to lower case, supporting unicode
TC_API JChar JCharToLower(JChar c)
{
   char buf5[5];
   // prioritize ascii
   if (('A' <= c && c <= 'Z') || (0xc0 <= c && c <= 0xdd && c != 215)) // added 215 to here too - guich@502_6: replaced the strange unicode values by hexadecimal
      return c + 32;
   else
   if (c > 255) // guich@554_24
   {
      CharP buf = buf5;
      CharP p;
      if (c < 0x1000)
      {
         int2hex(c,3,buf);
         p = searchChar3(UPPER3,buf); // search in upper,
         if (p)
            return hex2unsigned3(&LOWER3[p-UPPER3]); // return from lower
      }
      else
      {
         int2hex(c,4,buf);
         p = searchChar4(UPPER4,buf); // search in lower
         if (p)
            return hex2unsigned4(&LOWER4[p-UPPER4]); // return from upper
         // special cases
         if (c == 0x2126)
            return 0x3C9;
         if (c == 0x212A)
            return 0x6B;
         if (c == 0x212B)
            return 0xE5;
      }
   }
   return c;
}

// converts a char to upper case, supporting unicode
TC_API JChar JCharToUpper(JChar c)
{
   char buf5[5];
   if (('a' <= c && c <= 'z') || (0xe0 <= c && c <= 0xfd && c != 247)) // guich@502_6: replaced the strange unicode values by hexadecimal
      return c - 32;
   else
   if (c > 255) // guich@554_24
   {
      CharP buf = buf5;
      CharP p;
      if (c < 0x1000)
      {
         int2hex(c,3,buf);
         p = searchChar3(LOWER3,buf); // search in upper,
         if (p)
            return hex2unsigned3(&UPPER3[p-LOWER3]); // return from lower
      }
      else
      {
         int2hex(c,4,buf);
         p = searchChar4(LOWER4,buf); // search in lower
         if (p)
            return hex2unsigned4(&UPPER4[p-LOWER4]); // return from upper
         if (c == 0x1FBE)
            return 0x399;

      }
   }
   return c;
}

TC_API int32 JCharPIndexOfJCharP(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen) // guich@320_6
{
   JChar c;
   int32 count = meLen >= 0 ? meLen : JCharPLen(me);
   int32 scount = otherLen >= 0 ? otherLen : JCharPLen(other);
   int32 j = (count - scount);
   int32 len;
   JCharP s;
   JCharP s1;
   if (start < 0)
      start = 0;
   else
   if (start >= count)
      return -1;
   if (scount == 0)
      return start;
   if (otherLen == 1)
      return JCharPIndexOfJChar(me, *other, start, meLen);

   me += start; // guich@557_4: fixes error when the given start was < 0.
   c = *other++;
   scount--;

notFound:
   while (1)
   {
      // search for the next ocurrence of the first char of other in me
      while (start <= j && *me != c)
      {
         start++;
         me++;
      }

      // passed end of string?
      if (start > j)
         return -1;

      // now search the other strings
      len = scount;
      s = me+1;
      s1 = other;
      while (len--)
         if (*s++ != *s1++)
         {
            start++;
            me++; // guich@321_3
            goto notFound;
         }

      return start;
   }
}

TC_API int32 JCharPLastIndexOfJCharP(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen)
{
   JChar c;
   int32 count = meLen >= 0 ? meLen : JCharPLen(me);
   int32 scount = otherLen >= 0 ? otherLen : JCharPLen(other);
   int32 len;
   JCharP s;
   JCharP s1;
   if (start < 0)
      start = 0;
   else
   if (start > count)
      return -1;
   if (scount == 0)
      return start;
   if (otherLen == 1)
      return JCharPLastIndexOfJChar(me, meLen, *other, start-1);

   me += --start; // guich@557_4: fixes error when the given start was < 0.
   c = *other++;
   scount--;

notFound:
   while (1)
   {
      // search for the next ocurrence of the first char of other in me
      while (start >= 0 && *me != c)
      {
         start--;
         me--;
      }

      // passed end of string?
      if (start < 0)
         return -1;

      // now search the other strings
      len = scount;
      s = me+1;
      s1 = other;
      while (len--)
         if (*s++ != *s1++)
         {
            start--;
            me--; // guich@321_3
            goto notFound;
         }

      return start;
   }
}

TC_API int32 JCharPIndexOfJChar(JCharP me, JChar what, int32 start, int32 meLen) // guich@320_6
{
   int32 n = meLen >= 0 ? meLen : JCharPLen(me);

   if (start < 0)
      start = 0;
   else
   if (start >= n)
      return -1;

   for (me += start; start < n; start++)
      if (*me++ == what)
         return start;
   return -1;
}

TC_API bool JCharPEqualsJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen)
{
   if (me == other) // same object?
      return true;
   else
   if (me && other)
   {
      if (meLen < 0) meLen = JCharPLen(me);
      if (otherLen < 0) otherLen = JCharPLen(other);
      if (meLen != otherLen)
         return false;
      while (meLen-- > 0)
         if (*me++ != *other++)
            return false;
      return true;
   }
   return false;
}

TC_API int32 JCharPCompareToJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen)
{
   int32 n,ml,ol;
   ml = meLen >= 0 ? meLen : JCharPLen(me);
   ol = otherLen >= 0 ? otherLen : JCharPLen(other);
   for (n = min32(ml, ol); n--; me++, other++)
      if (*me != *other)
         return (int32)*me - (int32)*other;
   return ml - ol;
}

TC_API int32 JCharPHashCode(JCharP s, int32 len)
{
   int32 hash=0;
   if (len < 0) len = JCharPLen(s);
   while (len-- > 0)
      hash = (hash<<5) - hash + (int32)*s++; // was 31*hash
   return hash;
}

TC_API TCHARP JCharP2TCHARP(JCharP from, int32 len)
{
   TCHARP buf;
   if ((buf = (TCHARP) xmalloc((len+1)*sizeof(TCHAR))) == null)
      return null;
   return JCharP2TCHARPBuf(from, len, buf);
}

TC_API TCHARP JCharP2TCHARPBuf(JCharP from, int32 len, TCHARP buf)
{
#ifdef UNICODE
   tcsncpy(buf, from, len);
   buf[len] = 0;
   return buf;
#else
   return JCharP2CharPBuf(from, len, buf);
#endif
}

TC_API JCharP TCHARP2JCharP(TCHARP from, JCharP to, int32 len)
{
#ifdef UNICODE
   tcsncpy(to, from, len);
   to[len] = 0;
   return to;
#else
   return CharP2JCharPBuf(from, len, to, true);
#endif
}

TC_API bool JCharPStartsWithJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen, int32 from)
{
   if (from < 0 || from > (meLen-otherLen))
      return false;
   else
      for (me += from; otherLen-- > 0;)
         if (*me++ != *other++)
            return false;
   return true;
}

TC_API bool JCharPEndsWithJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen)
{
   return JCharPStartsWithJCharP(me, other, meLen, otherLen, meLen - otherLen);
}

TC_API bool JCharPEqualsIgnoreCaseJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen)
{
   if (me == other) // same object?
      return true;
   else
   if (me && other)
   {
      if (meLen < 0) meLen = JCharPLen(me);
      if (otherLen < 0) otherLen = JCharPLen(other);
      if (meLen != otherLen)
         return false;
      while (meLen-- > 0)
         if (JCharToLower(*me++) != JCharToLower(*other++))
            return false;
      return true;
   }
   return false;
}

TC_API int32 JCharPLastIndexOfJChar(JCharP me, int32 meLen, JChar c, int32 startIndex)
{
   if (0 <= startIndex && startIndex < meLen)
      for (; startIndex >= 0; startIndex--)
         if (me[startIndex] == c) 
            return startIndex;
   return -1;
}

TC_API void JCharPDupBuf(JCharP original, int32 length, JCharP buffer)
{
   xmemmove(buffer, original, length << 1);
   buffer[length] = 0;
}

TC_API JCharP JCharPDup(JCharP original, int32 length)
{
   JCharP buffer = (JCharP)xmalloc((length + 1) << 1);
   
   if (buffer)
      xmemmove(buffer, original, length << 1);
   return buffer;
}
