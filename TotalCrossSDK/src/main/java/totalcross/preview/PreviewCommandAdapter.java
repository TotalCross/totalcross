// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

/**
 * Runtime-neutral command boundary for a future IDE/process protocol.
 * Implementations may bind the handler to the in-process launcher or to a
 * worker transport without exposing AWT or TotalCross UI objects.
 */
public final class PreviewCommandAdapter implements AutoCloseable {
  public interface Handler {
    void start(String mainWindowClass, String[] args);

    void pump();

    void resize(int width, int height, double density);

    void pointer(int x, int y, int button, boolean pressed);

    void key(int keyCode, boolean pressed, int modifiers);

    void prepareReload();

    void replaceMainWindow(String mainWindowClass, String[] args);

    void close();
  }

  public interface LifecycleListener {
    void onLifecycleEvent(PreviewLifecycleEvent event);
  }

  private final Handler handler;
  private final LifecycleListener lifecycle;

  public PreviewCommandAdapter(Handler handler, LifecycleListener lifecycle) {
    if (handler == null) {
      throw new IllegalArgumentException("handler cannot be null");
    }
    this.handler = handler;
    this.lifecycle = lifecycle;
  }

  public void start(String mainWindowClass, String... args) {
    emit(PreviewLifecycleEvent.of(PreviewLifecycleEvent.Kind.START, mainWindowClass));
    try {
      handler.start(mainWindowClass, args == null ? new String[0] : args.clone());
      emit(PreviewLifecycleEvent.of(PreviewLifecycleEvent.Kind.READY, mainWindowClass));
    } catch (RuntimeException e) {
      emit(PreviewLifecycleEvent.error(e));
      throw e;
    }
  }

  public void pump() {
    handler.pump();
  }

  public void resize(int width, int height, double density) {
    handler.resize(width, height, density);
  }

  public void pointer(int x, int y, int button, boolean pressed) {
    handler.pointer(x, y, button, pressed);
  }

  public void key(int keyCode, boolean pressed, int modifiers) {
    handler.key(keyCode, pressed, modifiers);
  }

  public void prepareReload() {
    handler.prepareReload();
    emit(PreviewLifecycleEvent.of(PreviewLifecycleEvent.Kind.RELOAD_READY, null));
  }

  public void replaceMainWindow(String mainWindowClass, String... args) {
    handler.replaceMainWindow(mainWindowClass, args == null ? new String[0] : args.clone());
  }

  public void framePresented(PreviewFrame frame) {
    if (frame != null) {
      emit(PreviewLifecycleEvent.of(PreviewLifecycleEvent.Kind.FRAME, null));
    }
  }

  @Override
  public void close() {
    handler.close();
    emit(PreviewLifecycleEvent.of(PreviewLifecycleEvent.Kind.CLOSED, null));
  }

  private void emit(PreviewLifecycleEvent event) {
    if (lifecycle != null) {
      lifecycle.onLifecycleEvent(event);
    }
  }
}
