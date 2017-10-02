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

public class BC178to181 extends ExtField1 {
  private boolean fb1;
  private char fc1;
  private byte fB1;
  private short fs1;
  private int fi1;
  private long fl1;
  private float ff1;
  private double fd1;
  private Object fo1;

  public static boolean static_b1;
  private static char static_c1;
  private static byte static_B1;
  private static short static_s1;
  private static int static_i1;
  private static long static_l1;
  private static float static_f1;
  private static double static_d1;
  private static Object static_o1;

  public void start() {
    // getField -> this
    boolean b1 = fb1; // regI (2)
    char c1 = fc1; // regI (4)
    byte B1 = fB1; // regI (6)
    short s1 = fs1; // regI (8)
    int i1 = fi1; // regI (10)
    long l1 = fl1; // reg64 (2)
    float f1 = ff1; // reg64 (4)
    double d1 = fd1; // reg64 (6)
    Object o1 = fo1; // regO (2)

    // putField -> this
    fb1 = b1;
    fc1 = c1;
    fB1 = B1;
    fs1 = s1;
    fi1 = i1;
    fl1 = l1;
    ff1 = f1;
    fd1 = d1;
    fo1 = o1;

    // getStatic -> this
    b1 = static_b1;
    c1 = static_c1;
    B1 = static_B1;
    s1 = static_s1;
    i1 = static_i1;
    l1 = static_l1;
    f1 = static_f1;
    d1 = static_d1;
    o1 = static_o1;

    // putStatic -> this
    static_b1 = b1;
    static_c1 = c1;
    static_B1 = B1;
    static_s1 = s1;
    static_i1 = i1;
    static_l1 = l1;
    static_f1 = f1;
    static_d1 = d1;
    static_o1 = o1;

    // getField -> ext
    i1 = super.ext_fi1;
    l1 = super.ext_fl1;
    d1 = super.ext_fd1;
    o1 = super.ext_fo1;

    // putField -> ext
    ext_fi1 = i1;
    ext_fl1 = l1;
    ext_fd1 = d1;
    ext_fo1 = o1;

    // getField -> this
    BC178to181 thisObj = new BC178to181();
    i1 = thisObj.fi1;
    thisObj.fd1 = d1;

    // getField -> ext static
    i1 = ExtField2.fi1;
    l1 = ExtField2.fl1;
    d1 = ExtField2.fd1;
    o1 = ExtField2.fo1;

    // putField -> ext static
    ExtField2.fi1 = i1;
    ExtField2.fl1 = l1;
    ExtField2.fd1 = d1;
    ExtField2.fo1 = o1;
  }

  public BC178to181() {
  }
}

class ExtField1 {
  protected int ext_fi1;
  protected long ext_fl1;
  protected double ext_fd1;
  protected Object ext_fo1;
}

class ExtField2 {
  public static int fi1;
  public static long fl1;
  public static double fd1;
  public static Object fo1;
}
