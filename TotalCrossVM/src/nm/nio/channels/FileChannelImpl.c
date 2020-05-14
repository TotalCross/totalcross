// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "FileChannelImpl.h"
#include "xtypes.h"
#include "errno.h"

TC_API void jncFCI_read(NMParams p) {
    TCObject fileChannel = p->obj[0];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    int32 buffer;
    int32 ret = read(fd, &buffer, 4);
    p->retI = buffer;
}

TC_API void jncFCI_read_b(NMParams p) {
    TCObject fileChannel = p->obj[0];
    TCObject byteBuffer = p->obj[1];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    TCObject byteBufferArray = ByteBuffer_array(byteBuffer);
    int32 byteBufferOffset = ByteBuffer_offset(byteBuffer);
    int32 byteBufferLength = ByteBuffer_length(byteBuffer);
    void* arrayStart = ARRAYOBJ_START(byteBufferArray);
    int32 ret = read(fd, arrayStart, byteBufferLength);
    p->retI = ret;
}

TC_API void jncFCI_read_Bii(NMParams p) {
    TCObject fileChannel = p->obj[0];
    TCObject byteBuffer = p->obj[1];
    int32 offset = p->i32[0];
    int32 length = p->i32[1];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    void* arrayStart;
    int32 ret;
    if(byteBuffer == NULL) {
        throwNullArgumentException(p->currentContext, "b");
        return;
    }
    if(offset < 0 || length < 0 || length > (ARRAYOBJ_LEN(byteBuffer) + offset)) {
        throwException(p->currentContext, IndexOutOfBoundsException, NULL);
        return;
    }
    arrayStart = ARRAYOBJ_START(byteBuffer) + offset;
    ret = read(fd, arrayStart, length);
    if(ret < 0) {
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        return;
    }
    p->retI = ret;
}

TC_API void jncFCI_write_Bii(NMParams p) {
    TCObject fileChannel = p->obj[0];
    TCObject byteBuffer = p->obj[1];
    int32 offset = p->i32[0];
    int32 length = p->i32[1];
    int32 fd = FileChannelImpl_nfd(fileChannel);
    void* arrayStart;
    int32 ret;
    if(byteBuffer == NULL) {
        throwNullArgumentException(p->currentContext, "b");
        return;
    }
    if(offset < 0 || length < 0 || length > (ARRAYOBJ_LEN(byteBuffer) + offset)) {
        throwException(p->currentContext, IndexOutOfBoundsException, NULL);
        return;
    }
    arrayStart = ARRAYOBJ_START(byteBuffer) + offset;
    ret = write(fd, arrayStart, length);
    if(ret < 0) {
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(errno));
        return;
    }
    p->retI = ret;
}