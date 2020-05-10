// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   return PM_NONE;
}