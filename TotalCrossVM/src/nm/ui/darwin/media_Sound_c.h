// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined (darwin)
 #ifdef __cplusplus
  extern "C" {
 #endif

 void iphone_soundPlay(CharP filename);
 void iphone_soundBeep();
 void iphone_soundToText(JCharP parameters, int32 parameterLength,
                         JCharP *outText, int32 *outLength);

 #ifdef __cplusplus
  };
 #endif
#endif // darwin


static void soundPlay(CharP filename)
{
   iphone_soundPlay(filename);
}

static void soundSetEnabled(int b)
{
}

/*static void soundBeep(void)
{
   iphone_soundBeep();
}*/

bool soundTone(int freq, unsigned short duration)
{
   return false;
}

static void soundToText(NMParams p)
{
   TCObject params = p->obj[0];
   JCharP text = NULL;
   int32 length = 0;
   iphone_soundToText(params ? String_charsStart(params) : NULL,
                      params ? String_charsLen(params) : 0, &text, &length);
   if (text)
   {
      p->retO = createStringObjectFromJCharP(p->currentContext, text, length);
      free(text);
   }
}
