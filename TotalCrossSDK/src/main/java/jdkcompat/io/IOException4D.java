package jdkcompat.io;

public class IOException4D extends Exception {
  private static final long serialVersionUID = -2139950177232639948L;

  public IOException4D() {
  }

  public IOException4D(String message) {
    super(message);
  }

  public IOException4D(Throwable cause) {
    super(cause);
  }

  public IOException4D(String message, Throwable cause) {
    super(message, cause);
  }

}
