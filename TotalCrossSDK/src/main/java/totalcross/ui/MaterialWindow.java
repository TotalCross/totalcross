package totalcross.ui;

import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.font.Font;
import totalcross.ui.icon.Icon;
import totalcross.ui.icon.MaterialIcons;

/** A window with a top bar + return button supporting slide-in animations. */
public class MaterialWindow extends SlidingWindow {
	private Bar bar;

	protected SideMenuContainer findSideMenu(Container c) {
		if (c instanceof SideMenuContainer) {
			return (SideMenuContainer) c;
		}
		Control[] children = c.getChildren();
		for (Control control : children) {
			if (control instanceof SideMenuContainer) {
				return (SideMenuContainer) control;
			}
			if (control instanceof Container) {
				return findSideMenu((Container) control);
			}
		}
		return null;
	}

	public MaterialWindow(Presenter<Container> provider) {
		this("", false, provider);
	}
	public MaterialWindow(boolean delayInitUI, Presenter<Container> provider) {
		this("", delayInitUI, provider);
	}
	public MaterialWindow(String title, boolean delayInitUI, Presenter<Container> provider) {
		super(delayInitUI, provider);
		Font medium = Font.getFont("Roboto Medium", false, 20);
		Icon i = new Icon(MaterialIcons._ARROW_BACK);

		// cannot use empty constructor for Bar, otherwise we won't be able to use setTitle later
		bar = new Bar(title);
		bar.setFont(medium != null ? medium : Font.getFont(bar.getFont().name, false, 20));
		bar.drawBorders = false;
		bar.backgroundStyle = Container.BACKGROUND_SOLID;

		bar.backColor = 0x4A90E2;
		SideMenuContainer smc = findSideMenu(MainWindow.getMainWindow());
		if (smc != null) {
			Control[] children2 = smc.getChildren();
			for (Control control2 : children2) {
				if (control2 instanceof Bar) {
					bar.setBackForeColors(control2.getBackColor(), control2.getForeColor());
				}
			}
		}

		bar.addButton(i, false);
		bar.addPressListener(
		new PressListener() {
			@Override
			public void controlPressed(ControlEvent e) {
				if (bar.getSelectedIndex() == 1) {
					MaterialWindow.this.unpop();
				}
			}
		});
	}

	public void setBarFont(Font f) {
		bar.setFont(f);
	}

	@Override
	public void setForeColor(int color) {
		bar.setForeColor(color);
	}

	@Override
	public void setBackColor(int color) {
		bar.setBackColor(color);
	}

	@Override
	public void setTitle(String title) {
		if (title != null) {
			bar.setTitle(title);
		}
	}

	public String getTitle() {
		return bar.getTitle();
	}

	public void setIcon(Icon icon) {
		bar.removeButton(1);
		bar.addButton(icon, false);
	}

	public Bar getBar() {
		return this.bar;
	}

	@Override
	public void initUI() {
		add(bar, LEFT, TOP);
		if (!delayInitUI) {
			add(provider.getView(), LEFT, AFTER, FILL, FILL, bar);
		} else {
			delayedUiSpinner = new Spinner();
			add(delayedUiSpinner, CENTER, CENTER);
			delayedUiSpinner.start();
		}
	}

	@Override
	protected void postPopup() {
		super.postPopup();
		if (delayInitUI) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Container view = provider.getView();
					MainWindow.mainWindowInstance.runOnMainThread(new Runnable() {
						@Override
						public void run() {
							add(view, LEFT, AFTER, FILL, FILL, bar);
							remove(delayedUiSpinner);
							delayedUiSpinner.stop();
						}
					});
				}
			}).start();
		}
	}
}
