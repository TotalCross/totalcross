/******************************************************************************
 * Copyright (c) 2004 palmOne, Inc. or its subsidiaries.
 * All rights reserved.
 *****************************************************************************/

#ifndef __PALMONECAMERALIBARM_H__
#define __PALMONECAMERALIBARM_H__

#include "palmOneCameraCommon.h"

#ifdef __cplusplus
extern "C" {
#endif

extern Err CamLibOpen(void);
extern Err CamLibClose(void);
extern Err CamLibSleep(void);
extern Err CamLibWake(void);
extern Err CamLibGetVersion(UInt32 sdkVersion, UInt32 *libVersionP);
extern Err CamLibControl(CamLibControlType cmdId, void *parameterP);

#ifdef __cplusplus
}
#endif

#endif
