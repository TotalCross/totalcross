package jdkcompat.lang;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessImpl4D extends Process {

    InputStream inputStream;
    OutputStream outputStream;
    InputStream errorStream;

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int exitValue() {
        // TODO Auto-generated method stub
        return 0;
    }

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
    public int waitFor() {
        // TODO Auto-generated method stub
        return 0;
    }

}