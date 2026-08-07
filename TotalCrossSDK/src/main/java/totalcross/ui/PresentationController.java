// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

final class PresentationController {
  interface Delegate {
    PresentationEntry createPresentationEntry();

    void onPresentationPopup();

    void postPresentationPopup();

    void onPresentationUnpop();

    void postPresentationUnpop();
  }

  private final Delegate delegate;
  private PresentationHandle handle;

  PresentationController(Delegate delegate) {
    this.delegate = delegate;
  }

  void popupNonBlocking() {
    if (handle != null && handle.isActive()) {
      return;
    }
    Window owner = Window.getTopMost();
    if (owner == null) {
      owner = MainWindow.getMainWindow();
    }
    if (owner == null) {
      throw new IllegalStateException("presentation requires an active Window");
    }
    delegate.onPresentationPopup();
    handle = owner.presentationHost().present(delegate.createPresentationEntry());
    delegate.postPresentationPopup();
  }

  void popup() {
    popupNonBlocking();
    while (handle != null && handle.isActive() && !MainWindow.quittingApp) {
      Window.pumpEvents();
    }
  }

  void unpop() {
    if (handle == null || !handle.isActive()) {
      return;
    }
    delegate.onPresentationUnpop();
    handle.setDismissedAction(new Runnable() {
      @Override
      public void run() {
        delegate.postPresentationUnpop();
      }
    });
    handle.dismiss();
  }

  PresentationHandle handle() {
    return handle;
  }
}
