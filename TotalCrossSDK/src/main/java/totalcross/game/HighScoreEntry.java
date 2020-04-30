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
 * A highscore entry. <br>
 * <br>
 * This class represents one entry in the game highscores database. A highscore entry
 * has a given rank and is composed by a score and a performer name.
 * @see HighScores
 */
@Deprecated
public class HighScoreEntry {
  private HighScores parent;
  /** The performer's name. */
  public String name;
  /** The performer's s*/
  public int score;

  protected HighScoreEntry(HighScores parent) {
    this.parent = parent;
  }

  /**
   * Get the rank of this Entry in the HighScores table.
   * @return score rank.
   */
  public int getRank() {
    // should be computed because entries may me messed up by inserts.
    int n = parent.entries.length;
    for (int i = 0; i < n; i++) {
      if (parent.entries[i] == this) {
        return i;
      }
    }
    return -1;
  }
}
