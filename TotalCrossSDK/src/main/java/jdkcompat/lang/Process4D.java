package jdkcompat.lang;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class Process4D {

    boolean isAlive;

    public abstract void destroy();

    public Process4D destroyForcibly() {
        return this;
    }

    public abstract int exitValue();

    public abstract InputStream getErrorStream();
    
    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public boolean isAlive() {
        return isAlive;
    }

    public abstract int waitFor();

}