package totalcross.lang.reflect;

public class InvocationTargetException4D extends Exception
{
   public InvocationTargetException4D()
   {
   }
   public InvocationTargetException4D(Throwable t)
   {
      super(t.getClass().getName()+": "+t.getMessage());
   }
}
