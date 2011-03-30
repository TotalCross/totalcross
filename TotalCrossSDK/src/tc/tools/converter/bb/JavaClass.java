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



package tc.tools.converter.bb;

import tc.tools.converter.bb.attribute.InnerClasses;
import tc.tools.converter.bb.constant.Class;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class JavaClass implements JavaClassStructure
{
   public int minorVersion;
   public int majorVersion;
   public Vector constantPool;
   public int accessFlags;
   public JavaConstant thisClass;
   public JavaConstant superClass;
   public Vector interfaces;
   public Vector fields;
   public Vector methods;
   public Vector attributes;

   public static final int MAGIC_NUMBER = 0xCAFEBABE;

   public static final int ACC_PUBLIC = 0x0001;
   public static final int ACC_FINAL = 0x0010;
   public static final int ACC_SUPER = 0x0020;
   public static final int ACC_INTERFACE = 0x0200;
   public static final int ACC_ABSTRACT = 0x0400;

   public static final int PLATFORM_DESKTOP = 1;
   public static final int PLATFORM_DEVICE = 2;
   public static final int PLATFORM_BLACKBERRY = 4;
   public static final int PLATFORM_ALL = 7;

   public JavaClass()
   {
      constantPool = new Vector();
      interfaces = new Vector();
      fields = new Vector();
      methods = new Vector();
      attributes = new Vector();
   }

   public String toString()
   {
      return getClassName();
   }

   public String getClassName()
   {
      return ((Class)thisClass.info).getValueAsName().value;
   }

   public String getSuperClassName()
   {
      return ((Class)superClass.info).getValueAsName().value;
   }

   public JavaConstant getConstant(byte index1, byte index2, JavaClassStructure caller) throws IndexOutOfBoundsException
   {
      return getConstant((((int)index1 << 8) & 0xFF00) | ((int)index2 & 0xFF), caller);
   }

   public JavaConstant getConstant(int index, JavaClassStructure caller) throws IndexOutOfBoundsException
   {
      if (index <= 0 || index > constantPool.size())
         throw new IndexOutOfBoundsException("Invalid constant pool index: " + index + "; class: " + this + ", caller: " + caller);

      return (JavaConstant)constantPool.items[index];
   }

   public int getConstantIndex(JavaConstant constant, JavaClassStructure caller) throws IndexOutOfBoundsException
   {
      int index = constantPool.indexOf(constant);
      if (index <= 0 || index > constantPool.size())
         throw new IndexOutOfBoundsException("Invalid constant pool index: " + index + "; class: " + this + ", caller: " + caller);

      return index;
   }

   public JavaField getField(String name, String descriptor)
   {
      JavaField field = null;

      int count = fields.size();
      for (int i = 0; i < count; i++)
      {
         field = (JavaField)fields.items[i];
         if (field.getName().equals(name) && field.getDescriptor().equals(descriptor))
            return field;
      }

      return null;
   }

   public JavaMethod[] getMethod(String name, String descriptor)
   {
      Vector v = new Vector();
      JavaMethod method = null;

      int count = methods.size();
      for (int i = 0; i < count; i++)
      {
         method = (JavaMethod)methods.items[i];
         if ((name == null || method.getName().equals(name)) && (descriptor == null || method.getDescriptor().equals(descriptor)))
            v.addElement(method);
      }

      if (v.size() == 0)
         return null;
      else
      {
         JavaMethod[] result = new JavaMethod[v.size()];
         v.copyInto(result);
         return result;
      }
   }

   public boolean isPublic()
   {
      return (accessFlags & ACC_PUBLIC) == ACC_PUBLIC;
   }

   public boolean isFinal()
   {
      return (accessFlags & ACC_FINAL) == ACC_FINAL;
   }

   public boolean isSuper()
   {
      return (accessFlags & ACC_SUPER) == ACC_SUPER;
   }

   public boolean isInterface()
   {
      return (accessFlags & ACC_INTERFACE) == ACC_INTERFACE;
   }

   public boolean isAbstract()
   {
      return (accessFlags & ACC_ABSTRACT) == ACC_ABSTRACT;
   }

   public boolean isSubClass(String className)
   {
      if (getSuperClassName().equals(className))
         return true;

      // Now, check interfaces
      Class iface;

      int count = interfaces.size();
      for (int i = 0; i < count; i++)
      {
         iface = (Class)((JavaConstant)interfaces.items[i]).info;
         if (iface.getValueAsName().value.equals(className))
            return true;
      }

      return false;
   }

   public InnerClasses.Classes asInnerClass()
   {
      for (int i = 0; i < attributes.size(); i++)
      {
         JavaAttribute attr = (JavaAttribute)attributes.items[i];
         if (attr.info instanceof InnerClasses)
         {
            Vector classes = ((InnerClasses)attr.info).classes;
            for (int j = 0; j < classes.size(); j++)
            {
               InnerClasses.Classes ic = (InnerClasses.Classes)classes.items[j];
               if (ic.innerClass == thisClass)
                  return ic;
            }
         }
      }

      return null;
   }

   public int getPlatforms()
   {
      int platforms = PLATFORM_DESKTOP;

      for (int i = 0; i < methods.size(); i++)
      {
         JavaMethod method = (JavaMethod)methods.items[i];
         String name = method.getName();

         if ((platforms & PLATFORM_BLACKBERRY) == 0 && name.endsWith("4B"))
         {
            name = name.substring(0, name.length() - 2);
            for (int j = 0; j < methods.size(); j++)
            {
               JavaMethod m = (JavaMethod)methods.items[j];
               if (name.equals(m.getName())) // found desktop method
                  platforms |= PLATFORM_BLACKBERRY;
            }
         }
         else if ((platforms & PLATFORM_DEVICE) == 0 && name.endsWith("4D"))
         {
            name = name.substring(0, name.length() - 2);
            for (int j = 0; j < methods.size(); j++)
            {
               JavaMethod m = (JavaMethod)methods.items[j];
               if (name.equals(m.getName())) // found desktop method
                  platforms |= PLATFORM_DEVICE;
            }
         }

         if ((platforms & PLATFORM_ALL) == PLATFORM_ALL) // no more platforms to check
            break;
      }

      return platforms;
   }

   public void assertConsistency() throws ClassConsistencyException
   {
      for (int i = 0; i < methods.size(); i++)
      {
         JavaMethod method = (JavaMethod)methods.items[i];
         String name = method.getName();

         if (name.endsWith("4B"))
         {
            name = name.substring(0, name.length() - 2);

            boolean found = false;
            for (int j = 0; j < methods.size(); j++)
            {
               JavaMethod m = (JavaMethod)methods.items[j];
               if (name.equals(m.getName()) && method.isEquivalentTo(m)) // found desktop method
               {
                  found = true;
                  break;
               }
            }

            if (!found)
               throw new ClassConsistencyException(this, "The method '" + method.toString() + "' does not have an equivalent");
         }
      }
   }

   public void assertConsistency(JavaClass other) throws ClassConsistencyException
   {
      if (other == null)
         throw new ClassConsistencyException(other, this, null);
      if (J2BB.compare(getClassName(), other.getClassName()) != 0)
         throw new ClassConsistencyException(other, this, "Class names must be equal");
      if (J2BB.compare(getSuperClassName(), other.getSuperClassName()) != 0)
         throw new ClassConsistencyException(other, this, "Super classes must be equal");
      if (accessFlags != other.accessFlags)
         throw new ClassConsistencyException(other, this, "Access flags must be equal");

      for (int i = 0; i < fields.size(); i++)
      {
         JavaField field = (JavaField)fields.items[i];
         String name = field.getName();

         if (!name.contains("$") && !name.startsWith("_") && !field.isPrivate()) // not private, the "to" class must have the same field
         {
            boolean found = false;
            for (int j = 0; j < other.fields.size(); j++)
            {
               JavaField f = (JavaField)other.fields.items[j];
               if (f.isEquivalentTo(field))
               {
                  found = true;
                  break;
               }
            }

            if (!found)
               throw new ClassConsistencyException(other, this, "The field '" + field.toString() + "' does not have an equivalent");
         }
      }

      for (int i = 0; i < methods.size(); i++)
      {
         JavaMethod method = (JavaMethod)methods.items[i];
         String name = method.getName();

         if (!method.isSynthetic() && !name.equals("<clinit>") && !name.startsWith("_") && !name.endsWith("4D") && !method.isPrivate()) // not private, the "to" class must have the same method
         {
            boolean found = false;
            for (int j = 0; j < other.methods.size(); j++)
            {
               JavaMethod m = (JavaMethod)other.methods.items[j];
               if (m.isEquivalentTo(method))
               {
                  found = true;
                  break;
               }
            }

            if (!found)
               throw new ClassConsistencyException(other, this, "The method '" + method.toString() + "' does not have an equivalent");
         }
      }
   }

   public int length()
   {
      int len = 24 + (interfaces.size() * 2);

      int count = constantPool.size();
      for (int i = 0; i < count; i ++)
      {
         JavaConstant constant = (JavaConstant)constantPool.items[i];
         len += constant.length();
         i += constant.slots() - 1;
      }

      count = fields.size();
      for (int i = 0; i < count; i ++)
         len += ((JavaField)fields.items[i]).length();

      count = methods.size();
      for (int i = 0; i < count; i ++)
         len += ((JavaMethod)methods.items[i]).length();

      count = attributes.size();
      for (int i = 0; i < count; i ++)
         len += ((JavaAttribute)attributes.items[i]).length();

      return len;
   }

   public void load(DataStream ds) throws IOException
   {
      if (ds.readInt() != MAGIC_NUMBER)
         return;

      minorVersion = ds.readUnsignedShort();
      majorVersion = ds.readUnsignedShort();

      int count = ds.readUnsignedShort() - 1;
      constantPool.removeAllElements();
      for (int i = 0; i <= count; i ++)
         constantPool.addElement(new JavaConstant(this));
      for (int i = 1; i <= count; i ++)
      {
         JavaConstant constant = (JavaConstant)constantPool.items[i];
         constant.load(ds);

         i += constant.slots() - 1;
      }

      accessFlags = ds.readUnsignedShort();
      thisClass = getConstant(ds.readUnsignedShort(), this);
      superClass = getConstant(ds.readUnsignedShort(), this);

      count = ds.readUnsignedShort();
      interfaces.removeAllElements();
      for (int i = 0; i < count; i ++)
         interfaces.addElement(getConstant(ds.readUnsignedShort(), this));

      count = ds.readUnsignedShort();
      fields.removeAllElements();
      for (int i = 0; i < count; i ++)
      {
         JavaField field = new JavaField(this);
         field.load(ds);

         fields.addElement(field);
      }

      count = ds.readUnsignedShort();
      methods.removeAllElements();
      for (int i = 0; i < count; i ++)
      {
         JavaMethod method = new JavaMethod(this);
         method.load(ds);

         methods.addElement(method);
      }

      count = ds.readUnsignedShort();
      attributes.removeAllElements();
      for (int i = 0; i < count; i ++)
      {
         JavaAttribute attribute = new JavaAttribute(this);
         attribute.load(ds);

         attributes.addElement(attribute);
      }
   }

   public void save(DataStream ds) throws IOException
   {
      ds.writeInt(MAGIC_NUMBER);
      ds.writeShort(minorVersion);
      ds.writeShort(majorVersion);

      int count = constantPool.size() - 1;
      ds.writeShort(count + 1);
      for (int i = 1; i <= count; i ++)
      {
         JavaConstant constant = (JavaConstant)constantPool.items[i];
         constant.save(ds);

         i += constant.slots() - 1;
      }

      ds.writeShort(accessFlags);
      ds.writeShort(getConstantIndex(thisClass, this));
      ds.writeShort(getConstantIndex(superClass, this));

      count = interfaces.size();
      ds.writeShort(count);
      for (int i = 0; i < count; i ++)
         ds.writeShort(getConstantIndex((JavaConstant)interfaces.items[i], this));

      count = fields.size();
      ds.writeShort(count);
      for (int i = 0; i < count; i ++)
         ((JavaField)fields.items[i]).save(ds);

      count = methods.size();
      ds.writeShort(count);
      for (int i = 0; i < count; i ++)
         ((JavaMethod)methods.items[i]).save(ds);

      count = attributes.size();
      ds.writeShort(count);
      for (int i = 0; i < count; i ++)
         ((JavaAttribute)attributes.items[i]).save(ds);
   }
}
