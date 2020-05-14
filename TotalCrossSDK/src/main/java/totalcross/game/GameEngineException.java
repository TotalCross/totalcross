// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
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
