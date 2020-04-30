// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.ResizeRecord;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.Time;
import totalcross.sys.Vm;

/**
 * A Logger object is used to log messages for a specific system or application
 * component. Loggers are normally named, using a hierarchical dot-separated
 * namespace. Logger names can be arbitrary strings, but they should normally be
 * based on the package name or class name of the logged component, such as
 * totalcross.net or totalcross.io. In addition it is possible to retrieve one
 * global "anonymous" Logger that can be used in the whole system.
 *
 * A logger can be created or retrieved (if it already exists) by calling getLogger.
 *
 * To dispose a logger after using it, just call dispose and it will be permanently
 * discarded.
 *
 * To log a message, you may call the log method or any of the other convenience
 * methods (info, severe, entering, etc).
 *
 * @author Bruno Soares
 * @version 1.0 04 Jun 2008
 */
public class Logger {
  private String name;
  private Vector outputHandlers;
  private int level;
  private byte[] separator;
  private StringBuffer sbuf = new StringBuffer(64);
  private Time time = new Time();

  /** OFF is a special level that can be used to turn off logging. */
  public static final int OFF = 0;

  /** FINEST indicates a highly detailed tracing message. */
  public static final int FINEST = 1;

  /** FINER indicates a fairly detailed tracing message. */
  public static final int FINER = 2;

  /** FINE is a message level providing tracing information. */
  public static final int FINE = 4;

  /** CONFIG is a message level for static configuration messages. */
  public static final int CONFIG = 8;

  /** INFO is a message level for informational messages. */
  public static final int INFO = 16;

  /** WARNING is a message level indicating a potential problem. */
  public static final int WARNING = 32;

  /** SEVERE is a message level indicating a serious failure. */
  public static final int SEVERE = 64;

  /** ALL indicates that all messages should be logged. */
  public static final int ALL = 127;

  static class DebugConsoleWrapper extends Stream {
    byte[][] bs = new byte[3][];
    int p;
    StringBuffer sb = new StringBuffer(1024);

    @Override
    public int readBytes(byte[] buf, int start, int count) throws IOException {
      return -1;
    }

    @Override
    public int writeBytes(byte[] buf, int start, int count) throws IOException {
      bs[p++] = buf;
      return count;
    }

    public void flush() {
      sb.setLength(0);
      for (int i = 0; i < p; i++) {
        sb.append(new String(bs[i]));
        bs[i] = null;
      }
      int l = sb.length();
      if (sb.charAt(l - 1) == '\n') {
        sb.setLength(l - 1);
      }
      Vm.debug(sb.toString());
      p = 0;
    }

    @Override
    public void close() throws IOException {
    }
  }

  /** Stream where the bytes are written to the debug console */
  public static final Stream DEBUG_CONSOLE = new DebugConsoleWrapper();

  private static int defaultLevel = ALL;
  private static String defaultSeparator = "\n";
  private static Logger global;
  private static Hashtable loggers = new Hashtable(5);

  static {
    // Initialize TotalCross default loggers
    getLogger("totalcross", Logger.WARNING | Logger.SEVERE, Logger.DEBUG_CONSOLE);
    getLogger("totalcross.event", Logger.WARNING | Logger.SEVERE, Logger.DEBUG_CONSOLE);
    getLogger("totalcross.net", Logger.WARNING | Logger.SEVERE, Logger.DEBUG_CONSOLE);
    getLogger("totalcross.sys", Logger.WARNING | Logger.SEVERE, Logger.DEBUG_CONSOLE);
  }

  /**
   * Internal use only.
   */
  private Logger(String name) {
    this.name = name;
    outputHandlers = new Vector();
    level = defaultLevel;
    separator = defaultSeparator.getBytes();
  }

  /**
   * Returns the logger with a specific name, keeping its level and output
   * handlers unchanged. If the logger does not exist, it will be created
   * and stored for future use.
   * @param name The logger name.
   * @return The logger.
   * @throws NullPointerException if name is null.
   */
  public static Logger getLogger(String name) {
    return getLogger(name, -1, null);
  }

  /**
   * Returns the logger with a specific name, setting the level specified
   * and keeping the output handlers unchanged. If the logger does not exist,
   * it will be created and stored for future use.
   * @param name The logger name.
   * @param level The logger level to set or -1 to keep it unchanged.
   * @return The logger.
   * @throws NullPointerException if name is null.
   */
  public static Logger getLogger(String name, int level) {
    return getLogger(name, level, null);
  }

