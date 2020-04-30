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

import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.ImageException;

/**
 * This abstract class represents the game API engine.
 * <br>
 * <pre>
 * Version 1.1 of the GameEngine features:
 * <ul>
 * <li>Highscores management.
 * <li>Versionned options management of String, Integer, Double and Boolean types.
 * <li>Quick text display through text pre-rendering for game status & score display.
 * <li>Sprite/animated sprite support.
 * <li>Animation control.
 * <li>AnimatedButton control.
 * <li>Game framework extending the TotalCross MainWindow.
 * </ul>
 *
 * You can find a complete game API sample named 'Ping' in the TotalCross samples folder.<br>
 * <B>NOTE: This sample may be used as a skeleton for your own game development and the
 * source reading may be much helpful to understand this framework.</B>
 * You also can change the game settings at the top of the file 'Ping.java' to
 * experiment the different game API behaviours.<br>
 * <B>Further i recommand the reading of the TotalCross game API tutorial that gives
 * many details on this framework use.</B>
 *
 *
 * <u><B>1) GAME FRAMEWORK DESCRIPTION</B></u>
 *
 * Basically the game engine consists in a class that extends TotalCross's MainWindow.
 * This class is named <A href="GameEngine.html"><B>GameEngine</B></A> and provides
 * many game oriented services like game settings and game highscores management.
 * You won't have to access the game associated settings nor the highscore databases
 * directly, you will have to use <A href="HighScores.html"><B>HighScores</B></A>
 * and <A href="Options.html"><B>Options</B></A> interfaces instead.
 * This services are retrievable by the <i>getHisghscores()</i> and the <i>getOptions()</i> calls.<br>
 *
 * <u><B>2) GAME SETUP</B></u>
 *
 * A game using this API has to extend the <A href="GameEngine.html"><B>GameEngine</B></A> class,
 * like this:
 *
 * <i>public class MyOwnGame extends GameEngine {
 * ...
 * }
 * </i>
 *
 * to setup the game engine, you will have to provide some information.
 * These information are defined through the following GameEngine member variables:
 *
 * -<u>gameName</u>             the name of the game, this information is used to name the game associated databases.
 * -<u>gameCreatorID</u>        the creatorID of the game
 * -<u>gameVersion</u>          the game version number
 * -<u>gameHighscoresSize</u>   number of best scores to save in the highscores database
 * -<u>gameRefreshPeriod</u>    refresh period in milliseconds for action games, or NO_AUTO_REFRESH for non animated games.
 * -<u>gameDoClearScreen</u>    enable/disable the whole screen erasing between frames displays.
 * -<u>gameHasUI</u>            declare UI uses, if false the drawing is improved
 *
 * You see below the definition of a the sample game named "Scape":
 *
 * import totalcross.game.*;
 * import totalcross.util.props.*;
 * ...
 *
 * <i>public class Ping extends <U>GameEngine</U></i> {
 *
 * // constructor
 * public Ping()
 * {
 *   totalcross.sys.Settings.setPalmOSStyle(true);
 *
 *   // define the game API setup attributes
 *
 *   gameName             = "Scape";
 *
 *   // when not run on device, appCreatorId does not always return the same value.
 *
 *   gameCreatorID        = Settings.onJavaSE ? totalcross.sys.Settings.appCreatorId:"Scpe";
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
 * <u><B>3) GAME FRAMEWORK DETAILS</B></u>
 *
 * The GameEngine class traps many TotalCross event handler functions to fulfill tasks
 * behind the scenes. Thus the following TotalCross functions cannot be overloaded in your
 * game's main window:
 * <i>
 * void initUI  ();
 * void onExit   ();
 * void onEvent  (Event ev);
 * </i>
 * they are replaced by the following game API functions that may be overloaded :
 * <i>
 * void onGameInit   ();
 * void onGameExit   ();
 *
 * void onKey        (KeyEvent evt);
 * void onPenDown    (PenEvent evt);
 * void onPenUp      (PenEvent evt);
 * void onPenDrag    (PenEvent evt);
 *
 * void onTimer      (ControlEvent evt);
 * void onOtherEvent (Event evt);
 * </i>
 *
 * The framework extends the event handlers to provide game specific events, such as:
 * <i>
 * void onGameStart  ();
 * void onGameStop   ();
 * </i>
 * The first one is called when the game mainloop starts (when the game enters the
 * <i>"run mode"</i>), the second one when the run mode is leaved.
 *
 * You can control (run/stop) and retrieve the current state by the game API functions and fields:
 * <i>
 * void    start     ();
 * void    stop      ();
 * boolean gameIsRunning;
 * </i>
 * The game API supports both arcade games that are "time driven" (a timer causes
 * frequent screen refreshes to get an animation of the game elements/objects)
 * and "static" games such as cards, puzzles, etc.
 *
 * When entering the game run mode by the <i>start()</i> call, time based games
 * arm a timer that causes scheduled calls to your overloaded <i>onPaint()</i>
 * to draw a new frame/image.<br>
 * On static games that may be designated as "user event driven" refreshes have to
 * be launched by an explicit call of <i>refresh()</i> that also causes the call
 * of your overloaded <i>onPaint()</i> to draw a new frame/image. Static games
 * can also use timers to signal the end of a reflection time, etc.<br>
 *
 * <u><B>4) GAME SPRITES</B></u>
 *
 * A <A href="Sprite.html"><B>Sprite</B></A> is a graphical object typicaly used in games that can be moved,
 * with collision detection feature and background saving/restoring capability.
 *
 * To create a sprite you will have to provide an Image object, it's transparency color
 * if needed (DRAW_SPRITE mode), a flag indicating if the background have to be saved
 * (required if screen clearing is disabled as far as the screen is not
 * erased to be redrawn completly) and the sprite valid positions area (Rect).
 *
 * You may also have to define the drawOp for the Sprite drawing. It's common values are:
 * <li>USE_CURRENT_DRAWOP default
 * <li>DRAW_SPRITE
 * <li>DRAW_PAINT
 *
 * You can override the onPositionChange() method to manage valid positions, collisions,
 * bounces or any other object position based condition.<br>
 * The default implementation checks position validity by using the <i>region</i> argument
 * of the sprite constructor.
 * see the "Scape" source code for more details.
 *
 * When background saving is enabled, each sprite display with the show() method
 * restores any previously saved background.<br>
 * In some games, sprites may overlap... if you write such a game, you may have to
 * call the sprite hide() method explicitly (which is not necessary in the common
 * usage) in the reverse draw order to restore the background.
 *
 * <u><B>5) ANIMATIONS</B></u>
 *
 * This <A href="Animation.html"><B>Animation</B></A> control provides an image sequence display. You can handle the
 * animation frames displays by your own, or call one of the several <i>start()</i>
 * methods provided to launch a thread driven animation.
 *
 * The animation is composed by a collection of Image objects that can be loaded
 * from indexed BMP files (one frame per image) or from a so-called multi-image
 * bitmap. It is an image format that contains all the animation frames side by side.
 * All the images share a same color palette and must have the same size.
 *
 * <u><B>6) ANIMATED BUTTONS</B></u>
 *
 * The <A href="AnimatedButton.html"><B>AnimatedButton</B></A> class is a button implementation that extends the
 * Animation class. It uses an animation containing the different states of the
 * button and their transition frames in a specific layout.
 *
 * </pre>
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
@Deprecated
public abstract class GameEngine extends GameEngineMainWindow {
  public final static int GAME_ENGINE_VERSION = 110; // version 1.1 - remodeled by guich
  /**
   * Name of the game. <br>
   * The Highscores/Options databases are prefixed with this name.
   */
  public String gameName;

  /**
   * Game CreatorID. <br>
   * The Highscores/Options databases references are created with this ID.<br>
   * This must be a String with exactly four characters.
   * This has the effect to link the databases to the game software
   * causing an automatic database removal when the application is
   * deleted from the device.
   */
  public String gameCreatorID;

  /**
   * Game version. <br>
   * This information is written in the Options database and may be compared
   * to it's old version retrievable by the getOldVersion() function of the
   * Options interface.
   * E.g. 100 for 1.00)
   */
  public int gameVersion;

  /**
   * Amount of highscores entries in the highscores database. <br>
   */
  public int gameHighscoresSize;

  /**
   * No automatic refresh. <br>
   * @see #gameRefreshPeriod
   */
  public final static int NO_AUTO_REFRESH = -1;

  /**
   * Automatic refresh period in milliseconds. <br>
   * The NO_AUTO_REFRESH value prevents the engine to do time
   * based refreshes. You will have to call explicitly the refresh() function
   * to force a screen repainting.
   * @see #refresh()
   */
  public int gameRefreshPeriod = NO_AUTO_REFRESH;

  /**
   * True if the screen should be cleared before the onPaint() call. <br>
   */
  public boolean gameDoClearScreen;

  /** True if the game is running. Set by the GameEngineMainWindow. To stop or
   * start the game use the methods stop or run. Setting this variable has
   * no effect.
   */
  protected boolean gameIsRunning;

  /** Must be set to true if the game screen has any control from the totalcross.ui.
   * package. Complex games should not have UI elements
   */
  protected boolean gameHasUI;

  /**
   * Event notication called when the game is initialized. <br>
   * It's the first place where API calls can take place because
   * the game engine has been initialized.<br>
   * You can do game initialization at this place. Typically Sprites may be
   * created in the overloaded function.
   */
  public abstract void onGameInit();

  /**
   * Event notication called when the game exits. <br>
   */
  public void onGameExit() {
  }

  /**
   * Event notication called when the game mainloop is entered. <br>
   */
  public void onGameStart() {
  }

  /**
   * Event notication called when the game mainloop is leaved. <br>
   * @see #stop
   */
  public void onGameStop() {
  }

  /**
   * Event notication called when a control event is signaled. <br>
   * @param evt control event that occurred.
   */
  public void onTimer(TimerEvent evt) {
  }

  /**
   * Event notication called when a key event is signaled. <br>
   * @param evt key event that occurred.
   */
  public void onKey(KeyEvent evt) {
  }

  /**
   * Event notication called when a pen down event is signaled. <br>
   * @param evt pen event that occurred.
   */
  public void onPenDown(PenEvent evt) {
  }

  /**
   * Event notication called when a pen up event is signaled. <br>
   * @param evt pen event that occurred.
   */
  public void onPenUp(PenEvent evt) {
  }

  /**
   * Event notication called when a pen drag/move event is signaled. <br>
   * @param evt pen event that occurred.
   */
  public void onPenDrag(PenEvent evt) {
  }

  /**
   * Event notication called when any other event is signaled. <br>
   * @param evt event that occurred.
   */
  public void onOtherEvent(Event evt) {
  }

  /** Called at each refresh to draw the current game state */
  @Override
  public void onPaint(Graphics g) {
  }

  /**
   * Get the game highscores.
   * @return HighScores.
   */
  @Override
  public HighScores getHighScores() {
    return super.getHighScores();
  }

  /**
   * Get a new instance of the game options.
   * @return Options.
   */
  @Override
  public Options getOptions() {
    return super.getOptions();
  }

  /**
   * Create a new TextRenderer.
   * A TextRenderer performs a fast String display with an optional integer value.
   * @param font to display with.
   * @param foreColor text color, may be null.
   * @param text to render.
   * @param maxDigits digits to display.
   * @return a new TextRenderer.
   * @throws ImageException
   * @see TextRenderer TextRenderer for more information
   */
  @Override
  public final TextRenderer createTextRenderer(Font font, int foreColor, String text, int maxDigits)
      throws ImageException {
    return super.createTextRenderer(font, foreColor, text, maxDigits);
  }

  /**
   * Create a new TextRenderer. A TextRenderer performs a fast String display with an optional integer value.
   * 
   * @param font
   *           to display with.
   * @param foreColor
   *           text color, may be null.
   * @param text
   *           to render.
   * @param maxDigits
   *           digits to display.
   * @param zeroPadding
   *           pad with leading zeros.
   * @return a new TextRenderer.
   * @throws ImageException
   * @see TextRenderer TextRenderer for more information
   */
  @Override
  public final TextRenderer createTextRenderer(Font font, int foreColor, String text, int maxDigits,
      boolean zeroPadding) throws ImageException // fdie@420_27
  {
    return super.createTextRenderer(font, foreColor, text, maxDigits, zeroPadding);
  }

  /** Must be called to start the game. */
  @Override
  final public void start() {
    super.start();
  }

  /** Must be called to make the game stop. */
  @Override
  final public void stop() {
    super.stop();
  }

  /**
   * This function causes an onPaint() call to draw a new frame.<br>
   * This function has to be called in non time based games to refresh
   * the complete screen.
   */
  @Override
  final public void refresh() {
    super.refresh();
  }

  /** Creates a new GameEngine */
  public GameEngine() {
    super.setGameEngine(this);
  }
}