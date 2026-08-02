// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator.awt;

/** Small AWT-side value adapter kept separate from simulator event dispatch. */
final class AwtInputBridge {
  private AwtInputBridge() {
  }

  static int button(int awtButton) {
    return awtButton;
  }
}
