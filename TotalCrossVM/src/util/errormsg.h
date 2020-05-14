// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef ERRORMSG_H
#define ERRORMSG_H

typedef CharP (*getErrorMessageFunc)(int32 errorCode, CharP buffer, uint32 size);

TC_API CharP getErrorMessage(int32 errorCode, CharP buffer, uint32 size);

#endif
