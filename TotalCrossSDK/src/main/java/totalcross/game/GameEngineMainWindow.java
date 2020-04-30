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

import totalcross.io.IOException;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.GfxSurface;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * Game MainWindow provided by the GameEngine. Note: you can't access this class
 * directly. You must use the methods provided by the GameEngine instead.
 *
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
@Deprecated
class GameEngineMainWindow extends MainWindow {
  /**
   * TotalCross game API version.
   */
  private static GameEngine engine;

  private Options options;
  private HighScores hscores;

  private boolean isRunning;
  private Graphics graf;

  private TimerEvent gameTimer;
  private boolean flicker;

  private Control gameTimerControl = new Control() {
    @Override
    public final void onEvent(Event e) {
      if (isRunning) {
        refresh();
      }

      if (!isRunning) {
        removeTimer(gameTimer);
        gameTimer = null;
        _doPaint();
        // Notifies the game run mode quit by calling onGameStop().
        engine.onGameStop();
        Control.isHighlighting = true; // guich@550_30: support pen less devices
      }
    }
  };

  /** Do nothing. All attributes must be set by the Game implementor */
  protected GameEngineMainWindow() {
    paintBackground = false;
  }

  /** Called just after the GameEngine is constructed */
  protected void setGameEngine(GameEngine engine) {
    GameEngineMainWindow.engine = engine;
  }

  /**
   * Returns a Graphics instance for the drawing surface. A new instance is
   * always returned
   */
  static final Graphics getEngineGraphics() {
    if (engine == null) {
      throw new GameEngineException("GameEngine not initialized");
    }
    return engine.getGraphics();
  }

  /** Returns the drawing surface */
  static final GfxSurface getSurface() {
    if (engine == null) {
      throw new GameEngineException("GameEngine not initialized");
    }
    return (GfxSurface) engine;
  }

  /**
   * Replace initUI() handling to initialize the engine.<br>
   * <B>Could not be overloaded.</B> Notifies onGameInit().<br>
   */
  @Override
  public final void initUI() {
    transparentBackground = true; // guich@tc122_54
    flicker = engine.gameDoClearScreen;
    if (!engine.gameHasUI) {
      graf = getGraphics();
    }

    engine.gameIsRunning = isRunning = false;

    engine.onGameInit();
  }

  /**
   * Replace onExit() handling to shutdown the engine.<br>
   * <B>Could not be overloaded.</B> Notifies onGameExit().
   */
  @Override
  public final void onExit() {
    engine.onGameExit();
    shutdown();
  }

  /**
   * Get a new instance of the game highscores.
   *
   * @return HighScores.
   */
  HighScores getHighScores() {
    if (hscores != null) {
      return hscores;
    }
    if (engine.gameHighscoresSize <= 0) {
      return null;
    }
    try {
      return hscores = new HighScores(engine);
    } catch (IOException e) {
      //         e.printStackTrace();
    }
    return null;
  }

  /**
   * Get a new instance of the game options.
   *
   * @return Options.
   */
  Options getOptions() {
    return options != null ? options : (options = new Options(engine));
  }

