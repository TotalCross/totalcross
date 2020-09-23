//
// Created by Panayotis on 15/9/20.
//

#include "testdbg.h"
#include <stdio.h>

#include "rvmdebug.h"

int testdbg() {
    Options options;
//    options.printDebugPort = 1;
//    options.waitForResume = 1;

    // setup the TCP channel socket and wait
    // for the debugger to connect
    if (!_rvmHookSetupTCPChannel(&options)) return 1;
    if (!_rvmHookHandshake(&options)) return 1;

    return 0;
}