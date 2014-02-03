package totalcross.lang;

/** A Float value in TotalCross is actually a Double value. */

public class Float4D extends Double4D
{
   public static final Class TYPE = Float.class;
   double v;
   
   public Float4D(double v)
   {
      super(v);
   }
   public double floatValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Float4D && ((Float4D)o).v == this.v; 
   }
}
