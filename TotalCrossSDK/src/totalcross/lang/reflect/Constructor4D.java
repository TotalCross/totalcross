package totalcross.lang.reflect;

public class Constructor4D implements Member4D
{
   int mod;
   String name;
   Class declaringClass; // class that owns this method
   Class parameterTypes[], exceptionTypes[];

   public Class getDeclaringClass()
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
      if (!(obj instanceof Field4D))
         return false;
      Field4D f = (Field4D)obj;
      return f.mod == mod && f.name.equals(name) && f.declaringClass.getName().equals(declaringClass.getName());
   }
   
   public int hashCode()
   {
      return declaringClass.getName().hashCode() ^ name.hashCode();
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(128);
      sb.append(super.toString()).append(" ").append(Modifier4D.toString(mod)).append(" ").append(declaringClass.getName()).append(".").append(name).append(" (@").append(declaringClass.getName()).append(")");
      return sb.toString();
   }
   
   public Class[] getParameterTypes()
   {
      return parameterTypes;
   }
   
   public Class[] getExceptionTypes()
   {
      return exceptionTypes;
   }

   
   public native Object newInstance(Object initargs[]) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException4D;
}
