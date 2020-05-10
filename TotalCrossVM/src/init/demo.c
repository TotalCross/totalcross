// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "compilation.date"

int32 getCompilationDate()
{
   return COMPILATION_MASK ^ COMPILATION_DATE;
}
