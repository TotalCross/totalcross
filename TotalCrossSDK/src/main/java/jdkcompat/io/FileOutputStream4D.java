// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.IOException;
import java.io.OutputStream;

import jdkcompat.nio.channels.FileChannelImpl4D;

public class FileOutputStream4D extends OutputStream {

    FileChannelImpl4D fileChannel;

    FileOutputStream4D(FileChannelImpl4D fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public void write(int b) throws IOException {
        
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        fileChannel.write(b, off, len);
    }

}