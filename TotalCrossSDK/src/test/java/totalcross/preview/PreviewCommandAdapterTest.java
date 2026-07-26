// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PreviewCommandAdapterTest {
  @Test
  void forwardsCommandsAndReportsStructuredLifecycle() {
    RecordingHandler handler = new RecordingHandler();
    List<PreviewLifecycleEvent.Kind> events = new ArrayList<>();
    PreviewCommandAdapter adapter = new PreviewCommandAdapter(handler,
        event -> events.add(event.getKind()));

    adapter.start("example.Main", "one");
    adapter.resize(320, 480, 2);
    adapter.pointer(4, 5, 1, true);
    adapter.key(65, true, 2);
    adapter.prepareReload();
    adapter.replaceMainWindow("example.Other", "two");
    adapter.pump();
    adapter.close();

    assertEquals(List.of("start", "resize", "pointer", "key", "prepare", "replace", "pump", "close"),
        handler.calls);
    assertEquals(List.of(PreviewLifecycleEvent.Kind.START, PreviewLifecycleEvent.Kind.READY,
        PreviewLifecycleEvent.Kind.RELOAD_READY, PreviewLifecycleEvent.Kind.CLOSED), events);
    assertArrayEquals(new String[] { "one" }, handler.startArgs);
  }

  private static final class RecordingHandler implements PreviewCommandAdapter.Handler {
    final List<String> calls = new ArrayList<>();
    String[] startArgs;

    public void start(String mainWindowClass, String[] args) { calls.add("start"); startArgs = args; }
    public void pump() { calls.add("pump"); }
    public void resize(int width, int height, double density) { calls.add("resize"); }
    public void pointer(int x, int y, int button, boolean pressed) { calls.add("pointer"); }
    public void key(int keyCode, boolean pressed, int modifiers) { calls.add("key"); }
    public void prepareReload() { calls.add("prepare"); }
    public void replaceMainWindow(String mainWindowClass, String[] args) { calls.add("replace"); }
    public void close() { calls.add("close"); }
  }
}
