// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.IOException;
import java.io.InputStream;

import jdkcompat.nio.channels.FileChannelImpl4D;


public class FileInputStream4D extends InputStream {
    FileChannelImpl4D fileChannel;

    FileInputStream4D(FileChannelImpl4D fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public int read() throws IOException {
        return fileChannel.read();
    }

    @Override
    public int read(byte[] dst) throws IOException {
        return fileChannel.read(dst, 0, dst.length);
    }

    @Override
    public int read(byte[] dsts, int offset, int length) throws IOException {
        return fileChannel.read(dsts, offset, length);
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }

    @Override
    public int available() throws IOException {
        return fileChannel.available();
    }

    
}