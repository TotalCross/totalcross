// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import totalcross.ui.MainWindow;
import totalcross.ui.Window;

class LauncherNestedEventPumpTest {
  @Test
  void activeLauncherPumpsNestedEvents() throws Exception {
    Launcher activeLauncher = new Launcher();
    EventLoop eventLoop = new EventLoop(new MainWindow() {
    });

    activeLauncher.eventLoop = eventLoop;

    StringBuilder order = new StringBuilder();
    CountDownLatch completed = new CountDownLatch(1);

    try {
      eventLoop.post(() -> {
        order.append('A');

        // Blocking popups call this while already inside the event thread.
        Window.pumpEvents();

        order.append('C');
      });

      eventLoop.post(() -> order.append('B'));
      eventLoop.post(completed::countDown);

      assertTrue(
          completed.await(2, TimeUnit.SECONDS),
          "Nested event pumping did not complete");

      assertEquals(
          "ABC",
          order.toString(),
          "Window.pumpEvents() must process the next queued event immediately");
    } finally {
      eventLoop.shutdown();
    }
  }
}