// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net;

import totalcross.sys.Vm;

/**
 * An URI represents a Uniform Resource Identifier, or an address of something in the Internet.
 *
 * @author Pierre G. Richard
 */
public class URI {
  /** The scheme represented in this URI. */
  public ByteString scheme;
  /** The authority represented in this URI. */
  public ByteString authority;
  /** The user information represented in this URI. */
  public ByteString userInfo;
  /** The host represented in this URI. */
  public ByteString host;
  /** The path represented in this URI. */
  public URI.Path path;
  /** The query stored in this URI. */
  public ByteString query;
  /** The fragment stored in this URI. */
  public ByteString fragment;
  /** The port stored in this URI. */
  public int port;

  static private final char[] noEncode = { '\u0000', '\u0000', '\u6401', '\u03ff', '\ufffe', '\u87ff', '\ufffe',
      '\u07ff' };
  static private final char toHexNibble[] = "0123456789ABCDEF".toCharArray();

  /**
   * Constructor from a String
   *
   * @param spec String that holds the specified URI
   */
  public URI(String spec) {
    final byte[] b = spec.getBytes();
    init(b, 0, b.length, null, false);
  }

  /**
   * Constructor from a String and a base URI
   *
   * @param spec String that holds the specified URI
   * @param baseURI base URI for deriving relative components
   */
  public URI(String spec, URI baseURI) {
    final byte[] b = spec.getBytes();
    init(b, 0, b.length, baseURI, false);
  }

  /**
   * Constructor from a byte array.
   *
   * @param spec byte array that holds the specified URI
   * @param start where the URI starts in the byte array
   * @param len length of the URI
   */
  public URI(byte[] spec, int start, int len) {
    init(spec, start, len, null, true);
  }

  /**
   * Constructor from a byte array, and a base URI missing components derive from.
   *
   * @param spec byte array that holds the specified URI
   * @param start where the URI starts in the byte array
   * @param len length of the URI
   * @param baseURI base URI for deriving relative components
   */
  public URI(byte[] spec, int start, int len, URI baseURI) {
    init(spec, start, len, baseURI, true);
  }

  private byte[] spec;
  private int start, len, end;
  private boolean isRelative;

  /**
   * Internal method to initialize the URI fields.
   *
   * @param _spec byte array that holds the specified URI
   * @param _start where the URI starts in the byte array
   * @param _len length of the URI
   * @param baseURI base URI for deriving relative components
   * @param meedCopy if the byte array needs to be copied
   */
  private void init(byte[] _spec, int _start, int _len, URI baseURI, boolean needCopy) {
    int i;
    start = _start;
    len = _len;
    spec = _spec;

    port = -1;

    stripURL();

    // Copy the byte array
    if (needCopy) {
      end -= start;
      byte[] temp = new byte[end];
      Vm.arrayCopy(spec, start, temp, 0, end);
      spec = temp;
      start = 0;
    }

    // Scheme: $1 in ^(([^:/?#]+):)? (RFC2396, Appendix B)
    skipPart1();

    // Find out if the spec URI is relative. This should just be: | "if ((scheme == null) && (baseURI != null))", |
    // however, if the scheme of the spec URI matches the scheme of the | base URI, and the base URI is a hierarchical
    // URI scheme, | then maintain backwards compatibility and treat it as if | the spec didn't contain the scheme;
    // see 5.2.3 of RFC2396
    if ((baseURI != null) && ((scheme == null) || ((scheme.equalsIgnoreCase(baseURI.scheme)) && (baseURI.path != null)
        && (baseURI.path.len > 0) && (baseURI.path.base[baseURI.path.pos] == '/')))) {
      scheme = ByteString.copy(baseURI.scheme);
      authority = ByteString.copy(baseURI.authority);
      userInfo = ByteString.copy(baseURI.userInfo);
      host = ByteString.copy(baseURI.host);
      path = new Path(baseURI.path);
      port = baseURI.port;
      isRelative = true;
    }

    // Strip off the fragment 
    stripFragment();

    if (isPathEmpty(baseURI)) {
      return;
    }

    // Given the logic above, this section is hit when spec is one of: | - a relative URI (no scheme) starting with
    // '/' or '?' or something else. | - an absolute URI with '/' or '?' following the scheme | Strip off the query part
    for (i = start; i < end; ++i) {
      if (spec[i] == '?') {
        query = new ByteString(i + 1, spec.length - (i + 1), spec);
        end = i;
        break;
      }
    }

    if (isAbsolute()) {
      return;
    }

    // Given the logic above, this section is hit when spec is one of: | - a relative URI (no scheme) starting with
    // '/', | - an absolute URI with '/' following the scheme | It is an absolute path. | Parse the authority part if any
    parseRelative();
    if (start == end) {
      path = new Path(0, 1, new byte[] { '/' });
    } else {
      path = new Path(start, end - start, spec);
    }
  }

