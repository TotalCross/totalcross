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



package tc.test.converter.testfiles;

public class BC058_075to078
{
   public BC058_075to078()
   {
      Object o1 = null, o2 = null, o3 = null, o4 = null; // regO: 2, 3, 4, 5

      o2 = o1;
      o4 = o2;

      if (false) {o2=o3; o3=o4;} // remove warnings
   }
}
