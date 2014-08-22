/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter;

import tc.*;

import totalcross.io.*;
import totalcross.util.*;

/** Constant pool specification.

Global constant pool:
   htI32    - each entry is stored as int32    - int constants.
   htI64    - each entry is stored as int64    - long constants.
   htDbl    - each entry is stored as double   - double constants.
   htStr    - each entry is stored as char16*  - String constants.
   htCls    - each entry is stored as char8*   - class names identifiers.
   htMtdRef - each entry is stored as uint16*  - references for methods:
                                                 class name index (=), method name index (pushed into htMtd), index for each
                                                 parameter (taken from htCls or htConst - if class or primitive type, respectively).
   htSF - each entry is stored as uint32       - references for static fields, stored as (class << 16) | field:
                                                 class name index (pushed into htCls) and field name index (pushed into htMtd)
   htIF - each entry is stored as uint32       - references for instance fields, stored as (class << 16) | field:
                                                 class name index (pushed into htCls) and field name index (pushed into htMtd)

Important: position 0 of the Contant Pool must not be used. Symbols must start from index 1.

*/

public class GlobalConstantPool implements tc.tools.converter.tclass.TClassConstants
{
   private static int dCount,lCount;
   private static Hashtable htI32    = new Hashtable(2000);
   private static Hashtable htI64    = new Hashtable(2000);
   private static Hashtable htDbl    = new Hashtable(2000);
   private static Hashtable htStr    = new Hashtable(2000); // String constants
   private static Hashtable htMtdFld = new Hashtable(2000); // method and field names
   private static Hashtable htMtd    = new Hashtable(2000); // methods
   private static Hashtable htSF     = new Hashtable(2000); // static fields
   private static Hashtable htIF     = new Hashtable(2000); // instance fields
   private static Hashtable htCls    = new Hashtable(2000); // class and primitive references/names
   private static Vector vI32    = new Vector(2000);
   private static Vector vI64    = new Vector(2000);
   private static Vector vDbl    = new Vector(2000);
   private static Vector vStr    = new Vector(2000);
   private static Vector vMtd    = new Vector(2000);
   private static Vector vSF     = new Vector(2000);
   private static Vector vIF     = new Vector(2000);
   private static Vector vCls     = new Vector(2000);
   private static Vector vMtdFld  = new Vector(6000);
   static Hashtable obfuscatedClasses = new Hashtable(100);
   public static boolean checkLimit;
   private static final String limitReached1 =
      "\nThe maximum number of ";
   private static final String limitReached2 =
      " for a TCZ file has been reached. Now you have to options two bypass this limitation:\n\n" +
      "1) You will have to split your TCZ into two files, one is the main file, and the other is a library file. If you name the library file with 'Lib' suffix, it will be automatically loaded when your application starts. Otherwise, you can name it with any name and call Vm.attachLibrary to dynamically bind it.\n\n" +
      "2) Another option is to obfuscate your code using Proguard. Why obfuscation will work? Because it will rename all private and package access fields, methods and classes with the same name, thus, will decrease the number of identifiers.\n" +
      "First, download Proguard 3.9 from here:\n" +
      "www.totalcross.com/proguard3.9.zip\n" +
      "Inside that zip you have proguard.jar, its license, and a build.xml ANT script.\n" +
      "To use the script, you must create a jar file named in.jar, with all classes and files that your application uses, which will be used by the script as the input file. Once the obfuscation finishes, an out.jar file will be written. Also a desktop_map.txt file is also written; this file has the mapping between the original name and the obfuscated one.\n" +
      "To run the script, just type \"ant\" from the command line. Then, rename the out.jar file to your application's main window name and call tc.Deploy passing it. Here's an example, assuming that the class that extends MainWindow is SalesForce:\n\n" +
      "jar cvf in.jar bin\\*.class\n" +
      "ant\n" +
      "ren out.jar SalesForce.jar\n" +
      "java tc.Deploy SalesForce.jar -all\n\n" +
      "Note that all this can be do from a single ANT script, if you are proficient in ANT.\n\n" +
      "Note also that this limitation will not be applied if you run tc.Deploy only for BlackBerry, using -bb option.\n";