  private void parseRelative() {
    int i;
    boolean b1 = (start < (end - 1));
    boolean b2 = (spec[start + 1] == '/');
    if (b1 && b2) {
      int atSignPos = -1;
      start += 2;
      for (i = start; i < end; ++i) {
        if (spec[i] == '/') {
          break;
        }
        if ((spec[i] == '@') && (atSignPos == -1)) {
          atSignPos = i;
        }
      }
      authority = new ByteString(start, i - start, spec);
      if (atSignPos != -1) {
        userInfo = new ByteString(authority.pos, atSignPos - start, spec);
        host = new ByteString(atSignPos + 1, i - (atSignPos + 1), spec);
      } else {
        host = ByteString.copy(authority);
      }
      start = i;

      /// I should check that the host.len is not zero. 

      // Strip off the port, if any 
      port = -1; // default value 
      if (host.len > 0) {
        i = 0;
        if (host.base[host.pos] == '[') {
          // If the host is surrounded by [ and ] | then it is an IPv6 literal address as specified in RFC2732.
          i = host.indexOf((byte) ']', 0);
          if ((i < 0) || ((++i < host.len) && (host.base[host.pos + i] != ':'))) {
            i = -1;
          }
        } else {
          i = host.indexOf((byte) ':', 0);
        }
        if (i >= 0) {
          // a colon was found
          int ppos = host.pos + i;
          int epos = host.pos + host.len;
          if (++ppos < epos) {
            // port can be omitted
            port = ByteString.convertToInt(host.base, ppos, epos);
          }
          host.len = i;
        }
      }
    }
  }

  private boolean isAbsolute() {
    int i;
    if ((end == start) || (spec[start] != '/')) {
      // Not a slash. | The spec is either an absolute URL with no path (just a query), | or the spec is a relative
      // URI. | Take care of the former. | Then, resolve a relative-path according to RFC2396, 5.2.6. | Try to remove
      // the last segment from the Base URI path | and recompose as said in RFC2396, 5.2.6a and 5.2.6b. | If the Base
      // URI path has only one segment (no '/'), | a leading slash is needed to end the authority. | If the Base URI
      // has no authority, then it is useless.
      if (!isRelative) {
        return true;
      } else {
        byte[] buffer = null;

        if ((path != null) && ((i = path.lastIndexOf((byte) '/')) >= 0)) {
          // inherited path
          buffer = new byte[(i + 1) + (end - start)];
          Vm.arrayCopy(path.base, path.pos, buffer, 0, i + 1);
          Vm.arrayCopy(spec, start, buffer, i + 1, end - start);
        } else if (authority != null) {
          // then inherit authority
          buffer = new byte[1 + (end - start)];
          buffer[0] = (byte) '/'; // insert a slash.
          Vm.arrayCopy(spec, start, buffer, 1, end - start);
        } else {
          // no slash and no (inherited) authority (opaque?) 
          path = new Path(start, end - start, spec);
          return true;
        }

        path = new Path(0, buffer.length, buffer);
        path.collapsePath();
        return true;
      }
    }
    return false;
  }

  private boolean isPathEmpty(URI baseURI) {
    if (start == end) {
      // The path is empty, and the authority and query are undefined. | If the scheme is also undefined, the
      // RFC2396 at 5.2.2 implies | that the query and fragment are inheritated from the baseURI | if undefined.
      if (isRelative) {
        query = ByteString.copy(baseURI.query);
        if (fragment == null) {
          fragment = ByteString.copy(baseURI.fragment);
        }
      }
      return true;
    }

    if ((!isRelative) && (spec[start] != '/') && (spec[start] != '?')) {
      // The path is in an opaque part - no query part in here
      path = new Path(start, end - start, spec);
      return true;
    }
    return false;
  }

