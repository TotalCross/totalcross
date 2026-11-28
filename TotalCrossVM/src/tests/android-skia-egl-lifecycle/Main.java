// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** API 30 graphics lifecycle probe. Keep existing image owners across EGL changes. */
public class Main extends MainWindow implements PressListener {
  private Image sharedImage;
  private Image retainedImage;
  private Image newlyCreatedImage;

  public Main() {
    super("", Window.NO_BORDER);
    setDeviceTitle("Skia EGL Probe");
    Settings.windowSize = Settings.WINDOWSIZE_600X800;
  }

  @Override
  public void initUI() {
    super.initUI();
    setBackColor(Color.WHITE);

    sharedImage = colorTile(Color.RED, Color.YELLOW);
    retainedImage = colorTile(Color.BLUE, Color.WHITE);
    add(new ImageControl(sharedImage), LEFT + 24, TOP + 64, 112, 112);
    add(new ImageControl(sharedImage), LEFT + 152, TOP + 64, 112, 112);
    add(new ImageControl(retainedImage), LEFT + 280, TOP + 64, 112, 112);

    ImageControl control = new ImageControl(colorTile(Color.GREEN, Color.MAGENTA));
    add(control, LEFT + 24, TOP + 208, 112, 112);

    Button createImage = new Button("Create image after recreation");
    createImage.addPressListener(this);
    add(createImage, LEFT + 24, TOP + 352, 344, 56);
    Vm.debug("TC_SKIA_PROBE READY shared=2 retained=1 content=red-yellow-blue-green");
  }

  @Override
  public void controlPressed(ControlEvent event) {
    if (newlyCreatedImage != null) {
      return;
    }
    newlyCreatedImage = colorTile(Color.CYAN, Color.BLACK);
    add(new ImageControl(newlyCreatedImage), LEFT + 152, TOP + 208, 112, 112);
    Vm.debug("TC_SKIA_PROBE NEW_IMAGE_CREATED");
  }

  private static Image colorTile(int outer, int inner) {
    try {
      Image image = new Image(96, 96);
      Graphics graphics = image.getGraphics();
      graphics.backColor = outer;
      graphics.fillRect(0, 0, 96, 96);
      graphics.backColor = inner;
      graphics.fillRect(24, 24, 48, 48);
      return image;
    } catch (ImageException error) {
      throw new RuntimeException("Could not create probe image", error);
    }
  }
}