  /**
   * Create a new TextRenderer. A TextRenderer performs a fast String display
   * with an optional integer value.
   *
   * @param font
   *           to display with.
   * @param foreColor
   *           text color, may be null.
   * @param text
   *           to render.
   * @param maxDigits
   *           digits to display.
   * @return a new TextRenderer.
   * @throws ImageException
   * @see TextRenderer TextRenderer for more information
   */
  TextRenderer createTextRenderer(Font font, int foreColor, String text, int maxDigits) throws ImageException {
    return new TextRenderer(font, foreColor, backColor, text, maxDigits);
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
  TextRenderer createTextRenderer(Font font, int foreColor, String text, int maxDigits, boolean zeroPadding)
      throws ImageException //fdie@420_27
  {
    return new TextRenderer(font, foreColor, backColor, text, maxDigits, zeroPadding);
  }

  /*
   * Shutdown the GameEngine and releases its resources.
   */
  private final void shutdown() {
    engine.gameIsRunning = isRunning = false;

    try {
      if (hscores != null) {
        hscores.close();
      }
      if (options != null) {
        options.close();
      }
      if (gameTimer != null) {
        removeTimer(gameTimer);
      }
    } catch (totalcross.io.IOException e) {
    }
  }

  /*
   * Stops the game. <br> Arms a timer, because onGameStop() events that may
   * start a UI we have to be in the event handling.<br> The game will be
   * interrupted at next timer event.
   */
  void stop() {
    engine.gameIsRunning = isRunning = false;
    if (engine.gameRefreshPeriod <= 0) {
      gameTimer = super.addTimer(gameTimerControl, 10);
    }
  }

  /**
   * Start the game mainloop. <br>
   * NOTE: If the game is time based (arcade game), this function arms the
   * mainloop timer which causes scheduled onPaint() calls to let the game
   * redraw a new frame.<br>
   * For non time based games, you have to call explicitly the refresh()
   * function to redraw a frame (on pen events for instance or any other kind
   * of events...)
   *
   * @see #stop
   */
  void start() {
    // first let's clean the screen, maybe we can also set the background color
    //refresh();

    engine.onGameStart();
    Control.isHighlighting = false; // guich@550_30: support pen less devices
    engine.gameIsRunning = isRunning = true;

    if (engine.gameRefreshPeriod > 0) {
      gameTimer = super.addTimer(gameTimerControl, engine.gameRefreshPeriod);
    }

    // start first game display
    refresh();
  }

  /**
   * Use an image as background. <br>
   * The provided image is scaled if required to the screen size and displayed at each frame refresh. NOTE: the
   * "gameHasUI" also have to be set to false to support this feature
   * 
   * @throws ImageException
   */
  public Image useBackground(Image bg) throws ImageException //fdie@420_26
  {
    if (bg != null && (bg.getHeight() != height || bg.getWidth() != width)) {
      bg = bg.getScaledInstance(width, height);
    }
    bgSurface = bg;
    paintBackground = (bg != null);
    return bg;
  }

  protected Image bgSurface;
  private boolean paintBackground;

  /**
   * Refreshes the screen. If there are UI elements on the screen
   * (GameEngine.hasUI = true) calls the slower _doPaint. Otherwise, just
   * prepares the buffer and repaint it.
   */
  void refresh() {
    if (!engine.gameHasUI) {
      if (flicker && bgSurface == null) {
        graf.backColor = backColor;
        graf.fillRect(0, 0, totalcross.sys.Settings.screenWidth, totalcross.sys.Settings.screenHeight);
      } else if (paintBackground) {
        graf.drawImage(bgSurface, 0, 0); // tc: replaced copyScreen
        paintBackground = flicker; // continue background display if "clearscreen" is enabled
      }
      engine.onPaint(graf);
      updateScreen(); // tc100 g0.copyScreen(isurf, 0,0,this.height);
    } else {
      _doPaint();
    }
  }

  /**
   * Replace onEvent handling to identify and notify some usefull game
   * events.<br>
   * <B>Could not be overloaded.</B>
   *
   * @param evt
   *           event that occurs.
   */
  @Override
  public final void onEvent(Event evt) {
    switch (evt.type) {
    case TimerEvent.TRIGGERED:
      engine.onTimer((TimerEvent) evt);
      break;
    case KeyEvent.KEY_PRESS:
    case KeyEvent.SPECIAL_KEY_PRESS:
      engine.onKey((KeyEvent) evt);
      break;
    case PenEvent.PEN_DOWN:
      engine.onPenDown((PenEvent) evt);
      break;
    case PenEvent.PEN_DRAG:
      engine.onPenDrag((PenEvent) evt);
      break;
    case PenEvent.PEN_UP:
      engine.onPenUp((PenEvent) evt);
      break;
    default:
      engine.onOtherEvent(evt);
    }
  }
}
