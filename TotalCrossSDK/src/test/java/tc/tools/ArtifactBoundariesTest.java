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
    void previewArtifactContainsOnlyPreviewSurface() throws Exception {
        Set<String> preview = entries("totalcross-preview-runtime");
        assertTrue(preview.stream().anyMatch(name -> name.startsWith("totalcross/preview/")));
        assertFalse(preview.stream().anyMatch(name -> name.startsWith("tc/tools/converter/")));
        assertFalse(preview.stream().anyMatch(name -> name.startsWith("tc/tools/deployer/")));
    }

    private static Set<String> entries(String prefix) throws IOException {
        Path artifact = Files.list(ARTIFACTS).filter(path -> path.getFileName().toString().startsWith(prefix + "-"))
                .findFirst().orElseThrow();
        try (JarFile jar = new JarFile(artifact.toFile())) {
            return jar.stream().map(entry -> entry.getName()).collect(Collectors.toSet());
        }
    }
}
