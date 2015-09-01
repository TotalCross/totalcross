package totalcross.io.device.gps;

/** Exception is thrown when the GPS is disabled. */
public class GPSDisabledException extends totalcross.io.IOException
{
   /** Constructs an empty Exception. */
   public GPSDisabledException()
   {
      super();
   }

   /** Constructs an exception with the given message. */
   public GPSDisabledException(String msg)
   {
      super(msg);
   }

}
