// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined(darwin)

void privateGetInt(NMParams p, int32 hk, TCHARP key, TCHARP value);
void privateGetString(NMParams p, int32 hk, TCHARP key, TCHARP value);
void privateGetBlob(NMParams p, int32 hk, TCHARP key, TCHARP value);
void privateSetInt(Context c, int32 hk, TCHARP key, TCHARP value, int32 data);
void privateSetString(Context c, int32 hk, TCHARP key, TCHARP value, CharP data);
void privateSetBlob(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len);
bool privateDelete(int32 hk, TCHARP key, TCHARP value);
TCObject privateList(Context c, int32 hk, TCHARP key);

#else

static void privateGetInt(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
}

static void privateGetString(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
}

static void privateGetBlob(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
}

static void privateSetInt(Context c, int32 hk, TCHARP key, TCHARP value, int32 data)
{
}

static void privateSetString(Context c, int32 hk, TCHARP key, TCHARP value, CharP data)
{
}

static void privateSetBlob(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len)
{
}

static bool privateDelete(int32 hk, TCHARP key, TCHARP value)
{
   return false;
}

static TCObject privateList(Context c, int32 hk, TCHARP key)
{
   return null;
}

#endif
