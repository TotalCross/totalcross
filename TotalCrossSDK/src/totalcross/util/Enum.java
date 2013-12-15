package totalcross.util;

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
   public String name;
   public int value;
   static Hashtable htv2n = new Hashtable(10); // value to name
   static Hashtable htn2c = new Hashtable(10); // name to Enum
   static int counter;
   static Vector values;
   
   protected Enum(String name)
   {
      this(counter++, name);
   }
   
   protected Enum(int value, String name)
   {
      this.value = value;
      this.name = name;
      htv2n.put(value,name);
      htn2c.put(name, this);
   }
   
   protected static Enum get(String name)
   {
      return (Enum)htn2c.get(name);
   }
   
   protected static Enum get(int value, Enum def)
   {
      Enum ret = (Enum)htv2n.get(value);
      if (ret == null)
         ret = def;
      return def;
   }
   
   public static Vector values()
   {
      return values == null ? values = htn2c.getValues() : values;
   }
}
