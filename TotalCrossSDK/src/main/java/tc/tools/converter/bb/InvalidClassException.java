package tc.tools.converter.bb;

public class InvalidClassException extends RuntimeException {
  public InvalidClassException(JavaClass jc, String reason) {
    super("Class '" + jc + "' is not valid" + (reason != null ? ": " + reason : ""));
  }

  public InvalidClassException(String reason) {
    super(reason);
  }
}
