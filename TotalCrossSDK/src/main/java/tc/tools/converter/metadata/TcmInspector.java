// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class TcmInspector {
  private TcmInspector() {
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Usage: TcmInspector <file.tcm> <primary.tcz> [split.tcz ...]");
    }
    Path sidecar = Paths.get(args[0]);
    List<Path> artifacts = new ArrayList<Path>();
    for (int i = 1; i < args.length; i++) {
      artifacts.add(Paths.get(args[i]));
    }
    TcmFile file = new TcmReader().read(sidecar, artifacts);
    int fields = 0;
    int methods = 0;
    int calls = 0;
    int origins = 0;
    int frames = 0;
    for (CompilationMetadata.ClassMetadata type : file.metadata.classes) {
      fields += type.fields.size();
      methods += type.methods.size();
      for (CompilationMetadata.MethodMetadata method : type.methods) {
        calls += method.callSites.size();
        origins += method.origins.size();
        frames += method.verificationFrames.size();
      }
    }
    System.out.println("TCM v1 valid: artifacts=" + file.artifacts.size() + " classes=" + file.metadata.classes.size()
        + " fields=" + fields + " methods=" + methods + " calls=" + calls + " origins=" + origins + " frames="
        + frames + " build=" + file.buildIdentity);
  }
}
