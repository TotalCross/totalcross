
#include "Process.h"
#include "cpproc.h"
#include "errno.h"
#include <signal.h>

TC_API void jlPI_waitFor(NMParams p) {
    TCObject process = p->obj[0];
    int status;
    pid_t pid;
    int err;
    /* Try to reap a child process, but don't block */
    err = cpproc_waitpid(ProcessImpl_pid(process), &status, &pid, 0);
    if (err == 0 && pid == 0) {
        p->retI = 0;
        return;
    }


    /* Check result from waitpid() */
    if (err != 0) {
        if (err == EINTR){
            throwExceptionNamed(p->currentContext, "java.lang.InterruptedException", NULL);
        }
        return;
    }

    /* Get exit code; for signal termination return negative signal value XXX */
    if (WIFEXITED (status)) {
        status =  WEXITSTATUS (status);
    }
    else if (WIFSIGNALED (status)) {
        status = - WTERMSIG (status);
    }
    else {
        status = 0; /* process merely stopped; ignore */
    }

    /* Done */
    p->retI = status;
}

TC_API void jlPI_exitValue(NMParams p) {
    TCObject process = p->obj[0];
    int status;
    pid_t pid;
    int err;
    /* Try to reap a child process, but don't block */
    err = cpproc_waitpid(ProcessImpl_pid(process), &status, &pid, WNOHANG);
    if (err == 0 && pid == 0) {
        throwExceptionNamed(p->currentContext, "java.lang.IllegalThreadStateException", NULL);
        return;
    }


    /* Check result from waitpid() */
    if (err != 0) {
        if (err == ECHILD || err == EINTR){
            return;
        }
        throwExceptionNamed(p->currentContext, "java.lang.InternalError", NULL);
        return;
    }

    /* Get exit code; for signal termination return negative signal value XXX */
    if (WIFEXITED (status)) {
        status =  WEXITSTATUS (status);
    }
    else if (WIFSIGNALED (status)) {
        status = - WTERMSIG (status);
    }
    else {
        status = 0; /* process merely stopped; ignore */
    }

    /* Done */
    p->retI = status;
}

TC_API void jlPI_destroy(NMParams p) {
    TCObject process = p->obj[0];
    int pid = ProcessImpl_pid(process);
    int err;
    err = cpproc_kill(pid, SIGKILL);
}
