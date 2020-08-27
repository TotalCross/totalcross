package totalcross.io.serial;

import java.io.IOException;

import totalcross.io.XStream;

public class XSerialPort extends XStream {

   /**
    * throws error on BOTH
    */
   public int baudRate(int direction) {
      return 0;
   }

   public XSerialPort set(int... set) {
      for (int i : set) {
         if ((i & BaudRate.BASE_BAUD) == BaudRate.BASE_BAUD) {
            setBaudRate(Direction.BOTH, i - BaudRate.BASE_BAUD);
         } else if ((i & DataBits.BASE_BITS) == DataBits.BASE_BITS) {
            setDataBits(i - DataBits.BASE_BITS);
         }
      }
      return this;
   }

   /**
    * baudRate must be positive
    *
    * @param direction
    * @param baudRate
    * @return
    */
   public XSerialPort setBaudRate(int direction, int baudRate) {
      return this;
   }

   public boolean	isBreakEnabled() {
      return false;
   }

   public XSerialPort setBreakEnabled(boolean set) {
      return this;
   }

   public int dataBits() {
      // [5-8]
      return 0;
   }

   public XSerialPort setDataBits(int dataBits) {
      // [5-8]
      return this;
   }

   public boolean	isDataTerminalReady() {
      return false;
   }

   public XSerialPort setDataTerminalReady(boolean set) {
      return this;
   }

   public int	flowControl() {
      return 0;
   }

   public XSerialPort	setFlowControl(int flowControl) {
      return this;
   }

   public int	parity() {
      return 0;
   }

public XSerialPort	setParity(int parity) {
   return this;
}

public boolean	isRequestToSend() {
   return false;
}

public XSerialPort	setRequestToSend(boolean set) {
   return this;
}

public int	stopBits() {
   return 0;
}

public XSerialPort	setStopBits(int stopBits) {
   return this;
}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CharSequence readLine() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] readLine(long maxSize) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long read(byte[] buf, long start, long count) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long write(byte[] buf, long start, long count) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
   
}