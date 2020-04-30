// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef RADIODEVICE_H
#define RADIODEVICE_H

#include "tcvm.h"

enum
{
   WIFI        = 0,
   PHONE       = 1,
   BLUETOOTH   = 2,
};

enum
{
   RADIO_STATE_DISABLED          = 0,
   RADIO_STATE_ENABLED           = 1,
   BLUETOOTH_STATE_DISCOVERABLE  = 2,
};

#endif //RADIODEVICE_H
