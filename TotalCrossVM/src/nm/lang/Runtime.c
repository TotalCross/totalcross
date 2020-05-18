
#include "Runtime.h"
#include "cpproc.h"
#include <sys/types.h>

TC_API void jlR_exec_SSs(NMParams p) {
    int fds[CPIO_EXEC_NUM_PIPES];
    TCObject cmd = p->obj[1];
    TCObject envp = p->obj[2];
    TCObject dirPath = p->obj[3];
    TCObject fileChannelIn;
    TCObject fileChannelOut;
    TCObject fileChannelErr;
    TCObject fileInputStream;
    TCObject fileOutputStream;
    TCObject fileErrInputStream;
    TCObject process;
    char** cmdArray;
    char** envpArray;
    char* filePathArray = String2CharP(dirPath);
    int cmdArrayLen;
    int envpArrayLen;
    int filePathArrayLen = strlen(filePathArray);
    char* strings;
    int i;
	int j;
    int num_strings = 0;
    char **newEnviron = NULL;
    int pipe_count = 3;
    pid_t pid = -1;
    int err;

    if(cmd != NULL)
    {
        cmdArrayLen = cmd->arrayLen;
        cmdArray = malloc(sizeof(char*) * cmdArrayLen);

        for(j = 0; j < cmdArrayLen; j++) {
            cmdArray[j] = String2CharP(*((TCObjectArray) ARRAYOBJ_START(cmd) + j));
        }
    } else { 
        cmdArrayLen = 0;
    }
    if(envp != NULL)
    {
        envpArrayLen = envp->arrayLen;
        envpArray = malloc(sizeof(char*) * envpArrayLen);
        for(j = 0; j < envpArrayLen; j++) {
            envpArray[j] = String2CharP(*((TCObjectArray) ARRAYOBJ_START(envp) + j));
        }
    } else { 
        envpArrayLen = 0;
    }

    err = cpproc_forkAndExec(cmdArray, envpArray, fds, pipe_count, &pid, filePathArray);
    if(err != 0) 
    {
        //error message
        printf("erro %d", err);
    }
    
    fileChannelIn = createObject(p->currentContext, "java.nio.channels.FileChannelImpl");
    if(fileChannelIn == NULL) { 
        //nothing here
		return;
    }
    FileChannelImpl_nfd(fileChannelIn) = fds[0];
    fileInputStream = createObject(p->currentContext, "java.io.FileInputStream");
    if(fileInputStream == NULL) {
        //nothing here(for now)
		return;
    }
    FileInputStream_fileChannel(fileInputStream) = fileChannelIn;

    fileChannelOut = createObject(p->currentContext, "java.nio.channels.FileChannelImpl");
    if(fileChannelOut == NULL) {
        //nothing here
		return;
    }
    FileChannelImpl_nfd(fileChannelOut) = fds[1];
    fileOutputStream = createObject(p->currentContext, "java.io.FileOutputStream");
    if(fileOutputStream == NULL) {
        //nothing here for now
        return;
    }
    FileOutputStream_fileChannel(fileOutputStream) = fileChannelOut;

    fileChannelErr = createObject(p->currentContext, "java.nio.channels.FileChannelImpl");
    if (fileChannelErr == NULL)
    {
        //nothing here for now
        return;
    }
    FileChannelImpl_nfd(fileChannelErr) = fds[2];
    fileErrInputStream = createObject(p->currentContext, "java.io.FileInputStream");
    if (fileErrInputStream == NULL)
    {
        //nothing here for now
        return;
    }
    FileInputStream_fileChannel(fileErrInputStream) = fileChannelErr;
    

    process = createObject(p->currentContext, "java.lang.ProcessImpl");
    ProcessImpl_inputStream(process) = fileInputStream;
    ProcessImpl_outputStream(process) = fileOutputStream;
    ProcessImpl_errorStream(process) = fileErrInputStream;
    p->retO = process;
}