   private static TCValue temp = new TCValue();
   private static final String DefaultConstants[]  =
   {
      "&V",
      "&b", // boolean
      "&B", // byte
      "&C",
      "&S",
      "&I",
      "&L",
      "&F",
      "&D",
      "java.lang.String",
      "java.lang.Object",
      "[&b",
      "[&B",
      "[&C",
      "[&S",
      "[&I",
      "[&L",
      "[&F",
      "[&D",
      "[java.lang.String",
      "[java.lang.Object",
      "java.lang.Array",
      CONSTRUCTOR_NAME,
      STATIC_INIT_NAME,
   };

   public static void init()
   {
      htI32.clear();
      htI64.clear();
      htDbl.clear();
      htStr.clear();
      htMtdFld.clear();
      htMtd.clear();
      htSF.clear();
      htIF.clear();
      vI32.removeAllElements();
      vI64.removeAllElements();
      vDbl.removeAllElements();
      vStr.removeAllElements();
      vMtdFld.removeAllElements();
      vMtd.removeAllElements();
      vSF.removeAllElements();
      vIF.removeAllElements();
      // make all arrays start from 1
      dCount = lCount = 1;
      vI32.addElement(temp);
      vI64.addElement(temp);
      vDbl.addElement(temp);
      vCls.addElement(temp);
      vStr.addElement(temp);
      vMtd.addElement(temp);
      vSF.addElement(temp);
      vIF.addElement(temp);
      vMtdFld.addElement(temp);
      // add the default constants
      for (int i =0; i < DefaultConstants.length; i++)
         put(DefaultConstants[i], POOL_CLS, htCls, vCls);
   }

/*   public static void mark(TCMethod m)
   {
      m.vI32Start = vI32.size();
      m.vI64Start = vI64.size();
      m.vDblStart = vDbl.size();
      m.vStrStart = vStr.size();
      m.vCstStart = vConst.size();
   }
*/
/*   public static boolean canRemoveSymbol(TCMethod m, int idx, int type)
   {
      switch (type)
      {
         case POOL_I32:
            return idx >= m.vI32Start;
         case POOL_I64:
            return idx >= m.vI64Start;
         case POOL_DBL:
            return idx >= m.vDblStart;
         case POOL_STR:
            return idx >= m.vStrStart;
         case POOL_SYM:
            return idx >= m.vCstStart;
      }
      return false;
   }

   public static void removeSymbol(int idx, int type)
   {
      switch (type)
      {
         case POOL_I32:
            temp.set(((TCValue)vI32.items[idx]).asInt);
            htI32.remove(temp);
            vI32.removeElementAt(idx);
            break;
         case POOL_I64:
            temp.set(((TCValue)vI64.items[idx]).asLong);
            htI64.remove(temp);
            vI64.removeElementAt(idx);
            break;
         case POOL_DBL:
            temp.set(((TCValue)vDbl.items[idx]).asDouble);
            htDbl.remove(temp);
            vDbl.removeElementAt(idx);
            break;
         case POOL_STR:
            String v = ((TCValue)vStr.items[idx]).asStr;
            htStr.remove(v);
            vStr.removeElementAt(idx);
            break;
         case POOL_SYM:
            v = ((TCValue)vConst.items[idx]).asStr;
            htConst.remove(v);
            vConst.removeElementAt(idx);
            break;
      }
   }
*/
   public static int put(int value)
   {
      temp.set(value); // set a temp value
      TCValue v = (TCValue)htI32.get(temp);
      if (v == null)
      {
         v = new TCValue(value); // new TCValue
         htI32.put(v,v);           // put it in the hashtable (used in put)
         vI32.addElement(v);       // add it to the vector (used in get)
         v.index = htI32.size(); // set the index and return it
         if (v.index >= 65530)
         {
            if (Deploy.isOnlyBB())
               v.index = 65529;
            else
               throw new ConverterException(limitReached1+"int constants"+limitReached2);
         }
      }
      return v.index; // return the current index if it was already put
   }

