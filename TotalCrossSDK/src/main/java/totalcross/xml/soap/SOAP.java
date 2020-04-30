// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml.soap;

import totalcross.io.ByteArrayStream;
import totalcross.io.CompressedByteArrayStream;
import totalcross.io.Stream;
import totalcross.net.HttpStream;
import totalcross.net.URI;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.html.EscapeHtml;
import totalcross.util.ElementNotFoundException;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;
import totalcross.util.zip.CompressedStream;
import totalcross.util.zip.GZipStream;
import totalcross.util.zip.ZLibStream;
import totalcross.xml.DumpXml;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlTokenizer;

/**
 * Used to dispatch requests to a SOAP web service. Here's a full sample:
 *
 * <pre>
 * try
 * {
 *    Convert.setDefaultConverter("UTF8"); // disable this if you don't need to support UNICODE
 *    SOAP s = new SOAP(&quot;getClientStatus&quot;, &quot;http://localhost:8080/axis/ClientHandler.jws&quot;);
 *    s.setParam(&quot;João Pedro&quot;, &quot;name&quot;);
 *    s.setParam(&quot;227887227-2&quot;, &quot;id&quot;);
 *    s.execute();
 *    String status = (String) s.getAnswer();
 *    Convert.setDefaultConverter(""); // disable this if you don't need to support UNICODE
 * }
 * catch (XmlRpcException e)
 * {
 *    e.printStackTrace();
 * }
 * </pre>
 *
 * Note that, when sending the request, the parameters are concatenated forming
 * a unique string.
 * <p>
 * Important: The response may be escaped, use use
 * totalcross.ui.html.EscapeHtml.unescape to convert it back.
 * <p>
 * If you want to use this class to connect to a C# server, you must do the
 * following changes:
 *
 * <pre>
 * SOAP.prefix = &quot;&lt;?xml version=\&quot;1.0\&quot; encoding=\&quot;utf-8\&quot;?&gt;&quot;
 *       + &quot;&lt;soap:Envelope xmlns:xsi=\&quot;http://www.w3.org/2001/XMLSchema-instance\&quot; xmlns:xsd=\&quot;http://www.w3.org/2001/XMLSchema\&quot; xmlns:soap=\&quot;http://schemas.xmlsoap.org/soap/envelope/\&quot;&gt;&quot;
 *       + &quot;&lt;soap:Body&gt;&quot;;
 * SOAP.suffix = &quot;&lt;/soap:Body&gt;&quot; + &quot;&lt;/soap:Envelope&gt;&quot;;
 * // Then, for each instance, set:
 * soapInstance.nameSpace = &quot;http://tempuri.org/&quot;;
 * </pre>
 *
 * @since SuperWaba 5.7
 */
/**
 * @author SUPERWABA
 *
 */
public class SOAP // guich@570_34
{
  /**
   * Turn this TRUE to print the xml in the console. You may also set
   * <code>HttpStream.debugHeader = true</code>.
   * Caution: don't use this on device because it increases a lot the memory usage.
   */
  public static boolean debug;
  /**
   * The SOAP request will ask the server for GZip or ZLib encoded response by default.<br>
   * To disable encoding, set this field to true.
   */
  public static boolean disableEncoding; // flsobral@tc110_72: SOAP will now ask the server for encoded content by default. (either gzip or zlib)
  private static final int STRING_TYPE = 0;
  private static final int INT_TYPE = 1;
  private static final int DOUBLE_TYPE = 2;
  private static final int FLOAT_TYPE = 3;
  private static final int BOOLEAN_TYPE = 4;
  /** The HttpStream used to retrieve the last response. */
  protected HttpStream hs; // guich@583_14

  private static IntHashtable htTypes;
  static {
    htTypes = new IntHashtable(23);
    htTypes.put("xsd:string", STRING_TYPE);
    htTypes.put("xsd:int", INT_TYPE);
    htTypes.put("xsd:double", DOUBLE_TYPE);
    htTypes.put("xsd:float", FLOAT_TYPE);
    htTypes.put("xsd:boolean", BOOLEAN_TYPE);
    htTypes.put("string", STRING_TYPE);
    htTypes.put("int", INT_TYPE);
    htTypes.put("double", DOUBLE_TYPE);
    htTypes.put("float", FLOAT_TYPE);
    htTypes.put("boolean", BOOLEAN_TYPE);
  }

