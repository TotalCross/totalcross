/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: J2BB.java,v 1.45 2011-01-04 13:19:21 guich Exp $

package tc.tools.converter.bb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import tc.tools.converter.bb.attribute.Code;
import tc.tools.converter.bb.attribute.InnerClasses;
import tc.tools.converter.bb.attribute.LineNumberTable;
import tc.tools.converter.bb.attribute.LocalVariableTable;
import tc.tools.converter.bb.constant.Class;
import tc.tools.converter.bb.constant.MethodRef;
import tc.tools.converter.bb.constant.NameAndType;
import tc.tools.converter.bb.constant.UTF8;
import tc.tools.deployer.Utils;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.util.Vector;
import totalcross.util.zip.TCZ;

public class J2BB
{
   private static byte[] buf = new byte[2048];
   private static StringBuffer sb = new StringBuffer(1024);
   private static Vector v = new Vector();
   private static byte[] fourBytes = new byte[4];
   
   private static class ReplaceMethodEntry
   {
      public String className;
      public String name;
      public String descriptor;
      public String targetName;
      
      public ReplaceMethodEntry(String className, String name, String descriptor, String targetName)
      {
         this.className = className;
         this.name = name;
         this.descriptor = descriptor;
         this.targetName = targetName;
      }
   }
   
   private static class UTF8ReplaceEntry
   {
      public String originalValue;
      public String replacedValue;
      public Vector skip;
      
      public UTF8ReplaceEntry(String originalValue, String replacedValue, String[] skip)
      {
         this.originalValue = originalValue;
         this.replacedValue = replacedValue;
         this.skip = skip == null ? new Vector() : new Vector(skip);
      }
   }
   
   private static Vector methodReplaces;
   private static Hashtable utf8Replaces;
   
   static // initialize static replacements
   {
      methodReplaces = new Vector();
      methodReplaces.addElement(new ReplaceMethodEntry("java/lang/String", "<init>", "([B)V", "stringInit"));
      methodReplaces.addElement(new ReplaceMethodEntry("java/lang/String", "<init>", "([BII)V", "stringInit"));
      methodReplaces.addElement(new ReplaceMethodEntry("java/lang/String", "lastIndexOf", "(Ljava/lang/String;)I", "stringLastIndexOf"));
      methodReplaces.addElement(new ReplaceMethodEntry("java/lang/String", "lastIndexOf", "(Ljava/lang/String;I)I", "stringLastIndexOf"));
      
      utf8Replaces = new Hashtable();
      utf8Replaces.put("java/lang/Math", new UTF8ReplaceEntry("java/lang/Math", "totalcross/lang/Math", new String[] {"totalcross/lang/Math"}));
   }

   public static File process(Vector entriesList, File targetDir) throws IOException, totalcross.io.IOException, ClassConsistencyException, InvalidClassException
   {
      File output = new File(targetDir, "output.jar");
      if (output.exists() && !output.delete())
         throw new IOException("Cannot delete file: " + output.getPath());

      JarOutputStream jos = new JarOutputStream(new FileOutputStream(output));
      int n = entriesList.size();

      for (int i = 0; i < n; i ++) // pack files into a JAR
      {
         TCZ.Entry e = (TCZ.Entry) entriesList.items[i];
         if (!e.name.endsWith(".class") || isValidClass(e, entriesList))
         {
            jos.putNextEntry(new JarEntry(e.name));
            jos.write(e.bytes);
            jos.closeEntry();
         }
      }

      jos.close();

      convert(output);

      return output;
   }

