// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessImpl4D extends Process {

    InputStream inputStream;
    OutputStream outputStream;
    InputStream errorStream;
    int pid;

    @Override
    native public void destroy();

    @Override
    native public int exitValue();

    @Override
    public InputStream getErrorStream() {
        return this.errorStream;
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    native public int waitFor();

}