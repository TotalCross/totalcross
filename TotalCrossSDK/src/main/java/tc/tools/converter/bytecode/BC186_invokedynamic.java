// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.bytecode;

public class BC186_invokedynamic extends MethodCall {
   public BC186_invokedynamic() {
     super(readUInt16(pc + 1));
     pcInc = 5;
   }
 
   @Override
   public void exec() {
   }
 }
