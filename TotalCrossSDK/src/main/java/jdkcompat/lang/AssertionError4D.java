package jdkcompat.lang;

import java.io.Serializable;

public class AssertionError4D extends Error implements Serializable {
  private static final long serialVersionUID = -78080685734455754L;

  public AssertionError4D() {
    super();
  }

  public AssertionError4D(String message, Throwable cause) {
    super(message, cause);
  }

  public AssertionError4D(boolean detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(char detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(double detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(float detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(int detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(long detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(Object detailMessage) {
    this(String.valueOf(detailMessage));
  }

  private AssertionError4D(String message) {
    super(message);
  }

}
