package totalcross.lang;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;

public class Integer4D extends Number4D implements Comparable<Integer4D>
{
  public static final Class<Integer> TYPE = Integer.class;
  int v;

  public Integer4D(int v)
  {
    this.v = v;
  }
  @Override
  public int intValue()
  {
    return v;
  }
  @Override
  public boolean equals(Object o)
  {
    return o != null && o instanceof Integer4D && ((Integer4D)o).v == this.v; 
  }
  @Override
  public int hashCode()
  {
    return v;
  }
  @Override
  public String toString()
  {
    return String.valueOf(v);
  }
  public static Integer4D valueOf(int i)
  {
    return new Integer4D(i);
  }

  public static Integer4D valueOf(String s) throws NumberFormatException
  {
    try
    {
      return new Integer4D(Convert.toInt(s));
    }
    catch (InvalidNumberException ine)
    {
      throw new NumberFormatException(ine.getMessage());
    }
  }
  public static int parseInt(String str) throws NumberFormatException
  {
    try
    {
      return Convert.toInt(str);
    }
    catch (InvalidNumberException ine)
    {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  public static int parseInt(String str, int radix) throws NumberFormatException
  {
    try
    {
      if (radix == 10) {
        return Convert.toInt(str);
      }
      return (int)Convert.toLong(str, radix);
    }
    catch (InvalidNumberException ine)
    {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  public static String toHexString(int i)
  {
    return Convert.unsigned2hex(i, 4);
  }
  public static String toString(int v)
  {
    return String.valueOf(v);
  }
  @Override
  public long longValue()
  {
    return v;
  }
  @Override
  public double doubleValue()
  {
    return v;
  }
  @Override
  public int compareTo(Integer4D o)
  {
    return this.v - o.v;
  }
}
