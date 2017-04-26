package totalcross.lang.reflect;

public interface Member4D
{
   public static final int DECLARED = 0;
   public static final int PUBLIC = 1;
   
   public abstract Class<?> getDeclaringClass();
   public abstract String getName();
   public abstract int getModifiers();
}