  /** A flag that indicates if the SOAP connection was using either GZip or ZLib.
   * This is a ready-only flag, set during the execute method, and changing its 
   * value has no effect.
   */
  public boolean wasCompressionUsed; // guich@tc114_89
  /*
   * luciana@570_45 - Added these attributes and a constructor that only
   * receives the basic parameters (URI, method, and vector to receive the answer)
   * and initialize these attributes. Now the user can create a SOAP object,
   * sets its parameters and then call execute() to handle the SOAP request.
   * There are setParam methods that receives only the parameter value and
   * others that receives the parameter value and parameter name. This is
   * important because some servers require the names of the parameters.
   */
  public String namespace;
  public String namespaceId;
  public String uri;
  public String mtd;
  /** The open timeout for the connection. Defaults to 25 seconds. */
  public int openTimeout;
  /** The read timeout. Defaults to 60 seconds. */
  public int readTimeout;
  /** The write timeout. Defaults to 60 seconds. */
  public int writeTimeout; // guich@tc114_8
  /** An alternative tag used to identify when a tag is a answer tag. */
  public String alternativeReturnTag;

  private Object answer;
  private String errorReason;
  private int errorReasonState;
  // luciana@570_45 - holds the parameter index of the request
  protected int paramIndex;

  private static final int DEFAULT_OPEN_TIMEOUT = 25000;
  private static final int DEFAULT_READ_WRITE_TIMEOUT = 60000;
  // luciana@570_45 - used when user doesn't specify the parameter name
  private static final String DEFAULT_PARAM_NAME = "arg";

  private class ParseAnswer extends XmlTokenizer {
    private boolean isInReturn;
    private boolean isInType;
    private int type;
    private String lastTag;
    private StringBuffer sb = new StringBuffer(2048); // guich@571_12: now all references and characters are appended until an end-tag is found

    private boolean isReturn(String tag) {
      // flsobral@tc100b5: split the test in three, so we can compare alternativeReturnTag with tag before creating a new instance in lower case to compare with the default.
      if (tag == null) {
        return false;
      }
      if ((alternativeReturnTag != null && tag.endsWith(alternativeReturnTag))) {
        return true;
      }
      String lowerTag = tag.toLowerCase(); // flsobral@tc100b5_44: comparison was case sensitive.
      return (lowerTag.endsWith("return") || lowerTag.endsWith("result"));
    }

    public ParseAnswer(Stream stream) throws SyntaxException, totalcross.io.IOException {
      tokenize(stream);
    }

    @Override
    public void foundCharacter(char charFound) {
      sb.append(charFound);
    }

    @Override
    public void foundStartTagName(byte buffer[], int offset, int count) {
      String tag = lastTag = new String(Convert.charConverter.bytes2chars(buffer, offset, count)); // flsobral@tc100b5_46: all String constructors now use the CharacterConverter.bytes2chars.
      if (errorReasonState == 1 && tag.equals("faultstring")) {
        errorReasonState = 2;
      }
      if (isInReturn) {
        try {
          type = htTypes.get(tag.hashCode());
        } catch (ElementNotFoundException e) {
          type = 0;
        }
      }
      if (isReturn(tag)) {
        isInReturn = true;
      }
    }

    @Override
    public void foundEndTagName(byte buffer[], int offset, int count) {
      if (sb.length() >= 0) // flsobral@tc100: replaced > by >= so empty tags are not ignored
      {
        if (isInReturn) {
          storeAnswer();
        }
        sb.setLength(0);
      }
      String tag = new String(buffer, offset, count);
      if (isReturn(tag)) {
        isInReturn = false;
      }
    }

    @Override
    public void foundEndEmptyTag() // case <ValidateUserResult />
    {
      if (isReturn(lastTag)) {
        isInReturn = false;
      }
    }

    @Override
    public void foundAttributeName(byte buffer[], int offset, int count) {
      if (isInReturn) {
        String tag = new String(Convert.charConverter.bytes2chars(buffer, offset, count));
        if (tag.equals("xsi:type")) {
          isInType = true;
        }
      }
    }

    @Override
    public void foundAttributeValue(byte buffer[], int offset, int count, byte dlm) {
      if (isInReturn && isInType) {
        String tag = new String(Convert.charConverter.bytes2chars(buffer, offset, count));
        try {
          type = htTypes.get(tag.hashCode());
        } catch (ElementNotFoundException e) {
          type = -99999;
        }
        isInType = false;
      }
    }

