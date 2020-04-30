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
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.io.ResizeRecord;
import totalcross.util.Properties;

/**
 * The game options management class. <br>
 * <br>
 * The options are stored in a waba PDBFile object which is linked to the
 * application through the software creatorID. This causes the options database
 * deletion when the game with the same creatorID is erased.
 * <p>
 * The complete database name is composed by the game name 'appl' and its
 * creatorID, both provided during the engine initialization via the
 * GameEngineClient interface.<br>
 * The options database name is : ${appl}_OPT.${creatorID}.DATA
 * <p>
 * You can find a complete game API sample named 'Scape' in the TotalCross samples
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
 * // declare 2 game settings
 *
 * protected &lt;B&gt;Properties.Str&lt;/B&gt;      optUserName;
 * protected &lt;B&gt;Properties.Boolean&lt;/B&gt;  optSound;
 *
 * //---------------------------------------------------------
 * // overload the API's game init event.
 * // this function is called when the game is launched.
 * //---------------------------------------------------------
 *
 * &lt;i&gt;public void onGameInit()&lt;/i&gt; {
 *
 *   // access the game settings: 'username' &amp; 'sound'
 *   // if the properties do not yet exist, the default values are used
 *
 *   &lt;B&gt;Options&lt;/B&gt; settings=&lt;U&gt;getOptions&lt;/U&gt;();
 *
 *   optUserName = settings.&lt;B&gt;declareString&lt;/B&gt;    (&quot;userName&quot;,&quot;noname&quot;);
 *   optSound    = settings.&lt;B&gt;declareBoolean&lt;/B&gt;   (&quot;sound&quot;,false);
 *   ...
 *
 *   if (optSound.&lt;B&gt;value&lt;/B&gt;) Sound.tone(1520,10);
 *
 *  }
 * }
 * </pre>
 *
 * @author Frank Diebolt
 * @version 1.0
 */
@Deprecated
public class Options extends Properties {
  private PDBFile cat;
  private final static String dbName_suffix = "_OPT.";
  private final static String dbType = ".DATA";
  private static final String duplicatedProperty = "OPT:duplicated:";

  protected Options(GameEngine engine) {
    try {
      try {
        cat = new PDBFile(engine.gameName + dbName_suffix + engine.gameCreatorID + dbType, PDBFile.READ_WRITE);
      } catch (FileNotFoundException fnfe) {
        cat = new PDBFile(engine.gameName + dbName_suffix + engine.gameCreatorID + dbType, PDBFile.CREATE_EMPTY);
        cat.addRecord(100);
      }
      newVersion = engine.gameVersion;

      cat.setRecordPos(0);
      DataStream ds = new DataStream(cat);
      oldVersion = ds.readInt();
      load(ds);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Declare a boolean option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   *
   * @param name
   *           the property name.
   * @param value
   *           the property default value.
   * @return a boolean property object.
   */
  public Boolean declareBoolean(String name, boolean value) {
    Value v = get(name);
    if (v != null) {
      if (v.type == Boolean.TYPE) {
        return (Boolean) v;
      }
      throw new GameEngineException(duplicatedProperty + name);
    }
    Boolean b = new Boolean(value);
    put(name, b);
    return b;
  }

  /**
   * Declare a long option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   *
   * @param name
   *           the property name.
   * @param value
   *           the property default value.
   * @return a long property object.
   */
  public Long declareLong(String name, long value) {
    Value v = get(name);
    if (v != null) {
      if (v.type == Long.TYPE) {
        return (Long) v;
      }
      throw new GameEngineException(duplicatedProperty + name);
    }
    Long b = new Long(value);
    put(name, b);
    return b;
  }

  /**
   * Declare an integer option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   *
   * @param name
   *           the property name.
   * @param value
   *           the property default value.
   * @return an integer property object.
   */
  public Int declareInteger(String name, int value) {
    Value v = get(name);
    if (v != null) {
      if (v.type == Int.TYPE) {
        return (Int) v;
      }
      throw new GameEngineException(duplicatedProperty + name);
    }
    Int i = new Int(value);
    put(name, i);
    return i;
  }

  /**
   * Declare a double option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   *
   * @param name
   *           the property name.
   * @param value
   *           the property default value.
   * @return a double property object.
   */
  public Double declareDouble(String name, double value) {
    Value v = get(name);
    if (v != null) {
      if (v.type == Double.TYPE) {
        return (Double) v;
      }
      throw new GameEngineException(duplicatedProperty + name);
    }
    Double d = new Double(value);
    put(name, d);
    return d;
  }

  /**
   * Declare a string option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   *
   * @param name
   *           the property name.
   * @param value
   *           the property default value.
   * @return a string property object.
   */
  public Str declareString(String name, String value) {
    Value v = get(name);
    if (v != null) {
      if (v.type == Str.TYPE) {
        return (Str) v;
      }
      throw new GameEngineException(duplicatedProperty + name);
    }
    Str s = new Str(value);
    put(name, s);
    return s;
  }

  /**
   * The options database new version number.
   */
  public int newVersion;
  /**
   * The options database old version number.
   */
  public int oldVersion;

  /**
   * Get a property value given the key.
   *
   * @param key
   *           name of the property
   * @return Value that can be casted to Properties.Str, Properties.Int,...
   *         depending on the value type, that can be retrieved with the
   *         <code>type</code> read-only property.
   */
  public Properties.Value getProp(String key) {
    return get(key);
  }

  /**
   * stores the settings database. <br>
   *
   * @return false if an error occurs
   */
  public boolean save() {
    // use this nice object to resize the options record
    try {
      ResizeRecord rs = new ResizeRecord(cat, 100);
      rs.restartRecord(0);
      DataStream ds = new DataStream(rs);
      ds.writeInt(newVersion);
      save(ds);
    } catch (totalcross.io.IOException e) {
      return false;
    }

    return true;
  }

  /**
   * closes the settings database.
   *
   * @throws IOException
   */
  public void close() throws IOException {
    if (save()) {
      cat.close();
    }
  }
}
