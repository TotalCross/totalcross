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

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.io.ResizeRecord;
import totalcross.ui.dialog.MessageBox;

/**
 * The game highscores management class. <br>
 * <br>
 * The highscores are stored in a waba PDBFile object which is linked to the
 * application through the software creatorID. This causes the highscores
 * database deletion when the game with the same creatorID is erased.
 * <p>
 * The complete database name is composed by the game name 'appl' and its
 * creatorID, both provided during the engine setup.<br>
 * The highscores database name is : ${appl}_HSC.${creatorID}.DATA
 * <p>
 * You can find a complete game API sample named 'Ping' in the TotalCross samples
 * folder.<br>
 * Here is some sample code:
 *
 * <pre>
 * import totalcross.game.*;
 * import totalcross.util.props.*;
 * ...
 *
 * &lt;i&gt;public class Ping extends &lt;U&gt;GameEngine&lt;/U&gt;&lt;/i&gt; {
 *
 * // constructor
 * public Ping()
 * {
 *   totalcross.sys.Settings.setPalmOSStyle(true);
 *
 *   // define the game API setup attributes
 *
 *   gameName             = &quot;Ping&quot;;
 *
 *   // when not run on device, appCreatorId does not always return the same value.
 *
 *   gameCreatorID        = Settings.onJavaSE ? totalcross.sys.Settings.appCreatorId:&quot;PiNg&quot;;
 *
 *   gameVersion          = 100;   // v1.00
 *   gameHighscoresSize   = 7;     // store the best 7 highscores
 *   gameRefreshPeriod    = 75;    // 75 ms refresh periods
 *   gameIsDoubleBuffered = true;  // used double buffering to prevent flashing display
 *   gameDoClearScreen    = true;  // screen is cleared before each frame display
 *   gameHasUI            = false; // no UI elements, frame displays are optimized
 *   ...
 * }
 *
 * public void addUserScore(Property.Str user,int score) {
 *
 *   &lt;B&gt;HighScores&lt;/B&gt; hs=&lt;U&gt;getHighScores&lt;/U&gt;();
 *
 *   // try to add a new score to the highscores
 *
 *   HighScoreEntry insEntry=&lt;B&gt;hs.add&lt;/B&gt;(score,user);
 *
 *   if (insEntry!=null) {    // is score a highscore ?
 *
 *     // get access to all highscores
 *     HighScoreEntry[] entries=&lt;B&gt;hs.getEntries()&lt;/B&gt;;
 *
 *     for (int n=0; n&lt;&lt;B&gt;hs.size&lt;/B&gt;(); n++) {
 *
 *       // access the nth highscore entry
 *       HighScoreEntry entry=entries[n];
 *
 *       Vm.debug(&quot;name: &quot;+entry.name+&quot; score=&quot;+Convert.toString(entry.score));
 *     }
 *
 *   }
 *
 * }
 * </pre>
 *
 * @author Frank Diebolt
 * @version 1.0
 */
@Deprecated
public class HighScores extends PDBFile {
  private final static String dbName_suffix = "_HSC.";
  private final static String dbType = ".DATA";

  HighScoreEntry entries[];
  private boolean dirty;
  private int validEntries; // number of valid highscore entries

  /**
   * This class must be instantiated using the
   * GameEngineMainWindow.getHighScores
   *
   * @throws totalcross.io.IOException
   */
  protected HighScores(GameEngine engine) throws totalcross.io.IOException {
    super(engine.gameName + dbName_suffix + engine.gameCreatorID + dbType, PDBFile.CREATE);

    entries = new HighScoreEntry[engine.gameHighscoresSize];
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new HighScoreEntry(this);
    }
    dirty = false;

    if (getRecordCount() < 1) {
      return;
    }
    try {
      setRecordPos(0);
      DataStream ds = new DataStream(this);

      /* int version= */ds.readInt();
      /*
       * this test is not needed until a database format change occurs... if
       * (version>GameEngine.GAME_ENGINE_VERSION) throw new
       * GameEngineException("HS:game API "+GameEngine.GAME_ENGINE_VERSION+" out
       * of date error");
       */

      validEntries = ds.readInt();
      for (int i = 0; i < entries.length; i++) {
        if (i < validEntries) {
          entries[i].name = ds.readString();
          entries[i].score = ds.readInt();
        }
      }
    } catch (IOException e) {
      MessageBox.showException(e, false);
    }
  }

  /**
   * Amount of highscore entries.
   *
   * @return the number of entries in the highscore table.
   */
  public final int size() {
    return entries.length;
  }

  /**
   * Retrieve the highscore entries.
   *
   * @return an array to the highscore entries.
   */
  public final HighScoreEntry[] getEntries() {
    return entries;
  }

  /**
   * add a new score to the highscore table.
   *
   * @param score
   *           to add.
   * @param name
   *           of the performer, may be null.
   * @see HighScoreEntry
   * @return added entry or null
   */
  public HighScoreEntry add(int score, String name) {
    int insert;

    // lookup the position to insert the score
    for (insert = 0; insert < entries.length; insert++) {
      if (entries[insert].name == null || score > entries[insert].score) {
        break;
      }
    }

    if (insert >= entries.length) {
      return null; // didn't found a lower score to replace
    }

    // reuse the last highscore entry, sorry but you are ejected
    HighScoreEntry free = entries[entries.length - 1];

    // create a free entry by moving back the least scores
    for (int m = entries.length - 2; m >= insert; m--) {
      entries[m + 1] = entries[m];
    }

    // store the new score
    free.name = name;
    free.score = score;
    entries[insert] = free;
    if (validEntries < entries.length) {
      validEntries++;
    }

    dirty = true;
    return entries[insert];
  }

  /**
   * Stores the highscores database.
   *
   * @return false if no changes to save or an error occurs
   */
  public boolean save() {
    if (!dirty) {
      return false;
    }

    // use this nice object to resize the highscore record
    ResizeRecord rs = new ResizeRecord(this, 100);
    try {
      rs.restartRecord(0);

      DataStream ds = new DataStream(rs);
      ds.writeInt(GameEngine.GAME_ENGINE_VERSION);
      ds.writeInt(validEntries);
      for (int i = 0; i < validEntries; i++) {
        ds.writeString(entries[i].name);
        ds.writeInt(entries[i].score);
      }

      rs.endRecord();
    } catch (totalcross.io.IOException e) {
      throw new GameEngineException(e.getMessage());
    }
    dirty = false;

    return true;
  }

  /**
   * closes the highscores database.
   *
   * @throws totalcross.io.IOException
   */
  @Override
  public void close() throws totalcross.io.IOException {
    save();
    super.close();
  }
}
