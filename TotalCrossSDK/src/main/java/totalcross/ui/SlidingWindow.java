package totalcross.ui;

import totalcross.sys.SpecialKeys;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;

/** A window with a top bar + return button supporting slide-in animations. */
public class SlidingWindow extends Window implements PenListener, KeyListener {
	protected Presenter<Container> provider;
	protected ControlAnimation currentAnimation;
	protected int animDir;
	protected int slackSpace;
	protected int totalTime = 400;
	protected Spinner delayedUiSpinner;

	protected boolean delayInitUI;

	public SlidingWindow(Presenter<Container> provider) {
		this(false, provider);
	}

	public SlidingWindow(boolean delayInitUI, Presenter<Container> provider) {
		super(null, Window.NO_BORDER);
		this.provider = provider;
		this.delayInitUI = delayInitUI;
		fadeOtherWindows = false;
		animDir = BOTTOM;
		slackSpace = 0;

		this.addPenListener(this);
		this.addKeyListener(this);
		this.callListenersOnAllTargets = true;
	}

	protected void setRect(boolean screenResized) {
		switch (animDir) {
			case LEFT:
			case RIGHT:
				setRect(animDir, TOP, SCREENSIZE, SCREENSIZE, null, screenResized);
				break;
			default:
				setRect(100000, 100000, SCREENSIZE, SCREENSIZE, null, screenResized);
				break;
		}
	}

	@Override
	public void unpop() {
		if (currentAnimation != null) {
			return;
		}

		if (animDir == CENTER) {
			currentAnimation = FadeAnimation.create(this, false, null, totalTime);
		} else {
			currentAnimation = PathAnimation.create(this, -animDir, null, totalTime, slackSpace);
		}
		currentAnimation.setAnimationFinishedAction(new AnimationFinished() {
			@Override
			public void onAnimationFinished(ControlAnimation anim) {
				currentAnimation = null;
				SlidingWindow.super.unpop();
			}
		});
		currentAnimation.start();
	}

	@Override
	public void popup() {
		setRect(false);
		super.popup();
	}

	@Override
	public void initUI() {
		if (!delayInitUI) {
			Container c = provider.getView();
			add(c, LEFT, TOP, FILL, FILL, this);
		} else {
			delayedUiSpinner = new Spinner();
			add(delayedUiSpinner, CENTER, CENTER);
			delayedUiSpinner.start();
		}
	}

	@Override
	protected void postPopup() {
		if (delayInitUI) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Container view = provider.getView();
					MainWindow.mainWindowInstance.runOnMainThread(new Runnable() {
						@Override
						public void run() {
							add(view, LEFT, AFTER, FILL, FILL, SlidingWindow.this);
							remove(delayedUiSpinner);
							delayedUiSpinner.stop();
						}
					});
				}
			}).start();
		}
	}

	@Override
	public void onPopup() {
		if (currentAnimation != null) {
			return;
		}

		screenResized(); // fix problem when the container is on portrait, then landscape, then closed,
		// then portrait, then open
		if (animDir == CENTER) {
			resetSetPositions();
			setRect(CENTER, CENTER, KEEP, KEEP);
			currentAnimation = FadeAnimation.create(this, true, null, totalTime);
		} else {
			currentAnimation = PathAnimation.create(this, animDir, null, totalTime, slackSpace);
		}
		currentAnimation.setAnimationFinishedAction(new AnimationFinished() {
			@Override
			public void onAnimationFinished(ControlAnimation anim) {
				currentAnimation = null;
			}
		});
		currentAnimation.start();
	}

	@Override
	public void screenResized() {
		/* needed to void the original 'screenResized' implementation */
		reposition();
	}

	/** Gets the slack space left by this window on pop-up */
	public int getSlackSpace() {
		return slackSpace;
	}

	/** Sets the slack space left by this window on pop-up */
	public void setSlackSpace(int slackSpace) {
		this.slackSpace = slackSpace;
	}

	@Override
	public void penDrag(DragEvent e) {
		double margin = 0.20;
		if (animDir == RIGHT && e.direction == DragEvent.RIGHT && e.xTotal > 150
			&& (e.x - e.xTotal) < width * (margin)) {
			SlidingWindow.this.unpop();
		}
		if (animDir == BOTTOM && e.direction == DragEvent.DOWN && e.yTotal > 150
			&& (e.y - e.yTotal) < height * (margin)) {
			SlidingWindow.this.unpop();
		}
		if (animDir == LEFT && e.direction == DragEvent.LEFT && e.xTotal < -150
			&& (e.x - e.xTotal) > width * (1 - margin)) {
			SlidingWindow.this.unpop();
		}
		if (animDir == TOP && e.direction == DragEvent.UP && e.yTotal < -150
			&& (e.y - e.yTotal) > height * (1 - margin)) {
			SlidingWindow.this.unpop();
		}
	}

	@Override
	public void penDown(PenEvent e) { }
	@Override
	public void penUp(PenEvent e) { }
	@Override
	public void penDragStart(DragEvent e) { }
	@Override
	public void penDragEnd(DragEvent e) { }

	@Override
	public void specialkeyPressed(KeyEvent e) {
		if (e.key == SpecialKeys.ESCAPE) {
			SlidingWindow.this.unpop();
		}
	}
	@Override
	public void keyPressed(KeyEvent e) { }
	@Override
	public void actionkeyPressed(KeyEvent e) { }
}
