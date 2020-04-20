package jdkcompat.nio;

public class ByteBufferImpl extends ByteBuffer4D{

    ByteBufferImpl(byte[] array, int offset, int length) {
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

}