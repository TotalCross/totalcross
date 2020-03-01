/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.sys;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public abstract class AbstractCharacterConverter extends Charset {

  protected AbstractCharacterConverter(String canonicalName, String[] aliases) {
    super(canonicalName, aliases);
  }

  public abstract char[] bytes2chars(byte bytes[], int offset, int length);

  public abstract byte[] chars2bytes(char chars[], int offset, int length);

  @Override
  public boolean contains(Charset cs) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public CharsetDecoder newDecoder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CharsetEncoder newEncoder() {
    // TODO Auto-generated method stub
    return null;
  }
}
