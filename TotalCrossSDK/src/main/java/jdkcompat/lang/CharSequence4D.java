package jdkcompat.lang;

public interface CharSequence4D {
  int length();
  char charAt(int index);
  CharSequence subSequence(int start, int end);
  @Override
  public String toString();
}
