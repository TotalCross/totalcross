// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sample.components.ui;

import totalcross.io.IOException;
import totalcross.res.Resources;
import totalcross.sample.components.BaseScreen;
import totalcross.sample.util.Colors;
import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.ScrollContainer;
import totalcross.ui.TopMenu;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

public class TopMenuSample extends BaseScreen {
	final int gap = UnitsConverter.toPixels(DP + 20);
	private TopMenu topMenu;

	public TopMenuSample () {
		super("https://totalcross.gitbook.io/playbook/components/side-menu");
	}

	private class FilterContainer extends Container implements PressListener {
		@Override
		public void initUI() {
			super.initUI();
			Label l = new Label("FILTERS", CENTER, Color.WHITE, true);
			l.transparentBackground = true;
			add(l, LEFT, TOP, FILL, PREFERRED);
			add(new ComboBox(new String[] { "Name", "Name 1", "Name 2", "Name 3" }), LEFT + gap, AFTER + gap, FILL - gap,
					PREFERRED);
			add(new Edit(), LEFT, CENTER, PARENTSIZE - gap*2, PREFERRED);
			Button b;
			this.add(b = new Button("Search"), CENTER, AFTER, PREFERRED + fmH * 4, PREFERRED + fmH * 6);
			b.setBackColor(Color.CYAN);
			add(b = new Button("Close"), AFTER + gap, AFTER + gap,  PREFERRED + fmH * 4, PREFERRED + fmH * 6);
			b.setBackColor(Color.RED);
			b.addPressListener(this);
		}

		@Override
		public void controlPressed(ControlEvent e) {
			topMenu.unpop(new TopMenu.AnimationListener() {
				@Override
				public void onAnimationFinished() {
					new MessageBox("Message", "The search was done.").popup();
				}
			});
		}
	}

	@Override
	public void onContent(ScrollContainer content) {
		Control[] items = { new TopMenu.Item("Videocalls", Resources.warning),
				new TopMenu.Item("Insert emoticon", Resources.exit),
				new ComboBox(new String[] { "Smile", "Sad", "Laugh" }), new TopMenu.Item("Add text", Resources.back),
				new TopMenu.Item("See contact", Resources.menu), new TopMenu.Item("Add slide", Resources.warning),
				new TopMenu.Item("Add subject", Resources.exit), new TopMenu.Item("Add persons", Resources.back),
				new TopMenu.Item("Programmed messages", Resources.menu),
				new TopMenu.Item("Add to the phone book", Resources.warning), };

		Label message = new Label("Click on the buttons to show the TopMenu", CENTER);
		message.autoSplit = true;
		content.add(message, LEFT + gap, TOP + gap, FILL - gap, PREFERRED);

		Button[] tmBtn = new Button[4];
		Button[] filterBtn = new Button[2];
		tmBtn[0] = new Button("TOP");
		content.add(tmBtn[0], CENTER, AFTER + gap);
		tmBtn[0].addPressListener(e -> {
			topMenu = new TopMenu(items, TOP);
			topMenu.popup();
		});

		tmBtn[1] = new Button("RIGHT");
		content.add(tmBtn[1], RIGHT - gap, CENTER);
		tmBtn[1].addPressListener(e -> {
			topMenu = new TopMenu(items, RIGHT);
			topMenu.popup();
		});
		filterBtn[0] = new Button("Filter RIGHT");
		content.add(filterBtn[0], RIGHT - gap, AFTER + gap);
		filterBtn[0].addPressListener(e -> {
			topMenu = new TopMenu(new Control[] { new FilterContainer() }, RIGHT);
			topMenu.totalTime = 500;
			topMenu.autoClose = false;
			try {
				topMenu.backImage = new Image("images/back1.jpg");
			} catch (IOException | ImageException e1) {
				e1.printStackTrace();
			}
			topMenu.backImageAlpha = 96;
			topMenu.setFadeOnPopAndUnpop(false);
			topMenu.popup();
		});

		tmBtn[2] = new Button("BOTTOM");
		content.add(tmBtn[2], CENTER, BOTTOM - gap);
		tmBtn[2].addPressListener(e -> {
			topMenu = new TopMenu(items, BOTTOM);
			topMenu.popup();
		});

		tmBtn[3] = new Button("LEFT");
		content.add(tmBtn[3], LEFT + gap, CENTER);
		tmBtn[3].addPressListener(e -> {
			topMenu = new TopMenu(items, LEFT);
			topMenu.popup();
		});
		filterBtn[1] = new Button("Filter LEFT");
		content.add(filterBtn[1], LEFT + gap, AFTER + gap);
		filterBtn[1].addPressListener(e -> {
			topMenu = new TopMenu(new Control[] { new FilterContainer() }, LEFT);
			topMenu.totalTime = 500;
			topMenu.autoClose = false;
			try {
				topMenu.backImage = new Image("images/back1.jpg");
			} catch (IOException | ImageException e1) {
				e1.printStackTrace();
			}
			topMenu.backImageAlpha = 96;
			topMenu.setFadeOnPopAndUnpop(false);
			topMenu.popup();
		});

		for (Button btn : tmBtn)
			btn.setBackForeColors(Colors.P_600, Colors.ON_P_600);
		for (Button btn : filterBtn)
			btn.setBackForeColors(Colors.P_800, Colors.ON_P_800);
	}
}
