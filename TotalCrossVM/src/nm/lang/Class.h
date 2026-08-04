// Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef Class_h
#define Class_h

#include "tcvm.h"

// java.lang.Class
#define Class_nativeStruct(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Class_targetName(o)         FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Class_methods(o)            FIELD_OBJ(o, OBJ_CLASS(o), 4)

#endif