    private void storeAnswer() {
      String tag = sb.toString();
      switch (type) {
      case STRING_TYPE:
      case INT_TYPE:
      case DOUBLE_TYPE:
      case FLOAT_TYPE:
      case BOOLEAN_TYPE:
        if (answer == null) {
          answer = tag;
        } else if (answer instanceof Vector) {
          ((Vector) answer).addElement(tag);
        } else {
          // otherwise, more than one answer was found; create a vector and store all items found
          Vector v = new Vector(10);
          v.addElement(answer);
          v.addElement(tag);
          answer = v;
        }
        break;
      }
    }

    @Override
    public void foundCharacterData(byte buffer[], int offset, int count) {
      if (errorReasonState == 2) // guich@582_1: get the error message
      {
        errorReasonState = 3;
        int i = offset + count;
        while (buffer[i] != '<') {
          // ok, here we'll trick the parser: find where the message ends. this avoid problems when the message has strange chars on it. it is safe, since the message is below 1k, the size of the buffer
          i++;
        }
        errorReason = new String(Convert.charConverter.bytes2chars(buffer, offset, i - offset));
      }
      if (isInReturn) {
        sb.append(new String(Convert.charConverter.bytes2chars(buffer, offset, count)));
      }
    }
  }

  /**
   * The prefix string used when sending requests. Note that it uses UTF-8, so
   * unicode characters are not supported.
   */
  public static String prefix = "<soapenv:Envelope " + "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
      + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
      + "<soapenv:Body>";
  /** The suffix string used when sending requests. */
  public static String suffix = "</soapenv:Body>" + "</soapenv:Envelope>";

  protected StringBuffer sbuf = new StringBuffer(1024);

  /**
   * Constructs a SOAP request with the given parameters. The default namespace
   * will be used, along with an open timeout of 25 seconds, and a read and write timeout
   * of 60 seconds.
   *
   * @param mtd
   *           The method you're calling.
   * @param uri
   *           The complete URI.
   */
  public SOAP(String mtd, String uri) {
    this.namespace = "http://schemas.xmlsoap.org/soap/";
    this.mtd = mtd;
    this.uri = uri;
    this.openTimeout = DEFAULT_OPEN_TIMEOUT;
    this.readTimeout = DEFAULT_READ_WRITE_TIMEOUT;
    this.writeTimeout = DEFAULT_READ_WRITE_TIMEOUT;
  }

  /**
   * Sets a parameter with the name and type specified. Important: unicode
   * characters are not accepted because the default header uses UTF-8.
   */
  public void setParam(String param, String paramName, String paramType) {
    sbuf.append('<').append(paramName).append(" xsi:type=\"xsd:").append(paramType).append("\">").append(param)
        .append("</").append(paramName).append('>');
    paramIndex++;
  }

