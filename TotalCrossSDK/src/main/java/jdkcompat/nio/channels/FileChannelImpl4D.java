// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileChannelImpl4D extends FileChannel {

    private int nfd = -1;

    FileChannelImpl4D(int fd) {
        this.nfd = fd;
    }

    FileChannelImpl4D() {

    }

    native public int read() throws IOException;

    public int available() {
        return 0;
    }

    @Override
    native public int read(ByteBuffer dst) throws IOException;

    native public int read(byte[] dst, int offset, int length) throws IOException;

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    
    native public int write(byte[] src, int offset, int length) throws IOException;

    @Override
    public int write(ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long position() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long size() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void force(boolean metaData) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
      public int read(ByteBuffer dst, long position) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void implCloseChannel() throws IOException {
        // TODO Auto-generated method stub

    }

}