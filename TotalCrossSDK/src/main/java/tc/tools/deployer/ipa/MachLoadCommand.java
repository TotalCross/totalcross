// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.IOException;

/**
 * More information in "Mac OS X Internals: A Systems Approach, by Amit Singh"
 */
public abstract class MachLoadCommand {
  public static final int LC_SEGMENT = 1;
  public static final int LC_SYMTAB = 2;
  public static final int LC_DYSYMTAB = 11;
  public static final int LC_CODE_SIGNATURE = 0x1d;
  public static final int LC_ENCRYPTION_INFO = 0x21;
  public static final int LC_DYLD_INFO = 0x22;
  public static final int LC_SEGMENT_64 = 0x19;

  public static MachLoadCommand readFromStream(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException {
    MachLoadCommand command = null;
    int commandType = (int) (reader.readUnsignedIntLE() & 0x7fffffff);
    long commandDataSize = reader.readUnsignedIntLE();

    switch (commandType) {
    case LC_SEGMENT:
      command = new MachLoadCommandSegment();
      break;

    case LC_SYMTAB:
      commandDataSize = 24;
      break;

    case LC_DYSYMTAB:
      commandDataSize = 80;
      break;

    case LC_SEGMENT_64:
      command = new MachLoadCommandSegment64();
      break;

    case LC_CODE_SIGNATURE:
      command = new MachLoadCommandCodeSignature();
      break;

    case LC_ENCRYPTION_INFO:
      commandDataSize = 20;
      break;

    case LC_DYLD_INFO:
      commandDataSize = 48;
      break;
    }

    if (command == null) {
      reader.skip(commandDataSize - 8);
    } else {
      command.parseFromStream(reader);
    }
    return command;
  }

  abstract protected void parseFromStream(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException;
}
