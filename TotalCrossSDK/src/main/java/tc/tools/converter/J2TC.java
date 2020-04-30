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
package tc.tools.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import tc.Deploy;
import tc.tools.JarClassPathLoader;
import tc.tools.converter.bytecode.BC004_iconst_1;
import tc.tools.converter.bytecode.BC005_iconst_2;
import tc.tools.converter.bytecode.BC006_iconst_3;
import tc.tools.converter.bytecode.BC018_ldc;
import tc.tools.converter.bytecode.BC019_ldc_w;
import tc.tools.converter.bytecode.BC020_ldc2_w;
import tc.tools.converter.bytecode.BC179_putstatic;
import tc.tools.converter.bytecode.BC183_invokespecial;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.ir.CFG;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaCode;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaConstantPool;
import tc.tools.converter.java.JavaException;
import tc.tools.converter.java.JavaField;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandConstant32;
import tc.tools.converter.oper.OperandConstant64;
import tc.tools.converter.oper.OperandExternal;
import tc.tools.converter.oper.OperandReg;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandStack;
import tc.tools.converter.oper.OperandSym;
import tc.tools.converter.oper.OperandSymD64;
import tc.tools.converter.oper.OperandSymO;
import tc.tools.converter.regalloc.RegAllocation;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCClassFlags;
import tc.tools.converter.tclass.TCCode;
import tc.tools.converter.tclass.TCException;
import tc.tools.converter.tclass.TCField;
import tc.tools.converter.tclass.TCFieldFlags;
import tc.tools.converter.tclass.TCInt32Field;
import tc.tools.converter.tclass.TCMethod;
import tc.tools.converter.tclass.TCMethodFlags;
import tc.tools.converter.tclass.TCObjectField;
import tc.tools.converter.tclass.TCValue64Field;
import tc.tools.deployer.DeploySettings;
import tc.tools.deployer.DeployerException;
import tc.tools.deployer.Utils;
import totalcross.crypto.cipher.AESCipher;
import totalcross.crypto.cipher.AESKey;
import totalcross.crypto.cipher.Cipher;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.DataStreamLE;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.ui.image.Image;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;
import totalcross.util.zip.TCZ;

public final class J2TC implements JConstants, TCConstants {
  public static Hashtable htAddedClasses = new Hashtable(0xFF); // will also be used to check if there are files ending with 4D
  public static Hashtable htExcludedClasses = new Hashtable(0xFF); // will also be used to check if there are files ending with 4D
  private static Hashtable htValidExtensions = new Hashtable(0xF);
  private static String totalcrossMain = "totalcross/MainClass";
  private static String totalcrossService = "totalcross/Service";
  private static String totalcrossUiMainWindow = "totalcross/ui/MainWindow";
  public static boolean dump, dumpBytecodes;
  /** The output converted TCClass */
  public TCClass converted;
  /** The bytes of the stored class */
  public byte[] bytes;
  public int origSize;
  public static Vector callForName = new Vector(4);
  public static boolean notResolvedForNameFound;
  public static String currentClass, currentMethod;

  private static ByteArrayStream tcbas = new ByteArrayStream(16000);
  private static ByteArrayStream tcbasz = new ByteArrayStream(16000);
  private static Vector vFinalVar = new Vector(4); // initialization of static final fields
  private static int nextRegIStatic = 0;
  private static int nextRegDStatic = 0;
  private static int nextRegOStatic = 0;
  private static boolean syncWarned;
  private static int allowedBytes = Deploy.FREE_MAX_SIZE;

  public J2TC(JavaClass jc) throws IOException, Exception {
    jc.className = Bytecode2TCCode.replaceTotalCrossLangToJavaLang(jc.className);
    //  if xxx is the current class, compile it if and only if not exist a class xxx4D
    boolean has4D = htAddedClasses.exists(jc.className + "4D.class");
    if (!has4D || jc.className.equals("totalcross/util/Vector") || jc.className.equals("totalcross/util/Hashtable")) {
      if (isInnerClassOfNon4DClass(jc.className) && !jc.className.equals("totalcross/util/Hashtable$Entry")) {
        return;
      }
      jc.className = Bytecode2TCCode.removeSuffix4D(jc.className);
      converted = convertJClass2TClass(jc);
      tcbas.reset();
      tcbasz.reset();
      converted.write(new DataStreamLE(tcbas));
      origSize = tcbas.getPos();
      allowedBytes -= origSize;
      if (DeploySettings.isFreeSDK && allowedBytes < 0) {
        throw new DeployerException("The free SDK allows only 100k of code.");
      }
      //if (dump) {System.out.println(jc.className); byte[] bytes = tcbas.toByteArray(); System.out.println(TCZ.toString(bytes,0,bytes.length));}
      tc.tools.converter.Storage.compressAndWrite(tcbas, new DataStream(tcbasz));
      bytes = tcbasz.toByteArray();
    } else {
      Utils.println("Replacing " + jc.className + " by its 4D");
    }
  }

  private static boolean isInnerClassOfNon4DClass(String className) // guich@tc100b5_27
  {
    // check if there are inner classes of the class that has 4D and "remove" them
    String originalName = className;
    className = Bytecode2TCCode.removeSuffix4D(className);
    boolean is4D = !originalName.equals(className);
    int dollar;
    if (!is4D && (dollar = className.indexOf('$')) >= 0) // is this not a 4D class?
    {
      String withoutDollar = className.substring(0, dollar);
      if (htAddedClasses.exists(withoutDollar + "4D.class")) {
        Utils.println("Skipping inner " + className);
        return true;
      }
    }
    return false;
  }

  //This constructor was created solely to perform the test cases of conversion Bytecode2TCCode. After that, it could be removed.
  public J2TC(JavaClass jc, boolean dummy) throws IOException, Exception {
    converted = convertJClass2TClass(jc);
    if (DeploySettings.testClass) {
      return;
    }
    tcbas.reset();
    tcbasz.reset();
    converted.write(new DataStreamLE(tcbas));
    origSize = tcbas.getPos();
    if (dump) {
      System.out.println(jc.className);
      byte[] bytes = tcbas.toByteArray();
      System.out.println(Utils.toString(bytes, 0, bytes.length));
    }
    tc.tools.converter.Storage.compressAndWrite(tcbas, new DataStream(tcbasz));
    bytes = tcbasz.toByteArray();
  }

  private static boolean isMainClassOrService(JavaClass jc) {
    if (jc.superClass.equals(totalcrossService)) {
      return DeploySettings.isService = true;
    }
    if (jc.interfaces != null) {
      for (int i = 0; i < jc.interfaces.length; i++) {
        if (totalcrossMain.equals(jc.interfaces[i])) {
          return DeploySettings.isMainClass = true;
        }
      }
    }
    return false;
  }

  private TCClass convertJClass2TClass(JavaClass jc) throws Exception {
    // don't forget to fill jc.sourceFile
    TCClass tc = new TCClass();
    currentClass = tc.sourceFile = tc.className = jc.className;
    Bytecode2TCCode.currentJClass = jc;
    Bytecode2TCCode.currentTCClass = tc;

    // convert the flags
    tc.flags = convertFlags(jc);

    // other attributes
    tc.interfaces = jc.interfaces;
    tc.superClass = jc.superClass;
    // add the class name to the cp
    GlobalConstantPool.putCls(jc.className);
    if (jc.superClass != null) {
      GlobalConstantPool.putCls(jc.superClass);
    }
    if (jc.interfaces != null) {
      for (int i = 0; i < jc.interfaces.length; i++) {
        GlobalConstantPool.putCls(jc.interfaces[i]);
      }
    }

    // clean the list of initialization of static final fields
    vFinalVar.removeAllElements();
    OperandReg.init(null, false);
    // convert the fields
    if (jc.fields != null && jc.fields.length > 0) {
      convertFields(jc, tc);
    }

    // convert the methods
    if (jc.methods != null && jc.methods.length > 0) {
      convertMethods(jc, tc);
    }

    currentClass = null;
    return tc;
  }

