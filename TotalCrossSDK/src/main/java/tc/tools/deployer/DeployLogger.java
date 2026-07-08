// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class DeployLogger {
  public enum Level {
    QUIET,
    NORMAL,
    VERBOSE,
    DEBUG
  }

  private static final String NEWLINE = System.lineSeparator();
  private static Level level = Level.NORMAL;
  private static String agentLogPath;
  private static BufferedWriter agentWriter;

  private DeployLogger() {
  }

  public static synchronized void configure(Level newLevel, String newAgentLogPath) {
    if (newLevel != null) {
      level = newLevel;
    }
    setAgentLogPath(newAgentLogPath);
  }

  public static synchronized void setLevel(Level newLevel) {
    if (newLevel != null) {
      level = newLevel;
    }
  }

  public static synchronized Level getLevel() {
    return level;
  }

  public static boolean isQuiet() {
    return level == Level.QUIET;
  }

  public static boolean isDebug() {
    return level == Level.DEBUG;
  }

  public static boolean isVerbose() {
    return level == Level.VERBOSE || level == Level.DEBUG;
  }

  public static synchronized void setAgentLogPath(String newAgentLogPath) {
    agentLogPath = newAgentLogPath;
    closeAgentWriter();
    if (agentLogPath == null || agentLogPath.trim().isEmpty()) {
      return;
    }
    try {
      File agentLogFile = new File(agentLogPath);
      File parent = agentLogFile.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
      agentWriter = new BufferedWriter(new FileWriter(agentLogFile, false));
    } catch (IOException e) {
      agentWriter = null;
      System.err.println("Could not open deploy agent log at " + agentLogPath + ": " + e.getMessage());
    }
  }

  public static synchronized void close() {
    closeAgentWriter();
  }

  public static void normal(String message) {
    write(message, Level.NORMAL, false, false);
  }

  public static void verbose(String message) {
    write(message, Level.VERBOSE, false, false);
  }

  public static void debug(String message) {
    write(message, Level.DEBUG, false, false);
  }

  public static void force(String message) {
    write(message, Level.QUIET, true, false);
  }

  public static void warn(String message) {
    write(message, Level.QUIET, true, true);
  }

  public static void error(String message) {
    warn(message);
  }

  public static void agent(String message) {
    writeToAgentLog(message);
  }

  private static synchronized void write(String message, Level minimumLevel, boolean alwaysStdout, boolean stderr) {
    if (message == null) {
      return;
    }
    if (alwaysStdout || level.ordinal() >= minimumLevel.ordinal()) {
      if (stderr) {
        System.err.println(message);
      } else {
        System.out.println(message);
      }
    }
    writeToAgentLog(message);
  }

  private static void writeToAgentLog(String message) {
    if (agentWriter == null || message == null) {
      return;
    }
    try {
      agentWriter.write(message);
      agentWriter.write(NEWLINE);
      agentWriter.flush();
    } catch (IOException e) {
      System.err.println("Could not write to deploy agent log at " + agentLogPath + ": " + e.getMessage());
      closeAgentWriter();
    }
  }

  private static synchronized void closeAgentWriter() {
    if (agentWriter == null) {
      return;
    }
    try {
      agentWriter.close();
    } catch (IOException e) {
      System.err.println("Could not close deploy agent log at " + agentLogPath + ": " + e.getMessage());
    } finally {
      agentWriter = null;
    }
  }
}
