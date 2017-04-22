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



package tc.tools.converter.tclass;

import totalcross.io.DataStreamLE;

public final class TCFieldFlags // uint16 structure
{
   // Java access flags
   public boolean isFinal        ;// 1;
   public int     type           ;// 4; // the Type, ranging from Type_Null to Type_Object
   public boolean isPublic       ;// 1;
   public boolean isPrivate      ;// 1;
   public boolean isProtected    ;// 1;
   public boolean isStatic       ;// 1;
   public boolean isVolatile     ;// 1;
   public boolean isTransient    ;// 1;
   public boolean isArray        ;// 1;

   public void write(DataStreamLE ds) throws totalcross.io.IOException
   {
      int v =
         ((isArray        ?1:0) << 0)  |
         (type                  << 1)  |
         ((isPublic       ?1:0) << 5)  |
         ((isPrivate      ?1:0) << 6)  |
         ((isProtected    ?1:0) << 7)  |
         ((isStatic       ?1:0) << 8)  |
         ((isVolatile     ?1:0) << 9)  |
         ((isTransient    ?1:0) << 10) |
         ((isFinal        ?1:0) << 11) |
         0;
      ds.writeShort(v);
   }

   public String toString()
   {
      return
      (isPublic       ? "public " : "") +
      (isPrivate      ? "private " : "") +
      (isProtected    ? "protected " : "") +
      (isStatic       ? "static " : "") +
      (isVolatile     ? "volatile " : "") +
      (isTransient    ? "transient " : "") +
      (isFinal        ? "final " : "");
   }
}
