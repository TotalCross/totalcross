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
package tc.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Created by guich to help the implementation of native methods in the TotalCross vm. */

public class NativeMethodsPrototypeGenerator {
  public static String inputFilePath;
  
  public static void main(String[] argv) {
    try {
      if (argv.length < 1) // guich@330_10
      {
        System.out.println("Format: NativeMethodsPrototypeGenerator <input-file> <testcase>");
        System.out.println("<input-file>: the file to be parsed");
        System.out.println("<testcase>: if true, creates the stubs for the test cases");
      } else {
        List<String> v = new ArrayList<String>();
        if (argv[0].equals("makeNativeHT")) {
          nativeHTSuffix = argv[1];
          nativeHTPath = argv[2];
          for (int i = 3; i < argv.length; i++) {
            v.addAll(readFile(argv[i]));
          }
          makeNativeHT = true;
        } else {
          inputFilePath = argv[0];
          v.addAll(readFile(inputFilePath));
          makeTestCases = argv.length >= 2
              && (Boolean.valueOf(argv[1]).booleanValue() || argv[1].equalsIgnoreCase("yes"));
        }
        int n = v.size();
        if (n == 0) {
          throw new Exception(argv[0] + " is an empty file");
        }

        for (int i = 0; i < n; i++) {
          String line = v.get(i);
          if (line.trim().length() == 0) {
            continue;
          }
          int i4d = line.indexOf("4D");
          if (i4d >= 0 && !Character.isLetter(line.charAt(i4d + 2))) {
            throw new Exception("Invalid signature with 4D suffix detected: " + line);
          }
          String[] parts = split(line, "|");
          parseNative(parts[0], parts[1]);
        }
        go();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String[] split(String s, String p) {
    StringTokenizer st = new StringTokenizer(s, p);
    String[] as = new String[st.countTokens()];
    for (int i = 0; i < as.length; i++) {
      as[i] = st.nextToken();
    }
    return as;
  }

  private static String CRLF = "\r\n";
  private static Vector<String> prototypes = new Vector<String>(500);
  private static Vector<String> prototypesH = new Vector<String>(500);
  private static Vector<String> testcases = new Vector<String>(500);
  private static Vector<String> iosarray = new Vector<String>(500);
  private static Hashtable<String, String> htNames = new Hashtable<String, String>(1023);
  private static int errorCount;
  private static boolean makeTestCases;
  private static boolean makeNativeHT;
  private static String nativeHTSuffix;
  private static String nativeHTPath;

  //////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////
  private static Hashtable<String, String> htTypes = new Hashtable<String, String>(31);
  static {
    htTypes.put("int[", "[I");
    htTypes.put("long[", "[J");
    htTypes.put("boolean[", "[Z");
    htTypes.put("byte[", "[B");
    htTypes.put("char[", "[C");
    htTypes.put("short[", "[S");
    htTypes.put("float[", "[F");
    htTypes.put("double[", "[D");
    htTypes.put("string[", "[Ljava/lang/String;");

    htTypes.put("int", "I");
    htTypes.put("long", "J");
    htTypes.put("boolean", "Z");
    htTypes.put("byte", "B");
    htTypes.put("char", "C");
    htTypes.put("short", "S");
    htTypes.put("float", "F");
    htTypes.put("double", "D");
    htTypes.put("string", "Ljava/lang/String;");
    htTypes.put("stringbuffer", "Ljava/lang/StringBuffer;");
    htTypes.put("object", "Ljava/lang/Object;"); // guich@200b4: include the object class
    htTypes.put("class", "Ljava/lang/Class;");
    htTypes.put("throwable", "Ljava/lang/Throwable;");
    htTypes.put("void", "V");
    //case 'L': // object
    //case '[': // array
  }

  //////////////////////////////////////////////////////////////////////////////////
  private static void createFunctionPrototype(String funcName, String originalFuncDesc) {
    String s = "";
    String sn = "TC_API void " + funcName + "(NMParams p)";
    s += "//////////////////////////////////////////////////////////////////////////";
    s += CRLF + sn + " // " + originalFuncDesc;
    s += CRLF + "{";
    s += CRLF + "}" + CRLF;

    iosarray.addElement("htPutPtr(&htNativeProcAddresses, hashCode(\"" + funcName + "\"), &" + funcName + ");\n");
    prototypes.addElement(s);
    prototypesH.addElement(CRLF + sn + ";");

    if (makeTestCases) {
      s = "";
      s += CRLF + "TESTCASE(" + funcName + ") // " + originalFuncDesc;
      s += CRLF + "{";
      s += CRLF + "   TEST_SKIP;";
      s += CRLF + "   finish: ;";
      s += CRLF + "}";
      testcases.addElement(s);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////
  /** convert the method to methodName and methodDesc;
   * it supports array parameters;
   * in objects as parameters, you MUST suply the whole class method (eg: totalcross.ui.gfx.Rect)
   * the only exceptions are String, that it recognizes alone (no need to specify the package) and the same class name, which it prepends the package.
   * specify if the method is private static or not so the skeleton of the implementation prototype can be built correctly.
   * If you need to specify the function name to avoid conflicts, use this method. Otherwise, to get the
   * function name automatically generated, use the method parseNative(className,method) defined above.
   */
  private static void parseNative(String className, String method) {
    String origMethod = method;
    if (method.indexOf("throws") >= 0) {
      method = method.substring(0, method.indexOf("throws") - 1);
    }
    StringTokenizer st = new StringTokenizer(method, " (),;]"); // DONT ADD '[' !
    String methodName = "";
    String methodDesc = "(";
    String methodRet = "";
    // parse the return type
    String tok = st.nextToken();
    while (tok.equalsIgnoreCase("private") || tok.equalsIgnoreCase("public") || tok.equalsIgnoreCase("protected")
        || tok.equalsIgnoreCase("static") || tok.equalsIgnoreCase("native") || tok.equalsIgnoreCase("final")) {
      tok = st.nextToken();
    }
    if (tok.indexOf(".") != -1) // does it have a package?
    {
      boolean isArray = false;
      if (tok.endsWith("[")) // case totalcross.ui.gfx.Rect[]
      {
        isArray = true;
        tok = new String(tok.toCharArray(), 0, tok.length() - 1);
      }
      methodRet = "L" + tok + ";";
      if (isArray) {
        methodRet = "[" + methodRet;
      }
    } else {
      methodRet = (String) htTypes.get(tok.toLowerCase());
      if (methodRet == null) {
        System.out.println(
            errorCount++ + " - unrecognized return token: " + tok + "    in " + method + " (" + className + ")");
      }
    }

    // parse the name of the method
    methodName = st.nextToken();
    if (methodName.equals("[")) // case int []getInts()
    {
      methodRet = "[" + methodRet;
      methodName = st.nextToken();
    }
    // parse the parameters
    String last = "";
    int count = 0;
    while (st.hasMoreTokens()) {
      tok = st.nextToken();
      count++;
      if (count == 1 && htTypes.get(tok.toLowerCase()) == null
          && methodName.toLowerCase().startsWith(tok.toLowerCase())) // guich@200b4: is the first parameter the class name? windowXXXXX(Window yyyy)
      {
        // get the package name
        String s = className.replace('/', '.');
        s = s.substring(0, s.lastIndexOf('.') + 1);
        tok = s + tok;
      }
      if (tok.indexOf('.') != -1) // does it have a package?
      {
        boolean isArray = false;
        if (tok.endsWith("[")) // case totalcross.ui.gfx.Rect[]
        {
          isArray = true;
          tok = new String(tok.toCharArray(), 0, tok.length() - 1);
        }
        last = "L" + tok + ";";
        if (isArray) {
          last = "[" + last;
        }
        methodDesc += last;
      } else if (tok.indexOf("[") != -1) // is (the last parameter) an array? case int []a
      {
        if (tok.indexOf("[") > 0) {
          System.out.println(
              errorCount++ + " - Please write arrays like 'char []c' instead of 'char[] c'. Error in " + method);
          break;
        }
        int len = last.length();
        // take the last param off and insert it again as array
        methodDesc = new String(methodDesc.toCharArray(), 0, methodDesc.length() - len);
        methodDesc += "[" + last;
      } else {
        String s = (String) htTypes.get(tok.toLowerCase());
        if (s != null && s.length() > 0 && !s.equals("V")) // void is valid only in return
        {
          last = s;
          methodDesc += s;
        } else if (Character.isUpperCase(tok.charAt(0))) {
          System.out.println(
              errorCount++ + " - unrecognized token: " + tok + "    in " + methodName + " (" + className + ")");
        }
      }
    }
    methodDesc += ")" + methodRet;
    // convert all . to / (for package names)
    methodDesc = methodDesc.replace('.', '/');

    // prepare the function name
    // java/lang/StringBuffer|StringBuffer append(char []str, int offset, int len)
    // -> jls_append_cii
    String functionName = createMethodSignature(className, methodName, methodDesc);

    // check if there's already another function with this signature
    String lastfn = (String) htNames.get(functionName);
    if (lastfn != null) {
      System.out.println(errorCount++ + " - duplicate function name " + functionName + ": \n   " + lastfn + "\n   "
          + className + " " + method);
    } else {
      htNames.put(functionName, className + " " + method);
    }
    createFunctionPrototype(functionName, className + " " + origMethod);
  }

  //////////////////////////////////////////////////////////////////////////////////
  // (Lsuperwaba/ext/xplat/io/search/CatalogSearch;[BII)I -> cBii
  private static String createMethodSignature(String className, String methodName, String d) {
    StringBuffer fn = new StringBuffer(32);
    appendFirstLetter(fn, split(className, "/"));
    fn.append('_');
    fn.append(methodName);
    fn.append('_');

    int n = d.length();
    boolean isUp = false;
    char c;
    for (int i = 0; i < n; i++) {
      switch (d.charAt(i)) {
      case '(':
        continue;
      case ')':
        i = n;
        continue;
      case '[': {
        isUp = true; // use upper case for arrays
        continue;
      }
      case 'L': {
        for (c = d.charAt(++i); d.charAt(i) != ';'; i++) {
          if (d.charAt(i) == '/') {
            c = d.charAt(i + 1);
          }
        }
        break;
      }
      default:
        c = d.charAt(i);
        // convert java types to tc types
        if (c == 'J') {
          c = 'L';
        } else if (c == 'Z') {
          c = 'b';
        }
      }
      fn.append(isUp ? Character.toUpperCase(c) : Character.toLowerCase(c));
      isUp = false;
    }
    if (fn.charAt(fn.length() - 1) == '_') {
      fn.setLength(fn.length() - 1);
    }
    if (fn.length() > 32) {
      fn.setLength(32);
    }
    return fn.toString();
  }

  //////////////////////////////////////////////////////////////////////////////////
  private static void appendFirstLetter(StringBuffer fn, String[] pack) {
    int i, n;
    char c;
    for (i = 0; i < pack.length; i++) // take the first letter of each name
    {
      String s = pack[i];
      if (s.indexOf('/') > 0) {
        s = s.substring(s.lastIndexOf('/') + 1);
      }
      n = s.length();
      if (i < pack.length - 1) {
        fn.append(Character.toLowerCase(s.charAt(0))); // for the middle package names, use only the first letter
      } else {
        for (int j = 0; j < n; j++) {
          c = s.charAt(j);
          if (('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')) {
            fn.append(c);
          }
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////
  private static FileOutputStream fos;

  private static void println(String s) {
    try {
      if (fos == null) {
        if (inputFilePath != null) {
          fos = new FileOutputStream(new File(new File(inputFilePath).getParentFile(), "NativeMethodsPrototypes.txt"));
        } else {
          fos = new FileOutputStream("NativeMethodsPrototypes.txt");
        }
      }
      fos.write(s.getBytes());
      //System.out.println(s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////
  private static void go() throws Exception {
    if (errorCount > 0) {
      System.out.println(
          "\nClasses that are not part of the method description must be preceded with the full name (package + classname)");
    } else {
      // ios prototypes
      if (makeNativeHT) {
        FileWriter fw = new FileWriter(new File(nativeHTPath, "nativeProcAddresses" + nativeHTSuffix + ".c"));
        fw.write("#include \"tcvm.h\"\n");
        if ("TC".equals(nativeHTSuffix)) {
          fw.write("#include \"nm/NativeMethods.h\"\n");
        } else {
          fw.write("#include \"NativeMethods.h\"\n");
        }
        fw.write("#include \"utils.h\"\n\n");

        fw.write("void fillNativeProcAddresses" + nativeHTSuffix + "()\n{\n");
        for (int i = 0; i < iosarray.size(); i++) {
          fw.write("   " + iosarray.elementAt(i).toString());
        }
        fw.write("   htPutPtr(&htNativeProcAddresses, hashCode(\"getMainContext\"), &getMainContext);\n");
        fw.write("}\n");

        fw.close();
      } else {
        // prototype headers
        for (int i = 0; i < prototypesH.size(); i++) {
          println(prototypesH.elementAt(i).toString());
        }
        println(CRLF + CRLF);

        // prototype bodies
        for (int i = 0; i < prototypes.size(); i++) {
          println(prototypes.elementAt(i).toString());
        }

        if (makeTestCases) {
          for (int i = 0; i < testcases.size(); i++) {
            println(testcases.elementAt(i).toString());
          }
        }

        if (fos != null) {
          fos.close();
        }
        System.out.println("\n\noutput sent to " + System.getProperty("user.dir") + System.getProperty("file.separator")
            + "NativeMethodsPrototypes.txt");
      }
    }
    System.exit(0);
  }

  /**
   * Read the contents of the file on the given path into a List
   * 
   * @param path
   * @return
   * @throws IOException
   */
  private static List<String> readFile(String path) throws IOException {
    List<String> list = null;
    try (Stream<String> lines = Files.lines(Paths.get(path))) {
      list = lines.collect(Collectors.<String>toList());
    }
    return list == null ? Collections.<String>emptyList() : list;
  }
}