   public static int put(long value)
   {
      temp.set(value);
      TCValue v = (TCValue)htI64.get(temp);
      if (v == null)
      {
         v = new TCValue(value); // new TCValue
         htI64.put(v,v);           // put it in the hashtable (used in put)
         vI64.addElement(v);       // add it to the vector (used in get)
         v.index = htI64.size(); // set the index and return it
         lCount++;
         if (v.index >= 65530)
         {
            if (Deploy.isOnlyBB())
               v.index = 65529;
            else
               throw new ConverterException(limitReached1+"long constants"+limitReached2);
         }
      }
      return v.index; // return the current index if it was already put
   }

   public static int put(double value)
   {
      temp.set(value);
      TCValue v = (TCValue)htDbl.get(temp);
      if (v == null)
      {
         v = new TCValue(value); // new TCValue
         htDbl.put(v,v);           // put it in the hashtable (used in put)
         vDbl.addElement(v);       // add it to the vector (used in get)
         v.index = htDbl.size(); // set the index and return it
         dCount++;
         if (v.index >= 65530)
         {
            if (Deploy.isOnlyBB())
               v.index = 65529;
            else
               throw new ConverterException(limitReached1+"double constants"+limitReached2);
         }
      }
      return v.index; // return the current index if it was already put
   }

   static String javaPrimitiveType2TCType(String value)
   {
      switch (value.charAt(0))
      {
         case 'V': return "&V";
         case 'Z': return "&b";
         case 'B': return "&B";
         case 'C': return "&C";
         case 'S': return "&S";
         case 'I': return "&I";
         case 'J': return "&L";
         case 'F': return "&D"; // guich@tc100b3_2: changed F to D. without this change, "float f = 0.1f; String s = "test"+f;" results in calling "StringBuffer.append(F)", which does not exist on device, since all floats are converted to double
         case 'D': return "&D";
      }
      return null;
   }

   public static String getJavaTypeName(String s)
   {
      if (s.length() == 0)
         return "";
      switch (s.charAt(0))
      {
         case '&':
            return getPrimitiveJavaName(s);
         case '[': // [[[ -> [][][]
         {
            String c = "";
            int i =0, n = s.length();
            for (; i < n; i++)
               if (s.charAt(i) == '[')
                  c += "[]";
               else
                  break;
            return (i == n ? "" : getJavaTypeName(s.substring(i))) + c;
         }
         default: return s;
      }
   }

   public static String getPrimitiveJavaName(String js)
   {
      switch (js.charAt(1))
      {
         case 'b': return "boolean";
         case 'C': return "char";
         case 'B': return "byte";
         case 'S': return "short";
         case 'I': return "int";
         case 'L': return "long";
         case 'F': return "float";
         case 'D': return "double";
      }
      return "*none*";
   }

   private static String javaType2TCType(String value)
   {
      value = value.replace('/','.'); // java use java/lang/String, we use java.lang.String
      int len = value.length();
      char last  = value.charAt(len-1);
      String prefix="";
      while (value.charAt(0) == '[')
      {
         prefix += '[';
         value = value.substring(1);
      }
      len = value.length();

      if (last == ';') // class name? Lxxx; -> xxx
      {
         value = value.substring(1,len-1);
         if (value.endsWith("4D"))
            value = value.substring(0,value.length()-2);
         if (value.startsWith("totalcross.lang"))
            value = totalcross.sys.Convert.replace(value, "totalcross.lang", "java.lang");
      }
      else
      {
         // surely a primitive-type array
         if (last == 'Z') // boolean
            last = 'b';
         else
         if (last == 'J') // long
            last = 'L';
         value = "&"+last;
      }
      return prefix+value;
   }

