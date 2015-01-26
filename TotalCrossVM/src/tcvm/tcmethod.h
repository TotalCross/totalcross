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



#ifndef METHOD_H
#define METHOD_H

/// Returns the method with the given class, method name and parameters
/// If searchSuperclasses is true, the super classes are searched if the
/// method is not found in the current class.
/// If the method is not static, you must not pass the first "this" parameter.
/// In other words, the parameters are the ones as shown in the Java code.
/// The methodName is the method name or CONSTRUCTOR_NAME (for a constructor)
/// The parameter arguments are: J_INT, J_DOUBLE, J_LONG, J_BYTE, J_SHORT, J_CHAR, J_BOOLEAN,
/// CHAR_ARRAY, BYTE_ARRAY, SHORT_ARRAY, INT_ARRAY, LONG_ARRAY, DOUBLE_ARRAY, BOOLEAN_ARRAY, FLOAT_ARRAY
/// For other objects, pass the full class name, like "java.lang.String" or "totalcross.ui.MainWindow".

TC_API Method getMethod(TCClass c, bool searchSuperclasses, CharP methodName, int32 nparams, ...);
typedef Method (*getMethodFunc)(TCClass c, bool searchSuperclasses, CharP methodName, int32 nparams, ...);

#endif
