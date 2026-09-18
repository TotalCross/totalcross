// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import totalcross.sys.VmStandardOutputStream;

class PrintStream4DTest {
  @Test
  void printsSupportedValuesAndUtf8Text() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream4D stream = new PrintStream4D(bytes);

    stream.print(true);
    stream.print(' ');
    stream.print(12);
    stream.print(' ');
    stream.print(34L);
    stream.print(' ');
    stream.print(1.5f);
    stream.print(' ');
    stream.print(2.5d);
    stream.print(' ');
    stream.print(new char[] { 'x', 'y' });
    stream.print(' ');
    stream.print("café");
    stream.print(' ');
    stream.print((Object) null);

    assertEquals("true 12 34 1.5 2.5 xy café null", text(bytes));
  }

  @Test
  void genericStreamsRetainPlatformDefaultEncoding() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream4D stream = new PrintStream4D(bytes);

    stream.print("café");

    assertArrayEquals("café".getBytes(), bytes.toByteArray());
  }

  @Test
  void standardBridgeUsesUtf8Encoding() throws Exception {
    PrintStream4D stream = new PrintStream4D(new VmStandardOutputStream(VmStandardOutputStream.OUT));
    Method encode = PrintStream4D.class.getDeclaredMethod("encode", String.class);
    encode.setAccessible(true);

    assertArrayEquals("café".getBytes(StandardCharsets.UTF_8),
        (byte[]) encode.invoke(stream, "café"));
  }

  @Test
  void printlnOverloadsAndNullValuesWriteOneNewlineEach() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream4D stream = new PrintStream4D(bytes);

    stream.println();
    stream.println(false);
    stream.println('c');
    stream.println(1);
    stream.println(2L);
    stream.println(3.0f);
    stream.println(4.0d);
    stream.println(new char[] { 'a', 'b' });
    stream.println("text");
    stream.println((String) null);
    stream.println((Object) null);

    assertEquals("\nfalse\nc\n1\n2\n3.0\n4.0\nab\ntext\nnull\nnull\n", text(bytes));
  }

  @Test
  void charArrayNullMatchesPrintStreamContract() {
    PrintStream4D stream = new PrintStream4D(new ByteArrayOutputStream());

    assertThrows(NullPointerException.class, () -> stream.print((char[]) null));
    assertThrows(NullPointerException.class, () -> stream.println((char[]) null));
  }

  @Test
  void printlnUsesOneUnderlyingWriteForEachRecord() {
    RecordingOutputStream output = new RecordingOutputStream();
    PrintStream4D stream = new PrintStream4D(output);

    stream.println("atomic");
    stream.println(7);
    stream.println();

    assertEquals(3, output.writeCalls);
    assertEquals("atomic\n7\n\n", text(output));
  }

  @Test
  void appendSupportsNullAndRanges() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream4D stream = new PrintStream4D(bytes);

    assertSame(stream, stream.append((CharSequence) null));
    assertSame(stream, stream.append("abcdef", 1, 4));
    assertSame(stream, stream.append('!'));

    assertEquals("nullbcd!", text(bytes));
    assertThrows(IndexOutOfBoundsException.class, () -> stream.append("abc", 2, 1));
  }

  @Test
  void byteWritesSupportSlicesAndWriteBytes() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream4D stream = new PrintStream4D(bytes);
    byte[] input = "012345".getBytes(StandardCharsets.US_ASCII);

    stream.write(input, 1, 3);
    stream.writeBytes(input);

    assertArrayEquals("123012345".getBytes(StandardCharsets.US_ASCII), bytes.toByteArray());
    assertThrows(IndexOutOfBoundsException.class, () -> stream.write(input, 4, 3));
    assertThrows(NullPointerException.class, () -> stream.write(null, 0, 0));
  }

  @Test
  void autoFlushesOnNewlineAndExplicitFlushTracksCalls() {
    FlushTrackingOutputStream output = new FlushTrackingOutputStream();
    PrintStream4D stream = new PrintStream4D(output, true);

    stream.print("partial");
    assertEquals(0, output.flushes);
    stream.println("line");
    assertEquals(1, output.flushes);
    stream.flush();
    assertEquals(2, output.flushes);
  }

  @Test
  void failuresSetErrorAndCloseIsIdempotent() {
    FailingOutputStream output = new FailingOutputStream();
    PrintStream4D stream = new PrintStream4D(output);

    stream.print("fails");
    assertTrue(stream.checkError());
    stream.close();
    stream.close();
    stream.println("after close");
    assertTrue(stream.checkError());
    assertEquals(1, output.closes);
  }

  @Test
  void closeFlushesOnceAndWritingAfterCloseSetsError() {
    FlushTrackingOutputStream output = new FlushTrackingOutputStream();
    PrintStream4D stream = new PrintStream4D(output);

    stream.print("pending");
    stream.close();
    stream.close();
    assertEquals(1, output.flushes);
    assertFalse(stream.checkError());

    stream.print("ignored");
    assertTrue(stream.checkError());
    assertEquals("pending", text(output));
  }

  @Test
  void flushingAfterCloseSetsError() {
    TestPrintStream stream = new TestPrintStream(new ByteArrayOutputStream());

    stream.close();
    stream.resetError();
    stream.flush();

    assertTrue(stream.checkError());
  }

  @Test
  void clearAndSetErrorRemainProtectedHooks() {
    TestPrintStream stream = new TestPrintStream(new ByteArrayOutputStream());

    assertFalse(stream.checkError());
    stream.markError();
    assertTrue(stream.checkError());
    stream.resetError();
    assertFalse(stream.checkError());
  }

  @Test
  void checkErrorIncludesUnderlyingPrintStreamTrouble() {
    java.io.PrintStream underlying = new java.io.PrintStream(new FailingOutputStream());
    underlying.print("fails");
    assertTrue(underlying.checkError());

    PrintStream4D stream = new PrintStream4D(underlying);
    assertTrue(stream.checkError());
  }

  @Test
  void closedCheckErrorIgnoresUnderlyingPrintStreamTrouble() {
    java.io.PrintStream underlying = new java.io.PrintStream(new FailingOutputStream());
    underlying.print("fails");
    assertTrue(underlying.checkError());

    TestPrintStream stream = new TestPrintStream(underlying);
    stream.close();
    stream.resetError();

    assertFalse(stream.checkError());
  }

  @Test
  void constructorRejectsNullOutput() {
    assertThrows(NullPointerException.class, () -> new PrintStream4D((OutputStream) null));
  }

  private static String text(ByteArrayOutputStream bytes) {
    return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
  }

  private static final class FlushTrackingOutputStream extends ByteArrayOutputStream {
    private int flushes;

    @Override
    public void flush() throws IOException {
      flushes++;
      super.flush();
    }
  }

  private static final class RecordingOutputStream extends ByteArrayOutputStream {
    private int writeCalls;

    @Override
    public void write(byte[] bytes, int offset, int length) {
      writeCalls++;
      super.write(bytes, offset, length);
    }

    @Override
    public void write(int value) {
      writeCalls++;
      super.write(value);
    }
  }

  private static final class FailingOutputStream extends OutputStream {
    private int closes;

    @Override
    public void write(int b) throws IOException {
      throw new IOException("write failure");
    }

    @Override
    public void close() throws IOException {
      closes++;
    }
  }

  private static final class TestPrintStream extends PrintStream4D {
    private TestPrintStream(OutputStream out) {
      super(out);
    }

    private void markError() {
      setError();
    }

    private void resetError() {
      clearError();
    }
  }
}
