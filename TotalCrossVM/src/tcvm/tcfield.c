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

#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////////
//                          Static Field support                            //
// Static fields are never inherited; they can only be accessed with the    //
// class name where they are defined. The values are stored in the          //
// xxxStaticValues.                                                         //
//////////////////////////////////////////////////////////////////////////////

//////////
// Returns the reference to the field, which can be read or changed
// used by external libraries, NOT by the vm

static int32 getStaticFieldIndex(CharP fieldName, FieldArray fields)
{
   FieldArray f = fields;
   uint32 len = ARRAYLENV(fields);
   for (; len-- > 0; f++)
      if (strEq(f->name, fieldName))
         return (int32)(f-fields);
   return -1;
}

int32* getStaticFieldInt(TCClass c, CharP fieldName)
{
   int32 idx = getStaticFieldIndex(fieldName, c->i32StaticFields);
   return idx >= 0 ? &c->i32StaticValues[idx] : null;
}

double* getStaticFieldDouble(TCClass c, CharP fieldName)
{
   int32 idx = getStaticFieldIndex(fieldName, c->v64StaticFields);
   return idx >= 0 ? &c->v64StaticValues[idx] : null;
}

int64* getStaticFieldLong(TCClass c, CharP fieldName)
{
   int32 idx = getStaticFieldIndex(fieldName, c->v64StaticFields);
   return idx >= 0 ? (int64*)&c->v64StaticValues[idx] : null;
}

TC_API void jlC_forName_s(NMParams p);

TCObject* getStaticFieldObject(Context context, TCClass c, CharP fieldName)
{
   int32 idx = getStaticFieldIndex(fieldName, c->objStaticFields);
   if (idx >= 0)
   {
      TCObject* ret = &c->objStaticValues[idx];
      Field f = &c->objStaticFields[idx];
      if (strEq(f->targetClassName, "java.lang.Class") && OBJ_CLASS(*ret)->flags.isString)
      {
         TNMParams params;
         tzero(params);
         params.currentContext = context;
         params.obj = ret;
         jlC_forName_s(&params);
         c->objStaticValues[idx] = params.retO;
      }
      return &c->objStaticValues[idx];
   }
   return null;
}

//////////
// functions used by the vm - these are called only once per field,
// because the fields are bound after the first call

static VoidP getSFieldFromInterfaces(TCClass ext0, CharP fieldName, RegType t)
{
   int32 i,len;
   FieldArray fields=null, f;
   TCClass ext;
   for (ext = ext0; ext; ext = ext->superClass) // if didn't find in the classes, search in the interfaces
      for (i = ARRAYLENV(ext->interfaces)-1; i >= 0; i--)
         for (f = fields = ext->interfaces[i]->staticFields[(int32)t], len = ARRAYLENV(fields); len-- > 0; f++)
            if (strEq(f->name, fieldName))
            {
               int32 idx = (uint16)(f-fields);
               switch (t)
               {
                  case RegI: return &ext->interfaces[i]->i32StaticValues[idx];
                  case RegO: return &ext->interfaces[i]->objStaticValues[idx];
                  default:   return &ext->interfaces[i]->v64StaticValues[idx];
               }
            }
   return null;
}

VoidP getSField_Ref(Context currentContext, TCClass c, int32 sym, RegType t)
{
   uint32 fieldIndex = c->cp->sfieldField[sym];
   uint32 classIndex = c->cp->sfieldClass[sym], len;
   CharP className = c->cp->cls[classIndex];
   CharP fieldName = c->cp->mtdfld[fieldIndex];
   
   FieldArray fields=null, f;
   TCClass ext = strEq(c->name, className) ? c : loadClass(currentContext, className, false), ext0 = ext;
   while (ext)
   {
      for (f = fields = ext->staticFields[(int32)t], len = ARRAYLENV(fields); len-- > 0; f++)
         if (strEq(f->name, fieldName) && strEq(f->sourceClassName, className))
         {
            int32 idx = (uint16)(f-fields);
            switch (t)
            {
               case RegI: return c->cp->boundSField[sym] = &ext->i32StaticValues[idx];
               case RegO: return c->cp->boundSField[sym] = &ext->objStaticValues[idx];
               default:   return c->cp->boundSField[sym] = &ext->v64StaticValues[idx];
            }
         }
      ext = ext->superClass;
      if (ext)
         className = ext->name;
   }
   if (ext == null) // guich@tc310
   {
      VoidP ret = getSFieldFromInterfaces(ext0, fieldName, t);
      if (ret == null && ext0 != c)
         ret = getSFieldFromInterfaces(c, fieldName, t);
      if (ret != null)
         return ret;
   }
   if (ext == null)
      debug("@@@ class not found. %s at %s.%s",c->name, className,fieldName);
   return ext == null ? SF_CLASS_ERROR : SF_FIELD_ERROR;
}