  private static final String APPVER_VALID = "0123456789.";

  private static void searchForProperties(ByteCode bcs[], String superClass) {
    // Settings.appCreatorId = "Flor";
    //    4    7:ldc1            #2   <String "Flor">
    //    5    9:putstatic       #14  <Field String Settings.appCreatorId>
    for (int j = 0; j < bcs.length; j++) {
      switch (bcs[j].bc) {
      case 18: // ldc
        if (DeploySettings.appTitle == null && j < 7) // guich@tc100br_14: restrict it to the very first begining of the constructor
        {
          // super("Agenda " + VERSION, TAB_ONLY_BORDER)
          //    0    0:aload_0
          //    1    1:new             #3   <Class StringBuffer>
          //    2    4:dup
          //    3    5:ldc1            #1   <String "Agenda ">
          //    4    7:invokespecial   #14  <Method void StringBuffer(String)>
          if (bcs[j + 1].bc == 183) // invoke special?
          {
            BC183_invokespecial invoke = (BC183_invokespecial) bcs[j + 1];
            String className = invoke.className;
            String signature = invoke.signature;
            if (className.equals("java/lang/StringBuffer") && signature.equals("<init>(Ljava/lang/String;)")) {
              BC018_ldc ldc = (BC018_ldc) bcs[j];
              DeploySettings.appTitle = Utils.stripNonLetters((String) ldc.val.asObj); // if the user wrote "Agenda 1.0", break at the "Agenda"
              continue;
            }
          }
          // else - commented out for classes that don't extend MainWindow directly
          // super("Agenda",TAB_ONLY_BORDER)
          //    0    0:aload_0
          //    1    1:ldc1            #1   <String "Agenda">
          //    2    3:iconst_4
          //    3    4:invokespecial   #13  <Method void MainWindow(String, byte)>
          if ((j + 2) < bcs.length && (bcs[j + 1].bc == 183 || bcs[j + 2].bc == 183)) {
            BC183_invokespecial invoke = (BC183_invokespecial) bcs[bcs[j + 2].bc == 183 ? j + 2 : j + 1];
            String className = invoke.className;
            String signature = invoke.signature;
            if ((className.equals(totalcrossUiMainWindow) || className.equals(superClass))
                && signature.startsWith("<init>(Ljava/lang/String;")) {
              BC018_ldc ldc = (BC018_ldc) bcs[j];
              DeploySettings.appTitle = Utils.stripNonLetters((String) ldc.val.asObj); // if the user wrote "Agenda 1.0", break at the "Agenda"
            }
          }
        }
        break;
      case 179: // putstatic?
      {
        BC179_putstatic p = (BC179_putstatic) bcs[j];
        if (p.className.equals("totalcross/sys/Settings")) {
          String field = p.fieldName;
          if (field.equals("resizableWindow")) {
            DeploySettings.resizableWindow = bcs[j - 1] instanceof BC004_iconst_1;
          } else if (field.equals("isFullScreen")) {
            DeploySettings.isFullScreen = bcs[j - 1] instanceof BC004_iconst_1;
          } else if (field.equals("allowBackup")) {
            DeploySettings.allowBackup = bcs[j - 1] instanceof BC004_iconst_1;
          } else if (field.equals("windowFont")) {
            if (bcs[j - 1] instanceof BC004_iconst_1) {
              DeploySettings.windowFont = Settings.WINDOWFONT_DEFAULT;
            }
          } else if (field.equals("windowSize")) {
            if (bcs[j - 1] instanceof BC004_iconst_1) {
              DeploySettings.windowSize = Settings.WINDOWSIZE_320X480;
            } else if (bcs[j - 1] instanceof BC005_iconst_2) {
              DeploySettings.windowSize = Settings.WINDOWSIZE_480X640;
            } else if (bcs[j - 1] instanceof BC006_iconst_3) {
              DeploySettings.windowSize = Settings.WINDOWSIZE_600X800;
            }
          } else if (18 <= bcs[j - 1].bc && bcs[j - 1].bc <= 20) // guich@tc113_14: supports ldc_w and lcd2_w too
          {
            String value;
            if (bcs[j - 1].bc == 18) {
              value = (String) ((BC018_ldc) bcs[j - 1]).val.asObj;
            } else if (bcs[j - 1].bc == 19) {
              value = (String) ((BC019_ldc_w) bcs[j - 1]).val.asObj;
            } else {
              value = (String) ((BC020_ldc2_w) bcs[j - 1]).val.asObj;
            }

            if (field.equals("fullScreenPlatforms")) {
              DeploySettings.fullScreenPlatforms = value;
            } else if (field.equals("applicationId")) {
              DeploySettings.applicationId = totalcross.sys.Settings.applicationId = value;
            } else if (field.equals("appVersion")) {
              for (int i = 0, n = value.length(); i < n; i++) {
                if (APPVER_VALID.indexOf(value.charAt(i)) == -1) {
                  throw new IllegalArgumentException(
                      "Settings.versionStr '" + value + "' can only have digits and a dot.");
                }
              }
              if (value.endsWith(".") && DeploySettings.appBuildNumber != -1) {
                value += DeploySettings.appBuildNumber;
              }
              DeploySettings.appVersion = totalcross.sys.Settings.appVersion = value;
            } else if (field.equals("iosCFBundleIdentifier")) {
              Settings.iosCFBundleIdentifier = value;
            } else if (field.equals("companyInfo")) {
              DeploySettings.companyInfo = value;
            } else if (field.equals("companyContact")) {
              DeploySettings.companyContact = value;
            } else if (field.equals("appCategory")) {
              totalcross.sys.Settings.appCategory = value;
            } else if (field.equals("appPackageIdentifier")) {
              totalcross.sys.Settings.appPackageIdentifier = value;
            } else if (field.equals("appPackagePublisher")) {
              totalcross.sys.Settings.appPackagePublisher = value;
            } else if (field.equals("appLocation")) {
              totalcross.sys.Settings.appLocation = value;
            } else if (field.equals("appDescription")) {
              totalcross.sys.Settings.appDescription = value;
            } else if (field.equals("activationServerURI")) {
              totalcross.sys.Settings.activationServerURI = value;
            } else if (field.equals("activationServerNamespace")) {
              totalcross.sys.Settings.activationServerNamespace = value;
            } else if (field.equals("pushTokenAndroid")) {
              totalcross.sys.Settings.pushTokenAndroid = value;
            }
          }
        }
        break;
      }
      }
    }
  }

  private static Vector instruction2TCCode(Vector vet) {
    Vector v = new Vector(vet.items.length);
    for (int i = 0; i < vet.size(); i++) {
      Instruction inst = (Instruction) vet.items[i];
      inst.toTCCode(v);
    }
    return v;
  }

  private static void setApplicationProperties(JavaClass jc) throws Exception {
    TCZ.mainClassName = DeploySettings.mainClassName = jc.className;
    DeploySettings.isMainWindow = !isMainClassOrService(jc);
    if (!DeploySettings.isMainWindow) {
      System.out.println("Application is MainClass or Service");
    }

    for (int i = 0; i < jc.methods.length; i++) {
      if (jc.methods[i].signature.equals("<init>()")) {
        searchForProperties(jc.methods[i].code.bcs, jc.superClass);
      }
    }
    for (int i = 0; i < jc.methods.length; i++) {
      if (jc.methods[i].signature.equals("<clinit>()")) {
        searchForProperties(jc.methods[i].code.bcs, jc.superClass);
      }
    }
  }

