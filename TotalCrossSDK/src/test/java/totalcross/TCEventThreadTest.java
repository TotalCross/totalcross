// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TCEventThreadTest {
  @Test
  void invokeInEventThreadNotifiesWaitingCallerWhenRunnableFails() {
    TCEventThread eventThread = new TCEventThread(new NoopMainClass());
    try {
      boolean completed = eventThread.invokeInEventThread(true, new Runnable() {
        @Override
        public void run() {
          throw new RuntimeException("boom");
        }
      }, 5000);

      assertTrue(completed);
    } finally {
      eventThread.stopGracefully();
    }
  }

  private static class NoopMainClass implements MainClass {
    @Override
    public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp) {
    }

    @Override
    public void appStarting(int timeAvail) {
    }

    @Override
    public void appEnding() {
    }

    @Override
    public void _onTimerTick(boolean canUpdate) {
    }
  }
}
