// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Starts the distributed simulator with a real AWT frame on a virtual X11 display.
 * This test intentionally needs Xvfb; the ordinary SDK test remains headless.
 */
@Tag("simulator-gui")
class StandaloneSimulatorGuiTest {
    private static final String FIXTURE = "GuiStandaloneFixture";
    private static final String CLASS_NAME = "tc.tools.standalone." + FIXTURE;

    @Test
    void aggregateJarCreatesAwtWindowInitializesUiAndPaints(@TempDir Path tempDir) throws Exception {
        assertTrue(!java.awt.GraphicsEnvironment.isHeadless(),
                "GUI simulator smoke test requires a graphical display (run under xvfb-run)");

        Path sdkJar = StandaloneSdkJarTest.DISTRIBUTED_SDK;
        assertTrue(Files.isRegularFile(sdkJar), "the distributed SDK must exist");

        // Embedding the fixture keeps the invocation exactly java -jar, without
        // extra classpath arguments or a separate runtime implementation jar.
        Path source = tempDir.resolve(FIXTURE + ".java");
        Files.writeString(source, fixtureSource());
        Path classes = tempDir.resolve("classes");
        Files.createDirectories(classes);
        StandaloneSdkJarTest.compileFixtures(sdkJar, classes, source);

        // Keep the executable next to libs/ so the original manifest resolves
        // exactly the same external dependencies as the normal distribution.
        Path executable = Files.createTempFile(sdkJar.getParent(), "simulator-gui-test-", ".jar");
        try {
            StandaloneSdkJarTest.copySdkAndAddFixtures(executable, classes, List.of(FIXTURE));
            runAndVerify(executable, tempDir);
        } finally {
            Files.deleteIfExists(executable);
        }
    }

    private static String fixtureSource() {
        return "package tc.tools.standalone;\n"
                + "public final class " + FIXTURE + " extends totalcross.ui.MainWindow {\n"
                + "  private volatile boolean uiInitialized;\n"
                + "  private volatile boolean painted;\n"
                + "  @Override public void initUI() {\n"
                + "    add(new totalcross.ui.Label(\"Simulator smoke\"), CENTER, CENTER);\n"
                + "    uiInitialized = true;\n"
                + "    System.out.println(\"SIMULATOR_GUI_INIT_OK\");\n"
                + "  }\n"
                + "  @Override public void onPaint(totalcross.ui.gfx.Graphics g) {\n"
                + "    super.onPaint(g);\n"
                + "    if (!uiInitialized || painted) return;\n"
                + "    if (java.awt.GraphicsEnvironment.isHeadless())\n"
                + "      throw new IllegalStateException(\"AWT is headless\");\n"
                + "    boolean visible = java.util.Arrays.stream(java.awt.Frame.getFrames())\n"
                + "        .anyMatch(java.awt.Frame::isVisible);\n"
                + "    if (!visible) throw new IllegalStateException(\"AWT frame not visible\");\n"
                + "    g.foreColor = totalcross.ui.gfx.Color.BLUE;\n"
                + "    g.drawLine(2, 2, 10, 10);\n"
                + "    painted = true;\n"
                + "    System.out.println(\"SIMULATOR_GUI_PAINT_OK\");\n"
                + "    Thread shutdown = new Thread(() -> {\n"
                + "      try { Thread.sleep(200); } catch (InterruptedException e) {\n"
                + "        Thread.currentThread().interrupt();\n"
                + "      }\n"
                + "      System.exit(0);\n"
                + "    }, \"simulator-smoke-shutdown\");\n"
                + "    shutdown.setDaemon(true);\n"
                + "    shutdown.start();\n"
                + "  }\n"
                + "}\n";
    }

    private static void runAndVerify(Path jar, Path tempDir) throws Exception {
        String log = StandaloneSdkJarTest.runWithJavaJar(jar, tempDir.resolve("simulator-gui.log"),
                false, "/scr", "320x480x32", "/scale", "1", CLASS_NAME);
        assertTrue(log.contains("SIMULATOR_GUI_INIT_OK"),
                "MainWindow.initUI() was never called:\n" + log);
        assertTrue(log.contains("SIMULATOR_GUI_PAINT_OK"),
                "no visible AWT frame and first paint were observed:\n" + log);
    }
}
