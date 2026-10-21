// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the distributed SDK from the historical java -jar entry point in another JVM.
 * Test application classes are embedded in a temporary copy of the SDK, rather
 * than changing the runtime classpath or relying on Gradle's compiled outputs.
 */
@Tag("artifact-boundary")
class StandaloneSdkJarTest {
    static final Path DISTRIBUTED_SDK =
            Path.of(System.getProperty("totalcross.aggregate.sdk.jar"));
    private static final String PACKAGE_PATH = "tc/tools/standalone/";
    private static final List<String> FIXTURES =
            List.of("PlainStandaloneFixture", "AnnotatedStandaloneFixture");

    @Test
    void aggregateSdkRunsPlainAndAnnotatedApplicationsWithJavaJar(@TempDir Path tempDir) throws Exception {
        assertTrue(Files.isRegularFile(DISTRIBUTED_SDK), "the SDK distribution must exist");

        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        Path plainSource = writeFixture(tempDir, FIXTURES.get(0), false);
        Path annotatedSource = writeFixture(tempDir, FIXTURES.get(1), true);
        compileFixtures(DISTRIBUTED_SDK, classesDir, plainSource, annotatedSource);

        // Preserve the original manifest's relative dist/libs dependency locations.
        Path standaloneJar = Files.createTempFile(
                DISTRIBUTED_SDK.getParent(), "standalone-sdk-test-", ".jar");
        try {
            copySdkAndAddFixtures(standaloneJar, classesDir, FIXTURES);
            for (String fixture : FIXTURES) {
                String output = runWithJavaJar(standaloneJar, tempDir.resolve(fixture + ".log"),
                        true, PACKAGE_PATH.replace('/', '.') + fixture);
                assertTrue(output.contains("STANDALONE_SDK_OK:" + fixture),
                        "standalone simulator did not initialize " + fixture + ":\n" + output);
            }
        } finally {
            Files.deleteIfExists(standaloneJar);
        }
    }

    private static Path writeFixture(Path directory, String name, boolean annotated) throws IOException {
        String annotation = annotated
                ? "@totalcross.sys.runtime.RuntimeConfiguration\n"
                    + "@totalcross.sys.runtime.RuntimeRule(when = @totalcross.sys.runtime.RuntimeWhen("
                    + "allOf = {@totalcross.sys.runtime.RuntimeCondition("
                    + "family = totalcross.sys.RuntimeFamily.DESKTOP)}))\n"
                : "";
        String source = "package tc.tools.standalone;\n"
                + annotation
                + "public final class " + name + " implements totalcross.MainClass {\n"
                + "  public void appStarting(int timeAvail) {\n"
                + "    System.out.println(\"STANDALONE_SDK_OK:" + name + "\");\n"
                + "  }\n"
                + "  public void appEnding() {}\n"
                + "  public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp) {}\n"
                + "  public void _onTimerTick(boolean canUpdate) {}\n"
                + "}\n";
        Path path = directory.resolve(name + ".java");
        Files.writeString(path, source);
        return path;
    }

    static void copySdkAndAddFixtures(Path target, Path classesDir, List<String> fixtureNames) throws IOException {
        try (JarFile original = new JarFile(DISTRIBUTED_SDK.toFile())) {
            assertNotNull(original.getManifest(), "the SDK executable manifest is required");
            try (JarOutputStream output = new JarOutputStream(
                    Files.newOutputStream(target), original.getManifest())) {
                java.util.Enumeration<JarEntry> entries = original.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (JarFile.MANIFEST_NAME.equalsIgnoreCase(entry.getName())) {
                        continue;
                    }
                    output.putNextEntry(new JarEntry(entry.getName()));
                    if (!entry.isDirectory()) {
                        try (java.io.InputStream input = original.getInputStream(entry)) {
                            input.transferTo(output);
                        }
                    }
                    output.closeEntry();
                }
                for (String fixture : fixtureNames) {
                    String entryName = PACKAGE_PATH + fixture + ".class";
                    output.putNextEntry(new JarEntry(entryName));
                    Files.copy(classesDir.resolve(entryName), output);
                    output.closeEntry();
                }
            }
        }
    }

    static void compileFixtures(Path sdkJar, Path classesDir, Path... sources) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "a JDK is required to compile the regression fixtures");
        java.util.List<String> arguments = new java.util.ArrayList<>(List.of(
                "--release", "17", "-classpath", sdkJar.toString(),
                "-d", classesDir.toString()));
        for (Path source : sources) {
            arguments.add(source.toString());
        }
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int compilation = compiler.run(null, diagnostics, diagnostics,
                arguments.toArray(new String[0]));
        assertEquals(0, compilation, diagnostics.toString());
    }

    static String runWithJavaJar(Path sdkJar, Path log, boolean headless, String... applicationArgs)
            throws Exception {
        String javaName = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT)
                .contains("win") ? "java.exe" : "java";
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", javaName);
        java.util.List<String> command = new java.util.ArrayList<>(List.of(
                javaExecutable.toString(), "-Djava.awt.headless=" + headless,
                "-jar", sdkJar.toString()));
        command.addAll(java.util.Arrays.asList(applicationArgs));
        Process process = new ProcessBuilder(command)
                .directory(sdkJar.getParent().toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        try {
            boolean finished = process.waitFor(45, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor();
            }
            String output = Files.readString(log);
            assertTrue(finished, "standalone simulator timed out:\n" + output);
            assertEquals(0, process.exitValue(), "standalone simulator failed:\n" + output);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
