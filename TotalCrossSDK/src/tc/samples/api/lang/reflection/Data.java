package tc.samples.api.lang.reflection;

public class Data
{
   public String name, address;
   public int number;
   public byte age;
   
   public Data(String name, String address, int number, byte age)
   {
      this.name = name;
      this.address = address;
      this.number = number;
      this.age = age;
   }
   public byte getAge() {return age;}
}
