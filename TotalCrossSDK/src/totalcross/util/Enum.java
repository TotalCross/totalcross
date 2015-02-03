package totalcross.util;

import totalcross.sys.*;

/** Class used to simulate an enumeration.
 * <pre>
    public static class DatePrecision extends Enum 
    {
        public static final DatePrecision NONE = new DatePrecision(-1,"NONE");
        public static final DatePrecision SECONDS = new DatePrecision("SECONDS");
        public static final DatePrecision MILLISECONDS = new DatePrecision("MILLISECONDS");

        private DatePrecision(String s)
        {
           super(s);
        }

        private DatePrecision(int v, String s)
        {
           super(v,s);
        }

        public static DatePrecision getPrecision(String precision) 
        {
            return (DatePrecision)get(precision.toUpperCase());
        }
    }
 * </pre>
 */
public class Enum
{
   private static class EnumGroup
   {
      Hashtable htv2n = new Hashtable(10); // value to name
      Hashtable htn2c = new Hashtable(10); // name to Enum
      Hashtable htv2c = new Hashtable(10); // name to Enum
      int counter;
      Vector values;
   }
   
   static Hashtable htGroup = new Hashtable(10);
   private static EnumGroup getGroup(Class<?> c)
   {
      String key = c.getName();
      EnumGroup eg = (EnumGroup)htGroup.get(key);
      if (eg == null)
         htGroup.put(key, eg = new EnumGroup());
      return eg;
   }  
   
   public String name;
   public int value;
   
   protected Enum(String name)
   {
      this(Convert.MIN_INT_VALUE, name);
   }
   
   protected Enum(int value, String name)
   {
      EnumGroup eg = getGroup(getClass());
      if (value == Convert.MIN_INT_VALUE)
         value = eg.counter++;
      this.value = value;
      this.name = name;
      eg.htv2n.put(value,name);
      eg.htn2c.put(name, this);
      eg.htv2c.put(value, this);
   }
   
   protected static Enum get(Class<?> c, String name)
   {
      return (Enum)getGroup(c).htn2c.get(name);
   }
   
   protected static Enum get(Class<?> c, int value, Enum def)
   {
      Enum ret = (Enum)getGroup(c).htv2c.get(value);
      if (ret == null)
         ret = def;
      return def;
   }
   
   public static Vector values(Class<?> c)
   {
      EnumGroup eg = getGroup(c);
      return eg.values == null ? eg.values = eg.htn2c.getValues() : eg.values;
   }
}
