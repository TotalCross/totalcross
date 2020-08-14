package totalcross.ui;

import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * This is a top menu like those on Android. It opens and closes using animation
 * and fading effects.
 *
 * @since TotalCross 3.03
 */
public class TopMenu extends Window {
	public static interface AnimationListener {
		public void onAnimationFinished();
	}

	/** The percentage of the area used for the icon and the caption */
	public static int percIcon = 20, percCap = 80;
	protected Control[] items;
	private int animDir;
	protected int selected = -1;
	/**
	 * Set to false to disable the close when pressing in a button of the menu.
	 */
	public boolean autoClose = true;
	/** Defines the animation delay */
	public int totalTime = 800;
	/**
	 * The percentage of the screen that this TopMenu will take: LEFT/RIGHT will
	 * take 50% of the screen's width, other directions will take 80% of the
	 * screen's width. Must be ser before calling <code>popup()</code>.
	 */
	public int percWidth;
	private AnimationListener alist;
	/** The width in pixels instead of percentage of screen's width. */
	public int widthInPixels;

	/** An optional image to be used as background */
	public Image backImage;
	/** The alpha value to be applied to the background image */
	public int backImageAlpha = 128;

	/** Insets used to place the ScrollContainer. */
	public Insets scInsets = new Insets(0, 0, 0, 0);

	public boolean drawSeparators = true;

	public int separatorColor = -1;

	public boolean showElevation = false;

	private ControlAnimation currentAnimation;
	private boolean fadeOnPopAndUnpop;

	public static class Item extends Container {
		Control tit;
		Object icon; // Image or Control
		public boolean highlight;

		/**
		 * Used when you want to fully customize your Item by extending this class.
		 */
		protected Item() {
			setBackForeColors(UIColors.topmenuBack, UIColors.topmenuFore);
			this.highlight = true;
		}

		/** Pass a Control and optionally an icon */
		public Item(Control c, Image icon) {
			this();
			this.tit = c;
			this.icon = icon;
			this.highlight = true;
		}

		/** Pass a Control and optionally another control that serves as an icon */
		public Item(Control c, Control icon) {
			this();
			this.tit = c;
			this.icon = icon;
			this.highlight = true;
		}

		/** Creates a Label and optionally an icon */
		public Item(String cap, Image icon) {
			this(new Label((String) cap, LEFT), icon);
		}

		public Item(String cap, Control icon) {
			this(new Label((String) cap, LEFT), icon);
		}

		private TopMenu getTopMenu() {
			return (TopMenu) getParentWindow();
		}

		@Override
		public void initUI() {
			if (tit instanceof Label) {
				Label l = (Label) tit;
				l.setAlpha(222);
			}

			int itemH = fmH + Edit.prefH;
			int perc = percCap;
			Control c = null;
			if (icon == null) {
				perc = 100;
			} else {
				if (icon instanceof Control) {
					c = (Control) icon;
				} else {
					try {
						c = new ImageControl(((Image)icon).getHwScaledInstance(itemH, itemH));
						((ImageControl) c).centerImage = true;
					} catch (ImageException e) {
					}
				}
				add(c == null ? (Control) new Spacer(itemH, itemH) : c, LEFT, TOP, PARENTSIZE + percIcon, FILL);
			}
			add(new Container() {
				@Override
				public void initUI() {
					/*
					 * 72dp left spacing and 16 dp right spacing
					 * https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
					 */
					this.setInsets((int)((Settings.screenWidth < 320 ? 64 : 72) * Settings.screenDensity),
								   (int)((Settings.screenWidth < 320 ? 8 : 16) * Settings.screenDensity), 0, 0);
					add(tit, LEFT, CENTER, FILL, PREFERRED);
				}
			}, LEFT, CENTER, FILL, PREFERRED);
		}

		@Override
		public void onEvent(Event e) {
			if (e.type == PenEvent.PEN_UP && !hadParentScrolled()) {
				// Should not accept clicks if the TopMenu animation is playing
				if (getTopMenu().currentAnimation != null) {
					return;
				}

				if (highlight) {
					int original = backColor;
					int highlight = Color.getBrightness(original) < 200 ? Color.brighter(original) : Color.darker(
										original);
					setBackColors(this, highlight);
					repaintNow();
					postPressedEvent();
					setBackColors(this, original);
				} else {
					postPressedEvent();
				}
			}
		}

