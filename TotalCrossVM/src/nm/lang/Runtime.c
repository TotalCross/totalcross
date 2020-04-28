
#include "Runtime.h"
#include "cpproc.h"
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

    if(cmd != NULL)
    {
        cmdArrayLen = cmd->arrayLen;
        cmdArray = xmalloc(sizeof(char*) * (cmdArrayLen + 1));

        for(j = 0; j < cmdArrayLen; j++) {
            cmdArray[j] = String2CharP(*((TCObjectArray) ARRAYOBJ_START(cmd) + j));
        }
		cmdArray[cmdArrayLen] = NULL;
    } else { 
        cmdArrayLen = 0;
        cmdArray = NULL;
    }
    if(envp != NULL)
    {
        envpArrayLen = envp->arrayLen;
        envpArray = xmalloc(sizeof(char*) * (envpArrayLen + 1));
        for(j = 0; j < envpArrayLen; j++) {
            envpArray[j] = String2CharP(*((TCObjectArray) ARRAYOBJ_START(envp) + j));
        }
		envpArray[envpArrayLen] = NULL;
    } else { 
        envpArrayLen = 0;
        envpArray = NULL;
    }
    if(dirPath != NULL) {
        filePathArray = String2CharP(dirPath);
    } else {
        filePathArray = NULL;
    }

    err = cpproc_forkAndExec(cmdArray, envpArray, fds, pipe_count, &pid, filePathArray);
    if(err != 0) {
        //error message
        throwException(p->currentContext, "java.io.IOException", strerror(err));
        return;
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
return;
}

TCObject* createFileStream(Context context, const int streamType, int fd) {
    TCObject* fileStream;
    TCObject* fileChannel = createObject(context, "java.nio.channels.FileChannelImpl");
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