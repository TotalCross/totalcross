// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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