  private void stripFragment() {
    for (int i = start; i < end; ++i) {
      if (spec[i] == '#') {
        fragment = new ByteString(i + 1, end - (i + 1), spec);
        end = i;
        break;
      }
    }
  }

  private void skipPart1() {
    for (int i = start; i < end; ++i) {
      switch (spec[i]) {
      case (byte) '/':
      case (byte) '#':
      case (byte) '?':
        break;
      case (byte) ':':
        if (i > start) {
          scheme = new ByteString(start, i - start, spec);
          start = i + 1;
        }
        break;
      default:
        continue;
      }
      break;
    }
  }

  private void stripURL() {
    // trim spaces - get rid of "url:" 
    for (end = start + len; (end > start) && (spec[end - 1] <= ' '); --end) {
      ;
    }
    for (start = 0; (start < end) && (spec[start] <= ' '); ++start) {
      ;
    }
    if (((end - start) >= 4) && (spec[start + 3] == ':') && ((spec[start] == 'u') || ((spec[start] == 'U')))
        && ((spec[start + 1] == 'r') || ((spec[start + 1] == 'R')))
        && ((spec[start + 2] == 'l') || ((spec[start + 2] == 'L')))) {
      start += 4;
    }
  }

  /**
   * Return a String representation of this URI
   *
   * @return a String representation of this URI
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(50);
    if (scheme != null) {
      sb.append(scheme.toString());
      sb.append(':');
    }
    if (authority != null) {
      sb.append("//");
      sb.append(authority.toString());
    }
    if (path != null) {
      sb.append(path.toString());
    }
    if (query != null) {
      sb.append("?");
      sb.append(query.toString());
    }
    if (fragment != null) {
      sb.append("#");
      sb.append(fragment.toString());
    }
    return sb.toString();
  }

  /**
   * Encodes to a <code>application/x-www-form-urlencoded</code>
   *
   * @param val the String to encode
   * @return the encoded String.
   */
  public static String encode(String val) {
    StringBuffer sb = new StringBuffer(val.length() * 3 / 2);
    encode(val, sb);
    return sb.toString();
  }

  /**
   * Decodes a <code>application/x-www-form-urlencoded</code> string
   *
   * @param val the String to decode
   * @return the decoded String.
   */
  public static String decode(String val) {
    StringBuffer sb = new StringBuffer(val.length() * 2 / 3);
    decode(val, sb);
    return sb.toString();
  }

  /**
   * Encodes to a <code>application/x-www-form-urlencoded</code>
   *
   * @param val the String to encode
   * @param sb the StringBuffer where to write the encoded form
   */
  public static void encode(String val, StringBuffer sb) {
    char chars[] = val.toCharArray();
    int len = chars.length;

    for (int i = 0; i < len; ++i) {
      char c = chars[i];
      if ((c < 128) && (0 != ((noEncode[c >> 4]) & (1 << (c & 0xF))))) {
        if (c == ' ') {
          c = '+';
        }
        sb.append(c);
      } else {
        // convert to UTF-8 on the fly
        if (c >= 0x80) {
          // 2 to 3 bytes
          sb.append('%');
          if (c >= 0x800) {
            // 3 bytes sequence
            int b = (0x80 | ((c >> 6) & 0x3F));
            sb.append('E');
            sb.append(toHexNibble[c >> 12]);
            sb.append('%');
            sb.append(toHexNibble[b >> 4]);
            sb.append(toHexNibble[b & 0xF]);
          } else {
            sb.append((c > 1023) ? 'D' : 'C');
            sb.append(toHexNibble[(c >> 6) & 0xF]);
          }
          c = (char) (0x80 | (c & 0x3F));
        }
        sb.append('%');
        sb.append(toHexNibble[c >> 4]);
        sb.append(toHexNibble[c & 0xF]);
      }
    }
  }

