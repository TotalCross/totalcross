package totalcross.io.serial;

import java.io.IOException;
import java.util.Arrays;

import totalcross.io.XStream;

public class XSerialPort extends XStream {

   private String portName;
   private int openMode;
   private int[] baudRate = new int[] { BaudRate.BAUD_9600, BaudRate.BAUD_9600 };
   private int dataBits = DataBits.DATA_8;
   private int parity = Parity.NO_PARITY;
   private int stopBits = StopBits.OneStop;
   private boolean breakEnabled;
   private boolean dataTerminalReady;
   private int flowControl;
   private boolean requestToSend;

   public XSerialPort(String portName) {
      this.portName = portName;
   }

   public String portName() {
      return portName;
   }

   public XSerialPort setPortName(String portName) {
      this.portName = portName;
      return this;
   }

   public XSerialPort open(int openMode) {
      this.openMode = openMode;
      return this;
   }

   /**
    * throws error on BOTH
    */
   public int baudRate(int direction) {
      if (direction == Direction.BOTH)
         throw new IllegalArgumentException("Argument direction must be INPUT (0) or OUPUT (1)");
      return this.baudRate[direction];
   }

   /**
    * baudRate must be positive
    *
    * @param direction
    * @param baudRate
    * @return
    */
   public XSerialPort setBaudRate(int direction, int baudRate) {
      if (baudRate <= 0)
         throw new IllegalArgumentException("Argument baudRate must be greater than 0");
      if (direction == Direction.BOTH) {
         Arrays.fill(this.baudRate, baudRate);
      } else {
         this.baudRate[direction] = baudRate;
      }
      return this;
   }

   public boolean isBreakEnabled() {
      return this.breakEnabled;
   }

   public XSerialPort setBreakEnabled(boolean breakEnabled) {
      this.breakEnabled = breakEnabled;
      return this;
   }

   public int dataBits() {
      // [5-8]
      return dataBits;
   }

   public XSerialPort setDataBits(int dataBits) {
      if (dataBits <= 5 && dataBits >= 8)
         throw new IllegalArgumentException("Argument dataBits must be 5,6,7 or 8");
      this.dataBits = dataBits;
      return this;
   }

   public boolean isDataTerminalReady() {
      return false;
   }

   public XSerialPort setDataTerminalReady(boolean set) {
      return this;
   }

   public int flowControl() {
      return 0;
   }

   public XSerialPort setFlowControl(int flowControl) {
      return this;
   }

   public int parity() {
      return this.parity;
   }

   public XSerialPort setParity(int parity) {
      this.parity = parity;
      return this;
   }

   public boolean isRequestToSend() {
      return false;
   }

   public XSerialPort setRequestToSend(boolean set) {
      return this;
   }

   public int stopBits() {
      return stopBits;
   }

   public XSerialPort setStopBits(int stopBits) {
      this.stopBits = stopBits;
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