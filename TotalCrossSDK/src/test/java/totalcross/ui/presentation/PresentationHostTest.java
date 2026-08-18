// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.SafeAreaMode;
import totalcross.ui.Window;
import totalcross.ui.gfx.Rect;

class PresentationHostTest {
  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @Test
  void deferredHostAttachesLayoutsAndDismissesWithoutWindowCoupling() {
    Window owner = new Window();
    owner.setSafeAreaMode(SafeAreaMode.DISABLED);
    owner.setRect(0, 0, 320, 640);
    Container content = new Container();
    SlidePresentationTransition transition = new SlidePresentationTransition(Control.BOTTOM);
    PresentationEntry entry = new PresentationEntry(content, PresentationEntry.Layer.ROUTE,
        PresentationEntry.fillViewport(), transition, true, false, true, 0, 0, 0);
    PresentationHost host = new PresentationHost(owner);

    PresentationHandle handle = host.present(entry);

    assertSame(owner, host.getParent());
    assertEquals(new Rect(0, 0, 320, 640), handle.viewport.getRect());
    assertEquals(new Rect(0, 0, 320, 640), handle.frame.getRect());
    assertSame(content, handle.frame.getChildren()[0]);
    assertEquals(1, host.activeCount());

    owner.setRect(0, 0, 640, 320);
    host.ownerLayoutChanged();
    assertEquals(new Rect(0, 0, 640, 320), handle.viewport.getRect());
    assertEquals(new Rect(0, 0, 640, 320), handle.frame.getRect());

    handle.dismiss();
    assertEquals(PresentationHandle.State.DISMISSED, handle.state());
    assertEquals(0, host.activeCount());
    assertNull(host.getParent());
  }
}
