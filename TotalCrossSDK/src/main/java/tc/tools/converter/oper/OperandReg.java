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
package tc.tools.converter.oper;

import totalcross.util.ElementNotFoundException;
import totalcross.util.IntHashtable;

public class OperandReg extends Operand {
  // register index
  public int index;
  public boolean isTemp;

  // These hash table associates a local index of the current frame (Java) to a register (TClass).
  public static IntHashtable hashI = new IntHashtable(31);
  public static IntHashtable hash64 = new IntHashtable(31);
  public static IntHashtable hashO = new IntHashtable(31);

  /* next number of register to be attributed */
  public static int nextRegI;
  public static int nextReg64;
  public static int nextRegO;

  /* register count reserved (to parameters) */
  public static int paramRegI;
  public static int paramReg64;
  public static int paramRegO;

  /* The index of the parameter in the Java Frame */
  public static int paramIdx;
  static boolean showni, showno, shown64;

  public static void init(String params[], boolean methodIsStatic) {
    showni = showno = shown64 = false;
    hashI.clear();
    hash64.clear();
    hashO.clear();

    nextRegI = 0;
    nextReg64 = 0;
    if (methodIsStatic) {
      nextRegO = 0;
      paramIdx = 0;
    } else {
      hashO.put(0, 0);
      nextRegO = 1;
      paramIdx = 1;
    }

    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        switch (params[i].charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I': // boolean, char, byte, short, int
          hashI.put(paramIdx++, nextRegI++);
          break;
        case 'J':
        case 'F':
        case 'D': // long, float, double
          hash64.put(paramIdx, nextReg64++);
          paramIdx += 2;
          break;
        default: // *** case 'L':  case '[': ***  object
          hashO.put(paramIdx++, nextRegO++);
        }
      }
    }

    paramRegI = nextRegI;
    paramReg64 = nextReg64;
    paramRegO = nextRegO;

    nextRegI = nextReg64 = nextRegO = 64;
  }

  // Constructor for temporary registers
  public OperandReg(int kind) {
    super(kind);
    isTemp = true;
    switch (kind) {
    case opr_regI:
    case opr_regIb:
    case opr_regIs:
    case opr_regIc:
      index = nextRegI++;
      break;

    case opr_regL:
    case opr_regD:
      index = nextReg64++;
      break;

    case opr_regO:
      index = nextRegO++;
      break;
    }
  }

  public OperandReg(int kind, String wordIndex, int index) {
    super(kind);
    this.index = index;
  }

  // Constructor for local variables in current java frame
  public OperandReg(int kind, int framePosition) {
    super(kind);
    switch (kind) {
    case opr_regI:
    case opr_regIb:
    case opr_regIs:
      try {
        index = hashI.get(framePosition);
      } catch (ElementNotFoundException e) {
        index = nextRegI++;
        hashI.put(framePosition, index);
      }
      break;
    case opr_regL:
    case opr_regD:
      try {
        index = hash64.get(framePosition);
      } catch (ElementNotFoundException e) {
        index = nextReg64++;
        hash64.put(framePosition, index);
      }
      break;

    case opr_regO:
      try {
        index = hashO.get(framePosition);
      } catch (ElementNotFoundException e) {
        index = nextRegO++;
        hashO.put(framePosition, index);
      }
      break;
    }
  }

  @Override
  public boolean isReg() {
    return true;
  }
}
