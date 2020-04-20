package jdkcompat.nio;

public abstract class ByteBuffer4D {

    byte[] array;
    int offset;
    int length;

    public ByteBuffer4D wrap(byte[] array, int offset, int length) {
        return new ByteBufferImpl(array, offset, length);
    }
}