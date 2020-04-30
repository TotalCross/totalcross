// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.dialog.keyboard;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Control;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class Builder extends Control {

  public static class EnumTipoBotao {
    public static final EnumTipoBotao BTN_SPOTLIGHT = new EnumTipoBotao("BTN_SPOTLIGHT"),
        BTN_REGULAR = new EnumTipoBotao("BTN_REGULAR"),
        BTN_SECUNDARY = new EnumTipoBotao("BTN_SECUNDARY"),
        BTN_ERROR = new EnumTipoBotao("BTN_ERROR"),
        BTN_FORM = new EnumTipoBotao("BTN_FORM");

    private final String name;

    private EnumTipoBotao(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static final EnumTipoBotao BTN_FORM = EnumTipoBotao.BTN_FORM;
  public static final EnumTipoBotao BTN_SPOTLIGHT = EnumTipoBotao.BTN_SPOTLIGHT;
  public static final EnumTipoBotao BTN_REGULAR = EnumTipoBotao.BTN_REGULAR;
  public static final EnumTipoBotao BTN_SECUNDARY = EnumTipoBotao.BTN_SECUNDARY;
  public static final EnumTipoBotao BTN_ERROR = EnumTipoBotao.BTN_ERROR;

  public static int TEXT_HIGHLIGHT_COLOR = 0x049CEE; // Pode ser sobreescrito
  // o valor de destaque
  // do texto
  public static int TEXT_REQUIRED_COLOR = Color.RED;

  public static int APP_FORE_COLOR;

  public static int APP_BACK_COLOR;

  public static int APP_SEC_FORE_COLOR;

  public static int APP_BUTTON_COLOR;

  public static final int SPACE_TO_BORDER = 10;

  public static void setDefaultColor(
      int APP_FORE_COLOR, int APP_BACK_COLOR, int APP_SEC_FORE_COLOR, int APP_BUTTON_COLOR) {

    Builder.APP_FORE_COLOR = APP_FORE_COLOR;
    Builder.APP_BACK_COLOR = APP_BACK_COLOR;
    Builder.APP_SEC_FORE_COLOR = APP_SEC_FORE_COLOR;
    Builder.APP_BUTTON_COLOR = APP_BUTTON_COLOR;
  }

  public static Button createButton(
      String text, Image img, int textPosition, int gap, EnumTipoBotao status) {

    Button button = new Button(text, img, textPosition, gap);

    if (BTN_REGULAR == status) {
      button.setFont(button.getFont().asBold());
      button.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
    } else if (BTN_SPOTLIGHT == status) {
      button.setFont(button.getFont().asBold());
      button.setBackForeColors(Color.getRGB(98, 190, 64), Color.WHITE);
    } else if (BTN_FORM == status) {
      button.setFont(button.getFont().asBold());
      button.setBackForeColors(APP_BACK_COLOR, APP_FORE_COLOR);
    } else {
      button.setFont(button.getFont().asBold());
      button.setBackForeColors(Color.getRGB(98, 190, 64), Color.WHITE);
    }

    button.addPressListener(
        new PressListener() {

          @Override
          public void controlPressed(ControlEvent event) {
            if (event.type == ControlEvent.PRESSED
                && (Settings.platform.equals(Settings.ANDROID))) {
              Vm.vibrate(15);
            }
          }
        });

    return button;
  }

  /** Creates a regular Button */
  public static Button createButton(String text) {
    return Builder.createButton(text, null, 0, 0, BTN_REGULAR);
  }

  /** Creates a button displaying the given text. */
  public static Button createButton(String text, EnumTipoBotao status) {
    return Builder.createButton(text, null, 0, 0, status);
  }

  public static Button createButton(String msg, Image img) {
    return createButton(msg, img, RIGHT, (int) (Settings.screenWidth * 0.01), BTN_REGULAR);
  }
}
