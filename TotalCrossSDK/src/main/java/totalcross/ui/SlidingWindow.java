// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.SpecialKeys;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.gfx.Rect;

/** A container with a top bar + return button supporting slide-in animations. */
public class SlidingWindow extends Container
    implements PenListener, KeyListener, PresentationController.Delegate {
  protected Presenter<Container> provider;
  protected ControlAnimation currentAnimation;
  protected int animDir;
  protected int slackSpace;
  protected int totalTime = 400;
  protected Spinner delayedUiSpinner;
  protected boolean delayInitUI;

  private final PresentationController presentationController;

  public SlidingWindow(Presenter<Container> provider) {
    this(false, provider);
  }

  public SlidingWindow(boolean delayInitUI, Presenter<Container> provider) {
    this.provider = provider;
    this.delayInitUI = delayInitUI;
    animDir = BOTTOM;
    presentationController = new PresentationController(this);
    addPenListener(this);
    addKeyListener(this);
    callListenersOnAllTargets = true;
  }

  public void popup() {
    presentationController.popup();
  }

  public void popupNonBlocking() {
    presentationController.popupNonBlocking();
  }

  public void unpop() {
    presentationController.unpop();
  }

  @Override
  public void initUI() {
    if (!delayInitUI) {
      add(provider.getView(), LEFT, TOP, FILL, FILL);
    } else {
      delayedUiSpinner = new Spinner();
      add(delayedUiSpinner, CENTER, CENTER);
      delayedUiSpinner.start();
    }
  }

  protected void postPopup() {
    if (delayInitUI) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          final Container view = provider.getView();
          MainWindow.mainWindowInstance.runOnMainThread(new Runnable() {
            @Override
            public void run() {
              addDelayedView(view);
              remove(delayedUiSpinner);
              delayedUiSpinner.stop();
            }
          });
        }
      }).start();
    }
  }

  protected void addDelayedView(Container view) {
    add(view, LEFT, TOP, FILL, FILL);
  }

  public void onPopup() {
  }

  protected void onUnpop() {
  }

  protected void postUnpop() {
  }

  public void screenResized() {
    presentationController.relayout();
  }

  /** Gets the slack space left by this presentation on pop-up. */
  public int getSlackSpace() {
    return slackSpace;
  }

  /** Sets the slack space left by this presentation on pop-up. */
  public void setSlackSpace(int slackSpace) {
    this.slackSpace = slackSpace;
  }

  @Override
  public PresentationEntry createPresentationEntry() {
    PresentationTransition transition = animDir == CENTER
        ? new FadePresentationTransition() : new SlidePresentationTransition(animDir);
    return new PresentationEntry(this, PresentationEntry.Layer.ROUTE,
        new PresentationEntry.BoundsResolver() {
          @Override
          public void resolve(Rect viewport, Rect bounds) {
            bounds.set(0, 0, viewport.width, viewport.height);
            switch (animDir) {
            case LEFT:
              bounds.x = -slackSpace;
              break;
            case RIGHT:
              bounds.x = slackSpace;
              break;
            case TOP:
              bounds.y = -slackSpace;
              break;
            case BOTTOM:
              bounds.y = slackSpace;
              break;
            default:
              break;
            }
          }
        }, transition, true, false, true, -1, totalTime);
  }

  @Override
  public void onPresentationPopup() {
    onPopup();
  }

  @Override
  public void postPresentationPopup() {
    postPopup();
  }

  @Override
  public void onPresentationUnpop() {
    onUnpop();
  }

  @Override
  public void postPresentationUnpop() {
    postUnpop();
  }

  PresentationHandle presentationHandle() {
    return presentationController.handle();
  }

  @Override
  public void penDrag(DragEvent event) {
    double margin = 0.20;
    if (animDir == RIGHT && event.direction == DragEvent.RIGHT && event.xTotal > 150
        && event.x - event.xTotal < width * margin) {
      unpop();
    } else if (animDir == BOTTOM && event.direction == DragEvent.DOWN && event.yTotal > 150
        && event.y - event.yTotal < height * margin) {
      unpop();
    } else if (animDir == LEFT && event.direction == DragEvent.LEFT && event.xTotal < -150
        && event.x - event.xTotal > width * (1 - margin)) {
      unpop();
    } else if (animDir == TOP && event.direction == DragEvent.UP && event.yTotal < -150
        && event.y - event.yTotal > height * (1 - margin)) {
      unpop();
    }
  }

  @Override
  public void specialkeyPressed(KeyEvent event) {
    if (event.key == SpecialKeys.ESCAPE) {
      unpop();
    }
  }

  @Override
  public void penDown(PenEvent event) {
  }

  @Override
  public void penUp(PenEvent event) {
  }

  @Override
  public void penDragStart(DragEvent event) {
  }

  @Override
  public void penDragEnd(DragEvent event) {
  }

  @Override
  public void keyPressed(KeyEvent event) {
  }

  @Override
  public void actionkeyPressed(KeyEvent event) {
  }
}
