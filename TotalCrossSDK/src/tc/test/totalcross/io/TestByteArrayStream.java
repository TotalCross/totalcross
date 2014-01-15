package tc.test.totalcross.io;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.unit.TestCase;

public class TestByteArrayStream extends TestCase
{
	protected static byte[] bufferConst = new byte[10];


	private void testConstruct() {
		testConstruct_B();
		testConstruct_Bi();
		testConstruct_i();
	}

	private void testConstruct_i() {
		int nTests = 3;
		int values[] = new int[nTests];
		int i = 0;

		values[0] = -1;
		values[1] = 1;
		values[2] = 0;

		try {
			new ByteArrayStream(values[i]);
			fail("Constructor (" + values[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {

		}
		i++;

		try {
			new ByteArrayStream(values[i]);
		} catch (IllegalArgumentException ex) {
			fail("Constructor (" + values[i] + "), test " + (i + 1));
		}
		i++;

		try {
			new ByteArrayStream(values[i]);
		} catch (IllegalArgumentException ex) {
			fail("Constructor (" + values[i] + "), test " + (i + 1));
		}
		i++;
	}

	private void testConstruct_Bi() {
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
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		} catch (IllegalArgumentException ex) {}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
		} catch (IllegalArgumentException ex) {
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
		} catch (IllegalArgumentException ex) {
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
		} catch (IllegalArgumentException ex) {
			fail("Constructor (" + testBuff[i] + ", " + sizes[i] + "), test " + (i + 1));
		}
		i++;

		try {
			new ByteArrayStream(testBuff[i], sizes[i]);
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
			new ByteArrayStream(new byte[10]);
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

	public void testRun() {
		for (int i = 0; i  < 10; i++) {
			bufferConst[i] = (byte) i;
		}

		testConstruct(); // If passed here, all constructors work as expected
		testReadBytesAndSkip();
		testMarkAndReset();
		testClose();

	}

	public static void main(String[] args) {
		TestByteArrayStream t = new TestByteArrayStream();
		//learning = true;
		t.testRun();
		Vm.debug("assertion counter " + assertionCounter);
	}

}