  /**
   * Returns the logger with a specific name, setting the level specified
   * and optionally adding the given stream to the list of output handlers.
   * If the logger does not exist, it will be created and stored for future use.
   * @param name The logger name.
   * @param level The logger level to set or -1 to keep it unchanged.
   * @param outputStream The stream to add to the logger's list of output
   * handlers or <code>null</code> to keep it unchanged.
   * @return The logger.
   * @throws NullPointerException if name is null.
   */
  public static Logger getLogger(String name, int level, Stream outputStream) {
    // Get logger (create if needed)
    Logger logger = (Logger) loggers.get(name);
    if (logger == null) {
      logger = new Logger(name);
      loggers.put(name, logger);
    }

    // Set new logger level (level = -1 means DO NOT CHANGE the current level)
    if (level != -1) {
      logger.setLevel(level);
    }

    // Add DEBUG_CONSOLE to the list of output handlers (if not added yet)
    if (outputStream != null) {
      logger.addOutputHandler(outputStream);
    }

    return logger;
  }

  /**
   * Returns the global anonymous logger.
   * @return Always return the global logger.
   */
  public static Logger getGlobalLogger() {
    if (global == null) {
      global = new Logger(null);
    }

    return global;
  }

  /**
   * Returns the logger name.
   * @return The name provided when this logger was created or null, if this
   * is the global anonymous logger.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current logger level.
   * @return An integer number representing the current logger level. To check
   * if the logger is set to log a specific type of message (SEVERE, WARNING, etc),
   * just check if the level OR'ed with the message level is different than zero.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Sets the current logger level.
   * @param level The logger level to set. This should be a composition of one or more
   * of the message types constants (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE) or
   * OFF, if you want to disable all logging, or ALL, if you want to enable all logging.
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * Sets the initial logger level, which will be used when a new logger is created.
   * @param level The logger level to set. This should be a composition of one or more
   * of the message types constants (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE) or
   * OFF, if you want to disable all logging, or ALL, if you want to enable all logging.
   */
  public static void setDefaultLevel(int level) {
    defaultLevel = level;
  }

  /**
   * Returns the initial logger level, which is used when a new logger is created.
   * @return An integer number representing the default logger level.
   */
  public static int getDefaultLevel() {
    return defaultLevel;
  }

  /**
   * Gets the string used to separate two log messages.
   * @return The string written after each log message.
   */
  public String getSeparator() {
    return separator == null ? null : new String(separator);
  }

  /**
   * Sets the string used to separate two log messages.
   * @param separator The string written after each log message.
   */
  public void setSeparator(String separator) {
    this.separator = separator == null ? null : separator.getBytes();
  }

  /**
   * Sets the initial message separator, which will be used when a new logger is
   * created. A separator is a string that separates two log messages (e.g.: \n
   * (newline), white spaces, etc).
   * @param separator The separator string.
   */
  public static void setDefaultSeparator(String separator) {
    defaultSeparator = separator;
  }

  /**
   * Returns the initial message separator, which is used when a new logger is
   * created. A separator is a string that separates two log messages (e.g.: \n
   * (newline), white spaces, etc).
   * @return The separator string.
   */
  public static String getDefaultSeparator() {
    return defaultSeparator;
  }

  /**
   * Adds an output stream to the logger's output handler set. This means that every logged
   * message (depending on the current logger level) will be written to this stream.
   * @param output The output stream.
   */
  public void addOutputHandler(Stream output) {
    if (output == null) {
      throw new NullPointerException();
    }

    if (outputHandlers.indexOf(output) == -1) {
      outputHandlers.addElement(output);
    }
  }

  /**
   * Removes an output handler from the logger's output handler set.
   * @param output The output stream.
   * @return True, if and only if, the stream was successfully removed.
   */
  public boolean removeOutputHandler(Stream output) {
    if (output == null) {
      throw new NullPointerException();
    }

    return outputHandlers.removeElement(output);
  }

  /**
   * Get the output handlers associated with this logger.
   * @return An array of all registered output handlers.
   */
  public Stream[] getOutputHandlers() {
    Stream[] array = new Stream[outputHandlers.size()];
    outputHandlers.copyInto(array);
    return array;
  }

