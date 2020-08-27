package totalcross.io.serial;

public class BaudRate {
      static final int BASE_BAUD = 0xF000000;
      public final static int BAUD_1200	= BASE_BAUD + 1200;
      public final static int BAUD_2400	= BASE_BAUD + 2400;
      public final static int BAUD_4800	 = BASE_BAUD + 4800;
      public final static int BAUD_9600	 = BASE_BAUD + 9600;
      public final static int BAUD_19200 = BASE_BAUD + 19200;
      public final static int BAUD_38400 = BASE_BAUD + 38400;
      public final static int BAUD_57600 = BASE_BAUD + 57600;
      public final static int BAUD_115200 = BASE_BAUD + 115200;

      public static final int as(int baudRate) {
         return BASE_BAUD + baudRate;
      }
   }