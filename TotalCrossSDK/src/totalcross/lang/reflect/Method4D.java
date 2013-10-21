package totalcross.lang.reflect;

public class Method4D implements Member4D
{
   public native Class getDeclaringClass();
   public native String getName();
   public native int getModifiers();
   public native Class getReturnType();
   public native Class[] getParameterTypes();
   public native Class[] getExceptionTypes();
   public native boolean equals(Object obj);
   public native int hashCode();
   public native String toString();
   public native Object invoke(Object obj, Object args[]) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException4D;
}
