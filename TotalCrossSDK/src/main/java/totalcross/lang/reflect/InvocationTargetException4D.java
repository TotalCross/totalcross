package totalcross.lang.reflect;

public class InvocationTargetException4D extends ReflectiveOperationException
{
  public InvocationTargetException4D()
  {
  }
  public InvocationTargetException4D(Throwable t)
  {
    super(t.getClass().getName()+": "+t.getMessage());
  }
}