  /**
   * Sets a string parameter in the order of the method call, identifying it as
   * <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(String param) {
    setParam(param, DEFAULT_PARAM_NAME + paramIndex, "string");
  }

  /**
   * Sets a string parameter in the order of the method call, identifying it as
   * <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(String param, String paramName) {
    setParam(param, paramName, "string");
  }

  /**
   * Sets a int parameter in the order of the method call, identifying it as
   * <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(int param) {
    setParam(Convert.toString(param), DEFAULT_PARAM_NAME + paramIndex, "int");
  }

  /**
   * Sets a int parameter in the order of the method call, identifying it as
   * <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(int param, String paramName) {
    setParam(Convert.toString(param), paramName, "int");
  }

  /**
   * Sets a double parameter in the order of the method call, identifying it as
   * <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(double param) {
    setParam(String.valueOf(param), DEFAULT_PARAM_NAME + paramIndex, "double");
  }

  /**
   * Sets a double parameter in the order of the method call, identifying it as
   * <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(double param, String paramName) {
    setParam(String.valueOf(param), paramName, "double");
  }

  /**
   * Sets a boolean parameter in the order of the method call, identifying it
   * as <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(boolean param) {
    setParam(Convert.toString(param), DEFAULT_PARAM_NAME + paramIndex, "boolean");
  }

  /**
   * Sets a boolean parameter in the order of the method call, identifying it
   * as <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(boolean param, String paramName) {
    setParam(Convert.toString(param), paramName, "boolean");
  }

  /**
   * Sets an array parameter in the order of the method call. The array type
   * will be <code>paramType</code> and the name is <code>paramName</code>. Important: unicode
   * characters are not accepted because the default header uses UTF-8.
   *
   * @param param
   * @param paramName
   * @param paramType
   */
  public void setParam(String[] param, String paramName, String paramType) {
    StringBuffer sb = sbuf;
    int len = param.length;
    sb.append('<').append(paramName).append(" soapenc:arrayType=\"xsd:").append(paramType).append("[]\" ")
        .append("xsi:type=\"soapenc:Array\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">");
    for (int i = 0; i < len; i++) {
      sb.append('<').append(paramType).append(" xsi:type=\"xsd:").append(paramType).append("\">").append(param[i])
          .append("</").append(paramType).append('>');
    }
    sb.append("</").append(paramName).append('>');
    //Vm.debug(sb.toString());
    paramIndex++;
  }

  /**
   * Sets a String array parameter in the order of the method call, identifying
   * it as <code>arg+index</code>. Important: unicode characters are not accepted because
   * the default header uses UTF-8.
   *
   * @param param
   */
  public void setParam(String[] param) {
    setParam(param, DEFAULT_PARAM_NAME + paramIndex);
  }

  /**
   * Sets a String array parameter in the order of the method call, identifying
   * it as <code>paramName</code>. Important: unicode characters are not accepted because
   * the default header uses UTF-8.
   *
   * @param param
   * @param paramName
   */
  public void setParam(String[] param, String paramName) {
    setParam(param, paramName, "string");
  }

  /**
   * Sets a int array parameter in the order of the method call, identifying it
   * as <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(int[] param) {
    setParam(param, DEFAULT_PARAM_NAME + paramIndex);
  }

  /**
   * Sets a int array parameter in the order of the method call, identifying it
   * as <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(int[] param, String paramName) {
    int len = param.length;
    String array[] = new String[len];
    for (int i = 0; i < len; i++) {
      array[i] = Convert.toString(param[i]);
    }
    setParam(array, paramName, "int");
  }

  /**
   * Sets a byte array parameter in the order of the method call, identifying it
   * as <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(byte[] param, String paramName) {
    int len = param.length;
    String array[] = new String[len];
    for (int i = 0; i < len; i++) {
      array[i] = Convert.toString(param[i]);
    }
    setParam(array, paramName, "byte");
  }

  /**
   * Sets a double array parameter in the order of the method call, identifying
   * it as <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(double[] param) {
    setParam(param, DEFAULT_PARAM_NAME + paramIndex);
  }

  /**
   * Sets a double array parameter in the order of the method call, identifying
   * it as <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(double[] param, String paramName) {
    int len = param.length;
    String array[] = new String[len];
    for (int i = 0; i < len; i++) {
      array[i] = String.valueOf(param[i]);
    }
    setParam(array, paramName, "double");
  }

  /**
   * Sets a boolean array parameter in the order of the method call,
   * identifying it as <code>arg+index</code>
   *
   * @param param
   */
  public void setParam(boolean[] param) {
    setParam(param, DEFAULT_PARAM_NAME + paramIndex);
  }

  /**
   * Sets a boolean array parameter in the order of the method call,
   * identifying it as <code>paramName</code>.
   *
   * @param param
   * @param paramName
   */
  public void setParam(boolean[] param, String paramName) {
    int len = param.length;
    String array[] = new String[len];
    for (int i = 0; i < len; i++) {
      array[i] = Convert.toString(param[i]);
    }
    setParam(array, paramName, "boolean");
  }

  /**
   * Sets an object param identifying it as <code>paramName</code>. The object fields
   * names and values must be informed as the String arrays <fieldNames> and
   * <fieldValues>. Important: unicode characters are not accepted because the
   * default header uses UTF-8.
   *
   * @param paramName
   * @param fieldNames
   * @param fieldValues
   */
  public void setObjectParam(String paramName, String[] fieldNames, String[] fieldValues) {
    StringBuffer sb = sbuf;
    int len = fieldNames.length;
    sb.append('<').append(paramName).append('>');
    for (int i = 0; i < len; i++) {
      sb.append("<").append(fieldNames[i]);
      if (fieldValues[i] == null) {
        sb.append(" xsi:nil=\"true\"/>");
      } else {
        sb.append('>').append(fieldValues[i]).append("</").append(fieldNames[i]).append('>');
      }
    }
    sb.append("</").append(paramName).append('>');
    paramIndex++;
  }

  public void setObjectArrayParam(String paramName, String[] fieldNames, Vector fieldValues) {
    StringBuffer sb = sbuf;
    int arraySize;
    if (fieldValues == null || (arraySize = fieldValues.size()) == 0) {
      sb.append('<').append(paramName).append(" xsi:nil=\"true\"/>");
    } else {
      int len = fieldNames.length;
      for (int j = 0; j < arraySize; j++) {
        sb.append('<').append(paramName).append('>');
        String[] fields = (String[]) fieldValues.items[j];
        for (int i = 0; i < len; i++) {
          sb.append("<").append(fieldNames[i]);
          if (fields[i] == null) {
            sb.append(" xsi:nil=\"true\"/>");
          } else {
            sb.append('>').append(fields[i]).append("</").append(fieldNames[i]).append('>');
          }
        }
        sb.append("</").append(paramName).append('>');
      }
    }
    paramIndex++;
  }

  /**
   * Returns the answer of the soap request. Important: the values may be
   * escaped; use totalcross.ui.html.EscapeHtml.unescape to convert it
   * back.
   *
   * @return Object
   */
  public Object getAnswer() {
    return answer;
  }

  /**
   * Creates the options that will be sent to the HttpStream. You can override
   * this method to create the options and add your own options to it before
   * returning.
   *
   * @since SuperWaba 5.83.
   */
  protected HttpStream.Options createOptions() // guich@583_14
  {
    HttpStream.Options options = new HttpStream.Options();
    options.setCharsetEncoding(HttpStream.Options.CHARSET_UTF8);
    return options;
  }

  /** HttpStream.Options used by this SOAP connection. */
  private HttpStream.Options httpOptions;

  /**
   * Set the proxy settings to be used by this SOAP connection. You may optionally set the username and password for
   * basic proxy authorization.<br>
   * Proxy authorization is disabled if either username or password is null.
   * 
   * @param address
   *           the proxy address
   * @param port
   *           the proxy port
   * @param username
   *           the username for basic proxy authorization. Passing a null value disables proxy authorization.
   * @param password
   *           the password for basic proxy authorization. Passing a null value disables proxy authorization.
   * 
   * @since TotalCross 1.27
   */
  public void useProxy(String address, int port, String username, String password) {
    httpOptions = createOptions();
    httpOptions.proxyAddress = address;
    httpOptions.proxyPort = port;
    httpOptions.setBasicProxyAuthorization(username, password);
  }

  /**
   * This method must be called to execute the soap request
   *
   * @throws SOAPException
   */
  public void execute() throws SOAPException {
    try {
      answer = null;
      paramIndex = 0;
      if (httpOptions == null) {
        httpOptions = createOptions();
      }
      httpOptions.readTimeOut = readTimeout;
      httpOptions.openTimeOut = openTimeout;
      httpOptions.writeTimeOut = writeTimeout;
      httpOptions.httpType = HttpStream.POST; //doPost = true;
      if (!disableEncoding) {
        httpOptions.postHeaders.put("Accept-Encoding", "deflate;q=1.0, gzip;q=0.5"); // flsobral@tc110_77: zlib encoding is preferred over gzip encoding.
      }
      httpOptions.postHeaders.put("Content-Type", "text/xml; charset=utf-8");
      httpOptions.postHeaders.put("SOAPAction", "\"" + namespace + (!namespace.endsWith("/") ? "/" : "") + mtd + "\""); // flsobral@tc100b5_48: only add a trailing slash if the namespace does not have one already.
      httpOptions.postPrefix = "<?xml version=\"1.0\" encoding=\"" + httpOptions.getCharsetEncoding() + "\"?>" + prefix
          + (namespaceId == null ? "<" + mtd + " xmlns=\"" + namespace + "\">"
              : "<" + namespaceId + ":" + mtd + " xmlns:" + namespaceId + "=\"" + namespace + "\">"); // guich@tc123_39: don't concatenate the args with the prefix and suffix
      httpOptions.postDataSB = sbuf;
      httpOptions.postSuffix = namespaceId == null ? "</" + mtd + ">" + suffix
          : "</" + namespaceId + ":" + mtd + ">" + suffix;
      if (debug) {
        Vm.debug("post: " + httpOptions.postPrefix + httpOptions.postDataSB + httpOptions.postSuffix);
      }
      hs = new HttpStream(new URI(uri), httpOptions);
      boolean ok = hs.isOk();
      if (!ok) {
        if (hs.responseCode == 500) {
          errorReasonState = 1; // find it in next parse
        } else {
          throw new SOAPException("Error on HttpStream: " + hs.getStatus());
        }
      }
      Stream receivedStream;
      int initialSize = hs.contentLength > 0 ? hs.contentLength : 1024;
      if (hs.contentEncoding != null) {
        wasCompressionUsed = false;
        if (hs.contentEncoding.equalsIgnoreCase("deflate")) {
          ZLibStream zs = new ZLibStream(hs, CompressedStream.INFLATE);
          receivedStream = zs;
          wasCompressionUsed = true;
        } else if (hs.contentEncoding.equalsIgnoreCase("gzip")) {
          GZipStream zs = new GZipStream(hs, CompressedStream.INFLATE); // flsobral@tc112_35: Better performance with GZipStream instead of GZip.
          receivedStream = zs;
          wasCompressionUsed = true;
        } else {
          throw new SOAPException("Unsupported encoding: " + hs.contentEncoding);
        }
      } else {
        //flsobral@tc110_73: Use ByteArrayStream if the content is already encoded, if the length is unknown, or if the length is known and lower than 70k. (zlib requires at least 65k to run, so we'll only use it when reading more than 70k.)
        boolean useBBAS = (hs.contentLength >= -1 && hs.contentLength <= 70000) || Vm.getFreeMemory() < 1024 * 1024;
        if (useBBAS) {
          CompressedByteArrayStream bbas = new CompressedByteArrayStream();
          bbas.readFully(hs, 5, initialSize);
          receivedStream = bbas;
        } else {
          ByteArrayStream bas = new ByteArrayStream(initialSize);
          bas.readFully(hs, 5, initialSize);
          receivedStream = bas;
        }
      }
      if (debug) {
        if (receivedStream instanceof CompressedStream) {
          CompressedByteArrayStream bbas = new CompressedByteArrayStream();
          bbas.readFully(receivedStream, 5, initialSize);
          receivedStream.close();
          receivedStream = bbas;
        }
        new DumpXml(receivedStream);
        if (receivedStream instanceof ByteArrayStream) {
          ((ByteArrayStream) receivedStream).mark(); // flsobral@tc110_73: use mark instead of reset.
        } else {
          ((CompressedByteArrayStream) receivedStream).setMode(CompressedByteArrayStream.READ_MODE); // reset the buffer so it can be re-read
        }
      }
      ParseAnswer pa = new ParseAnswer(receivedStream);
      receivedStream.close();
      if (!ok && hs.responseCode == 500) {
        throw new SOAPException("Error 500 on HttpStream. Reason: "
            + (errorReasonState == 3 ? EscapeHtml.unescape(errorReason) : "could not detect error in response")); // guich@tc114_9: unescape the error message
      }
      if (answer instanceof Vector) {
        Vector v = (Vector) answer;
        int len = v.size();
        switch (pa.type)
        // if this is an array of a known type
        {
        case STRING_TYPE:
          String[] stringArray = new String[len];
          v.copyInto(stringArray);
          answer = stringArray;
          break;
        case INT_TYPE:
          int[] intArray = new int[len];
          for (int i = 0; i < len; i++) {
            intArray[i] = Convert.toInt((String) v.items[i]);
          }
          answer = intArray;
          break;
        case DOUBLE_TYPE:
        case FLOAT_TYPE:
          double[] doubleArray = new double[len];
          for (int i = 0; i < len; i++) {
            doubleArray[i] = Convert.toDouble((String) v.items[i]);
          }
          answer = doubleArray;
          break;
        case BOOLEAN_TYPE:
          boolean[] booleanArray = new boolean[len];
          for (int i = 0; i < len; i++) {
            booleanArray[i] = "true".equals(v.items[i]);
          }
          answer = booleanArray;
          break;
        }
      }
      if (hs != null) {
        hs.close();
        hs = null;
      }
    } catch (Exception e) {
      if (e instanceof SOAPException) {
        throw (SOAPException) e;
      }
      throw new SOAPException(e);
    } finally {
      sbuf.setLength(0);
    }
  }
}
