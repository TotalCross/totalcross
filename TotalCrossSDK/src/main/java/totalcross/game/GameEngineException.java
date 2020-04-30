// Copyright (C) 2000-2012 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.game;

/**
 * GameEngine exception. Note that it extends RuntimeException, and, thus, is unchecked.
 */
@Deprecated
public class GameEngineException extends RuntimeException {
  /** Constructs an empty Exception. */
  public GameEngineException() {
  }

  /** Constructs an exception with the given message. */
  public GameEngineException(String msg) {
    super(msg);
  }
}
