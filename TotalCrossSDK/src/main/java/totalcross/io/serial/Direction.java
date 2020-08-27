package totalcross.io.serial;

interface Direction {
      final static int BASE_DIRECTION = 0x1000000;
      public final static int INPUT = BASE_DIRECTION + 0;
      public final static int OUTPUT = BASE_DIRECTION + 1;
      public final static int BOTH = BASE_DIRECTION + 2;
   }