void getSField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className)
{
   uint32 fieldIndex = cp->sfieldField[sym];
   uint32 classIndex = cp->sfieldClass[sym];
   *className = cp->cls[classIndex];
   *fieldName = cp->mtdfld[fieldIndex];
}

///////////////////////////////////////////////////////////////////////////////
//                          Instance Field support                           //
// Instance fields are inherited from super classes. An object is a block of //
// memory that holds all instance fields, backwards: the ones  declared in   //
// the class placed first, then the ones in the superclass, and so on.       //
///////////////////////////////////////////////////////////////////////////////

// used by both
uint16 getInstanceFieldIndex(CharP fieldName, CharP fieldClassName, TCObject o, RegType t)
{
   bool found=false;
   FieldArray fields, f;
   TCClass ext = OBJ_CLASS(o);
   while (ext)
   {
      for (fields = ext->instanceFields[(int32)t], f = fields+ARRAYLENV(fields); --f >= fields;) // guich@tc110_101: must go backwards
         if (fields)      
         {
            if (strEq(f->name, fieldName) && (found || (found=(f->sourceClassName == fieldClassName || strEq(f->sourceClassName, fieldClassName))))) // guich@tc110_101: once the class was found, we can search all superclasses
                return (uint16)(f-fields);
         }
         else
            break;
      ext = ext->superClass;
      if (ext)
         fieldClassName = ext->name;
   }
   return UNBOUND_FIELD_ERROR;
}

// used by external libraries, NOT by the vm

int32* getInstanceFieldInt(TCObject instance, CharP fieldName, CharP className)
{
   uint16 idx = getInstanceFieldIndex(fieldName, className, instance, RegI);
   return idx < UNBOUND_ERROR ? &FIELD_I32(instance, idx) : null;
}

double* getInstanceFieldDouble(TCObject instance, CharP fieldName, CharP className) // class name is the class where the field is declared, or null if from current class
{
   uint16 idx = getInstanceFieldIndex(fieldName, className, instance, RegD);
   return idx < UNBOUND_ERROR ? &FIELD_DBL(instance, OBJ_CLASS(instance), idx) : null;
}

int64* getInstanceFieldLong(TCObject instance, CharP fieldName, CharP className)
{
   uint16 idx = getInstanceFieldIndex(fieldName, className, instance, RegL);
   return idx < UNBOUND_ERROR ? &FIELD_I64(instance, OBJ_CLASS(instance), idx) : null;
}

TCObject* getInstanceFieldObject(TCObject instance, CharP fieldName, CharP className)
{
   uint16 idx = getInstanceFieldIndex(fieldName, className, instance, RegO);
   return idx < UNBOUND_ERROR ? &FIELD_OBJ(instance, OBJ_CLASS(instance), idx) : null;
}

//////////
// functions used by the vm - these are called only once per field,
// because the fields are bound after the first call

void getIField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className)
{
   uint32 fieldIndex = cp->ifieldField[sym];
   uint32 classIndex = cp->ifieldClass[sym];
   *className = cp->cls[classIndex];
   *fieldName = cp->mtdfld[fieldIndex];
}

uint16 getIField_Index(ConstantPool cp, TCObject o, int32 sym, RegType t)
{
   if (sym == 0)
      return UNBOUND_FIELD_ERROR;
   else
   {
      uint32 fieldIndex = cp->ifieldField[sym];
      uint32 classIndex = cp->ifieldClass[sym];
      CharP className = cp->cls[classIndex];
      CharP fieldName = cp->mtdfld[fieldIndex];
      uint16 idx = getInstanceFieldIndex(fieldName, className, o, t); // the strEq is needed to avoid a NoSuchField that occurs if you try to get the "width" field in a class that extends MainWindow from inside the class that extends MainWindow
      //debug("%s.%s bound to %d", className, fieldName, idx);
      return idx < UNBOUND_ERROR ? (cp->boundIField[sym] = idx) : idx;
   }
}
