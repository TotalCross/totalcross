// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("artifact-boundary")
class ArtifactBoundariesTest {
    private static final Path ARTIFACTS = Path.of(System.getProperty("totalcross.artifact.dir"));

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

    private static Set<String> entries(String prefix) throws IOException {
        Path artifact = Files.list(ARTIFACTS).filter(path -> path.getFileName().toString().startsWith(prefix + "-"))
                .findFirst().orElseThrow();
        try (JarFile jar = new JarFile(artifact.toFile())) {
            return jar.stream().map(entry -> entry.getName()).collect(Collectors.toSet());
        }
    }
}
