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
