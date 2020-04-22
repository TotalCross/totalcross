package jdkcompat.lang;

import java.io.InputStream;
import java.io.OutputStream;

public class ProcessImpl4D extends Process {

    InputStream inputStream;

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int waitFor() {
        // TODO Auto-generated method stub
        return 0;
    }

}