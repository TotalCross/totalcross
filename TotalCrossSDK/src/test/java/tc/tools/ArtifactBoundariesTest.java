// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("artifact-boundary")
class ArtifactBoundariesTest {
    private static final Path ARTIFACTS = Path.of(System.getProperty("totalcross.artifact.dir"));
    private static final Path DISTRIBUTED_SDK = Path.of(System.getProperty("totalcross.aggregate.sdk.jar"));

    @Test
    void apiDoesNotContainToolingOrPreviewImplementation() throws Exception {
        Set<String> entries = entries("totalcross-api");
        assertTrue(entries.stream().anyMatch(name -> name.startsWith("totalcross/ui/")));
        assertFalse(entries.stream().anyMatch(name -> name.startsWith("tc/tools/converter/")));
        assertFalse(entries.stream().anyMatch(name -> name.startsWith("tc/tools/deployer/")));
        assertFalse(entries.stream().anyMatch(name -> name.startsWith("totalcross/preview/")));
    }

    @Test
    void apiDoesNotExpose4DImplementationClasses() throws Exception {
        assertFalse(entries("totalcross-api").stream().anyMatch(name -> name.endsWith("4D.class")
                || name.contains("4D$")));
    }

    @Test
    void apiDoesNotExposeStandardStreamRuntimeBridge() throws Exception {
        assertFalse(entries("totalcross-api").contains("totalcross/sys/VmStandardOutputStream.class"));
        assertTrue(entries("totalcross-runtime-java").contains("totalcross/sys/VmStandardOutputStream.class"));
    }

    @Test
    void apiContainsRuntimeConfigurationContractsButNotStartupImplementation() throws Exception {
        Set<String> api = entries("totalcross-api");
        Set<String> runtimeJava = entries("totalcross-runtime-java");
        assertTrue(api.contains("totalcross/sys/Platform.class"));
        assertTrue(api.contains("totalcross/sys/RuntimeFamily.class"));
        assertTrue(api.contains("totalcross/sys/GraphicsBackend.class"));
        assertTrue(api.contains("totalcross/sys/Architecture.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeEnvironment.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeSelector.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeConfiguration.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeRule.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeRules.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeWhen.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeCondition.class"));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ResolvedRuntimeConfiguration.class"));
    }

    @Test
    void converterAndDeployerRemainSeparate() throws Exception {
        Set<String> converter = entries("totalcross-converter");
        Set<String> deployer = entries("totalcross-deployer");
        assertTrue(converter.stream().anyMatch(name -> name.startsWith("tc/tools/converter/")));
        assertFalse(converter.stream().anyMatch(name -> name.startsWith("tc/tools/deployer/")));
        assertTrue(deployer.contains("tc/Deploy.class"));
        assertTrue(deployer.stream().anyMatch(name -> name.startsWith("tc/tools/deployer/")));
        assertFalse(deployer.stream().anyMatch(name -> name.startsWith("tc/tools/converter/")));
    }

    @Test
    void simulatorArtifactContainsLauncherSimulatorAndPreviewContract() throws Exception {
        Set<String> simulator = entries("totalcross-simulator");
        assertTrue(simulator.contains("totalcross/Launcher.class"));
        assertTrue(simulator.contains("totalcross/TotalCrossApplication.class"));
        assertTrue(simulator.contains("tc/simulator/EventLoop.class"));
        assertTrue(simulator.stream().anyMatch(name -> name.startsWith("tc/simulator/")));
        assertTrue(simulator.stream().anyMatch(name -> name.startsWith("tc/preview/")));
        assertFalse(simulator.stream().anyMatch(name -> name.startsWith("totalcross/preview/")));
        assertFalse(simulator.stream().anyMatch(name -> name.startsWith("tc/tools/converter/")));
        assertFalse(simulator.stream().anyMatch(name -> name.startsWith("tc/tools/deployer/")));
        assertFalse(simulator.contains("totalcross/TCEventThread.class"));
    }

    @Test
    void runtimeJavaExcludesSimulatorApplicationEntryPoints() throws Exception {
        Set<String> runtimeJava = entries("totalcross-runtime-java");
        assertFalse(runtimeJava.contains("totalcross/Launcher.class"));
        assertFalse(runtimeJava.stream().anyMatch(name -> name.startsWith("totalcross/Launcher$")
                && name.endsWith(".class")));
        assertFalse(runtimeJava.contains("totalcross/TotalCrossApplication.class"));
    }

    @Test
    void aggregateSdkContainsTotalCrossApplication() throws Exception {
        assertTrue(entries("totalcross-sdk").contains("totalcross/TotalCrossApplication.class"));
    }