   // for a method: putMtd("java.lang.String","substring",{"I","I"}, "substring(II)")
   // method's parameters must be in Java's format (L...; for class)
   public static int putMethod(String className, String name, String[] params, String signature) // puts an reference for a method
   {
      // normalize everything
      if (name.equals("<init>")) name = CONSTRUCTOR_NAME; else
      if (name.equals("<clinit>")) name = STATIC_INIT_NAME;

      String value = className+"|"+signature;
      temp.set(value, POOL_MTD);
      // check if it was already added
      TCValue v = (TCValue)htMtd.get(temp);
      if (v != null)
         return v.index;

      int plen = params == null ? 0 : params.length;

      TCValue i = new TCValue(value, POOL_MTD);
      htMtd.put(i,i);
      i.index = vMtd.size(); // before adding - index 0 was already added
      if (i.index >= 4095)
      {
         if (Deploy.isOnlyBB())
            i.index = 4094;
         else
            throw new ConverterException(limitReached1+"methods"+limitReached2);
      }
      vMtd.addElement(i);
      // store the array with all parameters in the unused asObj
      int[] all = new int[plen+2];
      all[0] = putCls(className);
      all[1] = putMethodOrFieldName(name);
      for (int j = 0; j < plen; j++)
      {
         String p = params[j];
         if (p.charAt(0) == 'L' || p.charAt(0) == '[') // class or array
            all[j+2] = putParam(p);
         else
         {
            p = javaPrimitiveType2TCType(p);
            TCValue tc = (TCValue)htCls.get(p);
            all[j+2] = tc.index;
         }
      }
      i.asObj = all;
      return i.index;
   }

   // e.g. for a field : putFld("java.lang.String","fieldname")
   // instance fields must be stored in a
   public static int putStaticField(String className, String name) // puts a reference for a static field
   {
      return putField(htSF, vSF, POOL_SF, className, name);
   }

   public static int putInstanceField(String className, String name) // puts a reference for a instance field
   {
      return putField(htIF, vIF, POOL_IF, className, name);
   }

   private static int putField(Hashtable ht, Vector v, int type, String className, String name) // puts a reference for a field
   {
      String value = className+"|"+name;
      temp.set(value, type);
      // check if it was already added
      TCValue vv = (TCValue)ht.get(temp);
      if (vv != null)
         return vv.index;

      TCValue i = new TCValue(value, type);
      ht.put(i,i);
      i.index = v.size(); // before adding - index 0 was already added
      if (type == POOL_SF && i.index >= 32700)
      {
         if (Deploy.isOnlyBB())
            i.index = 32699;
         else
            throw new ConverterException(limitReached1+"static fields"+limitReached2);
      }
      if (type == POOL_IF && i.index >= 4095)
      {
         if (Deploy.isOnlyBB())
            i.index = 4094;
         else
            throw new ConverterException(limitReached1+"instance fields"+limitReached2);
      }
         
      v.addElement(i);
      // store the class and field index as a single int32
      int c = putCls(className);
      int f = putMethodOrFieldName(name); // field name was already obfuscated during field convertion (which comes before method convertion)
      i.asInt = (c << 16) | f;
      return i.index;
   }

   public static int putClsOrParam(String value) // value is sure a Java type, but it may be an array or a class without the Lxxx;
   {
      return value.charAt(0) == '[' ? putParam(value) : putCls(value);
   }

   public static int putCls(String value) // pure class name: value must be a single class, no arrays, no Lxxx; !
   {
      if (value.endsWith("4D"))
         value = value.substring(0,value.length()-2);
      if (((value.charAt(0) == 'L' && value.charAt(value.length()-1) == ';') || value.charAt(0) == '['))
         System.err.println("*Class is incorrect* "+value);
      value = value.replace('/','.');
      
      if (value.equals("java.util.Vector")) value = "totalcross.util.Vector";
      else
      if (value.equals("java.util.Hashtable")) value = "totalcross.util.Hashtable";
      else
      if (value.indexOf("StringBuilder") >= 0)
         value = value.replaceFirst("StringBuilder", "StringBuffer");

      if (htCls.exists(value))
         return ((TCValue)htCls.get(value)).index;

      if (value.startsWith("totalcross.lang."))
         value = totalcross.sys.Convert.replace(value, "totalcross.lang.", "java.lang.");
      int idx = put(value, POOL_CLS, htCls, vCls);
      if (idx >= 4095 && checkLimit) // guich@tc110_23: corrected limit
      {
         if (Deploy.isOnlyBB())
            idx = 4094;
         else
            throw new ConverterException(limitReached1+"class references"+limitReached2);
      }
      return idx;
   }

