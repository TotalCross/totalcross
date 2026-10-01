// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    void rasterAndDiagnosticsFeatureBridgesStayInTheInternalRuntimeArtifact() throws Exception {
        Set<String> api = entries("totalcross-api");
        Set<String> sdk = entries("totalcross-sdk");
        Set<String> distributedSdk = entries(DISTRIBUTED_SDK);
        Set<String> runtimeJava = entries("totalcross-runtime-java");
        String diagnosticsBridge = "totalcross/sys/RuntimeDiagnosticsFeatureBridge";
        String rasterBridge = "totalcross/ui/image/ImageRasterFeatureBridge";
        String compactStorageBridge = "totalcross/ui/image/ImageCompactStorageCapabilityBridge";

        assertTrue(runtimeJava.contains(diagnosticsBridge + ".class"));
        assertTrue(runtimeJava.contains(rasterBridge + ".class"));
        assertTrue(runtimeJava.contains(compactStorageBridge + ".class"));
        for (Set<String> applicationArtifact : java.util.List.of(api, sdk, distributedSdk)) {
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(diagnosticsBridge)));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(rasterBridge)));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(compactStorageBridge)));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(
                    "totalcross/ui/DisplayPreparation")));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(
                    "totalcross/ui/image/ImagePreparation")));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(
                    "totalcross/ui/image/PreparedImageResult")));
            assertFalse(applicationArtifact.stream().anyMatch(name -> name.startsWith(
                    "totalcross/sys/RuntimeDiagnosticsImageBridge")));
        }
        assertFalse(runtimeJava.stream().anyMatch(name -> name.startsWith(
                "totalcross/sys/RuntimeDiagnosticsImageBridge")));
        assertFalse(Files.exists(Path.of("src/main/java/totalcross/sys/RuntimeDiagnosticsImageBridge.java")));
    }

    @Test
    void imageDrawingBridgeRetainsItsPreP2PublicSurface() {
        Set<String> publicMethods = java.util.Arrays.stream(totalcross.ui.image.ImageDrawingBridge.class
                .getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers()))
                .map(ArtifactBoundariesTest::methodSignature)
                .collect(Collectors.toSet());

        assertTrue(publicMethods.equals(Set.of(
                "resolveForDrawing(totalcross.ui.image.Image,double)",
                "drawPlanForDrawing(totalcross.ui.image.Image,double)")));
    }

    @Test
    void scrollContainerExposesOnlyTheExplicitPreparationOperation() {
        Set<String> publicPreparationMethods = java.util.Arrays.stream(
                totalcross.ui.ScrollContainer.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getName().toLowerCase(java.util.Locale.ROOT).contains("prepar"))
                .map(ArtifactBoundariesTest::methodSignature)
                .collect(Collectors.toSet());
        assertTrue(publicPreparationMethods.equals(Set.of("prepareForDisplay(java.lang.Runnable)")));
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
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeConfigurationReport.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeRule.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeRules.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeWhen.class"));
        assertTrue(api.contains("totalcross/sys/runtime/RuntimeCondition.class"));
        assertTrue(api.contains("totalcross/ui/image/ImageStorageProfile.class"));
        assertTrue(api.contains("totalcross/ui/image/ImageRuntimeRule.class"));
        assertTrue(api.contains("totalcross/ui/image/ImageRuntimeRules.class"));
        assertFalse(api.stream().anyMatch(name -> name.startsWith(
                "totalcross/ui/image/ImageCompactStorageCapabilityBridge")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationFeatureBridge")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationMetadata")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationStartup")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimePolicy")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(api.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationFeatureBridge.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimePolicy.class"));
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
        assertTrue(sdk.contains("totalcross/sys/RuntimeDiagnostics.class"));
        assertTrue(sdk.contains("totalcross/sys/RuntimeDiagnosticSnapshot.class"));
        assertTrue(sdk.contains("totalcross/ui/image/Image.class"));
        assertTrue(sdk.contains("totalcross/ui/gfx/Graphics.class"));
        assertTrue(sdk.contains("totalcross/sys/RuntimeFamily.class"));
        assertTrue(sdk.contains("totalcross/sys/GraphicsBackend.class"));
        assertTrue(sdk.contains("totalcross/sys/Architecture.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeEnvironment.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeSelector.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeConfiguration.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeConfigurationReport.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeRule.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeRules.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeWhen.class"));
        assertTrue(sdk.contains("totalcross/sys/runtime/RuntimeCondition.class"));
        assertTrue(sdk.contains("totalcross/ui/image/ImageStorageProfile.class"));
        assertTrue(sdk.contains("totalcross/ui/image/ImageRuntimeRule.class"));
        assertTrue(sdk.contains("totalcross/ui/image/ImageRuntimeRules.class"));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationFeatureBridge")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationMetadata")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationStartup")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimePolicy")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(sdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationMetadata")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationStartup")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeConfigurationFeatureBridge")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith(
                "totalcross/ui/image/ImageCompactStorageCapabilityBridge")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationMetadata")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimeConfigurationStartup")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ImageRuntimePolicy")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/RuntimeRuleResolver")));
        assertFalse(distributedSdk.stream().anyMatch(name -> name.startsWith("totalcross/sys/runtime/ResolvedRuntimeConfiguration")));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/RuntimeConfigurationFeatureBridge.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimeConfigurationMetadata.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimeConfigurationStartup.class"));
        assertTrue(runtimeJava.contains("totalcross/sys/runtime/ImageRuntimePolicy.class"));
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
                + "import totalcross.sys.RuntimeDiagnostics;\n"
                + "import totalcross.sys.RuntimeDiagnosticSnapshot;\n"
                + "import totalcross.sys.runtime.RuntimeConfiguration;\n"
                + "import totalcross.sys.runtime.RuntimeCondition;\n"
                + "import totalcross.sys.runtime.RuntimeEnvironment;\n"
                + "import totalcross.sys.runtime.RuntimeRule;\n"
                + "import totalcross.sys.runtime.RuntimeSelector;\n"
                + "import totalcross.sys.runtime.RuntimeWhen;\n"
                + "import totalcross.sys.runtime.RuntimeConfigurationReport;\n"
                + "import totalcross.ui.image.ImageRuntimeRule;\n"
                + "import totalcross.ui.image.ImageStorageProfile;\n"
                + "import totalcross.ui.image.Image;\n"
                + "import totalcross.ui.gfx.Graphics;\n"
                + "@RuntimeConfiguration\n"
                + "@RuntimeRule(when=@RuntimeWhen(allOf=@RuntimeCondition(platform=Platform.MACOS)))\n"
                + "@ImageRuntimeRule(when=@RuntimeWhen(allOf=@RuntimeCondition(platform=Platform.MACOS)), "
                + "storage=ImageStorageProfile.COMPACT)\n"
                + "public final class PublicRuntimeConfigurationSurface {\n"
                + "  RuntimeEnvironment environment = RuntimeEnvironment.current();\n"
                + "  RuntimeSelector selector = RuntimeSelector.platform(Platform.MACOS);\n"
                + "  Image image;\n"
                + "  Graphics graphics;\n"
                + "  RuntimeDiagnosticSnapshot diagnostics() {\n"
                + "    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, true);\n"
                + "    return RuntimeDiagnostics.snapshot();\n"
                + "  }\n"
                + "  String describe() { return RuntimeConfigurationReport.describe(); }\n"
                + "}\n");
        assertTrue(compile(compiler, sdkJar, publicSource, tempDir.resolve("public-classes")),
                "public runtime configuration contracts must compile against the aggregate SDK");

        Path internalSource = tempDir.resolve("InternalRuntimeConfigurationAccess.java");
        Files.writeString(internalSource, ""
                + "import totalcross.sys.RuntimeDiagnosticsFeatureBridge;\n"
                + "import totalcross.sys.runtime.RuntimeConfigurationMetadata;\n"
                + "import totalcross.sys.runtime.RuntimeConfigurationFeatureBridge;\n"
                + "import totalcross.sys.runtime.RuntimeConfigurationStartup;\n"
                + "import totalcross.sys.runtime.ImageRuntimeConfigurationMetadata;\n"
                + "import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;\n"
                + "import totalcross.sys.runtime.ImageRuntimePolicy;\n"
                + "import totalcross.ui.image.ImageRasterFeatureBridge;\n"
                + "public final class InternalRuntimeConfigurationAccess {\n"
                + "  Object decode(byte[] bytes) {\n"
                + "    RuntimeDiagnosticsFeatureBridge.recordCounter(null, 0);\n"
                + "    ImageRasterFeatureBridge.recordRasterFallback();\n"
                + "    RuntimeConfigurationStartup.initializeForSimulator(null);\n"
                + "    ImageRuntimeConfigurationStartup.initializeAtStartup();\n"
                + "    ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();\n"
                + "    RuntimeConfigurationFeatureBridge.appendDescriptionSections(new StringBuilder());\n"
                + "    return RuntimeConfigurationMetadata.decode(bytes);\n"
                + "  }\n"
                + "}\n");
        assertFalse(compile(compiler, sdkJar, internalSource, tempDir.resolve("internal-classes")),
                "application code must not compile against startup or codec plumbing");

        Path imagePreparationSource = tempDir.resolve("InternalImagePreparationAccess.java");
        Files.writeString(imagePreparationSource, ""
                + "import totalcross.ui.image.ImagePreparationFeatureBridge;\n"
                + "import totalcross.ui.image.ImagePreparationRequest;\n"
                + "import totalcross.ui.image.ImagePreparationScheduler;\n"
                + "import totalcross.ui.image.PreparedImageResult;\n"
                + "public final class InternalImagePreparationAccess {\n"
                + "  void prepare() { ImagePreparationFeatureBridge.prepareForDisplay(null, 1.0, 0L, null); }\n"
                + "}\n");
        assertFalse(compile(compiler, sdkJar, imagePreparationSource,
                tempDir.resolve("image-preparation-internal-classes")),
                "application code must not compile against Q preparation internals");
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

    private static String methodSignature(Method method) {
        return method.getName() + "(" + java.util.Arrays.stream(method.getParameterTypes())
                .map(Class::getName).collect(Collectors.joining(",")) + ")";
    }
}
