// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "FileChannelImpl.h"
#include "xtypes.h"
#if defined(linux) && !defined(darwin)
#include "errno.h"
#endif

TC_API void jncFCI_read(NMParams p) {
#if defined(linux) && !defined(darwin)
    TCObject fileChannel = p->obj[0];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    int32 buffer;
    int32 ret = read(fd, &buffer, 4);
    if(ret < 0) {
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        return;
    }
    p->retI = buffer;
#else
    throwExceptionNamed(p->currentContext, "java.lang.UnsupportedOperationException", "this method only works on linux");
#endif
}

TC_API void jncFCI_read_Bii(NMParams p) {
#if defined(linux) && !defined(darwin)
    TCObject fileChannel = p->obj[0];
    TCObject byteArray = p->obj[1];
    int32 offset = p->i32[0];
    int32 length = p->i32[1];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    void* arrayStart;
    int32 ret;
    if(byteArray == NULL) {
        throwNullArgumentException(p->currentContext, "b");
        return;
    }
    if(offset < 0 || length < 0 || length > (ARRAYOBJ_LEN(byteArray) + offset)) {
        throwException(p->currentContext, IndexOutOfBoundsException, NULL);
        return;
    }
    arrayStart = ARRAYOBJ_START(byteArray) + offset;
    ret = read(fd, arrayStart, length);
    if(ret < 0) {
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        return;
    }
    p->retI = ret;
#else
    throwExceptionNamed(p->currentContext, "java.lang.UnsupportedOperationException", "this method only works on linux");
#endif
}

TC_API void jncFCI_write_Bii(NMParams p) {
#if defined(linux) && !defined(darwin)
    TCObject fileChannel = p->obj[0];
    TCObject byteArray = p->obj[1];
    int32 offset = p->i32[0];
    int32 length = p->i32[1];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    void* arrayStart;
    int32 ret;
    if(byteArray == NULL) {
        throwNullArgumentException(p->currentContext, "b");
        return;
    }
    if(offset < 0 || length < 0 || length > (ARRAYOBJ_LEN(byteArray) + offset)) {
        throwException(p->currentContext, IndexOutOfBoundsException, NULL);
        return;
    }
    arrayStart = ARRAYOBJ_START(byteArray) + offset;
    ret = write(fd, arrayStart, length);
    if(ret < 0) {
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        return;
    }
    p->retI = ret;
#else
    throwExceptionNamed(p->currentContext, "java.lang.UnsupportedOperationException", "this method only works on linux");
#endif
}

TC_API void jncFCI_implCloseChannel(NMParams p) {
#if defined(linux) && !defined(darwin)
    TCObject fileChannel = p->obj[0];
    int32 fd = FileChannelImpl_nfd(fileChannel);

    if (fd != -1) {
        if (close(fd) != 0) {
            throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        }
        FileChannelImpl_nfd(fileChannel) = -1;
    }
#else
    throwExceptionNamed(p->currentContext, "java.lang.UnsupportedOperationException", "this method only works on linux");
#endif
}