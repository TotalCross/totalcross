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