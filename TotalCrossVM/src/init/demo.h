// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef DEMO_H
#define DEMO_H

/// Gets the virtual machine compilation date in format YYYYMMDD, xored by 12341234 (defined below)
int32 getCompilationDate();
#define COMPILATION_MASK 12341234

#endif
