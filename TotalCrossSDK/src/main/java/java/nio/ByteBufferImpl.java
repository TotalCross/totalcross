package java.nio;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class ByteBufferImpl {

    byte[] array;
    int offset;
    int length;

    ByteBufferImpl(byte[] array, int offset, int length) {
        this.array = array;
        this.offset = offset;
        this.length = length;
    }
}