package totalcross.lang.reflect;

public class Field4D
{
   int index;
   int mod;
   Object nativeField; // TClass
   String name;
   Class declaringClass; // class that owns this field
   Class type;

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
   public Class getType()
   {
      return type;
   }
   public boolean equals(Object obj)
   {
      if (!(obj instanceof Field4D))
         return false;
      Field4D f = (Field4D)obj;
      return f.mod == mod && f.name.equals(name) && f.type.getName().equals(type.getName()) && f.declaringClass.getName().equals(declaringClass.getName());
   }
   public int hashCode()
   {
      return declaringClass.getName().hashCode() ^ name.hashCode();
   }
   public String toString()
   {
      return super.toString()+" "+Modifier4D.toString(mod)+" "+type.getName()+"."+name+" (@"+declaringClass.getName()+")";
   }
   
   public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;
   public native void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
   public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;
   public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;
   public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;
   public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;
   public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;
   public native void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;
   public native void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;
   public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;
}
