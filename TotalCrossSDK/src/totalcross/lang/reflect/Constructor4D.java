package totalcross.lang.reflect;

public class Constructor4D implements Member4D
{
   int mod;
   Object nativeStruct; // TClass
   String name;
   Class<?> declaringClass; // class that owns this method
   Class<?> parameterTypes[];
   Class<?> exceptionTypes[];
   String cached;

   public Class<?> getDeclaringClass()
   {
      return declaringClass;
   }
   
   public String getName()
   {
      return name;
   }
   
   public int getModifiers()
   {
      return mod;
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof Constructor4D))
         return false;
      Constructor4D m = (Constructor4D)obj;
      if (m.mod != mod || !m.name.equals(name) || !m.declaringClass.getName().equals(declaringClass.getName()) || parameterTypes.length != m.parameterTypes.length/* || exceptionTypes.length != m.exceptionTypes.length*/)
         return false;
      for (int i =0; i < parameterTypes.length; i++) if (!parameterTypes[i].equals(m.parameterTypes[i])) return false;
      //for (int i =0; i < exceptionTypes.length; i++) if (!exceptionTypes[i].equals(m.exceptionTypes[i])) return false; - not needed
      return true;
   }
   public int hashCode()
   {
      return declaringClass.getName().hashCode() ^ name.hashCode();
   }
   
   public String toString()
   {
      if (cached != null) return cached;
      StringBuffer sb = new StringBuffer(128); // public static final void TCTestWin$TestMethod.printTest(int,short,java.lang.String,boolean,java.lang.Object,long,byte,char,double)
      sb.append(Modifier4D.toString(mod)); if (sb.length() > 0) sb.append(' ');
      sb.append(name).append('(');
      for (int i = 0, last = parameterTypes.length-1; i <= last; i++)
      {
         sb.append(Method4D.toString(parameterTypes[i]));
         if (i < last) sb.append(',');
      }
      return cached = sb.append(')').toString();
   }
   
   public Class<?>[] getParameterTypes()
   {
      return parameterTypes;
   }
   public Class<?>[] getExceptionTypes()
   {
      return exceptionTypes;
   }
   
   public native Object newInstance(Object initargs[]) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException4D;
}