  private TCClassFlags convertFlags(JavaClass jc) {
    TCClassFlags f = new TCClassFlags();
    f.isPublic = jc.isPublic;
    f.isFinal = jc.isFinal;
    f.isInterface = jc.isInterface;
    f.isAbstract = jc.isAbstract;
    f.isArray = jc.className.equals("java/lang/Array");
    f.isString = jc.className.equals("java/lang/String");
    //f.isSuper = jc.isSuper; - unused
    return f;
  }

  private void convertMethods(JavaClass jc, TCClass tc) throws Exception {
    JavaMethod[] jms = jc.methods;
    int methodCount = jms.length;
    boolean staticInit = false;
    int newMethodCount = methodCount;
    IntHashtable methodsIgnored = new IntHashtable(31);
    Bytecode2TCCode.methodIndexes.removeAllElements();

    // The class has static initializer or method with suffix 4D ?
    for (int i = 0; i < methodCount; i++) {
      JavaMethod jm = jms[i];
      currentMethod = jm.name + " (stage 1)";
      if (jm.name.equals("<clinit>")) {
        staticInit = true;
      } else {
        String sign = jm.name + "4D" + jm.signature.substring(jm.name.length());
        if (Bytecode2TCCode.hasMethodWith4D(jc, sign)) {
          methodsIgnored.put(i, i); // put its index
          newMethodCount--;
          Utils.println("removing method " + jm.signature + " of class " + jc.className);
        }
      }
      currentMethod = null;
    }

    // If the class does not have static initializer, but it has final fields, even if not declared, the method static initializer is created
    // It is inserted last position of the array of methods.
    if (!staticInit && vFinalVar.size() > 0) {
      tc.methods = new TCMethod[newMethodCount + 1];
      TCMethod tcm = new TCMethod();
      tc.methods[newMethodCount] = tcm;
      tcm.flags = new TCMethodFlags();
      tcm.flags.isStatic = true;
      tcm.cpName = GlobalConstantPool.putMethodOrFieldName("<clinit>");
      tcm.tcclass = tc;
      tcm.paramCount = 0;
      tcm.cpParams = new int[0];
      vFinalVar.addElement(new Instruction(RETURN_void, -999));
      tcm.insts = vFinalVar;
      vFinalVar = instruction2TCCode(vFinalVar);
      tcm.code = new TCCode[vFinalVar.size()];
      tcm.iCount = nextRegIStatic;
      tcm.v64Count = nextRegDStatic;
      tcm.oCount = nextRegOStatic;
      vFinalVar.copyInto(tcm.code);
    } else {
      tc.methods = new TCMethod[newMethodCount];
    }

    // first we generate the headers of all methods
    for (int i = 0, k = 0; i < methodCount; i++) {
      JavaMethod jm = jms[i];
      currentMethod = jm.name + " (stage 2)";
      // if this method ends with 4D, ignore it.
      if (methodsIgnored.exists(i)) {
        continue;
      }

      if (jm.name.endsWith("4D")) {
        String name = jm.name;
        jms[i].name = name.substring(0, name.length() - 2);
        jms[i].signature = name.substring(0, name.length() - 2) + jm.signature.substring(name.length());
      }

      Bytecode2TCCode.methodIndexes.addElement(jms[i].signature);

      TCMethod tcm = new TCMethod();
      tc.methods[k++] = tcm;
      tcm.flags = convertFlags(jm);
      tcm.cpName = GlobalConstantPool.putMethodOrFieldName(jm.name);
      tcm.tcclass = tc;
      // setup the parameters
      tcm.paramCount = jm.paramCount;
      // tcm.paramRegs - computed on class load
      tcm.cpParams = new int[jm.paramCount];
      for (int j = 0; j < jm.paramCount; j++) {
        String p = jm.params[j];
        boolean isClass = p.charAt(0) == 'L' || p.charAt(0) == '[';
        if (isClass) {
          tcm.cpParams[j] = GlobalConstantPool.putParam(p); // already in java's format
        } else {
          p = GlobalConstantPool.javaPrimitiveType2TCType(p);
          tcm.cpParams[j] = GlobalConstantPool.getPrimitiveTypeIndex(p);
        }
      }

      // the return type
      if (!jm.ret.equals("V")) {
        String p = jm.ret;
        boolean isClass = p.charAt(0) == 'L' || p.charAt(0) == '[';
        if (isClass) {
          tcm.cpReturn = GlobalConstantPool.putParam(p); // already in java's format
        } else {
          p = GlobalConstantPool.javaPrimitiveType2TCType(p);
          tcm.cpReturn = GlobalConstantPool.getPrimitiveTypeIndex(p);
        }
      }
      currentMethod = null;
    }

    // now that the headers all methods were generated, we can convert their instructions.
    for (int i = 0, k = 0; i < methodCount; i++) {
      JavaMethod jm = jms[i];
      currentMethod = jm.name + " (stage 3)";

      // if this method ends with 4D, ignore it.
      if (methodsIgnored.exists(i)) {
        continue;
      }

      TCMethod tcm = tc.methods[k++];
      JavaCode jcode = jm.replaceWithNative ? null : jm.code;
      OperandReg.init(jm.params, jm.isStatic);
      if (jcode != null) {
        Bytecode2TCCode.javaCodeCurrent = jcode;
        ByteCode[] bc = jcode.bcs;
        Bytecode2TCCode.init(bc);

        if (jcode.eh != null) {
          tcm.exceptionHandlers = convertExceptions(jcode.eh);
        }
        Bytecode2TCCode.currentExceptionHandlers = tcm.exceptionHandlers;

        OperandStack stack = new OperandStack(100);
        // this vector stores the current method instructions
        Vector vcode = new Vector(64);

        boolean isStaticInitializer = false;
        // inserting initialization of final fields to method static initializer
        if (jm.name.equals("<clinit>")) // guich@tc110: must check the full name, not the cpname
        {
          isStaticInitializer = true;
          for (int j = 0; j < vFinalVar.size(); j++) {
            vcode.addElement(vFinalVar.items[j]);
          }

          OperandReg.nextRegI = nextRegIStatic;
          OperandReg.nextReg64 = nextRegDStatic;
          OperandReg.nextRegO = nextRegOStatic;
        }

        if (dumpBytecodes) {
          System.out.println(jc.className + "." + jm.name);
        }
        for (int j = 0; j < bc.length; j++) {
          Instruction tccode = Bytecode2TCCode.convert(bc, bc[j], stack, vcode, isStaticInitializer, jm.signature);
          if (dumpBytecodes) {
            System.out.println(bc[j] + " -> " + tccode); // dump
          }
        }
        Bytecode2TCCode.updateBranchs(vcode);
        tcm.exceptionHandlers = Bytecode2TCCode.updatePCsOfExceptionHandler(tcm.exceptionHandlers);
        tcm.insts = vcode;
      }

      tcm.iCount = OperandReg.nextRegI;
      tcm.oCount = OperandReg.nextRegO;
      tcm.v64Count = OperandReg.nextReg64;
      tcm.iParamCount = OperandReg.paramRegI;
      tcm.oParamCount = OperandReg.paramRegO;
      tcm.v64ParamCount = OperandReg.paramReg64;
      currentMethod = null;
    }

    CFG.buildCFG(tc.methods);
    //CFG.printAllMethods(tc.methods);
    RegAllocation.makeRegAllocation(tc.methods);
    generateCode(tc.methods);
    methodsIgnored.clear();
  }

  public static void generateCode(TCMethod[] methods) {
    if (methods != null) {
      int len = methods.length;
      for (int i = 0; i < len; i++) {
        TCMethod tcm = methods[i];
        if (tcm != null && tcm.insts != null) {
          Vector vcode = instruction2TCCode(tcm.insts);
          tcm.code = new TCCode[vcode.size()];
          vcode.copyInto(tcm.code);
          Bytecode2TCCode.generateLineNumbers(tcm);
        }
      }
    }
  }

