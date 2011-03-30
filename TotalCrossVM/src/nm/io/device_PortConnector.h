/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: device_PortConnector.h,v 1.6 2011-01-04 13:31:16 guich Exp $

#ifndef PORT_CONNECTOR_H
#define PORT_CONNECTOR_H

#include "tcvm.h"

enum
{
   SERIAL_DEFAULT     = 0x0000,
   SERIAL_IRCOMM      = 0x1000,
   SERIAL_SIR         = 0x1001,
   SERIAL_USB         = 0x1002,
   SERIAL_BLUETOOTH   = 0x1003
};

enum
{
   SERIAL_PARITY_NONE = 0,
   SERIAL_PARITY_EVEN = 1,
   SERIAL_PARITY_ODD  = 2
};

#endif
