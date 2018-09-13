package jdkcompat.nio.charset;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashSet;
import java.util.Set;

import totalcross.sys.Convert;

public abstract class Charset4D implements Comparable<Charset> {

  private final String name;

  private final Set<String> setAliases;

  protected Charset4D(String name, String[] aliases) {
    this.name = name;

    setAliases = new HashSet<>();
    for (String alias : aliases) {
      setAliases.add(alias);
    }
  }

  public final String name() {
    return name;
  }

  public Set<String> aliases() {
    return new HashSet<>(setAliases);
  }

  /**
   * Returns a charset object for the named charset.
   * 
   * @param charsetName
   *          The name of the requested charset; may be either a canonical name
   *          or an alias
   * @return A charset object for the named charset
   * @throws IllegalArgumentException
   *           If the given charsetName is null
   * @throws UnsupportedCharsetException
   *           If no support for the named charset is available
   */
  public static Charset forName(String charsetName) throws IllegalArgumentException, UnsupportedCharsetException {
    Charset charset = Convert.charsetForName(charsetName);
    if (charset == null) {
      throw new UnsupportedCharsetException(charsetName);
    }
    return charset;
  }
}