  private TCException[] convertExceptions(JavaException[] jexs) {
    TCException tcexs[] = new TCException[jexs.length], t;
    JavaException j;
    for (int i = 0; i < jexs.length; i++) {
      t = tcexs[i] = new TCException();
      j = jexs[i];
      t.className = j.catchType != null ? j.catchType : "java/lang/Throwable"; // guich@tc111_15: base exception class is throwable, not Exception
      GlobalConstantPool.putCls(t.className);
      t.startPC = Bytecode2TCCode.setGotoIndex(j.startPC);
      t.endPC = Bytecode2TCCode.setGotoIndex(j.endPC);
      t.handlerPC = Bytecode2TCCode.setGotoIndex(j.handlerPC);
    }
    return tcexs;
  }

  private TCMethodFlags convertFlags(JavaMethod jm) {
    TCMethodFlags f = new TCMethodFlags();
    f.isAbstract = jm.isAbstract;
    f.isFinal = jm.isFinal;
    f.isNative = jm.isNative || jm.replaceWithNative;
    f.isPrivate = jm.isPrivate;
    f.isProtected = jm.isProtected;
    f.isPublic = jm.isPublic;
    f.isStatic = jm.isStatic;
    f.isSynchronized = jm.isSynchronized;
    // jm.isStrict - not used
    if (jm.isSynchronized && !syncWarned) {
      System.out.println("Synchronized is not supported for methods yet.");
      syncWarned = true;
    }
    return f;
  }

  private void convertFields(JavaClass jc, TCClass tc) {
    JavaField[] jfs = jc.fields;
    Vector sI32 = new Vector(5);
    Vector sV64 = new Vector(5);
    Vector sObj = new Vector(5);
    Vector iI32 = new Vector(5);
    Vector iV64 = new Vector(5);
    Vector iObj = new Vector(5);

    Bytecode2TCCode.htInstanceFieldIndexes.clear();
    Bytecode2TCCode.htStaticFieldIndexes.clear();

    for (int i = 0; i < jfs.length; i++) {
      JavaField f = jfs[i];
      TCFieldFlags flags = convertFlags(f);
      Object o;
      int index = 0;
      switch (flags.type) {
      case INT:
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case SHORT: {
        TCInt32Field t = new TCInt32Field();
        t.flags = flags;
        t.cpName = GlobalConstantPool.putMethodOrFieldName(f.name);
        t.cpType = GlobalConstantPool.getPrimitiveTypeIndex(GlobalConstantPool.javaPrimitiveType2TCType(f.type));
        o = t;
        if (f.constantValue != null) {
          t.value = ((Integer) f.constantValue).intValue();
        }
        if (flags.isStatic) {
          index = GlobalConstantPool.putStaticField(jc.className, f.name);
          if (flags.isFinal) {
            Operand target = new OperandSym(opr_staticI, index);
            Operand value = new OperandConstant32(t.value, type_Int);
            GenerateInstruction.newInstruction(vFinalVar, pref_MOV, target, value, -999);
          }
          sI32.addElement(o);
        } else {
          index = GlobalConstantPool.putInstanceField(jc.className, f.name);
          iI32.addElement(o);
        }
        break;
      }
      case LONG:
      case FLOAT:
      case DOUBLE: {
        TCValue64Field t = new TCValue64Field();
        t.flags = flags;
        t.cpName = GlobalConstantPool.putMethodOrFieldName(f.name);
        t.cpType = GlobalConstantPool.getPrimitiveTypeIndex(GlobalConstantPool.javaPrimitiveType2TCType(f.type));
        o = t;
        if (flags.isStatic) {
          sV64.addElement(o);
          index = GlobalConstantPool.putStaticField(jc.className, f.name);
        } else {
          iV64.addElement(o);
          index = GlobalConstantPool.putInstanceField(jc.className, f.name);
        }

        if (f.constantValue != null) {
          if (flags.type == LONG) {
            t.lvalue = ((Long) f.constantValue).longValue();
            if (flags.isFinal && flags.isStatic) {
              int kind;
              Operand target;
              if (flags.isStatic) {
                kind = opr_staticL;
                target = new OperandSym(kind, index);
              } else {
                kind = opr_fieldL;
                target = new OperandSym(kind, index);
                OperandReg regO = new OperandRegO(0);
                target = new OperandExternal(regO, (OperandSym) target);
              }
              Operand value = new OperandConstant64(t.lvalue, type_Long);
              GenerateInstruction.newInstruction(vFinalVar, pref_MOV, target, value, -999);
            }
          } else {
            t.dvalue = flags.type == DOUBLE ? ((Double) f.constantValue).doubleValue()
                : ((Float) f.constantValue).doubleValue();
            if (flags.isFinal && flags.isStatic) {
              int kind;
              Operand target;
              if (flags.isStatic) {
                kind = opr_staticD;
                target = new OperandSym(kind, index);
              } else {
                kind = opr_fieldD;
                target = new OperandSym(kind, index);
                OperandReg regO = new OperandRegO(0);
                target = new OperandExternal(regO, (OperandSym) target);
              }
              Operand value = new OperandSymD64(GlobalConstantPool.put(t.dvalue));
              GenerateInstruction.newInstruction(vFinalVar, pref_MOV, target, value, -999);
            }
          }
        }
        break;
      }
      default: // OBJECT
      {
        TCObjectField t = new TCObjectField();
        t.flags = flags;
        t.cpName = GlobalConstantPool.putMethodOrFieldName(f.name);
        t.cpType = GlobalConstantPool.putParam(f.type);
        o = t;
        int idx = -1;
        /*
         *   addTestCase( TestPalmMemoryCard.class );
         * generates:
         *   field type: Ljava/lang/Class;
         *   field name: class$samples$sys$testcases$TestPalmMemoryCard
         *
         * another sample: totalcross/ui/html/Table$Row$EmptyCell
         * is referenced as: Ljava/lang/Class; - class$totalcross$ui$html$Table$Row$EmptyCell
         */
        if (f.type.equals("Ljava/lang/Class;") && f.name.startsWith("class$")) {
          String normalized = convertClassName(f.name);
          try {
            Convert.toInt(normalized); // ignore numeric-only classes
          } catch (InvalidNumberException ine) {
            callForName.addElement(normalized);
          }
        }

        if (f.constantValue != null) {
          JavaConstantInfo info = (JavaConstantInfo) f.constantValue;
          t.value = jc.cp.getString1(info.index1);
          idx = GlobalConstantPool.putStr((String) t.value); // add the value to the temporary constant pool
        }
        if (flags.isStatic) {
          index = GlobalConstantPool.putStaticField(jc.className, f.name);
          if (flags.isFinal && idx != -1) {
            Operand target = new OperandSym(opr_staticO, index);
            Operand value = new OperandSymO(idx);
            GenerateInstruction.newInstruction(vFinalVar, pref_MOV, target, value, -999);
          }
          sObj.addElement(o);
        } else {
          index = GlobalConstantPool.putInstanceField(jc.className, f.name);
          /*if (flags.isFinal && idx != -1)
                  {
                     Operand target = new OperandSym(opr_fieldO, index);
                     OperandReg regO = new OperandRegO(0);
                     target = new OperandExternal(regO, (OperandSym) target);
                     Operand value = new OperandSymO(idx);
                     GenerateInstruction.newInstruction(vFinalVar, pref_MOV, target, value, 0);
                  }*/
          iObj.addElement(o);
        }
        break;
      }
      }
      // set the index to access the field in the constant pool (class name + field name)
      ((TCField) o).cpField = index;
    }

    nextRegIStatic = OperandReg.nextRegI;
    nextRegDStatic = OperandReg.nextReg64;
    nextRegOStatic = OperandReg.nextRegO;

    // now convert the vectors to arrays
    if (iI32.size() > 0) {
      tc.int32InstanceFields = v2aI32(iI32);
    }
    if (iV64.size() > 0) {
      tc.value64InstanceFields = v2aV64(iV64);
    }
    if (iObj.size() > 0) {
      tc.objectInstanceFields = v2aObj(iObj);
    }

    if (sI32.size() > 0) {
      tc.int32StaticFields = v2aI32(sI32);
    }
    if (sV64.size() > 0) {
      tc.value64StaticFields = v2aV64(sV64);
    }
    if (sObj.size() > 0) {
      tc.objectStaticFields = v2aObj(sObj);
    }
  }