    @Test
    void aggregateSdkContainsApplicationRuntimeContractsButNotInternalPlumbing() throws Exception {
        Set<String> sdk = entries("totalcross-sdk");
        Set<String> distributedSdk = entries(DISTRIBUTED_SDK);
        Set<String> runtimeJava = entries("totalcross-runtime-java");
        assertTrue(sdk.contains("totalcross/sys/Platform.class"));
        assertTrue(sdk.contains("totalcross/sys/RuntimeFamily.class"));
        assertTrue(sdk.contains("totalcross/sys/GraphicsBackend.class"));
        assertTrue(sdk.contains("totalcross/sys/Architecture.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeEnvironment.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeSelector.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeConfiguration.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeRule.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeRules.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeWhen.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeCondition.class"));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeRuleResolver.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ResolvedRuntimeConfiguration.class"));
    }

    @Test
    void aggregateSdkCompileSurfaceAllowsContractsAndRejectsInternalPlumbing(@TempDir Path tempDir) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "artifact API surface test requires a JDK compiler");
        Path sdkJar = DISTRIBUTED_SDK;
        Path publicSource = tempDir.resolve("PublicRuntimeConfigurationSurface.java");
        Files.writeString(publicSource, ""
                + "import totalcross.sys.Platform;\n"
                + "import totalcross.sys.runtime.RuntimeConfiguration;\n"
                + "import totalcross.sys.runtime.RuntimeCondition;\n"
                + "import totalcross.sys.runtime.RuntimeEnvironment;\n"
                + "import totalcross.sys.runtime.RuntimeRule;\n"
                + "import totalcross.sys.runtime.RuntimeSelector;\n"
                + "import totalcross.sys.runtime.RuntimeWhen;\n"
                + "@RuntimeConfiguration\n"
                + "@RuntimeRule(when=@RuntimeWhen(allOf=@RuntimeCondition(platform=Platform.MACOS)))\n"
                + "public final class PublicRuntimeConfigurationSurface {\n"
                + "  RuntimeEnvironment environment = RuntimeEnvironment.current();\n"
                + "  RuntimeSelector selector = RuntimeSelector.platform(Platform.MACOS);\n"
                + "}\n");
        assertTrue(compile(compiler, sdkJar, publicSource, tempDir.resolve("public-classes")),
                "public runtime configuration contracts must compile against the aggregate SDK");

        Path internalSource = tempDir.resolve("InternalRuntimeConfigurationAccess.java");
        Files.writeString(internalSource, ""
                + "import totalcross.sys.runtime.RuntimeConfigurationMetadata;\n"
                + "import totalcross.sys.runtime.RuntimeConfigurationStartup;\n"
                + "public final class InternalRuntimeConfigurationAccess {\n"
                + "  Object decode(byte[] bytes) {\n"
                + "    RuntimeConfigurationStartup.initializeForSimulator(null);\n"
                + "    return RuntimeConfigurationMetadata.decode(bytes);\n"
                + "  }\n"
                + "}\n");
        assertFalse(compile(compiler, sdkJar, internalSource, tempDir.resolve("internal-classes")),
                "application code must not compile against startup or codec plumbing");
    }

    @Test
    void aggregateSdkManifestDoesNotExposeTheInternalRuntimeArtifactToCompilers() throws Exception {
        try (JarFile jar = new JarFile(DISTRIBUTED_SDK.toFile())) {
            String classPath = jar.getManifest().getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            assertFalse(classPath != null && classPath.contains("totalcross-runtime-java.jar"));
        }
        assertTrue(Files.isRegularFile(DISTRIBUTED_SDK.getParent().resolve("libs/totalcross-runtime-java.jar")),
                "the internal artifact remains available to explicit SDK runtime classpaths");
    }

    private static Set<String> entries(String prefix) throws IOException {
        return entries(artifact(prefix));
    }

    private static Set<String> entries(Path path) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            return jar.stream().map(entry -> entry.getName()).collect(Collectors.toSet());
        }
    }

    private static Path artifact(String prefix) throws IOException {
        try (java.util.stream.Stream<Path> artifacts = Files.list(ARTIFACTS)) {
            return artifacts.filter(path -> path.getFileName().toString().startsWith(prefix + "-"))
                    .filter(path -> !path.getFileName().toString().contains("-sources"))
                    .filter(path -> !path.getFileName().toString().contains("-javadoc"))
                    .sorted()
                    .findFirst().orElseThrow();
        }
    }

    private static boolean compile(JavaCompiler compiler, Path sdkJar, Path source, Path output) throws IOException {
        Files.createDirectories(output);
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int result = compiler.run(null, diagnostics, diagnostics, "-classpath", sdkJar.toString(), "-d",
                output.toString(), source.toString());
        return result == 0;
    }
}
