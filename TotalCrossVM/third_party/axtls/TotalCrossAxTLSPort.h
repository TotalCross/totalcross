// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TOTALCROSS_AXTLS_PORT_H
#define TOTALCROSS_AXTLS_PORT_H

#include "tcvm.h"
#include <axtls/axtls_port.h>
#include <axtls/ssl.h>

typedef struct TC_AxTLSContext {
   SSL_CTX* sslContext;
   AXTLS_PORT_HOOKS hooks;
   Context currentContext;
   TCObject socket;
   TCObject ioBuffer;
   Method socketReadWrite;
} TC_AxTLSContext, *TC_AxTLSContextP;

TC_AxTLSContextP tcAxTLSCreate(Context currentContext, uint32 options, int32 sessions);
bool tcAxTLSSetSocket(TC_AxTLSContextP context, Context currentContext, TCObject socket);
void tcAxTLSSetCurrentContext(TC_AxTLSContextP context, Context currentContext);
void tcAxTLSDestroy(TC_AxTLSContextP context);

#endif
