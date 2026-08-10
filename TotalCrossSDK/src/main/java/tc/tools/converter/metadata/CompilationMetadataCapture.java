// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaField;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.tclass.TCMethod;
import totalcross.util.Vector;

/** Conversion-scoped boundary for optional compilation-metadata capture. */
public interface CompilationMetadataCapture {
  CompilationMetadataCapture NONE = new DisabledCompilationMetadataCapture();

  boolean isEnabled();

  void captureClass(JavaClass source, String effectiveName);

  void captureField(JavaClass owner, JavaField field, int tcFieldSymbol);

  void captureMethodHeader(JavaMethod source, TCMethod target);

  SiteCapture beginBytecode(JavaClass owner, JavaMethod method, ByteCode bytecode);

  void endBytecode(SiteCapture site, Vector instructions, int firstInstruction);

  void finishMethod(JavaMethod source, TCMethod target);

  void recordResolvedClassForName(String className);

  void recordUnresolvedClassForName();

  CompilationMetadata snapshot();

  interface SiteCapture {
  }
}

final class DisabledCompilationMetadataCapture implements CompilationMetadataCapture {
  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void captureClass(JavaClass source, String effectiveName) {
  }

  @Override
  public void captureField(JavaClass owner, JavaField field, int tcFieldSymbol) {
  }

  @Override
  public void captureMethodHeader(JavaMethod source, TCMethod target) {
  }

  @Override
  public SiteCapture beginBytecode(JavaClass owner, JavaMethod method, ByteCode bytecode) {
    return null;
  }

  @Override
  public void endBytecode(SiteCapture site, Vector instructions, int firstInstruction) {
  }

  @Override
  public void finishMethod(JavaMethod source, TCMethod target) {
  }

  @Override
  public void recordResolvedClassForName(String className) {
  }

  @Override
  public void recordUnresolvedClassForName() {
  }

  @Override
  public CompilationMetadata snapshot() {
    return CompilationMetadata.empty();
  }
}
