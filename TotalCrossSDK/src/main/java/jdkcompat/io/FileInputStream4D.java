package jdkcompat.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import jdkcompat.nio.channels.FileChannelImpl;


public class FileInputStream4D extends InputStream {
    FileChannelImpl fileChannel;

    FileInputStream4D(FileChannelImpl fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public int read() throws IOException {
        return fileChannel.read();
    }

    @Override
    public int read(byte[] dst) throws IOException {
        return fileChannel.read(ByteBuffer.wrap(dst, 0, dst.length));
    }

    @Override
    public int read(byte[] dsts, int offset, int length) throws IOException {
        return fileChannel.read(ByteBuffer.wrap(dsts, offset, length));
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