  private String convertClassName(String name) {
    // class$totalcross$ui$html$Table$Row$EmptyCell -> totalcross.ui.html.Table$Row$EmptyCell
    StringBuffer sb = new StringBuffer(name.substring(6));
    boolean firstUpper = false;
    for (int k = 0; k < sb.length(); k++) {
      if (sb.charAt(k) == '$') {
        if ('a' <= sb.charAt(k + 1) && sb.charAt(k + 1) <= 'z') {
          sb.setCharAt(k, '.');
        } else if (!firstUpper) {
          firstUpper = true;
          sb.setCharAt(k, '.');
        }
      }
    }
    //System.out.println("converting "+name+" -> "+sb);
    return sb.toString();
  }

  private TCObjectField[] v2aObj(Vector obj) // vector to array
  {
    TCObjectField[] a = new TCObjectField[obj.size()];
    obj.copyInto(a);
    return a;
  }

  private TCValue64Field[] v2aV64(Vector obj) {
    TCValue64Field[] a = new TCValue64Field[obj.size()];
    obj.copyInto(a);
    return a;
  }

  private TCInt32Field[] v2aI32(Vector obj) {
    TCInt32Field[] a = new TCInt32Field[obj.size()];
    obj.copyInto(a);
    return a;
  }

  private TCFieldFlags convertFlags(JavaField jf) {
    TCFieldFlags tcf = new TCFieldFlags();
    tcf.isPublic = jf.isPublic;
    tcf.isPrivate = jf.isPrivate;
    tcf.isProtected = jf.isProtected;
    tcf.isStatic = jf.isStatic;
    tcf.isFinal = jf.isFinal;
    tcf.isVolatile = jf.isVolatile;
    tcf.isTransient = jf.isTransient;
    tcf.isArray = jf.type.startsWith("[");
    tcf.type = ByteCode.convertJavaType(jf.type);
    return tcf;
  }

  static class NameBytes {
    String name;
    byte[] bytes;
  }

