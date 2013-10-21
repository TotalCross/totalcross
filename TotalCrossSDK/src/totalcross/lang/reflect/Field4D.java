package totalcross.lang.reflect;

public class Field4D
{
   public native Class getDeclaringClass();
   public native String getName();
   public native int getModifiers();
   public native Class getType();
   public native boolean equals(Object obj);
   public native int hashCode();
   public native String toString();
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