  /**
   * Decodes a <code>application/x-www-form-urlencoded</code> string
   *
   * @param val the String to decode
   * @param sb the StringBuffer where to write the decoded form
   */
  public static void decode(String val, StringBuffer sb) {
    char chars[] = val.toCharArray(); // supposed to be all < 0x80 (utf8)
    int len = chars.length;
    int i = 0;

    while (i < len) {
      char c = chars[i++];
      switch (c) {
      case '+':
        c = ' ';
        break;
      case '%': {
        // convert from UTF-8 on the fly
        boolean first = true;
        char acc = 0;
        int seq = 0;
        c = 0;
        while (i < len) {
          char tmp = chars[i++];
          if (tmp <= '9') {
            if (tmp < '0') {
              c = '\uffff';
              break;
            }
            c += (tmp & 0xF);
          } else {
            if ((tmp = (char) ((tmp & ~('a' - 'A')) - 'A')) >= (char) (16 - 10)) {
              c = '\uffff';
              break;
            }
            c += (tmp + 10);
          }
          if (first) {
            first = false;
            c <<= 4;
            continue;
          }
          if (seq == 0) {
            if (c < 0x80) {
              // if a 1 byte sequence,
              break; // success.
            }
            acc = (char) (c & 0x1F); // first (clean) byte gotten!
            seq = ((c & 0xE0) == 0xC0) ? 1 : 2; // bytes sequence
          } else {
            // seq is 1 or 2
            c ^= 0x80;
            if ((c & 0xC0) != 0) {
              // not a follower?
              c = '\uffff'; // too bad...
              break;
            }
            acc = (char) ((acc << 6) | c);
            if (seq == 1) {
              c = acc;
              break;
            }
            seq = 1;
          }
          if (chars[i++] != '%') {
            // expecting a % here
            --i;
            c = '\uffff'; // too bad...
            break;
          }
          c = 0;
          first = true;
        }
      }
        break;
      default:
        break;
      }
      sb.append(c);
    }
  }

  /** Used as the path component of this URI. */
  public static class Path extends ByteString {
    private static final byte[] sds = { (byte) '/', (byte) '.', (byte) '/' };
    private static final byte[] sdds = { (byte) '/', (byte) '.', (byte) '.', (byte) '/' };

    /**
     * Constructor
     *
     * @param start where this Path starts in the byte array
     * @param len length of this Path
     * @param spec byte array that holds this Path
     */
    Path(int pos, int len, byte[] spec) {
      super(pos, len, spec);
    }

    /**
     * Copy Constructor
     *
     * @param src Path to copy from
     */
    Path(Path src) {
      super(src.pos, src.len, src.base);
    }

    /**
     * Reduce <code>a/b/./c/d/../e</code> to <code>a/b/c/e</code>
     */
    final void collapsePath() {
      // Remove embedded /./ 
      for (int found = 0; (found = indexOf(sds, found)) >= 0;) {
        this.len -= 2;
        Vm.arrayCopy(base, pos + found + 2, base, pos + found, this.len - found);
      }

      // Remove embedded /../ 
      for (int found = 0; (found = indexOf(sdds, found)) >= 0;) {
        for (int ppos = pos + found - 1;; --ppos) {
          if (ppos < pos) {
            // do not process...
            ++found;
            break;
          }
          // be careful: "/../../" means nothing. 
          if ((base[ppos] == (byte) '/') && (base[ppos + 1] != (byte) '.')) {
            Vm.arrayCopy(base, pos + found + 3, base, ppos, this.len - (found + 3));
            ppos -= pos; // relative
            this.len -= (found - ppos + 3);
            found = ppos;
            break;
          }
        }
      }

      // Remove a possibly trailing .. or . 
      if (base[this.len + pos - 1] == (byte) '.') {
        int ppos = pos + this.len - 3;
        if ((ppos > pos) && ((base[ppos] == (byte) '/') && (base[ppos + 1] == (byte) '.'))) {
          do {
            if (base[--ppos] == (byte) '/') {
              this.len = ppos - pos + 1;
              break;
            }
          } while (ppos > pos);
        } else if ((++ppos > pos) && (base[ppos] == (byte) '/')) {
          --this.len;
        }
      }
    }
  }
}