   private static boolean isValidClass(TCZ.Entry e, Vector entriesList) throws totalcross.io.IOException, ClassConsistencyException, InvalidClassException
   {
      if (e.name.startsWith("java/lang/"))
         return false;
      else if (e.name.matches(".+4D(\\x24.+)?\\x2E[Cc][Ll][Aa][Ss][Ss]"))
         return false;
      else
      {
         JavaClass thisClass = new JavaClass();
         thisClass.load(new DataStream(new ByteArrayStream(e.bytes)));
         String name = thisClass.getClassName();
         
         if (e.name.matches(".+4B(\\x24.+)?\\x2E[Cc][Ll][Aa][Ss][Ss]"))
         {
            name = bb2Desktop(e.name.substring(0, e.name.length() - 6));
            if (!name.startsWith("totalcross/Launcher")) // never checks Launchers for consistency
            {
               TCZ.Entry other = getClass(entriesList, name);
               if (other != null)
               {
                  JavaClass sdkClass = new JavaClass();
                  sdkClass.load(new DataStream(new ByteArrayStream(other.bytes)));
   
                  InnerClasses.Classes inner = sdkClass.asInnerClass();
                  if (inner == null || inner.innerName != null && !inner.isPrivate())
                  {
                     Utils.println("Checking consistency of " + thisClass.getClassName() + "...");
                     sdkClass.assertConsistency(thisClass);
                  }
               }
            }
         }
         else
         {
            int iIdx = name.indexOf('$');
            if (iIdx >= 0)
               name = name.substring(0, iIdx);
   
            if (getClass(entriesList, name + "4B") == null)
            {
               if ((thisClass.getPlatforms() & JavaClass.PLATFORM_BLACKBERRY) == JavaClass.PLATFORM_BLACKBERRY)
               {
                  Utils.println("Checking consistency of " + thisClass.getClassName() + "...");
                  thisClass.assertConsistency();
               }
            }
            else
               return false;
         }
         
         if (!name.equals("Stub")) // if this class is not a Stub, it cannot have a main method
         {
            JavaMethod[] main = thisClass.getMethod("main", "([Ljava/lang/String;)V");
            if (main != null)
            {
               for (int i = main.length - 1; i >= 0; i--)
                  if ((main[i].accessFlags & JavaMethod.ACC_STATIC) == JavaMethod.ACC_STATIC)
                     throw new InvalidClassException(thisClass, "Only the BlackBerry's stub class can define a 'public static void main(String args[])' method. Remove the method from this class.");
            }
         }
         
         return true;
      }
   }

   private static TCZ.Entry getClass(Vector entriesList, String name)
   {
      TCZ.Entry e;
      int n = entriesList.size();

      for (int i = 0; i < n; i ++)
      {
         e = (TCZ.Entry)entriesList.items[i];
         if (e.name.toLowerCase().endsWith(".class") && e.name.substring(0, e.name.length() - 6).equals(name))
            return e;
      }

      return null;
   }

   /**
    * @param input
    * @throws IOException
    */
   private static void convert(File input) throws IOException, totalcross.io.IOException, ClassConsistencyException
   {
      input = input.getCanonicalFile();
      File backup = new File(input.getParent(), input.getName() + ".bak");
      if (backup.exists() && !backup.delete())
         throw new IOException("Cannot delete file: " + backup.getPath());
      if (!input.renameTo(backup))
         throw new IOException("Cannot rename file: " + input.getPath() + " to " + backup.getPath());

      // First, analyze classes
      ZipInputStream zis = new ZipInputStream(new FileInputStream(backup));
      ZipEntry ze;

      while ((ze = zis.getNextEntry()) != null)
         if (ze.getName().toLowerCase().endsWith(".class"))
            analize(ze, zis);

      zis.close();

      // Then, convert all classes
      zis = new ZipInputStream(new FileInputStream(backup));
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(input));

      while ((ze = zis.getNextEntry()) != null)
      {
         String name = ze.getName();
         name = name.substring(0, name.lastIndexOf('.'));
         name = utf8Replaces.containsKey(name) ? ((UTF8ReplaceEntry)utf8Replaces.get(name)).replacedValue + ".class" : ze.getName();
         zos.putNextEntry(new ZipEntry(name));

         if (name.toLowerCase().endsWith(".class"))
            convert(zis, zos);
         else
         {
            int r;
            while ((r = zis.read(buf)) > 0)
               zos.write(buf, 0, r);
         }

         zos.closeEntry();
      }

      zis.close();
      zos.close();

