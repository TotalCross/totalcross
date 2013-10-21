package totalcross.lang.reflect;

public class Constructor4D implements Member4D
{
   public native Class getDeclaringClass();
   public native String getName();
   public native int getModifiers();
   public native Class[] getParameterTypes();
   public native Class[] getExceptionTypes();
   public native boolean equals(Object obj);
   public native int hashCode();
   public native String toString();
   public native Object newInstance(Object initargs[]) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException4D;
}
