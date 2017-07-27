package tc.test.totalcross.io;

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.sys.Vm;
import totalcross.unit.TestCase;

public class TestByteArrayStream extends TestCase
{
  protected static byte[] bufferConst = new byte[10];
  protected static byte[] bufferCopy;
  protected boolean buggousBehavior = true;

  protected void copyTheBuffer() {
    bufferCopy = new byte[bufferConst.length];
    Vm.arrayCopy(bufferConst,0,bufferCopy,0,bufferConst.length);
  }

  private void testConstruct() {
    testConstruct_B();
    testConstruct_Bi();
    testConstruct_i();
  }

  private void testConstruct_i() {
    ByteArrayStream bas;
    int nTests = 3;
    int values[] = new int[nTests];
    int i = 0;

    values[0] = -1;
    values[1] = 1;
    values[2] = 0;

    try {
      bas = new ByteArrayStream(values[i]);
      assertEquals(0, bas.getPos());
      assertEquals(values[i], bas.available());
      fail("Constructor (" + values[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {

    }
    i++;

    try {
      bas = new ByteArrayStream(values[i]);
      assertEquals(0, bas.getPos());
      assertEquals(values[i], bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (" + values[i] + "), test " + (i + 1));
    }
    i++;

    try {
      bas = new ByteArrayStream(values[i]);
      assertEquals(0, bas.getPos());
      assertEquals(values[i], bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (" + values[i] + "), test " + (i + 1));
    }
    i++;
  }

  private void testConstruct_Bi() {
    ByteArrayStream bas;
    int nTests = 10;
    int sizeBuff = 10;
    byte[] buff = new byte[sizeBuff];

    byte[][] testBuff = new byte[nTests][];
    int sizes[] = new int[nTests];
    int i;

    for (i = 0; i < nTests; i += nTests/2) {
      sizes[0 + i] = -1;
      sizes[1 + i] = 0;
      sizes[2 + i] = sizeBuff - 1;
      sizes[3 + i] = sizeBuff;
      sizes[4 + i] = sizeBuff + 1;
    }

    for (i = 0; i < nTests; i++) {
      if (i < nTests/2) {
        testBuff[i] = null;
      } else {
        testBuff[i] = buff;
      }
    }

    i = 0;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    }
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    }
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    }
    i++;

    try {
      bas = new ByteArrayStream(testBuff[i], sizes[i]);
      assertEquals(0, bas.getPos());
      assertEquals(sizes[i], bas.available());
      fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
    } catch (IllegalArgumentException ex) {}
    i++;
  }

  private void testConstruct_B() {
    try {
      new ByteArrayStream(null);
      fail("Constructor (null), test " + 1);
    } catch (IllegalArgumentException ex) {}
    try {
      ByteArrayStream bas = new ByteArrayStream(new byte[10]);
      assertEquals(0, bas.getPos());
      assertEquals(10, bas.available());
    } catch (IllegalArgumentException ex) {
      fail("Constructor (new byte[10]), test " + 2);
    }
  }

  private void testReadBytesAndSkip() {
    ByteArrayStream bas;
    byte ba[] = new byte[10];
    int bytesRead;
    int i;
    int bytesSkipped;

    for (i = 0; i < 10; i++) {
      ba[i] = (byte) (10 - i);
    }

    bas = new ByteArrayStream(bufferConst);
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(1, bas.getPos());
    assertEquals(bufferConst[0], ba[0]);

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(2, bas.getPos());
    assertEquals(bufferConst[1], ba[0]);

    bytesRead = bas.readBytes(ba, 0, 2);
    assertEquals(2, bytesRead);
    assertEquals(4, bas.getPos());
    assertEquals(bufferConst[2], ba[0]);
    assertEquals(bufferConst[3], ba[1]);

    bytesRead = bas.readBytes(ba, 4, 2);
    assertEquals(2, bytesRead);
    assertEquals(6, bas.getPos());
    assertEquals(bufferConst[4], ba[4]);
    assertEquals(bufferConst[5], ba[5]);

    bytesRead = bas.readBytes(ba, 0, 10);
    assertEquals(4, bytesRead);
    assertEquals(10, bas.getPos());
    for (i = 0; i < 4; i++) {
      assertEquals(bufferConst[6 + i], ba[i]);
    }

    bytesRead = bas.readBytes(ba, 0, 42);
    assertEquals(-1, bytesRead);
    assertEquals(10, bas.getPos());

    bas.reset();
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(1, bas.getPos());
    assertEquals(bufferConst[0], ba[0]);

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(2, bas.getPos());
    assertEquals(bufferConst[1], ba[0]);

    bas.reset();
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 10);
    assertEquals(10, bytesRead);
    assertEquals(10, bas.getPos());
    assertEquals(bufferConst, ba);

    bas.reset();
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 42);
    assertEquals(10, bytesRead);
    assertEquals(10, bas.getPos());
    assertEquals(bufferConst, ba);

    bytesRead = bas.readBytes(ba, 0, 0);
    assertEquals(0, bytesRead);
    assertEquals(10, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(-1, bytesRead);
    assertEquals(10, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 0);
    assertEquals(0, bytesRead);
    assertEquals(10, bas.getPos());

    bas.reset();
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 0);
    assertEquals(0, bytesRead);
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(1, bas.getPos());
    assertEquals(bufferConst[0], ba[0]);

    bytesRead = bas.readBytes(ba, 0, 0);
    assertEquals(0, bytesRead);
    assertEquals(1, bas.getPos());

    try {
      bytesRead = bas.readBytes(ba, 0, -10);
      fail();
    } catch (IllegalArgumentException ex) {}
    assertEquals(1, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 10);
    assertEquals(9, bytesRead);
    assertEquals(10, bas.getPos());
    for (i = 0; i < 9; i++) {
      assertEquals(bufferConst[1 + i], ba[i]);
    }

    bytesSkipped = bas.skipBytes(-20);
    assertEquals(-10, bytesSkipped);
    assertEquals(0, bas.getPos());

    bytesSkipped = bas.skipBytes(-20);
    assertEquals(0, bytesSkipped);
    assertEquals(0, bas.getPos());

    bytesSkipped = bas.skipBytes(20);
    assertEquals(10, bytesSkipped);
    assertEquals(10, bas.getPos());
    bytesRead = bas.readBytes(ba, 0, 42);
    assertEquals(-1, bytesRead);
    assertEquals(10, bas.getPos());

    bas.reset();
    assertEquals(0, bas.getPos());

    bytesSkipped = bas.skipBytes(10);
    assertEquals(10, bytesSkipped);
    assertEquals(10, bas.getPos());

    bytesSkipped = bas.skipBytes(20);
    assertEquals(0, bytesSkipped);
    assertEquals(10, bas.getPos());

    bytesSkipped = bas.skipBytes(-4);
    assertEquals(-4, bytesSkipped);
    assertEquals(6, bas.getPos());

    bytesSkipped = bas.skipBytes(-4);
    assertEquals(-4, bytesSkipped);
    assertEquals(2, bas.getPos());

    bytesSkipped = bas.skipBytes(4);
    assertEquals(4, bytesSkipped);
    assertEquals(6, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 10);
    assertEquals(4, bytesRead);
    assertEquals(10, bas.getPos());
    for (i = 0; i < 4; i++) {
      assertEquals(bufferConst[6 + i], ba[i]);
    }
  }