  /**
   * Logs a given message.
   * @param level The message level.
   * @param message The message to log.
   * @param prependInfo A flag indicating whether this log message must be prepended
   * with the current date and time, level string, logger name, etc.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void log(int level, String message, boolean prependInfo) {
    if ((this.level & level) != 0) {
      StringBuffer sb = sbuf;
      sb.setLength(0);

      if (prependInfo) {
        time.update();
        Convert.appendTimeStamp(sb, time, true, true); // guich@tc123_62
        if (name != null) {
          sb.append(" - ").append(name);
        }
        sb.append(" - ");
        switch (level) {
        case FINEST:
          sb.append("[ FINEST  ] ");
          break;
        case FINER:
          sb.append("[ FINER   ] ");
          break;
        case FINE:
          sb.append("[ FINE    ] ");
          break;
        case CONFIG:
          sb.append("[ CONFIG  ] ");
          break;
        case INFO:
          sb.append("[ INFO    ] ");
          break;
        case WARNING:
          sb.append("[ WARNING ] ");
          break;
        case SEVERE:
          sb.append("[ SEVERE  ] ");
          break;
        }
      }

      if (message == null) {
        message = "null";
      }

      byte[] b1 = Convert.getBytes(sb); // guich@tc123_35: don't append the message into the StringBuffer...
      byte[] b2 = message.getBytes(); // ... just use its bytes directly
      for (int i = outputHandlers.size() - 1; i >= 0; i--) {
        try {
          Stream s = (Stream) outputHandlers.items[i];

          if (b1 != null) {
            s.writeBytes(b1, 0, b1.length);
          }
          s.writeBytes(b2, 0, b2.length);
          if (separator != null) {
            s.writeBytes(separator, 0, separator.length);
          }

          if (s instanceof DebugConsoleWrapper) {
            ((DebugConsoleWrapper) s).flush();
          } else if (level == SEVERE && s instanceof File) {
            ((File) s).flush();
          }
        } catch (IOException e) {
        }
      }
    }
  }

  private static final byte[] NULL_BYTES = "null".getBytes();

  /**
   * Used internally.
   */
  public void logInfo(StringBuffer message) // guich@tc123_62
  {
    if ((this.level & INFO) != 0) {
      byte[] b2 = message == null ? NULL_BYTES : Convert.getBytes(message); // ... just use its bytes directly
      for (int i = outputHandlers.size() - 1; i >= 0; i--) {
        try {
          Stream s = (Stream) outputHandlers.items[i];

          s.writeBytes(b2, 0, b2.length);
          if (separator != null) {
            s.writeBytes(separator, 0, separator.length);
          }

          if (s instanceof DebugConsoleWrapper) {
            ((DebugConsoleWrapper) s).flush();
          }
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Log a FINEST message. If the logger is currently enabled for the FINEST
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void finest(String message) {
    log(FINEST, message, true);
  }

  /**
   * Log a FINER message. If the logger is currently enabled for the FINER
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void finer(String message) {
    log(FINER, message, true);
  }

  /**
   * Log a FINE message. If the logger is currently enabled for the FINE
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void fine(String message) {
    log(FINE, message, true);
  }

  /**
   * Log a CONFIG message. If the logger is currently enabled for the CONFIG
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void config(String message) {
    log(CONFIG, message, true);
  }

  /**
   * Log a INFO message. If the logger is currently enabled for the INFO
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void info(String message) {
    log(INFO, message, true);
  }

  /**
   * Log a WARNING message. If the logger is currently enabled for the WARNING
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void warning(String message) {
    log(WARNING, message, true);
  }

  /**
   * Log a SEVERE message. If the logger is currently enabled for the SEVERE
   * message level then the given message is written to all the registered output
   * handler streams.
   * @param message The message to log.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void severe(String message) {
    log(SEVERE, message, true);
  }

  /**
   * Log a method entry. This is a convenience method that can be used to log
   * entry to a method. A record with message "ENTRY", log level FINER, and
   * the given sourceMethod and sourceClass is logged.
   * @param sourceClass The name of class that issued the logging request.
   * @param sourceMethod name of method that is being entered.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void entering(String sourceClass, String sourceMethod, String params) {
    log(FINER, "ENTRY " + sourceClass + "." + sourceMethod + "(" + params + ")", true);
  }

  /**
   * Log a method return. This is a convenience method that can be used to log
   * entry to a method. A record with message "RETURN", log level FINER, and
   * the given sourceMethod and sourceClass is logged.
   * @param sourceClass The name of class that issued the logging request.
   * @param sourceMethod The name of method that is being returned.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void exiting(String sourceClass, String sourceMethod) {
    log(FINER, "RETURN " + sourceClass + "." + sourceMethod, true);
  }

  /**
   * Log throwing an exception. This is a convenience method to log that a method
   * is terminating by throwing an exception. The logging is done using the
   * FINER level. If the logger is currently enabled for the given message level
   * then the record is forwarded to all registered output handlers.
   * The record's message is set to "THROW".
   * @param sourceClass he name of class that issued the logging request.
   * @param sourceMethod The name of method that is throwing the exception.
   * @param thrown The Throwable that is being thrown.
   * @throws NullPointerException if one or more registered output handler
   * streams are null or this logger has been disposed.
   */
  public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
    log(SEVERE, "THROW " + sourceClass + "." + sourceMethod + ": " + Vm.getStackTrace(thrown), true);
  }

  /**
   * Permanently discard this logger, removing it from the loggers registry.
   * @param closeOutputHandlers If <code>true</code>, all output handler streams
   * will be closed before disposing this logger.
   */
  public void dispose(boolean closeOutputHandlers) {
    if (name != null) // the global logger cannot be disposed
    {
      if (closeOutputHandlers) {
        for (int i = outputHandlers.size() - 1; i >= 0; i--) {
          try {
            Stream s = (Stream) outputHandlers.items[i];
            if (s instanceof ResizeRecord) {
              ((ResizeRecord) s).getStream().close(); // close the PDB associated to it, since ResizeRecord no longer closes the underlying stream
            } else {
              s.close();
            }
          } catch (IOException ex) {
          }
        }
      }

      loggers.remove(name);
      name = null;
      outputHandlers = null;
    }
  }
}