		private void setBackColors(Container c, int b) {
			c.setBackColor(b);
			for (Control child = c.children; child != null; child = child.next) {
				child.setBackColor(b);
				if (child instanceof Container) {
					setBackColors((Container)child, b);
				}
			}
		}
	}

	/**
	 * @param animDir
	 *          LEFT, RIGHT, TOP, BOTTOM, CENTER
	 */
	public TopMenu(Control[] items, int animDir, byte borderStyle) {
		super(null, borderStyle);
		this.items = items;
		this.animDir = animDir;
		titleGap = 0;
		sameBackgroundColor = fadeOtherWindows = false;
		uiAdjustmentsBasedOnFontHeightIsSupported = false;
		borderColor = UIColors.separatorFore;
		setBackForeColors(UIColors.separatorFore, UIColors.topmenuFore);
		fadeOnPopAndUnpop = true;

		MainWindow.getMainWindow().addTimer(1000);
	}

	/**
	 * @param animDir
	 *          LEFT, RIGHT, TOP, BOTTOM, CENTER
	 */
	public TopMenu(Control[] items, int animDir) {
		this(items, animDir, ROUND_BORDER);
	}

	@Override
	public void popup() {
		setRect(false);
		super.popup();
	}

	protected void setRect(boolean screenResized) {
		int ww = setW = widthInPixels != 0 ? widthInPixels : SCREENSIZE + (percWidth > 0 ? percWidth : 50);
		switch (animDir) {
			case LEFT:
				setRect(0 - ww, TOP, ww, FILL, null, screenResized);
				break;
			case RIGHT:
				setRect(RIGHT + ww, TOP, ww, FILL, null, screenResized);
				break;
			default:
				resetSetPositions(); // required to make sure the height gets updated by the following setRect
				setRect(LEFT, animDir, ww,
						(int) Math.min(Settings.screenHeight * 3 / 4, Settings.screenDensity * items.length * 50), null,
						screenResized);
				break;
		}
		Window.needsPaint = true;
	}

	public double itemHeightFactor = 2;

	public Container header = null;

