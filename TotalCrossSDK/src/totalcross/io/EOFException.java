package totalcross.io;

/**
 * Signals that an end of file or end of stream has been reached unexpectedly during input.
 * 
 * This exception is mainly used by data input streams to signal end of stream. Note that many other input operations
 * return a special value on end of stream rather than throwing an exception.
 * 
 * @since TotalCross 1.62
 */
public class EOFException extends IOException
{
   /**
    * Constructs an EOFException with null as its error detail message.
    */
   public EOFException()
   {
      super();
   }

   /**
    * Constructs an IOException with the specified detail message.
    * 
    * @param msg
    *           the detail message.
    */
   public EOFException(String msg)
   {
      super(msg);
   }
}