   public static int putParam(String value) // part of a method signature - value = (Lxxx; or [zzz)
   {
      if (value.charAt(0) != 'L' && value.charAt(0) != '[')
         System.out.println("*Param is incorrect* "+value);
      value = javaType2TCType(value);
      if (htCls.exists(value))
         return ((TCValue)htCls.get(value)).index;

      int idx = put(value, POOL_CLS, htCls, vCls);
      if (idx >= 4095 && checkLimit) // guich@tc110_23: corrected limit
      {
         if (Deploy.isOnlyBB())
            idx = 4094;
         else
            throw new ConverterException(limitReached1+"identifiers"+limitReached2);
      }
      return idx;
   }

   public static int putMethodOrFieldName(String value) // puts the name of a field or method in the identifiers array
   {
      if (value.equals("<init>"))   value = CONSTRUCTOR_NAME; else
      if (value.equals("<clinit>")) value = STATIC_INIT_NAME;

      if (htMtdFld.exists(value))
         return ((TCValue)htMtdFld.get(value)).index;

      int idx = put(value, POOL_SYM, htMtdFld, vMtdFld);
      if (idx > 32700 && checkLimit) // guich@tc110_23: corrected limit
      {
         if (Deploy.isOnlyBB())
            idx = 32699;
         else
            throw new ConverterException(limitReached1+"identifiers"+limitReached2);
      }
      return idx;
   }

   public static int putStr(String value) // mainly Strings
   {
      return put(value, POOL_STR, htStr, vStr);
   }

   private static int put(String value, int type, Hashtable ht, Vector v)
   {
      if (v == vMtdFld && value.length() > 255)
         throw new ConverterException("Identifier '"+value+"' too long. Size is limited to 255 characters, but it has "+value.length());
      temp.set(value, type);
      // first, check if it was already added
      TCValue i = (TCValue)ht.get(temp);
      if (i == null)
      {
         i = new TCValue(value, type);
         ht.put(i,i);
         i.index = v.size(); // before adding - index 0 was already added
         v.addElement(i);
      }
      return i.index;
   }