	@Override
	public void initUI() {
		int gap = 2;
		int n = items.length;
		/*
		 * 48dp height - https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
		 */
		int itemH = 48;
		int prefH = n * itemH + gap * n;
		boolean isLR = animDir == LEFT || animDir == RIGHT;

		if (showElevation && animDir == LEFT) {
			this.transparentBackground = true;
			try {
				ImageControl ic = new ImageControl(new Image("totalcross/res/mat/drawer_shadow.9-hdpi.png"));
				ic.transparentBackground = true;
				ic.strechImage = true;
				ic.scaleToFit = true;
				add(ic, RIGHT, TOP, PREFERRED, FILL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ScrollContainer sc = new ScrollContainer(false, true);
		if (backImage != null) {
			try {
				sc.transparentBackground = true;
				Rect r = getClientRect();
				Image img = backImage.smoothScaledFixedAspectRatio(r.height, true);
				img = img.getClippedInstance(0, 0, r.width, r.height);
				img.alphaMask = backImageAlpha;
				add(new ImageControl(img), LEFT, TOP, FILL, FILL);
			} catch (Throwable t) {
				t.printStackTrace(); // don't add the image
			}
		}
		add(sc, LEFT + scInsets.left, TOP + scInsets.top, (showElevation
				&& animDir == LEFT ? FIT : FILL) - scInsets.right,
			FILL);
		sc.setBackColor(backColor);
		if (header != null) {
			/*
			 * header background aspect ratio of 16:9
			 * https://material.io/guidelines/layout/metrics-keylines.html#metrics-keylines-ratio-keylines
			 */
			sc.add(header, LEFT, TOP, FILL,
				   widthInPixels > 0 ? (widthInPixels * 9 / 16) : (PARENTSIZE + (percWidth * 9 / 16)));
		}
		for (int i = 0; i < n; i++) {
			Control tmi = items[i];
			tmi.appId = i + 1;
			sc.add(tmi, LEFT, AFTER, FILL, tmi instanceof TopMenu.Item ? DP + 48 : PREFERRED);
			if (i == n - 1) {
				break;
			}
			if (drawSeparators) {
				Ruler r = new Ruler(Ruler.HORIZONTAL, false);
				r.setBackColor(backColor);
				if (separatorColor != -1) {
					r.setForeColor(separatorColor);
				}
				sc.add(r, LEFT, AFTER, FILL, gap);
			}
		}
	}

	@Override
	public void onEvent(Event e) {
		switch (e.type) {
			case ControlEvent.PRESSED:
				if (autoClose && e.target != this && ((Control) e.target).isChildOf(this)
					&& !(((Control) e.target).parent instanceof AccordionContainer)) {
					selected = ((Control) e.target).appId - 1;
					postPressedEvent();
					unpop();
				}
				break;
			case PenEvent.PEN_DRAG_END:
				DragEvent de = (DragEvent) e;
				if (sameDirection(animDir, de.direction) && de.xTotal >= width / 2) {
					unpop();
				}
				break;
		}
	}

	private boolean sameDirection(int animDir, int dragDir) {
		if (animDir < 0) {
			animDir = -animDir;
		}
		return (dragDir == DragEvent.LEFT && animDir == LEFT) || (dragDir == DragEvent.RIGHT
				&& animDir == RIGHT)
			   || (dragDir == DragEvent.UP && animDir == TOP) || (dragDir == DragEvent.DOWN && animDir == BOTTOM);
	}

	@Override
	public void screenResized() {
		setRect(true);
		reposition(true);
	}

	@Override
	protected boolean onClickedOutside(PenEvent event) {
		if (event.type == PenEvent.PEN_DOWN) {
			unpop();
		}
		return true;
	}

	/**
	 * Unpops the current TopMenu
	 */
	@Override
	public void unpop() {
		unpop(null);
	}

	/**
	 * Unpops the current TopMenu with the given animation listener. This listener
	 * will be notified when the unpop animation ends.
	 *
	 * @param alist
	 */
	public void unpop(AnimationListener alist) {
		if (currentAnimation != null) {
			return;
		}

		this.alist = alist;
		if (animDir == CENTER) {
			currentAnimation = FadeAnimation.create(this, false, null, totalTime);
		} else {
			currentAnimation = PathAnimation.create(this, -animDir, null, totalTime);
			if (fadeOnPopAndUnpop) {
				currentAnimation.with(FadeAnimation.create(this, false, null, totalTime));
			}
		}
		currentAnimation.setAnimationFinishedAction(new AnimationFinished() {
			@Override
			public void onAnimationFinished(ControlAnimation anim) {
				currentAnimation = null;
				TopMenu.super.unpop();
			}
		});
		currentAnimation.start();
	}

	@Override
	public void postUnpop() {
		super.postUnpop();
		if (alist != null) {
			alist.onAnimationFinished();
		}
	}

	@Override
	public void onPopup() {
		if (currentAnimation != null) {
			return;
		}

		selected = -1;
		if (animDir == CENTER) {
			resetSetPositions();
			setRect(CENTER, CENTER, KEEP, KEEP);
			currentAnimation = FadeAnimation.create(this, true, null, totalTime);
		} else {
			currentAnimation = PathAnimation.create(this, animDir, null, totalTime);
			if (fadeOnPopAndUnpop) {
				currentAnimation.with(FadeAnimation.create(this, true, null, totalTime));
			}
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
	protected void postPopup() {
		super.postPopup();
	}

	public int getSelectedIndex() {
		return selected;
	}

	/** Returns if this control fades in/out on pop/unpop */
	public boolean isFadeOnPopAndUnpop() {
		return fadeOnPopAndUnpop;
	}

	/** sets if this control fades in/out on pop/unpop */
	public void setFadeOnPopAndUnpop(boolean fadeOnPopAndUnpop) {
		this.fadeOnPopAndUnpop = fadeOnPopAndUnpop;
	}
}
