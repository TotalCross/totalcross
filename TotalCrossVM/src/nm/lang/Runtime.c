// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "Runtime.h"
#include "cpproc.h"
#include "errno.h"
#include <sys/types.h>



TC_API void jlR_exec_SSs(NMParams p) {
    int fds[CPIO_EXEC_NUM_PIPES];
    TCObject cmd = p->obj[1];
    TCObject envp = p->obj[2];
    TCObject dirPath = p->obj[3];
    TCObject fileInputStream;
    TCObject fileOutputStream;
    TCObject fileErrInputStream;
    TCObject process;
    char** cmdArray;
    char** envpArray;
    char* filePathArray;
    int cmdArrayLen;
    int envpArrayLen;
	int j;
    int pipe_count = 3;
    pid_t pid = -1;
    int err;
    volatile Heap heap = heapCreate();

    IF_HEAP_ERROR(heap) {
        throwException(p->currentContext, OutOfMemoryError, NULL);
        goto cleanup;
    }

    if(cmd != NULL) {
        cmdArrayLen = cmd->arrayLen;
        cmdArray = heapAlloc(heap, sizeof(char*) * (cmdArrayLen + 1));
        for(j = 0; j < cmdArrayLen; j++) {
            TCObject o = *((TCObjectArray) ARRAYOBJ_START(cmd) + j);
            char* s = heapAlloc(heap, (String_charsLen(o) + 1) * sizeof(char));
            cmdArray[j] = String2CharPBuf(o, s);
        }
		cmdArray[cmdArrayLen] = NULL;
    } else { 
        cmdArrayLen = 0;
        cmdArray = NULL;
    }
    if(envp != NULL) {
        envpArrayLen = envp->arrayLen;
        envpArray = heapAlloc(heap, sizeof(char*) * (envpArrayLen + 1));
        for(j = 0; j < envpArrayLen; j++) {
            TCObject o = *((TCObjectArray) ARRAYOBJ_START(envp) + j);
            char* s = heapAlloc(heap, (String_charsLen(o) + 1) * sizeof(char));
            envpArray[j] = String2CharPBuf(o, s);
        }
		envpArray[envpArrayLen] = NULL;
    } else { 
        envpArrayLen = 0;
        envpArray = NULL;
    }
    if(dirPath != NULL) {
        filePathArray = heapAlloc(heap, sizeof(char) * (dirPath->arrayLen + 1));
        filePathArray = String2CharPBuf(dirPath, filePathArray);
    } else {
        filePathArray = NULL;
    }

    err = cpproc_forkAndExec(cmdArray, envpArray, fds, pipe_count, &pid, filePathArray);
    if(err != 0) {
        //error message
        throwExceptionNamed(p->currentContext, "java.io.IOException", strerror(err));
        goto cleanup;
    }  
    
    process = createObject(p->currentContext, "java.lang.ProcessImpl");
    if(process == NULL) {
        goto cleanup;
    }
    
    fileInputStream = createFileStream(p->currentContext, FILE_STREAM_INPUT, fds[CPIO_EXEC_STDIN]);
    if(fileInputStream == NULL) {
		goto cleanup;
    }

    fileOutputStream = createFileStream(p->currentContext, FILE_STREAM_OUTPUT, fds[CPIO_EXEC_STDOUT]);
    if(fileOutputStream == NULL) {
        goto cleanup;
    }

    fileErrInputStream = createFileStream(p->currentContext, FILE_STREAM_INPUT, fds[CPIO_EXEC_STDERR]);
    if (fileErrInputStream == NULL){
        goto cleanup;
    }

    ProcessImpl_inputStream(process) = fileInputStream;
    ProcessImpl_outputStream(process) = fileOutputStream;
    ProcessImpl_errorStream(process) = fileErrInputStream;
    ProcessImpl_pid(process) = pid;
    p->retO = process;

cleanup:
    if(fileErrInputStream != NULL) {
        setObjectLock(fileErrInputStream, UNLOCKED);
    }
    if(fileInputStream != NULL) {
        setObjectLock(fileInputStream, UNLOCKED);
    }
    if(fileOutputStream != NULL) {
        setObjectLock(fileOutputStream, UNLOCKED);
    }
    if(process != NULL) {
        setObjectLock(process, UNLOCKED);
    }
    if(heap != NULL) {
        heapDestroy(heap);
    }
return;
}

TCObject createFileStream(Context context, const int streamType, int fd) {
    TCObject fileStream;
    TCObject fileChannel = createObject(context, "java.nio.channels.FileChannelImpl");
    if(fileChannel == NULL) {
		return NULL;
    }
    FileChannelImpl_nfd(fileChannel) = fd;
    fileStream = createObject(context, streamType == FILE_STREAM_OUTPUT ? "java.io.FileOutputStream" : "java.io.FileInputStream");
    if(fileStream == NULL) {
        setObjectLock(fileChannel, UNLOCKED);
		return NULL;
    }

    if(streamType == FILE_STREAM_OUTPUT) {
        FileOutputStream_fileChannel(fileStream) = fileChannel;
    } else {
        FileInputStream_fileChannel(fileStream) = fileChannel;
    }

    setObjectLock(fileChannel, UNLOCKED);

    return fileStream;
}