
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
    // Method getPath = getMethod(OBJ_CLASS(file), false, "getPath", 0);
    // TValue filePathValue = executeMethod(mainContext, getPath, file);
    // TCObject filePath = filePathValue.asObj;
    char *cmdArray = String2CharP(cmd);
    char *envpArray = String2CharP(envp);
    char *filePathArray = String2CharP(dirPath);
    int cmdArrayLen = strlen(cmdArray);
    int envpArrayLen = strlen(envpArray);
    int filePathArrayLen = strlen(filePathArray);
    char* strings;
    int i;
    int num_strings = 0;
    char **newEnviron = NULL;
    int pipe_count = 3;
    pid_t pid = -1;
    int err;

    if((strings = malloc (((cmdArrayLen + 1)
			  + (envpArray != NULL ? envpArrayLen + 1 : 0)
			  + (filePathArray !=
			     NULL ? 1 : 0)) * sizeof (*strings))) == NULL)
    {
        //error message here
    }
    for (i = 0; i < cmdArrayLen; i++)
    {
      if ((strings[num_strings++] = cmdArray[i]) == NULL)
	    {
            //done here?
        }
    }
    strings[num_strings++] = NULL;
    if (envpArray != NULL)
    {
        newEnviron = strings + num_strings;
        for (i = 0; i < envpArrayLen; i++)
        {
            if ((strings[num_strings++] = envpArray[i]) == NULL)
            {
                //done here?
            }
        }
        strings[num_strings++] = NULL;	/* terminate array with NULL */
    }
    err = cpproc_forkAndExec(strings, newEnviron, fds, pipe_count, &pid, filePathArray);
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
    FileChannelImpl_nfd(fileChannelIn) = fds[1];
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
    FileChannelImpl_nfd(fileChannelOut) = fds[2];
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
    FileChannelImpl_nfd(fileChannelErr) = fds[1];
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