   public static String getString(int index)
   {
      try
      {
         return ((TCValue)vStr.items[index]).asStr;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static String getMethodFieldName(int index)
   {
      try
      {
         return ((TCValue)vMtdFld.items[index]).asStr;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static String getClassName(int index)
   {
      try
      {
         return ((TCValue)vCls.items[index]).asStr;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static String getType(int index)
   {
      try
      {
         String s = ((TCValue)vCls.items[index]).asStr;
         if (s.charAt(0) == '&' || (s.charAt(0) == '[' && s.charAt(1) == '&')) // primitive type?
            return getJavaTypeName(s);
         return s;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static TCValue getMtdRef(int index)
   {
      try
      {
         return (TCValue)vMtd.items[index];
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static String getMtdName(int index)
   {
      try
      {
         return ((TCValue)vMtd.items[index]).asStr;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   public static int getStaticField(int index)
   {
      try
      {
         return ((TCValue)vSF.items[index]).asInt;
      }
      catch (Exception e)
      {
         return -1;
      }
   }

   public static int getInstanceField(int index)
   {
      try
      {
         return ((TCValue)vIF.items[index]).asInt;
      }
      catch (Exception e)
      {
         return -1;
      }
   }

   public static int getClassIndex(String ident)
   {
      try
      {
         ident = ident.replace('/','.');
         if (ident.endsWith("4D"))
            ident = ident.substring(0,ident.length()-2);
         if (ident.startsWith("totalcross.lang."))
            ident = totalcross.sys.Convert.replace(ident, "totalcross.lang.", "java.lang.");
         return ((TCValue)htCls.get(ident.hashCode())).index;
      }
      catch (Exception ee)
      {
         throw new ConverterException("CLASS OR PRIMITIVE NOT FOUND: "+ident);
      }
   }

   public static int getPrimitiveTypeIndex(String t)
   {
      try
      {
         TCValue tcv = (TCValue)htCls.get(t);
         return tcv.index;
      }
      catch (Exception e)
      {
         throw new ConverterException("PRIMITIVE TYPE NOT FOUND: " + t);
      }
   }

   public static void write(DataStreamLE ds) throws totalcross.io.IOException
   {
      TCValue v;
      int i32Count    = vI32.size();
      int dblCount    = dCount;
      int i64Count    = lCount;
      int mtdCount    = vMtd.size();
      int clsCount    = vCls.size();
      int SFCount     = vSF.size();
      int IFCount     = vIF.size();
      int mtdfldCount = vMtdFld.size();
      int strCount    = vStr.size();
      ds.writeShort(i32Count);
      ds.writeShort(i64Count);
      ds.writeShort(dblCount);
      ds.writeShort(clsCount);
      ds.writeShort(SFCount);
      ds.writeShort(IFCount);
      ds.writeShort(mtdCount);
      ds.writeShort(mtdfldCount);
      ds.writeShort(strCount);
      int i;
      boolean dump = J2TC.dump;
      if (dump) System.out.println("\nConstant pool");
      int size = 0,si,sl,sd,ssf,sif,sm,smf,sc,ss,partSize;
      // write the constant pool arrays
      // note that the first element of each array is always zero, so it isnt written

      // int
      si = size;
      for (i = 1; i < i32Count; i++)
      {
         v = (TCValue)vI32.items[i];
         size += ds.writeInt(v.asInt);
         if (dump) System.out.println("i32["+v.index+"] : "+v.asInt);
      }
      si = size - si;
      sl = size;
      // long
      for (i = 1; i < i64Count; i++)
      {
         v = (TCValue)vI64.items[i];
         size += ds.writeLong(v.asLong);
         if (dump) System.out.println("i64["+v.index+"] : "+v.asLong);
      }
      sl = size - sl;
      sd = size;
      // double
      for (i = 1; i < dblCount; i++)
      {
         v = (TCValue)vDbl.items[i];
         size += ds.writeDouble(v.asDouble);
         if (dump) System.out.println("dbl["+v.index+"] : "+v.asDouble);
      }
      sd = size - sd;
      ssf = size;
      // static fields
      int last =0,now;
      for (i = 1; i < SFCount; i++) // field
      {
         v = (TCValue)vSF.items[i];
         now = v.asInt & 0xFFFF;
         size += ds.writeShort(now-last);
         last = now;
      }
      for (i = 1; i < SFCount; i++) // class
      {
         v = (TCValue)vSF.items[i];
         now= (v.asInt >> 16) & 0xFFFF;
         size += ds.writeShort(now);
         if (dump) System.out.println("SF["+v.index+"] : "+((v.asInt >> 16) & 0xFFFF)+" "+(v.asInt & 0xFFFF));
      }
      ssf = size - ssf;
      sif = size;
      // instance fields
      last = 0;
      for (i = 1; i < IFCount; i++) // field
      {
         v = (TCValue)vIF.items[i];
         now = v.asInt & 0xFFFF;
         size += ds.writeShort(now-last);
         last = now;
      }
      for (i = 1; i < IFCount; i++) // class
      {
         v = (TCValue)vIF.items[i];
         now= (v.asInt >> 16) & 0xFFFF;
         size += ds.writeShort(now);
         if (dump) System.out.println("IF["+v.index+"] : "+((v.asInt >> 16) & 0xFFFF)+" "+(v.asInt & 0xFFFF));
      }
      sif = size - sif;
      sm = size;
      // methods
      partSize = 0;
      for (i = 1; i < mtdCount; i++)
      {
         v = (TCValue)vMtd.items[i];
         int[] s = (int[])v.asObj;
         size += ds.writeByte(s.length-2); // min length is 2, subtracting 2 improves compression
         partSize += s.length * 2;
      }
      if (mtdCount > 1) ds.writeInt(partSize);
      for (i = 1; i < mtdCount; i++)
      {
         v = (TCValue)vMtd.items[i];
         int[] s = (int[])v.asObj;
         size += Storage.writeUnsignedShortArray(ds, s);
         if (dump) {System.out.print("Mtd["+v.index+"] : "); for (int j=0; j < s.length; j++) System.out.print(s[j]+" "); System.out.println();}
      }
      sm = size - sm;
      smf = size;
      partSize = 0;
      // method and field names
      for (i = 1; i < mtdfldCount; i++)
      {
         v = (TCValue)vMtdFld.items[i];
         partSize += 1 + v.asStr.length();
      }
      ds.writeInt(partSize);
      for (i = 1; i < mtdfldCount; i++)
      {
         v = (TCValue)vMtdFld.items[i];
         size += ds.writeSmallString(v.asStr);
         if (dump) System.out.println("mtdfld["+v.index+"] : "+v.asStr);
      }
      smf = size - smf;
      sc = size;
      partSize = 0;
      // class names
      for (i = 1; i < clsCount; i++)
      {
         v = (TCValue)vCls.items[i];
         // convert sql classes
         if (v.asStr.equals("java.sql.SQLException"))
            v.asStr = "totalcross.sql.SQLException";
         else
         if (v.asStr.equals("java.sql.SQLWarning"))
            v.asStr = "totalcross.sql.SQLWarning";

         String s = v.asStr.charAt(0) == '&' ? v.asStr.substring(1) : v.asStr; // remove the & from the primitives
         partSize += 1 + s.length();
      }
      ds.writeInt(partSize);
      for (i = 1; i < clsCount; i++)
      {
         v = (TCValue)vCls.items[i];
         String s = v.asStr.charAt(0) == '&' ? v.asStr.substring(1) : v.asStr; // remove the & from the primitives
         size += ds.writeSmallString(s);
         if (dump) System.out.println("Cls["+v.index+"] : "+s);
      }
      sc = size - sc;
      ss = size;
      // Strings
      for (i = 1; i < strCount; i++) // String constants must be the last written
      {
         v = (TCValue)vStr.items[i];
         String s = v.asStr;
         int s0 = size;
         int l = s.length();
         boolean can = true;
         for (int j= l; --j >= 0;)
            can &= (int)s.charAt(j) <= 255;
         if (can && l < 254) // no unicode chars and below 254 in length?
            size += ds.writeSmallString(s);
         else
         {
            if (can && l > 255)
            {
               size += ds.writeByte(254); // mark
               size += ds.writeShort(l);
               for (int j = 0; j < l; j++)
                  ds.writeByte(s.charAt(j));
               size += l;
            }
            else // !can or l=254 or l=255
            {
               size += ds.writeByte(255); // mark
               size += Storage.writeChars(ds, s);
            }
         }
         if (dump) System.out.println("str["+v.index+"] : "+s+" "+(size-s0));
      }
      ss = size - ss;

      if (J2TC.dump) System.out.println("\nConstant pool size: "+size+" (int="+si+",long="+sl+",double="+sd+",static field="+ssf+",instance field="+sif+",method ref="+sm+",method/field names="+smf+",class names="+sc+",string="+ss+")\n");
      if (clsCount > 1000 || mtdCount > 1000 || SFCount > 1000 || IFCount > 1000) System.out.println("Total References - Classes: "+clsCount+", Methods: "+mtdCount+", instance fields: "+IFCount+" (each one limited to 4095)");
   }

   public static boolean isEmpty() // guich@tc111_2: returns if it contains at least one user class
   {
      return vCls.size() == (DefaultConstants.length+1);
   }
}