  private static void expandZip(Map<String, JavaClass> classes, Vector vin, String fName) throws Exception {
    JavaClass jc;
    // open the jar file and get each entry
    java.io.File f = new java.io.File(fName);
    if (!f.exists()) {
      throw new IllegalArgumentException("File " + fName + " not found!");
    }
    ZipInputStream zIn = new ZipInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(f), 65000));
    try {
      String mainCandidate = Utils.getFileName(fName);
      if (mainCandidate != null && mainCandidate.indexOf('.') > 0) {
        mainCandidate = mainCandidate.substring(0, mainCandidate.indexOf('.'));
      }
      int mainCandidateIndex = -1;
      // guich@tc310: now we read all classes and search for the MainClass one.
      // then we place it at the begining of the list, to ensure that it will be in the first tcz if they are splitted.
      ArrayList<NameBytes> all = new ArrayList<NameBytes>(500);
      for (java.util.zip.ZipEntry zEntry = zIn.getNextEntry(); zEntry != null; zEntry = zIn.getNextEntry()) {
        // if the name is a path, exit.
        String name = zEntry.getName();
        if (name.endsWith("/") || name.toLowerCase().startsWith("meta-inf") || name.endsWith(".java")) {
          continue;
        }

        byte[] bytes = Utils.readJavaInputStream(zIn);
        NameBytes nb = new NameBytes();
        nb.name = name;
        nb.bytes = bytes;
        if (mainCandidateIndex == -1 && name.endsWith(".class")
            && Utils.getFileNameWithoutExt(name).equals(mainCandidate)) {
          mainCandidateIndex = all.size();
        }
        all.add(nb);
      }
      if (mainCandidateIndex != -1) // move the MainWindow to the begin
      {
        NameBytes nb = all.get(mainCandidateIndex);
        all.remove(mainCandidateIndex);
        all.add(0, nb);
      }
      for (NameBytes nb : all) {
        String name = nb.name;
        byte[] bytes = nb.bytes;
        if (name.endsWith(".class")) {
          jc = classes.get(name.substring(0, name.length() - 6));
          jc = jc.parse(bytes, false);
          if (Utils.getFileName(jc.className).equals(mainCandidate)) {
            setApplicationProperties(jc);
          }
          name = jc.className + ".class";
        } else {
          jc = null;
        }
        if (!htAddedClasses.exists(name)) // ignore exclusion list!
        {
          vin.addElement(new TCZ.Entry(bytes, name, bytes.length, jc));
          htAddedClasses.put(name, jc == null ? (Object) bytes : jc);
        }
      }
    } finally {
      try {
        zIn.close();
      } catch (Exception e) {
      }
    }
  }

  private static int processFiles(Vector vin, Vector vout) throws Exception {
    int s = 0;
    for (int i = 0; i < vin.size();) {
      try {
        TCZ.Entry fe = (TCZ.Entry) vin.items[i];
        String name = fe.name;
        String nameLow = name.toLowerCase();
        byte[] bytes = fe.bytes;
        int len = bytes.length;
        boolean print = true;
        s += len;
        if (nameLow.endsWith(".class")) // is this a class file?
        {
          GlobalConstantPool.saveState();
          // start the job
          J2TC j2 = new J2TC((JavaClass) fe.extra);
          if (j2.converted == null || DeploySettings.testClass) {
            print = false;
          } else {
            bytes = j2.bytes; // replace the bytes by the tclass ones
            System.out.print("Adding " + j2.converted.className);
            if (!DeploySettings.testClass) {
              vout.addElement(new TCZ.Entry(bytes, j2.converted.className, len)); // note that all files must be added - use the real package name
            }
          }
        } else if (!DeploySettings.testClass) {
          ByteArrayStream basz = new ByteArrayStream(bytes);
          print = !name.equals("tckey.bin") && !name.equals("tcparms.bin") && !name.equals(DeploySettings.TCAPP_PROP);

          if (print) {
            System.out.print("Adding " + name);
          }
          tcbasz.reset();
          if (nameLow.endsWith(".bmp") || nameLow.endsWith(".gif")) // convert gif and bmp to png
          {
            new Image(bytes).createPng(basz = new ByteArrayStream(8192));
            len = basz.getPos();
            System.out.print(" (converted to PNG)");
          }

          tc.tools.converter.Storage.compressAndWrite(basz, new DataStream(tcbasz));
          bytes = tcbasz.toByteArray();
          vout.addElement(new TCZ.Entry(bytes, name, len)); // note that all files must be added
        }
        if (print && len != 0) {
          System.out.println("... " + (bytes.length * 100 / len) + "%");
        }

        vin.removeElementAt(0); // no error, remove element.
      } catch (ConstantPoolLimitReachedException cplre) {
        System.out.println("Limit reached for " + cplre.getMessage() + ". Splitting tcz...");
        GlobalConstantPool.restoreState();
        return s;
      }
    }
    // now adds the list of classes used in forName
    if (!DeploySettings.isJarOrZip) {
      int n = callForName.size();
      for (int i = n - 1; i >= 0; i--) // first, remove the ones that were already processed
      {
        String c = (String) callForName.items[i];
        c = c.replace('.', '/') + ".class";
        if (htAddedClasses.exists(c) || htExcludedClasses.exists(c)) {
          callForName.removeElementAt(i);
        }
      }
      n = callForName.size();
      if (n > 0) {
        System.out.println("Adding " + n + " classes specified with Class.forName");
        String[] classes = (String[]) callForName.toObjectArray();

        Vector temp = new Vector(50); // process callForName on this temporary vector
        callForName.removeAllElements();
        for (int i = 0; i < n; i++) {
          String name = (String) classes[i];
          name = name.replace('.', '/') + ".class";
          if (!inExclusionList(name)) {
            addAndExpand(temp, name, false);
          }
        }
        if (temp.size() > 0) {
          s += processFiles(temp, vout);
          n = temp.size();
          for (int i = 0; i < n; i++) {
            vin.addElement(temp.items[i]);
          }
        }
      }
    }
    return s;
  }

  private static Hashtable htVisited = new Hashtable(1000);

  private static void addAndExpand(Vector vin, String name, boolean throwErrorIfInexistant) throws Exception {
    JavaClass jc;
    if (htVisited.exists(name) || name.startsWith("tc/tools") || name.startsWith("tc/Deploy")
        || name.startsWith("net/rim")) {
      return;
    }
    htVisited.put(name, name);
    byte[] bytes = Utils.findAndLoadFile(name, false);
    boolean isClass = name.toLowerCase().endsWith(".class") && !isPrimitiveClass(name);
    if (bytes == null) {
      if (isClass) {
        if (throwErrorIfInexistant) {
          throw new IllegalArgumentException("File not found: " + name);
        } else {
          System.out.println("Class.forName not found: " + name);
        }
      }
      return;
    }
    if (isClass) {
      jc = new JavaClass(bytes, false);
      name = jc.className + ".class";
    } else {
      jc = null;
    }
    if (!htAddedClasses.exists(name) && !htExcludedClasses.exists(name)) {
      if (inExclusionList(name) && !name.equals(DeploySettings.mainClassName)) {
        //System.out.println("Added to exclusion: "+name);
        htExcludedClasses.put(name, jc == null ? (Object) bytes : jc);
      } else {
        if (isClass) {
          vin.addElement(new TCZ.Entry(bytes, name, bytes.length, jc));
          htAddedClasses.put(name, jc);
        } else if (isValidFile(name)) // add only if the resource is valid
        {
          vin.addElement(new TCZ.Entry(bytes, name, bytes.length));
          htAddedClasses.put(name, bytes);
        }
        //System.out.println("Added class "+name);
      }
      if (!inProhibitedList(name, true) && isClass) {
        expandClass(vin, jc);
      }
    }
  }

  private static boolean isPrimitiveClass(String name) {
    return name.equals("byte.class") || name.equals("short.class") || name.equals("int.class")
        || name.equals("boolean.class") || name.equals("char.class") || name.equals("long.class")
        || name.equals("float.class") || name.equals("double.class") || name.equals("void.class");
  }

  private static void expandClass(Vector vin, JavaClass jc) throws Exception {
    JavaConstantPool jcp = jc.cp;
    for (int i = 1; i < jcp.numConstants; i++) {
      if (jcp.constants[i] instanceof JavaConstantInfo) {
        JavaConstantInfo jci = (JavaConstantInfo) jcp.constants[i];
        String c = jcp.getString1(jci.index1);
        switch (jci.type) {
        case 7: // class identifier
          c += ".class";
        case 8: // string
          if (!inProhibitedList(c, false) && isValidFile(c)
              && !htAddedClasses.exists(c)/* && !inExclusionList(c)*//* && !htExcludedClasses.exists(c)*/) {
            addAndExpand(vin, c, true);
          }
          break;
        }
      }
    }
  }

  public static boolean inProhibitedList(String c, boolean blockTC) {
    c = c.replace('.', '/');
    //return c.length() == 0 || c.startsWith("java/") || c.charAt(0) == '[' || c.startsWith("sun/") || c.startsWith("com/sun/") || c.startsWith("javax/");
    return c.length() == 0 || (blockTC && !DeploySettings.isTotalCrossJarDeploy && c.startsWith("totalcross/"))
        || c.startsWith("java/") || c.charAt(0) == '[' || c.startsWith("sun/") || c.startsWith("com/sun/")
        || c.startsWith("javax/");
  }

  private static boolean isValidFile(String c) {
    int dot = c.lastIndexOf('.');
    return dot > 0 && c.charAt(0) != '*' && c.indexOf("www.") < 0
        && htValidExtensions.exists(c.substring(dot).toLowerCase());
  }

  private static boolean inExclusionList(String c) {
    for (int i = DeploySettings.exclusionList.size() - 1; i >= 0; i--) {
      if (c.startsWith((String) DeploySettings.exclusionList.items[i])) {
        return true;
      }
    }
    return false;
  }

  private static void setupHt() {
    htValidExtensions.put(".bin", "");
    htValidExtensions.put(".txt", "");
    htValidExtensions.put(".xml", "");
    htValidExtensions.put(".htm", "");
    htValidExtensions.put(".html", "");
    htValidExtensions.put(".bmp", "");
    htValidExtensions.put(".gif", "");
    htValidExtensions.put(".jpg", "");
    htValidExtensions.put(".jpeg", "");
    htValidExtensions.put(".png", "");
    htValidExtensions.put(".wav", "");
    htValidExtensions.put(".mp3", "");
    htValidExtensions.put(".pdf", "");
    htValidExtensions.put(".class", "");
  }

  /** Convert a single class file or a zip/jar with a set of files.
   *  Outputs a tcz file. */
  public static void process(String fName, int options) throws Exception {
    String cn = null;
    fName = fName.replace('\\', '/');
    ByteCode.initClasses();
    Vector vin = new Vector(200);
    DeploySettings.entriesList = vin; // keep track of input files
    setupHt();
    GlobalConstantPool.init();

    String fLow = fName.toLowerCase();
    if (fLow.endsWith(".tcz")) // if the user passed a tcz file, use it to create the installers
    {
      DeploySettings.inputFileWasTCZ = true; // guich@tc100b5_53: don't delete the tcz file if it was the input file.
      if (DeploySettings.tczFileName == null) {
        DeploySettings.tczFileName = fName;
      }
      fName = fName.substring(0, fName.length() - 4); // remove the .tcz
      DeploySettings.currentDir = Utils.getParent(fName);
      if (DeploySettings.currentDir == null) {
        DeploySettings.currentDir = "./";
      } else {
        fName = fName.substring(DeploySettings.currentDir.length() + 1);
      }
      DeploySettings.targetDir = Convert.appendPath(
          DeploySettings.targetDir != null ? DeploySettings.targetDir : DeploySettings.currentDir, "/install/");
      if (DeploySettings.filePrefix != null) {
        cn = fName = DeploySettings.filePrefix;
      } else {
        DeploySettings.filePrefix = cn = fName;
      }
      //flsobral@tc123_56: fixed Deploy to correctly handle using a tcz file as input.
      File f = new File(DeploySettings.tczFileName, File.READ_WRITE);
      TCZ tczFile = new TCZ(f);
      TCZ.mainClassName = DeploySettings.mainClassName = tczFile.names[0];
      f.close();
    } else {
      int inSize = 0;
      Map<String, JavaClass> classes = new HashMap<>();
      DeploySettings.isJarOrZip = fLow.endsWith(".zip") || fLow.endsWith(".jar");
      if (fLow.endsWith(".jar")) {
        JarClassPathLoader.addFile(fName);
        classes = getJavaClassFromJar(new JarFile(new java.io.File(fName)));
      }
      if (DeploySettings.isJarOrZip) // process bat of files?
      {
        if (DeploySettings.excludeOptionSet) {
          throw new IllegalArgumentException(
              "/x cannot be used with jar/zip files. You must exclude the files from the jar/zip yourself.");
        }
        String dir = Utils.getParent(fName);
        if (dir != null) {
          DeploySettings.currentDir = dir;
        }
        expandZip(classes, vin, fName);
        cn = fName;
      } else {
        File f = new File(fName);
        if (f.exists() && f.isDir()) {
          boolean found = false;
          String[] files = new File(fName).listFiles();
          String dir = fName; // may be later replaces
          if (files != null) {
            for (int i = 0; i < files.length; i++) {
              if (files[i].endsWith(".class")) {
                try {
                  f = new File(dir + "\\" + files[i], File.READ_WRITE);
                  byte[] bytes = new byte[f.getSize()];
                  f.readBytes(bytes, 0, bytes.length);
                  f.close();
                  JavaClass jc = new JavaClass(bytes, true);
                  if (isMain(jc) && !jc.className.contains("$")) {
                    if (found) {
                      throw new IllegalArgumentException(
                          "More than one MainWindow/MainClass found in the given folder: " + fName + " and " + files[i]
                              + ". You must use the other options to deploy your application (a jar or specify the main class name)");
                    } else {
                      fName = f.getPath();
                      if (fName.startsWith("./") || fName.startsWith(".\\")) {
                        fName = fName.substring(2);
                      }
                      fLow = fName.toLowerCase();
                      found = true;
                    }
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                } // just ignore
              }
            }
          }
          if (!found) {
            throw new IllegalArgumentException(
                "Path does not contain a class that extends MainWindow, Conduit, GameEngineMainWindow nor TestSuite");
          }
        }
        if (fLow.endsWith(".class") || (fName.indexOf('.') < 0 && new File(fName + ".class").exists())) // if the user passed a .class or a class without .class...
        {
          if (!fLow.endsWith(".class")) {
            fName += ".class";
            fLow += ".class";
          }
          byte[] bytes = Utils.loadFile(fName, true);
          JavaClass jc = new JavaClass(bytes, false);
          if (jc.className.indexOf("totalcross/") >= 0 && jc.className.indexOf("test/") == -1) {
            throw new IllegalArgumentException(
                "You can't deploy totalcross packages using a single .class. Add it to a "
                    + jc.className.substring(jc.className.lastIndexOf('/') + 1)
                    + ".jar file and deploy that jar file.");
          }
          setApplicationProperties(jc);
          // if (jc.className.indexOf('/') > 0) // does it have a package? - guich@tc114_84: support class without package
          {
            DeploySettings.baseDir = Utils.getBaseFolder(DeploySettings.currentDir, fName, jc.className);
            if (DeploySettings.baseDir == null) {
              throw new IllegalArgumentException(
                  "Class " + jc.className + " is at a directory that does not correspond to the package declaration!");
            }
            DeploySettings.mainClassDir = Utils.getParent(DeploySettings.baseDir + "/" + jc.className);
            if (DeploySettings.mainClassDir != null) {
              DeploySettings.mainClassDir += DeploySettings.SLASH;
            }
          }
          DeploySettings.mainPackage = jc.className;
          addAndExpand(vin, jc.className + ".class", true);
        } else {
          throw new IllegalArgumentException(
              "Invalid input: only .class/.zip/.jar/.tcz or a folder is supported. Make sure that the file is the FIRST parameter passed to tc.Deploy!");
        }
      }
      if (DeploySettings.currentDir == null) {
        DeploySettings.currentDir = "./";
      }
      if (DeploySettings.rasKey != null && !DeploySettings.isTotalCrossJarDeploy
          && (DeploySettings.isMainWindow || DeploySettings.isService || DeploySettings.isMainClass)) // registration key was specified - guich@tc310: only if not deploying the sdk and if its not a library
      {
        AESCipher cipher = new AESCipher();
        AESKey key = new AESKey(new byte[] { (byte) 0x06, (byte) 0x05, (byte) 0xF4, (byte) 0xF0, (byte) 0xF4,
            (byte) 0x08, (byte) 0x01, (byte) 0x09, (byte) 0xF7, (byte) 0x09, (byte) 0xFE, (byte) 0xFC, (byte) 0xF5,
            (byte) 0x04, (byte) 0x00, (byte) 0x0B });

        cipher.reset(Cipher.OPERATION_ENCRYPT, key, Cipher.CHAINING_CBC, null, Cipher.PADDING_PKCS5);
        cipher.update(DeploySettings.rasKey);
        byte[] riv = cipher.getIV();
        byte[] out = cipher.getOutput();

        ByteArrayStream bas = new ByteArrayStream(128);
        DataStream ds = new DataStream(bas);
        ds.writeInt(riv.length);
        ds.writeBytes(riv);
        ds.writeInt(out.length);
        ds.writeBytes(out);

        byte[] enc = bas.toByteArray();
        vin.addElement(new TCZ.Entry(enc, "tckey.bin", enc.length));
      }
      if (DeploySettings.mainClassName != null && !DeploySettings.isTotalCrossJarDeploy && DeploySettings.isMainWindow) //flsobral@tc126: test for mainClassName instead of mainClassDir. The later is always null when deploy is used with a zip/jar file. This fixes third-party server activation when the application is deployed from a jar file. - guich@tc310 only if not deploying the sdk and if its not a library
      {
        Hashtable htVmParams = new Hashtable(4);
        if (totalcross.sys.Settings.activationServerURI != null) {
          htVmParams.put("activationServerURI", totalcross.sys.Settings.activationServerURI);
        }
        if (totalcross.sys.Settings.activationServerNamespace != null) {
          htVmParams.put("activationServerNamespace", totalcross.sys.Settings.activationServerNamespace);
        }
        if (totalcross.sys.Settings.applicationId != null) {
          htVmParams.put("applicationId", totalcross.sys.Settings.applicationId);
        }
        if (totalcross.sys.Settings.appVersion != null) {
          htVmParams.put("appVersion", totalcross.sys.Settings.appVersion);
        }
        if (totalcross.sys.Settings.pushTokenAndroid != null) {
          htVmParams.put("pushTokenAndroid", totalcross.sys.Settings.pushTokenAndroid);
        }
        if (totalcross.sys.Settings.iosCertDate != null) {
          htVmParams.put("iosCertDate", totalcross.sys.Settings.iosCertDate.toIso8601());
        }
        byte[] htDump = htVmParams.getKeyValuePairs("=").toString("\n").getBytes();
        vin.addElement(new TCZ.Entry(htDump, "tcparms.bin", htDump.length));
      }
      if (DeploySettings.tcappProp != null) {
        vin.addElement(
            new TCZ.Entry(DeploySettings.tcappProp, DeploySettings.TCAPP_PROP, DeploySettings.tcappProp.length));
      }

      TCMethod.checkJavaCalls = true;
      GlobalConstantPool.checkLimit = true;
      Vector tczs = new Vector(5);
      Vector tczMsg = new Vector(5);

      for (int part = 0; vin.size() > 0; part++) {
        Vector vout = new Vector(200);
        inSize = processFiles(vin, vout);
        if (notResolvedForNameFound && !DeploySettings.isJarOrZip) {
          System.out.println(
              "ATTENTION: An unhandled Class.forName was detected. The deployer can handle Class.forName(x), when x is a String with "
                  + "the class name, but cannot handle when x is a variable. Thus, the referenced classes will not be added to the deploy. "
                  + "Consider to use a jar file with all the project classes and pass it as parameter to tc.Deploy.");
        }

        if (!GlobalConstantPool.isEmpty()) // guich@tc111_2: only store if there's at least one user class
        {
          // convert the global constant pool
          tcbas.reset();
          tcbasz.reset();
          GlobalConstantPool.write(new DataStreamLE(tcbas));
          int orig = tcbas.getPos();
          Storage.compressAndWrite(tcbas, new DataStream(tcbasz));
          vout.addElement(new TCZ.Entry(tcbasz.toByteArray(), "ConstantPool", orig));
        }
        // now that all files were processed, put everything in a single tcz file.
        // set the TCZ attributes
        short attr = 0;
        cn = DeploySettings.mainClassName;
        if (cn != null) {
          if (DeploySettings.appIdSpecifiedAsArgument) {
            throw new IllegalArgumentException(
                "The application Id can only be specified in the command line during the creation of libraries. For applications, you must create a static initializer in the main class, assigning the desired id. For example:\n\npublic class "
                    + Utils.getFileName(cn)
                    + " extends MainWindow // or implements MainClass\n{\n   static\n   {\n      totalcross.sys.Settings.applicationId = \"Crtr\";\n   }\n   ...");
          }
          if (part == 0) {
            attr |= (DeploySettings.isMainWindow ? TCZ.ATTR_HAS_MAINWINDOW : TCZ.ATTR_HAS_MAINCLASS);
          }
        } else {
          cn = fName;
        }
        if (DeploySettings.resizableWindow) {
          attr |= TCZ.ATTR_RESIZABLE_WINDOW;
        }
        if (DeploySettings.windowFont == Settings.WINDOWFONT_DEFAULT) {
          attr |= TCZ.ATTR_WINDOWFONT_DEFAULT;
        }
        switch (DeploySettings.windowSize) {
        case Settings.WINDOWSIZE_320X480:
          attr |= TCZ.ATTR_WINDOWSIZE_320X480;
          break;
        case Settings.WINDOWSIZE_480X640:
          attr |= TCZ.ATTR_WINDOWSIZE_480X640;
          break;
        case Settings.WINDOWSIZE_600X800:
          attr |= TCZ.ATTR_WINDOWSIZE_600X800;
          break;
        }
        if (cn.indexOf('/') >= 0) {
          cn = cn.substring(cn.lastIndexOf('/') + 1); // strip the package name;
        }
        if (DeploySettings.isJarOrZip && (cn.toLowerCase().endsWith(".zip") || cn.toLowerCase().endsWith(".jar"))) {
          cn = cn.substring(0, cn.length() - 4);
        }
        if (DeploySettings.filePrefix == null) {
          DeploySettings.filePrefix = cn;
        }
        DeploySettings.tczFileName = ((DeploySettings.targetDir != null ? DeploySettings.targetDir
            : DeploySettings.currentDir) + "/" + DeploySettings.filePrefix + ".tcz").replace('\\', '/');

        if (vin.size() == 0) {
          if (DeploySettings.targetDir != null && DeploySettings.targetDir.length() > 0) {
            File outFolder = new File(DeploySettings.targetDir);
            if (!outFolder.exists()) {
              outFolder.createDir();
            }
          }
          DeploySettings.targetDir = Convert.appendPath(
              DeploySettings.targetDir != null ? DeploySettings.targetDir : DeploySettings.currentDir, "/install/"); // don't move from here!
        }
        // create the TCZ file
        if (isEmpty(vout)) {
          throw new DeployerException("ERROR: no files added to the TCZ!");
        }
        if (!DeploySettings.testClass) {
          String filename = part == 0 ? DeploySettings.tczFileName
              : DeploySettings.tczFileName.replace(".tcz", "_" + part + "lib.tcz");
          tczs.addElement(filename);
          TCZ out = new TCZ(vout, filename, attr);
          int ratio = inSize == 0 ? 0 : out.size * 100 / inSize;
          tczMsg.addElement("File " + filename + " written (" + ratio + "% - " + out.size + " bytes)");
        }
        GlobalConstantPool.init();
      }
      DeploySettings.tczs = (String[]) tczs.toObjectArray();
      for (int i = 0; i < tczMsg.size(); i++) {
        System.out.println(tczMsg.items[i]);
      }
    }
    if (!DeploySettings.testClass) {
      if (DeploySettings.mainClassName != null) {
        System.out.println("Main class name: " + DeploySettings.mainClassName);
      }
      if (DeploySettings.appTitle != null && DeploySettings.appTitle.trim().length() > 0) {
        System.out.println("Application title: " + DeploySettings.appTitle);
      } else {
        System.out.println("Application title not provided, using: " + (DeploySettings.appTitle = cn));
      }
      if (DeploySettings.companyInfo != null) {
        System.out.println("Company information: " + DeploySettings.companyInfo);
      }
      if (DeploySettings.applicationId != null) {
        System.out.println("Application id: " + DeploySettings.applicationId);
      } else {
        String name = cn;
        if (name.indexOf('.') > 0) {
          name = name.substring(0, name.lastIndexOf('.'));
        }
        DeploySettings.applicationId = Utils.getCreator(name);
        System.out
            .println("Application id not provided. Created from \"" + name + "\": " + DeploySettings.applicationId);
      }
      if (DeploySettings.appVersion != null) {
        System.out.println("Application version: " + DeploySettings.appVersion);
      }
      if (totalcross.sys.Settings.appCategory != null) {
        System.out.println("Application category: " + totalcross.sys.Settings.appCategory);
      }
      if (totalcross.sys.Settings.appDescription != null) {
        System.out.println("Application description: " + totalcross.sys.Settings.appDescription);
      }
      if (totalcross.sys.Settings.appLocation != null) {
        System.out.println("Application location: " + totalcross.sys.Settings.appLocation);
      }
      if (DeploySettings.isFullScreen) {
        System.out.println("Application will be Full Screen " + (DeploySettings.fullScreenPlatforms != null
            ? ("on platforms " + DeploySettings.fullScreenPlatforms) : ""));
        if (DeploySettings.fullScreenPlatforms == null
            || DeploySettings.fullScreenPlatforms.toLowerCase().indexOf("android") >= 0) {
          Utils.println(
              "Caution! Android should not be fullscreen because the virtual keyboard will not appear correctly. Consider removing \"Android\" from the Settings.fullScreenPlatforms field");
        }
      }
    }
  }

  private static boolean isEmpty(Vector v) {
    int n = v.size();
    if (n == 0) {
      return true;
    }
    boolean onlyBin = true;
    for (int i = 0; i < n; i++) {
      String name = ((totalcross.util.zip.TCZ.Entry) v.items[i]).name;
      onlyBin &= name.toLowerCase().endsWith(".bin");
    }
    return onlyBin;
  }

  private static boolean isMain(JavaClass jc) {
    if ("totalcross/ui/MainWindow".equals(jc.superClass) || "totalcross/game/GameEngineMainWindow".equals(jc.superClass)
        || "totalcross/game/GameEngine".equals(jc.superClass) || "totalcross/unit/TestSuite".equals(jc.superClass)
        || "totalcross/io/sync/Conduit".equals(jc.superClass) || "totalcross/Service".equals(jc.superClass)) {
      return true;
    }
    if (jc.interfaces != null) {
      for (int i = 0; i < jc.interfaces.length; i++) {
        if (jc.interfaces[i].equals("totalcross/MainClass")) {
          return true;
        }
      }
    }
    return false;
  }

  public static Map<String, JavaClass> getJavaClassFromJar(JarFile jarFile) throws Exception {
    Map<String, JavaClass> ret = new HashMap<String, JavaClass>();
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();

      String entryName = entry.getName();
      if (entryName.endsWith(".class")) {
        ClassNode classNode = new ClassNode();

        InputStream classFileInputStream = jarFile.getInputStream(entry);
        try {
          ClassReader classReader = new ClassReader(classFileInputStream);
          classReader.accept(classNode, 0);
        } finally {
          classFileInputStream.close();
        }
        ret.put(classNode.name, new JavaClass(classNode));
      }
    }
    return ret;
  }
}