  public void testMarkAndReset() {
    ByteArrayStream bas;
    byte ba[] = new byte[10];
    int bytesRead;
    int i;
    int bytesSkipped;

    for (i = 0; i < 10; i++) {
      ba[i] = (byte) (10 - i);
    }

    bas = new ByteArrayStream(bufferConst);
    assertEquals(0, bas.getPos());

    bytesRead = bas.readBytes(ba, 0, 4);
    assertEquals(4, bytesRead);
    assertEquals(4, bas.getPos());
    for (i = 0; i < 4; i++) {
      assertEquals(bufferConst[i], ba[i]);
    }

    bas.mark();
    assertEquals(0, bas.getPos());
    assertEquals(4, bas.available());

    bytesRead = bas.readBytes(ba, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(1, bas.getPos());
    assertEquals(3, bas.available());
    assertEquals(bufferConst[0], ba[0]);

    bytesSkipped = bas.skipBytes(-1);
    assertEquals(-1, bytesSkipped);
    assertEquals(0, bas.getPos());
    assertEquals(4, bas.available());

    bytesRead = bas.readBytes(ba, 0, 40);
    assertEquals(4, bytesRead);
    assertEquals(4, bas.getPos());
    for (i = 0; i < 4; i++) {
      assertEquals(bufferConst[i], ba[i]);
    }

    bas.reset();
    assertEquals(0, bas.getPos());
    assertEquals(10, bas.available());
  }

  // As close does nothing, there is no thing to test
  // When behavior of close change, so this method should change
  public void testClose() {
    ByteArrayStream bas = new ByteArrayStream(bufferConst);

    bas.close();
  }

  public void testGetAndSetBufferAndToByteArray() {
    ByteArrayStream bas = new ByteArrayStream(bufferConst);
    byte ba[];
    byte br[] = new byte[1];
    int bytesRead;
    int i;

    ba = bas.getBuffer();

    assertEquals((Object)bufferConst, (Object)ba);

    ba = bas.toByteArray();
    assertEquals(0, bas.getPos());
    assertEquals(0, ba.length);

    bas.skipBytes(3);
    assertEquals(3, bas.getPos());
    ba = bas.toByteArray();
    assertEquals(3, bas.getPos());
    assertEquals(3, ba.length);
    for (i = 0; i < 3;i++) {
      assertEquals(bufferConst[i], ba[i]);
    }

    bas.skipBytes(10);
    assertEquals(10, bas.getPos());
    ba = bas.toByteArray();
    assertEquals(10, bas.getPos());
    assertEquals(bufferConst, ba);

    ba[9] = (byte) -1;
    assertNotEquals(bufferConst, ba);
    for (i = 0; i < 9;i++) {
      assertEquals(bufferConst[i], ba[i]);
    }

    bas.skipBytes(-1);
    assertEquals(9, bas.getPos());
    bytesRead = bas.readBytes(br, 0, 1);
    assertEquals(1, bytesRead);
    assertEquals(bufferConst[9], br[0]);

    try {
      bas.setBuffer(null);
      fail();
    } catch (IllegalArgumentException ex) {}

    bas.setBuffer(br);
    assertEquals(0, bas.getPos());
    assertEquals((Object) br, (Object) bas.getBuffer());
  }

  public void testSetPos() {
    ByteArrayStream bas;
    int i;
    int oldPos;

    bas = new ByteArrayStream(bufferCopy);
    for (i = -3; i < bufferCopy.length; i++) {
      oldPos = bas.getPos();
      try {
        bas.setPos(i);
        assertEquals(i, bas.getPos());
      } catch (IOException e) {
        assertLower(i, 0);
        assertEquals(oldPos, bas.getPos());
      }
    }

    try {
      bas.setPos(0);
      assertEquals(0, bas.getPos());
    } catch (IOException e) {
      fail();
    }

    int seekPos[] = new int[3];
    seekPos[0] = totalcross.io.RandomAccessStream.SEEK_SET;
    seekPos[1] = totalcross.io.RandomAccessStream.SEEK_CUR;
    seekPos[2] = totalcross.io.RandomAccessStream.SEEK_END;
    int max = bufferCopy.length * 2;
    int min = -bufferCopy.length * 2;
    int j;
    int seekUsed;
    int expectedPos;

    for (j = 0; j < seekPos.length; j++) {
      seekUsed = seekPos[j];
      for (i = min; i < max; i++) {
        oldPos = bas.getPos();
        switch (seekUsed) {
        case totalcross.io.RandomAccessStream.SEEK_SET:
          expectedPos = i;
          break;
        case totalcross.io.RandomAccessStream.SEEK_CUR:
          expectedPos = oldPos + i;
          break;
        case totalcross.io.RandomAccessStream.SEEK_END:
          if (buggousBehavior) {
            expectedPos =  bas.getPos() + bas.available() + i - 1;
          } else {
            expectedPos =  bas.getPos() + bas.available() + i;
          }
          break;
        default:
          expectedPos = 0;
        }

        try {
          bas.setPos(i, seekUsed);
          assertEquals(expectedPos, bas.getPos());
        } catch (IOException e) {
          assertLower(expectedPos, 0);
          assertEquals(oldPos, bas.getPos());
        }
      }
    }
  }

  public void testResetAndReuse() {
    int i;
    int bytesShifted;
    ByteArrayStream bas = new ByteArrayStream(bufferCopy, 5);
    assertEquals(0, bas.getPos());
    assertEquals(5, bas.available());

    try {
      bas.setPos(2);
    } catch (IOException e) {
      fail();
    }
    assertEquals(2, bas.getPos());

    bas.reset();
    assertEquals(0, bas.getPos());
    assertEquals(10, bas.available());
    assertEquals(bufferConst, bas.getBuffer());

    try {
      bas.setPos(2);
    } catch (IOException e) {
      fail();
    }
    assertEquals(2, bas.getPos());
    bytesShifted = bas.reuse();
    assertEquals(0, bas.getPos());
    assertEquals(2, bytesShifted);
    for (i = 0; i < 8; i++) {
      assertEquals(bufferConst[2 + i], bas.getBuffer()[i]);
    }
    // available does not change

    bytesShifted = bas.reuse();
    assertEquals(0, bas.getPos());
    assertEquals(0, bytesShifted);
  }

  public void testSetSize() {
    ByteArrayStream bas;
    int i;

    bas = new ByteArrayStream(bufferCopy, 5);
    assertEquals(5, bas.available());

    bas.setSize(3,  true);
    assertEquals(0, bas.getPos());
    assertEquals(5, bas.available());

    bas.skipBytes(2);
    assertEquals(2, bas.getPos());
    assertEquals(3, bas.available());

    bas.setSize(4, true);
    assertEquals(2, bas.getPos());
    assertEquals(3, bas.available());

    bas.setSize(5, true);
    assertEquals(2, bas.getPos());
    assertEquals(3, bas.available());

    try {
      bas.setPos(4);
    } catch (IOException e) {
      fail();
    }
    assertEquals(4, bas.getPos());

    bas.setSize(10, true);
    assertEquals(4, bas.getPos());
    assertEquals(6, bas.available());

    assertNotEquals(bufferCopy, bas.getBuffer());
    for (i = 0; i < 4; i++) {
      assertEquals(bufferCopy[i], bas.getBuffer()[i]);
    }

    bas = new ByteArrayStream(bufferCopy, 9);
    assertEquals(0, bas.getPos());
    assertEquals(9, bas.available());

    bas.setSize(10, false);
    assertEquals(0, bas.getPos());
    assertEquals(10, bas.available());
    assertNotEquals(bufferCopy, bas.getBuffer());
  }

  @Override
  public void testRun() {
    for (int i = 0; i  < 10; i++) {
      bufferConst[i] = (byte) i;
    }

    testConstruct(); // If passed here, all constructors work as expected
    testReadBytesAndSkip();
    testMarkAndReset();
    testClose();
    testGetAndSetBufferAndToByteArray();

    copyTheBuffer();
    testSetPos();

    copyTheBuffer();
    testResetAndReuse();

    copyTheBuffer();
    testSetSize();
  }

  public static void main(String[] args) {
    TestByteArrayStream t = new TestByteArrayStream();
    //learning = true;
    t.testRun();
    Vm.debug("assertion counter " + assertionCounter);
  }

}