      backup.delete();
   }

   private static void analize(ZipEntry ze, InputStream is) throws IOException
   {
      String original = ze.getName();
      original = original.substring(0, original.lastIndexOf('.'));
      String name = original;

      if (name.matches(".+4B(\\x24.+)?"))
         utf8Replaces.put(original, new UTF8ReplaceEntry(original, bb2Desktop(name), null));
   }

   private static void convert(InputStream is, OutputStream os) throws IOException, totalcross.io.IOException
   {
      ByteArrayStream bas = new ByteArrayStream(1024);
      DataStream ds = new DataStream(bas);
      int total, r;

      while ((r = is.read(buf)) > 0)
         bas.writeBytes(buf, 0, r);
      bas.setPos(0);

      JavaClass jclass = new JavaClass();
      jclass.load(ds);

      checkMethods(jclass);
      checkConstantPool(jclass);
      checkFinalizable(jclass);
      checkMethodReplacement(jclass); // bruno@tc123_8

      bas.reuse();
      jclass.save(ds);
      total = bas.getPos();
      bas.setPos(0);

      while (bas.getPos() < total)
      {
         r = total - bas.getPos();
         if (r > buf.length)
            r = buf.length;

         bas.readBytes(buf, 0, r);
         os.write(buf, 0, r);
      }
   }
   
   private static void checkMethodReplacement(JavaClass jclass)
   {
      int replaceCount = methodReplaces.size();
      JavaConstant utf8Launcher4BName = null;
      JavaConstant classLauncher4B = null;
      JavaConstant[] utf8MethodName = new JavaConstant[replaceCount];
      JavaConstant[] utf8MethodDescriptor = new JavaConstant[replaceCount];
      JavaConstant[] ntMethod = new JavaConstant[replaceCount];
      JavaConstant[] mrMethod = new JavaConstant[replaceCount];
      
      JavaMethod[] methods = jclass.getMethod(null, null);
      if (methods != null) // all methods
      {
         for (int i = 0; i < methods.length; i++)
         {
            if (methods[i].getCode() != null)
            {
               Code info = (Code)methods[i].getCode().info;
               byte[] b = info.code;
               
               for (int j = 0; j < replaceCount; j++)
               {
                  ReplaceMethodEntry methodReplace = (ReplaceMethodEntry)methodReplaces.items[j];
                  boolean isInit = methodReplace.name.equals("<init>");
                  
                  int newPc = -1, pc = 0, count = b.length;
                  for (; pc < count;)
                  {
                     int op = (int)b[pc] & 0xFF;
                     if (isInit && op == 187) // new
                     {
                        Class c = (Class)jclass.getConstant(b[pc + 1], b[pc + 2], null).info;
                        if (c.getValueAsName().value.equals(methodReplace.className))
                           newPc = pc;
                        pc += 3;
                     }
                     else if (op == 182 || (op == 183 && newPc != -1)) // invokevirtual OR invokespecial
                     {
                        MethodRef ref = (MethodRef)jclass.getConstant(b[pc + 1], b[pc + 2], null).info;
                        Class c = ref.getValue1AsClass();
                        NameAndType nt = ref.getValue2AsNameAndType();
                        
                        if (c.getValueAsName().value.equals(methodReplace.className) && nt.getValue1AsName().value.equals(methodReplace.name) && (nt.getValue2AsDescriptor().value.equals(methodReplace.descriptor)))
                        {
                           Utils.println("Replacing method call at " + jclass.toString() + "." + methods[i].toString() + " (PC " + pc + ")...");
                           int methodRefIndex;
                           
                           if (classLauncher4B == null)
                           {
                              utf8Launcher4BName = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "totalcross/Launcher", null);
                              classLauncher4B = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_CLASS, utf8Launcher4BName, null);
                           }
                           if (utf8MethodName[j] == null)
                              utf8MethodName[j] = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, methodReplace.targetName, null);
                           
                           if (mrMethod[j] == null)
                           {
                              String desc = isInit ? methodReplace.descriptor.substring(0, methodReplace.descriptor.indexOf(')') + 1) + "L" + methodReplace.className + ";" : "(L" + methodReplace.className + ";" + methodReplace.descriptor.substring(1);
                              utf8MethodDescriptor[j] = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, desc, null);
                              ntMethod[j] = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_NAME_AND_TYPE, utf8MethodName[j], utf8MethodDescriptor[j]);
                              mrMethod[j] = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_METHOD_REF, classLauncher4B, ntMethod[j]);
                           }
                           methodRefIndex = jclass.getConstantIndex(mrMethod[j], null);
                           
                           if (op == 183) // invokespecial
                           {
                              // First of all, clear instructions that were allocating the new object
                              b[newPc++] = (byte)0; // nop (clear new op)
                              b[newPc++] = (byte)0; // nop (clear new index1)
                              b[newPc++] = (byte)0; // nop (clear new index2)
                              if ((b[newPc] & 0xFF) == 89) // dup
                                 b[newPc++] = (byte)0; // nop (clear dup op)
                           }
                           
                           // Then, replace "invokevirtual/invokespecial <source>" with "invokestatic <target>"
                           b[pc++] = (byte)184; // invokestatic
                           b[pc++] = (byte)((methodRefIndex >> 8) & 0xFF);
                           b[pc++] = (byte)(methodRefIndex & 0xFF);
                        }
                        else
                           pc += 3;
                        newPc = -1;
                     }
                     else
                        pc += getPCCount(b, pc);
                  }
               }
            }
         }
      }
   }
      
   private static void checkFinalizable(JavaClass jclass)
   {
      if (!jclass.getClassName().equals("totalcross/Launcher$Finalizable") && !jclass.isSubClass("totalcross/Launcher$Finalizable"))
      {
         JavaMethod[] methods = jclass.getMethod("finalize", "()V");
         if (methods != null) // has finalize
         {
            Utils.println("Emulating finalization for " + jclass.toString() + "...");

            // First, change method visibility to public
            JavaMethod m = methods[0];
            m.accessFlags &= ~JavaMethod.ACC_PRIVATE; // should not be private
            m.accessFlags &= ~JavaMethod.ACC_PROTECTED; // should not be protected
            m.accessFlags |= JavaMethod.ACC_PUBLIC; // should be PUBLIC!
            
            // First, add HasFinalize interface
            JavaConstant utf8Finalizable = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "totalcross/Launcher$Finalizable", null);
            JavaConstant utf8Launcher4BName = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "totalcross/Launcher", null);
            JavaConstant utf8Launcher4BDescriptor = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "Ltotalcross/Launcher;", null);
            JavaConstant utf8Launcher4BInstance = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "instance", null);
            JavaConstant utf8AddOrRemoveFinalizableName = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "addOrRemoveFinalizable", null);
            JavaConstant utf8AddOrRemoveFinalizableDescriptor = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "(Ltotalcross/Launcher$Finalizable;Z)V", null);
            JavaConstant classFinalizable = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_CLASS, utf8Finalizable, null);
            JavaConstant classLauncher4B = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_CLASS, utf8Launcher4BName, null);
            JavaConstant ntLauncher4BInstance = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_NAME_AND_TYPE, utf8Launcher4BInstance, utf8Launcher4BDescriptor);
            JavaConstant ntAddOrRemoveFinalizable = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_NAME_AND_TYPE, utf8AddOrRemoveFinalizableName, utf8AddOrRemoveFinalizableDescriptor);
            JavaConstant frLauncher4BInstance = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_FIELD_REF, classLauncher4B, ntLauncher4BInstance);
            JavaConstant mrAddOrRemoveFinalizable = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_METHOD_REF, classLauncher4B, ntAddOrRemoveFinalizable);

            // Mark class of type Finalizable
            jclass.interfaces.addElement(classFinalizable);

            // Add call to "Launcher4B.instance.addOrRemoveFinalizable(this, true)" to class constructors
            JavaMethod[] inits = jclass.getMethod("<init>", null);

            int count1 = inits.length;
            for (int i = 0; i < count1; i++)
            {
               // Search for call to super constructor inside constructor code
               Code info = (Code)inits[i].getCode().info;
               byte[] b = info.code;

               int pc = 0, count2 = b.length;
               for (; pc < count2;)
               {
                  int op = (int)b[pc] & 0xFF;
                  if (op == 183) // invokespecial
                  {
                     MethodRef ref = (MethodRef)jclass.getConstant(b[pc + 1], b[pc + 2], null).info;
                     pc += 3;

                     if (ref.value1 == jclass.superClass && ref.getValue2AsNameAndType().getValue1AsName().value.equals("<init>"))
                        break;
                  }
                  else
                     pc += getPCCount(b, pc);
               }
               if (pc >= count2) // call to super constructor not found
                  continue;

               // Extend code to fit our opcodes
               b = new byte[b.length + 8];
               System.arraycopy(info.code, 0, b, 0, pc);
               System.arraycopy(info.code, pc, b, pc + 8, info.code.length - pc);
               info.code = b;

               // Now, add Launcher4B.instance.addFinalize(this) just after the call to super constructor
               // Basically, this is the code executed:
               // getstatic (0xB2) frLauncher4BInstance
               // aload_0 (0x2A)
               // iconst_0
               // invokevirtual (0xB6) mrAddOrRemoveFinalizable

               int frLauncher4BInstanceIndex = jclass.getConstantIndex(frLauncher4BInstance, null);
               int mrAddOrRemoveFinalizableIndex = jclass.getConstantIndex(mrAddOrRemoveFinalizable, null);

               b[pc++] = (byte)0xB2;
               b[pc++] = (byte)((frLauncher4BInstanceIndex >> 8) & 0xFF);
               b[pc++] = (byte)(frLauncher4BInstanceIndex & 0xFF);
               b[pc++] = (byte)0x2A;
               b[pc++] = (byte)0x03;
               b[pc++] = (byte)0xB6;
               b[pc++] = (byte)((mrAddOrRemoveFinalizableIndex >> 8) & 0xFF);
               b[pc++] = (byte)(mrAddOrRemoveFinalizableIndex & 0xFF);

               updatePC(info, pc - 8, 8);

               if (info.maxStack < 3)
                  info.maxStack = 3;
            }

            // Now, if class defines 'dontFinalize' field, check for dynamic changes
            if (jclass.getField("dontFinalize", "Z") != null)
            {
               JavaConstant utf8DontFinalizeName = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "dontFinalize", null);
               JavaConstant utf8DontFinalizeDescriptor = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_UTF8, "Z", null);
               JavaConstant ntDontFinalize = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_NAME_AND_TYPE, utf8DontFinalizeName, utf8DontFinalizeDescriptor);
               JavaConstant frDontFinalize = JavaConstant.createConstant(jclass, JavaConstant.CONSTANT_FIELD_REF, jclass.thisClass, ntDontFinalize);

               checkFinalizableChanges(jclass, frLauncher4BInstance, mrAddOrRemoveFinalizable, frDontFinalize);
            }
         }
      }
   }
   
   private static void checkFinalizableChanges(JavaClass jclass, JavaConstant frLauncher4BInstance, JavaConstant mrAddOrRemoveFinalizable, JavaConstant frDontFinalize)
   {
      // First, get indices to use in instructions
      setIntToBytes(jclass.getConstantIndex(frLauncher4BInstance, null), fourBytes, 0, 2);
      byte frLauncher4BInstanceIndex1 = fourBytes[0];
      byte frLauncher4BInstanceIndex2 = fourBytes[1];

      setIntToBytes(jclass.getConstantIndex(mrAddOrRemoveFinalizable, null), fourBytes, 0, 2);
      byte mrAddOrRemoveFinalizableIndex1 = fourBytes[0];
      byte mrAddOrRemoveFinalizableIndex2 = fourBytes[1];

      setIntToBytes(jclass.getConstantIndex(frDontFinalize, null), fourBytes, 0, 2);
      byte frDontFinalizeIndex1 = fourBytes[0];
      byte frDontFinalizeIndex2 = fourBytes[1];

      // Then, check all methods
      int count = jclass.methods.size(), pc, count2;
      for (int i = 0; i < count; i++)
      {
         Code info = (Code)((JavaMethod)jclass.methods.items[i]).getCode().info;
         byte[] b = info.code;

         pc = 0;
         count2 = b.length;
         for (; pc < count2;)
         {
            int op = (int)b[pc] & 0xFF;
            if (op == 181 && jclass.getConstant(b[pc + 1], b[pc + 2], null).equals(frDontFinalize)) // putfield dontFinalize
            {
               // Now, add Launcher4B.addOrRemoveFinalizable(this, this.dontFinalize) just after the putfield
               // getstatic frLauncher4BInstance
               // aload_0
               // aload_0
               // getfield dontFinalize
               // invokevirtual mrAddOrRemoveFinalizable

               pc += 3;
               count2 += 11;
               b = new byte[count2];
               System.arraycopy(info.code, 0, b, 0, pc);
               System.arraycopy(info.code, pc, b, pc + 11, info.code.length - pc);
               info.code = b;

               b[pc++] = (byte)0xB2;
               b[pc++] = frLauncher4BInstanceIndex1;
               b[pc++] = frLauncher4BInstanceIndex2;
               b[pc++] = (byte)0x2A;
               b[pc++] = (byte)0x2A;
               b[pc++] = (byte)0xB4;
               b[pc++] = frDontFinalizeIndex1;
               b[pc++] = frDontFinalizeIndex2;
               b[pc++] = (byte)0xB6;
               b[pc++] = mrAddOrRemoveFinalizableIndex1;
               b[pc++] = mrAddOrRemoveFinalizableIndex2;

               updatePC(info, pc - 11, 11);
               updateOffsets(info, pc - 11, 11);
            }
            else
               pc += getPCCount(b, pc);
         }

         if (info.maxStack < 3)
            info.maxStack = 3;
      }
   }

   private static void updatePC(Code info, int pcFrom, int inc)
   {
      // Update pc in exceptions
      Code.Exception exc;
      int count = info.exceptions.size();

      for (int i = 0; i < count; i++)
      {
         exc = (Code.Exception)info.exceptions.items[i];
         if (exc.startPC >= pcFrom)
            exc.startPC += inc;
         if (exc.endPC >= pcFrom)
            exc.endPC += inc;
         if (exc.handlerPC >= pcFrom)
            exc.handlerPC += inc;
      }

      // Change scope of local variables
      LocalVariableTable[] lvts = info.getLocalVariableTables();
      LocalVariableTable lvt;
      LocalVariableTable.LocalVariable var;
      
      if (lvts != null) // guich@tc112_9
      {
      	for (int i = lvts.length - 1; i >= 0; i--)
	      {
   	      lvt = lvts[i];
      	   for (int j = lvt.variables.size() - 1; j >= 0; j--)
         	{
	            var = (LocalVariableTable.LocalVariable)lvt.variables.items[j];
   	         if (var.startPC >= pcFrom)
      	         var.startPC += inc;
         	   else if ((var.startPC + var.length - 1) >= pcFrom)
            	   var.length += inc;
	         }
   	   }
   	}
      
      // Update line numbers
      LineNumberTable[] lnts = info.getLineNumberTables();
      LineNumberTable lnt;
      LineNumberTable.LineNumber line;
      
      if (lnts != null) // guich@tc112_9
      {
	      for (int i = lnts.length - 1; i >= 0; i--)
	      {
   	      lnt = lnts[i];
      	   for (int j = lnt.lineNumbers.size() - 1; j >= 0; j--)
	         {
   	         line = (LineNumberTable.LineNumber)lnt.lineNumbers.items[j];
      	      if (line.startPC >= pcFrom)
         	      line.startPC += inc;
	         }
   	   }
   	}
   }

   private static void updateOffsets(Code info, int pcFrom, int inc)
   {
      byte[] b = info.code;
      int pc = 0, off, count = b.length;
      for (; pc < count;)
      {
         int op = (int)b[pc] & 0xFF;
         if ((op >= 153 && op <= 168) || (op >= 198 && op <= 199))
         {
            off = getIntFromBytes(b, pc + 1, 2);
            if ((off > 0 && pc < pcFrom && pc + off >= pcFrom) || (off < 0 && pc > pcFrom && (pc + off) <= pcFrom))
            {
               off += off > 0 ? inc : -inc;
               setIntToBytes(off, b, pc + 1, 2);
            }
            pc += 3;
         }
         else if (op >= 200 && op <= 201)
         {
            off = getIntFromBytes(b, pc + 1, 4);
            if ((off > 0 && pc < pcFrom && pc + off >= pcFrom) || (off < 0 && pc > pcFrom && (pc + off) <= pcFrom))
            {
               off += off > 0 ? inc : -inc;
               setIntToBytes(off, b, pc + 1, 4);
            }
            pc += 5;
         }
         else if (op >= 170 && op <= 171)
         {
            int pos = pc + 1;
            while (pos % 4 != 0) // zero pad
               pos++;

            off = getIntFromBytes(b, pos, 4);
            if ((off > 0 && pc < pcFrom && pc + off >= pcFrom) || (off < 0 && pc > pcFrom && (pc + off) <= pcFrom))
            {
               off += off > 0 ? inc : -inc;
               setIntToBytes(off, b, pos, 4);
            }
            pos += 4;

            if (op == 170)
            {
               int l = getIntFromBytes(b, pos, 4);
               pos += 4;
               int h = getIntFromBytes(b, pos, 4);
               pos += 4;

               int i = h - l + 1;
               while (i-- > 0)
               {
                  off = getIntFromBytes(b, pos, 4);
                  if ((off > 0 && pc < pcFrom && pc + off >= pcFrom) || (off < 0 && pc > pcFrom && (pc + off) <= pcFrom))
                  {
                     off += off > 0 ? inc : -inc;
                     setIntToBytes(off, b, pos, 4);
                  }
                  pos += 4;
               }
            }
            else
            {
               int np = getIntFromBytes(b, pos, 4);
               pos += 4;

               while (np-- > 0)
               {
                  pos += 4; // skip match

                  off = getIntFromBytes(b, pos, 4);
                  if ((off > 0 && pc < pcFrom && pc + off >= pcFrom) || (off < 0 && pc > pcFrom && (pc + off) <= pcFrom))
                  {
                     off += off > 0 ? inc : -inc;
                     setIntToBytes(off, b, pos, 4);
                  }
                  pos += 4;
               }
            }

            pc = pos;
         }
         else
            pc += getPCCount(b, pc);
      }
   }

   private static int getIntFromBytes(byte[] b, int off, int count)
   {
      int i = 0;

      i |= b[off++];
      i <<= 8;
      count--;

      while (--count > 0)
      {
         i |= b[off++] & 0xFF;
         i <<= 8;
      }

      i |= b[off] & 0xFF;

      return i;
   }

   private static void setIntToBytes(int i, byte[] b, int off, int count)
   {
      off += count - 1;
      while (--count > 0)
      {
         b[off--] = (byte)(i & 0xFF);
         i >>= 8;
      }
      b[off] = (byte)(i & 0xFF);
   }

   private static int getPCCount(byte[] b, int pc)
   {
      int op = (int)b[pc] & 0xFF;

      if (op <= 15 || (op >= 26 && op <= 53) || (op >= 59 && op <= 152) || (op >= 172 && op <= 177) || (op >= 190 && op <= 191) || (op >= 194 && op <= 195))
         return 1;
      else if (op == 16 || op == 18 || (op >= 21 && op <= 25) || (op >= 54 && op <= 58) || op == 169 || op == 188)
         return 2;
      else if (op == 17 || op == 19 || op == 20 || (op >= 153 && op <= 168) || (op >= 178 && op <= 187) || op == 189 || (op >= 192 && op <= 193) || (op >= 198 && op <= 199))
         return 3;
      else if (op == 197 || (op >= 200 && op <= 201))
         return 4;
      else if (op == 170)
      {
         int i = pc;
         while (++pc % 4 != 0); // zero pad

         pc += 4; // default
         int l = getIntFromBytes(b, pc, 4);
         pc += 4;
         int h = getIntFromBytes(b, pc, 4);
         pc += 4;

         return pc - i + ((h - l + 1) * 4);
      }
      else if (op == 171)
      {
         int i = pc;
         while (++pc % 4 != 0); // zero pad

         pc += 4; // default
         int np = getIntFromBytes(b, pc, 4);
         pc += 4;

         return pc - i + (np * 8);
      }
      else if (op == 196)
         return ((int)b[++pc]) == 132 ? 6 : 4;
      else
         return 1;
   }

   private static void checkMethods(JavaClass jclass)
   {
      HashSet bbMethodNames = new HashSet();

      int count = jclass.methods.size();
      for (int i = 0; i < count; i++)
      {
         JavaMethod method = (JavaMethod)jclass.methods.items[i];
         String name = method.getName();
         boolean remove;

         if (name.endsWith("4B"))
         {
            if (!bbMethodNames.contains(method.name)) // constant is not added to rename yet
               bbMethodNames.add(method.name); // will be renamed further
            
            remove = false;
         }
         else if (name.endsWith("4D"))
            remove = true;
         else
         {
            remove = false;
            name = name + "4B";
            for (int j = 0; j < count; j ++)
            {
               JavaMethod m = (JavaMethod)jclass.methods.items[j];
               if (name.equals(m.getName()) && method.isEquivalentTo(m))
               {
                  remove = true;
                  break;
               }
            }
         }

         if (remove)
         {
            jclass.methods.removeElementAt(i);
            i--;
            count--;
         }
      }

      // Finally, rename blackberry methods
      Iterator it = bbMethodNames.iterator();
      while (it.hasNext())
      {
         JavaConstant constant = (JavaConstant)it.next();
         String name = ((UTF8)constant.info).value;
         name = name.substring(0, name.length() - 2);
         ((UTF8)constant.info).value = name;
      }
   }

   private static void checkConstantPool(JavaClass jclass)
   {
      // Check all class and name/type constants
      int count = jclass.constantPool.size();
      for (int i = 1; i < count; i++)
      {
         JavaConstant constant = (JavaConstant)jclass.constantPool.items[i];
         i += constant.slots() - 1; // skip empty slots

         if (constant.tag == JavaConstant.CONSTANT_CLASS)
         {
            UTF8 name = ((Class)constant.info).getValueAsName();
            name.value = getUTF8ConstantValue(jclass, name);
         }
         else if (constant.tag == JavaConstant.CONSTANT_NAME_AND_TYPE)
         {
            UTF8 descriptor = ((NameAndType)constant.info).getValue2AsDescriptor();
            descriptor.value = getUTF8ConstantValue(jclass, descriptor);
         }
      }

      // Check class fields
      count = jclass.fields.size();
      for (int i = 0; i < count; i ++)
      {
         JavaField field = (JavaField)jclass.fields.items[i];

         UTF8 descriptor = (UTF8)field.descriptor.info;
         descriptor.value = getUTF8ConstantValue(jclass, descriptor);

         // Check field attributes
         int count2 = field.attributes.size();
         for (int j = 0; j < count2; j ++)
            checkAttribute((JavaAttribute)field.attributes.items[j]);
      }

      // Check class methods
      count = jclass.methods.size();
      for (int i = 0; i < count; i ++)
      {
         JavaMethod method = (JavaMethod)jclass.methods.items[i];

         UTF8 descriptor = (UTF8)method.descriptor.info;
         descriptor.value = getUTF8ConstantValue(jclass, descriptor);

         // Check method attributes
         int count2 = method.attributes.size();
         for (int j = 0; j < count2; j ++)
            checkAttribute((JavaAttribute)method.attributes.items[j]);
      }

      // Check class attributes
      count = jclass.attributes.size();
      for (int i = 0; i < count; i ++)
         checkAttribute((JavaAttribute)jclass.attributes.items[i]);
   }

   /**
    * @param attribute
    * @param classes
    */
   private static void checkAttribute(JavaAttribute attribute)
   {
      if (attribute.info instanceof LocalVariableTable)
      {
         Vector variables = ((LocalVariableTable)attribute.info).variables;
         int count = variables.size();
         for (int i = 0; i < count; i ++)
         {
            UTF8 descriptor = (UTF8)((LocalVariableTable.LocalVariable)variables.items[i]).descriptor.info;
            descriptor.value = getUTF8ConstantValue(attribute.jclass, descriptor);
         }
      }
      else if (attribute.info instanceof Code)
      {
         Code code = (Code)attribute.info;
         Vector attributes = code.attributes;
         int count = attributes.size();
         for (int i = 0; i < count; i ++)
            checkAttribute((JavaAttribute)attributes.items[i]);
      }
   }

   private static String getUTF8ConstantValue(JavaClass jclass, UTF8 utf8)
   {
      String base = utf8.value;
      String className = bb2Desktop(jclass.getClassName());

      Enumeration en = utf8Replaces.keys();
      while (en.hasMoreElements())
      {
         UTF8ReplaceEntry entry = (UTF8ReplaceEntry)utf8Replaces.get((String)en.nextElement());
         if (entry.skip.indexOf(className) == -1 && base.contains(entry.originalValue))
            base = base.replace(entry.originalValue, entry.replacedValue);
      }

      return base;
   }

   protected static int compare(String s1, String s2)
   {
      if (s1 != null)
         s1 = bb2Desktop(s1);
      if (s2 != null)
         s2 = bb2Desktop(s2);

      return s1 == null ? (s2 == null ? 0 : 1) : (s2 == null ? -1 : s1.compareTo(s2));
   }

   protected static int compare(String[] s1, String[] s2)
   {
      if (s1 == null)
         return s2 == null ? 0 : 1;
      if (s2 == null)
         return -1;

      if (s1.length < s2.length)
         return -1;
      if (s1.length > s2.length)
         return 1;

      int n = s1.length;
      for (int i = 0; i < n; i++)
      {
         int res = compare(s1[i], s2[i]);
         if (res != 0)
            return res;
      }

      return 0;
   }

   protected static String parseDescriptor(String desc)
   {
      if (desc.endsWith(";"))
         desc = desc.substring(0, desc.length() - 1);

      return desc;
   }

   protected static String[] tokenizeMethodDescriptor(String desc)
   {
      if (desc == null)
         return null;

      v.removeAllElements();

      int idx = desc.indexOf(')');
      v.addElement(parseDescriptor(desc.substring(idx + 1)));

      desc = desc.substring(1, idx);
      char[] chars = desc.toCharArray();
      boolean isObject = false;
      boolean isArray = false;

      sb.setLength(0);
      for (int i = 0; i < chars.length; i++)
      {
         sb.append(chars[i]);

         if (isObject)
         {
            if (chars[i] == ';')
            {
               isObject = false;
               isArray = false;
            }
         }
         else
         {
            if (chars[i] == 'L')
               isObject = true;
            else
               isArray = chars[i] == '[';
         }

         if (!isArray && !isObject)
         {
            v.addElement(parseDescriptor(sb.toString()));
            sb.setLength(0);
         }
      }

      String[] tokens = new String[v.size()];
      v.copyInto(tokens);
      return tokens;
   }

   private static String bb2Desktop(String name)
   {
      if (name.matches(".+4B(\\x24.+)?"))
      {
         String inner = "";
         int idx = name.indexOf('$');
         if (idx > 0) // separate class name from inner class name
         {
            inner = "$" + name.substring(idx + 1);
            name = name.substring(0, idx);
         }
         name = name.substring(0, name.length() - 2); // remove 4B
         name += inner; // add inner class name
      }

      return name;
   }
}
