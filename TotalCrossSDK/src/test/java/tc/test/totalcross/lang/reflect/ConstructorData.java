package tc.test.totalcross.lang.reflect;

public class ConstructorData {
  public String name, address;
  public int number;
  public byte age;

  public ConstructorData(String name, String address, int number, byte age) {
    this.name = name;
    this.address = address;
    this.number = number;
    this.age = age;
  }

  public byte getAge() {
    return age;
  }
}
