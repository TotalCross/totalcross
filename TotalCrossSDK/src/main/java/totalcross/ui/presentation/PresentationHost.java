// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.presentation;

import totalcross.ui.Container;
import totalcross.ui.SafeAreaLayout;
import totalcross.ui.Window;

import totalcross.sys.SpecialKeys;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.gfx.Rect;
import totalcross.util.Vector;

final class PresentationHost extends Container implements KeyListener {
  private final Window owner;
  private final Container routeLayer = new Container();
  private final Container overlayLayer = new Container();
  private final Vector handles = new Vector();
  private boolean layersAttached;

  PresentationHost(Window owner) {
    this.owner = owner;
    setSafeAreaLayout(SafeAreaLayout.FULL_BLEED);
    routeLayer.setVisible(false);
    overlayLayer.setVisible(false);
    addKeyListener(this);
    callListenersOnAllTargets = true;
  }

  PresentationHandle present(PresentationEntry entry) {
    ensureAttached();
    Container layer = entry.layer == PresentationEntry.Layer.ROUTE ? routeLayer : overlayLayer;
    layer.setVisible(true);
    PresentationHandle handle = new PresentationHandle(this, entry);
    layer.add(handle.entryHost, LEFT, TOP, FILL, FILL);
    handles.addElement(handle);
    bringToFront();
    handle.attach(safeBounds());
    return handle;
  }

  private void ensureAttached() {
    if (getParent() == null) {
      owner.add(this, LEFT, TOP, FILL, FILL);
    }
    if (!layersAttached) {
      add(routeLayer, LEFT, TOP, FILL, FILL);
      add(overlayLayer, LEFT, TOP, FILL, FILL);
      layersAttached = true;
    }
    bringToFront();
  }

  void ownerLayoutChanged() {
    if (getParent() == null) {
      return;
    }
    routeLayer.reposition();
    overlayLayer.reposition();
    Rect safe = safeBounds();
    Object[] items = handles.items;
    for (int i = 0, count = handles.size(); i < count; i++) {
      ((PresentationHandle) items[i]).layout(safe);
    }
  }

  private Rect safeBounds() {
    Rect safe = owner.getClientRect();
    safe.x -= x;
    safe.y -= y;
    return safe;
  }

  void remove(PresentationHandle handle) {
    Container layer = handle.entry.layer == PresentationEntry.Layer.ROUTE ? routeLayer : overlayLayer;
    layer.remove(handle.entryHost);
    handles.removeElement(handle);
    routeLayer.setVisible(hasLayer(PresentationEntry.Layer.ROUTE));
    overlayLayer.setVisible(hasLayer(PresentationEntry.Layer.OVERLAY));
    if (handles.size() == 0 && getParent() == owner) {
      owner.remove(this);
    }
  }

  private boolean hasLayer(PresentationEntry.Layer layer) {
    Object[] items = handles.items;
    for (int i = 0, count = handles.size(); i < count; i++) {
      if (((PresentationHandle) items[i]).entry.layer == layer) {
        return true;
      }
    }
    return false;
  }

  int activeCount() {
    return handles.size();
  }

  PresentationHandle topHandle() {
    int count = handles.size();
    return count == 0 ? null : (PresentationHandle) handles.items[count - 1];
  }

  private void dismissTopOnBack(KeyEvent event) {
    for (int i = handles.size(); --i >= 0;) {
      PresentationHandle handle = (PresentationHandle) handles.items[i];
      if (handle.entry.dismissOnBack) {
        handle.dismiss();
        event.consumed = true;
        return;
      }
    }
  }

  @Override
  public void keyPressed(KeyEvent event) {
    if (event.key == SpecialKeys.ESCAPE) {
      dismissTopOnBack(event);
    }
  }

  @Override
  public void actionkeyPressed(KeyEvent event) {
  }

  @Override
  public void specialkeyPressed(KeyEvent event) {
    if (event.key == SpecialKeys.ESCAPE) {
      dismissTopOnBack(event);
    }
  }
}
