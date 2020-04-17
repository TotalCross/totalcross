package jdkcompat.lang;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public abstract class Proccess {

    boolean isAlive;

    abstract void destroy();

    public Proccess destroyForcibly() {
        return this;
    }

    abstract int exitValue();

    abstract InputStream getErrorStream();
    
    abstract InputStream getInputStream();

    abstract OutputStream getOutputStream();

    public boolean isAlive() {
        return isAlive;
    }

    abstract int waitFor();

    public boolean waitFor(long timeout, TimeUnit unit) {
        return true;
    }


}